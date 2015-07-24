package com.hdu.yuan.heartrate;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import com.hdu.yuan.heartrate.R;

import android.os.Bundle;

import com.hdu.yuan.heartrate.fragment.HistoryEcgFragment;
import com.hdu.yuan.heartrate.fragment.HistoryFragment;
import com.hdu.yuan.heartrate.fragment.RecordFragment;
import com.hdu.yuan.heartrate.fragment.RegisterFragment;
import com.hdu.yuan.heartrate.fragment.TimeFragment;

import android.R.integer;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.Toast;
  
//该类实现了RecordFragment.OnScrollItemClickListener接口以及内部的方法，
public class RecordActivity extends FragmentActivity implements RecordFragment.OnScrollItemClickListener,TimeFragment.OnScrollItemClickListener{
	private static final String TAG="selection";
	private static final int CAMERA_REQUEST = 1888;
	RecordFragment recordFragment;
	RegisterFragment registerFragment;
	HistoryFragment historyFragment;
	HistoryEcgFragment historyecgFragment;
	TimeFragment timeFragment;
	int page=1;
	float xDown=0,xMove=0;
//	手指向右滑动时的最小速度  
//  private static  int XSPEED_MIN = 100;  
//  手指向右滑动时的最小距离  
//  private static  int XDISTANCE_MIN = 250;  
//	private VelocityTracker mVelocityTracker;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.record_container);
		FragmentTransaction transaction=getSupportFragmentManager().beginTransaction();
		recordFragment=new RecordFragment();
		transaction.add(R.id.record_container, recordFragment);
		
		
		
		transaction.addToBackStack(null);//注释后悔在返回时直接退出，不注释会返回空的界面
		//Commit the transaction
		transaction.commit();
//		XDISTANCE_MIN=((ECGApplication)getApplication()).getScreenWidth()/3+100;
	}
	//实现点击事件，用时间列表替换掉名单列表
	@Override
	public void onItemClickListener(int position, int id) {
		// TODO Auto-generated method stub
		 timeFragment=new TimeFragment();
		 Bundle arguments = new Bundle();  
         arguments.putInt("position",position);  
         arguments.putInt("id",id);
         //setArguments传递参数，将由getArguments方法提取传输参数
         timeFragment.setArguments(arguments);//timeFragment中方法实现了从本地和服务器获取时间列表
		 //replace会destory前一个，而add不会
		 FragmentTransaction transaction=getSupportFragmentManager().beginTransaction();
		 transaction.setCustomAnimations(R.anim.right_in, R.anim.left_out,R.anim.left_in, R.anim.right_out);
		 transaction.replace(R.id.record_container, timeFragment);
		 transaction.addToBackStack(null);
		 transaction.commit();
		 page++;
	}
	//进入用户基本信息注册界面，新创建的registerFragment，添加到界面中
	@Override
	public void onRegisterClick() {
		 //replace会destory前一个，而add不会
		registerFragment=new RegisterFragment();
		 FragmentTransaction transaction=getSupportFragmentManager().beginTransaction();
		 transaction.setCustomAnimations(R.anim.right_in, R.anim.left_out,R.anim.left_in, R.anim.right_out);
		 transaction.replace(R.id.record_container, registerFragment);
		 transaction.addToBackStack(null);
		 transaction.commit();
		 page++;
	}
	//实现RecordFragment中的接口OnScrollItemClickListener，并重写此方法，完成历史记录的显示并启动onclick方法
	public void onTimeItemClickListener(int position,int id,String time)
	{//TimeFragement中实现了此方法
		 historyFragment=new HistoryFragment();
		 historyecgFragment=new HistoryEcgFragment();
		 Bundle arguments = new Bundle();  
                 arguments.putInt("position",position);  
                 arguments.putInt("id",id);
                 arguments.putString("time", time);
         if(time.contains("ecg"))
         {
        	 historyecgFragment.setArguments(arguments);//setArguments传递参数，将由getArguments方法提取传输参数
    		 //replace会destory前一个，而add不会
    		 FragmentTransaction transaction=getSupportFragmentManager().beginTransaction();
    		 transaction.setCustomAnimations(R.anim.right_in, R.anim.left_out,R.anim.left_in, R.anim.right_out);
    		 transaction.replace(R.id.record_container, historyecgFragment);
    		 transaction.addToBackStack(null);
    		 transaction.commit();
    		 page++;
         }else{
	         //timerFragment里面获取了RecordFragment里面onItemClickListener方法传出的id
	         historyFragment.setArguments(arguments);//setArguments传递参数，将由getArguments方法提取传输参数
			 //replace会destory前一个，而add不会
			 FragmentTransaction transaction=getSupportFragmentManager().beginTransaction();
			 transaction.setCustomAnimations(R.anim.right_in, R.anim.left_out,R.anim.left_in, R.anim.right_out);
			 transaction.replace(R.id.record_container, historyFragment);
			 transaction.addToBackStack(null);
			 transaction.commit();
			 page++;
		 }
	}
    /**
     * 接收intent传回的信息
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);   
    	Log.d(TAG,"record activity");
    }
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();		
	} 

//    @Override
//	public boolean onTouchEvent(MotionEvent event) {
//    	 createVelocityTracker(event);
//		// TODO Auto-generated method stub
//    	 switch (event.getAction()) {  
//         case MotionEvent.ACTION_DOWN:  
//             xDown = event.getRawX();  
//             break;  
//         case MotionEvent.ACTION_MOVE:  
//             xMove = event.getRawX();  
//             //活动的距离  
//             int distanceX = (int) (xMove - xDown);  
//             //获取顺时速度  
//             int xSpeed = getScrollVelocity();  
//             //当滑动的距离大于我们设定的最小距离且滑动的瞬间速度大于我们设定的速度时，返回到上一个activity  
//             if(distanceX > XDISTANCE_MIN && xSpeed > XSPEED_MIN) {  
//            	 //onKeyDown(KeyEvent.KEYCODE_BACK,get);
////            	 onBackPressed();
////            	 page--;
//             }  
//             break;  
//         case MotionEvent.ACTION_UP:  
//             recycleVelocityTracker();  
//             break;  
//         default:  
//             break;  
//         }  
//         return true;
//	}
    /** 
     * 创建VelocityTracker对象，并将触摸content界面的滑动事件加入到VelocityTracker当中。 
     *  
     * @param event 
     *         
     */  
