package com.hdu.yuan.heartrate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import com.hdu.yuan.heartrate.object.MeasureArg;
import com.hdu.yuan.heartrate.object.People;
import com.hdu.yuan.heartrate.object.Point;
import com.hdu.yuan.heartrate.util.DBManager;
import com.hdu.yuan.heartrate.util.FileService;
import com.hdu.yuan.heartrate.view.SingleWaveDisplay;
import com.hdu.yuan.heartrate.view.SingleWaveDisplayEcg;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.provider.MediaStore;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

public class RegisterNewUserActivity extends Activity {
	private static final int CAMERA_REQUEST = 1888;
	private ECGApplication hegApplication;
	private int pointsPerSecond=250;
	private int MAXVALUE=3000;//能够接收到的点数
	private int pulseInterval=80;
	private int effLeft=25;
	private int effRight=35;
	private boolean deleteThreadAllowedFlag=true;
	//控件
	private Button startReceiveLongDataButton=null;
	private Button startRecordButton=null;
	private Button sexSelectorButton=null;
	private Button saveButton=null;
	private FrameLayout maskLayout;
	private EditText nameEdit,ageEdit;
	ImageView takePhoto;
	
	//资源
	Drawable manDrawable;
	Drawable womanDrawable;
	//全局变量
	private String name;
	private String gender;
	private String age;
	private String phoneNumber;
	private String originalSignalFilePath;
	private String CodeBookFilePath;
	private boolean isMan=true;
	private File originalSignalFile;
	private File CodeBookFile;
	
	private String resultString;
	private int size=0;
	private Timer timer;
	
	private BufferedReader bufferedReader;
	//private double[][] outputLBGCode;
	private float[] analyse=new float[MAXVALUE];
	//数据库
	DBManager dbManager;
	//蓝牙
	private BluetoothSocket bluetoothSocket=null;
	private OutputStream outputStream = null; 
	private InputStream inputStream = null;
	private ProgressBar progressBar=null;
	private SingleWaveDisplayEcg singleWaveDisplay=null;
	//private double[] dataToDraw=new double[3000];
	private boolean canReadFlag=true;
	private boolean isRecord=false;
	private static final String TAG = "RegisterThird";
	FileService fileService;
;
	
	private int checkSum=36;
	private int checkLeft=10;
	private int checkRight=26;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.registernewuser);
		//控件初始化
		startReceiveLongDataButton=(Button)findViewById(R.id.startReceiveNewUser);
		startRecordButton=(Button)findViewById(R.id.startRecordNewUser);
		sexSelectorButton=(Button)findViewById(R.id.sexSelectorNewUser);
		saveButton=(Button)findViewById(R.id.saveInfoNewUser);
		progressBar = (ProgressBar)findViewById(R.id.progressBarNewUser);
		singleWaveDisplay=(SingleWaveDisplayEcg)this.findViewById(R.id.singlewavedisplayNewUser);
		singleWaveDisplay.setOnClickListener(maskListenr);
		maskLayout=(FrameLayout)findViewById(R.id.maskPlayNewUser);
		takePhoto=(ImageView)findViewById(R.id.portraitNewUser);
		nameEdit=(EditText)findViewById(R.id.nameEditNewUser);
		ageEdit=(EditText)findViewById(R.id.ageEditNewUser);
		//资源初始化
		manDrawable=getResources().getDrawable(R.drawable.man);
		womanDrawable=getResources().getDrawable(R.drawable.woman);
		//数据库初始化
		dbManager=new DBManager(this);
		
		//存放临时采集文件，估计是采集后点击保存码本的时候删除
        fileService=new FileService(getApplicationContext());
        File tempFile=new File(Environment.getExternalStorageDirectory(),"tempkk.txt");
        if(tempFile.exists()){
        	tempFile.delete();
        }
        
        File tempCodeBookFile=new File(Environment.getExternalStorageDirectory(),"tempCodeBook.txt");
        if(tempCodeBookFile.exists()){
        	tempCodeBookFile.delete();
        }
        
		
		hegApplication=(ECGApplication)getApplication();
		bluetoothSocket=hegApplication.getBluetoothSocket();
		
		if(bluetoothSocket==null){
			Toast.makeText(this, "请返回蓝牙选项卡，完成与设备的蓝牙配对", Toast.LENGTH_SHORT).show();
		}
		
		File originalSignalDir=new File(Environment.getExternalStorageDirectory()+File.separator+"HeartSignal");
		if(!originalSignalDir.exists()){
			originalSignalDir.mkdirs();
		}
		File CodeBookDir=new File(Environment.getExternalStorageDirectory()+File.separator+"HeartCodeBook");
		if(!CodeBookDir.exists()){
			CodeBookDir.mkdirs();
		}
		
		originalSignalFile=new File(Environment.getExternalStorageDirectory()+File.separator+"HeartSignal","心音"+name+"数据.txt");
		CodeBookFile=new File(Environment.getExternalStorageDirectory()+File.separator+"HeartCodeBook","码本"+name+".txt");
		originalSignalFilePath=originalSignalFile.getAbsolutePath();
		CodeBookFilePath=CodeBookFile.getAbsolutePath();
		
		this.deleteThreadAllowedFlag=true;
