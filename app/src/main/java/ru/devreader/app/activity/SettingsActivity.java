package ru.devreader.app.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ListView;

import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;

import ru.devreader.app.R;
import ru.devreader.app.task.OTACheckTask;
import ru.devreader.app.util.AppUtils;
import android.os.Build;
import android.app.ActivityManager;

public class SettingsActivity extends PreferenceActivity {

	ListView mListView;
	
	Preference moreInstallInA2IGA, infoAppVersion, infoAppInstallPromt;
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
		mSharedPrefsEditor = mSharedPrefs.edit();
		
		ota_lastCheckDate = mSharedPrefs.getString("ota.lastCheckDate", getString(android.R.string.untitled));
		
		mListView = findViewById(android.R.id.list);
		mListView.setDivider(null);

		moreInstallInA2IGA = findPreference("more.installInA2IGA");
		moreInstallInA2IGA.setSummary(a2igaInstalledStatus());
		
		otaCheck = findPreference("ota.check");
		otaCheck.setSummary(String.format(getString(R.string.pref_ota_check_summary), ota_lastCheckDate));
		
		// ! Debug
		dbgInstallationInfo = findPreference("dbg.installationInfo");
		dbgInstallationInfo.setSummary("Install date: " + AppUtils.getInstallDate(this, getPackageName(), false, false) + 
									   "\nLast update date: " + AppUtils.getInstallDate(this, getPackageName(), true, false) +
									   "\nPackage name: " + getPackageName() +
									   "\nBeta build status: " + AppUtils.getVersionName(this, getPackageName()).contains("beta") +
									   "\nA2IGA install status: " + AppUtils.isAppInstalled(this, "ru.rx1310.app.a2iga"));
		
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
				
			default: break;

		}

		return super.onPreferenceTreeClick(prefScreen, pref);

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
