package blueMesh.display;

import android.os.Handler;
import android.bluetooth.BluetoothAdapter;
import blueMesh.display.Constants;

/**
 * Main class of BlueMesh service
 * @author schnej7
 *
 */
public class BlueMeshService {

	// private static String myID = null;

	private final Handler mHandler;
	
	private final BluetoothAdapter myBluetoothAdapter;
	// private ConnectThread mConnectThread;
	// private ConnectedThread mConnectedThread;
	private SearchThread searchThread;

	/**
	 * Constructor
	 * @param handler used to pass messages to the user
	 */
	public BlueMeshService(Handler handler) {
		
		myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		mHandler = handler;

		if (myBluetoothAdapter == null) {
			print(Constants.MSG_DEBUG, "myBluetoothAdapter is null");
		} else {
			print(Constants.MSG_DEBUG, "myBluetoothAdapter is non-null");
		}

	}

	/**
	 * Used to start the service
	 */
	public void start() {

		this.stop();
		
		if (myBluetoothAdapter == null) {
			print(Constants.MSG_DEBUG, "No bluetooth hardware...");
			
			return;
		}
		
		//Try to enable bluetooth if it is not enabled
		if( !myBluetoothAdapter.isEnabled()){
			print(Constants.MSG_DEBUG, "Trying to enable Bluetooth...");
			if(!myBluetoothAdapter.enable()){
				print(Constants.MSG_DEBUG, "Bluetooth State: " + myBluetoothAdapter.getState());
				print(Constants.MSG_DEBUG, "Bluetooth is not enabled!");
				stop();
			}
			while(!myBluetoothAdapter.isEnabled()){}
		}

		if (myBluetoothAdapter.isEnabled()) {
			print(Constants.MSG_DEBUG, "Bluetooth is enabled!");
			print(Constants.MSG_DEBUG, "Starting Service");
			searchThread = new SearchThread(mHandler, myBluetoothAdapter);
			searchThread.start(); 
		} else {
			print(Constants.MSG_DEBUG, "Bluetooth State: " + myBluetoothAdapter.getState());
			print(Constants.MSG_DEBUG, "Bluetooth is not enabled!");
			stop();
		}
	}
	
	/**
	 * Used to stop the service
	 * @return SUCCESS
	 */
	public int stop(){
		if(searchThread != null && searchThread.isAlive()){
			searchThread.kill();
		}
		print(Constants.MSG_DEBUG, "Stopped");
		
		return Constants.SUCCESS;
	}
	
	private synchronized void print(int mType, String message){
		MessageSenderThread msThread = 
				new MessageSenderThread(mHandler, message, mType);
		msThread.start();
	}
}
