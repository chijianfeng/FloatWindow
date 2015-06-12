package com.baidu.lightgame.plugin.view.floatwindow;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

/**
 * A simple button implementation with a similar look an feel to{@link FloatActionButton}.
 */
@SuppressLint({ "RtlHardcoded", "ClickableViewAccessibility" })
public class SubActionButton extends LinearLayout{
	
	private final static String TAG = "NewFloatWindow-cjf";
	private View contentView;
	private View textView;
	private FloatAction floatAction;
	
	private boolean isdraw;
	private LinearLayout.LayoutParams layoutParams;
	@SuppressLint("NewApi")
	public SubActionButton(Context context, 
							LinearLayout.LayoutParams layoutParams,
							int direction, 
							Drawable backgroundDrawable, 
							View contentView, 
							View textView,
							LinearLayout.LayoutParams contentParams,
							LinearLayout.LayoutParams textParams,
							FloatAction action) {
        super(context);
        this.setOrientation(LinearLayout.HORIZONTAL);
        this.floatAction = action;
        this.layoutParams = layoutParams;
        setLayoutParams(layoutParams);
        if(backgroundDrawable!=null){
        	setBackgroundResource(backgroundDrawable.mutate().getConstantState().newDrawable());
        }
        if(contentView != null) {
            setContentView(contentView,textView, contentParams , textParams ,direction);
        }
        setClickable(true);
        setWillNotDraw(false);			//low effective
    }

    /**
     * Sets a content view with custom LayoutParams that will be displayed inside this SubActionButton.
     * @param contentView
     * @param textView
     * @param params
     * @param textparams
     * @param direction
     */
    public void setContentView(View contentView,
    							View textView , 
    							LinearLayout.LayoutParams params , 
    							LinearLayout.LayoutParams textparams,
    							int dir) {
        if(params == null) {
        	final int margin = FloatApplication.sp2px(getContext(), (FloatApplication.VButtonWidth-FloatApplication.MENUIMGWIDTH)/2);
        	final int width = FloatApplication.sp2px(getContext(), FloatApplication.MENUIMGWIDTH);
        	final int height = FloatApplication.sp2px(getContext(), FloatApplication.MENUIMGHEIGHT);
        	if(dir==FloatApplication.LEFT){
        		params = new LinearLayout.LayoutParams(width, height, Gravity.LEFT);
        		params.setMargins(margin, 0, 0, 0);
        	}
        	else{
        		params = new LinearLayout.LayoutParams(width, height, Gravity.LEFT);
        		params.setMargins(FloatApplication.sp2px(getContext(),
        						FloatApplication.MENUTXTWIDTH+FloatApplication.TEXT2IMGDISTANCE)
        				        , 0, 0, 0);
        	}
        }
        if(textparams==null){
        	final int margin = FloatApplication.sp2px(getContext(), FloatApplication.TEXT2IMGDISTANCE);
        	final int width = FloatApplication.sp2px(getContext(), FloatApplication.MENUTXTWIDTH);
        	final int height = FloatApplication.sp2px(getContext(), FloatApplication.MENUTXTHEIGHT);
        	if(dir==FloatApplication.LEFT){
        		textparams = new LinearLayout.LayoutParams(width, height, Gravity.LEFT);
        		textparams.setMargins(margin, 0, 0, 0);
        	}
        	else{
        		textparams = new LinearLayout.LayoutParams(width, height, Gravity.LEFT);
        		textparams.setMargins(0, 0, 0, 0);
        	}
        }

        contentView.setClickable(false);
        textView.setClickable(false); 
        
        this.addView(contentView, params);
        this.addView(textView , textparams);
        
        this.contentView = getChildAt(0);
        this.textView = getChildAt(1);
    }

    /**
     * Sets a content view with default LayoutParams and direction
     * @param contentView
     * @param textView
     */
    public void setContentView(View contentView,View textView) {
        setContentView(contentView,textView, null,null,FloatApplication.LEFT);
    }
    
