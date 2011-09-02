package blueMesh.display;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.ArrayAdapter;
import android.widget.ListView;

@SuppressWarnings("unused")
public class BlueMeshDisplayActivity extends Activity{
	
	public static final int WRITE_MESSAGE = 1;
	
	private ArrayAdapter <String> mMessageArray;
	private ListView mMessageView;
	private TestService mTestService = null;
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        setup();
    }
    
    
    private void setup(){
    	mMessageArray = new ArrayAdapter<String>(this, R.layout.message);
    	mMessageView = (ListView) findViewById(R.id.ListMessages);
    	mMessageView.setAdapter(mMessageArray);
    	
    	mTestService = new TestService( this, mHandler);
    	mTestService.start();
    }
    
    
    private void clearDisplay(){
    	mMessageArray.clear();
    }
    
    private final Handler mHandler = new Handler(){
    	@Override
    	public void handleMessage( Message msg ){
	    	switch(msg.what){
	    	case WRITE_MESSAGE:
	    		byte[] writeBuf = (byte[]) msg.obj;
	    		String messageString = new String(writeBuf);
	    		mMessageArray.add(messageString);
	    		break;
	    		
	    	}
	    }
    };
}