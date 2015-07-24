package com.hdu.yuan.heartrate;
import java.io.IOException;
import java.util.UUID;
import android.app.Activity;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class BluetoothConnectionNfcConfirm extends Activity {
	private ECGApplication hegApplication;
	private BluetoothSocket bluetoothSocket;
	
	private TextView nfcMACinformationTextView;
	private TextView BluetoothDeviceOnNFCConnectionTextView;
	
	private NfcAdapter mNfcAdapter;
	PendingIntent m_NfcPendingIntent;
	IntentFilter[] m_NdefExchangeFilters;
	
	private BluetoothAdapter	bluetoothAdapter= BluetoothAdapter.getDefaultAdapter();
	
	private static final String TAG = "BluetoothConnectionNfcConfirm";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.bluetoothconnectionnfcconfirm);
		
		hegApplication=(ECGApplication)getApplication();
		bluetoothSocket=hegApplication.getBluetoothSocket();
		
		nfcMACinformationTextView=(TextView)this.findViewById(R.id.nfcMACinformationTextView);
		BluetoothDeviceOnNFCConnectionTextView=(TextView)this.findViewById(R.id.BluetoothDeviceOnNFCConnectionTextView);
		
		 mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
	        if (mNfcAdapter==null) {
	        	Toast.makeText(this, "设备不支持NFC！请手动配对蓝牙设备！", Toast.LENGTH_SHORT).show();
			}
        m_NfcPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
        		getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
		IntentFilter ndefDetected = new IntentFilter(
				NfcAdapter.ACTION_NDEF_DISCOVERED);
		try {
			ndefDetected.addDataType("text/plain");
		} catch (MalformedMimeTypeException e) {
		}
		m_NdefExchangeFilters = new IntentFilter[] {ndefDetected};
	
		bluetoothAdapter.enable();
	}
	
	
	@Override
	protected void onResume() {
		super.onResume();
		Log.i(TAG, "执行onResume()方法");
		
		 if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
	            NdefMessage[] messages = getNdefMessagesFromIntent(getIntent());
	            byte[] payload = messages[0].getRecords()[0].getPayload();
	            String MacAddress=new String(payload);
	            Log.i(TAG, "执行onResume（）方法第一个if.其中Mac地址为"+MacAddress);
	            nfcMACinformationTextView.setText(MacAddress);
	            Toast.makeText(this, "MAC地址为："+new String(payload), Toast.LENGTH_LONG).show();
	            setIntent(new Intent()); 
	        }
        if (mNfcAdapter!=null) {
        	enableNdefExchangeMode();
        	Log.i(TAG, "执行onResume()方法的第二个if");
        }
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		
        if ( NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            NdefMessage[] msgs = getNdefMessagesFromIntent(intent);
            String tempPayloadMacAddress = new String(msgs[0].getRecords()[0].getPayload());
            Log.i(TAG, "执行onNewIntent（）方法.其中Mac地址为"+tempPayloadMacAddress);
            nfcMACinformationTextView.setText(tempPayloadMacAddress);
            Toast.makeText(this, "MAC地址为："+tempPayloadMacAddress, Toast.LENGTH_SHORT).show();
            hegApplication.setMacAddressFromNFCTag(tempPayloadMacAddress);
            this.connectWithBluetoothDevice(tempPayloadMacAddress);
        }
        Log.i(TAG, "执行onNewIntent()方法");
	}
	@Override
	protected void onPause() {
		super.onPause();
		if (mNfcAdapter!=null) {
        	mNfcAdapter.disableForegroundNdefPush(this);
        	mNfcAdapter.disableForegroundDispatch(this);
        }
	}
	   private NdefMessage[] getNdefMessagesFromIntent(Intent intent) {
	        NdefMessage[] msgs = null;
	        String action = intent.getAction();
	        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
	                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
	            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
	            if (rawMsgs != null) {
	                msgs = new NdefMessage[rawMsgs.length];
	                for (int i = 0; i < rawMsgs.length; i++) {
	                    msgs[i] = (NdefMessage) rawMsgs[i];
	                }
	            } else {
	                byte[] empty = new byte[] {};
	                NdefRecord record = new NdefRecord(NdefRecord.TNF_UNKNOWN, empty, empty, empty);
	                NdefMessage msg = new NdefMessage(new NdefRecord[] {
	                    record
	                });
	                msgs = new NdefMessage[] {
	                    msg
	                };
	            }
	        } else {
	            Log.d(TAG, "Unknown intent.");
	            Toast.makeText(this, "Unknown intent.", Toast.LENGTH_LONG).show();
	            finish();
	        }
	        return msgs;
	    }
	   private void enableNdefExchangeMode() {
		  mNfcAdapter.enableForegroundDispatch(this, m_NfcPendingIntent, m_NdefExchangeFilters, null);
	    }
	   
	   private void disableNdefExchangeMode() {
	        mNfcAdapter.disableForegroundNdefPush(this);
	        mNfcAdapter.disableForegroundDispatch(this);
	    }
	   
	public void connectWithBluetoothDevice(String macAddress){
		UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");  
    	if(bluetoothAdapter == null)     
    	{    
    	    Toast.makeText(this, "Bluetooth不存在.", Toast.LENGTH_SHORT).show(); 
    	    finish();
    	    return;    
    	}    
    	if(!bluetoothAdapter.isEnabled())     
    	{    
    	    Toast.makeText(this, "请打开蓝牙后使用该按钮.", Toast.LENGTH_SHORT).show();    
    	    return;    
    	}
	    if(bluetoothSocket==null){	
	    	BluetoothDevice device = bluetoothAdapter.getRemoteDevice(macAddress); 
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
				Toast.makeText(this, "无法完成bluetoothSocket的连接", 0).show();
				e1.printStackTrace();
			}
			
	    }else{
	    	Toast.makeText(this, "已经完成连接且开启接收数据的线程，请断开连接后，再次执行该操作", 0).show();
	    }
	}
	
	public void nfcMACinformationConfirmButtonClicked(View v){
		finish();
	}
}
