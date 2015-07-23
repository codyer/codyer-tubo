package com.codyer.tubo.utils;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.googlecode.tesseract.android.TessBaseAPI;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.util.Log;
import android.view.View;

public class ImageUtils {
	private static TessBaseAPI baseApi = null;
	private static ImageUtils instance = null;

	private ImageUtils() {
		if (baseApi == null) {
			baseApi = new TessBaseAPI();
			baseApi.init(Constants.TESSBASE_PATH, Constants.DEFAULT_LANGUAGE);
		}
	}

	public static ImageUtils getInstance() {
		if (instance == null)
			instance = new ImageUtils();
		return instance;
	}

	/**
	 * 通过图片获取图片上的内容
	 * 
	 * @param bmp
	 *            包含文字的图片资源
	 * @return 图片中的字符
	 */
	public String getTextFromPic(Bitmap bmp) {

		if (baseApi == null || bmp == null) {
			return null;
		}

		bmp = bmp.copy(Bitmap.Config.ARGB_8888, false);
		baseApi.setImage(bmp);

		String value = baseApi.getUTF8Text();
		Log.d(Constants.TAG, " the value is ===> " + value);
		baseApi.clear();
		// baseApi.end();
		return value;
	}

	public static Bitmap cutBitmapByPath(String path, Rect r,
			Bitmap.Config config) {
		int width = r.width();
		int height = r.height();
		Bitmap mBitmap;
		if (width <= 0 || height <= 0) {
			return null;
		}
		Bitmap croppedImage = Bitmap.createBitmap(width, height, config);

		Canvas cvs = new Canvas(croppedImage);
		Rect dr = new Rect(0, 0, width, height);
		try {
			mBitmap = BitmapFactory.decodeFile(path);
			cvs.drawBitmap(mBitmap, r, dr, null);
			if (!mBitmap.isRecycled()) {
				mBitmap.recycle();
				mBitmap = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return croppedImage;
	}

	public static Bitmap cutBitmap(Bitmap mBitmap, Rect r, Bitmap.Config config) {
		int width = r.width();
		int height = r.height();

		if (width <= 0 || height <= 0) {
			return null;
		}
		Bitmap croppedImage = Bitmap.createBitmap(width, height, config);

		Canvas cvs = new Canvas(croppedImage);
		Rect dr = new Rect(0, 0, width, height);

		cvs.drawBitmap(mBitmap, r, dr, null);

		return croppedImage;
	}

	/**
	 * 转换图片成圆形
	 * 
	 * @param bitmap
	 *            传入Bitmap对象
	 * @return
	 */
	public Bitmap toRoundBitmap(Bitmap bitmap) {
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		float roundPx;
		float left, top, right, bottom, dst_left, dst_top, dst_right, dst_bottom;

		// 无非就是计算圆形区域
		if (width <= height) {
			roundPx = width / 2;
			float clip = (height - width) / 2;
			top = clip;
			bottom = width + clip;
			left = 0;
			right = width;

			height = width;

			dst_left = 0;
			dst_top = 0;
			dst_right = width;
			dst_bottom = width;
		} else {
			roundPx = height / 2;
			float clip = (width - height) / 2;

			left = clip;
			right = height + clip;
			top = 0;
			bottom = height;

			width = height;

			dst_left = 0;
			dst_top = 0;
			dst_right = height;
			dst_bottom = height;
		}

		Bitmap output = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		Canvas canvas = new Canvas(output);

		final Paint paint = new Paint();
		final Rect src = new Rect((int) left, (int) top, (int) right,
				(int) bottom);
		final Rect dst = new Rect((int) dst_left, (int) dst_top,
				(int) dst_right, (int) dst_bottom);
		final RectF rectF = new RectF(dst);

		paint.setAntiAlias(true);

		paint.setColor(0xFFFFFFFF);
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, src, dst, paint);
		return output;
	}

	/**
	 * 加载图片 ，按照照相时的旋转角度
	 * 
	 * @param pathName
	 * @param reqwidth
	 * @param reqHeight
	 * @return 压缩后的图片
	 */
	public static Bitmap loadBitmap(String pathName, int reqwidth, int reqHeight) {
		BitmapFactory.Options opts = new BitmapFactory.Options();

		if (reqwidth > 0 && reqHeight > 0) {
			opts.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(pathName, opts);

			int widthRatio = (int) Math.ceil(opts.outWidth / (float) reqwidth);
			int heightRatio = (int) Math.ceil(opts.outHeight
					/ (float) reqHeight);
			if (widthRatio > 1 && heightRatio > 1) {
				opts.inSampleSize = widthRatio > heightRatio ? widthRatio
						: heightRatio;
				// 只能是2的次幂
				if (opts.inSampleSize < 2)
					opts.inSampleSize = 1;
				else if (opts.inSampleSize < 3)
					opts.inSampleSize = 2;
				else if (opts.inSampleSize < 5)
					opts.inSampleSize = 4;
				else if (opts.inSampleSize < 9)
					opts.inSampleSize = 8;
				else
					opts.inSampleSize = 16;
			}

		}

		opts.inJustDecodeBounds = false;
		Bitmap b = BitmapFactory.decodeFile(pathName, opts);

		int degree = getRotationOfExif(pathName);
		if (degree != 0) {
			Matrix m = new Matrix();
			m.postRotate(degree);
			b = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), m,
					true);
		}
		return b;
	}

	/**
	 * 获取图片的压缩比例
	 * 
	 * @param pathName
	 * @param reqwidth
	 * @param reqHeight
	 * @return
	 */
	public static int getImageRatio(String pathName, int reqwidth, int reqHeight) {
		BitmapFactory.Options opts = new BitmapFactory.Options();

		if (reqwidth > 0 && reqHeight > 0 && pathName != null) {
			opts.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(pathName, opts);

			int widthRatio = (int) Math.ceil(opts.outWidth / (float) reqwidth);
			int heightRatio = (int) Math.ceil(opts.outHeight
					/ (float) reqHeight);
			if (widthRatio > 1 && heightRatio > 1) {
				opts.inSampleSize = widthRatio > heightRatio ? widthRatio
						: heightRatio;
				// 只能是2的次幂
				if (opts.inSampleSize < 2)
					opts.inSampleSize = 1;
				else if (opts.inSampleSize < 3)
					opts.inSampleSize = 2;
				else if (opts.inSampleSize < 5)
					opts.inSampleSize = 4;
				else if (opts.inSampleSize < 9)
					opts.inSampleSize = 8;
				else
					opts.inSampleSize = 16;

				return opts.inSampleSize;
			}
		}
		return 1;
	}

	/**
	 * 取得图片的旋转角度
	 * 
	 * @param pathName
	 * @return
	 */
	public static int getRotationOfExif(String pathName) {
		int degree = 0;
		try {
			ExifInterface exif = new ExifInterface(pathName);
			int orientation = exif.getAttributeInt(
					ExifInterface.TAG_ORIENTATION,
					ExifInterface.ORIENTATION_UNDEFINED);

			switch (orientation) {
			case ExifInterface.ORIENTATION_ROTATE_90:
				degree = 90;
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				degree = 180;
				break;
			case ExifInterface.ORIENTATION_ROTATE_270:
				degree = 270;
				break;
			default:
				degree = 0;
				break;
			}
		} catch (IOException e) {
			degree = 0;
		}

		return degree;
	}

	/**
	 * 获得图片的缩略图
	 * 
	 * @param imagePath
	 *            图片的路径
	 * @param width
	 *            获取到的缩略图的宽度
	 * @param height
	 *            获取到的缩略图的高度
	 * @return
	 */
	public static Bitmap getImageThumbnail(String imagePath, int width,
			int height) {
		Bitmap bitmap = null;
        Bitmap newBitmap = null;
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inPreferredConfig = Bitmap.Config.ARGB_4444;
		options.inPurgeable = true;
		options.inInputShareable = true;
		options.inJustDecodeBounds = true;
		// 获取这个图片的宽和高，注意此处的bitmap为null
		bitmap = BitmapFactory.decodeFile(imagePath, options);
		options.inJustDecodeBounds = false; // 设为 false
		// 计算缩放比
		int h = options.outHeight;
		int w = options.outWidth;
		int beWidth = w / width;
		int beHeight = h / height;
		int be = 1;
		if (beWidth < beHeight) {
			be = beWidth;
		} else {
			be = beHeight;
		}
		if (be <= 0) {
			be = 1;
		}
		options.inSampleSize = be;
		// 重新读入图片，读取缩放后的bitmap，注意这次要把options.inJustDecodeBounds 设为 false
		bitmap = BitmapFactory.decodeFile(imagePath, options);
		// 利用ThumbnailUtils来创建缩略图，这里要指定要缩放哪个Bitmap对象
		newBitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
				ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
		bitmap.recycle();
		return newBitmap;
	}

	/**
	 * 图片与边框组合
	 * 
	 * @param bm
	 *            原图片
	 * @param res
	 *            边框资源
	 * @return
	 */
	public static Bitmap combinateFrame(Bitmap bm, Resources res) {
		Bitmap bmp = BitmapFactory.decodeResource(res, 0);
		// 边框的宽高
		final int smallW = bmp.getWidth();
		final int smallH = bmp.getHeight();

		// 原图片的宽高
		final int bigW = bm.getWidth();
		final int bigH = bm.getHeight();

		int wCount = (int) Math.ceil(bigW * 1.0 / smallW);
		int hCount = (int) Math.ceil(bigH * 1.0 / smallH);

		// 组合后图片的宽高
		int newW = (wCount + 2) * smallW;
		int newH = (hCount + 2) * smallH;

		// 重新定义大小
		Bitmap newBitmap = Bitmap.createBitmap(newW, newH, Config.ARGB_8888);
		Canvas canvas = new Canvas(newBitmap);
		Paint p = new Paint();
		p.setColor(Color.TRANSPARENT);
		canvas.drawRect(new Rect(0, 0, newW, newH), p);

		Rect rect = new Rect(smallW, smallH, newW - smallW, newH - smallH);
		Paint paint = new Paint();
		paint.setColor(Color.WHITE);
		canvas.drawRect(rect, paint);

		// 绘原图
		canvas.drawBitmap(bm, (newW - bigW - 2 * smallW) / 2 + smallW, (newH
				- bigH - 2 * smallH)
				/ 2 + smallH, null);
		// 绘边框
		// 绘四个角
		int startW = newW - smallW;
		int startH = newH - smallH;
		Bitmap leftTopBm = BitmapFactory.decodeResource(res, 0); // 左上角
		Bitmap leftBottomBm = BitmapFactory.decodeResource(res, 2); // 左下角
		Bitmap rightBottomBm = BitmapFactory.decodeResource(res, 4); // 右下角
		Bitmap rightTopBm = BitmapFactory.decodeResource(res, 6); // 右上角

		canvas.drawBitmap(leftTopBm, 0, 0, null);
		canvas.drawBitmap(leftBottomBm, 0, startH, null);
		canvas.drawBitmap(rightBottomBm, startW, startH, null);
		canvas.drawBitmap(rightTopBm, startW, 0, null);

		leftTopBm.recycle();
		leftTopBm = null;
		leftBottomBm.recycle();
		leftBottomBm = null;
		rightBottomBm.recycle();
		rightBottomBm = null;
		rightTopBm.recycle();
		rightTopBm = null;

		// 绘左右边框
		Bitmap leftBm = BitmapFactory.decodeResource(res, 1);
		Bitmap rightBm = BitmapFactory.decodeResource(res, 5);
		for (int i = 0, length = hCount; i < length; i++) {
			int h = smallH * (i + 1);
			canvas.drawBitmap(leftBm, 0, h, null);
			canvas.drawBitmap(rightBm, startW, h, null);
		}

		leftBm.recycle();
		leftBm = null;
		rightBm.recycle();
		rightBm = null;

		// 绘上下边框
		Bitmap bottomBm = BitmapFactory.decodeResource(res, 3);
		Bitmap topBm = BitmapFactory.decodeResource(res, 7);
		for (int i = 0, length = wCount; i < length; i++) {
			int w = smallW * (i + 1);
			canvas.drawBitmap(bottomBm, w, startH, null);
			canvas.drawBitmap(topBm, w, 0, null);
		}

		bottomBm.recycle();
		bottomBm = null;
		topBm.recycle();
		topBm = null;

		canvas.save(Canvas.ALL_SAVE_FLAG);
		canvas.restore();

		return newBitmap;
	}

	public boolean isNumber(String str) {
		Pattern pattern = Pattern.compile("[0-9]+");
		Matcher matcher = pattern.matcher((CharSequence) str);
		boolean result = matcher.matches();
		if (result) {
			System.out.println("[" + str + "] is a Number.");
		} else {
			System.out.println("[" + str + "] is not a Number.");
		}
		return result;
	}

	public static Bitmap binarization(Bitmap img) {
		int width = img.getWidth();
		int height = img.getHeight();
		int area = width * height;
		int gray[][] = new int[width][height];
		int average = 0;// 灰度平均值
		int graysum = 0;
		int graymean = 0;
		int grayfrontmean = 0;
		int graybackmean = 0;
		int pixelGray;
		int front = 0;
		int back = 0;
		int[] pix = new int[width * height];
		img.getPixels(pix, 0, width, 0, 0, width, height);
		for (int i = 1; i < width; i++) { // 不算边界行和列，为避免越界
			for (int j = 1; j < height; j++) {
				int x = j * width + i;
				int r = (pix[x] >> 16) & 0xff;
				int g = (pix[x] >> 8) & 0xff;
				int b = pix[x] & 0xff;
				pixelGray = (int) (0.3 * r + 0.59 * g + 0.11 * b);// 计算每个坐标点的灰度
				gray[i][j] = (pixelGray << 16) + (pixelGray << 8) + (pixelGray);
				graysum += pixelGray;
			}
		}
		graymean = (int) (graysum / area);// 整个图的灰度平均值
		average = graymean;
		Log.i("TAG", "Average:" + average);
		for (int i = 0; i < width; i++) // 计算整个图的二值化阈值
		{
			for (int j = 0; j < height; j++) {
				if (((gray[i][j]) & (0x0000ff)) < graymean) {
					graybackmean += ((gray[i][j]) & (0x0000ff));
					back++;
				} else {
					grayfrontmean += ((gray[i][j]) & (0x0000ff));
					front++;
				}
			}
		}
		int frontvalue = (int) (grayfrontmean / front);// 前景中心
		int backvalue = (int) (graybackmean / back);// 背景中心
		float G[] = new float[frontvalue - backvalue + 1];// 方差数组
		int s = 0;
		Log.i("TAG", "Front:" + front + "**Frontvalue:" + frontvalue
				+ "**Backvalue:" + backvalue);
		for (int i1 = backvalue; i1 < frontvalue + 1; i1++)// 以前景中心和背景中心为区间采用大津法算法（OTSU算法）
		{
			back = 0;
			front = 0;
			grayfrontmean = 0;
			graybackmean = 0;
			for (int i = 0; i < width; i++) {
				for (int j = 0; j < height; j++) {
					if (((gray[i][j]) & (0x0000ff)) < (i1 + 1)) {
						graybackmean += ((gray[i][j]) & (0x0000ff));
						back++;
					} else {
						grayfrontmean += ((gray[i][j]) & (0x0000ff));
						front++;
					}
				}
			}
			grayfrontmean = (int) (grayfrontmean / front);
			graybackmean = (int) (graybackmean / back);
			G[s] = (((float) back / area) * (graybackmean - average)
					* (graybackmean - average) + ((float) front / area)
					* (grayfrontmean - average) * (grayfrontmean - average));
			s++;
		}
		float max = G[0];
		int index = 0;
		for (int i = 1; i < frontvalue - backvalue + 1; i++) {
			if (max < G[i]) {
				max = G[i];
				index = i;
			}
		}

		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				int in = j * width + i;
				if (((gray[i][j]) & (0x0000ff)) < (index + backvalue)) {
					pix[in] = Color.rgb(0, 0, 0);
				} else {
					pix[in] = Color.rgb(255, 255, 255);
				}
			}
		}

		Bitmap temp = Bitmap.createBitmap(width, height,
				Bitmap.Config.ARGB_8888);
		temp.setPixels(pix, 0, width, 0, 0, width, height);
		return temp;
	}

	@SuppressWarnings("deprecation")
	public static Bitmap shot(Activity activity) {
		// View是你需要截图的View
		View view = activity.getWindow().getDecorView();
		view.setDrawingCacheEnabled(true);
		view.buildDrawingCache();
		Bitmap b1 = view.getDrawingCache();
		// 获取状态栏高度 /
		Rect frame = new Rect();
		activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
		int statusBarHeight = frame.top;
		Log.i("TAG", "" + statusBarHeight);
		// 获取屏幕长和高
		int width = activity.getWindowManager().getDefaultDisplay().getWidth();
		int height = activity.getWindowManager().getDefaultDisplay()
				.getHeight();
		// 去掉标题栏
		// Bitmap b = Bitmap.createBitmap(b1, 0, 25, 320, 455);
		Bitmap b = Bitmap.createBitmap(b1, 0, statusBarHeight, width, height
				- statusBarHeight);
		view.destroyDrawingCache();
		return b;
	}
}
