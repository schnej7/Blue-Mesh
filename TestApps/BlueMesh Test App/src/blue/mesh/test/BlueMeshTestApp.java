package blue.mesh.test;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import blue.mesh.BlueMeshService;
import blue.mesh.BlueMeshServiceBuilder;

import java.util.UUID;

//An Android test app to check basic functionality of the BlueMesh service
public class BlueMeshTestApp extends Activity{
	
	private BlueMeshService bmsMain;
	//second BMS which will write back anything it reads
	private BlueMeshService bmsAlt;
	private ReadThread readThread;
	//private WriteThread writeThread;
	
	private final String TAG = "BlueMeshTestApp";
	
	//constant sets of bytes which can be recognized as successful tests
	private final byte[] testBytes1 = {1, 2, 3, 4};
	private final byte[] testBytes2 = {5, 6, 7, 8};
	
	private boolean test1 = false;
	private boolean test2 = false;
	
	//Handler to determine when tests have been completed
	private final Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg){
			byte[] bytes = (byte[]) msg.obj;
			if(bytes == testBytes1)
				test1 = true;
			else if(bytes == testBytes2)
				test2 = true;
		}
	};
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		//create the test BMS objects and launch
		try{
			BlueMeshServiceBuilder bmsb = new BlueMeshServiceBuilder();
			bmsMain = bmsb.bluetooth(true).build();
			/*
			bmsb = new BlueMeshServiceBuilder();
			bmsAlt = bmsb.uuid(UUID.fromString("a8b8-81c8-7971-8a8d-6a98-317c-da73-8d0f")).bluetooth(true).build();
			*/
		}
		catch(NullPointerException e){
			Log.e(TAG, "BlueMeshService Constructor failed.");
			return;
		}
		
		bmsMain.launch();
		//bmsAlt.launch();
	}
	
	//start the thread which connects the two devices, then send the test packets
	public void onStart(){
		readThread = new ReadThread();
		//writeThread = new WriteThread();
		
		readThread.start();
		//writeThread.start();
		
		//bmsAlt.write(testBytes1);
		bmsMain.write(testBytes1);
		bmsMain.write(testBytes2);
		
		
		//construct test feedback string
		String s = "Test 1: ";
		if(test1) s += "passed\n";
		else s += "failed\n";
		
		s += "Test 2: ";
		if(test2) s += "paseed";
		else s += "failed";
		
		//display results
		Toast.makeText(this, s, Toast.LENGTH_LONG).show();
	}
	
	public void onDestroy(){
		super.onDestroy();
		readThread.interrupt();
		//writeThread.interrupt();
		
		if(bmsMain != null){
			Log.d(TAG, "disconnecting bmsMain");
			bmsMain.disconnect();
		}
		if(bmsAlt != null){
			Log.d(TAG, "disconnecting bsmAlt");
			bmsAlt.disconnect();
		}
	}
	
	//Thread which handles reading and writing by the test BMS objects
	//bmsMain checks for packets, then checks those found against the test arrays
	//bmsAlt returns anything it reads, allowing for additional tests
	private class ReadThread extends Thread{
		public void run(){
			Looper.myLooper();
			Looper.prepare();
			
			while(!this.isInterrupted()){
				byte[] readMain = null;//, readAlt = null;
				readMain = bmsMain.pull();
				//readAlt = bmsAlt.pull();
				
				if(readMain == null){// && readAlt == null){
					try{
						sleep(100);
					}
					catch(InterruptedException e){
						Log.e(TAG, "sleep() failed", e);
					}
				}
				
				//handle bytes read as appropriate
				else{
					if(readMain != null)
						mHandler.obtainMessage(0, readMain.length, -1, readMain).sendToTarget();
					//if(readAlt != null)
					//	bmsAlt.write(readAlt);
				}
			}
			
			Log.d(TAG, "testThread interrupted");
		}
	}
}
