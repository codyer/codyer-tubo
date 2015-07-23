package com.codyer.tubo.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class LoginUtils {

	public static boolean isFirstLogin(Context context) {
		SharedPreferences preferences;
		Editor editor;
		preferences = context.getSharedPreferences(Constants.APP_PREFERENCES_NAME,
				Context.MODE_PRIVATE);
		// 判断是不是首次登录
		if (preferences.getBoolean(Constants.APP_FIRST_LOGIN, true)) {
			editor = preferences.edit();
			// 将登录标志位设置为false，下次登录时不在显示首次登录界面
			editor.putBoolean(Constants.APP_FIRST_LOGIN, false);
			editor.commit();
			return true;
		}
		return false;
	}

}
