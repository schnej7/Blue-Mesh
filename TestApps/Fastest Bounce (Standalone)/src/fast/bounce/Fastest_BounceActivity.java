package fast.bounce;

import blue.mesh.BlueMeshService;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.view.KeyEvent;
import android.widget.Toast;
import java.lang.Thread;
import android.os.Handler;

public class Fastest_BounceActivity extends Activity {
    /** Called when the activity is first created. */
    
	private BlueMeshService bms;
	private Toast pad; //Used for writing out. See pad_out.
	private boolean pitcher; //Determines whether the program is throwing a bounce or catching
	private byte[] message = new byte[256]; //Arbitrary sending string
	private long StartTime;
	private long EndTime;
	private ReadThread Reader;
	
	// private backThread mythread;//Thread Patch
    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        pitcher = false;
        pad_out("Toast");
        
        //Thread Patch
        //mythread = new backThread();
        //mythread.start();
        
        pad_out("Starting BlueMesh");
		try{
			bms = new BlueMeshService(); //Manifest Problem
		}
		catch(NullPointerException e){
			pad_out("Bluetooth not enabled");
			finish(); //Kills Activity
		}
		pad_out("Launching BlueMesh");
		bms.launch();
    	//catcher(); //Hangs up Here
		Reader = new ReadThread();
		Reader.start();
        
		return;
        //Followed by onStart
         
    }
    
    /*public boolean onKeyDown(int keyCode, KeyEvent event) {
    	finish();
    	return true;
    }*/
    
    /*private class backThread extends Thread{
       //An attempt at a thread-patch, with the idea that BlueMeshService() would run in a thread. It doesn't. 
    	
    	public void run(){
    		Looper.prepare();
    		pad_out("Starting BlueMesh");
    		try{
    			bms = new BlueMeshService(); //Problem WITH thread, too.
    		}
    		catch(NullPointerException e){
    			pad_out("Bluetooth not enabled");
    			finish(); //Kills Activity
    		}
    		//pad_out("Launching BlueMesh");
    		//bms.launch()
        	//catcher();
    		Looper.loop();
       
    	}
    }*/
    
    private class ReadThread extends Thread {
    	
    	boolean stop = false;
    	
    	public void run(){
    		Looper.myLooper();
    	    Looper.prepare();
    	    while (true){
    	    	if(this.isInterrupted()){
    	    		stop = true;
    	    	}
	    		if(stop){
	    			//Log.d(TAG, "readThread interrupted");
	    			return;
	    		}
	    		byte bytes[] = null;
	    		bytes = bms.pull();
	    		if( bytes != null && !pitcher){
	    			bms.write(bytes);
	    		} else if (bytes != null && pitcher){
	    			mHandler.obtainMessage(0, bytes.length, -1, bytes).sendToTarget();
	    		}
    	}}}
    
    private final Handler mHandler = new Handler(){
        @Override
        public void handleMessage( Message msg ){
			EndTime = System.nanoTime();
			String output = String.valueOf(EndTime-StartTime);
			pad_out(output);
			pitcher = false;
        	}
       };
    
   /* private void catcher(){ //Catches continously until pitcher = true
		message = "derp";
		pad_out("Catching Flu");
		while (!pitcher){
			byte bytes[] = bms.pull();
			if( bytes != null){ //Edit this to check for "herp" later.
				bms.write(message.getBytes());
			}}
		return;
    }*/
    
    public boolean onKeyDown(int keyCode, KeyEvent event) { //This runs on any button pressed
		//Throw Command
    	pitcher = true;
    	StartTime = System.nanoTime();
		bms.write(message);
		
		/*while (true){
			byte bytes[] = bms.pull();
			if (bytes != null){ //Edit this to check for "derp" later.
				long EndTime = System.nanoTime();
				String output = String.valueOf(EndTime-StartTime);
				pad_out(output);
				break;
			}}
		pitcher = false;*/
		//catcher();
		return true; //Do not propegate
    }
    
	private void pad_out(CharSequence text){ //Displays a message using toast
		Context context = getApplicationContext();
		
    	pad = Toast.makeText(context, text, Toast.LENGTH_LONG);
    	pad.show();
    	return;
	}
}