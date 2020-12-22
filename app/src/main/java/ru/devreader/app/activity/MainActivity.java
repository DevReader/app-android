// ! rx1310 <rx1310@inbox.ru> | Copyright (c) DevReader, 2020 | MIT License

package ru.devreader.app.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;

import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import ru.devreader.app.R;
import ru.devreader.app.activity.MainActivity;
import ru.devreader.app.util.AppUtils;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
	
	// ? Страница, которая будет загружена в WebView
	final String loadUrl = "https://" + "devreader.github.io" + "/";
	
	WebView mWebView;
	
	boolean dbg_javaScript = true, dbg_appCache = true;
	
	SharedPreferences mSharedPrefs;
	SharedPreferences.Editor mSharedPrefsEditor;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		mSharedPrefsEditor = mSharedPrefs.edit();
		
		initWebView();
		
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
	
	void initWebView() {

		AppUtils.Log(this, "d", "Init WebView");

		mWebView = findViewById(R.id.el_webView);

		WebSettings WebViewSettings = mWebView.getSettings();
		WebViewSettings.setDefaultTextEncodingName("utf-8");

		if (dbg_javaScript) {
			AppUtils.Log(this, "d", "dbg.javaScript: " + dbg_javaScript);
			mWebView.getSettings().setJavaScriptEnabled(true);
			mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
		}

		if (dbg_appCache) {
			AppUtils.Log(this, "d", "dbg.appCache: " + dbg_appCache);
			mWebView.getSettings().setAppCacheEnabled(true);
			mWebView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
		}

		mWebView.setBackgroundColor(Color.parseColor("#121212"));
		mWebView.getSettings().setDomStorageEnabled(true);
		mWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);

		mWebView.setWebViewClient(new WebViewClient() {

				public boolean shouldOverrideUrlLoading(WebView webView, String url) {

					String urlPrefix = loadUrl;

					if (url != null && url.startsWith(urlPrefix)){
						return false;
					} else {
						webView.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
						return true;
					}

				}

				@SuppressWarnings("deprecation")
				@Override
				public void onReceivedError(WebView webView, int errCode, String errDesc, String failingUrl) {

					String log = "code: " + errCode + "\ndesc: " + errDesc + "\nurl: " + failingUrl;
					AppUtils.Log(MainActivity.this, "e", log);

				}

			});

		mWebView.setWebChromeClient(new WebChromeClient() {

				public void onProgressChanged(WebView webView, int nProgress) {

					AppUtils.Log(MainActivity.this, "i", "onProgressChanged");

					if (nProgress < 100) {

						AppUtils.Log(MainActivity.this, "i", "nProgress < 100");
						mWebView.setVisibility(View.GONE);

					} else if (nProgress == 100) {

						AppUtils.Log(MainActivity.this, "i", "nProgress == 100");

						mWebView.setVisibility(View.VISIBLE);

					} else {

						AppUtils.Log(MainActivity.this, "i", "nProgress / else");
						mWebView.setVisibility(View.GONE);

					}

				}


				@Override
				public void onReceivedTitle(WebView webView, String pageTitle) {
					super.onReceivedTitle(webView, pageTitle);
					if (!TextUtils.isEmpty(pageTitle)) {
						//setTitle(pageTitle);
					}
				}

			});

		//mWebView.loadUrl("file:///android_asset/" + "index.html");
		mWebView.loadUrl(loadUrl);
		AppUtils.Log(MainActivity.this, "i", "WebView load url: " + loadUrl);

	}
	
}
