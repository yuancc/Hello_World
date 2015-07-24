package com.hdu.yuan.heartrate.fragment;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import com.hdu.yuan.heartrate.R;
import com.hdu.yuan.heartrate.adapter.TimeAdapter;
import com.hdu.yuan.heartrate.object.History;
import com.hdu.yuan.heartrate.util.DBManager;
import com.hdu.yuan.heartrate.util.HttpUtil;
import com.hdu.yuan.heartrate.view.ScrollListView;
import com.hdu.yuan.heartrate.view.ScrollListView.RemoveDirection;
import com.hdu.yuan.heartrate.view.ScrollListView.RemoveListener;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
public class TimeFragment extends Fragment implements RemoveListener{
	public static final String TAG="ECG";
	private String url_chose = "http://115.28.0.158:9523/index.php";//http://192.168.161.102:80/index.php
	ScrollListView timeListView;
	List<History> historys;
	DBManager dbManager;
	TimeAdapter timeAdapter;
	Context context;
	
	
	OnScrollItemClickListener onScrollItemClickListener;
	int id;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view =inflater.inflate(R.layout.record_history_time, container, false);
		id=getArguments().getInt("id");
//		Log.d(TAG, "id="+id);
		//初始化
		timeListView=(ScrollListView)view.findViewById(R.id.record_time_list);
		
		context=getActivity();
		dbManager=new DBManager(context);
		timeListView.setRemoveListener(this);
		timeListView.setOnItemClickListener(onItemClickListener);
		historys=dbManager.queryHistoryByPeopleId(id);//按照查找人的id来排列时间列表
		
		timeAdapter=new TimeAdapter(historys, context);
		timeListView.setAdapter(timeAdapter);
		
		Thread thread=new Thread(new ServerData(id));
		thread.start();
		Thread threadecg=new Thread(new ServerEcgData(id));
		threadecg.start();
		return view;
	}
	
	Handler handler=new Handler()
	{
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			if(msg.what==1)
			{
					timeAdapter.notifyDataSetChanged();
			}
		}
	};
	Handler handlerecg=new Handler()
	{
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			if(msg.what==1)
			{
					timeAdapter.notifyDataSetChanged();
			}
		}
	};
	Handler deleteHandler=new Handler()
	{
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			if(msg.what==1)
			{
				Toast.makeText(context, "成功删除", Toast.LENGTH_SHORT).show();
			}
			else if(msg.what==2) {
				Toast.makeText(context, "删除失败", Toast.LENGTH_SHORT).show();
			}
		}
	};
	class ServerData implements Runnable
	{
		private int userId;
		public ServerData(int userId)
		{
			this.userId=userId;
		}
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			List<NameValuePair> params = new ArrayList<NameValuePair>();
		    params.add(new BasicNameValuePair("action","queryXYList"));
			params.add(new BasicNameValuePair("userId",String.valueOf(userId)));//2015-03-19 16:47:40
			
			//String builder=HttpUtil.getString("http://192.168.161.102:80/index.php", params).trim();
			String builder=HttpUtil.getString(url_chose, params).trim();
			if(!builder.equals("[]"))
			{
				try{
					JSONArray json=new JSONArray(builder);
					if(json.length()>0){
						   for(int i=0;i<json.length();i++){
						     JSONObject job = json.getJSONObject(i); 
						     History history=new History();
						     history.setId(Integer.parseInt(job.get("id").toString()));
						     history.setPeopleId(Integer.parseInt(job.get("userId").toString()));
						     history.setTimeOfRecord(job.get("xy_time").toString()+"_S");
						     historys.add(history);// 得到 每个对象中的属性值
						  }
						 }
						Message msg=new Message();
						msg.what=1;
						handler.sendMessage(msg);
				}catch(Exception e)
				{
					Log.d(TAG, builder+e.toString());
				}
			}
		}
		
	}
	class ServerEcgData implements Runnable
	{
		private int userId;
		public ServerEcgData(int userId)
		{
			this.userId=userId;
		}
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			List<NameValuePair> params = new ArrayList<NameValuePair>();
		    params.add(new BasicNameValuePair("action","queryXDList"));
			params.add(new BasicNameValuePair("userId",String.valueOf(userId)));//2015-03-19 16:47:40
			
			//String builder=HttpUtil.getString("http://192.168.161.102:80/index.php", params).trim();
			String builder=HttpUtil.getString(url_chose, params).trim();
			if(!builder.equals("[]"))
			{
				try{
					JSONArray json=new JSONArray(builder);
					if(json.length()>0){
						   for(int i=0;i<json.length();i++){
						     JSONObject job = json.getJSONObject(i); 
						     History history=new History();
						     history.setId(Integer.parseInt(job.get("id").toString()));
						     history.setPeopleId(Integer.parseInt(job.get("userId").toString()));
						     history.setTimeOfRecord(job.get("xd_time").toString()+"_S");
						     historys.add(history);// 得到 每个对象中的属性值
						  }
						 }
						Message msg=new Message();
						msg.what=1;
						handlerecg.sendMessage(msg);
				}catch(Exception e)
				{
					Log.d(TAG, builder+e.toString());
				}
			}
		}
		
	}
	
	class DeleteXY implements Runnable
	{
		private int id;
		public DeleteXY(int id)
		{
			this.id=id;
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub
			List<NameValuePair> params = new ArrayList<NameValuePair>();
		    params.add(new BasicNameValuePair("action","deleteUserXY"));
			params.add(new BasicNameValuePair("id",String.valueOf(id)));//2015-03-19 16:47:40
			String builder=HttpUtil.getString("url_chose", params).trim();
			Message msg=new Message();
			msg.what=builder.contains("success")?1:2;
			deleteHandler.sendMessage(msg);
		}
		
	}
	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		try {   
			onScrollItemClickListener = (OnScrollItemClickListener) activity;   
        } catch (ClassCastException e) {   
            throw new ClassCastException(activity.toString()   
                    + " must implement OnHeadlineSelectedListener");   
        }   
    } 
	@Override
	public void onDetach() {
		// TODO Auto-generated method stub
		super.onDetach();
		dbManager.closeDB();
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		//为什么这里要重新加载，忘了
		timeAdapter.setList(historys);
		timeAdapter.notifyDataSetChanged();
	}
	
	
	public interface OnScrollItemClickListener
	{
		public void onTimeItemClickListener(int position,int id,String time);
	}
	OnItemClickListener onItemClickListener=new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view,  
                int position, long id) {
			// TODO Auto-generated method stub
			onScrollItemClickListener.onTimeItemClickListener(position, historys.get(position).getId(),historys.get(position).getTimeOfRecord());
			Toast.makeText(context, historys.get(position).getTimeOfRecord(), Toast.LENGTH_SHORT).show();
		}
		
	};
	  //滑动删除之后的回调方法  
    @Override  
    public void removeItem(RemoveDirection direction, int position) { 
    	History history=timeAdapter.getItem(position);
    	int id=history.getId();
    	if(history.getTimeOfRecord().endsWith("_S"))
    	{
    		timeAdapter.remove(position);
    		Thread thread=new Thread(new DeleteXY(id));
    		thread.start();
    	}
    	else {
        	timeAdapter.remove(position);
        	dbManager.deleteHistoryById(id);
        	Toast.makeText(context, "成功删除"+history.getTimeOfRecord(), Toast.LENGTH_SHORT).show();
		}
    }
}
