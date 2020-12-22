// ! rx1310 <rx1310@inbox.ru> | Copyright (c) DevReader, 2020 | MIT License

package ru.devreader.app.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import ru.devreader.app.R;
import ru.devreader.app.util.AppUtils;

public class IntroActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		new Handler().postDelayed(new Runnable() {
			
			@Override
			public void run() {

				// ? Запуск MainActivity
				Intent i = new Intent(IntroActivity.this, MainActivity.class);
				IntroActivity.this.startActivity(i);
				AppUtils.Log(IntroActivity.this, "i", "Запуск MainActivity");
					
				// ? "Убийство" сплеша (иначе при нажатии пользователем
				// кнопки "Back" будет открыт снова сплеш.
				IntroActivity.this.finish();
				AppUtils.Log(IntroActivity.this, "i", "Завершение жизни IntroActivity");
					
				// ? Отображение анимации при переходе к MainActivity
				overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);

			}

		}, 1000); // ? 1000 = 1s (задержка перехода)

	}

}
