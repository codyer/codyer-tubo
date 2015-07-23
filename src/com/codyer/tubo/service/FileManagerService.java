package com.codyer.tubo.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipException;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.MediaStore.MediaColumns;

import com.codyer.tubo.utils.Constants;
import com.codyer.tubo.utils.ZipUtils;

public class FileManagerService {

	private Context context;

	/**
	 * @param context
	 */
	public FileManagerService(Context context) {
		this.context = context;
	}

	public FileManagerService() {
	}

	public void copyFiles() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		boolean filesCopyed = prefs.getBoolean(Constants.SHAREPREFERECE_FILES,
				false);
		if (!filesCopyed) {
			File file = new File(Constants.EXPECTED_FILE);
			file.mkdirs();
			File parent = file.getParentFile();
			if (parent.listFiles().length < 2) {

				// 把 Assert里的字库copy到文件下
				try {
					InputStream in = context.getResources().getAssets()
							.open("tessdata.zip");
					File zip = new File(Constants.TESSBASE_PATH
							+ "tessdata.zip");
					OutputStream out = new FileOutputStream(zip);
					byte[] temp = new byte[1024];
					int size = -1;
					while ((size = in.read(temp)) != -1) {
						out.write(temp, 0, size);
					}
					out.flush();
					out.close();
					in.close();
					// 解压其文件夹
					try {
						ZipUtils.upZipFile(zip, Constants.TESSBASE_PATH);
						writeResult(prefs, true);
					} catch (ZipException e) {
						writeResult(prefs, false);
						e.printStackTrace();
					} catch (IOException e) {
						writeResult(prefs, false);
						e.printStackTrace();
					}
					// 删除压缩文件.zip
					zip.deleteOnExit();
				} catch (IOException e) {
					writeResult(prefs, false);
					e.printStackTrace();
				}

			}
		}
	}

	private void writeResult(SharedPreferences prefs , Boolean result) {
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean(Constants.SHAREPREFERECE_FILES, result);
		editor.commit();
	}
	

	/**
	 * 通过URI 取得绝对路径
	 * 
	 * @param context
	 * @param uri
	 * @return
	 */
	public static String getFilePathByContentResolver(Context context, Uri uri) {
		if (null == uri) {
			return null;
		}
		Cursor c = context.getContentResolver().query(uri, null, null, null,
				null);
		String filePath = null;
		if (null == c) {
			throw new IllegalArgumentException("Query on " + uri
					+ " returns null result.");
		}
		try {
			if ((c.getCount() != 1) || !c.moveToFirst()) {
			} else {
				filePath = c.getString(c
						.getColumnIndexOrThrow(MediaColumns.DATA));
			}
		} finally {
			c.close();
		}
		return filePath;
	}


}
