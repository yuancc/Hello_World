package com.hdu.yuan.heartrate.fragment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.hdu.yuan.heartrate.AdministrationActivitySecond;
import com.hdu.yuan.heartrate.R;
import com.hdu.yuan.heartrate.RecordActivity;
import com.hdu.yuan.heartrate.adapter.PeopleAdapter;
import com.hdu.yuan.heartrate.object.People;
import com.hdu.yuan.heartrate.util.DBManager;
import com.hdu.yuan.heartrate.util.FileService;
import com.hdu.yuan.heartrate.view.ScrollListView;
import com.hdu.yuan.heartrate.view.ScrollListView.RemoveDirection;
import com.hdu.yuan.heartrate.view.ScrollListView.RemoveListener;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class RecordFragment extends Fragment implements RemoveListener{
	public static final String TAG="ECG";
	ScrollListView peopleListView;
	List<People> peoples;
	DBManager dbManager;
	PeopleAdapter peopleAdapter;
	Context context;
	OnScrollItemClickListener onScrollItemClickListener;
	Button registerButton;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState){
		// TODO Auto-generated method stub
		View view =inflater.inflate(R.layout.record, container, false);
		//初始化
		peopleListView=(ScrollListView)view.findViewById(R.id.peopleList);//获取已注册用户列表
		context=getActivity();
		dbManager=new DBManager(context);
		registerButton=(Button)view.findViewById(R.id.startRegister);//注册新用户按钮
		registerButton.setOnClickListener(registerClickListener);
		peopleListView.setRemoveListener(this);//setRemoveListener()滑动删除的回调接口
		peopleListView.setOnItemClickListener(onItemClickListener);
		peoples=dbManager.queryListPeople();//用户序列化输出
		peopleAdapter=new PeopleAdapter(peoples, context);
		peopleListView.setAdapter(peopleAdapter);
		
		return view;
	}
	
	OnClickListener registerClickListener=new OnClickListener() {//注册新用户事件
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			onScrollItemClickListener.onRegisterClick();
		}
	};
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
		peopleAdapter.setList(dbManager.queryListPeople());
		peopleAdapter.notifyDataSetChanged();
	}
	public interface OnScrollItemClickListener
	{
		public void onItemClickListener(int position,int id);
		public void onRegisterClick();
	}
	OnItemClickListener onItemClickListener=new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view,  
                int position, long id) {
			// TODO Auto-generated method stub   onItemClickListener方法把position和id传送到setArguments
			//调用的是RecordActivity中实现的onItemClickListener方法
			onScrollItemClickListener.onItemClickListener(position, peoples.get(position).getId());
			Toast.makeText(context, peoples.get(position).getName(), Toast.LENGTH_SHORT).show();
		}
		
	};
	  //滑动删除之后的回调方法  
    @Override  
    public void removeItem(RemoveDirection direction, int position) { 
    	People people=peopleAdapter.getItem(position);
    	int id=people.getId();
    	String imagePath=people.getImagePath();
    	peopleAdapter.getHashMap().remove(imagePath);
    	peopleAdapter.remove(position);  
        switch (direction) {  
        case RIGHT:  
        	Toast.makeText(context, "成功删除  "+ people.getName(), Toast.LENGTH_SHORT).show();  
            break;  
        case LEFT:  
            Toast.makeText(context, "成功删除  "+ people.getName(), Toast.LENGTH_SHORT).show();  
            break;  
        default:  
            break;  
        } 
        File file =new File(imagePath);
        if(file.exists())
        {
        	FileService.deleteFile(file); 
        }
        dbManager.deletePeople(id);
        dbManager.deleteHistoryByPeopleId(id);
    }
	@Override
	public void startActivityForResult(Intent intent, int requestCode) {
		// TODO Auto-generated method stub
		intent = new Intent(getActivity(), AdministrationActivitySecond.class);  
		super.startActivityForResult(intent, requestCode);
	}
  
}
