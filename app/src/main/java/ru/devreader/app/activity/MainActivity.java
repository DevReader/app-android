// ! rx1310 <rx1310@inbox.ru> | Copyright (c) DevReader, 2020 | MIT License

package ru.devreader.app.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;

import android.util.Base64;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.annotation.TargetApi;
import android.widget.TextView;

import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

import java.io.InputStream;

import ru.devreader.app.R;
import ru.devreader.app.activity.MainActivity;
import ru.devreader.app.task.OTACheckTask;
import ru.devreader.app.util.AppUtils;

public class MainActivity extends AppCompatActivity {
	
	// ? Страница, которая будет загружена в WebView
	//final String loadUrl = "file:///android_asset/" + "test/index.html";
	final String loadUrl = "https://" + "devreader.github.io" + "/";
	
	WebView mWebView;
	FloatingActionButton mFabBack, mFabHome, mFabScrollToTop;
	BottomSheetDialog mDialogMenu;
	LinearLayout mLoadingDummy,
				 mErrorDummy,
				 mPageTitleContainer;
	
	boolean isPageLoadError = false;
	
	boolean dbg_javaScript, 
			dbg_webViewCache, 
			dbg_injectJs, 
			dbg_shouldOverrideUrlLoadingV2,
			dbg_showLoadUrl,
			dbg_renderPriorityHigh,
			dbg_showWebViewErrLog;
			
	boolean isOtaAutoCheckEnabled,
			isExitDialogEnabled,
			isHideFabOnScrollEnabled,
			isLastPageRememberEnabled,
			isImagesDnlEnabled,
			isPageZoomEnabled,
			isLargerFontEnabled,
			isTextWrapEnabled,
			isPageTitleEnabled;
			
	String isLastRememberedPage;
	
	TextView mPageTitle;
			
	SharedPreferences mSharedPrefs;
	SharedPreferences.Editor mSharedPrefsEditor;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		mSharedPrefsEditor = mSharedPrefs.edit();
		
		// ? Prefs
		isOtaAutoCheckEnabled = mSharedPrefs.getBoolean("ota.checkAuto", false);
		isLastPageRememberEnabled = mSharedPrefs.getBoolean("more.lastPageRemember", false);
		isLastRememberedPage = mSharedPrefs.getString("isLastPageUrl", "");
		
		// ? Проверка обновлений
		if (isOtaAutoCheckEnabled) OTACheckTask.checkUpdates(this, false);
		
		// ? Запуск WebView
		initWebView();
		
		// ? WebView
		mWebView = findViewById(R.id.el_webView);
		
		// ? Заглушка при загрузке страницы
		mLoadingDummy = findViewById(R.id.el_dummyLoading);
		
		// ? Заглушка при ошибке загрузки страницы
		mErrorDummy = findViewById(R.id.el_dummyError_m2);
		
		// ? Заголовок страницы
		mPageTitle = findViewById(R.id.el_pageTitle);
		mPageTitle.setTextAppearance(android.R.style.TextAppearance_WindowTitle);
		
		// ? LL, в котором находится заголовок страницы
		mPageTitleContainer = findViewById(R.id.el_pageTitleContainer);
		
