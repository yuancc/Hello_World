package com.hdu.yuan.heartrate;

import android.app.Application;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
public class ECGApplication extends Application {
	private BluetoothSocket globalBluetoothSocket;
	private String globalDeviceMacAddressFromNFCtag;
	private String nameOfBluetooth="";
	private int screenWidth;
	private int screenHeight;
	private BluetoothDevice remoteDevice;
	@Override
	public void onCreate() {
		super.onCreate();
		this.setBluetoothSocket(null);
		this.setMacAddressFromNFCTag(null);
	}

	public BluetoothSocket getBluetoothSocket() {
		return globalBluetoothSocket;
	}
	public void setName(String name)
	{
		nameOfBluetooth=name;
	}
	public String getName()
	{
		return nameOfBluetooth;
	}
	public void setBluetoothSocket(BluetoothSocket bluetoothSocket){
		this.globalBluetoothSocket=bluetoothSocket;
	}
	
	public String getMacAddressFromNFCTag(){
		return globalDeviceMacAddressFromNFCtag;
	}
	
	public void setMacAddressFromNFCTag(String deviceMacAddressFromNFCtag){
		this.globalDeviceMacAddressFromNFCtag=deviceMacAddressFromNFCtag;
	}

	public int getScreenWidth() {
		return screenWidth;
	}

	public ECGApplication setScreenWidth(int screenWidth) {
		this.screenWidth = screenWidth;
		return this;
	}

	public int getScreenHeight() {
		return screenHeight;
	}

	public ECGApplication setScreenHeight(int screenHeight) {
		this.screenHeight = screenHeight;
		return this;
	}
	public BluetoothDevice getRemoteDevice() {
		return remoteDevice;
	}

	public void setRemoteDevice(BluetoothDevice remoteDevice) {
		this.remoteDevice = remoteDevice;
	}
	
}
