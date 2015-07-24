package com.hdu.yuan.heartrate.util;
import org.apache.http.util.EncodingUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.InputStream;
public class FileUtils {
	public final static String TAG="cramack";
	public static final String ENCODING = "UTF-8";  
	
	//从resources中的raw 文件夹中获取文件并读取数据  
    public static  String getFromRaw(Context ctx,int resId){  
        String result = "";  
            try {  
                InputStream in = ctx.getResources().openRawResource(resId);  
                //获取文件的字节数  
                int lenght = in.available();  
                //创建byte数组  
                byte[]  buffer = new byte[lenght];  
                //将文件中的数据读到byte数组中  
                in.read(buffer);  
                result = EncodingUtils.getString(buffer, ENCODING);  
            } catch (Exception e) {  
                e.printStackTrace();  
            }  
            return result.trim();  
    }  
      
    //从assets 文件夹中获取文件并读取数据  
    public static  String getFromAssets(String fileName,Context ctx){  
        String result = "";  
            try {  
                InputStream in = ctx.getResources().getAssets().open(fileName);  
                //获取文件的字节数  
                int lenght = in.available();  
                //创建byte数组  
                byte[]  buffer = new byte[lenght];  
                //将文件中的数据读到byte数组中  
                in.read(buffer);  
                result = EncodingUtils.getString(buffer, ENCODING);  
            } catch (Exception e) {  
                e.printStackTrace();  
            }  
            return result.trim();  
    }
    
    public static Bitmap readBitmap(Context context,int resId)
    {
    	BitmapFactory.Options opt = new BitmapFactory.Options();
    	opt.inPreferredConfig = Bitmap.Config.RGB_565;
    	opt.inPurgeable = true;
    	opt.inInputShareable = true;
    	//获取资源图片
    	InputStream is = context.getResources().openRawResource(resId);
    	Bitmap bitmap = BitmapFactory.decodeStream(is,null, opt);
    	try{
    	is.close();
    	}catch(Exception e)
    	{
    		Log.d(TAG, "readBitmap meets a problem");
    	}
    	return bitmap;
    }
}
