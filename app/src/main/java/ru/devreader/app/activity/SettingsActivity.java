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
import ru.devreader.app.util.AppUtils;
import ru.devreader.app.task.OTACheckTask;

public class SettingsActivity extends PreferenceActivity {

	ListView mListView;
	
	Preference installInA2IGA;
	
	SharedPreferences mSharedPreferences;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.app_settings);

		mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

		getActionBar().setDisplayHomeAsUpEnabled(true);

		mListView = findViewById(android.R.id.list);
		mListView.setDivider(null);

		installInA2IGA = findPreference("installInA2IGA");
		installInA2IGA.setSummary(a2igaInstalledStatus());
		
	}

	public boolean onPreferenceTreeClick(PreferenceScreen prefScreen, Preference pref) {

		switch (pref.getKey()) {

			case "installInA2IGA":
				installInA2IGA();
				break;
				
			case "ota.check":
				if (AppUtils.getVersionName(this, getPackageName()).contains("beta")) {
					OTACheckTask.checkUpdates(this, true);
				} else {
					OTACheckTask.checkUpdates(this, false);
				}
				break;

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
			AppUtils.openURL(this, "https://github.com/rx1310/a2iga/releases");
		}

	}

}
