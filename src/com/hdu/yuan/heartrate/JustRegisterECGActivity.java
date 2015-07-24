package com.hdu.yuan.heartrate;

import android.app.Activity;
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
import com.hdu.yuan.heartrate.object.*;

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
public class JustRegisterECGActivity extends Activity{
		private final static String TAG = "selection";
		private static final int CAMERA_REQUEST = 1888;
		private String url_chose = "http://115.28.0.158:9523/index.php";// http://192.168.161.102:80/index.php
		ECGApplication hegApplication;
		private int pointsPerSecond=250;
		public static int MAXVALUE = 1000;// 10s能够接收到的点数
		private int ARGCOUNT = 10;
		/*----------增加ECG-----------*/
		private int pulseInterval = 80;
		private int effLeft = 25;
		private int effRight = 35;
		// private int pulseInterval=16;
		// private int effLeft=5;
		// private int effRight=7;
//		private float lastValue = 0;
//		private float beforLastValue = 0;
		private float lastValuemb = 0;
		private float beforLastValuemb = 0;
//		private int numberRead = 0;
//		private int COUNT = 0;// 清零计数
		//滤波处理应用参数
//		private int size = 25;
//		private float[] values = new float[size];// 队列形式
//		private int cursor = 0;
		// 控件
		private Button startReceiveLongDataButton = null;
		private Button startRecordButton = null;
		private FrameLayout maskLayoutECG;
		private CheckBox cBox = null;
		/*----------增加ECG-----------*/
		// private FrameLayout maskLayoutecg;
		/*--------------------------*/
		private EditText nameEdit, ageEdit;
		private TextView xueyang, maibo, tiwen, timeText;
		ImageView takePhoto;
		Gallery gallery;
		// 资源
		Drawable manDrawable;
		Drawable womanDrawable;
		// 全局变量
		private boolean isMan = true;
		List<People> peoples;
		// 数据库
		DBManager dbManager;
		ImageAdapter imageAdapter;
		private Timer timer;
		// 蓝牙
		private BluetoothSocket bluetoothSocket = null;
		private OutputStream outputStream = null;
		private InputStream inputStream = null;
		private InputStream bufferinputStream = null;
		private ProgressBar progressBar = null;
		private BufferedReader bufferedReader;
		private SingleWaveDisplayEcg singleWaveDisplayecg=null;
//		private boolean canReadFlag = true;
		private boolean canReadFlag = false;
		private boolean isRecord = false;
		private boolean STOP = false;
		private boolean xyfinish = false;
		// 需要存进数据库的数据
		MeasureArg mArg;
		float[] datasOfMearsured = new float[MAXVALUE];
		private int currentCursorDatas = 0;
		private int currentCursorEcgDatas = 0;
		private int currentCursorArg = 0;
		private int currentPeopleId = 0;
		
