package com.codyer.tubo;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import com.codyer.tubo.photos.PictureManagerActivity;
import com.codyer.tubo.service.FileManagerService;
import com.codyer.tubo.service.ShakeService;
import com.codyer.tubo.utils.Constants;
import com.codyer.tubo.utils.FileUtil;
import com.codyer.tubo.utils.ImageUtils;
import com.codyer.tubo.utils.LoginUtils;
import com.codyer.tubo.widgets.ImageAdapter;
import com.codyer.tubo.widgets.LoadingView;
import com.codyer.tubo.widgets.MySeekBar;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Gallery;
import android.widget.ImageButton;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SlidingDrawer;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;
import android.widget.ViewSwitcher.ViewFactory;

@SuppressLint("HandlerLeak")
@SuppressWarnings("deprecation")
public class MainActivity extends Activity implements ViewFactory {

	private ImageAdapter mImgAdapter;
	private int mOldSelctedIndex;
	private int mScreenWidth;
	private int mScreenHeight;
	private MySeekBar mSeekBar;
	private ImageSwitcher mSwitchView;
	private ImageView mRecView;
	private ImageButton mBtCamera;
	private ImageButton mBtPhoto;
	private ImageView mBtSetting;
	private SlidingDrawer slidingDrawer;
	private ImageButton mBtHandle;
	private Uri mCameraImageUri;
	private Uri mPhotoImageUri;
	private Uri mCropImageUri;
	private Uri mCurrentUri;
	private Gallery gallery;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Init();
		startService(new Intent(this, ShakeService.class));
	}

	private void Init() {
		initFiles();
		findViews();
		mOldSelctedIndex = 0;
		mCurrentUri = null;
		showMaskLayer(false);
		DisplayMetrics dm = getResources().getDisplayMetrics();
		mScreenWidth = dm.widthPixels;
		mScreenHeight = dm.heightPixels;

		// 创建ImageAdapter对象
		try {
			mImgAdapter = new ImageAdapter(MainActivity.this,
					R.layout.hsv_list_item);
		} catch (Exception e) {
			AlertDialog.Builder alert = new Builder(MainActivity.this);
			alert.setCancelable(false);
			alert.setTitle("提示");
			alert.setMessage("文件或者文件夹不存在，\n"
					+ "请在sd存储卡根目录建立/SpeedDial/Pictures/文件夹,并将图片\n"
					+ "保存在存储卡的/SpeedDial/Pictures/文件夹下，\n"
					+ "图片的格式应为jpg或者是gif和png格式,\n" + "然后打开本应用。");
			alert.setPositiveButton("确定",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							FileUtil.copyLogo(MainActivity.this , R.drawable.logo);
							dialog.cancel();	
							MainActivity.this.finish();
						}
					});
			alert.create();
			alert.show();
			e.printStackTrace();
			return;
		}
		
		// mHorListView.setAdapter(mImgAdapter);
		mSwitchView.setFactory(this);
		mSeekBar.setOnSeekBarChangeListener(new seekBarListener());

		gallery.setAdapter(mImgAdapter);
		gallery.setGravity(Gravity.CENTER_HORIZONTAL); // 设置水平居中显示
		gallery.setSelection(mImgAdapter.ls.size() * 100); // 设置起始图片显示位置（可以用来制作gallery循环显示效果）

		gallery.setOnItemClickListener(clickListener); // 设置点击图片的监听事件（需要用手点击才触发，滑动时不触发）
		gallery.setOnItemSelectedListener(selectedListener); // 设置选中图片的监听事件（当图片滑到屏幕正中，则视为自动选中）
		gallery.setOnItemLongClickListener(ItemLongClickListener);
		// gallery.setUnselectedAlpha(0.8f); // 设置未选中图片的透明度
		gallery.setSpacing(20); // 设置图片之间的间距

		mSwitchView.setOnTouchListener(new TouchListener());
		mRecView.setOnTouchListener(new TouchListener());
		mBtCamera.setOnClickListener(new OnButtonClickListener());
		mBtPhoto.setOnClickListener(new OnButtonClickListener());
		mBtSetting.setOnClickListener(new OnButtonClickListener());
		slidingDrawer
				.setOnDrawerOpenListener(new SlidingDrawer.OnDrawerOpenListener() {

					public void onDrawerOpened() {
						mBtHandle
								.setBackgroundResource(R.drawable.delete_bt_selector);
					}
				});
		slidingDrawer
				.setOnDrawerCloseListener(new SlidingDrawer.OnDrawerCloseListener() {

					public void onDrawerClosed() {
						mImgAdapter.notifyDataSetChanged();
						mBtHandle
								.setBackgroundResource(R.drawable.add_bt_selector);
					}
				});

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Bitmap bmp = null;
		if (resultCode != RESULT_OK) {
			return;
		}
		switch (requestCode) {
		case Constants.CAMERA_WITH_DATA:
			if (mCameraImageUri != null) {
				mCurrentUri = mCameraImageUri;
				bmp = ImageUtils.loadBitmap(mCurrentUri.getPath(),
						mScreenWidth, mScreenHeight);
				mSwitchView.setImageDrawable(new BitmapDrawable(
						MainActivity.this.getResources(), bmp));
				gallery.setSelection(mImgAdapter.ls.size() * 100);
			}
			break;
		case Constants.PHOTO_WITH_DATA:
			mCurrentUri = data.getData();
			mCurrentUri = Uri.parse(FileManagerService
					.getFilePathByContentResolver(this, mCurrentUri));
			mPhotoImageUri = getUri();
			FileUtil.copyfile(new File(mCurrentUri.getPath()), new File(
					mPhotoImageUri.getPath()), true);
			mCurrentUri = mPhotoImageUri;
			bmp = ImageUtils.loadBitmap(mCurrentUri.getPath(), mScreenWidth,
					mScreenHeight);
			mSwitchView.setImageDrawable(new BitmapDrawable(MainActivity.this
					.getResources(), bmp));
			gallery.setSelection(mImgAdapter.ls.size() * 100);
			break;
		case Constants.CROP_PHOTO:
			if (mCropImageUri != null) {
				bmp = BitmapFactory.decodeFile(mCropImageUri.getPath());
				bmp = ImageUtils.binarization(bmp);
				String resultString = null;
				ImageUtils imgUtils = ImageUtils.getInstance();
				resultString = imgUtils.getTextFromPic(bmp);
				showLoadingWindow(resultString);
			}
			break;
		}
		mImgAdapter.notifyDataSetChanged();
	}

	AdapterView.OnItemLongClickListener ItemLongClickListener = new AdapterView.OnItemLongClickListener() {

		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view,
				int position, long id) {
			Intent intent = new Intent(MainActivity.this, PictureManagerActivity.class);
			startActivity(intent);
			return false;
		}
	};
	// 点击图片的监听事件
	AdapterView.OnItemClickListener clickListener = new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {

			if (mOldSelctedIndex == position) {
				return;
			}
			String filepath = mImgAdapter.ls
					.get((mImgAdapter.ls.size() + position)
							% mImgAdapter.ls.size()).get("fpath").toString();
			Bitmap bmp = ImageUtils.loadBitmap(filepath, mScreenWidth,
					mScreenHeight);
			mSwitchView.setInAnimation(AnimationUtils.loadAnimation(
					MainActivity.this, R.anim.slide_in_up));
			mSwitchView.setOutAnimation(AnimationUtils.loadAnimation(
					MainActivity.this, R.anim.slide_out_down));
			mSwitchView.setImageDrawable(new BitmapDrawable(MainActivity.this
					.getResources(), bmp));

			mOldSelctedIndex = position;
			mCurrentUri = Uri.fromFile(new File(filepath));
			mSeekBar.setProgress(100
					* ((mImgAdapter.ls.size() + position) % mImgAdapter.ls
							.size()) / mImgAdapter.ls.size());
		}
	};

	// 选中图片的监听事件
	AdapterView.OnItemSelectedListener selectedListener = new AdapterView.OnItemSelectedListener() {
		@Override
		public void onItemSelected(AdapterView<?> parent, View view,
				int position, long id) {
			mSeekBar.setProgress(100
					* ((mImgAdapter.ls.size() + position) % mImgAdapter.ls
							.size()) / mImgAdapter.ls.size());
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {

		}
	};

	private class OnButtonClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			Intent intent;
			switch (v.getId()) {
			case R.id.imgbtCamera:
				mCameraImageUri = getUri();
				intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				intent.putExtra(MediaStore.EXTRA_OUTPUT, mCameraImageUri);
				startActivityForResult(intent, Constants.CAMERA_WITH_DATA);
				break;
			case R.id.imgbtPhoto:
				intent = new Intent(Intent.ACTION_GET_CONTENT, null);
				intent.setType("image/*");
				startActivityForResult(intent, Constants.PHOTO_WITH_DATA);
				break;
			case R.id.settingBt:
				intent = new Intent(MainActivity.this, SettingsActivity.class);
				startActivity(intent);
				break;
			}
		}

	}

	private class seekBarListener implements OnSeekBarChangeListener {

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			if (fromUser) {
				gallery.setSelection(progress * mImgAdapter.ls.size() / 100
						+ mImgAdapter.ls.size() * 100);
			}
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public View makeView() {

		ImageView imgView = new ImageView(this);
		imgView.setScaleType(ImageView.ScaleType.CENTER);
		imgView.setLayoutParams(new ImageSwitcher.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		return imgView;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// if (keyCode == KeyEvent.KEYCODE_MENU) {
		// return true;
		// }
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (slidingDrawer.isOpened()) {
				slidingDrawer.close();
				return false;
			}
			Exit();
		}
		return false;
	}

	// 监听menu键和返回键
	private static Boolean isExit = false;

	/**
	 * 双击退出
	 */
	public void Exit() {
		Timer tExit = null;
		if (isExit == false) {
			isExit = true; // 准备退出
			Toast.makeText(this, getResources().getString(R.string.exit),
					Toast.LENGTH_SHORT).show();
			tExit = new Timer();
			tExit.schedule(new TimerTask() {
				@Override
				public void run() {
					isExit = false; // 取消退出
				}
			}, 2000); // 如果2秒钟内没有按下返回键，则启动定时器取消掉刚才执行的任务

		} else {
			finish();
			System.exit(0);
		}
	}

	private class TouchListener implements View.OnTouchListener {

		TouchHelp imgContentTouchHelp = null;

		TouchHelp recViewTouchHelp = null;

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if (mScreenWidth == 0) {
				int[] size = getMainContentSize(MainActivity.this);
				mScreenWidth = size[0];
				mScreenHeight = size[1];
			}
			switch (v.getId()) {
			/**
			 * 获取单双击事件和画矩形
			 */
			case R.id.imgContent:
				if (imgContentTouchHelp == null) {
					imgContentTouchHelp = new TouchHelp();
				}
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					if (imgContentTouchHelp.isDoubleClicked()) {
						// double clicked
						if (mCurrentUri == null) {
							Toast.makeText(MainActivity.this, "Please choose a picture!", Toast.LENGTH_LONG).show();
						} else {
							mCropImageUri = getTempUri();
							cropImageUri(mCropImageUri, Constants.CROP_PHOTO);
						}
					}
					imgContentTouchHelp.ActionDown(event);
					break;
				case MotionEvent.ACTION_UP:
					imgContentTouchHelp.ActionUp(event);
					break;
				case MotionEvent.ACTION_MOVE:
					imgContentTouchHelp.ActionMove(event);

					// 隐藏其他控件
					showMaskLayer(true);
					break;
				default:
					break;
				}
				imgContentTouchHelp.DrawRec(mRecView);
				return true;// end of imgContent

				/**
				 * 获取单双击事件，移动位置
				 */
			case R.id.recView:
				if (recViewTouchHelp == null) {
					recViewTouchHelp = new TouchHelp(mRecView,
							MainActivity.this);
				}
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					if (recViewTouchHelp.isDoubleClicked()) {
						if (mCurrentUri == null) {
							Toast.makeText(MainActivity.this, "Please choose a picture!", Toast.LENGTH_LONG).show();
						} else {
							showLoadingWindow(null);
						}
					}
					recViewTouchHelp.ActionDown(event);
					// 其他控件的显示状态切换
					// showMaskLayer(!mMaskState);
					break;
				case MotionEvent.ACTION_UP:
					break;
				case MotionEvent.ACTION_MOVE:
					recViewTouchHelp.ActionMove(event);
					// 隐藏其他控件
					showMaskLayer(true);
					break;
				default:
					break;
				}
				return true;// end of mRecView
				/**
				 * 自己的事自己干就OK了
				 */
				// case R.id.horListView:
				// return mHorListView.onTouchEvent(event);
			case R.id.imgSeekBar:
				return mSeekBar.onTouchEvent(event);
			case R.id.imgbtCamera:
				// return m.onTouchEvent(event);
			case R.id.imgbtPhoto:
				// return m.onTouchEvent(event);

			default:
				break;
			}
			return false;
		}

	}

	// 使用匿名内部类来复写Handler当中的handlerMessage()方法
	Handler LoadingBarHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.arg1) {
			case 1:
				LoadingView.dismiss();
				String result = msg.obj.toString();
				result.replaceAll(" ", "");
				if (result.equalsIgnoreCase("")) {
					Log.e("TAG", "Failed . . .");
				} else {
					Intent intent = new Intent(MainActivity.this,
							ResultActivity.class);
					intent.putExtra("result", result);
					startActivity(intent);
				}
				break;
			default:
				break;
			}
		}
	};

	// private File mPicFile;

	private void showLoadingWindow(final String str) {
		LoadingView.show(this, getResources().getString(R.string.caculating),
				true, true);
		new Thread(new Runnable() {
			@Override
			public void run() {
				Message msg = new Message();
				if (str == null) {
					msg.obj = PullOutNumber();
				} else {
					msg.obj = str;
				}
				msg.arg1 = 1;
				LoadingBarHandler.dispatchMessage(msg);
			}
		}).start();
	}

	/**
	 * 从图片中取出数字
	 * 
	 * @return
	 */
	public String PullOutNumber() {
		ImageUtils imgUtils = ImageUtils.getInstance();
		Bitmap img = null;
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(mCurrentUri.getPath(), opts);
		int be = ImageUtils.getImageRatio(mCurrentUri.getPath(), mScreenWidth,
				mScreenHeight);
		Log.e("TAG", "be=" + be);
		float imgLeft = (float) (0.5 * ((float) mScreenWidth - ((float) opts.outWidth / be)));
		float imgTop = (float) (0.5 * ((float) mScreenHeight - ((float) opts.outHeight / be)));
		float imgRight = (float) (0.5 * ((float) mScreenWidth + ((float) opts.outWidth / be)));
		float imgBottom = (float) (0.5 * ((float) mScreenHeight + ((float) opts.outHeight / be)));
		
		float x0,y0,x1,y1;
		if (mRecView.getLeft() < imgLeft) {
			x0 = imgLeft;
		}else if (mRecView.getLeft() > imgRight) {
			x0 = imgRight;
		}else {
			x0 = mRecView.getLeft();
		}
		
		if (mRecView.getTop() < imgTop) {
			y0 = imgTop;
		}else if (mRecView.getTop() > imgBottom) {
			y0 = imgBottom;
		}else {
			y0 = mRecView.getTop();
		}
		
		if (mRecView.getRight() < imgLeft) {
			x1 = imgLeft;
		}else if (mRecView.getRight() > imgRight) {
			x1 = imgRight;
		}else {
			x1 = mRecView.getRight();
		}
		
		if (mRecView.getBottom() < imgTop) {
			y1 = imgTop;
		}else if (mRecView.getBottom() > imgBottom) {
			y1 = imgBottom;
		}else {
			y1 = mRecView.getBottom();
		}
		
		int left = (int) (be * (x0 - imgLeft));
		int top = (int) (be * (y0 - imgTop));
		int right = (int) (be * (x1 - imgLeft));
		int bottom = (int) (be * (y1 - imgTop));
		
		Rect r = new Rect(left, top, right, bottom);
		if (r.width() < 1 || r.height() < 1) {
			return "";
		}
		img = ImageUtils.cutBitmapByPath(mCurrentUri.getPath(), r,
				Bitmap.Config.ARGB_8888);
		FileUtil.saveBitmap(img);
		img = ImageUtils.binarization(img);
		String resultString = null;
		resultString = imgUtils.getTextFromPic(img);
		return resultString;
	}

	/**
	 * 获取屏幕宽高
	 * 
	 * @param activity
	 * @return int[0] 宽，int[1]高
	 */
	public int[] getScreenWidthAndSizeInPx(Activity activity) {
		DisplayMetrics displayMetrics = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay()
				.getMetrics(displayMetrics);
		int[] size = new int[2];
		size[0] = displayMetrics.widthPixels;
		size[1] = displayMetrics.heightPixels;
		return size;
	}

	/**
	 * 
	 * 获取状态栏高度
	 * 
	 * @param activity
	 * @return
	 */
	public int getStateHeight(Activity activity) {
		Rect rect = new Rect();
		activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
		return rect.top;
	}

	/**
	 * 获取标题栏的高度
	 * 
	 * @param activity
	 * @return
	 */
	public int getTitleHeight(Activity activity) {
		Rect rect = new Rect();
		Window window = activity.getWindow();
		window.getDecorView().getWindowVisibleDisplayFrame(rect);
		int statusBarHeight = rect.top;
		int contentViewTop = window.findViewById(Window.ID_ANDROID_CONTENT)
				.getTop();
		int titleBarHeight = contentViewTop - statusBarHeight;

		return titleBarHeight;
	}

	/**
	 * 获取实际应用的宽、高
	 * 
	 * @param activity
	 * @return int[0] 宽，int[1]高
	 */
	public int[] getMainContentSize(Activity activity) {
		int[] size = new int[2];
		size = getScreenWidthAndSizeInPx(activity);
		size[1] = size[1] - getTitleHeight(activity) - getStateHeight(activity);
		return size;
	}

	private void showMaskLayer(boolean state) {
		if (state) {
			if (slidingDrawer.isOpened()) {
				slidingDrawer.close();
			}
		}
	}

	/**
	 * 创建新文件时取得一个新URI
	 * 
	 * @return
	 */
	private Uri getUri() {
		Date date = new Date(System.currentTimeMillis());
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss",
				Locale.CHINESE);
		File picFile = new File(Constants.PICTURES_PATH,
				Constants.APP_PREFERENCES_NAME + dateFormat.format(date)
						+ ".jpg");
		if (!picFile.exists()) {
			try {
				picFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return Uri.fromFile(picFile);
	}

	private Uri getTempUri() {
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
		return Uri.fromFile(picFile);
	}

	/**
	 * 调用系统截图
	 * 
	 * @param uri
	 * @param outputX
	 * @param outputY
	 * @param requestCode
	 */
	private void cropImageUri(Uri uri, int requestCode) {
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(mCurrentUri, "image/*");
		intent.putExtra("crop", "true");
		// intent.putExtra("aspectX", 1);
		// intent.putExtra("aspectY", 1);
		// intent.putExtra("outputX", outputX);
		// intent.putExtra("outputY", outputY);
		intent.putExtra("scale", true);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
		intent.putExtra("return-data", false);
		intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
		intent.putExtra("noFaceDetection", true); // no face detection
		startActivityForResult(intent, requestCode);
	}

	private void initFiles() {
		if (LoginUtils.isFirstLogin(this)) {
			FileManagerService fileService = new FileManagerService(this);
			fileService.copyFiles();
		}
	}

	private void findViews() {
		mSwitchView = (ImageSwitcher) findViewById(R.id.imgContent);
		mRecView = (ImageView) findViewById(R.id.recView);
		mBtSetting = (ImageView) findViewById(R.id.settingBt);		
		slidingDrawer = (SlidingDrawer) findViewById(R.id.slidingDrawer_ref);
		mBtHandle = (ImageButton) findViewById(R.id.btHandle);
		mSeekBar = (MySeekBar) findViewById(R.id.imgSeekBar);
		mBtPhoto = (ImageButton) findViewById(R.id.imgbtPhoto);
		mBtCamera = (ImageButton) findViewById(R.id.imgbtCamera);
		// 获取Gallery对象
		gallery = (Gallery) findViewById(R.id.gallery);

	}

}
