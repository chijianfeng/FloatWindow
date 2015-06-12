package com.baidu.lightgame.plugin.view.floatwindow;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.graphics.Point;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;


@SuppressLint("NewApi")
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class SlideAnimationHandler extends MenuAnimationHandler{

	private final static String TAG = "NewFloatWindow-cjf";
	/** duration of animations, in milliseconds */
    protected static final int DURATION = 200;
    /** duration to wait between each of menu item */
    protected static final int LAG_BETWEEN_ITEMS = 50;

    protected int DIST_Y = 0;

    /** holds the current state of animation */

    /** holds the current state of animation */

    private boolean animating;

    public SlideAnimationHandler() {
        setAnimating(false);
    }
    
    @Override
    public boolean isAnimating() {
        return animating;
    }

    @Override
    protected void setAnimating(boolean animating) {
        this.animating = animating;
    }
    
    @SuppressLint("NewApi")
	public void animateMenuOpening(Point center){
    	super.animateMenuOpening(center);
    	setAnimating(true);
    	 Animator lastAnimation = null;
    	
    	 int height = 0;
    	 if(menu!=null&&menu.isSystemOverlay()){
    		 WindowManager.LayoutParams containerParams = (WindowManager.LayoutParams)menu.getOverlayContainer().getLayoutParams();
    		 height = containerParams.y;
    	 }else{
    		 
    	 }
    	 
    	 for(int i = 0; i<menu.getSubActionItems().size();i++){
    		
    		 menu.getSubActionItems().get(i).view.setAlpha(0);
    		 FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) menu.getSubActionItems().get(i).view.getLayoutParams();
    		 //first in top position
    		 params.setMargins(menu.getSubActionItems().get(i).x,menu.getActionViewCenter().y-height, 0, 0);
             menu.getSubActionItems().get(i).view.setLayoutParams(params);
             
             DIST_Y  = menu.getActionViewCenter().y-menu.getSubActionItems().get(i).y;
             PropertyValuesHolder pvhY = PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, -DIST_Y);
             PropertyValuesHolder pvhA = PropertyValuesHolder.ofFloat(View.ALPHA, 1);
             final ObjectAnimator animation = ObjectAnimator.ofPropertyValuesHolder(menu.getSubActionItems().get(i).view,
            		 																pvhY,
            		 																pvhA);
             animation.setDuration(DURATION);
             animation.setInterpolator(new DecelerateInterpolator());
             animation.addListener(new SubActionItemAnimationListener(menu.getSubActionItems().get(i), ActionType.OPENING));
             if(i == 0) {
                 lastAnimation = animation;
             }
             animation.setStartDelay(i * LAG_BETWEEN_ITEMS);
             animation.start();
    	 }
    	 if(lastAnimation != null) {
              lastAnimation.addListener(new LastAnimationListener(ActionType.OPENING));
         }
    }
    
    public void animateMenuClosing(Point center){
    	 super.animateMenuClosing(center);
    	 setAnimating(true);

         Animator lastAnimation = null;
         for(int i =0 ;i<menu.getSubActionItems().size();i++){
        	 DIST_Y  = menu.getActionViewCenter().y-menu.getSubActionItems().get(i).y;
        	 PropertyValuesHolder pvhY = PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, DIST_Y);
        	 PropertyValuesHolder pvhA = PropertyValuesHolder.ofFloat(View.ALPHA, 0);
        	 PropertyValuesHolder pvhSx = PropertyValuesHolder.ofFloat(View.SCALE_X, 0);
             PropertyValuesHolder pvhSy = PropertyValuesHolder.ofFloat(View.SCALE_Y, 0);
        	 final ObjectAnimator animation = ObjectAnimator.ofPropertyValuesHolder(menu.getSubActionItems().get(i).view, 
        			 																pvhY, 
        			 																pvhA,
        			 																pvhSx,
        			 																pvhSy);
             animation.setDuration(DURATION);
             animation.setInterpolator(new AccelerateInterpolator());
             animation.addListener(new SubActionItemAnimationListener(menu.getSubActionItems().get(i), ActionType.CLOSING));

             if(i == 0) {
                 lastAnimation = animation;
             }
             animation.setStartDelay(Math.abs(menu.getSubActionItems().size()-i) * LAG_BETWEEN_ITEMS);
             animation.start();
         }
         if(lastAnimation != null) {
             lastAnimation.addListener(new LastAnimationListener(ActionType.CLOSING));
         }
    }
    
    protected class SubActionItemAnimationListener implements Animator.AnimatorListener {

        private FloatActionMenu.Item subActionItem;
        private ActionType actionType;

        public SubActionItemAnimationListener(FloatActionMenu.Item subActionItem, ActionType actionType) {
            this.subActionItem = subActionItem;
            this.actionType = actionType;
        }

        @Override
        public void onAnimationStart(Animator animation) {

        }

        @Override
        public void onAnimationEnd(Animator animation) {
            restoreSubActionViewAfterAnimation(subActionItem, actionType);
        }

        @Override
        public void onAnimationCancel(Animator animation) {
            restoreSubActionViewAfterAnimation(subActionItem, actionType);
        }

        @Override public void onAnimationRepeat(Animator animation) {}
    }
}