    /**
     * change the layout base on side
     * @param side
     * */
    public void updateLayout(int side){
    	LinearLayout.LayoutParams imgparams =(LinearLayout.LayoutParams)contentView.getLayoutParams();
    	LinearLayout.LayoutParams txtparams = (LinearLayout.LayoutParams)textView.getLayoutParams();
    	final int imgmargin = FloatApplication.sp2px(getContext(), (FloatApplication.VButtonWidth-FloatApplication.MENUIMGWIDTH)/2);
    	final int txtmargin = FloatApplication.sp2px(getContext(), FloatApplication.TEXT2IMGDISTANCE);
    	//first remove all the view 
    	this.removeAllViews();
    	if(side==FloatApplication.LEFT){
    		layoutParams.gravity = Gravity.LEFT;
    		setLayoutParams(layoutParams);
    		imgparams.setMargins(imgmargin, 0, 0, 0);
    		txtparams.setMargins(txtmargin, 0, 0, 0);
    		this.addView(contentView , imgparams);
    		this.addView(textView , txtparams);
    	}else{
    		layoutParams.gravity = Gravity.RIGHT;
    		setLayoutParams(layoutParams);
    		imgparams.setMargins(0, 0, imgmargin, 0);
    		txtparams.setMargins(0, 0, txtmargin, 0);
    		this.addView(textView , txtparams);
    		this.addView(contentView , imgparams);
    		
    	}
    }

    @SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	private void setBackgroundResource(Drawable drawable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            setBackground(drawable);
        }
        else {
            setBackgroundDrawable(drawable);
        }
    }
    
    @SuppressLint("DrawAllocation")
	public void onDraw(Canvas canvas){
    	Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    	mPaint.setColor(Color.GREEN);
    	if(isdraw){
    		mPaint.setAlpha(100);
		}else{
			mPaint.setAlpha(0);
		}
		RectF rect = new RectF(0,0,layoutParams.width , layoutParams.height);
		canvas.drawRoundRect(rect, 20, 20, mPaint);
    }
    
    public boolean onTouchEvent(MotionEvent event){
    	switch(event.getAction()){
    	case MotionEvent.ACTION_DOWN:
    		isdraw = true;
    		this.invalidate();
    		break;
    	case MotionEvent.ACTION_UP:
    		if(floatAction!=null){
    			floatAction.doAction();
    		}else{
    			Toast.makeText(getContext(), "no action", Toast.LENGTH_SHORT).show();
    		}
    		isdraw = false;
    		this.invalidate();
    		break;
    	}
    	return super.onTouchEvent(event);
    }

    /**
     * A builder for {@link SubActionButton} in conventional Java Builder format
     */
    public static class Builder {

        private Context context;
        private LinearLayout.LayoutParams layoutParams;
        private int direction;
        private Drawable backgroundDrawable;
        private View contentView;
        private View textView;
        private LinearLayout.LayoutParams contentParams;
        private LinearLayout.LayoutParams textParams;
        private FloatAction floatAction;

        public Builder(Context context) {
            this.context = context;

            // Default SubActionButton settings
            int width = FloatApplication.sp2px(context, FloatApplication.MENUIMGWIDTH+FloatApplication.MENUTXTWIDTH+
            											FloatApplication.TEXT2IMGDISTANCE);
            int height = FloatApplication.sp2px(context, FloatApplication.MENUIMGHEIGHT);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height, Gravity.TOP | Gravity.CENTER);
            setLayoutParams(params);
        }

        public Builder setLayoutParams(LinearLayout.LayoutParams params) {
            this.layoutParams = params;
            return this;
        }
        
        public Builder setDirection(int dir){
        	this.direction = dir;
        	return this;
        }

        public Builder setBackgroundDrawable(Drawable backgroundDrawable) {
            this.backgroundDrawable = backgroundDrawable;
            return this;
        }

        public Builder setContentView(View contentView) {
            this.contentView = contentView;
            return this;
        }

        public Builder setContentView(View contentView, LinearLayout.LayoutParams contentParams) {
            this.contentView = contentView;
            this.contentParams = contentParams;
            return this;
        }
        
        public Builder setTextView(View textView , LinearLayout.LayoutParams textparams){
        	this.textView = textView;
        	this.textParams = textparams;
        	return this;
        }
        
        public Builder setAction(FloatAction action){
        	this.floatAction = action;
        	return this;
        }

        public SubActionButton build() {
            return new SubActionButton(context,
                    layoutParams,
                    direction,
                    backgroundDrawable,
                    contentView,
                    textView,
                    contentParams,
                    textParams,
                    floatAction
                    );
        }
    }

}
