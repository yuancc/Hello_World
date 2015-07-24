package com.hdu.yuan.heartrate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.hdu.yuan.heartrate.R;
import com.hdu.yuan.heartrate.R.id;
import com.hdu.yuan.heartrate.adapter.ImageAdapter;
import com.hdu.yuan.heartrate.object.People;
import com.hdu.yuan.heartrate.util.DBManager;
import com.hdu.yuan.heartrate.util.FileService;
import com.hdu.yuan.heartrate.util.HttpUtil;
import com.hdu.yuan.heartrate.view.SingleWaveDisplay;
import com.hdu.yuan.heartrate.view.SingleWaveDisplayEcg;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.R.integer;
import android.R.string;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.provider.MediaStore;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.hdu.yuan.heartrate.object.*;
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
@SuppressLint("NewApi")
public class RegisterActivity extends Activity {
	private final static String TAG="selection";
	private static final int CAMERA_REQUEST = 1888;
	private String url_chose = "http://115.28.0.158:9523/index.php";//http://192.168.161.102:80/index.php
	private ECGApplication hegApplication;
	//private int pointsPerSecond=100;
	public static int MAXVALUE=1000;//10s能够接收到的点数
	private int ARGCOUNT=10;
	/*----------增加ECG-----------*/
	private int pulseInterval=80;
	private int effLeft=25;
	private int effRight=35;
	//private int pulseInterval=16;
	//private int effLeft=5;
	//private int effRight=7;
	
	float lastValue=0;
 	float beforLastValue=0;
	float lastValuemb=0;
 	float beforLastValuemb=0;
 	
 	int numberRead=0;
 	int COUNT=0;//清零计数
 	int size=25;
 	
 	float[] values=new float[size];//队列形式
 	int cursor=0;
 	
	/*--------------------------*/
	//控件
	private Button startReceiveLongDataButton=null;
	private Button startRecordButton=null;
	private FrameLayout maskLayout;
	private CheckBox cBox=null;
	 ProgressDialog dialog2;
	/*----------增加ECG-----------*/
	private FrameLayout maskLayoutecg;
	/*--------------------------*/
	private EditText nameEdit,ageEdit;
	private TextView xueyang,maibo,tiwen,timeText;
	ImageView takePhoto;
	Gallery gallery;
	//资源
	Drawable manDrawable;
	Drawable womanDrawable;
	//全局变量
	private boolean isMan=true;
	List<People> peoples;
	//数据库
	DBManager dbManager;
	//
	ImageAdapter imageAdapter;
	//蓝牙
	private BluetoothSocket bluetoothSocket=null;
	private OutputStream outputStream = null; 
	private InputStream inputStream = null;
	private ProgressBar progressBar=null;
	private BufferedReader bufferedReader;
	private SingleWaveDisplay singleWaveDisplay=null;
	
	private SingleWaveDisplayEcg singleWaveDisplayecg=null;
	
	private boolean canReadFlag=true;
	private boolean isRecord=false;
	private boolean STOP=false;
	//private boolean xyfinish=false;
	//private boolean ecgfinish=false;
	
	
	//StringBuilder EcgstringBuilder=new StringBuilder();
	
	//需要存进数据库的数据
	MeasureArg mArg;
	float[] datasOfMearsured=new float[MAXVALUE];
	
	//float[] EcgDatasOfMearsured=new float[MAXVALUE];
	
	int currentCursorDatas=0;
	
	//int currentCursorEcgDatas=0;
	
	int currentCursorArg=0;
	int currentPeopleId=0;
	
