// ! rx1310 <rx1310@inbox.ru> | Copyright (c) DevReader, 2020 | MIT License

package ru.devreader.app.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Base64;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;

import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;

import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.InputStream;

import ru.devreader.app.R;
import ru.devreader.app.activity.MainActivity;
import ru.devreader.app.task.OTACheckTask;
import ru.devreader.app.util.AppUtils;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
	
	// ? Страница, которая будет загружена в WebView
	final String loadUrl = "file:///android_asset/" + "test/index.html";
	// final String loadUrl = "https://" + "devreader.github.io" + "/";
	
	WebView mWebView;
	FloatingActionButton mFabBack, mFabHome;
	BottomSheetDialog mDialogMenu;
	LinearLayout mLoadingDummy, mErrorDummy;
	
	boolean dbg_javaScript, 
			dbg_webViewCache, 
			dbg_injectJs, 
			dbg_shouldOverrideUrlLoadingV2,
			dbg_showLoadUrl;
			
	boolean isPageLoadError = false;
	boolean isFirstStart;
	boolean isFabAlphaEnabled, isOtaAutoCheckEnabled;
	
	float fabAlphaValue = (float) 0.6;
	
	SharedPreferences mSharedPrefs;
	SharedPreferences.Editor mSharedPrefsEditor;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		mSharedPrefsEditor = mSharedPrefs.edit();
		
		// ? Prefs
		isFabAlphaEnabled = mSharedPrefs.getBoolean("ui.fabAlpha", false);
		
		// ? Запуск WebView
		initWebView();
		
		// ? WebView
		mWebView = findViewById(R.id.el_webView);
		
		// ? Заглушка при загрузке страницы
		mLoadingDummy = findViewById(R.id.el_dummyLoading);
		
		mErrorDummy = findViewById(R.id.el_dummyError_m2);
		
		// ? Настройка FAB Back&Reload
		mFabBack = findViewById(R.id.el_fabBack);
		mFabBack.setOnClickListener(this);
		mFabBack.setOnLongClickListener(new View.OnLongClickListener() {
			public boolean onLongClick(View mView) {
				return true;
			}
		});
		
		// ? Настройка FAB Home
		mFabHome = findViewById(R.id.el_fabHome);
		mFabHome.setOnClickListener(this);
		mFabHome.setOnLongClickListener(new View.OnLongClickListener() {
			public boolean onLongClick(View mView) {
				startActivity(new Intent(MainActivity.this, SettingsActivity.class));
				return true;
			}
		});
		
	}

	@Override
	public void onClick(View mView) {

		switch (mView.getId()) {
			
			case R.id.el_fabBack:
				mWebView.goBack();
				break;
				
			case R.id.el_fabHome:
				mWebView.loadUrl(loadUrl);
				AppUtils.Log(MainActivity.this, "d", "mWebViewPageHome (homepage: " + loadUrl + ")");
				break;

			default: break;

		}

	}

	@Override
	protected void onResume() {
		super.onResume();
		
		// ? Prefs
		isFirstStart = mSharedPrefs.getBoolean("isFirstStart", true);
		isOtaAutoCheckEnabled = mSharedPrefs.getBoolean("ota.checkAuto", false);
		
		// ? Отображаю приветствие при первом запуске
		if (isFirstStart) {
			initFirstStartMessage();
		}
		
		// ? Настройка прозрачности fab'ов
		if (isFabAlphaEnabled) {
			mFabHome.setAlpha(fabAlphaValue);
			mFabBack.setAlpha(fabAlphaValue);
		}
		
		// ? Проверка обновлений
		if (isOtaAutoCheckEnabled) {
			if (AppUtils.getVersionName(this, getPackageName()).contains("beta")) {
				OTACheckTask.checkUpdates(this, true, false);
			} else {
				OTACheckTask.checkUpdates(this, false, false);
			}
		}
		
	}
	
	// ? Сообщение при первом запуске
	void initFirstStartMessage() {
		
		// ? Сохраняю настройку, которая теперь знает,
		// что первый запуск уже был
		mSharedPrefsEditor.putBoolean("isFirstStart", false);
		mSharedPrefsEditor.commit();
		
		// ? Отображаю диалог
		mDialogMenu = new BottomSheetDialog(MainActivity.this);
		View mDialogView = getLayoutInflater().inflate(R.layout.sheet_welcome, null);
		mDialogMenu.setContentView(mDialogView);
		mDialogMenu.show(); // ? Ну и показываю. Нмче нового не открыл бл.
		
	}
	
	// ? Настройка WebView
	void initWebView() {
		
		// ? Prefs
		dbg_javaScript = mSharedPrefs.getBoolean("dbg.js", true);
		dbg_webViewCache = mSharedPrefs.getBoolean("dbg.webViewCashe", true);
		dbg_injectJs = mSharedPrefs.getBoolean("dbg.injectJs", true);
		dbg_shouldOverrideUrlLoadingV2 = mSharedPrefs.getBoolean("dbg.shouldOverrideUrlLoadingV2", false);
		dbg_showLoadUrl = mSharedPrefs.getBoolean("dbg.showLoadUrl", false);
		
		AppUtils.Log(this, "d", "Init WebView");

		// ? Поиск элемента
		mWebView = findViewById(R.id.el_webView);

		// ? Получение доступа к настройке
		WebSettings WebViewSettings = mWebView.getSettings();
		WebViewSettings.setDefaultTextEncodingName("utf-8"); // ? Кодировка докум-тов
		
		// ? Если настройка "JavaScript support" активна
		if (dbg_javaScript) {
			AppUtils.Log(this, "d", "dbg.javaScript: " + dbg_javaScript);
			mWebView.getSettings().setJavaScriptEnabled(true); // ? Разрешаю запуск js-скриптов
			mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
		}

		// ? Если настройка "Page caching" активна
		if (dbg_webViewCache) {
			AppUtils.Log(this, "d", "dbg.appCache: " + dbg_webViewCache);
			mWebView.getSettings().setAppCacheEnabled(true); // ? Разрешаю кеширование страниц
			mWebView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT); // ? Режим кеширования
			// TODO: Нужно почитать о режиме кеширования страниц в WebView в документации
		}

		// ? Цвет фона WebView
		mWebView.setBackgroundColor(Color.parseColor("#121212"));
		
		// TODO: Почитать о "getSettings().setDomStorageEnabled(boolean)"
		mWebView.getSettings().setDomStorageEnabled(true);
		mWebView.getSettings().setUseWideViewPort(true);
		
		// ? Скрываю скроллбар
		mWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
		
		mWebView.getSettings().setDatabaseEnabled(true);
		mWebView.getSettings().setDatabasePath("/data/data/" + getPackageName() + "/databases/");
		
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
			WebViewSettings.setAllowFileAccessFromFileURLs(true);
			WebViewSettings.setAllowUniversalAccessFromFileURLs(true);
		}
		
		mWebView.setWebViewClient(new WebViewClient() {
			
			// ? Настраиваю переход по ссылкам
			public boolean shouldOverrideUrlLoading(WebView webView, String url) {

				String urlPrefix = loadUrl;
				
				if (dbg_shouldOverrideUrlLoadingV2) {
					
					/* ? Если начало ссылки, на которую нажал пользователь, соответствует
				     значению из urlPrefix, то открываем ссылку прямо в нашем приложении */
					if (url != null && url.startsWith(urlPrefix)){
						return false;
					} else {
						// .., а если нет, то отправляем пользователя в браузер
						webView.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
						return true;
					}
					
				} else {
					
					if (Uri.parse(url).getHost().length() == 0) {
						return false;
					}
					
					webView.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
					
					return true;
					
				}

			}

			// ? Отлов ошибок
			@SuppressWarnings("deprecation")
			@Override
			public void onReceivedError(WebView webView, int errCode, String errDesc, String failingUrl) {

				String log = "code: " + errCode + "\ndesc: " + errDesc + "\nurl: " + failingUrl;
				AppUtils.Log(MainActivity.this, "e", log);
				
				// ? Скроем WebView при ошибке
				mWebView.setVisibility(View.GONE);
				
				// ? Отобразим заглушку
				mErrorDummy.setVisibility(View.VISIBLE);
				
				// ? Перемена переменной
				isPageLoadError = true;
				
				// ! net::ERR_INTERNET_DISCONNECTED
				if (errCode == -2) {
					
				}
				
			}
			
			// ? Страница полностью загружена
			@Override
			public void onPageFinished(WebView webView, String url) {
				super.onPageFinished(webView, url);
				
				if (dbg_showLoadUrl) {
					AppUtils.showToast(MainActivity.this, "LOADED URL: " + url);
				}
				
				mErrorDummy.setVisibility(View.GONE);
				
				// ? Инжектирование скрипта
				if (dbg_injectJs) {
					injectJs();
				}
				
			}

		});

		mWebView.setWebChromeClient(new WebChromeClient() {
			
			// Отлов статуса загрузки страницы
			public void onProgressChanged(WebView webView, int nProgress) {

				AppUtils.Log(MainActivity.this, "i", "onProgressChanged");

				if (nProgress < 100 && mLoadingDummy.getVisibility() == View.GONE) {
					
					AppUtils.Log(MainActivity.this, "i", "nProgress < 100");
					mLoadingDummy.setVisibility(View.VISIBLE);
					
				} else if (nProgress == 100) {
					
					AppUtils.Log(MainActivity.this, "i", "nProgress == 100");
					
					mWebView.setVisibility(View.VISIBLE);
					mLoadingDummy.setVisibility(View.GONE);
					
					if (isPageLoadError) {
						//mErrorDummy.setVisibility(View.VISIBLE);
					} else {
						mErrorDummy.setVisibility(View.GONE);
					}
					
				} else {
					AppUtils.Log(MainActivity.this, "i", "nProgress / else");
				}

			}

			// ? Получаем заголовок страницы
			@Override
			public void onReceivedTitle(WebView webView, String pageTitle) {
				super.onReceivedTitle(webView, pageTitle);
				if (!TextUtils.isEmpty(pageTitle)) {
					//setTitle(pageTitle);
				}
			}

		});

		// ? Указываем WebView какую стр. загружать
		mWebView.loadUrl(loadUrl);
		AppUtils.Log(MainActivity.this, "i", "WebView load url: " + loadUrl);

	}

	@Override
	public void onBackPressed() {
		
		if (mWebView.canGoBack()) {
			mWebView.goBack();
			AppUtils.Log(MainActivity.this, "d", "mWebViewPageBack");
		} else {
			// ? Если это посл. страница, то выход из приложения
			super.onBackPressed();
			AppUtils.Log(MainActivity.this, "d", "mWebViewPageBack [edit app]");
			finish();
		}
		
	}
	
	// ? Перезагрузка страницы
	void mWebViewPageReload() {
		
		mWebView.reload();
		AppUtils.Log(MainActivity.this, "d", "mWebViewPageReload");
		
	}
	
	// ? Инжектирование скрипта для взаимодействия со страницей
	void injectJs() {
		
		try {
			
			InputStream mInputStream = getAssets().open("script.js");
			byte[] mBuffer = new byte[mInputStream.available()];
			
			mInputStream.read(mBuffer);
			mInputStream.close();
			
			String mEncoded = Base64.encodeToString(mBuffer, Base64.NO_WRAP);
			
			mWebView.loadUrl("javascript:(function() {" +
							 "var parent = document.getElementsByTagName('head').item(0);" +
							 "var script = document.createElement('script');" +
							 "script.type = 'text/javascript';" +
							 "script.innerHTML = window.atob('" + mEncoded + "');" +
							 "parent.appendChild(script)" +
							 "})()");
							 
			
			/*mWebView.loadUrl("javascript:(function() {" +
							 "var parent = document.getElementsByTagName('head').item(0);" +
							 "var style = document.createElement('style');" +
							 "style.type = 'text/css';" +
							 "style.innerHTML = window.atob('" + mEncoded + "');" +
							 "parent.appendChild(style)" +
							 "})()");*/
			
		} catch (Exception e) {
			e.printStackTrace();
			AppUtils.Log(this, "e", "injectJs: " + e);
		}
		
	}
	
	// ? 
	public boolean onKeyDown(int keyCode, KeyEvent keyEvent) {

		if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
			//onBackPressed();
			AppUtils.Log(MainActivity.this, "d", "KeyEvent.KEYCODE_BACK");
			//return true;
		} if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
			AppUtils.Log(MainActivity.this, "d", "KeyEvent.KEYCODE_VOLUME_UP");
		} if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
			AppUtils.Log(MainActivity.this, "d", "KeyEvent.KEYCODE_VOLUME_DOWN");
		} 

		return super.onKeyDown(keyCode, keyEvent);

	}
	
}
