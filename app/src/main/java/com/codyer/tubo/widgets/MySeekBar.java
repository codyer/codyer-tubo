package com.codyer.tubo.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.SeekBar;
public class MySeekBar extends SeekBar {

	/**
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public MySeekBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	/**
	 * @param context
	 * @param attrs
	 */
	public MySeekBar(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		super.dispatchTouchEvent(event);
		return true;
	}

	public MySeekBar(Context context) {
		super(context);
	}
	

}
