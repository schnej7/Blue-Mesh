package blue.mesh.test;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import blue.mesh.BlueMeshService;
import blue.mesh.BlueMeshServiceBuilder;

//An Android test app to check basic functionality of the BlueMesh service
public class BlueMeshTestApp extends Activity{
	
	String T1F = "Test1 Failed";
	
	private BlueMeshService bmsMain;
	//second BMS which will write back anything it reads
	private BlueMeshService bmsAlt;
	private ReadThread readThread;
	
	boolean done = true;
	boolean sender = false;
	
	protected Context context = this;
	//private WriteThread writeThread;
	
	private final String TAG = "BlueMeshTestApp";
	
	//constant sets of bytes which can be recognized as successful tests
	private final byte[] testBytes1 = "Test1".getBytes();
	private final byte[] test1Response = "Test1Response".getBytes();
	
	protected synchronized void change_sender( boolean value ){
		sender = value;
	}
	
	protected synchronized void change_done( boolean value ){
		done = value;
	}
	
	//Handler to determine when tests have been completed
	private final Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg){
			byte[] bytes = (byte[]) msg.obj;
			String msgString = new String(bytes);
			if(msgString.equals(T1F)){
				if( !done ){
					Toast.makeText(context, "Test 1 Failed: timed out", Toast.LENGTH_LONG).show();
					final Button buttonT1 = (Button) findViewById(R.id.btnTest1);
					buttonT1.setTextColor(Color.RED);
					enableAllButtons(true);
					change_sender(false);
					change_done(true);
				}
			}
			else if(msgString.equals(new String(testBytes1))){
				if( !sender ){
					Toast.makeText(context, "Test 1 bouncing " + msgString, Toast.LENGTH_LONG).show();
					bmsMain.write(test1Response);
				}
			}
			else if(msgString.equals(new String(test1Response))){
				Toast.makeText(context, "Test 1 Passed " + msgString, Toast.LENGTH_LONG).show();
				final Button buttonT1 = (Button) findViewById(R.id.btnTest1);
				buttonT1.setTextColor(Color.BLACK);
				enableAllButtons(true);
				change_sender(false);
				change_done(true);
			}
			else{
				Toast.makeText(context, "Test 1 Failed: " + msgString, Toast.LENGTH_LONG).show();
				final Button buttonT1 = (Button) findViewById(R.id.btnTest1);
				buttonT1.setTextColor(Color.RED);
				enableAllButtons(true);
				change_sender(false);
				change_done(true);
			}
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
	}
	
	public void enableAllButtons( Boolean enabled ){
		final Button buttonT1 = (Button) findViewById(R.id.btnTest1);
		buttonT1.setEnabled(enabled);
	}
	
	//start the thread which connects the two devices, then send the test packets
	public void onStart(){
		super.onStart();
		
		final CheckBox chkBlueMeshStarted = (CheckBox) findViewById(R.id.chkBMSStarted);
		//If the constructor failed...
		if( bmsMain == null ){
			
			final Button buttonQuit = (Button) findViewById(R.id.btnQuit);
	        buttonQuit.setOnClickListener(new View.OnClickListener() {
	            public void onClick(View v) {
	            	finish();
	            }
	        });
	            
			chkBlueMeshStarted.setChecked(false);
			chkBlueMeshStarted.setTextColor(Color.RED);
			return;
		}
		
		bmsMain.launch();
		
		chkBlueMeshStarted.setChecked(true);
		
		readThread = new ReadThread();		
		readThread.start();
		
		final Button buttonT1 = (Button) findViewById(R.id.btnTest1);
        buttonT1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	change_done(false);
            	Toast.makeText(context, "Writing test bytes", Toast.LENGTH_LONG).show();
            	enableAllButtons( false );
            	change_sender(true);
            	
            	bmsMain.write(testBytes1);
            	Timer t = new Timer();
            	t.schedule(
            	    new TimerTask()
            	    {
            	        public void run()
            	        {
            	        	change_sender(false);
            	        	mHandler.obtainMessage(0, T1F.getBytes().length, -1, T1F.getBytes()).sendToTarget();
            	        }
            	    },
            	    2000); // run in two seconds
            }
        });
        
		final Button buttonQuit = (Button) findViewById(R.id.btnQuit);
        buttonQuit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	cleanUp();
            	finish();
            }
        });
        
		Toast.makeText(context, "Trying to connect", Toast.LENGTH_LONG).show();
		
	}
	
	public void cleanUp(){
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
				byte[] readMain = null;
				
				//Pull can throw a null pointer exception
				//if bms has been stopped
				try{
					readMain = bmsMain.pull();
				}
				catch( NullPointerException e ){
					Log.e(TAG, "Could not pull", e);
					break;
				}
				
				//Try again
				if(readMain == null){
					try{
						sleep(100);
					}
					catch(InterruptedException e){
						Log.e(TAG, "sleep() failed", e);
					}
				}
				//handle bytes read as appropriate
				else{
					mHandler.obtainMessage(0, readMain.length, -1, readMain).sendToTarget();
				}
			}
			Log.d(TAG, "testThread interrupted");
		}
	}
}
