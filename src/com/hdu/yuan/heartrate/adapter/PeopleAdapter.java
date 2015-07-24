package com.hdu.yuan.heartrate.adapter;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.hdu.yuan.heartrate.object.*;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hdu.yuan.heartrate.R;
public class PeopleAdapter extends BaseAdapter {
	private final static String TAG="selection";
	HashMap<String, SoftReference<Bitmap>> imageCache;
	BitmapFactory.Options opt;
	List<People> list;
	LayoutInflater inflater;
	public PeopleAdapter(List<People> list,Context context)
	{
		this.list=list;
		inflater=LayoutInflater.from(context);//找布局文件
		imageCache=new HashMap<String, SoftReference<Bitmap>>();
		opt = new BitmapFactory.Options();
		opt.inPreferredConfig = Bitmap.Config.RGB_565;
		opt.inPurgeable = true;
		opt.inInputShareable = true;
	}
	public void remove(int positon)
	{
		list.remove(positon);
		notifyDataSetChanged();
	}
	public void setList(List<People> list)
	{
		this.list=null;
		this.list=list;
	}
	public HashMap<String, SoftReference<Bitmap>> getHashMap()
	{
		return imageCache;
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return list.size();
	}

	@Override
	public People getItem(int position) {
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
	            convertView=inflater.inflate(R.layout.record_people_item, null);
	            holder.image=(ImageView) convertView.findViewById(R.id.people_image);  
	            holder.name=(TextView) convertView.findViewById(R.id.people_name);
	            holder.age=(TextView) convertView.findViewById(R.id.people_age);	            
	            convertView.setTag(holder);  
	        }
	        //注意setText中只能放字符串，即使放数字不报错
	        
	        //holder.image.setBackground(null);
	        String imagePath=list.get(position).getImagePath();
	        SoftReference<Bitmap> s=imageCache.get(imagePath);
	        if(s!=null)
	        {
	        	Bitmap bitmap=s.get();
	        	if(bitmap!=null)
	        	{
	        		holder.image.setImageBitmap(bitmap);
	        	}
	        	else 
	        	{
		        	bitmap=BitmapFactory.decodeFile(imagePath, opt);
		        	SoftReference<Bitmap> softReference=new SoftReference<Bitmap>(bitmap);
		        	imageCache.put(imagePath, softReference);
		        	holder.image.setImageBitmap(bitmap);
				}
	        	 Log.d("ECG", "缓存读取");
	        }
	        else 
	        {
	        	Bitmap bitmap=BitmapFactory.decodeFile(imagePath, opt);
	        	SoftReference<Bitmap> softReference=new SoftReference<Bitmap>(bitmap);
	        	imageCache.put(imagePath, softReference);
	        	holder.image.setImageBitmap(bitmap);
			}
	        holder.name.setText(list.get(position).getName());
	        holder.age.setText(String.valueOf(list.get(position).getAge()));
	        return convertView; 
	}

	class Holder {
		ImageView image;
		TextView name;
		TextView age;
	}

}