		// ? Настройка FAB Back&Reload
		mFabBack = findViewById(R.id.el_fabBack);
		mFabBack.setOnClickListener(new View.OnClickListener() {
			public void onClick(View mView) {
				onBackPressed();
			}
		});
		mFabBack.setOnLongClickListener(new View.OnLongClickListener() {
			public boolean onLongClick(View mView) {
				AlertDialog.Builder b = new AlertDialog.Builder(MainActivity.this, R.style.AppTheme_Dialog_Alert);
				b.setItems(R.array.fabBack_longpress_menu, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface d, int itemPos) {
						if (itemPos == 0) {
							mWebViewPageReload(mWebView);
						} if (itemPos == 1) {
							MainActivity.this.recreate();
						}
					}
				});
				b.show();
				return true;
			}
		});
		
		// ? Настройка FAB Home
		mFabHome = findViewById(R.id.el_fabHome);
		mFabHome.setOnClickListener(new View.OnClickListener() {
			public void onClick(View mView) {
				mWebView.loadUrl(loadUrl);
				if (isPageLoadError) mErrorDummy.setVisibility(View.GONE);
				AppUtils.Log(MainActivity.this, "d", "mWebViewPageHome (homepage: " + loadUrl + ")");
			}
		});
		mFabHome.setOnLongClickListener(new View.OnLongClickListener() {
			public boolean onLongClick(View mView) {
				startActivity(new Intent(MainActivity.this, SettingsActivity.class));
				return true;
			}
		});
		
		// ? Настройка FAB Scroll Top
		mFabScrollToTop = findViewById(R.id.el_fabScrollToTop);
		mFabScrollToTop.hide();
		mFabScrollToTop.setOnClickListener(new View.OnClickListener() {
			public void onClick(View mView) {
				mWebView.scrollTo(0,0);
				mFabScrollToTop.hide();
			}
		});
		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		// ? Prefs
		isHideFabOnScrollEnabled = mSharedPrefs.getBoolean("ui.fabScroll", false);
		isPageTitleEnabled = mSharedPrefs.getBoolean("ui.pageTitle", false);
		
		// ? Исчезающие кнопки навигации
		if (isHideFabOnScrollEnabled) {
			hideFabOnScroll(true);
		} else {
			hideFabOnScroll(false);
		}
		
		// ? Состояние видимости заголовка сираницы
		if (isPageTitleEnabled) {
			mPageTitleContainer.setVisibility(View.VISIBLE);
		} else {
			mPageTitleContainer.setVisibility(View.GONE);
		}
		
	}
	
	@TargetApi(23)
	public void hideFabOnScroll(final boolean isNavFabHideEnabled){
		mWebView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
			@Override
			public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
				
				if (scrollY > oldScrollY) {
					mFabScrollToTop.show();
					if (isNavFabHideEnabled) {
						mFabBack.hide();
						mFabHome.hide();
					}
				} else if (scrollY < oldScrollY) {
					mFabScrollToTop.hide();
					if (isNavFabHideEnabled) {
						mFabBack.show();
						mFabHome.show();
					}
				}
				
			}
		});
	}
	
	// ? Настройка WebView
	void initWebView() {
		
		// ? Prefs
		dbg_javaScript = mSharedPrefs.getBoolean("dbg.js", true);
		dbg_webViewCache = mSharedPrefs.getBoolean("dbg.webViewCashe", true);
		dbg_injectJs = mSharedPrefs.getBoolean("dbg.injectJs", false);
		dbg_shouldOverrideUrlLoadingV2 = mSharedPrefs.getBoolean("dbg.shouldOverrideUrlLoadingV2", true);
		dbg_showLoadUrl = mSharedPrefs.getBoolean("dbg.showLoadUrl", false);
		dbg_renderPriorityHigh = mSharedPrefs.getBoolean("dbg.renderPriorityHigh", true);
		dbg_showWebViewErrLog = mSharedPrefs.getBoolean("dbg.showWebViewErrLog", false);
		
		isImagesDnlEnabled = mSharedPrefs.getBoolean("content.imagesDnl", false);
		isLargerFontEnabled = mSharedPrefs.getBoolean("content.largerFont", false);
		isPageZoomEnabled = mSharedPrefs.getBoolean("content.pageZoom", false);
		isTextWrapEnabled = mSharedPrefs.getBoolean("content.textWrap", false);
		
		AppUtils.Log(this, "d", "Init WebView");

		// ? Поиск элемента
		mWebView = findViewById(R.id.el_webView);

		// ? Получение доступа к настройке
		WebSettings WebViewSettings = mWebView.getSettings();
		WebViewSettings.setDefaultTextEncodingName("utf-8"); // ? Кодировка докум-тов
		WebViewSettings.setAppCachePath(getCacheDir().getAbsolutePath());
		WebViewSettings.setLoadWithOverviewMode(true);
		WebViewSettings.setAllowContentAccess(true);
        WebViewSettings.setAllowFileAccess(true);
		
		// ? Если настройка "High Render Priority" активна
		if (dbg_renderPriorityHigh) {
			WebViewSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);
		} else {
			WebViewSettings.setRenderPriority(WebSettings.RenderPriority.LOW);
		}
		
		// ? Если настройка "JavaScript support" активна
		if (dbg_javaScript) {
			AppUtils.Log(this, "d", "dbg.javaScript: " + dbg_javaScript);
			mWebView.getSettings().setJavaScriptEnabled(true); // ? Разрешаю запуск js-скриптов
			mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
		} else {
			AppUtils.Log(this, "d", "dbg.javaScript: " + dbg_javaScript);
			mWebView.getSettings().setJavaScriptEnabled(false); // ? Запрещаю запуск js-скриптов
			mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(false);
		}

		// ? Если настройка "Page caching" активна
		if (dbg_webViewCache) {
			AppUtils.Log(this, "d", "dbg.appCache: " + dbg_webViewCache);
			mWebView.getSettings().setAppCacheEnabled(true); // ? Разрешаю кеширование страниц
			mWebView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT); // ? Режим кеширования
			// TODO: Нужно почитать о режиме кеширования страниц в WebView в документации
		} else {
			AppUtils.Log(this, "d", "dbg.appCache: " + dbg_webViewCache);
			mWebView.getSettings().setAppCacheEnabled(false); // ? Запрещаю кеширование страниц
		}

		// ? Цвет фона WebView
		mWebView.setBackgroundColor(Color.parseColor("#121212"));
		
		mWebView.getSettings().setUseWideViewPort(true);
		
		// ? Скрываю скроллбар
		mWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
		
		mWebView.getSettings().setDatabaseEnabled(true);
		mWebView.getSettings().setDatabasePath("/data/data/" + getPackageName() + "/databases/");
		
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
			WebViewSettings.setAllowFileAccessFromFileURLs(true);
			WebViewSettings.setAllowUniversalAccessFromFileURLs(true);
		}
		
		if (isLargerFontEnabled) {
            mWebView.getSettings().setTextSize(WebSettings.TextSize.LARGER);
        } else {
            mWebView.getSettings().setTextSize(WebSettings.TextSize.NORMAL);
        }

        if (isPageZoomEnabled) {
            mWebView.getSettings().setSupportZoom(true);
            mWebView.getSettings().setBuiltInZoomControls(true);
        } else {
            mWebView.getSettings().setSupportZoom(false);
            mWebView.getSettings().setBuiltInZoomControls(false);
        }
		
		if (isTextWrapEnabled) {
            mWebView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        } else {
            mWebView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
        }
		
		if (isImagesDnlEnabled) {
            mWebView.getSettings().setDomStorageEnabled(false);
            mWebView.getSettings().setLoadsImagesAutomatically(false);
        } else {
            mWebView.getSettings().setDomStorageEnabled(true);
            mWebView.getSettings().setLoadsImagesAutomatically(true);
        }
		
		mWebView.setWebViewClient(new WebViewClient() {
			
			// ? Настраиваю переход по ссылкам
			public boolean shouldOverrideUrlLoading(WebView webView, String url) {

				String urlPrefix = loadUrl;
				
				if (dbg_shouldOverrideUrlLoadingV2) {
					
					/* ? Если начало ссылки, на которую нажал пользователь, соответствует
				     	 значению из urlPrefix, то открываем ссылку прямо в нашем приложении */
					if (url.contains(urlPrefix)){
						return false;
					} else {
						// .., а если нет, то отправляем пользователя в браузер
						//webView.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
						AppUtils.openURL(webView.getContext(), url);
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
				
				// ? Изменим текст Page Title
				mPageTitle.setText("🤷");
				
				// Перемена переменной
				isPageLoadError = true;
				
				// ! net::ERR_INTERNET_DISCONNECTED
				if (errCode == -2) {}
				
				if (dbg_showWebViewErrLog) {
					AppUtils.showToast(getApplicationContext(), log);
				}
				
			}
			
			// ? Страница полностью загружена
			@Override
			public void onPageFinished(WebView webView, String url) {
				super.onPageFinished(webView, url);
				
				// ? Всплыв. уведомление с названием загруженной страницы
				if (dbg_showLoadUrl) {
					AppUtils.showToast(MainActivity.this, "LOADED URL: " + url);
				}
				
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
					mPageTitle.setText("💆");
					
				} else if (nProgress == 100) {
					
					AppUtils.Log(MainActivity.this, "i", "nProgress == 100");
					
					mWebView.setVisibility(View.VISIBLE);
					mLoadingDummy.setVisibility(View.GONE);
					
				} else {
					AppUtils.Log(MainActivity.this, "i", "nProgress / else");
				}

			}

			// ? Получаем заголовок страницы
			@Override
			public void onReceivedTitle(WebView webView, String pageTitle) {
				super.onReceivedTitle(webView, pageTitle);
				if (!TextUtils.isEmpty(pageTitle)) {
					mPageTitle.setText(pageTitle);
				}
			}
			
			// ? 
			@Override
			public boolean onJsAlert(WebView webView, String url, String alertMessage, JsResult jsResult) {
						
				android.support.v7.app.AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MainActivity.this, R.style.AppTheme_Dialog_Alert);

				alertBuilder.setTitle(getString(R.string.app_name) + " (JS Alert)");
				alertBuilder.setMessage(alertMessage);
				alertBuilder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface d, int i) {
						d.dismiss();
					}
				});
				alertBuilder.show();
					
				jsResult.cancel();
				
				return true;
					
			}

		});

		// ? Указываем WebView какую стр. загружать
		if (isLastPageRememberEnabled) {
			mWebView.loadUrl(isLastRememberedPage);
		} else {
			mWebView.loadUrl(loadUrl);
		}
		
		AppUtils.Log(MainActivity.this, "i", "WebView load url: " + loadUrl);

	}
	
	// ? Нажатие кнопки Back
	@Override
	public void onBackPressed() {
		
		if (mWebView.canGoBack()) {
			
			// ? Перемещение на пред. стр.
			mWebView.goBack();
			
			// ? Если до этого была ошибка, то скрываем заглушку
			if (isPageLoadError) {
				mErrorDummy.setVisibility(View.GONE);
			}
			
			AppUtils.Log(MainActivity.this, "d", "mWebViewPageBack");
			
		} else {
			
			// ? Prefs
			isExitDialogEnabled = mSharedPrefs.getBoolean("more.showExitDialog", true);
			
			// ? Если настройка активна, то перед выходом будет показано предупреждение
			if (isExitDialogEnabled) {
				android.support.v7.app.AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MainActivity.this, R.style.AppTheme_Dialog_Alert);
				alertBuilder.setMessage(R.string.dlg_exit_message);
				alertBuilder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface d, int i) {
						AppUtils.Log(MainActivity.this, "d", "mWebViewPageBack [exit app]");
						MainActivity.this.finish();
					}
				});
				alertBuilder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface d, int i) {
						d.dismiss();
					}
				});
				alertBuilder.show();
			} else {
				super.onBackPressed();
				finish();
			}
			
		}
		
	}
	
	// ? Перезагрузка страницы
	public void mWebViewPageReload(View mView) {
		
		mWebView.reload();
		
		// ? Если до этого была ошибка, то скрываем заглушку
		if (isPageLoadError) {
			mErrorDummy.setVisibility(View.GONE);
		}
		
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
							 "style.innerHTML = window.atob('" + "styles.css" + "');" +
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

	@Override
	protected void onStop() {
		super.onStop();
		
		// ? Получение URL посл. страницы
		String getLastPageUrl = mWebView.getUrl().toString();
		
		// ? Сохранение настройки
		mSharedPrefsEditor.putString("isLastPageUrl", getLastPageUrl); // ? Сохраняю ссылку посл. стр.
		mSharedPrefsEditor.commit();
		
	}
	
}
