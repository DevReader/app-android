// ! rx1310 <rx1310@inbox.ru> | Copyright (c) DevReader, 2020 | MIT License

package ru.devreader.app.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;

import android.view.Display;
import android.view.KeyEvent;
import android.view.View;

import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import ru.devreader.app.R;
import ru.devreader.app.activity.MainActivity;
import ru.devreader.app.util.AppUtils;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
	
	// ? Страница, которая будет загружена в WebView
	final String loadUrl = "https://" + "devreader.github.io" + "/";
	
	Toolbar mToolbar;
	
	SharedPreferences mSharedPrefs;
	SharedPreferences.Editor mSharedPrefsEditor;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		mSharedPrefsEditor = mSharedPrefs.edit();

		initToolbar();
		
	}
	
	void initToolbar() {

		AppUtils.Log(this, "d", "Init Toolbar");

		mToolbar = findViewById(R.id.uiToolbar);

		setSupportActionBar(mToolbar); 

	}
	
	@Override
	public void onClick(View v) {

		switch (v.getId()) {
			
			//case R.id.*:
				//
				//break;

			default: break;

		}

	}
	
}