	float Newtw = 35.0f;
	float Oldtw = 35.0f;
	int Newxy = 95;
	int Oldxy = 95;
	int Newmb = 71;
	int Oldmb = 71;
	float OnefloatEcgResult = 0;//全局变量存储心电当前值
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register);
		//控件初始化 接收  记录
		startReceiveLongDataButton=(Button)findViewById(R.id.startReceive);//开始显示接收数据按钮
		startRecordButton=(Button)findViewById(R.id.startRecord);//开始存储数据按钮
		//saveButton=(Button)findViewById(R.id.saveInfo);
		progressBar = (ProgressBar)findViewById(R.id.progressBar1);
		singleWaveDisplay=(SingleWaveDisplay)this.findViewById(R.id.singlewavedisplay);
		
		singleWaveDisplayecg=(SingleWaveDisplayEcg)this.findViewById(R.id.singlewavedisplayecg);
		
		maskLayout=(FrameLayout)findViewById(R.id.maskPlay);//波形显示框
		maskLayoutecg=(FrameLayout)findViewById(R.id.maskPlayecg);//波形显示框
		//takePhoto=(ImageView)findViewById(R.id.portrait);
		gallery=(Gallery)findViewById(R.id.imagesGallery);//头像显示位置
		gallery.setSpacing(((ECGApplication)getApplication()).getScreenWidth()/2);
		Log.d(TAG,""+((ECGApplication)getApplication()).getScreenWidth());
		xueyang=(TextView)findViewById(R.id.xueyang);
		maibo=(TextView)findViewById(R.id.maibo);
		tiwen=(TextView)findViewById(R.id.tiwen);
		timeText=(TextView)findViewById(R.id.timeRegister);
		//
		peoples=new ArrayList<People>();
		imageAdapter=new ImageAdapter(peoples, this);
		gallery.setAdapter(imageAdapter);
		gallery.setOnItemClickListener(onItemClickListener);
		
		//绑定checkbox
		cBox=(CheckBox)findViewById(R.id.cb);
		//资源初始化
		manDrawable=getResources().getDrawable(R.drawable.man);
		womanDrawable=getResources().getDrawable(R.drawable.woman);
		//数据库初始化
		dbManager=new DBManager(this);

        //
		mArg=new MeasureArg();
		
		hegApplication=(ECGApplication)getApplication();
		
		
		
		
		
		
		//这个地方可以设置默认提醒，注释掉本行
		bluetoothSocket=hegApplication.getBluetoothSocket();
		
		
		
		
		
		
		
		
		
		if(bluetoothSocket!=null){
		}else{
			Toast.makeText(this, "请返回蓝牙选项卡，完成与设备的蓝牙配对", Toast.LENGTH_SHORT).show();
		}
		Thread thread=new Thread(new TimeThread());
		thread.start();
	}
	class TimeThread implements Runnable
	{
		@Override
		public void run() {
			// TODO Auto-generated method stub
			while(!STOP)
			{
				//"yyyy-MM-dd HH:mm:ss" 24小时制时间格式，"yyyy-MM-dd hh:mm:ss"12小时制时间格式
				SimpleDateFormat  sDateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");       
				String date=sDateFormat.format(new java.util.Date());
				Message msg=new Message();
				msg.what=5;
				msg.obj=date;
				textHandler.sendMessage(msg);
				try {
					Thread.sleep(100);
				} catch (Exception e) {
					// TODO: handle exception
				}
			}
		}
		
	}
	/*****
	 * 控件事件区
	 */
	@SuppressLint("NewApi")
	public void hideMaskAndRec(View v)
	{
		canReadFlag=!canReadFlag;
		if(canReadFlag)
		{
			Log.d(TAG, "停止");
			//设置视图的透明度。这是一个值从0到1 ，其中0表示该视图是完全透明的，1表示该视图是完全不透明的。
			maskLayout.setAlpha(0);
			maskLayoutecg.setAlpha(0);
			
			//之前没有启动该方法？？？？？？？？？？？？
			startReceiveLongDataClicked();
			
			
			dialog2= ProgressDialog.show(this, "身份认证", "正在认证。。");  
			dialog2.show();
			Thread thread=new Thread(runnable);
			thread.start(); 
			  
			  
			  
			 
		}
		else {
			Log.d(TAG, "开始");
			maskLayout.setAlpha(0.8f);
			maskLayoutecg.setAlpha(0.8f);
		}
	}
	public void startReceiveLongDataClicked(){
		bluetoothSocket=hegApplication.getBluetoothSocket();
		
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setMax(MAXVALUE);
			String startControlText="x";
			if(bluetoothSocket!=null){
				
				
				
				//点击存储后就不能响应再次点击
				startReceiveLongDataButton.setClickable(false);
				
				
				
				try {
					outputStream=bluetoothSocket.getOutputStream();
					inputStream=bluetoothSocket.getInputStream();
				} catch (IOException e) {
					e.printStackTrace();
				}
	    		byte[] msgBuffer = startControlText.getBytes();
	    		try {
					outputStream.write(msgBuffer);//使用输出流输出一个字符x给下位机
				} catch (IOException e) {
					e.printStackTrace();
				}
	    		
	    		Thread thread=new Thread(new ReadThread());
	    		thread.start();
	    		
	    		/********************ECG线程*********************/
	    		//Thread threadecg=new Thread(new ReadEcgThread());
	    		//threadecg.start();
	    	}else{
	    		Toast.makeText(this, "没有建立与设备的连接，无法发送数据x，无法开始AD转换后数据的接收", Toast.LENGTH_SHORT).show();
	    	}
	}
	@SuppressLint("NewApi")
	public void startRecord(View v)
	{
		isRecord=!isRecord;
		if(isRecord)
		{
			startRecordButton.setText("存储中.....");
			startRecordButton.setClickable(false);
		}
		else {
			startRecordButton.setText("开始存储");
		}
	}
	public void sexSelector(View v)
	{
		isMan=!isMan;
		if(isMan)
		{
			v.setBackground(manDrawable);
		}
		else {
			v.setBackground(womanDrawable);
		}
	}

	/***
	 * 
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
		}
		return false;//交由其他处理
	}
    Handler textHandler=new Handler()
    {
		@SuppressLint("NewApi")
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			if(msg.what==1)
			{
				MeasureArg measureArg=(MeasureArg)msg.obj;
				
				xueyang.setText(measureArg.xueyang+"%");
				maibo.setText(measureArg.maibo+"次/分");
				tiwen.setText(measureArg.tiwen+"℃");
				/*****************图造假*******************/
				//xueyang.setText("96%");
				//maibo.setText("75次/分");
				//tiwen.setText("36.6℃");
				
				currentCursorArg++;
				
				mArg.maibo=measureArg.maibo;
				mArg.tiwen=measureArg.tiwen;
				mArg.xueyang=measureArg.xueyang;
			}
			
			else if(msg.what==2)
			{
				say("存储成功");
				progressBar.setProgress(0);
				maskLayout.setAlpha(0.8f);
				startRecordButton.setText("开始存储");
				//数据存储完成，点击存储按钮可以使用
				startReceiveLongDataButton.setClickable(true);
				
							    			    						
			}
			else if(msg.what==5)
			{
				timeText.setText(msg.obj.toString());
			}
		}
    	
    };
    class  ReadThread implements Runnable{
		@Override
		public void run(){
			//把蓝牙输入的数据inputStream放到读数据缓存区
			bufferedReader=new BufferedReader(new InputStreamReader(inputStream));
			// TODO Auto-generated method stub				
            while(canReadFlag==true){
            	float data=readOneADresult(bufferedReader);
            	float dataecg=DealOneEcgdata(OnefloatEcgResult);
            	/*增加ECG数据提取部分*/
            	//float dataecg=readOneADresultecg(inputStream);
            	
            	//Log.d(TAG, "data="+data);
            	if(data!=-1)
            	{
                	//readOneADresult（）里面有温度、脉搏、血氧值得显示，所以此处只要添加脉搏波形值就行了
            		singleWaveDisplay.addNewDataToDraw(data);
            		
            		singleWaveDisplayecg.addNewDataToDraw(dataecg);
            		
            		if(isRecord)//判断是否要记录数据，如果需要记录的话，会在本线程里面把数据信息添加到数据库中
            		{
	            		datasOfMearsured[currentCursorDatas]=data;
	            		currentCursorDatas++;
	            		progressBar.setProgress(currentCursorDatas);
	            		if(currentCursorDatas==MAXVALUE)
	            		{
	            			String stopControlText="y";
	            			if(bluetoothSocket!=null){
	            	    		byte[] msgBuffer = stopControlText.getBytes();
	            	    		try {
	            					outputStream.write(msgBuffer);
	            				} catch (IOException e) {
	            					e.printStackTrace();
	            				}	
	            	    	}
	            			//恢复记录按钮
	            			startRecordButton.setClickable(true);
	            			canReadFlag=false;
	            			isRecord=false;
	            			currentCursorDatas=0;
	            			currentCursorArg=0;
	            			
	            			History history=new History();
	            			//顺带清空数据
	            			StringBuilder stringBuilder=new StringBuilder();
	            			for(int i=0;i<datasOfMearsured.length-1;i++)
	            			{
	            				stringBuilder.append(String.valueOf(datasOfMearsured[i])+",");//以“，”隔开脉搏波形值
	            				datasOfMearsured[i]=0;
	            			}
	            			stringBuilder.append(String.valueOf(datasOfMearsured[datasOfMearsured.length-1]));
	            				datasOfMearsured[datasOfMearsured.length-1]=0;
	            			
	            			history.setData(stringBuilder.toString());
	            			//history.setEcgData(EcgstringBuilder.toString());
	            			
	            			int position=gallery.getSelectedItemPosition();
	            			history.setPeopleId(imageAdapter.getList().get(position).getId());
	            			history.setMeasureArg(mArg);
	            			
	            			//Log.d(TAG, "   "+mArg.maibo+mArg.tiwen+mArg.xueyang);
	            			SimpleDateFormat  sDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");       
	            			String date=sDateFormat.format(new java.util.Date()); 
	            			history.setTimeOfRecord(date);
	            			
	            			//把数据按照格式放在数据库中，
	            			//db.execSQL("INSERT INTO history VALUES(null,?,?,?,?,?,?)", new Object[]{history.getPeopleId(), history.getData(),history.getMeasureArg().xueyang,history.getMeasureArg().tiwen,history.getMeasureArg().maibo,history.getTimeOfRecord()});
	            			
	            			if(cBox.isChecked())//存储到本地
	            			{
	            				dbManager.addHistory(history);
	            			}
	            			else {//存储在服务器
	            				List<NameValuePair> params = new ArrayList<NameValuePair>();
            				    params.add(new BasicNameValuePair("action","insertXY"));
	            				params.add(new BasicNameValuePair("userId",String.valueOf(history.getPeopleId())));//2015-03-19 16:47:40
	            				params.add(new BasicNameValuePair("xy_data",history.getData()));//2015-03-19 16:47:40
	            				params.add(new BasicNameValuePair("xy_time",history.getTimeOfRecord()));//2015-03-19 16:47:40
	            				params.add(new BasicNameValuePair("xy",history.getMeasureArg().xueyang));//2015-03-19 16:47:40
	            				params.add(new BasicNameValuePair("mb",history.getMeasureArg().maibo));//2015-03-19 16:47:40
	            				params.add(new BasicNameValuePair("tw",history.getMeasureArg().tiwen));//2015-03-19 16:47:40
	            				String builder=HttpUtil.getString(url_chose, params);
	            				Log.d(TAG, builder);
	            				
	            				
	            			//	List<NameValuePair> ecgparams = new ArrayList<NameValuePair>();
	            			//	ecgparams.add(new BasicNameValuePair("action","insertXD"));
	            			//	ecgparams.add(new BasicNameValuePair("userId",String.valueOf(history.getPeopleId())));//2015-03-19 16:47:40
		            		//	ecgparams.add(new BasicNameValuePair("xd_data",history.getEcgData()));//2015-03-19 16:47:40
		            		//	ecgparams.add(new BasicNameValuePair("xd_time",history.getTimeOfRecord()));//2015-03-19 16:47:40
		         		   //     String ecgbuilder=HttpUtil.getString("http://192.168.161.102:80/index.php", ecgparams);
		            				
	            			//	Log.d(TAG, ecgbuilder);
	            				//Toast.makeText(RegisterActivity.this, builder, Toast.LENGTH_SHORT).show();
							}
	            			
	            			Message message=new Message();
	            			message.what=2;
	            			textHandler.sendMessage(message);
	            			
	            		}
            		}
            	}
            }
		}
	}
  public float DealOneEcgdata(float floatEcgResult)
  {
	  COUNT++;
	  float readOneFloatResult=(float)(floatEcgResult*18.3f/7000);
	  Log.d("resultecg", String.valueOf(readOneFloatResult));
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
	 	if(COUNT>effRight && COUNT<pulseInterval-effLeft)//80-35=55
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
		 return readOneFloatResult;
  }
    //CC95DD100EE3439FF   第一个是血氧，第二个是脉搏，第三个是体温
 	public  float readOneADresult(BufferedReader in){
 		//byte[] buffer = new byte[512];
 		int bytes=0;
 		String string="";
 		String xystring="";
 		String xdstring="";
 		float result=-1;
 		try{
 				try{
 					string = in.readLine();//以行为分界，进行数据解析
 				}catch(IOException e)
 				{
 					Log.d(TAG, e.getMessage());
 				}
 				bytes=string.length();
 				if (bytes > 0)//表明接收到数据
 				{
 					//Log.d(TAG, string);
 					int indexStart=string.indexOf("AA");
 					int indexEnd=string.indexOf("BB");
 					if(indexEnd>indexStart && indexStart!=-1)
 					{
 						xystring=string.substring(indexStart+2,indexEnd);//提取AA……BB之间的字符
 	 					try
 	 					{
 	 						result=Float.parseFloat(xystring);
 	 						result=result*2.0f/1000;
 	 					}catch(Exception e)
 	 					{
 	 						e.printStackTrace();
 	 					}
 					}
 					int indexCC=string.indexOf("CC");
 					int indexDD=string.indexOf("DD");
 					int indexEE=string.indexOf("EE");
 					int indexFF=string.indexOf("FF");
 					int indexGG=string.indexOf("GG");
 					int indexHH=string.indexOf("HH");
 					//判断心电数值传输值
 					Log.d("xd",string.substring(indexGG+2,indexHH));
 					if(indexHH>indexGG && indexGG!=-1)
 					{
 						xdstring=string.substring(indexGG+2,indexHH);
 						Log.d("xdstring",xdstring);
 						try
 	 					{
 							OnefloatEcgResult=Float.parseFloat(xdstring)-450;
 							//OnefloatEcgResult=OnefloatEcgResult*2.0f/1000;
 	 					}catch(Exception e)
 	 					{
 	 						e.printStackTrace();
 	 					}
 					}
 					Log.d("OnefloatEcgResult",xdstring);
 					if(indexFF>indexCC && indexCC!=-1 &&indexDD!=-1 && indexEE!=-1)
 					{
 						Log.d(TAG,string);
 						/*MeasureArg measureArg=new MeasureArg();
 						String xString=string.substring(indexCC+2, indexDD);
 						measureArg.xueyang=xString;
 						String mString=string.substring(indexDD+2, indexEE);
 						measureArg.maibo=mString;
 						String tString=string.substring(indexEE+2, indexFF);
 						measureArg.tiwen=String.valueOf(Integer.parseInt(tString)*1.0f/100);*/
 						
 						MeasureArg measureArg=new MeasureArg();
 						String xString=string.substring(indexCC+2, indexDD);
 						Newxy = Integer.parseInt(xString);
 						if(Newxy>99||Newxy<80){
 							Newxy = Oldxy;
 						}
 						else{
 							Oldxy = Newxy;
 						}
 						measureArg.xueyang=String.valueOf(Newxy);
 						
 						String mString=string.substring(indexDD+2, indexEE);
 						Newmb = Integer.parseInt(mString);
 						if(Newmb>110||Newmb<50){
 							Newmb = Oldmb;
 						}
 						else{
 							Oldmb = Newmb;
 						}
 						measureArg.maibo=String.valueOf(Newmb);
 						
 						String tString=string.substring(indexEE+2, indexFF);
 						Newtw = Integer.parseInt(tString)*1.0f/100;
 						if(Newtw>40.0||Newtw<32.0){
 							Newtw = Oldtw;
 						}
 						else{
 							Oldtw = Newtw;
 						}
 						measureArg.tiwen=String.valueOf(Newtw);
 						
 						
 						Message msg=new Message();
 						msg.what=1;
 						msg.obj=measureArg;
 						textHandler.sendMessage(msg);
 					}
 				}
 		}catch (Exception e){
 		}
 		string=null;
 		if((result>0.0f)&&(result<1.5f))
 		{
 			beforLastValuemb=lastValuemb;
 			lastValuemb=result;
 			if(lastValuemb-beforLastValuemb>0.8f)//处理突然增高(脉搏波：1.2f显示的话在2.4f上；心电则原值显示)
 				result=beforLastValuemb+0.1f;
 			if(beforLastValuemb-lastValuemb>0.8f)//处理突然降低
 				result=beforLastValuemb-0.1f;
 			
 		}else
 			return lastValuemb;
 		
 		return result;
 	}
 	
 	private void say(String str)
 	{
 		Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
 	}
	OnItemClickListener onItemClickListener=new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view,  
                int position, long id) {
			// TODO Auto-generated method stub
			currentPeopleId=imageAdapter.getList().get(position).getId();
			Toast.makeText(RegisterActivity.this, imageAdapter.getList().get(position).getName(), Toast.LENGTH_SHORT).show();
		}
		
	};
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
//		Log.d(TAG, "ecg record onresume");
		imageAdapter.setList(dbManager.queryListPeople());
		//Log.d(TAG,imageAdapter.getCount()+"个");
		imageAdapter.notifyDataSetChanged();
	}
    @Override
	protected void onPause() {
		String stopControlText="y";
		if(bluetoothSocket!=null){
    		byte[] msgBuffer = stopControlText.getBytes();
    		try {
				outputStream.write(msgBuffer);
			} catch (IOException e) {
				e.printStackTrace();
			}	
    	}
		Log.i(TAG, "执行onPause");
		super.onPause();
	}
    
    @Override
	protected void onDestroy() {
    	dbManager.closeDB();
    	canReadFlag=false;
    	STOP=true;
		String stopControlText="y";
		if(bluetoothSocket!=null){
    		byte[] msgBuffer = stopControlText.getBytes();
    		try {
				outputStream.write(msgBuffer);
			} catch (IOException e) {
				e.printStackTrace();
			}
    		
	        }
		Log.i(TAG, "执行onDestroy");
		super.onDestroy();
	} 
    Runnable runnable=new Runnable() {
        
        @Override
        public void run() {
           
		  try {
		    Thread.sleep(5000);
		} catch (InterruptedException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
		  
		handler.sendEmptyMessage(0); 
		  
    	
        }
    };
    Handler handler=new Handler(){
	@Override
	public void handleMessage(Message msg) {
	    dialog2.dismiss();
	    Toast.makeText(RegisterActivity.this, "身份认证成功，开始测量。。。", 0).show(); 
	    startReceiveLongDataClicked();
	}
	
    };
}