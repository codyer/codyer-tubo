package com.codyer.tubo.utils;

import android.os.Environment;

public class Constants {
	public static final String TAG = "SpeedDialTag";
	public static final String APP_PREFERENCES_NAME = "SpeedDial";
	public static final String APP_FIRST_LOGIN = "FirstLogin";
	public static final String PHONE_SERVISE_URL = "http://webservice.webxml.com.cn/WebServices/MobileCodeWS.asmx";// 手机归属地web
																													// service地址
	public static final String SHAREPREFERECE_FILES = "DATA_FILES";
	public static final String TESSBASE_PATH = Environment
			.getExternalStorageDirectory().getAbsolutePath() + "/SpeedDial/tessdata/";
	public static final String PICTURES_PATH = Environment.getExternalStorageDirectory().getAbsolutePath()+"/SpeedDial/Pictures/";
	public static final String TEMP_PATH = Environment.getExternalStorageDirectory().getAbsolutePath()+"/SpeedDial/Temp/";
	public static final String DEFAULT_LANGUAGE = "eng";
	public static final String EXPECTED_FILE = TESSBASE_PATH + "tessdata/"
			+ DEFAULT_LANGUAGE + ".traineddata";
	public static final int RESULT_CAMERA_DATE = 1006;
	public static final int RESULT_CROP_DATE = 1007;

	public static final int THUMBNAIL_WITH = 220;
	public static final int THUMBNAIL_HIGHT = 280;

	public static final int CAMERA_WITH_DATA = 0x11;//请求相机功能
	public static final int PHOTO_WITH_DATA = 0x12; //请求相册
	public static final int CROP_PHOTO = 0x13;//请求相机功能

}
