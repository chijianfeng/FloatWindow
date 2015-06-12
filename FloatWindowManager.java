package com.baidu.lightgame.plugin.view.floatwindow;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.baidu.lightgame.plugin.common.GameHelperExt;
import com.baidu.lightgame.plugin.game.GameRuntime;
import com.baidu.lightgame.plugin.view.floatwindow.FloatActionMenu.MenuStateChangeListener;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

public class FloatWindowManager {
	
	private final static String TAG = "NewFloatWindow-cjf";
	private Context context;
	private File mResPath;
	private FloatActionMenu floatMenu;
	FloatActionButton floatActionBtn = null;
	private static boolean isSystem = false;
	
	public FloatWindowManager(Context context , File resPath){
		this.context = context;
		this.mResPath = resPath;
	}
	@SuppressLint("RtlHardcoded")
	public void addFlaotWindow(){
		ImageView actionimg = new ImageView(context);
		setBackgroundResource(actionimg, GameHelperExt.readDrawableFromFile(
										new File(mResPath,FloatApplication.GAME_PLAYER_HOVER_HALF_LEFT)));
		int size = FloatApplication.sp2px(context, FloatApplication.VButtonWidth);
		FrameLayout.LayoutParams actionLayout = new FrameLayout.LayoutParams(size,size, Gravity.LEFT);
		
		List<Drawable> listDrawble = new ArrayList<Drawable>();
		listDrawble.add(GameHelperExt.readDrawableFromFile(
										new File(mResPath,FloatApplication.GAME_PLAYER_HOVER_HALF_LEFT)));
		listDrawble.add(GameHelperExt.readDrawableFromFile(
										new File(mResPath,FloatApplication.GAME_PLAYER_HOVER_HALF_RIGHT)));
		listDrawble.add(GameHelperExt.readDrawableFromFile(
										new File(mResPath,FloatApplication.GAME_PLAYER_HOVER)));
		listDrawble.add(GameHelperExt.readDrawableFromFile(
										new File(mResPath,FloatApplication.GAME_PLAYER_BUTTON_CLOSE_DOWN)));
		listDrawble.add(GameHelperExt.readDrawableFromFile(
										new File(mResPath,FloatApplication.GAME_PLAYER_BUTTON_CLOSE_UP)));
		
		floatActionBtn = new FloatActionButton.Builder(context)
											  .setContentView(actionimg , actionLayout)
											  .setPosition(FloatActionButton.POSITION_TOP_LEFT)
											  .setSystemOverlay(isSystem)
											  .setBackgroundDrawablelist(listDrawble)
											  .build();
		//create sub menu item
		ImageItemView subimgItem1 = new ImageItemView(context , new File(mResPath , FloatApplication.GAME_PLAYER_GAME_SHARE));
		TextItemView subtxtItem1 = new TextItemView(context , FloatApplication.STRING_PLAYER_SHARE);
		ImageItemView subimgItem2 = new ImageItemView(context , new File(mResPath , FloatApplication.GAME_PLAYER_MORE_GAME));
		TextItemView subtxtItem2 = new TextItemView(context , FloatApplication.STRING_PLAYER_MORE_GAME);
		ImageItemView subimgItem3 = new ImageItemView (context , new File(mResPath , FloatApplication.GAME_PLAYER_SEND_TO_DESKTOP));
		TextItemView subtxtItem3 = new TextItemView(context , FloatApplication.STRING_PLAYER_SEND_DESKTOP);
		ImageItemView subimgItem4 = new ImageItemView(context , new File(mResPath , FloatApplication.GAME_PLAYER_GAME_EXIT));
		TextItemView subtxtItem4 = new TextItemView(context , FloatApplication.STRING_PLAYER_EXIT);
		
		SubActionButton.Builder submenubuilder= new SubActionButton.Builder(context)
																   .setDirection(FloatApplication.TOP2BOTTOM);
		
		int itemheight = FloatApplication.sp2px(context, FloatApplication.MENUIMGHEIGHT);
		int itemwidth = FloatApplication.sp2px(context, FloatApplication.MENUTXTWIDTH
														+FloatApplication.TEXT2IMGDISTANCE
														+(FloatApplication.VButtonWidth+FloatApplication.MENUIMGWIDTH)/2);
		FloatAction share_action = new FloatAction() {
            @Override
            public void doAction() {
                floatMenu.toggle(true);
                GameRuntime.getInstance().requireShare();
            }
        };
        FloatAction add_icon_action = new FloatAction() {
            @Override
            public void doAction() {
                floatMenu.toggle(true);
                GameRuntime.getInstance().requireAddIcon();
            }
        };
        FloatAction more_game_action = new FloatAction() {
            @Override
            public void doAction() {
                floatMenu.toggle(true);
                GameRuntime.getInstance().requireMoreGame();
            }
        };
        FloatAction exit_action = new FloatAction() {
            @Override
            public void doAction() {
                floatMenu.toggle(true);
                GameRuntime.getInstance().requireExit();
            }
        };
		floatMenu = new FloatActionMenu.Builder(context, isSystem)
									   .setAnimationHandler(new SlideAnimationHandler())
									   .addSubActionView(submenubuilder
											   .setContentView(subimgItem1, null)
											   .setTextView(subtxtItem1, null)
											   .setAction(share_action)
											   .build(),itemwidth,itemheight)
									   .addSubActionView(submenubuilder
											   .setContentView(subimgItem2)
											   .setTextView(subtxtItem2, null)
											   .setAction(more_game_action)
											   .build(),itemwidth , itemheight)
									   .addSubActionView(submenubuilder
											   .setContentView(subimgItem3, null)
											   .setTextView(subtxtItem3, null)
											   .setAction(add_icon_action)
											   .build(),itemwidth,itemheight)
									   .addSubActionView(submenubuilder
											   .setContentView(subimgItem4)
											   .setTextView(subtxtItem4, null)
											   .setAction(exit_action)
											   .build(),itemwidth ,itemheight)
									    .attachTo(floatActionBtn)
										.setStateChangeListener(new StateChangedListener())
										.build();
		Log.d(TAG , "add Flaot Window Finihsed");
		
	}
	
	public void removeFloatWindow(){
		floatMenu.removeAllView();
	}
	
	public void hiddenFloatWindow(){
		Log.d(TAG , "hidden the float Window");
		if(floatMenu.isSystemOverlay()){
			floatMenu.hiddenAllView();
		}
	}
	public void showFloatWindow(){
		Log.d(TAG , "show the float Window");
		if(floatMenu.isSystemOverlay()){
			floatMenu.showAllView();
		}
	}
	public void handleBackKey(){
		floatMenu.toggle(true);
	}
	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	private void setBackgroundResource(View v ,Drawable drawable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            v.setBackground(drawable);
        }
        else {
            v.setBackgroundDrawable(drawable);
        }
    }
	
	class StateChangedListener implements MenuStateChangeListener{

		@Override
		public void onMenuOpened(FloatActionMenu menu) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onMenuClosed(FloatActionMenu menu) {
			// TODO Auto-generated method stub
			if(floatActionBtn!=null){
				floatActionBtn.ChangeToHoverState();
			}
		}
	}
}
