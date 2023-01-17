package com.codyer.tubo.photos;

import java.util.ArrayList;
import java.util.List;

import com.codyer.tubo.R;
import com.codyer.tubo.photos.PictureManagerActivity.LoadImage;
import com.codyer.tubo.utils.FileUtil;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class ImageAdapter extends BaseAdapter {

	private List<LoadImage> picList = new ArrayList<LoadImage>(); // 图片集合
	private List<String> picNumber = new ArrayList<String>(); // 选中图片的位置集合

	private LayoutInflater inflater;
	private Context mContext;

	public ImageAdapter(Context mContext) {
		this.mContext = mContext;
		inflater = LayoutInflater.from(mContext);
	}

	@Override
	public int getCount() {
		return picList.size();
	}

	@Override
	public Object getItem(int position) {
		return picList.get(position);
	}

	/**
	 * 添加选中状态的图片位置
	 * 
	 * @param position
	 */
	public void addNumber(String position) {
		picNumber.add(position);
	}

	/**
	 * 去除已选中状态的图片位置
	 * 
	 * @param position
	 */
	public void delNumber(String position) {
		picNumber.remove(position);
	}

	/**
	 * 清空已选中的图片状态
	 */
	public void clear() {
		picNumber.clear();
		notifyDataSetChanged();
	}

	/**
	 * 添加图片
	 * 
	 * @param bitmap
	 */
	public void addPhoto(LoadImage loadImage) {
		picList.add(loadImage);
		notifyDataSetChanged();
	}

	/**
	 * 删除图片
	 * 
	 * @param loadimgLs
	 */
	public void deletePhoto(List<LoadImage> loadimgLs) {
		for (LoadImage img : loadimgLs) {//保留应用图片
			if (picList.contains(img)) {
				picList.remove(img);
			}
		}
		picNumber.clear();
		notifyDataSetChanged();
//		if (picList == null || picList.isEmpty()) {
//			FileUtil.copyLogo(this.mContext , R.drawable.logo);
//		}
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = inflater.inflate(R.layout.picturesmanager_item, null);
			holder.imgShow = (ImageView) convertView.findViewById(R.id.scan_img);
			holder.imgSelect = (ImageView) convertView
					.findViewById(R.id.scan_select);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
//		Drawable bit = new BitmapDrawable(picList.get(position).getBitmap());
		holder.imgShow.setImageBitmap(picList.get(position).getBitmap());
		if (picNumber.contains("" + position)) { // 如果该图片在选中状态，使其右上角的小对号图片显示，并且添加边框。
			holder.imgSelect.setVisibility(View.VISIBLE);
//			holder.image1.setImageResource(R.drawable.border);
		} else {
			holder.imgSelect.setVisibility(View.GONE);
		}
		return convertView;
	}

	public static class ViewHolder {
		public ImageView imgShow; // 要显示的图片
		public ImageView imgSelect; // 图片右上角的小对号图片(标示选中状态的玩意)
	}
}
