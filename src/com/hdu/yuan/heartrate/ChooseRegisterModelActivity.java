package com.hdu.yuan.heartrate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.hdu.yuan.heartrate.object.People;
import com.hdu.yuan.heartrate.object.Point;
import com.hdu.yuan.heartrate.util.DBManager;
import com.hdu.yuan.heartrate.view.SingleWaveDisplayEcg;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


public class ChooseRegisterModelActivity extends Activity implements View.OnClickListener {
	Button JustRegisterSaO2Button,JustRegisterECGButton,RegisterAllButton;
	private ECGApplication hegApplication;
	private BluetoothSocket bluetoothSocket=null;
	private OutputStream outputStream = null; 
	private InputStream inputStream = null;
	private BufferedReader bufferedReader;
	private ProgressBar progressBar=null;
	private SingleWaveDisplayEcg singleWaveDisplay=null;
	private FrameLayout maskLayout;
	private Button startReceive;
	private Button startIdentify;
	private TextView resultText;
	public static String registerimagePath="";//返回识别人的imagePath
	private Timer timer;
	//采集间隔
	private int pointsPerSecond=250;
	private int MAXVALUE=750;//能够接收到的点数
	private int pulseInterval=80;
	private int effLeft=25;
	private int effRight=35;
	private boolean isRecord=false;
	
	private int checkSum=36;
	private int checkLeft=10;
	private int checkRight=26;
	DBManager dbManager;
	private float[] analyse=new float[MAXVALUE];
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		Toast.makeText(this,"请放好电极身份识别后选择采集模式",Toast.LENGTH_LONG).show();
		super.onCreate(savedInstanceState);
		setContentView(R.layout.choose_register_modle);
		JustRegisterSaO2Button = (Button) findViewById(R.id.JustRegisterSaO2Button);
		JustRegisterECGButton = (Button) findViewById(R.id.JustRegisterECGButton);
		RegisterAllButton = (Button) findViewById(R.id.RegisterAllButton);
		
		JustRegisterSaO2Button.setOnClickListener((android.view.View.OnClickListener) this);
		JustRegisterECGButton.setOnClickListener((android.view.View.OnClickListener) this);
		RegisterAllButton.setOnClickListener((android.view.View.OnClickListener) this);
		
