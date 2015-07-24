package com.hdu.yuan.heartrate.util;

import java.util.ArrayList;
import java.util.List;

import com.hdu.yuan.heartrate.object.MeasureArg;
import com.hdu.yuan.heartrate.object.History;
import com.hdu.yuan.heartrate.object.People;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DBManager {
	public final static String TAG="selection";
	private DBHelper helper;
	private SQLiteDatabase db;
	
	public DBManager(Context context) {
		helper = new DBHelper(context);
		//因为getWritableDatabase内部调用了mContext.openOrCreateDatabase(mName, 0, mFactory);
		//所以要确保context已初始化,我们可以把实例化DBManager的步骤放在Activity的onCreate里
		db = helper.getWritableDatabase();
//		if(db!=null)
//		{
//			Cursor cursor = db.rawQuery("select name from sqlite_master where type='table' order by name", null);  
//			  while(cursor.moveToNext()){  
//			   //遍历出表名  
//			   String name = cursor.getString(0);  
//			   Log.d(TAG, name);  
//			  }  
//		}
//		else {
//			Log.d(TAG, "数据库为空");
//		}
	}


	/**
	 * 添加People
	 * @param aps
	 */
	public void addPeople(List<People> peoples)
	{
        db.beginTransaction();	//开始事务
        try {
        	for (People people : peoples) {
        		db.execSQL("INSERT INTO people VALUES(null,?,?,?,?,?,?)", new Object[]{people.getName(), people.getSex(),people.getAge(),people.getImagePath(),people.getIdentifydata(),people.getSize()});
        	}
        	db.setTransactionSuccessful();	//设置事务成功完成
        } finally {
        	db.endTransaction();	//结束事务
        }
	}
	public void addPeople(People people)
	{
        db.beginTransaction();	//开始事务
        try {
        	db.execSQL("INSERT INTO people VALUES(null,?,?,?,?,?,?)", new Object[]{people.getName(), people.getSex(),people.getAge(),people.getImagePath(),people.getIdentifydata(),people.getSize()});
        		db.setTransactionSuccessful();	//设置事务成功完成
        } finally {
        	db.endTransaction();	//结束事务
        }
	}
	/**
	 * 删除业务
	 */
	public void deletePeople(int _id) {
		db.delete("people", "_id = ?", new String[]{String.valueOf(_id)});
	}
	
	public boolean deleteAllPeoples()
	{
		try {
			db.delete("people", "_id>=?", new String[]{"0"});
		} catch (Exception e) {
			// TODO: handle exception
			return false;
		}
		return true;
	}
	
/**
 * 以下 是查询业务
 * @param rp_id
 * @return
 */
	public List<People> queryListPeople()
	{
		ArrayList<People> peoples = new ArrayList<People>();
		Cursor c = db.rawQuery("SELECT * FROM people", null);
        while (c.moveToNext()) {
        	People people=new People();
        	people.setId(c.getInt(c.getColumnIndex("_id")));
        	people.setName(c.getString(c.getColumnIndex("name")));
        	people.setSex(c.getString(c.getColumnIndex("sex")));
        	people.setAge(c.getString(c.getColumnIndex("age")));
        	people.setImagePath(c.getString(c.getColumnIndex("imagepath")));
        	people.setIdentifydata(c.getString(c.getColumnIndex("identifydata")));
        	people.setSize(c.getInt(c.getColumnIndex("size")));
        	peoples.add(people);
        }
        c.close();
        return peoples;
	}
	public People queryPeople(String name)
	{
		People people=new People();
		Cursor c = db.rawQuery("SELECT * FROM people where name= ? ", new String[]{name});
        while (c.moveToNext()) {
        	people.setId(c.getInt(c.getColumnIndex("_id")));
        	people.setName(c.getString(c.getColumnIndex("name")));
        	people.setSex(c.getString(c.getColumnIndex("sex")));
        	people.setAge(c.getString(c.getColumnIndex("age")));
        	people.setImagePath(c.getString(c.getColumnIndex("imagepath")));
        	people.setSize(c.getInt(c.getColumnIndex("size")));
        }
        c.close();
        return people;
	}
	/**
	 * 求数量
	 * @return
	 */
	public int getCountPeople()
	{
		Cursor c = db.rawQuery("SELECT count(*) FROM people", null);
		int count=0;
        while (c.moveToNext()) {
        	count=c.getInt(0);
        }
        return count;
	}
	
	
/**************************************************************************************/
	/**
	 * 添加People
	 * @param aps
	 */
	public void addHistory(List<History> historys)
	{
        db.beginTransaction();	//开始事务
        try {
        	for (History history : historys) {
        		db.execSQL("INSERT INTO history VALUES(null,?,?,?,?,?,?,?)", new Object[]{history.getPeopleId(), history.getData(),history.getEcgData(),history.getMeasureArg().xueyang,history.getMeasureArg().tiwen,history.getMeasureArg().maibo,history.getTimeOfRecord().toString()});
        	}
        	db.setTransactionSuccessful();	//设置事务成功完成
        } finally {
        	db.endTransaction();	//结束事务
        }
	}
	public void addHistory(History history)
	{
        db.beginTransaction();	//开始事务
        try {
        	db.execSQL("INSERT INTO history VALUES(null,?,?,?,?,?,?,?)", new Object[]{history.getPeopleId(), history.getData(),history.getEcgData(),history.getMeasureArg().xueyang,history.getMeasureArg().tiwen,history.getMeasureArg().maibo,history.getTimeOfRecord().toString()});
        		db.setTransactionSuccessful();	//设置事务成功完成
        } finally {
        	db.endTransaction();	//结束事务
        }
	}
	/**
	 * 删除业务
	 */
	public void deleteHistoryById(int _id) {
		db.delete("history", "id = ?", new String[]{String.valueOf(_id)});
	}
	public void deleteHistoryByPeopleId(int peopleid)
	{
		db.delete("history", "peopleid = ?", new String[]{String.valueOf(peopleid)});
	}
	
	public boolean deleteAllHistorys()
	{
		try {
			db.delete("history", "id>=?", new String[]{"0"});
		} catch (Exception e) {
			// TODO: handle exception
			return false;
		}
		return true;
	}
	
/**
 * 以下 是查询业务
 * @param rp_id
 * @return
 */
	public List<History> queryListHistory()
	{
		ArrayList<History> historys = new ArrayList<History>();
		Cursor c = db.rawQuery("SELECT * FROM history", null);
        while (c.moveToNext()) {
        	History history=new History();
        	history.setId(c.getInt(c.getColumnIndex("id")));
        	history.setPeopleId(c.getInt(c.getColumnIndex("peopleid")));
        	/*************ECG数据***************/
        	history.setEcgData(c.getString(c.getColumnIndex("EcgData")));
        	history.setData(c.getString(c.getColumnIndex("data")));
        	
        	MeasureArg measureArg=new MeasureArg();
        		measureArg.xueyang=c.getString(c.getColumnIndex("xueyang"));
        		measureArg.maibo=c.getString(c.getColumnIndex("maibo"));
        		measureArg.tiwen=c.getString(c.getColumnIndex("tiwen"));
        	history.setMeasureArg(measureArg);
        	history.setTimeOfRecord(c.getString(c.getColumnIndex("timeofrecord")));
        	historys.add(history);
        }
        c.close();
        return historys;
	}
	public List<History> queryHistoryByPeopleId(int peopleid)
	{
		ArrayList<History> historys = new ArrayList<History>();
		Cursor c = db.rawQuery("SELECT * FROM history where peopleid=?", new String[]{String.valueOf(peopleid)});
        while (c.moveToNext()) {
        	History history=new History();
        	history.setId(c.getInt(c.getColumnIndex("id")));
        	history.setPeopleId(c.getInt(c.getColumnIndex("peopleid")));
        	/*************ECG数据***************/
        	history.setEcgData(c.getString(c.getColumnIndex("EcgData")));
        	
        	history.setData(c.getString(c.getColumnIndex("data")));
        	MeasureArg measureArg=new MeasureArg();
        		measureArg.xueyang=c.getString(c.getColumnIndex("xueyang"));
        		measureArg.maibo=c.getString(c.getColumnIndex("maibo"));
        		measureArg.tiwen=c.getString(c.getColumnIndex("tiwen"));
        	history.setMeasureArg(measureArg);
        	history.setTimeOfRecord(c.getString(c.getColumnIndex("timeofrecord")));
        	historys.add(history);
        }
        c.close();
        return historys;
	}
	public History queryHistoryById(int id)
	{
		History history=new History();
		Cursor c = db.rawQuery("SELECT * FROM history where id=?", new String[]{String.valueOf(id)});
		while(c.moveToNext())
		{
	        history.setId(c.getInt(c.getColumnIndex("id")));
	        history.setPeopleId(c.getInt(c.getColumnIndex("peopleid")));
	        /*************ECG数据***************/
        	history.setEcgData(c.getString(c.getColumnIndex("EcgData")));
        	
	        history.setData(c.getString(c.getColumnIndex("data")));
	        MeasureArg measureArg=new MeasureArg();
	        	measureArg.xueyang=c.getString(c.getColumnIndex("xueyang"));
	        	measureArg.maibo=c.getString(c.getColumnIndex("maibo"));
	        	measureArg.tiwen=c.getString(c.getColumnIndex("tiwen"));
	        Log.d(TAG,""+measureArg.xueyang);
	        history.setMeasureArg(measureArg);
	        history.setTimeOfRecord(c.getString(c.getColumnIndex("timeofrecord")));
		}
        c.close();
        return history;
	}
	/**
	 * 求数量
	 * @return
	 */
	public int getCountHistory()
	{
		Cursor c = db.rawQuery("SELECT count(*) FROM history", null);
		int count=0;
        while (c.moveToNext()) {
        	count=c.getInt(0);
        }
        return count;
	}
	/**
	 * close database
	 */
	public void closeDB() {
		db.close();
	}
}
