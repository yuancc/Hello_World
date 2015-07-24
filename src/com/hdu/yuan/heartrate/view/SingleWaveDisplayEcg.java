package com.hdu.yuan.heartrate.view;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class SingleWaveDisplayEcg extends SurfaceView implements SurfaceHolder.Callback  {
	private DisplayThread displayThread;
	int numPtsToDraw=256;
	double[] DataToDraw=new double[numPtsToDraw];
	
	
	private int width;
	private int height;
	float _range=2.0f;
	int DEBUG=0;
	private Paint grid_Paint=new Paint();//表格
	private Paint coordinate_paint=new Paint();//坐标系
	private Paint line_Paint=new Paint();//折线

	private static final String TAG="singleWave";
	public SingleWaveDisplayEcg(Context context,AttributeSet attributeSet) {
		super(context, attributeSet);
		
		this.getHolder().addCallback(this);
		
		grid_Paint.setColor(Color.rgb(232, 232, 232));
		coordinate_paint.setColor(Color.rgb(139 ,137, 137));
		coordinate_paint.setTextSize(10.0f*getContext().getResources().getDisplayMetrics().density);
		coordinate_paint.setStrokeWidth(3.0f);
		line_Paint.setColor(Color.BLUE);
		line_Paint.setStrokeWidth(1.5f);
	}
	
	public void Draw(Canvas canvas){
		canvas.drawColor(Color.rgb(255, 255, 255));
		//画表格
		drawGrid(canvas);
		float delta=(float)width/(float)numPtsToDraw;
		for(int i=0;i<numPtsToDraw-1;i++){
			double y_i=(_range-DataToDraw[i])/(2*_range)*height;
			double y_i1=(_range-DataToDraw[i+1])/(2*_range)*height;
			canvas.drawLine((float)i*delta, (float)y_i, (float)(i+1)*delta, (float)y_i1, line_Paint);
		}
		canvas.translate(20*(width/40)+1, height/2.0f);//�������ԭ��
		//画坐标系
		drawCoordinate(canvas,coordinate_paint);
		
	}
	
	private void drawGrid(Canvas canvas) {
		for(int vertical = 1; vertical<40; vertical++){
	    	canvas.drawLine(
	    			vertical*(width/40), 0,
	    			vertical*(width/40), height,
	    			grid_Paint);
	    
	    }	    	
	    for(int horizontal = 1; horizontal<20; horizontal++){
	    	canvas.drawLine(
	    			0, horizontal*(height/20),
	    			width, horizontal*(height/20),
	    			grid_Paint);
	    }
	}
	
	public  void setDataToDraw(double[] datatodraw){
		
		if(datatodraw.length!=DataToDraw.length){
			Log.i("kk", "Ҫ��ʾ����ݸ�ʽ����");
			return;
		}
		Log.i("kk", "setDataToDraw������");
		DataToDraw=datatodraw;
	}
	
	public void addNewDataToDraw(double newData){
		for(int i=0;i<numPtsToDraw-1;i++){
			DataToDraw[i]=DataToDraw[i+1];
		}
		DataToDraw[numPtsToDraw-1]=newData;
//		if(Math.abs(newData)>1.5){
//			DataToDraw[numPtsToDraw-1]=(newData-1.65)*10;
//		}else{
//			DataToDraw[numPtsToDraw-1]=(newData+0.009)*12;
//		}
	}
	
	public void clearScreen(){
		for(int i=0;i<numPtsToDraw-1;i++){
			DataToDraw[i]=0;
		}
	}
	
	public void showThisDataAllTime(double[] dataToshow){
		for(int i=0;i<numPtsToDraw-1;i++){
			DataToDraw[i]=dataToshow[i];
		}
	}
	
	
	private void drawCoordinate(Canvas canvas,Paint p){
		//canvas.drawLine(-width/2.0f  , 0, width/2.0f-1.0f , 0,  coordinate_paint);//水平线
		canvas.drawLine(0, -height/2.0f , 0, height/2.0f , coordinate_paint);//垂直线
		for(int horizontal = 1; horizontal<10; horizontal++){
		    NumberFormat numberformat = new DecimalFormat("0.0");
		    canvas.drawLine(
		    			-10.0f, -height/2.0f + 2*horizontal*(height/20),
		    			10.0f, -height/2.0f + 2*horizontal*(height/20),
		    			coordinate_paint);	
		    canvas.drawText(numberformat.format((5 - horizontal)*_range/5.0f),
		    			20.0f, -height/2.0f + 2*horizontal*(height/20) , coordinate_paint);
		  }
	}
	
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		this.width=width;
		this.height=height;
		Log.d(TAG, "width="+width+"  height="+height);//sumsung  990    450
	}

	public void surfaceCreated(SurfaceHolder holder) {
		displayThread=new DisplayThread(getHolder(),this);
		if(! displayThread.isRunning()){
			displayThread.setRunning(true);
			displayThread.start();
		}
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		boolean retry = true;
		displayThread.setRunning(false);
		while (retry) {
			try {
				displayThread.join();
				displayThread = null;
				retry = false;
			} catch (InterruptedException e) {
			}
		}		
	}
	

	private class DisplayThread extends Thread{
		private SurfaceHolder _surfaceHolder;
		private SingleWaveDisplayEcg _SingleWave;
		private boolean runningFlag = false;
		private long startTime;
		private long endTime;
		private long realendTime;
		
		public DisplayThread(SurfaceHolder surfaceHolder,SingleWaveDisplayEcg singleWave) {//���췽��
			this._surfaceHolder = surfaceHolder;
			this._SingleWave=singleWave;
		}
		
		public boolean isRunning(){
			return runningFlag;
		}
		public void setRunning(boolean runFlag) {
			runningFlag = runFlag;
		}

		@Override
		public void run() {
			Canvas canvas2;
			while (runningFlag){
				canvas2=null;
				 startTime = System.currentTimeMillis();
				try{
					canvas2=_surfaceHolder.lockCanvas();
					synchronized(_surfaceHolder){
						if(canvas2!=null){
							_SingleWave.Draw(canvas2);
						}
					}
				}finally{
					if(canvas2!=null){
						_surfaceHolder.unlockCanvasAndPost(canvas2);
					}
				}
				endTime= System.currentTimeMillis();
				if (endTime -startTime < 50) {
					try{
					Thread.sleep(50- (endTime - startTime));
					} catch(InterruptedException e) {
					e.printStackTrace();
					}
				}
				realendTime=System.currentTimeMillis();
			}
			
		}
		
	}
	
}
