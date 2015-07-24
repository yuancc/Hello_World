package com.hdu.yuan.heartrate.adapter;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hdu.yuan.heartrate.R;
import com.hdu.yuan.heartrate.adapter.PeopleAdapter.Holder;
import com.hdu.yuan.heartrate.object.History;

public class TimeAdapter extends BaseAdapter {
	private final static String TAG="selection";
	List<History> list;
	LayoutInflater inflater;
	public TimeAdapter(List<History> list,Context context)
	{
		this.list=list;
		inflater=LayoutInflater.from(context);
	}
	public void remove(int positon)
	{
		list.remove(positon);
		notifyDataSetChanged();
	}
	public void setList(List<History> list)
	{
		this.list=null;
		this.list=list;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return list.size();
	}

	@Override
	public History getItem(int position) {
		// TODO Auto-generated method stub
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
		 final Holder holder;  
	        if(convertView!=null)  
	        {  
	            holder=(Holder) convertView.getTag();  
	        }else {  
	            holder=new Holder();  
	            convertView=inflater.inflate(R.layout.record_history_time_item, null);
	            holder.time=(TextView) convertView.findViewById(R.id.record_time);	            
	            convertView.setTag(holder);  
	        }
	        //注意setText中只能放字符串，即使放数字不报错
	        
	        //holder.image.setBackground(null);
	        holder.time.setText(list.get(position).getTimeOfRecord());
	        return convertView; 
	}

	class Holder {
		TextView time;
	}
}