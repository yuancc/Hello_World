package com.hdu.yuan.heartrate;
import com.hdu.yuan.heartrate.util.ClsUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class BluetoothConnectActivity extends Activity {
	private static final String TAG = "BluetoothConnectActivity";
	///蓝牙部分
	ECGApplication hegApplication;
	Button searchblueButton;
	Button DirectconnectButton;
	Button nfcMatchButton;
	List<HashMap<String,Object>> deviceDataList;
	private SimpleAdapter deviceAdapter;//列表适配器
	private StringBuilder stringBuilder=new StringBuilder();
	private BluetoothAdapter bluetoothAdapter= BluetoothAdapter.getDefaultAdapter();
	private BluetoothSocket bluetoothSocket = null;
	private String selectMacAddress;
	private String readNfcinfo;
	private boolean canReadFlag;
	ProgressBar progressBarDevice;
	private ListView DeviceListView;
	
	//蓝牙自动连接参数设置
	private boolean btOn=false;
	String strPsw="1234";
	private boolean connecting=false;
	private boolean connected=true;
	private String Blurtoochname="SaO2";
	
	//NFC连接部分
    private String BluetoochAdress;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bluetooth);
		Toast.makeText(this, "请选择无线连接模式", Toast.LENGTH_LONG).show();
		Log.i(TAG, "执行BluetoothConnectActivity的onCreate()方法");
		progressBarDevice=(ProgressBar)findViewById(R.id.waiting);
		searchblueButton = (Button) findViewById(R.id.search_blue);
		nfcMatchButton=(Button) findViewById(R.id.nfcconnect);
		DirectconnectButton = (Button) findViewById(R.id.Directconnect);
		DeviceListView=(ListView)this.findViewById(R.id.DeviceListView);
        DeviceListView.setOnItemClickListener(new ItemClickListener());
		
		initBluetooth();
		
		//NFC连接部分
	}
	private void initBluetooth()
	{
		hegApplication=(ECGApplication)getApplication();
        IntentFilter discoveryFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mDeviceReceiver, discoveryFilter);//registerReceiver注册BluetoothDevice.ACTION_FOUND类型的广播接收器
        IntentFilter foundFilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mDeviceReceiver, foundFilter);
	}
	 public void ConnectDirectClick(View v) {
		 Toast.makeText(this, "请等待连接", 1).show();
		 if(!connecting)
			{
			 BluetoochAdress = "20:14:10:30:16:23";
				new Thread(new ConnectToBT()).start();
				if(connected){
					Toast.makeText(this, "成功连接："+Blurtoochname, 1).show();
				}
				else{
					Toast.makeText(this, "连接："+Blurtoochname+"失败，请重新连接", 1).show();
				}
			}
			else {
				Toast.makeText(this, "正在连接中,请稍等!", 1).show();
			}
	}
	 public void ConnectByNFCClick(View v) {
		 if(bluetoothSocket==null){
		    	Intent startBluetoothConnectionNfcConfirmActivityIntent=new Intent(this,BluetoothConnectionNfcConfirm.class)
		    	.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		    	startActivity(startBluetoothConnectionNfcConfirmActivityIntent);
	    	}else{
	    		Toast.makeText(this, "已建立和蓝牙设备的连接，请断开连接后再点击该按钮", Toast.LENGTH_SHORT).show();
	    	}
	}
	 class ConnectToBT implements Runnable {
			public void run() {
				connecting=true;
				if(!btOn){//00:14:01:03:58:74,调试时蓝牙地址
					if(pair(BluetoochAdress, strPsw))
					{
						UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //自定义UUID 
				    	try {
				    		BluetoothSocket bluetoothSocket=hegApplication.getRemoteDevice().createRfcommSocketToServiceRecord(MY_UUID);
				    		bluetoothSocket.connect();
							hegApplication.setBluetoothSocket(bluetoothSocket);
							btOn=true;
							Blurtoochname=hegApplication.getRemoteDevice().getName();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
				    	connected=!connected;
					}
				}
				else 
				{
					try{
						if(hegApplication.getBluetoothSocket()!=null)
						{
							hegApplication.getBluetoothSocket().close();
							hegApplication.setBluetoothSocket(null);
						}
					} catch (IOException e) 
					{
						e.printStackTrace();
					}
					btOn=false;
				}
				connecting=false;
			}
		}
	 public boolean pair(String strAddr, String strPsw)
		{
			boolean result = false;
			BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
			bluetoothAdapter.cancelDiscovery();
			if (!bluetoothAdapter.isEnabled())
			{
				bluetoothAdapter.enable();
			}
			if (!BluetoothAdapter.checkBluetoothAddress(strAddr))
			{ // 检查蓝牙地址是否有效
				Log.d("mylog", "devAdd un effient!");
			}
			BluetoothDevice device = bluetoothAdapter.getRemoteDevice(strAddr);
			if (device.getBondState() != BluetoothDevice.BOND_BONDED)
			{
				try
				{
					Log.d("mylog", "NOT BOND_BONDED");
					ClsUtils.setPin(device.getClass(), device, strPsw); // 手机和蓝牙采集器配对
					ClsUtils.createBond(device.getClass(), device);
					hegApplication.setRemoteDevice( device); // 配对完毕就把这个设备对象传给全局的remoteDevice
					result = true;
				}
				catch (Exception e)
				{
					// TODO Auto-generated catch block
					Log.d("mylog", "setPiN failed!");
					e.printStackTrace();
				} //
			}
			else
			{
				Log.d("mylog", "HAS BOND_BONDED");
				try
				{
					ClsUtils.createBond(device.getClass(), device);
					ClsUtils.setPin(device.getClass(), device, strPsw); // 手机和蓝牙采集器配对
					ClsUtils.createBond(device.getClass(), device);
					hegApplication.setRemoteDevice( device); // 配对完毕就把这个设备对象传给全局的remoteDevice
					result = true;
				}
				catch (Exception e)
				{
					// TODO Auto-generated catch block
					Log.d("mylog", "setPiN failed!");
					e.printStackTrace();
				}
			}
			return result;
		}
		
	 public void ConnectByBluetoochClick(View v) {
	     	if(searchblueButton.getText().toString().equals("搜索蓝牙"))
	     	{
	         	searchBluetooth();
	         	searchblueButton.setText("停止搜索");
	     	}
	     	else if(searchblueButton.getText().toString().equals("停止搜索"))
	     	{
	     		bluetoothAdapter.cancelDiscovery();
	     		searchblueButton.setText("搜索蓝牙");
	     	}
		}
	 
	 public void searchBluetooth()
	    {
	    	if(bluetoothAdapter==null)
	    	{
	    	    Toast.makeText(this, "Bluetooth不存在.", Toast.LENGTH_SHORT).show(); 
	    	    finish();
	    	    return; 
	    	}
	    	if(!bluetoothAdapter.isEnabled())
	    	{
	            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
	            startActivity(enableIntent);
	            return;
	    	}
	    		bluetoothAdapter.startDiscovery();
	    		//显示等待
	    		progressBarDevice.setVisibility(View.VISIBLE);
	    		if(deviceDataList==null){
	    			deviceDataList=new ArrayList<HashMap<String,Object>>();
	            	deviceAdapter=new SimpleAdapter(BluetoothConnectActivity.this, deviceDataList, R.layout.device_item,
	            		new String[]{"devicename","deviceMacAddress"}, 
	            		new int[]{R.id.listViewItemDeviceNameTextView,R.id.listViewItemMacAddressTextView});
	    		}
	    		else {
					deviceDataList.clear();
					deviceAdapter.notifyDataSetChanged();
				}

	            DeviceListView.setAdapter(deviceAdapter);
	    }
	    
	 private final BroadcastReceiver mDeviceReceiver = new BroadcastReceiver() {
	        @Override
	        public void onReceive(Context context, Intent intent) {
	            String action = intent.getAction();

	            if (BluetoothDevice.ACTION_FOUND.equals(action)) 
	            {
	                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
	                HashMap<String,Object> deviceitem=new HashMap<String,Object>();
	                deviceitem.put("devicename", "设备："+device.getName());
	                deviceitem.put("deviceMacAddress", device.getAddress());
	                deviceDataList.add(deviceitem);
	                deviceAdapter.notifyDataSetChanged();
	                Log.d(TAG,"搜索到蓝牙设备");
	            } 
	            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) 
	            {
	            	stringBuilder.append("搜索蓝牙设备完毕\n");
	            	progressBarDevice.setVisibility(View.GONE);
//	            	showOnTextView();
//	                beginsearchButton.setText("重新搜索");
//	                beginsearchButton.setClickable(true);
	            }
	        }
	    };	
	    
	    public boolean onKeyDown(int keyCode, KeyEvent event) {
			if (keyCode == KeyEvent.KEYCODE_BACK) {
				new AlertDialog.Builder(BluetoothConnectActivity.this)
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
		
	    
	    private final class ItemClickListener implements OnItemClickListener{
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,long id) {
				ListView lView=(ListView)parent;
				HashMap<String,Object> item=(HashMap<String,Object>)lView.getItemAtPosition(position);
				selectMacAddress=(String)item.get("deviceMacAddress");
				String selectDeviceName=(String)item.get("devicename");
				new AlertDialog.Builder(BluetoothConnectActivity.this).setTitle("确认连接"+selectDeviceName+"吗？")
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
				    @Override
				    public void onClick(DialogInterface dialog, int which) {
				    	UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //自定义UUID 
				    	if(bluetoothAdapter == null)     
				    	{    
				    	    Toast.makeText(BluetoothConnectActivity.this, "Bluetooth不存在.", Toast.LENGTH_SHORT).show(); 
				    	    finish();
				    	    return;    
				    	}    
				    	if(!bluetoothAdapter.isEnabled())     
				    	{    
				    	    Toast.makeText(BluetoothConnectActivity.this, "请打开蓝牙后使用该按钮.", Toast.LENGTH_SHORT).show();    
				    	    return;    
				    	}
					    if(bluetoothSocket==null){	
					    	BluetoothDevice device = bluetoothAdapter.getRemoteDevice(selectMacAddress);
					    	try {
								bluetoothSocket=device.createRfcommSocketToServiceRecord(MY_UUID);
								hegApplication.setBluetoothSocket(bluetoothSocket);
							} catch (IOException e) {
								e.printStackTrace();
							} 
							try {
								bluetoothSocket.connect();
								hegApplication.setBluetoothSocket(bluetoothSocket);	
							} catch (IOException e1) {
								Toast.makeText(BluetoothConnectActivity.this, "无法完成bluetoothSocket的连接", 0).show();
								e1.printStackTrace();
							}			
							stringBuilder.append("连接至设备: "+device.getName()+",设备地址:"+device.getAddress()+"\n");
							//currentDevice.setText("当前连接的设备："+device.getName());
							canReadFlag=true;
					    }
					    else if(!bluetoothSocket.isConnected())
					    	{
						    	BluetoothDevice device = bluetoothAdapter.getRemoteDevice(selectMacAddress);
						    	try {
									bluetoothSocket=device.createRfcommSocketToServiceRecord(MY_UUID);
									hegApplication.setBluetoothSocket(bluetoothSocket);
								} catch (IOException e) {
									e.printStackTrace();
								} 
								try {
									bluetoothSocket.connect();
									hegApplication.setBluetoothSocket(bluetoothSocket);	
								} catch (IOException e1) {
									Toast.makeText(BluetoothConnectActivity.this, "无法完成bluetoothSocket的连接", 0).show();
									e1.printStackTrace();
								}			
								stringBuilder.append("连接至设备: "+device.getName()+",设备地址:"+device.getAddress()+"\n");
								//currentDevice.setText("当前连接的设备："+device.getName());
								canReadFlag=true;
					    	}else{
					    	Toast.makeText(BluetoothConnectActivity.this, "已经完成连接且开启接收数据的线程，请断开连接后，再次执行该操作", 0).show();
					    }
				    }
				})
				.setNegativeButton("返回", new DialogInterface.OnClickListener() {
				    @Override
				    public void onClick(DialogInterface dialog, int which) {
				    }
				}).show();
			}
		}
	    @Override
		protected void onDestroy() {
			// TODO Auto-generated method stub
			if(bluetoothSocket!=null)
			{
		    	try {
					bluetoothSocket.close();
					bluetoothSocket=null;
					hegApplication.setBluetoothSocket(bluetoothSocket);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		    this.unregisterReceiver(mDeviceReceiver);
			super.onDestroy();
		}
	    @Override
		protected void onStart() {
			// TODO Auto-generated method stub
			super.onStart();
			//Toast.makeText(this, "请选择无线连接模式", Toast.LENGTH_LONG).show();
		}
		@Override
		protected void onRestart() {
			// TODO Auto-generated method stub
			Toast.makeText(this, "请选择无线连接模式", Toast.LENGTH_LONG).show();
			super.onRestart();
		} 
		
		//NFC程序部分
}
