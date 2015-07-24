package com.hdu.yuan.heartrate.util;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class PreferenceService {
	private Context context;

	public PreferenceService(Context context) {
		this.context = context;
	}
	
	public  void saveSharedPreferencesUsernamePassword(String username,String password){
		SharedPreferences preferences=context.getSharedPreferences("loginControlData", Context.MODE_PRIVATE);
		Editor editor=preferences.edit();
		editor.putString("username", username);
		editor.putString("password", password);
		editor.commit();
	}
	
	public String getUsername(){
		String Username;
		SharedPreferences sharedPreferences=context.getSharedPreferences("loginControlData", Context.MODE_PRIVATE);
		Username=sharedPreferences.getString("username", null);
		return Username;
	}
	public String getPassword(){
		String Password;
		SharedPreferences sharedPreferences=context.getSharedPreferences("loginControlData", Context.MODE_PRIVATE);//指定从哪个xml文件中读取密码
		Password=sharedPreferences.getString("password", null);
		return Password;
	}
	
}
