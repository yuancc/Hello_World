package com.hdu.yuan.heartrate.fragment;

import com.hdu.yuan.heartrate.object.People;
import com.hdu.yuan.heartrate.util.DBManager;
import com.hdu.yuan.heartrate.util.FileService;
import com.hdu.yuan.heartrate.R;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class RegisterFragment extends Fragment implements View.OnClickListener{
	private static final String TAG="selection";
	private static final int CAMERA_REQUEST = 1888;
	ImageView imageView;
	Button registerButton,sexButton;
	EditText nameEdit,ageEdit;
	private boolean isMan=true;
	//资源
	Drawable manDrawable;
	Drawable womanDrawable;
	DBManager dbManager;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view =inflater.inflate(R.layout.record_register, container,false);
		//初始化
		dbManager=new DBManager(getActivity());
		imageView=(ImageView)view.findViewById(R.id.portrait);
		imageView.setOnClickListener(this);
		registerButton=(Button)view.findViewById(R.id.register_record);
		registerButton.setOnClickListener(this);
		sexButton=(Button)view.findViewById(R.id.sexSelector);
		sexButton.setOnClickListener(this);
		nameEdit=(EditText)view.findViewById(R.id.nameEdit);
		ageEdit=(EditText)view.findViewById(R.id.ageEdit);
		//资源初始化
		manDrawable=getResources().getDrawable(R.drawable.man);
		womanDrawable=getResources().getDrawable(R.drawable.woman);
		return view;
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
	}
	
	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.portrait:
			//图片存入地址
	        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);//构造intent
	        startActivityForResult(cameraIntent, CAMERA_REQUEST);
			break;
		case R.id.register_record:
			String name=nameEdit.getText().toString();
			String age =ageEdit.getText().toString();
			if(name.equals("") || age.equals(""))
			{
				Toast.makeText(getActivity(), "信息不完整", Toast.LENGTH_SHORT).show();
				break;
			}
			String sex=isMan?"男":"女";
			String imagePath=FileService.saveDrawableToSDCard(((BitmapDrawable)imageView.getBackground()).getBitmap());
			People people=new People();
			people.setName(name);
			people.setAge(age);
			people.setSex(sex);
			people.setImagePath(imagePath);
			dbManager.addPeople(people);
			Toast.makeText(getActivity(), "添加成功", Toast.LENGTH_SHORT).show();
			break;
		case R.id.sexSelector:
			isMan=!isMan;
			if(isMan)
			{
				v.setBackground(manDrawable);
			}
			else {
				v.setBackground(womanDrawable);
			}
			break;
		default:
			break;
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d(TAG,"register fragment");
		// TODO Auto-generated method stub
        if (requestCode == CAMERA_REQUEST) {
            //System.exit(0);
        	Bitmap photo=null;
        	if(data!=null)
        		photo = (Bitmap) data.getExtras().get("data");
            if(photo!=null)
            {
            	Bitmap bitmap=Bitmap.createBitmap(photo, 0, 0, photo.getWidth(), photo.getWidth());
            	BitmapDrawable bitmapDrawable=new BitmapDrawable(bitmap);
            	imageView.setBackground(bitmapDrawable);
            }
        }
	}
}
