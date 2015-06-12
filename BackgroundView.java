package com.baidu.lightgame.plugin.view.floatwindow;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

public class BackgroundView extends View{

	private final static String TAG = "NewFloatWindow-cjf";
	private WindowManager wm = (WindowManager)getContext().
			getApplicationContext().getSystemService("window");
	
	@SuppressWarnings("deprecation")
	private int ScreenWidth = wm.getDefaultDisplay().getWidth();
	@SuppressWarnings("deprecation")
	private int ScreenHeight =wm.getDefaultDisplay().getHeight();
	
	private FloatActionMenu parentMenu = null;
	
	private Paint mPaint = null;
	
	public BackgroundView(Context context) {
		super(context);
		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		this.setClickable(true);
	}
	
	public void setMenu(FloatActionMenu menu){
		this.parentMenu = menu;
	}
	
	@SuppressWarnings("deprecation")
	public int getwidth(){
		return wm.getDefaultDisplay().getWidth();
	}
	
	@SuppressWarnings("deprecation")
	public int getheight(){
		return wm.getDefaultDisplay().getHeight();
	}
	
	@SuppressLint("DrawAllocation")
	protected void onDraw(Canvas canvas){
		mPaint.setColor(Color.BLACK);
		mPaint.setAlpha(140);
		Rect rect = new Rect(0,0,ScreenWidth , ScreenHeight);
		canvas.drawRect(rect, mPaint);
	}
	
	@SuppressLint("ClickableViewAccessibility")
	public boolean onTouchEvent(MotionEvent event){
		if(event.getAction()==MotionEvent.ACTION_UP){
			Log.d(TAG , "Background click to close the menu");
			if(parentMenu !=null) parentMenu.close(true);
			else{ 
				Log.d(TAG , "menu is null");
			}
		}
		return true;
	}
}
