package com.codyer.tubo.photos;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.codyer.tubo.R;
import com.codyer.tubo.photos.ImageAdapter.ViewHolder;
import com.codyer.tubo.utils.Constants;
import com.codyer.tubo.utils.ImageUtils;
import com.codyer.tubo.widgets.LoadingView;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class PictureManagerActivity extends Activity {

	private GridView gridView;
	private ImageAdapter imgAdapter;

	private TextView seclectNumView;
	private LinearLayout editBtLayout;
	private Button deleteButton;
	private Button cancelButton;

	private List<LoadImage> fileNameList = new ArrayList<LoadImage>(); // 保存Adapter中显示的图片详情(要跟adapter里面的List要对应)
	private List<LoadImage> selectFileLs = new ArrayList<LoadImage>(); // 保存选中的图片信息

	private boolean isEditMode = false; // 是否正在长按状态
	private ImageButton backBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.picturesmanager);

		gridView = (GridView) findViewById(R.id.picture_grid);
		seclectNumView = (TextView) findViewById(R.id.pic_seclet_num);		
		editBtLayout = (LinearLayout) findViewById(R.id.pic_edit);
		deleteButton = (Button) findViewById(R.id.pic_delete);
		cancelButton = (Button) findViewById(R.id.pic_cancel);
		backBtn = (ImageButton) findViewById(R.id.back);
		backBtn.setOnClickListener(mClickListener);
		deleteButton.setOnClickListener(mClickListener);
		cancelButton.setOnClickListener(mClickListener);

		imgAdapter = new ImageAdapter(this);
		gridView.setAdapter(imgAdapter);
		gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				LoadImage loadimg = fileNameList.get(position);
				ViewHolder holder = (ViewHolder) view.getTag();
				if (isEditMode) {
					if (selectFileLs.contains(loadimg)) {
//						holder.imgShow.setImageDrawable(null);
						holder.imgSelect.setVisibility(View.GONE);
						imgAdapter.delNumber(position + "");
						selectFileLs.remove(loadimg);
					} else {
//						holder.imgShow.setImageResource(R.drawable.border); // 添加图片(带边框的透明图片)[主要目的就是让该图片带边框]
						holder.imgSelect.setVisibility(View.VISIBLE); // 设置图片右上角的对号显示
						imgAdapter.addNumber(position + ""); // 把该图片添加到adapter的选中状态，防止滚动后就没有在选中状态了。
						selectFileLs.add(loadimg);
					}
					seclectNumView.setText(getResources().getString(
							R.string.pick_image_start)
							+ selectFileLs.size()
							+ getResources().getString(R.string.pick_image_end));
				} else {
					Intent intent = new Intent(PictureManagerActivity.this,
							ImageDetailsActivity.class);
					intent.putExtra("image_position", position);
					startActivity(intent);
				}
			}
		});
		gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				LoadImage loadimg = fileNameList.get(position);
				ViewHolder holder = (ViewHolder) view.getTag();
				if (!isEditMode) {
					isEditMode = true;
					gridView.setPadding(0, 75, 0, 75); // 长按后，让gridview上下都分出点空间，显示删除按钮之类的。看效果图就知道了。
					seclectNumView.setVisibility(View.VISIBLE);
					editBtLayout.setVisibility(View.VISIBLE);
//					holder.imgShow.setImageResource(R.drawable.border);
					holder.imgSelect.setVisibility(View.VISIBLE);
					imgAdapter.addNumber(position + "");
					selectFileLs.add(loadimg);
					seclectNumView.setText(getResources().getString(
							R.string.pick_one_image));
					return true;
				}
				return false;
			}
		});
		LoadingView.show(this, getResources().getString(R.string.loading),
				true, true);
		// Toast.makeText(this, "加载图片中....", Toast.LENGTH_SHORT).show();
		new AsyncLoadedImage().execute();
	}

	/**
	 * 删除监听器事件
	 */
	private android.view.View.OnClickListener mClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.pic_delete:
				if (selectFileLs.isEmpty()) {
					Toast.makeText(PictureManagerActivity.this,
							getResources().getString(R.string.please_pick_image),
							Toast.LENGTH_SHORT).show();
					return;
				}
				for (LoadImage loadimg : selectFileLs) {
					File file = new File(loadimg.getFileName());
					boolean isTrue = file.delete();
					fileNameList.remove(loadimg);
					Log.i("----------------------删除图片------", isTrue
							+ "---------------");
				}
				imgAdapter.deletePhoto(selectFileLs);
				selectFileLs.clear();
				imgAdapter.clear();
				seclectNumView.setText(getResources().getString(R.string.please_pick_image));
				break;
			case R.id.pic_cancel:
				cancelDelete();
				break;
			case R.id.back:
				finish();
			default:
				break;
			}			
		}
	};

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (isEditMode && keyCode == KeyEvent.KEYCODE_BACK) { // 点击返回按键
			cancelDelete();
			return false;
		}
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * 异步加载图片展示
	 * 
	 * @author： codyer
	 * @date： 2012-8-1
	 */
	class AsyncLoadedImage extends AsyncTask<Object, LoadImage, Boolean> {

		@Override
		protected Boolean doInBackground(Object... params) {
			File fileDir = new File(Constants.PICTURES_PATH);
			File[] files = fileDir.listFiles();
			boolean result = false;
			if (files != null) {
				for (File file : files) {
					String fileName = file.getName();
					// 获取文件后缀
					String ext = fileName.substring(fileName.lastIndexOf(".",
							fileName.length()));
					Log.v("文件后缀::::", ext);

					// 判断文件的后缀是不是jpg或者png或者gif
					if (fileName.lastIndexOf(".") > 0
							&& (ext.equalsIgnoreCase(".jpg")
									|| ext.equalsIgnoreCase(".png")
									|| ext.equalsIgnoreCase(".gif")
									|| ext.equalsIgnoreCase(".bmp") || ext
										.equalsIgnoreCase(".jpeg"))) {
						// Bitmap bitmap;
						Bitmap newBitmap;
						try {
							newBitmap = ImageUtils.getImageThumbnail(
									file.getPath(), 110, 110);
							// BitmapFactory.Options options = new
							// BitmapFactory.Options();
							// options.inSampleSize = 10;
							// bitmap = BitmapFactory.decodeFile(file.getPath(),
							// options);
							// newBitmap =
							// ThumbnailUtils.extractThumbnail(bitmap, 67, 70);
							// bitmap.recycle();
							if (newBitmap != null) {
								LoadImage loadImage = new LoadImage(
										file.getPath(), newBitmap);
								fileNameList.add(loadImage);
								publishProgress(loadImage);
								result = true;
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
			return result;
		}

		@Override
		public void onProgressUpdate(LoadImage... value) {
			for (LoadImage loadImage : value) {
				imgAdapter.addPhoto(loadImage);
			}
		}

		@SuppressWarnings("deprecation")
		@Override
		protected void onPostExecute(Boolean result) {
			if (!result) {
				showDialog(1);
			} else {
				LoadingView.dismiss();
			}
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		//TODO do something
		AlertDialog dialog = new AlertDialog.Builder(PictureManagerActivity.this)
				.setTitle("温馨提示").setMessage("暂时还没有照片,请先采集照片！")
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// startActivity(new
						// Intent(PictureScanAct.this,TakePhotoAct.class));
					}
				})
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						finish();
					}
				}).show();
		return dialog;
	}

	/**
	 * 
	 */
	public void cancelDelete() {
		isEditMode = false;
		gridView.setPadding(0, 0, 0, 0); // 退出编辑转台时候，使gridview全屏显示
		seclectNumView.setVisibility(View.GONE);
		editBtLayout.setVisibility(View.GONE);
		selectFileLs.clear();
		imgAdapter.clear();
	}

	/**
	 * 图片详细信息bean
	 * 
	 * @author： codyer
	 * @date： 2012-8-31
	 */
	public class LoadImage {

		private String fileName;
		private Bitmap bitmap;

		public LoadImage() {
			super();
			// TODO Auto-generated constructor stub
		}

		public LoadImage(String fileName, Bitmap bitmap) {
			super();
			this.fileName = fileName;
			this.bitmap = bitmap;
		}

		/**
		 * @return the fileName
		 */
		public String getFileName() {
			return fileName;
		}

		/**
		 * @param fileName
		 *            the fileName to set
		 */
		public void setFileName(String fileName) {
			this.fileName = fileName;
		}

		/**
		 * @return the bitmap
		 */
		public Bitmap getBitmap() {
			return bitmap;
		}

		/**
		 * @param bitmap
		 *            the bitmap to set
		 */
		public void setBitmap(Bitmap bitmap) {
			this.bitmap = bitmap;
		}

		@Override
		public int hashCode() {
			return this.getFileName().hashCode();
		}

		@Override
		public boolean equals(Object o) {
			LoadImage loadImg = (LoadImage) o;
			return this.getFileName().equals(loadImg.getFileName());
		}

	}
}