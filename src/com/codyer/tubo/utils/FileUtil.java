package com.codyer.tubo.utils;

import java.util.*;
import java.io.*;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

//获取文件列表的集合
public class FileUtil {

	/** 保存方法 */
	public static void copyLogo(Context mContext, Integer PicID) {
		try {
			// 图片存放全路径
			File dir = new File(Constants.PICTURES_PATH);
			// 如果文件夹不存在，创建一个（只能在应用包下面的目录，其他目录需要申请权限 OWL）
			if (!dir.exists()) {
				dir.mkdirs();
			}
			// 获得封装 文件的InputStream对象
			InputStream is = mContext.getResources().openRawResource(PicID);
			File logoFile = new File(dir, "logo.jpg");
			if (!logoFile.exists()) {
				try {
					logoFile.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			FileOutputStream fos = new FileOutputStream(logoFile);
			byte[] buffer = new byte[8192];
			int count = 0;

			// 开始复制Logo图片文件
			while ((count = is.read(buffer)) > 0) {
				fos.write(buffer, 0, count);
			}
			fos.close();
			is.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** 保存方法 */
	public static void saveBitmap(Bitmap bm) {
		File tmpFileDir = new File(Constants.TEMP_PATH);
		if (!tmpFileDir.exists()) {
			tmpFileDir.mkdirs();
		}
		File picFile = new File(tmpFileDir, "crop.jpg");
		if (!picFile.exists()) {
			try {
				picFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			FileOutputStream out = new FileOutputStream(picFile);
			bm.compress(Bitmap.CompressFormat.PNG, 100, out);
			out.flush();
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param fromFile
	 *            被复制的文件
	 * @param toFile
	 *            复制的目录文件
	 * @param rewrite
	 *            是否重新创建文件
	 * 
	 *            文件的复制操作方法
	 */
	public static void copyfile(File fromFile, File toFile, Boolean rewrite) {

		if (!fromFile.exists()) {
			return;
		}

		if (!fromFile.isFile()) {
			return;
		}
		if (!fromFile.canRead()) {
			return;
		}
		if (!toFile.getParentFile().exists()) {
			toFile.getParentFile().mkdirs();
		}
		if (toFile.exists() && rewrite) {
			toFile.delete();
		}

		try {
			FileInputStream fosfrom = new FileInputStream(fromFile);
			FileOutputStream fosto = new FileOutputStream(toFile);

			byte[] bt = new byte[1024];
			int c;
			while ((c = fosfrom.read(bt)) > 0) {
				fosto.write(bt, 0, c);
			}
			// 关闭输入、输出流
			fosfrom.close();
			fosto.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * 用于遍历存储卡下指定目录下的所有image文件
	 * */
	public static List<Map<String, Object>> getFileList(String fpath) {
		List<Map<String, Object>> ls = new ArrayList<Map<String, Object>>();
		File fl = new File(fpath);
		File[] fls = fl.listFiles();
		if (fls != null && fls.length > 0) {
			for (int i = 0; i < fls.length; i++) {
				if (fls[i].isDirectory()) {
					getFileList(fpath);
				} else {
					if (fls[i].length() < 1) {
						fls[i].delete();
						continue;
					}
					// 获取文件后缀
					String ext = fls[i].getAbsolutePath().substring(
							fls[i].getAbsolutePath().lastIndexOf(".",
									fls[i].getAbsolutePath().length()));
					Log.v("文件后缀::::", ext);

					// 判断文件的后缀是不是jpg或者png或者gif
					if (ext.equalsIgnoreCase(".jpg")
							|| ext.equalsIgnoreCase(".png")
							|| ext.equalsIgnoreCase(".gif")
							|| ext.equalsIgnoreCase(".bmp")
							|| ext.equalsIgnoreCase(".jpeg")) {

						Map<String, Object> map = new HashMap<String, Object>();
						map.put("fpath", fls[i].getAbsolutePath());
						ls.add(map);
					}
				}
			}
		}
		Log.v("ls--->", "" + ls.size());
		return ls;
	}
}
