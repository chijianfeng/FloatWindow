package com.baidu.lightgame.plugin.view.floatwindow;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.SensorManager;
import android.os.Build;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

/**
 * Manager the float button and sub menu items
 * */
@SuppressLint("RtlHardcoded")
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class FloatActionMenu {
	
	private final static String TAG= "NewFloatWindow-cjf";
	/** Reference to the view (usually a button) to trigger the menu to show */
    private View mainActionView;
    
    /** List of menu items */
    private List<Item> subActionItems;
    /** Reference to the preferred {@link MenuAnimationHandler} object */
    private MenuAnimationHandler animationHandler;
    /** Reference to a listener that listens open/close actions */
    private MenuStateChangeListener stateChangeListener;
    /** whether the openings and closings should be animated or not */
    private boolean animated;
    /** whether the menu is currently open or not */
    private boolean open;
    /** whether the menu is an overlay for all other activities */
    private boolean systemOverlay;
    /** a simple layout to contain all the sub action views in the system overlay mode */
    private FrameLayout overlayContainer;
    
    private OrientationEventListener orientationListener;
    
    private BackgroundView mBackgroundView = null;
    
    public FloatActionMenu(final View mainactionview,
    						List<Item> subActionItems,
    						MenuAnimationHandler animationHandler,
                            boolean animated,
                            MenuStateChangeListener stateChangeListener,
                            final boolean systemOverlay){
    	this.mainActionView = mainactionview;
    	this.subActionItems = subActionItems;
    	this.animationHandler = animationHandler;
    	this.stateChangeListener = stateChangeListener;
    	this.animated = animated;
    	this.systemOverlay = systemOverlay;
    	this.open = false;
    	
    	this.mainActionView.setClickable(true);
    	this.mainActionView.setOnClickListener(new ActionViewClickListener());
    	this.mainActionView.bringToFront();
    	/**
    	 * set animation handler
    	 * */
    	if(animationHandler != null) {
            animationHandler.setMenu(this);
        }
    	
    	if(systemOverlay) {
            overlayContainer = new FrameLayout(mainActionView.getContext());
        }
        else {
            overlayContainer = null; // beware NullPointerExceptions!
        }
    	// Find items with undefined sizes
        for(final Item item : subActionItems) {
            if(item.width == 0 || item.height == 0) {
                if(systemOverlay) {
                    throw new RuntimeException("Sub action views cannot be added without " +
                            "definite width and height.");
                }
                // Figure out the size by temporarily adding it to the Activity content view hierarchy
                // and ask the size from the system
                addViewToCurrentContainer(item.view);
                // Make item view invisible, just in case
                item.view.setAlpha(0);
                // Wait for the right time
                item.view.post(new ItemViewQueueListener(item));
            } 
        }
        
        if(systemOverlay) {
            orientationListener = new OrientationEventListener(mainActionView.getContext(), SensorManager.SENSOR_DELAY_UI) {
                private int lastState = -1;

                public void onOrientationChanged(int orientation) {

                    Display display = getWindowManager().getDefaultDisplay();
                    if(display.getRotation() != lastState) {
                        lastState = display.getRotation();

                        if(isOpen()) {
                            close(false);
                        }
                    }
                }
            };
            orientationListener.enable();
        }
    }
    
    /**
     * Simply opens the menu by doing necessary calculations.
     * @param animated if true, this action is executed by the current {@link MenuAnimationHandler}
     */
    public void open(boolean animated) {

        // Get the center of the action view from the following function for efficiency
        // populate destination x,y coordinates of Items
    	if(animationHandler!=null&&animationHandler.isAnimating()){return;}
        Point center = calculateItemPositions();
        WindowManager.LayoutParams overlayParams = null;

        if(systemOverlay) {
        	 //add system overlay background
        	addSysBackgroundView();
            
            // If this is a system overlay menu, use the overlay container and place it behind
            // the main action button so that all the views will be added into it.
            attachOverlayContainer();

            overlayParams = (WindowManager.LayoutParams) overlayContainer.getLayoutParams();
        }else{
        	//add insert view 
        	addBackgroundView();
        }

        if(animated && animationHandler != null) {
            // If animations are enabled and we have a MenuAnimationHandler, let it do the heavy work
            if(animationHandler.isAnimating()) {
                // Do not proceed if there is an animation currently going on.
                return;
            }

            for (int i = 0; i < subActionItems.size(); i++) {
                // It is required that these Item views are not currently added to any parent
                // Because they are supposed to be added to the Activity content view,
                // just before the animation starts
                if (subActionItems.get(i).view.getParent() != null) {
                    throw new RuntimeException("All of the sub action items have to be independent from a parent.");
                }
                final FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(subActionItems.get(i).width, subActionItems.get(i).height, Gravity.TOP | Gravity.LEFT);

                if(systemOverlay) {
                	if(getMenudirection()==FloatApplication.BOTTOM2TOP){
                		params.setMargins(0, center.y - overlayParams.y - subActionItems.get(i).height, 0, 0);
                    }else{
                    	params.setMargins(0, center.y - overlayParams.y + subActionItems.get(i).height, 0, 0);
                    }
                }
                else {
                	if(getMenudirection()==FloatApplication.BOTTOM2TOP){
                		params.setMargins(0, center.y  - subActionItems.get(i).height, 0, 0);
                    }else{
                    	params.setMargins(0, center.y  + subActionItems.get(i).height, 0, 0);
                    }
                }
                addViewToCurrentContainer(subActionItems.get(i).view, params);
            }
            // Tell the current MenuAnimationHandler to animate from the center
            animationHandler.animateMenuOpening(center);
        }
        else {
            // If animations are disabled, just place each of the items to their calculated destination positions.
            for (int i = 0; i < subActionItems.size(); i++) {
                // This is currently done by giving them large margins

                final FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(subActionItems.get(i).width, subActionItems.get(i).height, Gravity.TOP | Gravity.LEFT);
                if(systemOverlay) {
                    params.setMargins(subActionItems.get(i).x - overlayParams.x, subActionItems.get(i).y - overlayParams.y, 0, 0);
                    subActionItems.get(i).view.setLayoutParams(params);
                }
                else {
                    params.setMargins(subActionItems.get(i).x, subActionItems.get(i).y, 0, 0);
                    subActionItems.get(i).view.setLayoutParams(params);
                    // Because they are placed into the main content view of the Activity,
                    // which is itself a FrameLayout
                }
                addViewToCurrentContainer(subActionItems.get(i).view, params);
            }
        }
        // do not forget to specify that the menu is open.
        open = true;
        getActionButton().clickState(getMenudirection());
        if(stateChangeListener != null) {
            stateChangeListener.onMenuOpened(this);
        }
     
    }
    
    /**
     * Closes the menu.
     * @param animated if true, this action is executed by the current {@link MenuAnimationHandler}
     */
    public void close(boolean animated) {
    	if(animationHandler!=null&&animationHandler.isAnimating()){return;}
        // If animations are enabled and we have a MenuAnimationHandler, let it do the heavy work
        if(animated && animationHandler != null) {
            if(animationHandler.isAnimating()) {
                // Do not proceed if there is an animation currently going on.
                return;
            }
            animationHandler.animateMenuClosing(getActionViewCenter());
        }
        else {
            // If animations are disabled, just detach each of the Item views from the Activity content view.
            for (int i = 0; i < subActionItems.size(); i++) {
                removeViewFromCurrentContainer(subActionItems.get(i).view);
            }
            detachOverlayContainer();
        }
        // do not forget to specify that the menu is now closed.
        open = false;
        removeBackGroundView();
        getActionButton().clickState(getMenudirection());
    }
    
    /**
     * @return whether the menu is open or not
     */
    public boolean isOpen() {
        return open;
    }
    
    /**
     * Toggles the menu
     * @param animated if true, the open/close action is executed by the current {@link MenuAnimationHandler}
     */
    public void toggle(boolean animated) {
        if(open) {
            close(animated);
        }
        else {
            open(animated);
        }
    }
    
    public void addViewToCurrentContainer(View view) {
        addViewToCurrentContainer(view, null);
    }
    
    /**
     * Intended to use for systemOverlay mode.
     * @return the WindowManager for the current context.
     */
    public WindowManager getWindowManager() {
        return (WindowManager) mainActionView.getContext().getSystemService(Context.WINDOW_SERVICE);
    }
    
    public static WindowManager.LayoutParams getDefaultSystemWindowParams() {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        params.format = PixelFormat.RGBA_8888;
        params.gravity = Gravity.TOP | Gravity.LEFT;
        return params;
    }
    
    public void attachOverlayContainer() {
        try {
            WindowManager.LayoutParams overlayParams = calculateOverlayContainerParams();

            overlayContainer.setLayoutParams(overlayParams);
            if(overlayContainer.getParent() == null) {
                getWindowManager().addView(overlayContainer, overlayParams);
            }
            getWindowManager().updateViewLayout(mainActionView, mainActionView.getLayoutParams());
        }
        catch(SecurityException e) {
            throw new SecurityException("Your application must have SYSTEM_ALERT_WINDOW " +
                    "permission to create a system window.");
        }
    }
    
    public void detachOverlayContainer() {
        getWindowManager().removeView(overlayContainer);
    }

    /**
     * Returns the center point of the main action view
     * @return the action view center point
     */
    public Point getActionViewCenter() {
        Point point = getActionViewCoordinates();
        point.x += mainActionView.getMeasuredWidth() / 2;
        point.y += mainActionView.getMeasuredHeight() / 2;
        return point;
    }
    
    /**
     * @return whether the menu is a system overlay or not
     */
    public boolean isSystemOverlay() {
        return systemOverlay;
    }
    
    public FrameLayout getOverlayContainer() {
        return overlayContainer;
    }
    
    /**
     * @return a reference to the sub action items list
     */
    public List<Item> getSubActionItems() {
        return subActionItems;
    }
    
    /**
     * Finds and returns the main content view from the Activity context.
     * @return the main content view
     */
    public View getActivityContentView() {
        try {
            return ((Activity) mainActionView.getContext()).getWindow().getDecorView().findViewById(android.R.id.content);
        }
        catch(ClassCastException e) {
            throw new ClassCastException("Please provide an Activity context for this FloatingActionMenu.");
        }
    }
    
    public void removeViewFromCurrentContainer(View view) {
        if(systemOverlay) {
            overlayContainer.removeView(view);
        }
        else {
            ((ViewGroup)getActivityContentView()).removeView(view);
        }
    }
    
    public void removeBackGroundView(){
        if(mBackgroundView==null){return;}
        if(systemOverlay){
        	getWindowManager().removeView(mBackgroundView);
    	}else{
    		((ViewGroup)getActivityContentView()).removeView(mBackgroundView);
    	}
        mBackgroundView = null;
    }
    
    /**
     * remove all view from the container
     * */
    public void removeAllView(){
    	removeBackGroundView();
    	for (int i = 0; i < subActionItems.size(); i++) {
            removeViewFromCurrentContainer(subActionItems.get(i).view);
        }
        //detachOverlayContainer();
    }
    
    public void hiddenAllView(){
    	for (int i = 0; i < subActionItems.size(); i++) {
            subActionItems.get(i).view.setVisibility(View.GONE);
        }
    	if(mBackgroundView!=null){
    		mBackgroundView.setVisibility(View.GONE);
    	}
    	if(null!=mainActionView){
    		mainActionView.setVisibility(View.GONE);
    	}
    }
    
    public void showAllView(){
    	for (int i = 0; i < subActionItems.size(); i++) {
            subActionItems.get(i).view.setVisibility(View.VISIBLE);
        }
    	if(mBackgroundView!=null){
    		mBackgroundView.setVisibility(View.VISIBLE);
    	}
    	if(null!=mainActionView){
    		mainActionView.setVisibility(View.VISIBLE);
    	}
    }
    
    public void setStateChangeListener(MenuStateChangeListener listener) {
        this.stateChangeListener = listener;
    }
    
    public MenuStateChangeListener getStateChangeListener(){
    	return stateChangeListener;
    }
    
    /**
     *get menu expand direction
     *@return direction 
     * */
    public int getMenudirection(){
    	final Point center = getActionViewCenter();
    	int direction = FloatApplication.TOP2BOTTOM;
    	int h = subActionItems.size()*subActionItems.get(0).height+center.y+mainActionView.getHeight()/2+
    			(subActionItems.size()-1)*spToDpi(FloatApplication.ITEM2ITEMDISTANCE);
    	if(h>getScreenSize().y){
    		direction = FloatApplication.BOTTOM2TOP;
    	}
    	return direction;
    }
    /**
     * get Menu side position
     * @return side
     * */
    public int getMenuSide(){
    	final Point center = getActionViewCenter();
    	return getScreenSize().x/2>=center.x?FloatApplication.LEFT:FloatApplication.RIGHT;
    }
    
    private void addViewToCurrentContainer(View view, ViewGroup.LayoutParams layoutParams) {
        if(systemOverlay) {
            overlayContainer.addView(view, layoutParams);
        }
        else {
            try {
                if(layoutParams != null) {
                    FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) layoutParams;
                    ((ViewGroup) getActivityContentView()).addView(view, lp);
                }
                else {
                    ((ViewGroup) getActivityContentView()).addView(view);
                }
            }
            catch(ClassCastException e) {
                throw new ClassCastException("layoutParams must be an instance of " +
                        "FrameLayout.LayoutParams.");
            }
        }
    }
    
    private void addSysBackgroundView(){
    	if(!systemOverlay||mBackgroundView!=null){return;}
    	mBackgroundView = new BackgroundView(mainActionView.getContext());
    	mBackgroundView.setMenu(this);
    	WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        params.format = PixelFormat.RGBA_8888;
        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = params.y = 0;
        params.width = mBackgroundView.getwidth();
        params.height = mBackgroundView.getheight();
        getWindowManager().addView(mBackgroundView, params);
    }
    
    private void addBackgroundView(){
    	if(mBackgroundView!=null){return;}
    	mBackgroundView = new BackgroundView(mainActionView.getContext());
    	mBackgroundView.setMenu(this);
    	int count = ((ViewGroup) getActivityContentView()).getChildCount();
    	Log.d(TAG , "before add bakground View Count is : "+ count );
    	((ViewGroup) getActivityContentView()).addView(mBackgroundView , count-1);
    }
    
    private WindowManager.LayoutParams calculateOverlayContainerParams() {
        // calculate the minimum viable size of overlayContainer
        WindowManager.LayoutParams overlayParams = getDefaultSystemWindowParams();
        int left = 9999, right = 0, top = 9999, bottom = 0;
        for(int i=0; i < subActionItems.size(); i++) {
            int lm = subActionItems.get(i).x;
            int tm = subActionItems.get(i).y;

            if(lm < left) {
                left = lm;
            }
            if(tm < top) {
                top = tm;
            }
            if(lm + subActionItems.get(i).width > right) {
                right = lm + subActionItems.get(i).width;
            }
            if(tm + subActionItems.get(i).height > bottom) {
                bottom = tm + subActionItems.get(i).height;
            }
        }
        
        Point actioncenter = getActionViewCenter();
        if(actioncenter.x<left){
        	left = actioncenter.x;
        }
        if(actioncenter.y<top){
        	top = actioncenter.y;	
        }
        if(actioncenter.x+mainActionView.getMeasuredWidth()/2>right){
        	right = actioncenter.x+mainActionView.getMeasuredWidth()/2;
        }
        if(actioncenter.y+mainActionView.getMeasuredHeight()/2>bottom){
        	bottom = actioncenter.y + mainActionView.getMeasuredHeight()/2;
        }
        
        overlayParams.width = right - left;
        overlayParams.height = bottom - top;
        overlayParams.x = left;
        overlayParams.y = top;
        overlayParams.gravity = Gravity.TOP | Gravity.LEFT;
        return overlayParams;
    }
    
    
    /**
     * Gets the coordinates of the main action view
     * This method should only be called after the main layout of the Activity is drawn,
     * such as when a user clicks the action button.
     * @return a Point containing x and y coordinates of the top left corner of action view
     */
    private Point getActionViewCoordinates() {
        int[] coords = new int[2];
        // This method returns a x and y values that can be larger than the dimensions of the device screen.
        mainActionView.getLocationOnScreen(coords);

        // So, we need to deduce the offsets.
        if(systemOverlay) {
            coords[1] -= 0;
        }
        else {
            Rect activityFrame = new Rect();
            getActivityContentView().getWindowVisibleDisplayFrame(activityFrame);
            coords[0] -= (getScreenSize().x - getActivityContentView().getMeasuredWidth());
            coords[1] -= (activityFrame.height() + activityFrame.top - getActivityContentView().getMeasuredHeight());
        }
        return new Point(coords[0], coords[1]);
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

    
    /**
     * calculate the desired position
     * @return Point
     * */
    private Point calculateItemPositions(){
    	final Point center = getActionViewCenter();
    	int direction = getMenudirection();
    	int side = getMenuSide();
    	final int itemdis = spToDpi(FloatApplication.ITEM2ITEMDISTANCE);
    	if(side==FloatApplication.LEFT&&direction==FloatApplication.TOP2BOTTOM){
    		int t = center.y + mainActionView.getHeight()/2+spToDpi(FloatApplication.VB2ITEMDISTANCE);
    		int l = center.x-spToDpi(FloatApplication.VButtonWidth/2);
    		int dis = 0;
    		for(int i = 0;i<subActionItems.size();i++){
    			subActionItems.get(i).x = l;
    			subActionItems.get(i).y = t + dis;
    			dis = subActionItems.get(i).height + itemdis;
    			t = subActionItems.get(i).y;
    			((SubActionButton)( subActionItems.get(i).view)).updateLayout(side);
    			
    		}
		}else if(side==FloatApplication.RIGHT&&direction==FloatApplication.TOP2BOTTOM){
			int t = center.y + mainActionView.getHeight()/2+spToDpi(FloatApplication.VB2ITEMDISTANCE);
    		int l = center.x + mainActionView.getWidth()/2;
    		int dis = 0;
    		for(int i = 0;i<subActionItems.size();i++){
    			subActionItems.get(i).x = l - subActionItems.get(i).width;
    			subActionItems.get(i).y = t + dis;
    			dis = subActionItems.get(i).height + itemdis;
    			t = subActionItems.get(i).y;
    			((SubActionButton)( subActionItems.get(i).view)).updateLayout(side);
    			
    		}
		}else if(side==FloatApplication.LEFT&&direction==FloatApplication.BOTTOM2TOP){
			int t = center.y - mainActionView.getHeight()/2 - spToDpi(FloatApplication.VB2ITEMDISTANCE);
    		int l = center.x - mainActionView.getWidth()/2;
    		int dis = 0;
    		for(int i = 0;i<subActionItems.size();i++){
    			subActionItems.get(i).x = l;
    			subActionItems.get(i).y = t - dis-subActionItems.get(i).height;
    			dis =  itemdis;
    			t = subActionItems.get(i).y;
    			((SubActionButton)( subActionItems.get(i).view)).updateLayout(side);
    			
    		}
		}else if(side==FloatApplication.RIGHT&&direction==FloatApplication.BOTTOM2TOP){
			int t = center.y - mainActionView.getHeight()/2 - spToDpi(FloatApplication.VB2ITEMDISTANCE);
    		int l = center.x + mainActionView.getWidth()/2;
    		int dis = 0;
    		for(int i = 0;i<subActionItems.size();i++){
    			subActionItems.get(i).x = l - subActionItems.get(i).width;
    			subActionItems.get(i).y = t - dis -  subActionItems.get(i).height;
    			dis =  itemdis;
    			t = subActionItems.get(i).y;
    			((SubActionButton)( subActionItems.get(i).view)).updateLayout(side);
    			
    		}
		}
    	
    	return center;
    }
    
    private int spToDpi(int size){
    	return FloatApplication.sp2px( mainActionView.getContext(), size);
    }
    
    private FloatActionButton getActionButton(){
    	return (FloatActionButton)mainActionView;
    }
    /**
     * This runnable calculates sizes of Item views that are added to the menu.
     */
    private class ItemViewQueueListener implements Runnable {

        private static final int MAX_TRIES = 10;
        private Item item;
        private int tries;

        public ItemViewQueueListener(Item item) {
            this.item = item;
            this.tries = 0;
        }

        @Override
        public void run() {
            // Wait until the the view can be measured but do not push too hard.
            if(item.view.getMeasuredWidth() == 0 && tries < MAX_TRIES) {
                item.view.post(this);
                return;
            }
            // Measure the size of the item view
            item.width = item.view.getMeasuredWidth();
            item.height = item.view.getMeasuredHeight();

            // Revert everything back to normal
            item.view.setAlpha(item.alpha);
            // Remove the item view from view hierarchy
            removeViewFromCurrentContainer(item.view);
        }
    }
    
    /**
     * A simple structure to put a view and its x, y, width and height values together
     */
    public static class Item {
        public int x;
        public int y;
        public int width;
        public int height;

        public float alpha;

        public View view;

        public Item(View view, int width, int height) {
            this.view = view;
            this.width = width;
            this.height = height;
            alpha = view.getAlpha();
            x = 0;
            y = 0;
        }
    }
    /**
     * A listener to listen open/closed state changes of the Menu
     */
    public static interface MenuStateChangeListener {
        public void onMenuOpened(FloatActionMenu menu);
        public void onMenuClosed(FloatActionMenu menu);
    }
    
    /**
     * A simple click listener used by the main action view
     */
    public class ActionViewClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
        	Log.d(TAG , "click invoked");
            toggle(animated);
        }
    }
    
    /**
     * A builder for {@link FloatingActionMenu} in conventional Java Builder format
     */
    public static class Builder {

        private View actionView;
        private List<Item> subActionItems;
        private MenuAnimationHandler animationHandler;
        private boolean animated;
        private MenuStateChangeListener stateChangeListener;
        private boolean systemOverlay;

        public Builder(Context context, boolean systemOverlay) {
            subActionItems = new ArrayList<Item>();
            // Default settings
            animationHandler = null;
            animated = true;
            this.systemOverlay = systemOverlay;
        }

        public Builder(Context context) {
            this(context, false);
        }

        public Builder addSubActionView(View subActionView, int width, int height) {
            subActionItems.add(new Item(subActionView, width, height));
            return this;
        }

        /**
         * Adds a sub action view that is already alive, but not added to a parent View.
         * @param subActionView a view for the menu
         * @return the builder object itself
         */
        public Builder addSubActionView(View subActionView) {
            if(systemOverlay) {
                throw new RuntimeException("Sub action views cannot be added without " +
                        "definite width and height. Please use " +
                        "other methods named addSubActionView");
            }
            return this.addSubActionView(subActionView, 0, 0);
        }

        /**
         * Inflates a new view from the specified resource id and adds it as a sub action view.
         * @param resId the resource id reference for the view
         * @param context a valid context
         * @return the builder object itself
         */
        public Builder addSubActionView(int resId, Context context) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(resId, null, false);
            view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            return this.addSubActionView(view, view.getMeasuredWidth(), view.getMeasuredHeight());
        }
        

        /**
         * Sets the current animation handler to the specified MenuAnimationHandler child
         * @param animationHandler a MenuAnimationHandler child
         * @return the builder object itself
         */
        public Builder setAnimationHandler(MenuAnimationHandler animationHandler) {
            this.animationHandler = animationHandler;
            return this;
        }

        public Builder enableAnimations() {
            animated = true;
            return this;
        }

        public Builder disableAnimations() {
            animated = false;
            return this;
        }

        public Builder setStateChangeListener(MenuStateChangeListener listener) {
            stateChangeListener = listener;
            return this;
        }

        public Builder setSystemOverlay(boolean systemOverlay) {
            this.systemOverlay = systemOverlay;
            return this;
        }

        /**
         * Attaches the whole menu around a main action view, usually a button.
         * All the calculations are made according to this action view.
         * @param actionView
         * @return the builder object itself
         */
        public Builder attachTo(View actionView) {
            this.actionView = actionView;
            return this;
        }

        public FloatActionMenu build() {
            return new FloatActionMenu(actionView,
                                          subActionItems,
                                          animationHandler,
                                          animated,
                                          stateChangeListener,
                                          systemOverlay);
        }
    }
}
