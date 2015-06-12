/** 
 * Filename:    ImageItemView.java 
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

import java.io.File;

import com.baidu.lightgame.plugin.common.GameHelperExt;

import android.content.Context;
import android.util.Log;
import android.widget.ImageView;

public class ImageItemView extends ImageView{

	@SuppressWarnings("unused")
	private int startTime = 0;
	
	@SuppressWarnings("unused")
	private int durationTime = 0;
	
	
	public ImageItemView(Context context) {
		super(context);
	}
	
	public ImageItemView(Context context , int id){
		super(context);
		this.setImageResource(id);
	}
	
	@SuppressWarnings("deprecation")
	public ImageItemView(Context context , File imgpath){
		super(context);
		Log.d("File exist", "   "+imgpath.exists());
		this.setBackgroundDrawable(GameHelperExt.readDrawableFromFile(imgpath));
	}
	
	public ImageItemView(Context context , int id , int starttime , int duration){
		super(context);
		this.setImageResource(id);
		startTime  = starttime;				//start the animation time.
		durationTime = duration;			//duration time
	}
}
