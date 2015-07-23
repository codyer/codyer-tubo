package com.codyer.tubo.service;

import java.util.ArrayList;
import java.util.List;

import com.codyer.tubo.MainActivity;
import com.codyer.tubo.service.ShakeListener.OnShakeListener;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Handler;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

public class ShakeService extends Service {
	private static final String TAG = "ShakeService";
	ShakeListener mShakeListener = null;
	 Vibrator mVibrator;
	private long lastTime;

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		Log.e(TAG, "onCreate");
		super.onCreate();
		// 获得振动器服务
		 mVibrator = (Vibrator) getApplication().getSystemService(
		 VIBRATOR_SERVICE);

		// 实例化加速度传感器检测类
		mShakeListener = new ShakeListener(ShakeService.this);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.e(TAG, "onDestory");
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		mShakeListener.setOnShakeListener(new OnShakeListener() {

			public void onShake() {
				long currentTime = System.currentTimeMillis();
				long inteval = currentTime - lastTime;
				lastTime = currentTime;
				Log.e("tag", "onShake()---lastTime = "+lastTime + "inteval="+inteval);
				if (inteval < 500 && isAtHome()) {
//				if (inteval < 500) {
					mShakeListener.stop();
					startVibrato(); // 开始 震动
					new Handler().postDelayed(new Runnable() {
						@Override
						public void run() {
//							ImageUtils.shot(gets)
							Intent intent = new Intent();
							intent.setClass(ShakeService.this,
									MainActivity.class);
							intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							startActivity(intent);
							 Toast mtoast;
							 mtoast = Toast.makeText(ShakeService.this,
							 "呵呵，成功了！。\n再试一次吧！", Toast.LENGTH_LONG);
							 mtoast.show();
							 mVibrator.cancel();
							mShakeListener.start();
						}
					}, 2000);
				}
			}
		});
	}

	// 定义震动
	public void startVibrato() {
		 mVibrator.vibrate(new long[] { 500, 200, 500, 200 }, -1);
//		 第一个｛｝里面是节奏数组，
//		 第二个参数是重复次数，-1为不重复，非-1则从pattern的指定下标开始重复
	}

	/**
	 * 获得属于桌面的应用的应用包名称
	 * 
	 * @return 返回包含所有包名的字符串列表
	 */
	private List<String> getHomes() {
		List<String> names = new ArrayList<String>();
		PackageManager packageManager = this.getPackageManager();
		// 属性
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_HOME);
		List<ResolveInfo> resolveInfo = packageManager.queryIntentActivities(
				intent, PackageManager.MATCH_DEFAULT_ONLY);
		for (ResolveInfo ri : resolveInfo) {
			names.add(ri.activityInfo.packageName);
			System.out.println(ri.activityInfo.packageName);
		}
		return names;
	}

	/**
	 * 判断是否在桌面，只有在桌面的时候才触发传感器
	 * 
	 * @return
	 */
	private boolean isAtHome() {
		boolean atHome;
		ActivityManager mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> rti = mActivityManager.getRunningTasks(1);
		atHome = getHomes().contains(rti.get(0).topActivity.getPackageName());
		return atHome;
	}
}
