package com.s0l.tweetpulse;

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import org.osmdroid.ResourceProxy;
import org.osmdroid.api.IMapView;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedOverlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.OverlayItem.HotspotPlace;

import java.util.ArrayList;

public class MyItemizedOverlay extends ItemizedOverlay<OverlayItem>
{
//----------------------------OSM======================
	private /*volatile*/ ArrayList<OverlayItem> mOverlaysOsm = new ArrayList<OverlayItem>();
	private Context mContext = null;
    private Bitmap px1 = null;
    private Bitmap px3 = null;
    private Bitmap px6 = null;

	public MyItemizedOverlay(Drawable pDefaultMarker, ResourceProxy pResourceProxy)
	{
		super(pDefaultMarker, pResourceProxy);
	}
    public void setContext(Context mContext) {
        this.mContext = mContext;
        px1 = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.t1px);
        px3 = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.t3px);
        px6 = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.t6px);
    }

	public synchronized void addItem(GeoPoint p, String title, String snippet)
	{
//      synchronized (mOverlaysOsm )
        {
		    OverlayItem newItem = new OverlayItem(title, snippet, p);
		    newItem.setMarkerHotspot(HotspotPlace.CENTER);
            mOverlaysOsm.add(newItem);
            populate();
        }
	}

    @Override
    public synchronized void draw(Canvas canvas, MapView mapView, boolean shadow) {
        if(!shadow)
        {
            int zoom = mapView.getZoomLevel();
            int size = size();
            for (int ctr = 0; ctr < size; ctr++)
            {
                GeoPoint in = createItem(ctr).getPoint();
                Point out = new Point();
                mapView.getProjection().toPixels(in, out);
                Rect currentMapBoundsRect = new Rect();
                mapView.getScreenRect(currentMapBoundsRect);
                if(currentMapBoundsRect.contains(out.x, out.y)){ //draw markers only in screen rect - must improve speed
                    if(zoom < 6)
                        canvas.drawBitmap(px1,
                                out.x - px1.getWidth()/2,  //shift the bitmap center
                                out.y - px1.getHeight()/2,  //shift the bitmap center
                                null);
                    else if(zoom >= 6 && zoom < 10)
                        canvas.drawBitmap(px3,
                                out.x - px3.getWidth()/2,  //shift the bitmap center
                                out.y - px3.getHeight()/2,  //shift the bitmap center
                                null);
                    else
                        canvas.drawBitmap(px6,
                                out.x - px6.getWidth()/2,  //shift the bitmap center
                                out.y - px6.getHeight()/2,  //shift the bitmap center
                                null);
                }
            }
        }
        else
            super.draw(canvas, mapView, shadow);
    }
    @Override
	protected synchronized OverlayItem createItem(int arg0)
	{
//      synchronized (mOverlaysOsm )
        {
		    return mOverlaysOsm.get(arg0);
        }
	}

	public synchronized boolean onSnapToItem(int arg0, int arg1, Point arg2, IMapView arg3)
	{
//      synchronized (mOverlaysOsm )
        {
		  return false;
        }
	}

	public synchronized void removeItem(int index)
    {
//      synchronized (mOverlaysOsm )
        {
            mOverlaysOsm.remove(index);
            populate();
        }
    }

	@Override
	public synchronized int size()
	{
        if (mOverlaysOsm != null)
            return mOverlaysOsm.size();
        else
            return 0;
	}
}