package com.hdu.yuan.heartrate.fragment;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.hdu.yuan.heartrate.R;
import com.hdu.yuan.heartrate.RegisterActivity;
import com.hdu.yuan.heartrate.object.History;
import com.hdu.yuan.heartrate.object.MeasureArg;
import com.hdu.yuan.heartrate.util.DBManager;
import com.hdu.yuan.heartrate.util.HttpUtil;
import com.hdu.yuan.heartrate.view.SingleWaveDisplay;

import android.R.bool;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

public class HistoryFragment extends Fragment {
	private static final String TAG="selection";
	private String url_chose = "http://115.28.0.158:9523/index.php";//http://192.168.161.102:80/index.php
	DBManager dbManager;
	Context context;
	TextView tiwenText,maiboText,xueyangText;
	
	SingleWaveDisplay singleWaveDisplay;
	FrameLayout maskPlay;
	Button play;
	TextView timeHistory;
	boolean isPlay=false;
	private float[] values;
	String datas;
	int size=0;
	int cursor=0;
	Thread thread;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view =inflater.inflate(R.layout.record_history, container,false);
		context=getActivity();
		dbManager=new DBManager(context);
		tiwenText=(TextView)view.findViewById(R.id.tiwen_history);
		maiboText=(TextView)view.findViewById(R.id.maibo_history);
		xueyangText=(TextView)view.findViewById(R.id.xueyang_history);
		singleWaveDisplay=(SingleWaveDisplay)view.findViewById(R.id.singlewavedisplay_history);
		maskPlay=(FrameLayout)view.findViewById(R.id.maskPlay_history);
		play=(Button)view.findViewById(R.id.start_play_history);
		play.setOnClickListener(onClickListener);
		timeHistory=(TextView)view.findViewById(R.id.time_history);
		
		Bundle bundle=getArguments();//得到传过来的Bundle
		String time=bundle.getString("time");
		if(time.endsWith("_S"))//从服务器获取数据
		{
			timeHistory.setText(time.replace("_S", ""));
			int id=bundle.getInt("id");
			Thread thread=new Thread(new GetData(id));
			thread.start();
		}
		else //从本地数据库获取
		{
			History history=dbManager.queryHistoryById(bundle.getInt("id"));
			
			timeHistory.setText(time);
			if(history!=null)
			{
				datas=history.getData();//从history中得到保存的数据
				String[] tempsStrings=datas.split(",");
				size=tempsStrings.length;//得到数据个数
				values=new float[size];
				for(int i=0;i<size;i++)
				{
					try{
					values[i]=Float.parseFloat(tempsStrings[i]);
					}catch(ClassCastException e)
					{
						Log.d(TAG, e.getMessage());
					}
				}
				
				
				MeasureArg measureArg=history.getMeasureArg();
					//Log.d(TAG, "   "+measureArg.tiwen);
				//这个地方修改本地存储的时候单位，如：+"%" 等等
				/*****************图造假*******************/
				tiwenText.setText(measureArg.tiwen!=null?measureArg.tiwen+"℃":"未知");
				maiboText.setText(measureArg.maibo!=null?measureArg.maibo+"次/分":"未知");
				xueyangText.setText(measureArg.xueyang!=null?measureArg.xueyang+"%":"未知");
				//tiwenText.setText("36.6℃");
				//maiboText.setText("75次/分");
				//xueyangText.setText("96%");
				
			}
		}
		return view;
	}
	Handler handler=new Handler()
	{
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			if(msg.what==1)
			{
				MeasureArg measureArg=(MeasureArg)msg.obj;
				tiwenText.setText(measureArg.tiwen+"℃");
				maiboText.setText(measureArg.maibo+"次/分");
				xueyangText.setText(measureArg.xueyang+"%");
				
				//tiwenText.setText("36.6℃");
				//maiboText.setText("75次/分");
				//xueyangText.setText("96%");
			}

		}
	};
	
	@Override
	public void onDetach() {
		// TODO Auto-generated method stub
		super.onDetach();
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}
	
	View.OnClickListener onClickListener=new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			isPlay=!isPlay;
			if(isPlay)
			{
				Log.d(TAG, "停止");
				maskPlay.setAlpha(0);
				if(thread==null)
				{
					thread=new Thread(new PlayThread());
					thread.start();
					isPlay=true;
					cursor=0;
				}
			}
			else {
				Log.d(TAG, "开始");
				maskPlay.setAlpha(0.8f);
			}
		}
	};
 	private void say(String str)
 	{
 		Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
 	}
 	
	Handler againHandler=new Handler()
	{

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			if(msg.what==1)
			{
				maskPlay.setAlpha(0.8f);
				say("播放完成");
			}
		}
		
	};
	class GetData implements Runnable
	{
		private int id;
		public GetData(int id)
		{
			this.id=id;
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub
			List<NameValuePair> params = new ArrayList<NameValuePair>();
		    params.add(new BasicNameValuePair("action","queryXY"));
			params.add(new BasicNameValuePair("id",String.valueOf(id)));//2015-03-19 16:47:40
			String builder=HttpUtil.getString(url_chose, params).trim();
			try{
				JSONArray json=new JSONArray(builder);
				if(json.length()>0){
					     	JSONObject job = json.getJSONObject(0); 
							datas=job.getString("xy_data");//从history中得到保存的数据
							String[] tempsStrings=datas.split(",");
							size=tempsStrings.length;//得到数据个数
							values=new float[size];
							for(int i=0;i<size;i++)
							{
								try{
								values[i]=Float.parseFloat(tempsStrings[i]);
								}catch(ClassCastException e)
								{
									Log.d(TAG, e.getMessage());
								}
							}
							MeasureArg measureArg=new MeasureArg();
							measureArg.tiwen=job.getString("tw")!=null?job.getString("tw"):"未知";
							measureArg.maibo=job.getString("mb")!=null?job.getString("mb"):"未知";
							measureArg.xueyang=job.getString("xy")!=null?job.getString("xy"):"未知";
								//Log.d(TAG, "   "+measureArg.tiwen);
							Message msg=new Message();
							msg.what=1;
							msg.obj=measureArg;
							handler.sendMessage(msg);
					 }
			}catch(JSONException e)
			{
				Log.d(TAG, e.toString());
			}
		}
		
	}
	class PlayThread implements Runnable
	{

		@Override
		public void run() {
			Log.d(TAG, "线程开始");
			// TODO Auto-generated method stub
			while(isPlay)
			{
				singleWaveDisplay.addNewDataToDraw(values[cursor]);
				cursor++;
				try {
					Thread.sleep(20);
				} catch (InterruptedException e) {
					// TODO: handle exception
					Log.d(TAG,"睡眠20ms被中断");
				}
				if(cursor==RegisterActivity.MAXVALUE)
				{
					isPlay=false;
					cursor=0;
					Message msg=new Message();
					msg.what=1;
					againHandler.sendMessage(msg);
				}
			}
			thread=null;
		}
		
	}
	
}