		//获取hegApplication 和socket
		hegApplication=(ECGApplication)getApplication();
		bluetoothSocket=hegApplication.getBluetoothSocket();
		//控件初始化
		progressBar=(ProgressBar)findViewById(R.id.progressBarIdentify);
		maskLayout=(FrameLayout)findViewById(R.id.maskPlayIdentify);
		startReceive=(Button)findViewById(R.id.startReceiveIdentify);
		startIdentify=(Button)findViewById(R.id.startRecordIdentify);
		resultText=(TextView)findViewById(R.id.resultIdentify);
		singleWaveDisplay=(SingleWaveDisplayEcg)findViewById(R.id.singlewavedisplayIdentify);
		//设置监听器
		//startReceive.setOnClickListener(receiveListener);
		//startIdentify.setOnClickListener(identifyListener);
		dbManager=new DBManager(this);
		startReceive();
	}
	@Override
	public void onClick(View v) {
//	Toast.makeText(this, "有选择模式按下",Toast.LENGTH_SHORT).show();
		stopTimer();
		switch(v.getId()){
		case R.id.JustRegisterSaO2Button:
			 Intent intentSaO2 = new Intent(ChooseRegisterModelActivity.this, JustRegisterSaO2Activity.class);
			 intentSaO2.addFlags(intentSaO2.FLAG_ACTIVITY_CLEAR_TOP);
		     Window wSaO2 = RegisterModelActivityGroup.registergroup.getLocalActivityManager().startActivity("JustRegisterSaO2Button",intentSaO2);  
		     View viewSaO2 = wSaO2.getDecorView();  
		     RegisterModelActivityGroup.registergroup.setContentView(viewSaO2);  
			 break;
			 
		case R.id.JustRegisterECGButton:
			 Intent intentECG = new Intent(ChooseRegisterModelActivity.this, JustRegisterECGActivity.class);
			 intentECG.addFlags(intentECG.FLAG_ACTIVITY_CLEAR_TOP);
		     Window wECG = RegisterModelActivityGroup.registergroup.getLocalActivityManager().startActivity("JustRegisterECGButton",intentECG);  
		     View viewECG = wECG.getDecorView();  
		     RegisterModelActivityGroup.registergroup.setContentView(viewECG);  
			 break;
			 
		case R.id.RegisterAllButton:
			 Intent intentAll = new Intent(ChooseRegisterModelActivity.this, RegisterAllActivity.class);
			 intentAll.addFlags(intentAll.FLAG_ACTIVITY_CLEAR_TOP);
		     Window wAll = RegisterModelActivityGroup.registergroup.getLocalActivityManager().startActivity("JustRegisterECGButton",intentAll);  
		     View viewAll = wAll.getDecorView();  
		     RegisterModelActivityGroup.registergroup.setContentView(viewAll);  
			 break;
		}
	}
	public void startReceive(){
		maskLayout.setVisibility(View.GONE);
		bluetoothSocket=hegApplication.getBluetoothSocket();
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setMax(MAXVALUE);
        Log.e("startReceive","执行startReceive（）");
			String startControlText="y";
			if(bluetoothSocket!=null){
				startReceive.setClickable(false);
				try {
					outputStream=bluetoothSocket.getOutputStream();
					inputStream=bluetoothSocket.getInputStream();
				} catch (IOException e) {
					e.printStackTrace();
				}
	    		byte[] msgBuffer = startControlText.getBytes();
	    		try {
	    			for (int i = 0; i < 3; i++) {
	    				outputStream.write(msgBuffer);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
	    		
	    		//updateNumberOfShortDataReceivedHandler.post(readShortDataThread);
	    		if(timer==null)
	    			timer=new Timer();
	    		timer.schedule(readTask, 100, 1000/pointsPerSecond);
	    	}else{
	    		Toast.makeText(this, "没有建立与设备的连接，无法发送数据x，无法开始AD转换后数据的接收", Toast.LENGTH_SHORT).show();
	    	}
	}
	public void startIdentifyonClick(View v){
		isRecord=true;
	}
	 Handler writeHandler=new Handler()
	    {

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				if(msg.arg1==MAXVALUE)
				{
					progressBar.setProgress(msg.arg1);
					People temp=null;
	            	try {
	            		//对数据进行分析
	            		//求周期
	            		//找到极值点
	            		int start=0;
	            		//间隔列表
	            		ArrayList<Point> list=new ArrayList<Point>();
	            		for(int i=10;i<MAXVALUE-26;i++)
	            		{
	            			if(analyse[i]-analyse[i-5]>0.35 && analyse[i]-analyse[i-6]>0.3 && analyse[i]-analyse[i-7]>0.25
	            					&& analyse[i]-analyse[i+5]>0.35 && analyse[i]-analyse[i+6]>0.3 && analyse[i]-analyse[i+7]>0.25
	            					&& analyse[i]>=analyse[i-1] && analyse[i]>=analyse[i+1]
	            					&& analyse[i]-analyse[i+12]>0.1 && analyse[i]-analyse[i+13]>0.1
	            					&& analyse[i]-analyse[i+22]>0.1 && analyse[i]-analyse[i+23]>0.1)
	            			{
	            				Point p=new Point();
	            				p.n=i;
	            				p.distance=i-start;
	            				list.add(p);
	            				start=i;
	            			}
	            		}
	            		
	            		if(list.size()<5)
	            		{
	            			Toast.makeText(ChooseRegisterModelActivity.this, "符合要求的点过少，请重新测量", 3).show();
	            			startIdentify.setClickable(true);
	    	            	isRecord=false;
	    	            	progressBar.setProgress(0);
	            			return;
	            		}
	            		
	            		//求极值点的平均值
	            		int sum=0;
	            		Point[] array=new Point[list.size()];
	            		//转变成数组
	            		for(int i=0;i<list.size();i++)
	            		{
	            			array[i]=list.get(i);
	            		}
	            		
	            		//排列  去掉 6个最大值 64个最小值
	            		Arrays.sort(array);
	            		
	            		for(int i=1;i<list.size()-1;i++)
	            		{
	            			sum+=array[i].distance;
	            		}
	            		//均值
	            		int average=sum/(list.size()-1);
	            		
	            		if(average<=checkSum)
	            		{
	            			Toast.makeText(ChooseRegisterModelActivity.this, "符合要求的点过少，请重新测量", 3).show();
	            			startIdentify.setClickable(true);
	    	            	isRecord=false;
	    	            	progressBar.setProgress(0);
	            			return;
	            		}
	            		
	            		//将中间的三段波取出来
	            		float [] tempFloat=new float[checkSum];
	            		//将中间的三个波段取出来
	            		int t=list.get(list.size()/2).n;
	            		int k=0;
	            		for(int i=t-checkLeft;i<t+checkRight;i++)
	            		{
	            			tempFloat[k++]+=analyse[i];
	            		}
	   		
	            		double td=Double.MAX_VALUE;
	            		List<People> peos=dbManager.queryListPeople();
	            		for(People peo:peos)
	            		{
	            			String[] ts=peo.getIdentifydata().split(",");

	            			if(ts.length==checkSum)
	            			{
	                			float [] tf=new float[ts.length];
	                			for(int i=0;i<checkSum;i++)
	                			{
	                				tf[i]=Float.parseFloat(ts[i]);
	                			}
	                			
	                			double distance=0;
	                			for(int i=0;i<checkSum;i++)
	                			{
	                				distance+=(tempFloat[i]-tf[i])*(tempFloat[i]-tf[i]);
	                			}
	                			if(distance<td)//比较出差值最小的人
	                			{
	                				temp=peo;
	                				td=distance;
	                			}
	            			}
	            			else
	            			{
	            				Toast.makeText(ChooseRegisterModelActivity.this, "匹配不成功", Toast.LENGTH_SHORT).show();
	            				startIdentify.setClickable(true);
	        	            	isRecord=false;
	        	            	progressBar.setProgress(0);
	            			}
	            		}
	            		
					} catch (Exception e) {
						e.printStackTrace();
					}
	            	if(temp!=null){
	            		resultText.setText("使用者："+temp.getName());//输出识别人的姓名
		            	registerimagePath = temp.getImagePath();
	            	}
	            	Toast.makeText(ChooseRegisterModelActivity.this, "分析完毕", Toast.LENGTH_SHORT).show();
	            	startIdentify.setText("重新测量");
//	            	startIdentify.setClickable(true);
//	            	isRecord=false;
//	            	progressBar.setProgress(0);
				}
				else 
				{
					progressBar.setProgress(msg.arg1);
				}
			}
	    	
	    };
	    TimerTask readTask=new TimerTask() {
	    	float lastValue=0;
	    	float beforLastValue=0;
	    	int numberRead=0;
	    	int COUNT=0;//清零计数
	    	int size=25;
	    	float[] values=new float[size];//队列形式
	    	int cursor=0;
			@Override
			public void run() {
				// TODO Auto-generated method stub			
	            	COUNT++;
	            	Log.i("ChoosereadTask_run", "readThread线程启动");	
//	            	bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
//
//	            	int readOneIntResult=(int) readOneADresult(bufferedReader);
//	            	readOneIntResult=lowPassFilter(readOneIntResult);
//	            	readOneIntResult=highPassFilter(readOneIntResult);
	            	int readOneIntResult=readOneADresult(inputStream);
	            	float readOneFloatResult=(float)(readOneIntResult*18.3f/128000);
//	            	readOneFloatResult=-readOneFloatResult;
//	            	readOneFloatResult=filter(readOneFloatResult);
	            	//(float)(floatResult*18.3/128000);
	            	if(readOneFloatResult<-0.8 && (readOneFloatResult-lastValue<-0.8 || readOneFloatResult-beforLastValue<-0.8))
	            	{
	            		readOneFloatResult=lastValue;
	            	}
	            	else if(readOneFloatResult>0.6 && readOneFloatResult<2.0 &&(readOneFloatResult-lastValue>0.3 || readOneFloatResult-beforLastValue>0.3))
	            	{
	            		readOneFloatResult+=0.6;
	            		COUNT=0;
	            	}
	            	else if(readOneFloatResult>2.0)
	            	{
	            		readOneFloatResult=1.8f;
	            	}
	            	//T波滤波
	            	if(COUNT>5 && COUNT<effRight)
	            	{
	            		readOneFloatResult=(readOneFloatResult+lastValue+beforLastValue)/3;
	            	}
	            	if(COUNT>effRight && COUNT<pulseInterval-effLeft)
	            		readOneFloatResult=0;
	            	//originalTrainSignal[numberRead]=readOneFloatResult;
	        		beforLastValue=lastValue;
	        		lastValue=readOneFloatResult;
	        		//float readOneFloatResult=filter(readOneFloatResultRaw);
	        		//Log.d(TAG,"value="+readOneFloatResult);
	            	//放进队列
	            	float outPut=0;
	            	if(cursor<size)
	            	{
	            		values[cursor]=readOneFloatResult;
	            		cursor++;
	            	}
	            	else 
	            	{
	            		//移位加中值滤波
	            		outPut=values[0];
	            		for(int i=0;i<size-1;i++)
	            		{
	            			if(COUNT!=0)
	            				values[i]=values[i+1];
	            			else{
	            				if(i<size-5)
	            				{
	            					values[i]=(values[i+1]+values[i+2]+values[i+3])/3;
	            				}
	            			}
	            		}
	            		values[size-1]=readOneFloatResult;
	            		cursor++;
					}
	            	
	            	if(isRecord)
	            	{
	            		analyse[numberRead++]=outPut;
	            	}
	            	singleWaveDisplay.addNewDataToDraw(outPut);
	        		if(numberRead!=MAXVALUE)
	        		{
	        			Message message=new Message();
	        			message.arg1=numberRead;
	        			writeHandler.sendMessage(message);
	        			
	        		}
	        		else {
	        			timer.cancel();
	        			Message message=new Message();
	        			message.arg1=numberRead;
	        			writeHandler.sendMessage(message);
					}
			}
		}; 
		public static int readOneADresult(InputStream inputStrm1){
	 		byte[] buffer = new byte[512];
	 		int bytes;
	 		short floatResult = 0;
	 		try{
	 			try {
	 				//bytes = inputStrm1.read(buffer, 0, 2)
	 				if ((bytes = inputStrm1.read(buffer)) > 0)
	 				{
				    	for(int i=0; i<bytes-4; i++)
				    	{
				    		if((buffer[i]&0xAA)==0xAA && (buffer[i+1]&0xAA)==0xAA)
				    		{
				    			floatResult=(short)(((buffer[i+2] & 0x000000FF) << 8) | (0x000000FF & buffer[i+3]));
				    			break;
				    		}
				    		//buf_data[i] = buffer[i];
				    	}
	 				}
	 			} catch (IOException e) {
	 				e.printStackTrace();
	 			}
	 		}catch (Exception e){
	 		}
	 		buffer=null;
	 		Log.d("ChoosefloatResult", String.valueOf(floatResult));
	 		return (int)floatResult;
	 	}	
//	public float readOneADresult(BufferedReader in) {
//		// byte[] buffer = new byte[512];
//		int bytes = 0;
//		String string = "";
//		float result = -1;
//		try {
//			try {
//				string = in.readLine();// 以行为分界，进行数据解析
//			} catch (IOException e) {
//				Log.d("ChooseRMA.readOneADresult", e.getMessage());
//			}
//			bytes = string.length();
//			if (bytes > 0)// 表明接收到数据
//			{
//				// Log.d(TAG, string);
//				int indexStart = string.indexOf("GG");
//				int indexEnd = string.indexOf("HH");
//				if (indexEnd > indexStart && indexStart != -1) {
//					string = string.substring(indexStart + 2, indexEnd);// 提取AA……BB之间的字符
//					Log.i("心电读取值：", string);
//					try {
//						result = Float.parseFloat(string)-450;
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
//				}
//			}
//		}catch (Exception e) {
//		}
//		//string = null;
//		return result;
//	}
		private void stopTimer(){  
	        if (timer != null) {  
	        	timer.cancel();  
	        	timer = null;  
	        }  
	        if (readTask != null) {  
	        	readTask.cancel();  
	        	readTask = null;  
	        }     
	    }  
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			return false;//返回到MainTabActivity的onKeyDown()方法
		}
		return super.onKeyDown(keyCode, event);
	}

}