//    private void createVelocityTracker(MotionEvent event) {  
//        if (mVelocityTracker == null) {  
//            mVelocityTracker = VelocityTracker.obtain();  
//        }  
//        mVelocityTracker.addMovement(event);  
//    }  
      
    /** 
     * 回收VelocityTracker对象。 
     */  
//    private void recycleVelocityTracker() {  
//        mVelocityTracker.recycle();  
//        mVelocityTracker = null;  
//    }  
      
    /** 
     * 获取手指在content界面滑动的速度。 
     *  
     * @return 滑动速度，以每秒钟移动了多少像素值为单位。 
     */  
//    private int getScrollVelocity() {  
//        mVelocityTracker.computeCurrentVelocity(1000);  
//        int velocity = (int) mVelocityTracker.getXVelocity();  
//        return Math.abs(velocity);  
//    }

	/*以下为退出函数*/
	/**
	 * 菜单、返回键响应
	 */

//	@Override
//public boolean onKeyDown(int keyCode, KeyEvent event) {
//		super.onKeyDown(keyCode, event);
//	    if(keyCode == KeyEvent.KEYCODE_BACK){
//	    	if(page>=2)
//	    	{
//	    		page--;
//	    		return true;
//	    	}
//	    	else if(page==1)
//	    	{
//	    		//finish();
//	    		return false;
//	    	}
//	    }
//	    return false;
//}
	/*@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    super.onKeyDown(keyCode, event);
	    if(keyCode == KeyEvent.KEYCODE_BACK){
	    	if(page>=2)
	    	{
	    		page--;
	    		return true;
	    	}
	    	else
	    	{
	    		
	    		return false;
	    	}
	    }
	    return false;
	}*/
//	@Override
//	public boolean onKeyDown(int keyCode, KeyEvent event) {
//	    super.onKeyDown(keyCode, event);
//	    if(keyCode == KeyEvent.KEYCODE_BACK){
//	    	if(page>=2)
//	    	{
//	    		page--;
//	    		return true;
//	    	}
//	    	else
//	    	{
////	    		MainTabActivity.isDialog = true;
////	    		Intent intent = new Intent(RecordActivity.this, AdministrationActivitySecond.class);  
////	            //intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
////	            Window w =AdministrationActivityGroup.group.getLocalActivityManager()  
////	                    .startActivity("LoginSecond2",intent);  
////	            View view = w.getDecorView();  
////	            AdministrationActivityGroup.group.setContentView(view);    
//	    		//finish();
//	    		Log.e("Record", "onkeydown_else");
//	    		Intent intent = new Intent(this, AdministrationActivitySecond.class); 
//	            //intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
//	            Window w = AdministrationActivityGroup.group.getLocalActivityManager().startActivity(  
//	                    "LoginFirst1", intent);  
//	            View view = w.getDecorView();  
//	            AdministrationActivityGroup.group.setContentView(view);
//	    		return true;
//	    	}
//	    }
//	    return super.onKeyDown(keyCode, event);
//	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		//super.onKeyDown(keyCode, event);
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if(page > 1){
				page--;
				
				//return false;
			}else{
				Intent intent = new Intent(RecordActivity.this, AdministrationActivitySecond.class);  
		        //intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
		        Window w =AdministrationActivityGroup.group.getLocalActivityManager()  
		                .startActivity("LoginFirst2",intent);  
		        View view = w.getDecorView();
		        AdministrationActivityGroup.group.setContentView(view);    
				
				return true;
			}
	        
		}
		return super.onKeyDown(keyCode, event);
	}
	
	/**
	 * 双击退出函数
	 
	private static Boolean isExit = false;

	private void exitBy2Click() {
		Timer tExit = null;
		if (isExit == false) {
			isExit = true; // 准备退出
			Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
			tExit = new Timer();
			tExit.schedule(new TimerTask() {
				@Override
				public void run() {
					isExit = false; // 取消退出
				}
			},2000); // 如果2秒钟内没有按下返回键，则启动定时器取消掉刚才执行的任务

		} else {
			//finish();
			//System.exit(0);
		}
	}*/
	
	
}
