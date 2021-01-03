// ! rx1310 <rx1310@inbox.ru> | Copyright (c) DevReader, 2021 | MIT License

package ru.devreader.app.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import ru.devreader.app.R;
import ru.devreader.app.util.AppUtils;

public class IntroActivity extends AppCompatActivity {

	boolean isFirstStart;

	SharedPreferences mSharedPrefs;
	SharedPreferences.Editor mSharedPrefsEditor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_intro);
		
		mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		mSharedPrefsEditor = mSharedPrefs.edit();

		// ? Prefs
		isFirstStart = mSharedPrefs.getBoolean("isFirstStart", true);

	}
	
	public void finishIntro(View mView) {
		
		// ? Запуск основной активности
		startActivity(new Intent(IntroActivity.this, MainActivity.class));
		
		// ? Завершение жизни текущей активности
		IntroActivity.this.finish();
		
		// ? Отображение анимации перехода
		overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);
		
		// ? Сохранение настройки
		mSharedPrefsEditor.putBoolean("isFirstStart", false);
		mSharedPrefsEditor.commit();
		
		AppUtils.Log(this, "i", "isFirstStart = false");
		
	}

}
