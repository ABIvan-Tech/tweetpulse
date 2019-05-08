package com.s0l.tweetpulse;

import android.content.Context;
import android.net.ConnectivityManager;
import android.widget.TextView;

import com.s0l.tweetpulse.MyItemizedOverlay;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.OverlayManager;

import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import android.os.Handler;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.service.dreams.DreamService;
import android.util.Log;

import java.util.ArrayList;

//userLookup:"//api.twitter.com/1/users/lookup.json https://dev.twitter.com/docs/api/1.1/get/users/lookup
//userShow:"//cdn.api.twitter.com/1/users/show.json" https://dev.twitter.com/docs/api/1.1/get/users/show
//status:"//cdn.api.twitter.com/1/statuses/show.json" https://dev.twitter.com/docs/api/1.1/get/statuses/show/%3Aid
//tweets:"//syndication.twimg.com/tweets.json"
//count:"//cdn.api.twitter.com/1/urls/count.json"
//friendship:"//cdn.api.twitter.com/1/friendships/exists.json"
//timeline:"//cdn.syndication.twimg.com/widgets/timelines/"
//timelinePoll:"//syndication.twimg.com/widgets/timelines/paged/"
//timelinePreview:"//syndication.twimg.com/widgets/timelines/preview/"	

/**
 * Plays a delightful show of colors.
 * <p>
 * This dream performs its rendering using OpenGL on a separate rendering thread.
 * </p>
 */
public class TwitterPulseService extends DreamService {

    static final String TAG = TwitterPulseService.class.getSimpleName();

    static final boolean DEBUG = false;

    private boolean mNightMode = false;

    static MapView mapView = null;

    static MapController mMapController = null;

    static OverlayManager mOverlayManager = null;

    static ResourceProxy mResourceProxy = null;

    private static ArrayList<MyItemizedOverlay> EventsInMap = new ArrayList<MyItemizedOverlay>();

    private static MyItemizedOverlay mOverlay = null;

    private static TwitterStream twitterStream = null;

    private final Handler mHandler1 = new Handler();

    //    private final Handler mHandler2 = new Handler();
    private volatile long countOfTweets = 0;

    static TextView TwittsText = null;

    static TextView CountOfTweetsText = null;

    private /*volatile*/ String twett = "";

    boolean isHaveWiFi = false;

    private String data = "";

    private static boolean statisic = true;

    private OnlineTileSourceBase[] maptype = {TileSourceFactory.MAPNIK, TileSourceFactory.HILLS,
            TileSourceFactory.MAPQUESTOSM};

    StatusListener listener = new StatusListener() {
        @Override
        public void onStatus(Status status) {
            if (status.getGeoLocation() != null) {
//        		Log.d(TAG, "onStatus "+/*"@" + status.getUser().getScreenName() + /*" - " + status.getText() + */status.getGeoLocation().toString());
//            	GeoPoint gp = new GeoPoint((int) (status.getGeoLocation().getLatitude() * 1E6),
//        				(int) (status.getGeoLocation().getLongitude() * 1E6));
                //Im change lat and lon - coz its bug in Twitter4J
                GeoPoint gp = new GeoPoint((int) (status.getGeoLocation().getLongitude() * 1E6),
                        (int) (status.getGeoLocation().getLatitude() * 1E6));
                mOverlay.addItem(gp, "", "");

                EventsInMap.add(mOverlay);
                countOfTweets++;
                twett = "@" + status.getUser().getScreenName() + " - " + status.getText();
                if (statisic) {
                    CountOfTweetsText.post(new Runnable() {
                        public void run() {
                            CountOfTweetsText.setText(
                                    getString(R.string.twittpulse_data_type) + " " + data + ": "
                                            + getString(R.string.twittpulse_twitt_count) + " "
                                            + String.valueOf(countOfTweets));
                        }
                    });
                    TwittsText.post(new Runnable() {
                        public void run() {
                            TwittsText.setText(twett);
                        }
                    });
                }
//        		handler.sendEmptyMessage(0);
            }
        }

        @Override
        public void onScrubGeo(long userId, long upToStatusId) {
//                Log.d(TAG, "Got scrub_geo event userId:" + userId + " upToStatusId:" + upToStatusId);
        }

        @Override
        public void onException(Exception ex) {
            ex.printStackTrace();
        }

        @Override
        public void onDeletionNotice(StatusDeletionNotice arg0) {
        }

        @Override
        public void onStallWarning(StallWarning arg0) {
        }

        @Override
        public void onTrackLimitationNotice(int arg0) {
        }
    };

