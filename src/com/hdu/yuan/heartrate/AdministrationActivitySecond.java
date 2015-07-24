package com.hdu.yuan.heartrate;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.hdu.yuan.heartrate.util.PreferenceService;

import android.app.Activity;
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

public class AdministrationActivitySecond extends Activity {
	private Button enterLoginThirdButton; 
	private Button checkecghistorydata; 
	private Button changeLoginUsernamePasswordButton;
	private TextView loginsecondIndicateTextView;
	private TextView loginsecondNewUsernameTextView;
	private EditText loginsecondNewUsernameEditText;
	private TextView loginsecondNewPasswordTextView;
	private EditText loginsecondNewPasswordEditText;
	private CheckBox changePasswordShowpasswordcheckBox;
	private Button changeUsernamePasswordButton;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.administrationactivitysecond);
		enterLoginThirdButton=(Button)this.findViewById(R.id.enterLoginThirdButton);
		changeLoginUsernamePasswordButton=(Button)this.findViewById(R.id.changeLoginUsernamePasswordButton);
		loginsecondIndicateTextView=(TextView)this.findViewById(R.id.loginsecondIndicateTextView);
		loginsecondNewUsernameTextView=(TextView)this.findViewById(R.id.loginsecondNewUsernameTextView);
		loginsecondNewUsernameEditText=(EditText)this.findViewById(R.id.loginsecondNewUsernameEditText);
		loginsecondNewPasswordTextView=(TextView)this.findViewById(R.id.loginsecondNewPasswordTextView);
		loginsecondNewPasswordEditText=(EditText)this.findViewById(R.id.loginsecondNewPasswordEditText);
		changePasswordShowpasswordcheckBox=(CheckBox)this.findViewById(R.id.changePasswordShowpasswordcheckBox);
		changeUsernamePasswordButton=(Button)this.findViewById(R.id.changeUsernamePasswordButton);
		
		changePasswordShowpasswordcheckBox.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if(changePasswordShowpasswordcheckBox.isChecked()){
					loginsecondNewPasswordEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
				}else{
					loginsecondNewPasswordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
				}
			}
		});
		
	}
	public void changeLoginUernamePasswordButtonClicked(View v){
		changeLoginUsernamePasswordButton.setClickable(false);
		loginsecondIndicateTextView.setVisibility(View.VISIBLE);
		loginsecondNewUsernameTextView.setVisibility(View.VISIBLE);
		loginsecondNewUsernameEditText.setVisibility(View.VISIBLE);
		loginsecondNewPasswordTextView.setVisibility(View.VISIBLE);
		loginsecondNewPasswordEditText.setVisibility(View.VISIBLE);
		changePasswordShowpasswordcheckBox.setVisibility(View.VISIBLE);
		changeUsernamePasswordButton.setVisibility(View.VISIBLE);
	}
	
		@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
	        Intent intent = new Intent(AdministrationActivitySecond.this, AdministrationActivityFirst.class);  
	        //intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
	        Window w =AdministrationActivityGroup.group.getLocalActivityManager()  
	                .startActivity("LoginFirst2",intent);  
	        View view = w.getDecorView();
	        AdministrationActivityGroup.group.setContentView(view);    
			
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
		
	public void	openLoginThirdActivityButtonClicked(View v){
        Intent intent = new Intent(AdministrationActivitySecond.this, RecordActivity.class);
        intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
        Window w = AdministrationActivityGroup.group.getLocalActivityManager()  
                .startActivity("LoginThird1",intent);  
        View view = w.getDecorView();  
        AdministrationActivityGroup.group.setContentView(view); 
        //startActivity(intent);
	}
	public void changeUsernamePasswordButtonClicked(View v){
		changeUsernamePasswordButton.setClickable(false);
		String textUsername=loginsecondNewUsernameEditText.getText().toString();
		String textPassword=loginsecondNewPasswordEditText.getText().toString();
		PreferenceService login_preferenceService=new PreferenceService(this);
		
		String textPasswordMD5;
		try {
			textPasswordMD5=this.getMD5(textPassword);
		} catch (NoSuchAlgorithmException e) {
			Toast.makeText(this, "无法加密该密码", Toast.LENGTH_LONG).show();
			textPasswordMD5="无法加密该密码";
		}
		
		if(!textUsername.equals("")){
			if(!textPassword.equals("")){
				
				login_preferenceService.saveSharedPreferencesUsernamePassword(textUsername, textPasswordMD5);
				Toast.makeText(this, "完成管理员账号的修改,请牢记用户名和密码", Toast.LENGTH_LONG).show();
				
				loginsecondIndicateTextView.setVisibility(View.INVISIBLE);
				loginsecondNewUsernameTextView.setVisibility(View.INVISIBLE);
				loginsecondNewUsernameEditText.setVisibility(View.INVISIBLE);
				loginsecondNewPasswordTextView.setVisibility(View.INVISIBLE);
				loginsecondNewPasswordEditText.setVisibility(View.INVISIBLE);
				changePasswordShowpasswordcheckBox.setVisibility(View.INVISIBLE);
				changeUsernamePasswordButton.setVisibility(View.INVISIBLE);
				
			}else{
				Toast.makeText(this, "请输入密码", Toast.LENGTH_LONG).show();
				changeUsernamePasswordButton.setClickable(true);
			}
		}else{
			Toast.makeText(this, "请输入用户名", Toast.LENGTH_LONG).show();
			changeUsernamePasswordButton.setClickable(true);
		}
		
	}
	
	public static String getMD5(String val) throws NoSuchAlgorithmException {
		MessageDigest md5 = MessageDigest.getInstance("MD5");
		md5.update(val.getBytes());
		byte[] m = md5.digest();
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
