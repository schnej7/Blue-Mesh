package fast.bounce;

import blue.mesh.BlueMeshService;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.Toast;

public class Fastest_BounceActivity extends Activity {
    /** Called when the activity is first created. */
    
	private BlueMeshService bms;
	private Toast pad; //Used for writing out. See pad_out.
	private boolean pitcher; //Determines whether the program is throwing a bounce or catching
	private String message;
    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        pitcher = false;
        pad_out("Toast");
        pad_out("Starting BlueMesh");
		try{
			bms = new BlueMeshService();
		}
		catch(NullPointerException e){
			pad_out("Bluetooth not enabled");
			finish(); //Kills Activity
		}
		//pad_out("Launching BlueMesh");
		//bms.launch()
    	//catcher();
        
		return;
        //Followed by onStart
    }
    
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	finish();
    	return true;
    }
    
    /*private void catcher(){ //Catches continously until pitcher = true
		message = "derp";
		while (!pitcher){
			byte bytes[] = bms.pull();
			if( bytes != null){ //Edit this to check for "herp" later.
				bms.write(message.getBytes());
			}}
		return;
    }
    
    public boolean onKeyDown(int keyCode, KeyEvent event) { //This runs on any button pressed
		//Throw Command
    	pitcher = true;
		message = "herp"; //Arbitrary send string
		long StartTime = System.nanoTime();
		bms.write(message.getBytes());
		while (true){
			byte bytes[] = bms.pull();
			if (bytes != null){ //Edit this to check for "derp" later.
				long EndTime = System.nanoTime();
				String output = String.valueOf(EndTime-StartTime);
				pad_out(output);
				break;
			}}
		pitcher = false;
		catcher();
		return true; //Do not propegate
    }*/
    
	private void pad_out(CharSequence text){ //Displays a message using toast
		Context context = getApplicationContext();
		
    	pad = Toast.makeText(context, text, Toast.LENGTH_LONG);
    	pad.show();
    	return;
	}
}