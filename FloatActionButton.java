package com.baidu.lightgame.plugin.view.floatwindow;


import java.lang.reflect.Field;
import java.util.List;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.view.ViewTreeObserver;

@SuppressLint("ClickableViewAccessibility")
public class FloatActionButton extends FrameLayout implements ViewTreeObserver.OnGlobalLayoutListener{
	
	private final static String TAG = "NewFloatWindow-cjf";
    /**
     * float button init position in screen
     * */
    public static final int POSITION_TOP_CENTER = 1;
    public static final int POSITION_TOP_RIGHT = 2;
    public static final int POSITION_RIGHT_CENTER = 3;
    public static final int POSITION_BOTTOM_RIGHT = 4;
    public static final int POSITION_BOTTOM_CENTER = 5;
    public static final int POSITION_BOTTOM_LEFT = 6;
    public static final int POSITION_LEFT_CENTER = 7;
    public static final int POSITION_TOP_LEFT = 8;
		
	private boolean systemOverlay;
	
	/**
	 * touch position
	 * */
	private float mTouchStartx;
	private float mTouchStarty;
	private float x;
	private float y;
	
	/**
	 * record the previous position 
	 * */
	private float prex = 0;
	private float prey = 0;
	
	/**
	 * is the menu is expand.
	 * */
	private int curState;
	
	/*support system overlayout & inner layout
	 * **/
	private ViewGroup.LayoutParams wmParam = null;
	
	private List<Drawable> listDrawable;
	
	private View contentView;
	
	private InValidView helpView = null;
	
	private int offset = 0;
    
    /**
     * Constructor that takes parameters collected using builder
     * @param context a reference to the current context
     * @param layoutParams
     * @param theme
     * @param backgroundDrawable
     * @param position
     * @param contentView
     * @param contentParams
     */
	public FloatActionButton(Context context, 
							 ViewGroup.LayoutParams layoutParams,
            				 Drawable backgroundDrawable, 
            				 List<Drawable> dl,
            				 int position, 
            				 View contentView,
            				 FrameLayout.LayoutParams contentParams,
            				 boolean systemOverlay) {
		super(context);
		this.systemOverlay = systemOverlay;
		this.listDrawable = dl;
		this.contentView = contentView;
        if(!systemOverlay && !(context instanceof Activity)) {
            throw new RuntimeException("Given context must be an instance of Activity, "
                    +"since this FAB is not a systemOverlay.");
        }
        
        setPosition(position, layoutParams);
        if(backgroundDrawable!=null){
        	setBackgroundResource(backgroundDrawable);
        }
        if(contentView != null) {
            setContentView(contentView, contentParams);
        }
        setClickable(true);
        if(systemOverlay){
        	offset = getStatusBarHeight(context);
        }else{
        	offset = 0;
        	this.bringChildToFront(contentView);
        	helpView = new InValidView(context);
        }
        attach(layoutParams); 
        if(systemOverlay){
        	wmParam = Builder.getDefaultSystemWindowParams(getContext()); 
        }else{
        	wmParam = layoutParams;
        }
        curState = FloatApplication.STATE_HALFHOVER;
	}

