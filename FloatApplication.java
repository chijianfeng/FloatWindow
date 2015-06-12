package com.baidu.lightgame.plugin.view.floatwindow;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.view.WindowManager;

@SuppressLint("RtlHardcoded")
public class FloatApplication extends Application{
	
	private static WindowManager.LayoutParams wmParams = 
			new WindowManager.LayoutParams(); 
	
	private static WindowManager.LayoutParams wmBackParams =
			new WindowManager.LayoutParams();
	
	/**
	 * define the float button state in the screen
	 */
	public static final int STATE_HOVER = 0;
	public static final int STATE_HALFHOVER = 1;
	public static final int STATE_EXPAND = 2;
	
	public static final int DISTANCE = 1;
	
	//direction from top or bottom
	public static final int TOP2BOTTOM = 1;
	
	public static final int BOTTOM2TOP = -1;
	
	//float window in left or right
	public static final int LEFT = 1;
	
	public static final int RIGHT = -1;
	
	/**
	 * float button width & Height unit is dip, the same as below
	 * */
	public static final int VButtonWidth = 40;
	public static final int VButtonHeight = 40;
	
	/**
	 * the distance between the float button to closet side(left or right)
	 * */
	public static final int VB2SIDEDISTANCE = 0;
	
	/**
	 * float button bottom to the top of first menu item
	 * */
	public static final int VB2ITEMDISTANCE = 10;
	
	/**
	 * float button content margin
	 * */
	public static final int VBCONTENTMARGIN = 0;
	
	/**
	 * distance between menu item
	 * */
	public static final int ITEM2ITEMDISTANCE = 10;
	
	/**
	 * distance between text and image in menu item
	 * */
	public static final int TEXT2IMGDISTANCE = 6;
	
	/**
	 * the menu image 's width and height
	 * */
	public static final int MENUIMGWIDTH = 30;
	
	public static final int MENUIMGHEIGHT = 30;
	
	/**
	 * the menu text 's width and height
	 * */
	public static final int MENUTXTWIDTH = 60; 	 	
	
	public static final int MENUTXTHEIGHT = 30;
	
	public static final int OFFSET = 0;
	
	public static final int FONTSIZE = 12;
	
	/**
	 * game text hint
	 * */
	public static final String STRING_PLAYER_SHARE = "分享游戏";
	public static final String STRING_PLAYER_SEND_DESKTOP = "发送桌面";
	public static final String STRING_PLAYER_MORE_GAME = "更多游戏";
	public static final String STRING_PLAYER_EXIT = "退出游戏";
	
	/**
	 * game picture resource
	 * */
	public static final String GAME_PLAYER_BUTTON_CLOSE_DOWN = "/game_player_button_close_down.png";
	public static final String GAME_PLAYER_BUTTON_CLOSE_UP = "/game_player_button_close_up.png";
	public static final String GAME_PLAYER_GAME_EXIT = "/game_player_game_exit.png";
	public static final String GAME_PLAYER_GAME_SHARE = "/game_player_game_share.png";
	public static final String GAME_PLAYER_HOVER = "/game_player_hover.png";
	public static final String GAME_PLAYER_MORE_GAME = "/game_player_more_game.png";
	public static final String GAME_PLAYER_SEND_TO_DESKTOP = "/game_player_send_to_desktop.png";
	public static final String GAME_PLAYER_HOVER_HALF_LEFT = "/game_player_hover_half_left.png";
	public static final String GAME_PLAYER_HOVER_HALF_RIGHT = "/game_player_hover_half_right.png";
	/**
	 * float window layout parameter.
	 * */
	public static WindowManager.LayoutParams getMywmParams(){
		return wmParams;
	}
	
	/**
	 * background window layout parameter.
	 */
	public static  WindowManager.LayoutParams getBackParams(){
		return wmBackParams;
	}
	
	/**
	 * change independent pixels to physical pixels
	 * */
	public static int dip2px(Context context , int dip){
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int)( scale * dip + 0.5f );
	}
	
	/**
	 * change the scaled pixels
	 * @param spV
	 * 			scaled pixels value
	 * */
	public static int sp2px(Context context , float spV){
		final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
		return (int)(spV * fontScale + 0.5f);
	}
}
