package com.hdu.yuan.heartrate;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.hdu.yuan.heartrate.util.PreferenceService;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class AdministrationActivityFirst extends Activity {
	private TextView instructionTextView; 
	private EditText usernameEditText;
	private EditText passwordEditText;
	private CheckBox showPasswordCheckBox;
	private Button loginButton;
	
	private PreferenceService login_preferenceService;
	private String UsernameAllreadyExist;
	private String PasswordAllreadyExist;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.administrationactivityfirst);
		
		loginButton=(Button)this.findViewById(R.id.loginButton);
		instructionTextView=(TextView)this.findViewById(R.id.instructionTextView);
		usernameEditText=(EditText)this.findViewById(R.id.usernameEditText);
		passwordEditText=(EditText)this.findViewById(R.id.passwordEditText);
		showPasswordCheckBox=(CheckBox)this.findViewById(R.id.showpasswordcheckBox);
		showPasswordCheckBox.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if(showPasswordCheckBox.isChecked()){
					passwordEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
				}else{
					passwordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
				}
			}
		});
		
		login_preferenceService=new PreferenceService(this);
		UsernameAllreadyExist=login_preferenceService.getUsername();
		PasswordAllreadyExist=login_preferenceService.getPassword();
		if((UsernameAllreadyExist==null)||(PasswordAllreadyExist==null)){
			Toast.makeText(this, "第一次使用，请设置管理员账号", Toast.LENGTH_LONG).show();
			instructionTextView.setText("第一次使用，请注册管理员账号");
			loginButton.setText("注册管理员账号");
		}else{
			Toast.makeText(this, "请输入用户名和密码登陆", Toast.LENGTH_LONG).show();
		}
	}
	
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		Toast.makeText(this, "请输入用户名和密码登陆", Toast.LENGTH_LONG).show();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			new AlertDialog.Builder(AdministrationActivityGroup.group)
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
	
	
	public void LoginButtonClicked(View v){
		String textUsername=usernameEditText.getText().toString();
		String textPassword=passwordEditText.getText().toString();
		String textPasswordMD5;
		try {
			textPasswordMD5 = this.getMD5(textPassword);
		} catch (NoSuchAlgorithmException e) {
			Toast.makeText(this, "无法加密该密码", Toast.LENGTH_LONG).show();
			textPasswordMD5="无法加密该密码";
		}
		
		if((UsernameAllreadyExist==null)||(PasswordAllreadyExist==null)){
			if(!textUsername.equals("")){
				if(!textPassword.equals("")){
					
					login_preferenceService.saveSharedPreferencesUsernamePassword(textUsername, textPasswordMD5);
					Toast.makeText(this, "完成管理员账号的创建,请牢记用户名和密码", Toast.LENGTH_LONG).show();
			        Intent intent = new Intent(AdministrationActivityFirst.this, AdministrationActivitySecond.class);
			        intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
			        Window w = AdministrationActivityGroup.group.getLocalActivityManager()  
			                .startActivity("LoginSecond1",intent);  
			        View view = w.getDecorView();  
			        AdministrationActivityGroup.group.setContentView(view);  
					
				}else{
					Toast.makeText(this, "请输入密码", Toast.LENGTH_LONG).show();
				}
			}else{
				Toast.makeText(this, "请输入用户名", Toast.LENGTH_LONG).show();
			}
		}else{
			if(!textUsername.equals("")){
				if(!textPassword.equals("")){
					if(textUsername.equals(UsernameAllreadyExist)&&textPasswordMD5.equals(PasswordAllreadyExist)){
						Toast.makeText(this, "完成管理员账号的登陆", Toast.LENGTH_LONG).show();
				        Intent intent = new Intent(AdministrationActivityFirst.this, AdministrationActivitySecond.class);
				        intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
				        Window w = AdministrationActivityGroup.group.getLocalActivityManager()  
				                .startActivity("LoginSecond1",intent);  
				        View view = w.getDecorView();  
				        AdministrationActivityGroup.group.setContentView(view);  
					}else{
						Toast.makeText(this, "输入的用户名或者密码有误，请重新输入", Toast.LENGTH_LONG).show();
						usernameEditText.setText("");
						passwordEditText.setText("");
					}
				}else{
					Toast.makeText(this, "请输入密码", Toast.LENGTH_LONG).show();
				}
			}else{
				Toast.makeText(this, "请输入用户名", Toast.LENGTH_LONG).show();
			}
			
		}
		
		
	}
	
	
	
	public static String getMD5(String val) throws NoSuchAlgorithmException {
		MessageDigest md5 = MessageDigest.getInstance("MD5");
		md5.update(val.getBytes());
		byte[] m = md5.digest();// 加密
		return getString(m);
	}

	private static String getString(byte[] b) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < b.length; i++) {
			sb.append(b[i]);
		}
		return sb.toString();
	}
	

	
}
