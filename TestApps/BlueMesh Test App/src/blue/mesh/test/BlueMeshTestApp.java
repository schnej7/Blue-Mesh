package blue.mesh.test;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import blue.mesh.BlueMeshService;
import blue.mesh.BlueMeshServiceBuilder;

//An Android test app to check basic functionality of the BlueMesh service
public class BlueMeshTestApp extends Activity{
	
	private BlueMeshService bmsMain;
	//second BMS which will write back anything it reads
	private BlueMeshService bmsAlt;
	private ReadThread readThread;
	
	boolean done = false;
	
	protected Context context = this;
	//private WriteThread writeThread;
	
	private final String TAG = "BlueMeshTestApp";
	
	//constant sets of bytes which can be recognized as successful tests
	private final byte[] testBytes1 = "Test1".getBytes();
	
	protected synchronized void change_done( boolean value ){
		done = value;
	}
	
	//Handler to determine when tests have been completed
	private final Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg){
			byte[] bytes = (byte[]) msg.obj;
			if(bytes.toString().equals(testBytes1.toString())){
				Toast.makeText(context, "Test 1 Passed " + bytes.toString(), Toast.LENGTH_LONG).show();
			}
			else{
				Toast.makeText(context, "Test 1 Failed: " + bytes.toString(), Toast.LENGTH_LONG).show();
			}
			change_done(true);
		}
			
	};
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
        setContentView(R.layout.main);
		
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
		super.onStart();
		
		readThread = new ReadThread();		
		readThread.start();
		
		final Button buttonT1 = (Button) findViewById(R.id.btnTest1);
        buttonT1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	bmsMain.write(testBytes1);
            	Toast.makeText(context, "Writing test bytes", Toast.LENGTH_LONG).show();
            }
        });
        
		final Button buttonQuit = (Button) findViewById(R.id.btnQuit);
        buttonQuit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	readThread.interrupt();
            	bmsMain.disconnect();
            	finish();
            }
        });
        
		Toast.makeText(context, "Trying to connect", Toast.LENGTH_LONG).show();
		
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
