package blueMesh.display;

import android.content.Context;
import android.os.Handler;
import android.bluetooth.BluetoothAdapter;
import blueMesh.display.Constants;

public class BlueMeshService {

	// private static String myID = null;

	private final Handler mHandler;
	
	private final BluetoothAdapter myBluetoothAdapter;
	// private ConnectThread mConnectThread;
	// private ConnectedThread mConnectedThread;
	private SearchThread searchThread;

	// Constructor
	public BlueMeshService(Context context, Handler handler) {
		
		myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		mHandler = handler;

		if (myBluetoothAdapter == null) {
			print_debug("myBluetoothAdapter is null");
		} else {
			print_debug("myBluetoothAdapter is non-null");
		}

	}

	// Used to start the service
	public void start() {

		if (myBluetoothAdapter == null) {
			print_debug("No bluetooth hardware...");
			
			return;
			// //////////////////////
			// TODO
			// Exit the program
			// Bluetooth is not supported on this device
			// //////////////////////
		}

		if (myBluetoothAdapter.isEnabled()) {
			print_debug("Bluetooth is enabled!");
			print_debug("Starting Service");
			searchThread = new SearchThread(mHandler, myBluetoothAdapter);
			searchThread.start();
			stop();
		} else {
			print_debug("Bluetooth State: " + myBluetoothAdapter.getState());
			print_debug("Bluetooth is not enabled!");
			stop();
		}
	}
	
	private int stop(){
		/////////////////////
		//TODO
		//Stop the service
		/////////////////////
		searchThread.done();
		searchThread.interrupt();
		
		return Constants.SUCCESS;
	}
	
	private synchronized void print_debug(String message){
		print(Constants.MSG_DEBUG, message);
	}
	
	private synchronized void print(int mType, String message){
		MessageSenderThread msThread = 
				new MessageSenderThread(mHandler, message, mType);
		msThread.start();
	}
}
