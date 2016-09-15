package com.example.sqqfinalrecord;

import java.io.IOException;

import android.R.integer;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity implements Callback{

	static final String TAG="sqqlog";
	SurfaceView surfaceview;
	SurfaceHolder surfaceHolder;
	Camera camera;
	
	TextView stop;
	boolean recording = true;
	String filename = null;
	//音频部分
	private int frequence = 44100; 
	//CHANNEL_IN_MONO	单声道	
	private int channelConfig = AudioFormat.CHANNEL_IN_STEREO;//CHANNEL_IN_STEREO//CHANNEL_IN_MONO
	private int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		stop = (TextView) findViewById(R.id.tip);
		filename = Environment.getExternalStorageDirectory().getAbsolutePath() + "/b/"
			+"sqq.flv";
		Log.d(TAG,"name"+filename);
		//在这里我们创建一个文件，用于保存录制内容
        //String path =Environment.getExternalStorageDirectory().getAbsolutePath()+"/b/sqq.pcm";
       
		if(FfmpegHelper.init(filename.getBytes())==-1){
			stop.setText("无法连接网络");
			return;
		}
		Button bt = (Button) findViewById(R.id.stop);
		bt.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Log.d(TAG, "onclick");
				FfmpegHelper.flush();
				FfmpegHelper.close();
				recording = false;
			}
		});
		
		surfaceview = (SurfaceView) findViewById(R.id.view);
		surfaceHolder = surfaceview.getHolder();
		surfaceHolder.addCallback(this);
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        
		
		//开始录制
		RecordTask task = new RecordTask();
		task.execute();
	}


	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Log.d(TAG, "oncreate");
		// TODO Auto-generated method stub
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
			camera = Camera.open(0);
		} else {
			camera = Camera.open();
		}
		try {
			if (camera != null) {
				camera.setPreviewDisplay(holder);
				surfaceHolder = holder;
			}
		} catch (IOException e) {
			e.printStackTrace();
			if (camera != null) {
				camera.release();
				camera = null;
			}
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub
		Camera.Parameters p = camera.getParameters();
		p.setPreviewSize(600, 800);
		//每秒 显示20~30帧
		//p.setPreviewFpsRange(30, 30);
		//p.setPictureFormat(PixelFormat.JPEG); // Sets the image format for
												// picture 设定相片格式为JPEG，默认为NV21
		//p.setPreviewFormat(ImageFormat.YV12); // Sets the image format
														// for preview
														// picture，默认为NV21
		// p.setRotation(90);
		camera.setPreviewCallback(new PreviewCallback() {

			@Override
			public void onPreviewFrame(byte[] yuvdata, Camera arg1) {
				//stop.setText(yuvdata.toString());
				Log.d(TAG, "onPreviewFrame");
				if(recording){
					FfmpegHelper.start(yuvdata);
					//FfmpegHelper.flush();
				}
			}

		});
		camera.setParameters(p);
		try {
			camera.setPreviewDisplay(surfaceHolder);
		} catch (Exception E) {

		}
		camera.startPreview();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		if (camera != null) {
			camera.setPreviewCallback(null);
			camera.stopPreview();
			camera.release();
			camera = null;
			surfaceview = null;
			surfaceHolder = null;
		}
		
	}
	
	class RecordTask extends AsyncTask<Void, integer, Void>{

		@Override
		protected Void doInBackground(Void... params) {
			
			int bufferSize = AudioRecord.getMinBufferSize(frequence, channelConfig, audioEncoding);
			//int bufferSize = 1024;
			AudioRecord record = new AudioRecord(MediaRecorder.AudioSource.MIC,
					frequence, channelConfig, audioEncoding , bufferSize);
			//开始录制
			record.startRecording();
			
			byte[] bb = new byte[bufferSize];
			while(recording){
				
				int size = record.read(bb, 0,bufferSize);
				if(size >0){
					
					FfmpegHelper.startAudio(bb,size);
				}
			}
    		if(record.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING){
    			record.stop();
    		}
    		record.release();
			return null;
		}
		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			stop.setText("已经停止了");
		}
		
	}

}
