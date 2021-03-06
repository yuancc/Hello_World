package com.hdu.yuan.heartrate;

import android.app.ActivityGroup;
import android.app.ActivityGroup;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

public class RegisterModelActivityGroup extends ActivityGroup {
		public static ActivityGroup registergroup;

		@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			registergroup=this;
		}

		@Override
		public void onBackPressed() {
			registergroup.getLocalActivityManager().getCurrentActivity().onBackPressed();  
	        
		}
		
		public boolean onKeyDown(int keyCode, KeyEvent event) {
			Log.e("registergroup", "onkeydown");
			Log.e("registergroup", registergroup.getLocalActivityManager().getCurrentActivity().toString());
			return registergroup.getLocalActivityManager().getCurrentActivity()
					.onKeyDown(keyCode, event);
		}

		@Override
		protected void onStart() {
			super.onStart();
	        Intent intent = new Intent(this, ChooseRegisterModelActivity.class); 
	        intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
	        Window w = registergroup.getLocalActivityManager().startActivity(  
	                "ChooseRegisterModelActivity", intent);  
	        View view = w.getDecorView();  
	        registergroup.setContentView(view);  
		}  
		
		 public boolean onCreateOptionsMenu(Menu menu) { 
		    	super.onCreateOptionsMenu(menu); 
		    	MenuInflater inflater = new MenuInflater(getApplicationContext()); 
		    	inflater.inflate(R.menu.menu, menu); 
		    	return true; 
		    } 

		    public boolean onOptionsItemSelected(MenuItem item) { 
		    	String info = ""; 
		    	switch (item.getItemId()) { 
		    	case R.id.menu_exit: 
		    		info = "点击退出"; 
			    	new AlertDialog.Builder(AdministrationActivityGroup.group)
					.setTitle("确认退出吗？")  
				    .setPositiveButton("确定",
				    new DialogInterface.OnClickListener()
				    {  
				        public void onClick(DialogInterface dialog, int which) {  
				        	Toast.makeText(AdministrationActivityGroup.group, "退出应用", Toast.LENGTH_SHORT).show(); 
				        	finish();  
				        }  
				    })  
				    .setNegativeButton("返回", 
				    new DialogInterface.OnClickListener() 
				    {  
				        public void onClick(DialogInterface dialog, int which) {  
				        }  
				    }).show();  
		    	break; 
		    	default: 
		    	info = "NULL"; 
		    	break; 
		    	} 
		    	Toast.makeText(this, info, Toast.LENGTH_SHORT).show(); 
		    	return super.onOptionsItemSelected(item); 
		    } 

}
