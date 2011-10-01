package blueMesh.display;

//import android.bluetooth.BluetoothAdapter;
import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;


@SuppressWarnings("unused")
public class BlueMeshService {

	//Constants
	public static final boolean DEBUG = true;
	public static final int FAIL = -1;
	public static final int SUCCESS = 0;
	public static final int ERR_STRING_TO_LARGE = 1;
	public static final int STATE_NONE = 2;
	public static final int STATE_SEARCHING = 3;
	public static final int STATE_CONNECTING = 4;
	public static final int STATE_CONNECTED = 5;
	public static final int NUMBER_OF_AVAILABLE_RADIOS = 8;
	public static final int AVAILABLE = 9;
	public static final int UNAVAILABLE = 10;
	public static final int INT_OUT_OF_RANGE = 11;
	
	//Number of devices connected
	private static int numRadiosConnected = 0;
	private static String myID = null;
	//List of devices connected
	private static List <String> connectedDeviceIDs;
	
	private final Handler mHandler;

	private final BluetoothAdapter mBluetoothAdapter;
    //private ConnectThread mConnectThread;
    //private ConnectedThread mConnectedThread;
	private radioManagerThread rmThread;

	//Constructor
	public BlueMeshService (Context context, Handler handler){
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		
		if( mBluetoothAdapter == null){
			//Bluetooth is not supported
			return;
		}
		
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
			
			if (DEBUG) print_debug("Running radioManagerThread");
			//This function is used to manage the bluetooth threads
			//used for connections between devices
			
			//All threads are available
			for( int i = 0; i < NUMBER_OF_AVAILABLE_RADIOS; i++){
				threadsInUse[i] = AVAILABLE;
			}
			
			////////////////////////
			// TO DO: Set myID
			///////////////////////
			myID = "ME";
			
			//Check if myID is in connectedDeviceIDs
			//If it is not, then add it
			boolean inList = false;
			for( int i = 0; i < connectedDeviceIDs.size(); i++){
				if( connectedDeviceIDs.get(i) == myID){
					inList = true;
					break;
				}
			}	
			if( !inList ){
				connectedDeviceIDs.add(myID);
			}

			int tempReturnVal;
			
			tempReturnVal = start_search_on_thread( 0 );
		}
		
		public int start_search_on_thread( int searchThread ){
			if (DEBUG){
				print_debug ("start_search_on_thread " + 
			String.valueOf(searchThread));
			}
			
			if( searchThread > NUMBER_OF_AVAILABLE_RADIOS &&
					searchThread < 0){
				return INT_OUT_OF_RANGE;
			}
			else if( threadsInUse[searchThread] == UNAVAILABLE ){
				return UNAVAILABLE;
			}
			else{
				threadsInUse[searchThread] = UNAVAILABLE;

				deviceThreads[searchThread] = new bluetoothDeviceThread( 
						searchThread );
				deviceThreads[searchThread].start();
				
				return SUCCESS;
			}
		}
		
		public int kill_search_on_thread( int searchThread){
			if (DEBUG){
				print_debug ("kill_search_on_thread " + 
			String.valueOf(searchThread));
			}
			
			if( searchThread > NUMBER_OF_AVAILABLE_RADIOS &&
					searchThread < 0){
				return INT_OUT_OF_RANGE;
			}
			else {
				deviceThreads[searchThread].stop();
				
				threadsInUse[searchThread] = AVAILABLE;
				return SUCCESS;
			}
		}
		
	}
	
	//Thread to search for other bluetooth devices
	private class bluetoothDeviceThread extends Thread{
		
		private int deviceID;
		
		public bluetoothDeviceThread ( int a_deviceID ){
			deviceID = a_deviceID;
		}
		
		public void run(){
			if (DEBUG) print_debug("Running searchingRadioThread");
			search();
		}
		
		public void search(){
			print_debug("Searching...");
		}
		
		private int killable(){
			if(Thread.holdsLock(mHandler)){
				return FAIL;
			}
			else{
				return SUCCESS;
			}
		}
		
		//Function used to send a message to be displayed by the UI thread
	}
	
	public synchronized int print_debug( String outString ){
		
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