	/**
     * Sets the position of the button by calculating its Gravity from the position parameter
     * @param position one of 8 specified positions.
     * @param layoutParams should be either FrameLayout.LayoutParams or WindowManager.LayoutParams
     */
    @SuppressLint("RtlHardcoded")
	public void setPosition(int position, ViewGroup.LayoutParams layoutParams) {

        boolean setDefaultMargin = false;

        int gravity;
        switch (position) {
            case POSITION_TOP_CENTER:
                gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
                break;
            case POSITION_TOP_RIGHT:
                gravity = Gravity.TOP | Gravity.RIGHT;
                break;
            case POSITION_RIGHT_CENTER:
                gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
                break;
            case POSITION_BOTTOM_LEFT:
                gravity = Gravity.BOTTOM | Gravity.LEFT;
                break;
            case POSITION_LEFT_CENTER:
                gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
                break;
            case POSITION_TOP_LEFT:
            default:
                setDefaultMargin = true;
                gravity = Gravity.TOP | Gravity.LEFT;
                break;
        }
        if(!systemOverlay) {
            try {
                FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) layoutParams;
                lp.gravity = gravity;
                if(setDefaultMargin) {
                    lp.leftMargin = 0;
                    lp.topMargin =  100;		//just init  position
                }
                setLayoutParams(lp);
            } catch (ClassCastException e) {
                throw new ClassCastException("layoutParams must be an instance of " +
                        "FrameLayout.LayoutParams, since this FAB is not a systemOverlay");
            }
        }
        else {
            try {
                WindowManager.LayoutParams lp = (WindowManager.LayoutParams) layoutParams;
                lp.gravity = gravity;
                if(setDefaultMargin) {
                    lp.x = 0;
                    lp.y = 30;
                }
                setLayoutParams(lp);
            } catch(ClassCastException e) {
                throw new ClassCastException("layoutParams must be an instance of " +
                        "WindowManager.LayoutParams, since this FAB is a systemOverlay");
            }
        }
    }
	
	@SuppressWarnings("deprecation")
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private void setBackgroundResource(Drawable drawable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            setBackground(drawable);
        }
        else {
            setBackgroundDrawable(drawable);
        }
    }
	
	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	private void setContentResource(Drawable drawable){
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            contentView.setBackground(drawable);
        }
        else {
            contentView.setBackgroundDrawable(drawable);
        }
	}
	
	/**
     * Sets a content view that will be displayed inside this FloatActionButton.
     * @param contentView
     */
    public void setContentView(View contentView, FrameLayout.LayoutParams contentParams) {
        FrameLayout.LayoutParams params;
        int leftmarin = FloatApplication.dip2px(getContext(), FloatApplication.VButtonWidth)/4;
        if(contentParams == null ){
            params = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER);
            final int margin = FloatApplication.dip2px(getContext(), FloatApplication.VBCONTENTMARGIN);
            params.setMargins(-leftmarin, margin, 0, 0);
        }
        else {
            params = contentParams;
            params.setMargins(-leftmarin, 0, 0, 0);
        }
        params.gravity = Gravity.CENTER;

        contentView.setClickable(false);
        this.addView(contentView, params);
    }
	
    /**
     * Attaches it to the content view with specified LayoutParams.
     * @param layoutParams
     */
    public void attach(ViewGroup.LayoutParams layoutParams) {
        if(systemOverlay) {
            try {
                getWindowManager().addView(this, layoutParams);
            }
            catch(SecurityException e) {
                throw new SecurityException("Your application must have SYSTEM_ALERT_WINDOW " +
                        "permission to create a system window.");
            }
        }
        else {
            ((ViewGroup) getActivityContentView()).addView(this, layoutParams);
            if(helpView!=null){
            	((ViewGroup) getActivityContentView()).addView(helpView);
            }else{
            	Log.d(TAG , "help View is NULL");
            }
        }
    }

    /**
     * Detaches it from the container view.
     */
    public void detach() {
        if(systemOverlay) {
            getWindowManager().removeView(this);
        }
        else {
            ((ViewGroup) getActivityContentView()).removeView(this);
            ((ViewGroup) getActivityContentView()).removeView(helpView);
            helpView = null;
        }
    }
    
    public WindowManager getWindowManager() {
        return (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
    }
    
    /**
     * Finds and returns the main content view from the Activity context.
     * @return the main content view
     */
    public View getActivityContentView() {
        try {
            return ((Activity) getContext()).getWindow().getDecorView().findViewById(android.R.id.content);
        }
        catch(ClassCastException e) {
            throw new ClassCastException("Please provide an Activity context for this FloatingActionButton.");
        }
    }
    
    public int getSide(int x){
    	if(x>getScreenSize().x/2){
    		return FloatApplication.RIGHT;
    	}else{
    		return FloatApplication.LEFT;
    	}
    }
    
    private int getSide(){
    	int[] coords = new int[2];
    	getLocationOnScreen(coords);
    	return getSide(coords[0]);
    }
    
    public void clickState(int dir){
    	if(curState==FloatApplication.STATE_HALFHOVER||curState==FloatApplication.STATE_HOVER){
    		this.removeCallbacks(runable);		
    		curState = FloatApplication.STATE_EXPAND;
    		if(dir==FloatApplication.BOTTOM2TOP){
    			setResourcebyindex(3);
    		}else{
    			setResourcebyindex(4);
    		}
    	}else{
    		curState = FloatApplication.STATE_HOVER;
    		this.removeCallbacks(runable);												//first remove delay function	
			this.postDelayed(runable, 2000);
    	}
    	changeContentLayout();
    }
    
    public void ChangeToHoverState(){
    	setResourcebyindex(2);
    	Log.d(TAG , "Chnage Float Window to Hover State");
    }
    
    public boolean onTouchEvent(MotionEvent event){
    	
    	if(curState==FloatApplication.STATE_EXPAND){
			return super.onTouchEvent(event);
		}
    	boolean isMoving = false;
    	x = event.getRawX();
		y = event.getRawY();
		int dis = FloatApplication.sp2px(getContext(), FloatApplication.DISTANCE);
		switch(event.getAction()){
			case MotionEvent.ACTION_DOWN:
				mTouchStartx = event.getX();
				mTouchStarty = event.getY() + offset;
				break;
			case MotionEvent.ACTION_UP:
				if(Math.abs(prex-x+mTouchStartx)>dis&&Math.abs(prey-y+mTouchStarty)>dis){
					isMoving = true;
					prex = x-mTouchStartx<getScreenSize().x/2?0:
						getScreenSize().x - FloatApplication.sp2px(getContext(), FloatApplication.VButtonWidth);
					prey =  y-mTouchStarty;
					ChangeState();
				}
				if(x>getScreenSize().x/2){
					x =	getScreenSize().x + mTouchStartx - wmParam.width;
				}else{
					x = mTouchStartx;
				}
				updateLayout();
				
				mTouchStartx = mTouchStarty = 0;
				break;
			case MotionEvent.ACTION_MOVE:
				updateLayout();
				ChangeState();
				break;
			default:
				break;
		}
		if(!isMoving){
			return super.onTouchEvent(event);
		}else{
			return true;
		}
    }
    
    private void updateLayout(){
		if(systemOverlay){
			((WindowManager.LayoutParams)wmParam).x = (int)(x-mTouchStartx);
			((WindowManager.LayoutParams)wmParam).y = (int)(y-mTouchStarty);
			getWindowManager().updateViewLayout(this, wmParam);
		}else{
			((FrameLayout.LayoutParams)wmParam).leftMargin = (int)(x-mTouchStartx);
			((FrameLayout.LayoutParams)wmParam).topMargin = (int)(y-mTouchStarty);
			setLayoutParams(wmParam);
		}
    }
    
    /**
     * Retrieves the screen size from the Activity context
     * @return the screen size as a Point object
     */
    @SuppressLint("NewApi")
	private Point getScreenSize() {
        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        return size;
    }
    
	private void ChangeState(){
    	if(curState ==FloatApplication.STATE_HALFHOVER){
    		this.removeCallbacks(runable);		
    		curState = FloatApplication.STATE_HOVER;
    		setResourcebyindex(2);
    		changeContentLayout();
    	} 
    	if(curState ==FloatApplication.STATE_HOVER){
    		this.removeCallbacks(runable);												//first remove delay function
			this.postDelayed(runable, 2000);
    	}
	}
	
	@SuppressLint("NewApi")
	private void changeContentLayout(){
		int margin =  FloatApplication.dip2px(getContext(), FloatApplication.VButtonWidth);
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(margin, margin, Gravity.CENTER);
		if(curState==FloatApplication.STATE_EXPAND||curState==FloatApplication.STATE_HOVER){
			params.setMargins(0, 0, 0, 0);
		}else{
			if(getSide()==FloatApplication.LEFT){
				params.setMargins((int)(margin*-.25f), 0, 0, 0);
			}else{
				params.setMargins((int)(margin*.25f), 0, 0, 0);
			}
		}
		this.removeView(contentView);
		this.addView(contentView , params);
	}
	
	@SuppressLint("NewApi")
	private void HandleHalfHover(){
		curState = FloatApplication.STATE_HALFHOVER;
		if(getSide()==FloatApplication.LEFT){
			setResourcebyindex(0);
			changeContentLayout();
		}else{
			setResourcebyindex(1);
			changeContentLayout();
		}
		
		this.removeCallbacks(runable);	
	}
    
    Runnable runable = new Runnable(){
		public void run(){
			HandleHalfHover();
		}
	};
	
	private void setResourcebyindex(int index){
		int count = 0;
		for(Drawable item:listDrawable){
			if(count==index){
				setContentResource(item);
				break;
			}
			count++;
		}
	}
	
	private static int getStatusBarHeight(Context context){
		Log.d(TAG , "System Version : "+Build.VERSION.SDK_INT);
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
			 return 0;
		}
		Class<?> c = null;
		Object obj = null;
		Field field  = null;
		int x = 0, statusBarhegiht =0;
		
		try{
			c = Class.forName("com.android.internal.R$dimen");
			obj = c.newInstance();
			field = c.getField("status_bar_height");
			x = Integer.parseInt(field.get(obj).toString());
			statusBarhegiht = context.getResources().getDimensionPixelSize(x);
		}catch(Exception e){
			e.printStackTrace();
		}
		Log.d(TAG , "The Status_bar_height is : " + statusBarhegiht);
		return statusBarhegiht;
	}
    
    /**
     * A builder for {@link FloatActionButton}
     * */
    @SuppressLint("RtlHardcoded")
	public static class Builder{
    	private Context context;
    	private ViewGroup.LayoutParams layoutParams;
    	private Drawable backgroundDrawable;
        private int position;
        private View contentView;
        private LayoutParams contentParams;
        private boolean systemOverlay;
        private List<Drawable> listDrawable;
    	
    	public Builder(Context ctx){
    		this.context = ctx;
    		//WindowManager.LayoutParams layoutParams = getDefaultSystemWindowParams(context);
    		int size = FloatApplication.sp2px(context, FloatApplication.VButtonWidth);
    		FrameLayout.LayoutParams layoutParams = new LayoutParams(size, size, 
    				Gravity.TOP | Gravity.LEFT);
            setLayoutParams(layoutParams);
            setPosition(FloatActionButton.POSITION_TOP_LEFT);
            setSystemOverlay(false);
    	}
    	
    	/**
    	 * set params for building float button
    	 * */
    	public Builder setLayoutParams(ViewGroup.LayoutParams params){
    		this.layoutParams = params;
    		return this;
    	}
    	public Builder setBackgroundDrawable(Drawable backgroundDrawable) {
            this.backgroundDrawable = backgroundDrawable;
            return this;
        }

        public Builder setBackgroundDrawable(int drawableId) {
            return setBackgroundDrawable(context.getResources().getDrawable(drawableId));
        }

        public Builder setPosition(int position) {
            this.position = position;
            return this;
        }

        public Builder setContentView(View contentView) {
            return setContentView(contentView, null);
        }

        public Builder setContentView(View contentView, LayoutParams contentParams) {
            this.contentView = contentView;
            this.contentParams = contentParams;
            return this;
        }

        public Builder setSystemOverlay(boolean systemOverlay) {
            this.systemOverlay = systemOverlay;
            if(systemOverlay){
            	setLayoutParams(getDefaultSystemWindowParams(context));
            }
            return this;
        }
        
        public Builder setBackgroundDrawablelist(List<Drawable> drawablelist){
        	this.listDrawable = drawablelist;
        	return this;
        }
        
        public FloatActionButton build() {
            return new FloatActionButton(context,
                                           layoutParams,
                                           backgroundDrawable,
                                           listDrawable,
                                           position,
                                           contentView,
                                           contentParams,
                                           systemOverlay);
        }

        public static WindowManager.LayoutParams getDefaultSystemWindowParams(Context context) {
            int size = FloatApplication.sp2px(context, FloatApplication.VButtonWidth);
            WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                    size,
                    size,
                    WindowManager.LayoutParams.TYPE_SYSTEM_ALERT, // z-ordering
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
            params.format = PixelFormat.RGBA_8888;
            params.gravity = Gravity.TOP | Gravity.LEFT;
            return params;
        }
    }


	@Override
	public void onGlobalLayout() {
		// TODO Auto-generated method stub
		Log.d(TAG , "Global layout change : "+this.getVisibility());
	}
	
	private class InValidView extends View{
		
		private Paint mPaint = null;
		
		public InValidView(Context context) {
			super(context);
			mPaint = new Paint();
			this.setClickable(false);
		}
		@SuppressLint({ "NewApi", "DrawAllocation" })
		protected void onDraw(Canvas canvas){
			mPaint.setColor(Color.BLACK);
			mPaint.setAlpha(0);					//Transplant
			Point size = new Point();
	        getWindowManager().getDefaultDisplay().getSize(size);
			Rect rect = new Rect(0,0, size.x, size.y);
			canvas.drawRect(rect, mPaint);
		}
	}
}
