/** 
 * Filename:    TestItemView.java 
 * Description:  
 * Copyright:   Baidu MIC Copyright(c)2015 
 * @author:     chiajianfeng
 * @version:    1.0 
 * Create at:   2015-5-13
 * 
 * Modification History: 
 * Date         Author         Version      Description 
 * -----------------------------------------------------
 * 2015-5-13   chijianfeng      1.0         1.0 Version 
 */
package com.baidu.lightgame.plugin.view.floatwindow;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;

public class TextItemView extends View{

	private static final String TAG = "TextItemView-chijianfeng";
	
	private String showText;
	
	private Rect tRect = new Rect(0,0,FloatApplication.MENUTXTWIDTH,FloatApplication.MENUTXTHEIGHT);	//default rect

	public TextItemView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public TextItemView(Context context , int id) {
		super(context);
		showText = getContext().getString(id);
	}
	
	public TextItemView(Context context , String str){
		super(context);
		showText = str;
		tRect = new Rect(0,
						 0,
						 FloatApplication.dip2px(getContext(), FloatApplication.MENUTXTWIDTH),
						 FloatApplication.dip2px(getContext(), FloatApplication.MENUIMGHEIGHT));
	}
	
	public void SetShowRect(Rect r){
		tRect = r;
	}
	
	public void SetText(String str){
		showText = str;
	}
	
	@SuppressLint("DrawAllocation")
	protected void onDraw(Canvas canvas){ 
		super.onDraw(canvas);
		if(showText==null){
			Log.e(TAG, "the Text is null");
			return;
		}
		Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaint.setTextSize(FloatApplication.sp2px(getContext(), FloatApplication.FONTSIZE));
		mPaint.setColor(Color.WHITE);
		FontMetricsInt fontMetrics = mPaint.getFontMetricsInt();
		
		int baseline = tRect.top + (tRect.bottom - tRect.top - fontMetrics.bottom + fontMetrics.top)/2
				- fontMetrics.top;
		mPaint.setTextAlign(Paint.Align.CENTER);
		canvas.drawText(showText, tRect.centerX(), baseline,mPaint);
	}
}
