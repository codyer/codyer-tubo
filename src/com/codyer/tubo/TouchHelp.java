package com.codyer.tubo;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

public class TouchHelp {

	private View mView;
	private int mScreenWidth;
	private int mScreenHeight;

	/**
	 * Double Touched Members
	 */
	private int mCount;
	private long mFirClick;
	private long mSecClick;

	/**
	 * Draw Rectangle Members
	 */
	private float mFirstX;
	private float mFirstY;
	private float mSecondX;
	private float mSecondY;
	private boolean mDrawing;

	/**
	 * Move Rectangle Members
	 */
	private float mLastX;
	private float mLastY;

	/**
	 * 移动位置 单击 双击 画矩形
	 * 
	 * @param mView
	 *            需要移动的View
	 * @param context
	 *            上下文，用来取得窗口宽和高
	 */
	public TouchHelp(View mView, Context context) {
		this.mView = mView;
		DisplayMetrics dm = context.getResources().getDisplayMetrics();
		mScreenWidth = dm.widthPixels;
		mScreenHeight = dm.heightPixels;
	}

	/**
	 * @param 可以判单击双击
	 *            可以在上面画矩形
	 */
	public TouchHelp() {
		this.mView = null;
	}

	/**
	 * false for single click true for double click in a action_down event , you
	 * can get the result
	 * 
	 * @return
	 */
	public boolean isDoubleClicked() {
		mCount++;
		if (mCount == 1) {
			mFirClick = System.currentTimeMillis();

		} else if (mCount == 2) {
			mSecClick = System.currentTimeMillis();
			if (mSecClick - mFirClick < 300) {
				// double clicked
				mCount = 0;
				mFirClick = 0;
				mSecClick = 0;
				return true;
			}
			mCount = 1;
			mFirClick = mSecClick;
			mSecClick = 0;
		}
		return false;
	}

	public void ActionDown(MotionEvent event) {
		/**
		 * For Draw Rectangle
		 */
		mFirstX = event.getRawX();
		mFirstY = event.getRawY();

		/**
		 * For Move View
		 */
		if (this.mView != null) {
			mLastX = event.getRawX();
			mLastY = event.getRawY();
		}
	}

	public void ActionUp(MotionEvent event) {
		/**
		 * For Draw Rectangle
		 */
		mDrawing = false;
	}

	public void ActionMove(MotionEvent event) {
		/**
		 * For Draw Rectangle
		 */
		mDrawing = true;
		mSecondX = event.getRawX();
		mSecondY = event.getRawY();

		/**
		 * For Move View
		 */
		if (this.mView != null) {
			float dx = event.getRawX() - mLastX;
			float dy = event.getRawY() - mLastY;

			int left = (int) (mView.getLeft() + dx);
			int top = (int) (mView.getTop() + dy);
			int right = (int) (mView.getRight() + dx);
			int bottom = (int) (mView.getBottom() + dy);
			if (left < 0) {
				left = 0;
				right = left + mView.getWidth();
			}
			if (right > mScreenWidth) {
				right = mScreenWidth;
				left = right - mView.getWidth();
			}
			if (top < 0) {
				top = 0;
				bottom = top + mView.getHeight();
			}
			if (bottom > mScreenHeight) {
				bottom = mScreenHeight;
				top = bottom - mView.getHeight();
			}
			mView.layout(left, top, right, bottom);
			mLastX = event.getRawX();
			mLastY = event.getRawY();
		}
	}

	/**
	 * For Draw Rectangle
	 * 
	 * @param mRecView
	 *            Rectangle Resource
	 */
	public void DrawRec(ImageView mRecView) {
		if (mDrawing && mRecView != null) {
			int left = (int) (mFirstX > mSecondX ? mSecondX : mFirstX);
			int top = (int) (mFirstY > mSecondY ? mSecondY : mFirstY);
			int right = (int) (mFirstX > mSecondX ? mFirstX : mSecondX);
			int bottom = (int) (mFirstY > mSecondY ? mFirstY : mSecondY);

			mRecView.setVisibility(View.VISIBLE);
			mRecView.getBackground().setAlpha(100);
			mRecView.layout(left, top, right, bottom);
			mRecView.setDrawingCacheEnabled(true);
		}
	}

}
