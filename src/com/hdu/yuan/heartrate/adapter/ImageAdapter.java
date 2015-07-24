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
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.hdu.yuan.heartrate.ChooseRegisterModelActivity;
import com.hdu.yuan.heartrate.R;
import com.hdu.yuan.heartrate.adapter.PeopleAdapter.Holder;
import com.hdu.yuan.heartrate.object.People;

public class ImageAdapter extends BaseAdapter{
	private final static String TAG="selection";
	HashMap<String, SoftReference<ImageView>> imageCache;
	BitmapFactory.Options opt;
	List<People> list;
	LayoutInflater inflater;
	Context context;
	public ImageAdapter(List<People> list,Context context)
	{
		this.list=list;
		this.context=context;
		inflater=LayoutInflater.from(context);
		imageCache=new HashMap<String, SoftReference<ImageView>>();
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
	public List<People> getList()
	{
		return this.list;
	}
	public HashMap<String, SoftReference<ImageView>> getHashMap()
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
	        //注意setText中只能放字符串，即使放数字不报错
	        //holder.image.setBackground(null);
	        //String imagePath=list.get(position).getImagePath();//在此处设置imagePath由识别结果而生？
			String imagePath=ChooseRegisterModelActivity.registerimagePath;
			if(imagePath=="")
			{
				imagePath=list.get(position).getImagePath();
			}
	        SoftReference<ImageView> s=imageCache.get(imagePath);
	        //Log.d(TAG, "getView");
	        if(s!=null)
	        {
	        	ImageView  imageView=s.get();
	        	if(imageView==null)
	        	{
	        		ImageView iv=new ImageView(context);
	        		int size=dip2px(context,100);
	        		LayoutParams layoutParams=new LayoutParams(size, size);
		        	Bitmap bitmap=BitmapFactory.decodeFile(imagePath, opt);
		        	iv.setImageBitmap(bitmap);
		        	SoftReference<ImageView> softReference=new SoftReference<ImageView>(iv);
		        	imageCache.put(imagePath, softReference);
		        	imageView=iv;
	        	}
	        	return imageView;
	        }
	        else 
	        {
        		ImageView iv=new ImageView(context);
        		int size=dip2px(context,150);
        		LayoutParams layoutParams=new LayoutParams(size, size);
	        	Bitmap bitmap=BitmapFactory.decodeFile(imagePath, opt);
	        	iv.setImageBitmap(bitmap);
	        	SoftReference<ImageView> softReference=new SoftReference<ImageView>(iv);
	        	imageCache.put(imagePath, softReference);
	        	return iv; 
			}
	}
    /** 
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素) 
     */  
    public static int dip2px(Context context, float dpValue) {  
        final float scale = context.getResources().getDisplayMetrics().density;  
        return (int) (dpValue * scale + 0.5f);  
    }  
  
    /** 
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp 
     */  
    public static int px2dip(Context context, float pxValue) {  
        final float scale = context.getResources().getDisplayMetrics().density;  
        return (int) (pxValue / scale + 0.5f);  
    } 
}
