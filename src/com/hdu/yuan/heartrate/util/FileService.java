package com.hdu.yuan.heartrate.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

public class FileService {
	private Context context;
	  //保存到SD卡  
    private static String sdState = Environment.getExternalStorageState();
    private static String path = Environment.getExternalStorageDirectory().toString();
	public FileService(Context context) {
		this.context = context;
	}
	 /** 
     * new文件名= 时间 + 全球唯一编号  中文名图片无法传输
     * @param fileName old文件名 
     * @return new文件名 
     */  
    private static String generateFileName() {  
        String uuid=UUID.randomUUID().toString();  
        //int position = fileName.lastIndexOf(".");     
        //String extension = fileName.substring(position);     
        return uuid;     
    }
    public static String saveDrawableToSDCard(Bitmap bitmap)
    {
    	String fileName=generateFileName()+".jpg";
    	File SpicyDirectory = new File("/sdcard/heartrate/images/");
    	SpicyDirectory.mkdirs();
    	 
    	String filename="/sdcard/heartrate/images/" + fileName;
    	FileOutputStream out = null;
    	try {
    	out = new FileOutputStream(filename);
    	bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
    	} catch (Exception e) {
    	e.printStackTrace();
    	} finally {
    	try {
    	out.flush();
    	} catch (IOException e) {
    	// TODO Auto-generated catch block
    	e.printStackTrace();
    	}
    	try {
    	out.close();
    	} catch (IOException e) {
    	// TODO Auto-generated catch block
    	e.printStackTrace();
    	}
    	out=null;
    	}
    	return filename;
    }
    public static Bitmap getBitmapFromSDCard(String filePath)
    {
    	Bitmap bitmap = null;  
        try  
        {  
            File file = new File(filePath);  
            if(file.exists())  
            {  
                bitmap = BitmapFactory.decodeFile(filePath);  
            }  
        } catch (Exception e)  
        {  
            // TODO: handle exception  
        }  
        return bitmap; 
    }
    public static void saveBitmap(Bitmap bitmap,String imageName)
    { 
       File file;
       File PicName;
       if(sdState.equals(Environment.MEDIA_MOUNTED))
       {
        //获得sd卡根目录
	        file = new File(path + "/sdcard/heartrate/images/");
	        if(!file.exists())
	        {
	         file.mkdirs();
	        }
	        PicName = new File(file, imageName);
	        try {
	         if(!PicName.exists())
	         {
	          PicName.createNewFile();
	         }
	      FileOutputStream fos = new FileOutputStream(PicName);
	      if(PicName.getName().endsWith(".png"))
	      {
	       bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
	      }
	      else if(PicName.getName().endsWith(".jpg"))
	      {
	       bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
	      }
	      fos.flush();
	      fos.close();
	     } catch (FileNotFoundException e) {
	      e.printStackTrace();
	     } catch (IOException e) {
	      e.printStackTrace();
	     }     
       		}
      }
     //从SD卡取
      public static Bitmap getBitmap(String imageName)
      {
       Bitmap bitmap = null;
       File imagePic;
       if(sdState.equals(Environment.MEDIA_MOUNTED))
       {
        
        imagePic = new File(path + "/sdcard/heartrate/images/", imageName);
        if(imagePic.exists())
        {
         try {
          bitmap = BitmapFactory.decodeStream(new FileInputStream(imagePic));
         } catch (FileNotFoundException e) {
//          e.printStackTrace();
         }
        }
       }
    return bitmap;
      }
      /**
       * 将SD卡文件删除
       * @param file
       */
      public static void  deleteFile(File file)
      {
    		 if (file.exists())
    		 {
    		  	if (file.isFile())
    		  	{
    		  		file.delete();
    		  	}
    		 }
      }
    /**
     * 
     * @param filename
     * @param content
     * @throws Exception
     */
	public void saveToSDCard(String filename, String content)throws Exception {
		File file = new File(Environment.getExternalStorageDirectory(), filename);
		FileOutputStream outStream = new FileOutputStream(file,true);//true��ʾ׷������
		outStream.write(content.getBytes());
		outStream.close();
	}
	
	public void save(String filename, String content) throws Exception {
		FileOutputStream outStream = context.openFileOutput(filename,
				Context.MODE_PRIVATE);
		outStream.write(content.getBytes());
		outStream.close();
	}

	public void saveAppend(String filename, String content) throws Exception {
		FileOutputStream outStream = context.openFileOutput(filename,
				Context.MODE_APPEND);
		outStream.write(content.getBytes());
		outStream.close();
	}

	public void saveReadable(String filename, String content) throws Exception {
		FileOutputStream outStream = context.openFileOutput(filename,
				Context.MODE_WORLD_READABLE);
		outStream.write(content.getBytes());
		outStream.close();
	}

	public void saveWriteable(String filename, String content) throws Exception {
		FileOutputStream outStream = context.openFileOutput(filename,
				Context.MODE_WORLD_WRITEABLE);
		outStream.write(content.getBytes());
		outStream.close();
	}

	public void saveRW(String filename, String content) throws Exception {
		FileOutputStream outStream = context.openFileOutput(filename,
				Context.MODE_WORLD_WRITEABLE + Context.MODE_WORLD_READABLE);
		outStream.write(content.getBytes());
		outStream.close();
	}

	public String read(String filename) throws Exception {
		FileInputStream inStream = context.openFileInput(filename);
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len = 0;
		while ((len = inStream.read(buffer)) != -1) {
			outStream.write(buffer, 0, len);
		}
		byte[] data = outStream.toByteArray();
		return new String(data);
	}
	
  	public void copyFromTo(String FromFilePath,String ToFilePath){
  		try {
  			FileInputStream fosfrom = new java.io.FileInputStream(FromFilePath);
  			FileOutputStream fosto = new FileOutputStream(ToFilePath);
  			byte bt[] = new byte[1024];
  			int c;
  			while ((c = fosfrom.read(bt)) > 0) {
  				fosto.write(bt, 0, c); 
  			}
  			fosfrom.close();
  			fosto.close();
  		} catch (Exception ex) {
  			Log.e("readfile", ex.getMessage());
  		}
  	}

}
