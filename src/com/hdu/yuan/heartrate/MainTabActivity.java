package com.hdu.yuan.heartrate;

import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.TabHost;

public class MainTabActivity extends TabActivity {
	private static final String TAG = "MainTabActivity";
	public static boolean isDialog = false;
	private TabHost tabHost;
	private String ChooseButton1 = "连接";
	private String ChooseButton2 = "采集";
	private String ChooseButton3 = "注册";
	private String ChooseButton4 = "管理";
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tab_main);//没有此行会在最上面显示TabHost
		tabHost=getTabHost();
		Log.i(TAG, "执行MainTabActivity的onCreate()方法");
		//系统进入MainTabActivity后会默认进入第一个tab_test1界面即 BluetoothConnectIntent时间相应的界面
        Intent BluetoothConnectIntent = new Intent(this,BluetoothConnectActivity.class);
        Intent CollectionActivityIntent=new Intent(this,RegisterModelActivityGroup.class);
        Intent LoginActivityIntent=new Intent(this,RegisterNewUserActivity.class);
        Intent AdministrationActivityIntent=new Intent(this,AdministrationActivityGroup.class);
        		
        tabHost.addTab(tabHost.newTabSpec("tab_test1").setIndicator(ChooseButton1, getResources().getDrawable((R.drawable.theme_menu_bluetooth))).setContent(BluetoothConnectIntent));
        tabHost.addTab(tabHost.newTabSpec("tab_test2").setIndicator(ChooseButton2, getResources().getDrawable((R.drawable.theme_menu_register))).setContent(CollectionActivityIntent));
        tabHost.addTab(tabHost.newTabSpec("tab_test3").setIndicator(ChooseButton3, getResources().getDrawable((R.drawable.theme_menu_identification))).setContent(LoginActivityIntent));
        tabHost.addTab(tabHost.newTabSpec("tab_test4").setIndicator(ChooseButton4, getResources().getDrawable((R.drawable.theme_menu_login))).setContent(AdministrationActivityIntent));
	}	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(isDialog){
			return super.onKeyDown(keyCode, event);
		}
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Log.e("onKeydown", "ok");
			new AlertDialog.Builder(MainTabActivity.this)
			.setTitle("确认退出吗？")  
		    .setPositiveButton("确定",
		    new DialogInterface.OnClickListener()
		    {  
		        public void onClick(DialogInterface dialog, int which) {  
		        	finish();  
		        }  
		    })  
		    .setNegativeButton("返回", 
		    new DialogInterface.OnClickListener() 
		    {  
		        public void onClick(DialogInterface dialog, int which) {  
		        }  
		    }).show();  
			return true;
		}
		
		return super.onKeyDown(keyCode, event);
	}
}
