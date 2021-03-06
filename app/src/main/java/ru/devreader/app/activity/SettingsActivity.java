package ru.devreader.app.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ListView;
import android.os.Build;
import android.app.ActivityManager;
import android.support.v4.os.BuildCompat;
import android.view.MenuItem;
import android.view.Menu;

import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;

import ru.devreader.app.R;
import ru.devreader.app.task.OTACheckTask;
import ru.devreader.app.util.AppUtils;
import ru.devreader.app.util.SysUtils;
import android.widget.BaseAdapter;

public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

	ListView mListView;
	
	Preference uiFabScroll;
	Preference moreInstallInA2IGA;
	Preference infoAppVersion, infoAppInstallPromt;
	Preference otaCheck;
	Preference dbgInstallationInfo;
	
	String ota_lastCheckDate;
	
	SharedPreferences mSharedPrefs;
	SharedPreferences.Editor mSharedPrefsEditor;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.app_settings);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		mSharedPrefs.registerOnSharedPreferenceChangeListener(this);
		mSharedPrefsEditor = mSharedPrefs.edit();
		
		uiFabScroll = findPreference("ui.fabScroll");
		
		if (Build.VERSION.SDK_INT <= 23) {
			uiFabScroll.setEnabled(false);
		}
		
		ota_lastCheckDate = mSharedPrefs.getString("ota.lastCheckDate", getString(android.R.string.untitled));
		
		mListView = findViewById(android.R.id.list);
		mListView.setDivider(null);

		moreInstallInA2IGA = findPreference("more.installInA2IGA");
		moreInstallInA2IGA.setSummary(a2igaInstalledStatus());
		
		otaCheck = findPreference("ota.check");
		otaCheck.setSummary(String.format(getString(R.string.pref_ota_check_summary), ota_lastCheckDate));
		
		// ! Debug
		dbgInstallationInfo = findPreference("dbg.installationInfo");
		dbgInstallationInfo.setSummary("— Package name: " + getPackageName() +
									   "\n— Beta build status: " + AppUtils.getVersionName(this, getPackageName()).contains("beta") +
									   "\n— A2IGA install status: " + AppUtils.isAppInstalled(this, "ru.rx1310.app.a2iga") +
									   "\n— Device: " + Build.DEVICE + " (" + Build.MODEL + ") / " + Build.PRODUCT +
									   "\n— Brand: " + Build.BRAND + " (" + Build.MANUFACTURER + ")" +
									   "\n— Fingerprint: " + Build.FINGERPRINT +
									   "\n— Detect MIUI: " + SysUtils.isMIUI());
		
		// ! Info
		infoAppVersion = findPreference("info.appVersion");
		infoAppVersion.setSummary(AppUtils.getVersionName(this, getPackageName()) + " (" + AppUtils.getVersionCode(this, getPackageName()) + ")");
		
		infoAppInstallPromt = findPreference("info.appInstallPromt");
		infoAppInstallPromt.setSummary(String.format(getString(R.string.pref_info_app_promt), AppUtils.getInstallDate(this, getPackageName(), false, false), AppUtils.getInstallDate(this, getPackageName(), true, false)));
	}
	
	public boolean onPreferenceTreeClick(PreferenceScreen prefScreen, Preference pref) {

		switch (pref.getKey()) {
			
			case "dbg.firstStartPrefReset":
				mSharedPrefsEditor.putBoolean("isFirstStart", true);
				mSharedPrefsEditor.commit();
				AppUtils.showToast(this, "Success!");
				AppUtils.Log(this, "d", "[firstStartPrefReset] isFirstStart = true");
				break;
				
			case "dbg.appCacheDelete":
				((ActivityManager) this.getSystemService(ACTIVITY_SERVICE)).clearApplicationUserData();
				break;

			case "more.installInA2IGA":
				installInA2IGA();
				break;
				
			case "ota.check":
				// ? Проверка обновлений
				OTACheckTask.checkUpdates(this, true);
				break;
				
			case "info.appVersion":
				AppUtils.openURL(this, "https://devreader.github.io/devreader");
				break;
				
			case "info.appSourceCode":
				AppUtils.openURL(this, "https://github.com/devreader/app-android");
				break;
				
			case "info.appDev":
				AppUtils.openURL(this, "https://t.me/rx1310_dev");
				break;
				
			case "info.tgChannel":
				AppUtils.openURL(this, "https://t.me/devreader");
				break;
				
			default: break;

		}

		return super.onPreferenceTreeClick(prefScreen, pref);

	}
	
	@Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPrefs, String key) {

		/*if (key.equals("")) {
            //
        }*/

	}

	// ? Статус установки A2IGA
	String a2igaInstalledStatus() {

		if (AppUtils.isAppInstalled(this, "ru.rx1310.app.a2iga")) {
			return getString(R.string.pref_more_a2iga_summary);
		} else {
			return getString(R.string.pref_more_a2iga_summary) + "\n\n" + getString(R.string.pref_more_a2iga_not_found_summary);
		}

	}

	// ? Установка A2IGA
	void installInA2IGA() {

		if (AppUtils.isAppInstalled(this, "ru.rx1310.app.a2iga")) {

			Intent sendPackageName = new Intent();
			sendPackageName.setAction(Intent.ACTION_SEND);
			sendPackageName.putExtra(Intent.EXTRA_TEXT, getPackageName());
			sendPackageName.setType("text/plain");
			startActivity(Intent.createChooser(sendPackageName, getString(R.string.pref_more_a2iga_chooser_title)));

		} else {
			AppUtils.openURL(this, "https://github.com/rx1310/a2iga/releases/latest");
		}

	}

}