//   	 	String startControlText="y";
//			if(bluetoothSocket!=null){
//				
//				try {
//					outputStream=bluetoothSocket.getOutputStream();
//					inputStream=bluetoothSocket.getInputStream();
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//	    		byte[] msgBuffer = startControlText.getBytes();
//	    		try {
//					outputStream.write(msgBuffer);
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//			}
		timer=new Timer();
	}
	/*****
	 * 控件事件区
	 */
	public void hideMaskAndRecNewUser(View v)
	{
		maskLayout.setVisibility(View.GONE);
		startReceiveLongDataClicked();
	}
	public void startReceiveLongDataClicked(){
		this.deleteThreadAllowedFlag=false;
		bluetoothSocket=hegApplication.getBluetoothSocket();
		
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setMax(MAXVALUE);
        
			String startControlText="y";
			if(bluetoothSocket!=null){
				startReceiveLongDataButton.setClickable(false);
				try {
					outputStream=bluetoothSocket.getOutputStream();
					inputStream=bluetoothSocket.getInputStream();
				} catch (IOException e) {
					e.printStackTrace();
				}
	    		byte[] msgBuffer = startControlText.getBytes();
	    		try {for (int i = 0; i < 3; i++) {
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
	public void saveInfo(View v)
	{
		if(resultString==null)
		{
			Toast.makeText(RegisterNewUserActivity.this, "结果为空，请确认", 2);
			return;
		}
		String name=nameEdit.getText().toString();
		String age =ageEdit.getText().toString();
		String sex=isMan?"男":"女";
		String imagePath=FileService.saveDrawableToSDCard(((BitmapDrawable)takePhoto.getBackground()).getBitmap());
		People people=new People();
		people.setName(name);
		people.setAge(age);
		people.setSex(sex);
		people.setImagePath(imagePath);
		people.setIdentifydata(resultString);//增加了识别码本
		people.setSize(size);
		dbManager.addPeople(people);
		try {
			fileService.saveToSDCard(name+".txt", resultString);
			Toast.makeText(RegisterNewUserActivity.this, "存储成功", 2).show();
			resultString=null;
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void startRecord(View v)
	{
		isRecord=!isRecord;
		if(isRecord)
		{
			startRecordButton.setText("注册中.....");
		}
		else {
			startRecordButton.setText("开始注册");
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
	public void takePhoto(View v)
	{
		//图片存入地址
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);//构造intent
        startActivityForResult(cameraIntent, CAMERA_REQUEST);//发出intent，并要求返回调用结果
	}
    /**
     * 接收cameraIntent传回的信息
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST) {
            //System.exit(0);
        	Bitmap photo=null;
        	if(data!=null)
        		photo = (Bitmap) data.getExtras().get("data");
            if(photo!=null)
            {
            	Bitmap bitmap=Bitmap.createBitmap(photo, 0, 0, photo.getWidth(), photo.getWidth());
            	BitmapDrawable bitmapDrawable=new BitmapDrawable(bitmap);
            	takePhoto.setBackground(bitmapDrawable);
            }
        }
        
    }
	/***
	 * 
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			return false;//返回到MainTabActivity的onKeyDown()方法
		}
		return super.onKeyDown(keyCode, event);
	}
	   
    Handler writeHandler=new Handler()
    {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			if(msg.arg1==MAXVALUE)
			{
				progressBar.setProgress(msg.arg1);
            	try {
            		size=0;//清零
            		//对数据进行分析
            		//求周期
            		//找到极值点
            		int start = 0;
            		//间隔列表
            		ArrayList<Point> list=new ArrayList<Point>();
            		for(int i=10;i<MAXVALUE-26;i++)//根据最值和其他值的关系找到T波点，3000个数据里面的周期
            		{
            			if(analyse[i]-analyse[i-5]>0.35 && analyse[i]-analyse[i-6]>0.3 && analyse[i]-analyse[i-7]>0.25
            					&& analyse[i]-analyse[i+5]>0.35 && analyse[i]-analyse[i+6]>0.3 && analyse[i]-analyse[i+7]>0.25
            					&& analyse[i]>=analyse[i-1] && analyse[i]>=analyse[i+1] 
            					&& analyse[i]-analyse[i+12]>0.1 && analyse[i]-analyse[i+13]>0.1
            					&& analyse[i]-analyse[i+22]>0.1 && analyse[i]-analyse[i+23]>0.1)
            			{
            				Point p=new Point();//每符合一次，即为一个心电波形，进行记录
            				p.n=i;
            				p.distance=i-start;
            				list.add(p);
            				start=i;
            			}
            		}
            		
            		if(list.size()<15)
            		{
            			Toast.makeText(RegisterNewUserActivity.this, "符合要求的点过少，请重新测量", 3).show();
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
            		
            		//排列  去掉 6个最大值 6个最小值
            		Arrays.sort(array);
            		
            		for(int i=6;i<list.size()-6;i++)
            		{
            			sum+=array[i].distance;
            		}
            		//均值
            		int average=sum/(list.size()-1);
            		if(average<checkSum)
            		{
            			Toast.makeText(RegisterNewUserActivity.this, "符合要求的点过少，请重新测量", 3).show();
            			return;
            		}
            		float [] tempFloat=new float[checkSum];
            		//将中间的三个波段取出来
            		int t=list.get(list.size()/2).n;
            		int k=0;
            		for(int i=t-checkLeft;i<t+checkRight;i++)
            		{
            			tempFloat[k++]+=analyse[i];
            		}
            		//取平均值,并生成字符串
            		StringBuffer sBuffer=new StringBuffer();
            		for(int i=0;i<checkSum;i++)
            		{
            			sBuffer.append(tempFloat[i]).append(',');
            		}
            		resultString=sBuffer.toString();
            		
            		maskLayout.setVisibility(View.VISIBLE);
				} catch (Exception e) {
					e.printStackTrace();
				}
            	Toast.makeText(RegisterNewUserActivity.this, "采集完毕", Toast.LENGTH_SHORT).show();
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
    	int checkCNT=0;
    	float[] values=new float[size];//队列形式
    	int cursor=0;

		@Override
		public void run() {
			// TODO Auto-generated method stub
			//Log.i(TAG, "readThread线程启动");				
            if (canReadFlag!=false) {
            	COUNT++;
//				bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
//
//            	int readOneIntResult=(int) readOneADresult(bufferedReader);
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
            	
            	if(isRecord)
            	{
            		if(checkCNT<MAXVALUE)
            			analyse[checkCNT++]=outPut;
	            	numberRead++;
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
		}
	}; 
  
    
    @Override
	protected void onPause() {
		String stopControlText="w";
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
    	timer.cancel();
    	dbManager.closeDB();
		String stopControlText="w";
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
    
    OnClickListener maskListenr=new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if(maskLayout.getVisibility()==View.GONE)
			{
				maskLayout.setVisibility(View.VISIBLE);
				timer.cancel();
			}
			else {
				maskLayout.setVisibility(View.GONE);
				
			}
		}
	};
    
     public double[] updateDataToDisplay(double[] DataToDraw,double newReceivedData){
     	double[] newDataToDraw=new double[DataToDraw.length];
     	for(int j=0;j<DataToDraw.length-1;j++){
     		newDataToDraw[j]=DataToDraw[j+1];//移位操作
     	}
     	newDataToDraw[DataToDraw.length-1]=newReceivedData;
     	return newDataToDraw;
     }
     
     public double[] updateDataToDisplayUsingArray(double[] DataToDraw,double[] tempReceivedData){
     	double[] newDataToDraw=new double[DataToDraw.length];
     	int tempMoveDistance=tempReceivedData.length;
     	for(int i=0;i<DataToDraw.length-tempMoveDistance;i++){
     		newDataToDraw[i]=DataToDraw[i+tempMoveDistance];
     	}
     	for(int j=0;j<tempReceivedData.length;j++){
     		newDataToDraw[DataToDraw.length-tempMoveDistance+j]=tempReceivedData[j];
     	}
     	return newDataToDraw;
     }
     
     
 	public void correctDataOfSerialPort(InputStream inputStrm) {
 		byte[] buffer = new byte[10];
 		int bytes;
 		try{
	 		try {
	 			if ((bytes = inputStrm.read(buffer, 0, 2)) > 0)
	 			{
	 				byte[] buf_data = new byte[bytes];
	 				for (int i = 0; i < bytes; i++) {
	 					buf_data[i] = buffer[i];
	 				}
	 				byte[] buf_data1 = new byte[2];
	 				buf_data1[0] = buf_data[1];
	 				buf_data1[1] = buf_data[0];
	 				String s1 = bytesToHexString(buf_data1);
	 				int intResult = Integer.parseInt(s1, 16);
	 				if ((intResult * 3.3 / 4096 > 3.3)||(intResult * 3.3 / 4096<1)) {
	 					inputStrm.read(buffer,2,1);
	 				}else{
	 				}
	 			}
	 		} catch (IOException e) {
	 			e.printStackTrace();
	 		}
 		}catch (Exception e){
 		}
 		buffer=null;
 	}
// 		public float readOneADresult(BufferedReader in) {
//			// byte[] buffer = new byte[512];
//			int bytes = 0;
//			String string = "";
//			float result = -1;
//			try {
//				try {
//					string = in.readLine();// 以行为分界，进行数据解析
//				} catch (IOException e) {
//					Log.d(TAG, e.getMessage());
//				}
//				bytes = string.length();
//				if (bytes > 0)// 表明接收到数据
//				{
//					// Log.d(TAG, string);
//					int indexStart = string.indexOf("GG");
//					int indexEnd = string.indexOf("HH");
//					if (indexEnd > indexStart && indexStart != -1) {
//						string = string.substring(indexStart + 2, indexEnd);// 提取AA……BB之间的字符
//						Log.i("心电读取值：", string);
//						try {
//							result = Float.parseFloat(string)-450;
//						} catch (Exception e) {
//							e.printStackTrace();
//						}
//					}
//				}
//			}catch (Exception e) {
//			}
//			//string = null;
//			return result;
// 	}
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
 		Log.d("floatResult", String.valueOf(floatResult));
 		return (int)floatResult;
 	}
 	public static float[] readSpecifiedLengthADresult(InputStream inputStrm1,int dataLength){
 		float[] resultData = new float[dataLength];
 		byte[] buffer = new byte[1024];
 		int bytes;
 		try{
 			try {
 				if ((bytes = inputStrm1.read(buffer, 0, dataLength*2)) > 0)
 				{
 					for(int j=0;j<(bytes/2);j++){
 						byte[] buf_data1 = new byte[2];
 						buf_data1[0] = buffer[j*2+1];
 						buf_data1[1] = buffer[j*2];
 						String s1 = bytesToHexString(buf_data1);
 						int intResult = Integer.parseInt(s1, 16);
 						float floatResult=(float) (intResult*3.3/4095);
 						resultData[j]=floatResult;
 					}
 				}
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}catch (Exception e){
 		}
 		buffer=null;
 		return resultData;
 	}
     
     public static String bytesToHexString(byte[] src){  
 	    StringBuilder stringBuilder = new StringBuilder("");  
 	    if (src == null || src.length <= 0) {  
 	        return null;  
 	    }  
 	    for (int i = 0; i < src.length; i++) {  
 	        int v = src[i] & 0xFF;  
 	        String hv = Integer.toHexString(v);  
 	        if (hv.length() < 2) {  
 	            stringBuilder.append(0);  
 	        }  
 	        stringBuilder.append(hv);  
 	    }  
 	    return stringBuilder.toString();  
 	}  
	public static double[] getOriginalSignalFromTXT(File file) throws Exception {
		double[] originalSignalOnTXT = null;
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line = null;
		String[] totalSignalFromOnePerson = null;
		while ((line = br.readLine()) != null) {
			totalSignalFromOnePerson = line.split("\t");
		}
		originalSignalOnTXT = new double[totalSignalFromOnePerson.length];
		for (int i = 0; i < totalSignalFromOnePerson.length; i++) {
			originalSignalOnTXT[i] = Double
					.parseDouble(totalSignalFromOnePerson[i]);
		}
		return originalSignalOnTXT;
	}
}
/**
try {
Intent cropIntent = new Intent("com.android.camera.action.CROP");
cropIntent.setDataAndType(picUri, "image/*");
cropIntent.putExtra("crop", "true");
cropIntent.putExtra("aspectX", 1);
cropIntent.putExtra("aspectY", 1);
cropIntent.putExtra("outputX", 256);
cropIntent.putExtra("outputY", 256);
cropIntent.putExtra("return-data", true);
startActivityForResult(cropIntent, PIC_CROP);
}
catch(ActivityNotFoundException anfe){
String errorMessage = "Whoops - your device doesn't support the crop action!";
Toast toast = Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT);
toast.show();
}
*/