package com.s0l.tweetpulse;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.util.Log;

public class TwitterPulseSettingsActivity extends PreferenceActivity implements Preference.OnPreferenceChangeListener
{
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle icicle)
	{
		  super.onCreate(icicle);
		  addPreferencesFromResource(R.xml.twitterpulsesettings);
//		  getListView().setBackgroundColor(0x1f000000);
	}

	@Override
	protected void onResume() {
	      super.onResume();
	        refresh();
	}

	@Override
	public boolean onPreferenceChange(Preference arg0, Object arg1) {
		return true;
	}

    @SuppressWarnings("deprecation")
    private void refresh()
    {
    	Preference pref = findPreference(getString(R.string.twittpulse_settings_about_key));

        String versionName = getVersionName(this);
        int versionNumber = getVersionCode(this);
        pref.setSummary(getString(R.string.twittpulse_version_name) + " " + versionName + " (" +getString(R.string.twittpulse_version_code)+ " " + String.valueOf(versionNumber) + ")" + "\n" + getString(R.string.twittpulse_version_author));
    }
    /**
     * Gets version code of given application.
     *
     * @param context
     * @return
     */
    public int getVersionCode(Context context) {
        PackageInfo pinfo;
        try {
            pinfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            int versionNumber = pinfo.versionCode;
            return versionNumber;
        } catch (NameNotFoundException e) {
            Log.e(context.getApplicationInfo().name, getString(R.string.twittpulse_version_nocode));
        }
        return 0;
    }

    /**
     * Gets version name of given application.
     *
     * @param context
     * @return
     */
    public String getVersionName(Context context) {
        PackageInfo pinfo;
        try {
            pinfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            String versionName = pinfo.versionName;
            return versionName;
        } catch (NameNotFoundException e) {
            Log.e(context.getApplicationInfo().name, getString(R.string.twittpulse_version_noname));
        }
        return null;
    }
}
