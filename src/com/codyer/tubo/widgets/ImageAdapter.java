package com.codyer.tubo.widgets;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.util.*;

import com.codyer.tubo.R;
import com.codyer.tubo.utils.Constants;
import com.codyer.tubo.utils.FileUtil;
import com.codyer.tubo.utils.ImageUtils;

public class ImageAdapter extends BaseAdapter {
	private Context ctx;
	public List<Map<String, Object>> ls;
	private Map<String, Bitmap> bmplist;
	private int size;
	private Bitmap bmp;
	private int mLayout;

	@Override
	public void notifyDataSetChanged() {
		this.ls.clear();
		this.ls = FileUtil.getFileList(Constants.PICTURES_PATH);
		super.notifyDataSetChanged();
	}

	public ImageAdapter(Context ctx, int resId) throws Exception {
		this.ctx = ctx;
		this.mLayout = resId;
		this.ls = FileUtil.getFileList(Constants.PICTURES_PATH);
		// 对获取的文件集合进行判断
		if (this.ls == null || this.ls.isEmpty()) {
			throw new Exception("No photos");
		}
		if (this.ls == null && this.ls.isEmpty()) {
			this.ls = new ArrayList<Map<String, Object>>();
		}
		if (this.bmplist == null) {
			this.bmplist = new HashMap<String, Bitmap>();
			new Thread(new LoadImgRunnable()).start();
		}
	}

	private class LoadImgRunnable implements Runnable {

		@Override
		public void run() {
			String filepath;
			Bitmap bm = null;
			for (int i = 0; i < ls.size(); i++) {
				filepath = (String) ls.get(i).get("fpath");
				if (!bmplist.containsKey(filepath)) {
					bm = ImageUtils
							.getImageThumbnail(filepath,
									Constants.THUMBNAIL_WITH,
									Constants.THUMBNAIL_HIGHT);
					bmplist.put(filepath, bm);
				}
			}
		}
	}

	@Override
	public int getCount() {
		size = ls.size();
		return Integer.MAX_VALUE;
	}

	@Override
	public Object getItem(int arg0) {
		return arg0;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		if (convertView == null) {
			convertView = LayoutInflater.from(ctx).inflate(mLayout, null);
		}
		ImageView iv = (ImageView) convertView.findViewById(R.id.img_view);

		// 获取图片路径
		if (size < 1) {
			return null;
		}
		String filepath = (String) ls.get((size + position) % size)
				.get("fpath");
		// Log.v("path--->", filepath);

		try {
			if (!bmplist.containsKey(filepath)) {
				bmp = ImageUtils.getImageThumbnail(filepath,
						Constants.THUMBNAIL_WITH, Constants.THUMBNAIL_HIGHT);
				bmplist.put(filepath, bmp);
			} else {
				bmp = bmplist.get(filepath);
			}
			// 设置图片到ImageView上
			iv.setImageBitmap(bmp);
//			iv.setBackgroundColor(Color.alpha(1)); 
			// 设置边界对齐
			iv.setAdjustViewBounds(true);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return convertView;
	}
}
