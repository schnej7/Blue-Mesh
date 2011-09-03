package blueMesh.display;

//import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.os.Handler;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;

public class BlueMeshService {

	public static final boolean DEBUG = true;
	public static final int SUCCESS = 0;
	public static final int ERR_STRING_TO_LARGE = 1;
	public static final int STATE_NONE = 2;
	public static final int STATE_SEARCHING = 3;
	public static final int STATE_CONNECTING = 4;
	public static final int STATE_CONNECTED = 5;
	public static final int NUMBER_OF_AVAILABLE_RADIOS = 8;
	public static final int AVAILABLE = 9;
	public static final int UNAVAILABLE = 10;
	
	public static int numRadiosConnected = 0;
	
	private final Handler mHandler;

	private final BluetoothAdapter mBluetoothAdapter;
    //private ConnectThread mConnectThread;
    //private ConnectedThread mConnectedThread;
	private radioManagerThread rmThread;

	//Constructor
	public BlueMeshService (Context context, Handler handler){
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		mHandler = handler;
	}
	
	//Used to start the service
	public synchronized void start(){
		rmThread = new radioManagerThread();
		rmThread.start();
	}
	
	//Thread used to spawn and handle RadioThreads
	private class radioManagerThread extends Thread{
		
		bluetoothDeviceThread[] deviceThreads = 
				new bluetoothDeviceThread[NUMBER_OF_AVAILABLE_RADIOS];
		int[] threadsInUse = new int[NUMBER_OF_AVAILABLE_RADIOS];
		
		
		
		public void run(){
			
			if (DEBUG) print("Running radioManagerThread");
			//This function is used to manage the bluetooth threads
			//used for connections between devices
			for( int i = 0; i < NUMBER_OF_AVAILABLE_RADIOS; i++){
				threadsInUse[i] = AVAILABLE;
			}
			threadsInUse[0] = UNAVAILABLE;

			deviceThreads[0] = new bluetoothDeviceThread();
			deviceThreads[0].start();

		}
		
		public int print( String outString ){
			
			//Create buffer for string to be converted to bytes to be 
			//displayed by the UI thread
			byte[] buffer = new byte[1024];
			int bytes;
			
			buffer = outString.getBytes();
			
			//Check size of input string
			if(buffer.length > 1024) return ERR_STRING_TO_LARGE;
			
			bytes = buffer.length;
			mHandler.obtainMessage(BlueMeshDisplayActivity.MSG_DEBUG,
					bytes, -1, buffer).sendToTarget();
			
			return SUCCESS;
		}
		
	}
	
	//Thread to search for other bluetooth devices
	private class bluetoothDeviceThread extends Thread{
		
		public void run(){
			if (DEBUG) print("Running searchingRadioThread");
			search();
		}
		
		public void search(){
			print("Searching...");
		}
		
		//Function used to send a message to be displayed by the UI thread
		public int print( String outString ){
			
			//Create buffer for string to be converted to bytes to be 
			//displayed by the UI thread
			byte[] buffer = new byte[1024];
			int bytes;
			
			buffer = outString.getBytes();
			
			//Check size of input string
			if(buffer.length > 1024) return ERR_STRING_TO_LARGE;
			
			bytes = buffer.length;
			mHandler.obtainMessage(BlueMeshDisplayActivity.MSG_DEBUG,
					bytes, -1, buffer).sendToTarget();
			
			return SUCCESS;
		}
	}
	
	
}
