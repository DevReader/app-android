// ! rx1310 <rx1310@inbox.ru> | Copyright (c) DevReader, 2020 | MIT License

package ru.devreader.app.util;

import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class SysUtils {
	
	public static String getSysProp(String propName) {
		
		String mLine;
		BufferedReader mBufferReader = null;
		Process mProcess;
		
		try {
			mProcess = Runtime.getRuntime().exec("getprop " + propName);
			mBufferReader = new BufferedReader(new InputStreamReader(mProcess.getInputStream()), 1024);
			mLine = mBufferReader.readLine();
			mBufferReader.close();
		} catch (IOException ex) {
			return null;
		} finally {
			if (mBufferReader != null) {
                try { mBufferReader.close();}
				catch (IOException e) { e.printStackTrace(); }
            }
		}
		
		return mLine;
		
	}

	public static boolean isMIUI() {
        return !TextUtils.isEmpty(getSysProp("ro.miui.ui.version.name"));
    }
	
}
