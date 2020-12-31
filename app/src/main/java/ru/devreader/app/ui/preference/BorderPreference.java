// ! rx1310 <rx1310@inbox.ru> | Copyright (c) DevReader, 2020 | MIT License

package ru.devreader.app.ui.preference;

import android.content.Context;
import android.util.AttributeSet;

import ru.devreader.app.R;

public class BorderPreference extends CategoryPreference {

	public BorderPreference(Context c, AttributeSet attrs) {
		super(c, attrs);
		setSelectable(false);
		setLayoutResource(R.layout.ui_preference_border);
	}

}
