package blueMesh.display;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class BlueMeshDisplayActivity extends Activity{
	
	private ArrayAdapter <String> mMessageArray;
	private ListView mMessageView;
	//private TestService mTestService = null;
	private BlueMeshService mBlueMeshService = null;
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);  
        
        setup();
    }
    
    //Used to setup the display and the service
    private void setup(){
    	mMessageArray = new ArrayAdapter<String>(this, R.layout.message);
    	mMessageView = (ListView) findViewById(R.id.ListMessages);
    	mMessageView.setAdapter(mMessageArray);
    	
    	mBlueMeshService = new BlueMeshService( this, mHandler);
    	mBlueMeshService.start();
    	clearDisplay();
    }
    
    //Used to clear the display
    private void clearDisplay(){
    	mMessageArray.clear();
    }
    
    //Handler used for receiving messages from the service
    private final Handler mHandler = new Handler(){
    	@Override
    	public void handleMessage( Message msg ){
    		byte[] writeBuf;
    		String messageString;
	    	switch(msg.what){
	    	case Constants.MSG_DEBUG:
	    		writeBuf = (byte[]) msg.obj;
	    		messageString = new String(writeBuf);
	    		mMessageArray.add(messageString);
	    		break;
	    	case Constants.MSG_NORMAL:
	    		writeBuf = (byte[]) msg.obj;
	    		messageString = new String(writeBuf);
	    		mMessageArray.add(messageString);
	    		break;
	    	}
    	}
    };
}