		private float Newtw = 35.0f;
		private float Oldtw = 35.0f;
		private int Newxy = 95;
		private int Oldxy = 95;
		private int Newmb = 71;
		private int Oldmb = 71;
//		private float OnefloatEcgResult = 0;//全局变量存储心电当前值
		@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.justregisterecg);
			// 控件初始化 接收 记录
			startReceiveLongDataButton = (Button) findViewById(R.id.startReceiveecg);// 开始显示接收数据按钮
			startRecordButton = (Button) findViewById(R.id.startRecordECG);// 开始存储数据按钮
			progressBar = (ProgressBar) findViewById(R.id.progressBarECG);
			singleWaveDisplayecg=(SingleWaveDisplayEcg)this.findViewById(R.id.singlewavedisplayecg);

			maskLayoutECG = (FrameLayout) findViewById(R.id.maskPlayecg);// 波形显示框
			// takePhoto=(ImageView)findViewById(R.id.portrait);
			gallery = (Gallery) findViewById(R.id.imagesGalleryOnlyECG);// 头像显示位置
			gallery.setSpacing(((ECGApplication) getApplication()).getScreenWidth() / 2);
			Log.d(TAG, "" + ((ECGApplication) getApplication()).getScreenWidth());
			xueyang = (TextView) findViewById(R.id.xueyangjustecg);
			maibo = (TextView) findViewById(R.id.maibojustecg);
			tiwen = (TextView) findViewById(R.id.tiwenjustecg);
			timeText = (TextView) findViewById(R.id.timeRegister);
			peoples = new ArrayList<People>();
			imageAdapter = new ImageAdapter(peoples, this);
			gallery.setAdapter(imageAdapter);
			gallery.setOnItemClickListener(onItemClickListener);

			// 绑定checkbox
			cBox = (CheckBox) findViewById(R.id.cbECG);
			// 资源初始化
			manDrawable = getResources().getDrawable(R.drawable.man);
			womanDrawable = getResources().getDrawable(R.drawable.woman);
			// 数据库初始化
			dbManager = new DBManager(this);
			mArg = new MeasureArg();
			hegApplication = (ECGApplication) getApplication();
			bluetoothSocket=hegApplication.getBluetoothSocket();
			if (bluetoothSocket != null) {
			} else {
				Toast.makeText(this, "请返回蓝牙选项卡，完成与设备的蓝牙配对", Toast.LENGTH_SHORT).show();
			}
			timer=new Timer();
			Thread thread = new Thread(new TimeThread());
			thread.start();
		}

		class TimeThread implements Runnable {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				while (!STOP) {
					// "yyyy-MM-dd HH:mm:ss"
					// 24小时制时间格式，"yyyy-MM-dd hh:mm:ss"12小时制时间格式
					SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					String date = sDateFormat.format(new java.util.Date());
					Message msg = new Message();
					msg.what = 5;
					msg.obj = date;
					textHandler.sendMessage(msg);
					try {
						Thread.sleep(100);
					} catch (Exception e) {
					}
				}
			}
		}
		/*****
		 * 控件事件区
		 */
		@SuppressLint("NewApi")
		public void hideMaskAndRececg(View v) {
			canReadFlag = !canReadFlag;
			if (canReadFlag) {
				Log.d(TAG, "停止");
				// 设置视图的透明度。这是一个值从0到1 ，其中0表示该视图是完全透明的，1表示该视图是完全不透明的。
				maskLayoutECG.setAlpha(0);
				startReceiveLongDataClicked();
			} else {
				Log.d(TAG, "开始");
				maskLayoutECG.setAlpha(0.8f);
			}
		}

		public void startReceiveLongDataClicked() {
			bluetoothSocket = hegApplication.getBluetoothSocket();
			progressBar.setVisibility(View.VISIBLE);
			progressBar.setMax(MAXVALUE);
			String startControlText = "y";
			if (bluetoothSocket != null) {
				// 点击存储后就不能响应再次点击
				startReceiveLongDataButton.setClickable(false);
				try {
					outputStream = bluetoothSocket.getOutputStream();
					inputStream = bluetoothSocket.getInputStream();
				} catch (IOException e) {
					e.printStackTrace();
				}
				byte[] msgBuffer = startControlText.getBytes();
				try {
					for (int i = 0; i < 3; i++) {
					outputStream.write(msgBuffer);// 使用输出流输出3个字符y给下位机
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				if(timer==null)
	    			timer=new Timer();
	    		timer.schedule(ecgreadTask, 100, 1000/pointsPerSecond);
//				Thread thread = new Thread(new ReadThread());//此线程检测数据并进行存储
//				thread.start();
			} else {
				Toast.makeText(this, "没有建立与设备的连接，无法发送数据x，无法开始AD转换后数据的接收",Toast.LENGTH_SHORT).show();
			}
		}
		private TimerTask ecgreadTask=new TimerTask() {
	    	float lastValue=0;
	    	float beforLastValue=0;
//	    	int numberRead=0;
	    	int COUNT=0;//清零计数
	    	int size=25;
//	    	int checkCNT=0;
	    	float[] values=new float[size];//队列形式
	    	int cursor=0;

			@Override
			public void run() {
				// TODO Auto-generated method stub
				Log.i("ECGreadTask_run", "readThread线程启动");				
	            if (canReadFlag!=false) {
	            	COUNT++;
	            	bufferinputStream = inputStream;//将需要测量的血氧脉搏体温信号放入缓存中
					bufferedReader = new BufferedReader(new InputStreamReader(bufferinputStream));
//					readOneADresult(bufferedReader);//此方法是为了读取出血氧脉搏体温信号
	            	int readOneIntResult=readOneADresult(inputStream);

	            	float readOneFloatResult=(float)(readOneIntResult*18.3f/128000);
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
	            	singleWaveDisplayecg.addNewDataToDraw(outPut);
	            	Log.e("ECGoutPut", String.valueOf(outPut));
	            	if (isRecord)// 判断是否要记录数据，如果需要记录的话，会在本线程里面把数据信息添加到数据库中
					{
						datasOfMearsured[currentCursorDatas] = outPut;
						currentCursorDatas++;
						progressBar.setProgress(currentCursorDatas);
						if (currentCursorDatas == MAXVALUE) {
							stopTimer();
							//timer.cancel();
							String stopControlText = "w";
							if (bluetoothSocket != null) {
								byte[] msgBuffer = stopControlText.getBytes();
								try {
									outputStream.write(msgBuffer);
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
							// 恢复记录按钮
							startRecordButton.setClickable(true);
							canReadFlag = false;
							isRecord = false;
							currentCursorDatas = 0;
							currentCursorArg = 0;

							History history = new History();
							// 顺带清空数据
							StringBuilder ECGstringBuilder = new StringBuilder();
							for (int i = 0; i < datasOfMearsured.length - 1; i++) {
								ECGstringBuilder.append(String.valueOf(datasOfMearsured[i]) + ",");// 以“，”隔开脉搏波形值
								datasOfMearsured[i] = 0;
							}
							ECGstringBuilder.append(String.valueOf(datasOfMearsured[datasOfMearsured.length - 1]));
							datasOfMearsured[datasOfMearsured.length - 1] = 0;
							history.setEcgData(ECGstringBuilder.toString());
							int position = gallery.getSelectedItemPosition();
							history.setPeopleId(imageAdapter.getList().get(position).getId());
							history.setMeasureArg(mArg);
							SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
							String date = sDateFormat.format(new java.util.Date());
							history.setTimeOfRecord(date+"ecg");

							// 把数据按照格式放在数据库中，
							// db.execSQL("INSERT INTO history VALUES(null,?,?,?,?,?,?)",
							// new Object[]{history.getPeopleId(),
							// history.getData(),history.getMeasureArg().xueyang,history.getMeasureArg().tiwen,history.getMeasureArg().maibo,history.getTimeOfRecord()});

							if (cBox.isChecked())// 存储到本地
							{
								dbManager.addHistory(history);
							} else {// 存储在服务器
								List<NameValuePair> ecgparams = new ArrayList<NameValuePair>();
								ecgparams.add(new BasicNameValuePair("action","insertXD"));
								ecgparams.add(new BasicNameValuePair("userId",String.valueOf(history.getPeopleId())));// 2015-03-19
								ecgparams.add(new BasicNameValuePair("xd_data",history.getEcgData()));// 2015-03-19
								ecgparams.add(new BasicNameValuePair("xd_time",history.getTimeOfRecord()));// 2015-03-19
								Log.d("xd_time", history.getTimeOfRecord());
								ecgparams.add(new BasicNameValuePair("xy", history.getMeasureArg().xueyang));// 2015-03-19
								ecgparams.add(new BasicNameValuePair("mb", history.getMeasureArg().maibo));// 2015-03-19
								ecgparams.add(new BasicNameValuePair("tw", history.getMeasureArg().tiwen));// 2015-03-19
								String ecgbuilder = HttpUtil.getString(url_chose,ecgparams);
								Log.d("justecgstring", ecgbuilder);
								// Toast.makeText(JustRegisterECGActivity.this,ecgbuilder, Toast.LENGTH_SHORT).show();
							}
							Message message = new Message();
							message.what = 2;
							textHandler.sendMessage(message);
						}
					}
//	            	if(isRecord)
//	            	{
//	            		if(checkCNT<MAXVALUE)
//	            			analyse[checkCNT++]=outPut;
//		            	numberRead++;
//	            	}
	            	
//	        		if(numberRead!=MAXVALUE)
//	        		{
//	        			Message message=new Message();
//	        			message.arg1=numberRead;
//	        			writeHandler.sendMessage(message);
//	        			
//	        		}
//	        		else {
//	        			timer.cancel();
//	        			Message message=new Message();
//	        			message.arg1=numberRead;
//	        			writeHandler.sendMessage(message);
//					}
	            }
			}
		}; 
		private void stopTimer(){  
	        if (timer != null) {  
	        	timer.cancel();  
	        	timer = null;  
	        }  
	        if (ecgreadTask != null) {  
	        	ecgreadTask.cancel();  
	        	ecgreadTask = null;  
	        }     
	    }  
		@SuppressLint("NewApi")
		public void startRecordECG(View v) {
			isRecord = !isRecord;
			if (isRecord) {
				startRecordButton.setText("存储中.....");
				startRecordButton.setClickable(false);
			} else {
				startRecordButton.setText("开始存储");
			}
		}

		public void sexSelector(View v) {
			isMan = !isMan;
			if (isMan) {
				v.setBackground(manDrawable);
			} else {
				v.setBackground(womanDrawable);
			}
		}

		/***
			 * 
			 */
		@Override
		public boolean onKeyDown(int keyCode, KeyEvent event) {
			if (keyCode == KeyEvent.KEYCODE_BACK) {
//				stopTimer();
				Intent intent = new Intent(JustRegisterECGActivity.this,ChooseRegisterModelActivity.class);
				intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
				Window w = RegisterModelActivityGroup.registergroup.getLocalActivityManager().startActivity("LoginFirst2",intent);
				View view = w.getDecorView();
				RegisterModelActivityGroup.registergroup.setContentView(view);
				return true;
			}
			return super.onKeyDown(keyCode, event);
		}

		Handler textHandler = new Handler() {
			@SuppressLint("NewApi")
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				if (msg.what == 1) {
					MeasureArg measureArg = (MeasureArg) msg.obj;
					xueyang.setText(measureArg.xueyang + "%");
					maibo.setText(measureArg.maibo + "次/分");
					tiwen.setText(measureArg.tiwen + "℃");
					currentCursorArg++;

					mArg.maibo = measureArg.maibo;
					mArg.tiwen = measureArg.tiwen;
					mArg.xueyang = measureArg.xueyang;
				}
				else if (msg.what == 2) {
					say("存储成功");
					progressBar.setProgress(0);
					maskLayoutECG.setAlpha(0.8f);
					startRecordButton.setText("开始存储");
					// 数据存储完成，点击存储按钮可以使用
					startReceiveLongDataButton.setClickable(true);
				} else if (msg.what == 5) {
					readOneADresult(bufferedReader);//这个地方如果放在TimerTask中会造成UI阻塞
					timeText.setText(msg.obj.toString());
				}
			}

		};

//		class ReadThread implements Runnable {
//			@Override
//			public void run() {
//				// 把蓝牙输入的数据inputStream放到读数据缓存区
//				bufferinputStream = inputStream;//将需要测量的血氧脉搏体温信号放入缓存中
//				bufferedReader = new BufferedReader(new InputStreamReader(bufferinputStream));
//				// TODO Auto-generated method stub
//				while (canReadFlag == true) {
//					readOneADresult(bufferedReader);//此方法是为了读取出血氧脉搏体温信号
//					int readOneIntResult=readOneADresult(inputStream);//此方法是为了读取出ECG信号
//	            	float dataecg=DealOneEcgdata(readOneIntResult);//心电信号处理
//					if (dataecg != -1) {
//						// readOneADresult（）里面有温度、脉搏、血氧值得显示，所以此处只要添加脉搏波形值就行了
//						singleWaveDisplayecg.addNewDataToDraw(dataecg);
//						if (isRecord)// 判断是否要记录数据，如果需要记录的话，会在本线程里面把数据信息添加到数据库中
//						{
//							datasOfMearsured[currentCursorDatas] = dataecg;
//							currentCursorDatas++;
//							progressBar.setProgress(currentCursorDatas);
//							if (currentCursorDatas == MAXVALUE) {
//								String stopControlText = "w";
//								if (bluetoothSocket != null) {
//									byte[] msgBuffer = stopControlText.getBytes();
//									try {
//										outputStream.write(msgBuffer);
//									} catch (IOException e) {
//										e.printStackTrace();
//									}
//								}
//								// 恢复记录按钮
//								startRecordButton.setClickable(true);
//								canReadFlag = false;
//								isRecord = false;
//								currentCursorDatas = 0;
//								currentCursorArg = 0;
//
//								History history = new History();
//								// 顺带清空数据
//								StringBuilder ECGstringBuilder = new StringBuilder();
//								for (int i = 0; i < datasOfMearsured.length - 1; i++) {
//									ECGstringBuilder.append(String.valueOf(datasOfMearsured[i]) + ",");// 以“，”隔开脉搏波形值
//									datasOfMearsured[i] = 0;
//								}
//								ECGstringBuilder.append(String.valueOf(datasOfMearsured[datasOfMearsured.length - 1]));
//								datasOfMearsured[datasOfMearsured.length - 1] = 0;
//								history.setEcgData(ECGstringBuilder.toString());
//								int position = gallery.getSelectedItemPosition();
//								history.setPeopleId(imageAdapter.getList().get(position).getId());
//								history.setMeasureArg(mArg);
//								SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//								String date = sDateFormat.format(new java.util.Date());
//								history.setTimeOfRecord(date);
//
//								// 把数据按照格式放在数据库中，
//								// db.execSQL("INSERT INTO history VALUES(null,?,?,?,?,?,?)",
//								// new Object[]{history.getPeopleId(),
//								// history.getData(),history.getMeasureArg().xueyang,history.getMeasureArg().tiwen,history.getMeasureArg().maibo,history.getTimeOfRecord()});
//
//								if (cBox.isChecked())// 存储到本地
//								{
//									dbManager.addHistory(history);
//								} else {// 存储在服务器
//									List<NameValuePair> ecgparams = new ArrayList<NameValuePair>();
//									ecgparams.add(new BasicNameValuePair("action","insertXD"));
//									ecgparams.add(new BasicNameValuePair("userId",String.valueOf(history.getPeopleId())));// 2015-03-19
//									ecgparams.add(new BasicNameValuePair("xd_data",history.getEcgData()));// 2015-03-19
//									ecgparams.add(new BasicNameValuePair("xd_time",history.getTimeOfRecord()));// 2015-03-19
//									ecgparams.add(new BasicNameValuePair("xy", history.getMeasureArg().xueyang));// 2015-03-19
//									ecgparams.add(new BasicNameValuePair("mb", history.getMeasureArg().maibo));// 2015-03-19
//									ecgparams.add(new BasicNameValuePair("tw", history.getMeasureArg().tiwen));// 2015-03-19
//									String ecgbuilder = HttpUtil.getString(url_chose,ecgparams);
//									Log.d(TAG, ecgbuilder);
//									// Toast.makeText(JustRegisterECGActivity.this,ecgbuilder, Toast.LENGTH_SHORT).show();
//								}
//								Message message = new Message();
//								message.what = 2;
//								textHandler.sendMessage(message);
//							}
//						}
//					}
//				}
//			}
//		}

//	public float DealOneEcgdata(float floatEcgResult) {
//		Log.e("COUNT", String.valueOf(COUNT));
//		Log.e("cursor", String.valueOf(cursor));
//		COUNT++;
//		float readOneFloatResult = (float) (floatEcgResult * 18.3f/128000);
//		Log.d("resultecg", String.valueOf(readOneFloatResult));
//		if (readOneFloatResult < -0.8&& (readOneFloatResult - lastValue < -0.8 || readOneFloatResult - beforLastValue < -0.8)) {
//			readOneFloatResult = lastValue;
//		} else if (readOneFloatResult > 0.6&& readOneFloatResult < 2.0&& (readOneFloatResult - lastValue > 0.3 || readOneFloatResult - beforLastValue > 0.3)) {
//			readOneFloatResult += 0.6;
//			COUNT = 0;
//		} else if (readOneFloatResult > 2.0) {
//			readOneFloatResult = 1.8f;
//		}
//		// T波滤波
//		if (COUNT > 5 && COUNT < effRight) {
//			readOneFloatResult = (readOneFloatResult + lastValue + beforLastValue) / 3;
//		}
//		if (COUNT > effRight && COUNT < pulseInterval - effLeft)// 80-35=55
//			readOneFloatResult = 0;
//		// originalTrainSignal[numberRead]=readOneFloatResult;
//		beforLastValue = lastValue;
//		lastValue = readOneFloatResult;
//		// float readOneFloatResult=filter(readOneFloatResultRaw);
//		// Log.d(TAG,"value="+readOneFloatResult);
//		// 放进队列
//		float outPut = 0;
//		if (cursor < size) {
//			values[cursor] = readOneFloatResult;
//			cursor++;
//		} else {
//			// 移位加中值滤波
//			outPut = values[0];
//			for (int i = 0; i < size - 1; i++) {
//				if (COUNT != 0)
//					values[i] = values[i + 1];
//				else {
//					if (i < size - 5) {
//						values[i] = (values[i + 1] + values[i + 2] + values[i + 3]) / 3;
//					}
//				}
//			}
//			values[size - 1] = readOneFloatResult;
//			cursor++;
//		}
//		Log.e("outPut", String.valueOf(outPut));
//		return outPut;
//	}
	private static int readOneADresult(InputStream inputStrm1){
 		byte[] buffer = new byte[256];
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
			    	}
 				}
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}catch (Exception e){
 		}
 		buffer=null;
 		Log.d("ECGfloatResult", String.valueOf(floatResult));
 		return (int)floatResult;
 	}
	// CC95DD100EE3439FF 第一个是血氧，第二个是脉搏，第三个是体温
		public void readOneADresult(BufferedReader in) {
			// byte[] buffer = new byte[512];
			int bytes = 0;
			String string = "";
			float result = -1;
			try {
				try {
					string = in.readLine();// 以行为分界，进行数据解析
				} catch (IOException e) {
					Log.d(TAG, e.getMessage());
				}
				bytes = string.length();
				if (bytes > 0)// 表明接收到数据
				{
					int indexCC = string.indexOf("CC");
					int indexDD = string.indexOf("DD");
					int indexEE = string.indexOf("EE");
					int indexFF = string.indexOf("FF");
					if(indexFF>indexCC && indexCC!=-1 &&indexDD!=-1 && indexEE!=-1)
						{
							Log.d(TAG,string);
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
							Log.d("ECG中血氧", measureArg.xueyang+"%");
							String mString=string.substring(indexDD+2, indexEE);
							Newmb = Integer.parseInt(mString);
							if(Newmb>110||Newmb<50){
								Newmb = Oldmb;
							}
							else{
								Oldmb = Newmb;
							}
							measureArg.maibo=String.valueOf(Newmb);
							Log.d("ECG中脉搏", measureArg.maibo+"次/min");
							String tString=string.substring(indexEE+2, indexFF);
							Newtw = Integer.parseInt(tString)*1.0f/100;
							if(Newtw>40.0||Newtw<32.0){
								Newtw = Oldtw;
							}
							else{
								Oldtw = Newtw;
							}
							measureArg.tiwen=String.valueOf(Newtw);
							Log.d("ECG中体温", measureArg.tiwen+"C");
							
							Message msg=new Message();
							msg.what=1;
							msg.obj=measureArg;
							textHandler.sendMessage(msg);
						}
				}
			}catch (Exception e) {
			}
		}
		
		private void say(String str) {
			Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
		}
		OnItemClickListener onItemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,long id) {
			currentPeopleId = imageAdapter.getList().get(position).getId();
			Toast.makeText(JustRegisterECGActivity.this,imageAdapter.getList().get(position).getName(),Toast.LENGTH_SHORT).show();
			}
		};
		@Override
		protected void onResume() {
			// TODO Auto-generated method stub
			super.onResume();
			// Log.d(TAG, "ecg record onresume");
			imageAdapter.setList(dbManager.queryListPeople());
			// Log.d(TAG,imageAdapter.getCount()+"个");
			imageAdapter.notifyDataSetChanged();
		}
		@Override
		protected void onPause() {
			String stopControlText = "w";
			if (bluetoothSocket != null) {
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
			canReadFlag = false;
			STOP = true;
			String stopControlText = "w";
			if (bluetoothSocket != null) {
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
}
