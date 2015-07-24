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

public class JustRegisterSaO2Activity extends Activity {
	private final static String TAG = "selection";
	private static final int CAMERA_REQUEST = 1888;
	private String url_chose = "http://115.28.0.158:9523/index.php";// http://192.168.161.102:80/index.php
	ECGApplication hegApplication;
	// private int pointsPerSecond=100;
	public static int MAXVALUE = 1000;// 10s能够接收到的点数
	private int ARGCOUNT = 10;
	/*----------增加ECG-----------*/
	private int pulseInterval = 80;
	private int effLeft = 25;
	private int effRight = 35;
	// private int pulseInterval=16;
	// private int effLeft=5;
	// private int effRight=7;
	private float lastValue = 0;
	private float beforLastValue = 0;
	private float lastValuemb = 0;
	private float beforLastValuemb = 0;
	private int numberRead = 0;
	private int COUNT = 0;// 清零计数
	//滤波处理应用参数
	private int size = 25;
	private float[] values = new float[size];// 队列形式
	private int cursor = 0;
	// 控件
	private Button startReceiveLongDataButton = null;
	private Button startRecordButton = null;
	private FrameLayout maskLayout;
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
	// 蓝牙
	private BluetoothSocket bluetoothSocket = null;
	private OutputStream outputStream = null;
	private InputStream inputStream = null;
	private ProgressBar progressBar = null;
	private BufferedReader bufferedReader;
	private SingleWaveDisplay singleWaveDisplay = null;
//	private boolean canReadFlag = true;
	private boolean canReadFlag = false;
	private boolean isRecord = false;
	private boolean STOP = false;
	// 需要存进数据库的数据
	MeasureArg mArg;
	float[] datasOfMearsured = new float[MAXVALUE];
	// float[] EcgDatasOfMearsured=new float[MAXVALUE];
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
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.justregistersao2);
		// 控件初始化 接收 记录
		startReceiveLongDataButton = (Button) findViewById(R.id.startReceiveSaO2);// 开始显示接收数据按钮
		startRecordButton = (Button) findViewById(R.id.startRecordSaO2);// 开始存储数据按钮
		progressBar = (ProgressBar) findViewById(R.id.progressBarSaO2);
		singleWaveDisplay = (SingleWaveDisplay) this.findViewById(R.id.singlewavedisplayOnlySaO2);
		maskLayout = (FrameLayout) findViewById(R.id.maskPlayOnlySaO2);// 波形显示框
		gallery = (Gallery) findViewById(R.id.imagesGalleryOnlySaO2);// 头像显示位置
		gallery.setSpacing(((ECGApplication) getApplication()).getScreenWidth() / 2);
		Log.d(TAG, "" + ((ECGApplication) getApplication()).getScreenWidth());
		xueyang = (TextView) findViewById(R.id.xueyang);
		maibo = (TextView) findViewById(R.id.maibo);
		tiwen = (TextView) findViewById(R.id.tiwen);
		timeText = (TextView) findViewById(R.id.timeRegister);
		peoples = new ArrayList<People>();
		imageAdapter = new ImageAdapter(peoples,this);
		gallery.setAdapter(imageAdapter);
		gallery.setOnItemClickListener(onItemClickListener);

		// 绑定checkbox
		cBox = (CheckBox) findViewById(R.id.cbSaO2);
		// 资源初始化
		manDrawable = getResources().getDrawable(R.drawable.man);
		womanDrawable = getResources().getDrawable(R.drawable.woman);
		// 数据库初始化
		dbManager = new DBManager(this);
		mArg = new MeasureArg();
		hegApplication = (ECGApplication) getApplication();
		if (bluetoothSocket != null) {
		} else {
			Toast.makeText(this, "请返回蓝牙选项卡，完成与设备的蓝牙配对", Toast.LENGTH_SHORT).show();
		}
		Thread thread = new Thread(new TimeThread());
		thread.start();
	}

	class TimeThread implements Runnable {
		@Override
		public void run() {
			while (!STOP) {
				// "yyyy-MM-dd HH:mm:ss"24小时制时间格式，"yyyy-MM-dd hh:mm:ss"12小时制时间格式
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
	public void hideMaskAndRecSaO2(View v) {
		canReadFlag = !canReadFlag;
		if (canReadFlag) {
			Log.d(TAG, "停止");
			// 设置视图的透明度。这是一个值从0到1 ，其中0表示该视图是完全透明的，1表示该视图是完全不透明的。
			maskLayout.setAlpha(0);
			startReceiveLongDataClicked();
		} else {
			Log.d(TAG, "开始");
			maskLayout.setAlpha(0.8f);
		}
	}

	public void startReceiveLongDataClicked() {
		bluetoothSocket = hegApplication.getBluetoothSocket();
		progressBar.setVisibility(View.VISIBLE);
		progressBar.setMax(MAXVALUE);
		String startControlText = "x";
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
					outputStream.write(msgBuffer);// 使用输出流输出一个字符x给下位机
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

			Thread thread = new Thread(new ReadThread());
			thread.start();
		} else {
			Toast.makeText(this, "没有建立与设备的连接，无法发送数据x，无法开始AD转换后数据的接收",Toast.LENGTH_SHORT).show();
		}
	}

	@SuppressLint("NewApi")
	public void startRecordSaO2(View v) {
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
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Intent intent = new Intent(JustRegisterSaO2Activity.this,ChooseRegisterModelActivity.class);
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
				maskLayout.setAlpha(0.8f);
				startRecordButton.setText("开始存储");
				// 数据存储完成，点击存储按钮可以使用
				startReceiveLongDataButton.setClickable(true);

			} else if (msg.what == 5) {
				timeText.setText(msg.obj.toString());
			}
		}

	};

	class ReadThread implements Runnable {
		@Override
		public void run() {
			// 把蓝牙输入的数据inputStream放到读数据缓存区
			bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
			// TODO Auto-generated method stub
			while (canReadFlag == true) {
				float data = readOneADresult(bufferedReader);
				if (data != -1) {
					Log.i("血氧显示值：", data+"");
					singleWaveDisplay.addNewDataToDraw(data);
					if (isRecord)// 判断是否要记录数据，如果需要记录的话，会在本线程里面把数据信息添加到数据库中
					{
						datasOfMearsured[currentCursorDatas] = data;
						currentCursorDatas++;
						progressBar.setProgress(currentCursorDatas);
						if (currentCursorDatas == MAXVALUE) {
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
							StringBuilder stringBuilder = new StringBuilder();
							for (int i = 0; i < datasOfMearsured.length - 1; i++) {
								stringBuilder.append(String.valueOf(datasOfMearsured[i]) + ",");// 以“，”隔开脉搏波形值
								datasOfMearsured[i] = 0;
							}
							stringBuilder.append(String.valueOf(datasOfMearsured[datasOfMearsured.length - 1]));
							datasOfMearsured[datasOfMearsured.length - 1] = 0;
							history.setData(stringBuilder.toString());
							int position = gallery.getSelectedItemPosition();
							history.setPeopleId(imageAdapter.getList().get(position).getId());
							history.setMeasureArg(mArg);
							SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
							String date = sDateFormat.format(new java.util.Date());
							history.setTimeOfRecord(date);

							// 把数据按照格式放在数据库中，
							// db.execSQL("INSERT INTO history VALUES(null,?,?,?,?,?,?)",
							// new Object[]{history.getPeopleId(),
							// history.getData(),history.getMeasureArg().xueyang,history.getMeasureArg().tiwen,history.getMeasureArg().maibo,history.getTimeOfRecord()});

							if (cBox.isChecked())// 存储到本地
							{
								dbManager.addHistory(history);
							} else {// 存储在服务器
								List<NameValuePair> params = new ArrayList<NameValuePair>();
								params.add(new BasicNameValuePair("action","insertXY"));
								params.add(new BasicNameValuePair("userId",String.valueOf(history.getPeopleId())));// 2015-03-19 16:47:40
								params.add(new BasicNameValuePair("xy_data",history.getData()));// 2015-03-19 16:47:40
								params.add(new BasicNameValuePair("xy_time",history.getTimeOfRecord()));// 2015-03-19 16:47:40
								params.add(new BasicNameValuePair("xy", history.getMeasureArg().xueyang));// 2015-03-19 16:47:40
								params.add(new BasicNameValuePair("mb", history.getMeasureArg().maibo));// 2015-03-19 16:47:40
								params.add(new BasicNameValuePair("tw", history.getMeasureArg().tiwen));// 2015-03-19 16:47:40
								String builder = HttpUtil.getString(url_chose,params);
								Log.d(TAG, builder);
							}
							Message message = new Message();
							message.what = 2;
							textHandler.sendMessage(message);
						}
					}
				}
			}
		}
	}

	// CC95DD100EE3439FF 第一个是血氧，第二个是脉搏，第三个是体温
	public float readOneADresult(BufferedReader in) {
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
				// Log.d(TAG, string);
				int indexStart = string.indexOf("AA");
				int indexEnd = string.indexOf("BB");
				if (indexEnd > indexStart && indexStart != -1) {
					string = string.substring(indexStart + 2, indexEnd);// 提取AA……BB之间的字符
					Log.i("血氧读取值：", string);
					try {
						result = Float.parseFloat(string);
						result = result * 1.0f / 500;
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				int indexCC = string.indexOf("CC");
				int indexDD = string.indexOf("DD");
				int indexEE = string.indexOf("EE");
				int indexFF = string.indexOf("FF");
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
						Log.d("SaO2中血氧", measureArg.xueyang+"%");
						String mString=string.substring(indexDD+2, indexEE);
						Newmb = Integer.parseInt(mString);
						if(Newmb>110||Newmb<50){
							Newmb = Oldmb;
						}
						else{
							Oldmb = Newmb;
						}
						measureArg.maibo=String.valueOf(Newmb);
						Log.d("SaO2中脉搏", measureArg.maibo+"次/min");
						String tString=string.substring(indexEE+2, indexFF);
						Newtw = Integer.parseInt(tString)*1.0f/100;
						if(Newtw>40.0||Newtw<32.0){
							Newtw = Oldtw;
						}
						else{
							Oldtw = Newtw;
						}
						measureArg.tiwen=String.valueOf(Newtw);
						Log.d("SaO2中体温", measureArg.tiwen+"C");
						
						Message msg=new Message();
						msg.what=1;
						msg.obj=measureArg;
						textHandler.sendMessage(msg);
					}
			}
		}catch (Exception e) {
		}
		string = null;
		if ((result > 0.0f) && (result < 2.0f)) {
			beforLastValuemb = lastValuemb;
			lastValuemb = result;
			if (lastValuemb - beforLastValuemb > 0.8f)// 处理突然增高(脉搏波：1.2f显示的话在2.4f上；心电则原值显示)
				result = beforLastValuemb + 0.1f;
			if (beforLastValuemb - lastValuemb > 0.8f)// 处理突然降低
				result = beforLastValuemb - 0.1f;
		} else
			return lastValuemb;
		return result;
	}
	private void say(String str) {
		Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
	}

	OnItemClickListener onItemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// TODO Auto-generated method stub
			currentPeopleId = imageAdapter.getList().get(position).getId();
			Toast.makeText(JustRegisterSaO2Activity.this,imageAdapter.getList().get(position).getName(),Toast.LENGTH_SHORT).show();
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
