package com.hdu.yuan.heartrate.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.util.Log;

public class HttpUtil {
	private static String url_chose = "http://115.28.0.158:9523/index.php";//http://192.168.161.102:80/index.php
	public static final String TAG="test";
	public static String getString(String url,List<NameValuePair> params)
	{
		url=url_chose;
		HttpClient client = new DefaultHttpClient(); 
		StringBuilder builder = new StringBuilder(); 
		HttpPost post = new HttpPost(url); 
		try {
		post.setEntity(new UrlEncodedFormEntity(params,"UTF-8"));
		} catch (UnsupportedEncodingException e1) {

		e1.printStackTrace();
		}
		try { 
		HttpResponse response = client.execute(post); 
		HttpEntity entity = response.getEntity();
		BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent())); 
		for (String s = reader.readLine(); s != null; s = reader.readLine()) { 
		builder.append(s); 
		}
		}catch(Exception e)
		{
			Log.d(TAG, e.getMessage());
		}
		return builder.toString();
	}
}
