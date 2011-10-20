package blueMesh.display;

import android.content.Context;
import android.os.Handler;
import android.bluetooth.BluetoothAdapter;
import blueMesh.display.Constants;


public class BlueMeshService{

	//private static String myID = null;
	
	private final Handler mHandler;

	private final BluetoothAdapter mBluetoothAdapter;
    //private ConnectThread mConnectThread;
    //private ConnectedThread mConnectedThread;
	private SearchThread searchThread;

	//Constructor
	public BlueMeshService (Context context, Handler handler){
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();		
		mHandler = handler;
		
		print_debug("Service constructer");
		
		if(mBluetoothAdapter == null){
			print_debug("mBluetoothAdapter is null");
		}
		else{
			print_debug("mBluetoothAdapter is non-null");
		}
        
	}
	
	//Used to start the service
	public void start(){
		
		if (mBluetoothAdapter == null){
			print_debug("No bluetooth hardware...");
			return;
		}

        if (mBluetoothAdapter.isEnabled()) {
            print_debug("Bluetooth is enabled!");
    		print_debug("Starting Service");
    		searchThread = new SearchThread(mHandler, mBluetoothAdapter);
    		searchThread.start();
        }
        else{
        	print_debug("Bluetooth State: " + mBluetoothAdapter.getState());
        	print_debug("Bluetooth is not enabled!");
        }
	}
	
	
	public synchronized int print_debug( String outString ){
		
		//Create buffer for string to be converted to bytes to be 
		//displayed by the UI thread
		byte[] buffer = new byte[1024];
		int bytes;
		
		buffer = outString.getBytes();
		
		//Check size of input string
		if(buffer.length > 1024) return Constants.ERR_STRING_TO_LARGE;
		
		bytes = buffer.length;
		mHandler.obtainMessage(Constants.MSG_DEBUG,
				bytes, -1, buffer).sendToTarget();
		
		return Constants.SUCCESS;
	}
	
}
