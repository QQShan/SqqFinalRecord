package com.example.sqqfinalrecord;

public class FfmpegHelper {

	static{
		System.loadLibrary("avutil-54");
    	System.loadLibrary("swresample-1");
    	System.loadLibrary("avcodec-56");
    	System.loadLibrary("avformat-56");
    	System.loadLibrary("swscale-3");
    	System.loadLibrary("postproc-53");
    	System.loadLibrary("avfilter-5");
    	System.loadLibrary("avdevice-56");
		System.loadLibrary("ffmpeghelper");
	}
	
	public static native int init(byte[] filename/*,byte[] path*/);
	
	public static native int start(byte[] yuvdata);

	public static native int startAudio(byte[] audiodata,int size);
	
	public static native int flush();
	
	public static native int close();
}