    Runnable mTimer1 = new Runnable() {
        public void run() {
            try {
                if (countOfTweets > 0) {
                    mapView.invalidate();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            ;
            mHandler1.postDelayed(this, 240);
        }
    };

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
// Exit dream upon user touch
        mNightMode = isPrefEnabled(this.getString(R.string.twittpulse_settings_night_key), true);
        setInteractive(true);
// Hide system UI
        setFullscreen(true);
        setScreenBright(mNightMode);
// Set the dream layout
        setContentView(R.layout.twittpulselayout);
        TwittsText = (TextView) findViewById(R.id.TwittsText);
        TwittsText.setEnabled(false);
        CountOfTweetsText = (TextView) findViewById(R.id.CountOfTwettsText);
        CountOfTweetsText.setEnabled(false);

        statisic = isPrefEnabled(this.getString(R.string.twittpulse_settings_statistics_key), true);

        mapView = (MapView) findViewById(R.id.omapview);
        if (mapView != null) {
            mapView.setUseSafeCanvas(true);
            // Turn off hardware acceleration here, or in manifest
            int map = Integer.parseInt(
                    isPrefEnabled(this.getString(R.string.twittpulse_settings_map_type_key), "0"));
            mapView.setTileSource(maptype[map]); //MAPNIK MAPQUESTOSM
            mapView.setMultiTouchControls(
                    isPrefEnabled(this.getString(R.string.twittpulse_settings_map_multitouch_key),
                            false));
            mapView.setBuiltInZoomControls(
                    isPrefEnabled(this.getString(R.string.twittpulse_settings_map_multitouch_key),
                            false));
            mapView.setEnabled(
                    isPrefEnabled(this.getString(R.string.twittpulse_settings_map_multitouch_key),
                            false));
//          mapView.setUseDataConnection(true);
            mMapController = mapView.getController();
            mOverlayManager = mapView.getOverlayManager();
            mMapController.setZoom(2);
            mResourceProxy = new DefaultResourceProxyImpl(this);
            mOverlay = new MyItemizedOverlay(this.getResources().getDrawable(R.drawable.t1px),
                    mResourceProxy);
            mOverlay.setContext(this);
//	        mOverlay.addItem(new GeoPoint(0,0), "", "");
            mOverlayManager.clear();
            mOverlayManager.add(mOverlay);
        }
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        try {
            ConfigurationBuilder cb = new ConfigurationBuilder();
            cb.setDebugEnabled(true);
            cb.setOAuthConsumerKey("");//removed by security :)
            cb.setOAuthConsumerSecret("");//removed by security :)
            cb.setOAuthAccessToken("-");//removed by security :)
            cb.setOAuthAccessTokenSecret("");//removed by security :)
            twitterStream = new TwitterStreamFactory(cb.build()).getInstance();
            //add location filter for what I hope is the whole planet. Just trying to limit results to only things that are geotagged
            FilterQuery locationFilter = new FilterQuery();
            double[][] locations = {{-180.0d, -90.0d}, {180.0d, 90.0d}};
            locationFilter.locations(locations);
            twitterStream.addListener(listener);
            twitterStream.filter(locationFilter);
            ConnectivityManager manager = (ConnectivityManager) getSystemService(
                    Context.CONNECTIVITY_SERVICE);
            isHaveWiFi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
                    .isConnectedOrConnecting();
            data = isHaveWiFi == true ? "Wi-Fi" : "Mobile";
            if (!isHaveWiFi) {
                twitterStream.sample();
                Log.d(TAG, "isHaveWiFi: " + isHaveWiFi);
            }
//          Log.d(TAG, "twitterStream:" + twitterStream.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        mHandler1.postDelayed(mTimer1, 1500);
//        mHandler2.postDelayed(mTimer2, 1500);
    }

    //Use this for initial setup, such as calling setContentView().
    @Override
    public void onDreamingStarted() {
        super.onDreamingStarted();
        Log.d(TAG, "onDreamingStarted Service Created");
    }

    //Your dream has started, so you should begin animations or other behaviors here.
    public void onDreamingStopped() {
        super.onDreamingStopped();

        Log.d(TAG, "onDreamingStopped Service Stopped");
    }

    //Use this to stop the things you started in onDreamingStarted().
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        new Thread() {
            @Override
            public void run() {
                try {
                    twitterStream.cleanUp();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    twitterStream.shutdown();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public boolean isPrefEnabled(String prefName, boolean defValue) {
        return PreferenceManager.getDefaultSharedPreferences(this).getBoolean(prefName, defValue);
    }

    public String isPrefEnabled(String prefName, String defValue) {
        return PreferenceManager.getDefaultSharedPreferences(this).getString(prefName, defValue);
    }
}
