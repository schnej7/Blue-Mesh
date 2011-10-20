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
		} else {
			print_debug("Bluetooth State: " + myBluetoothAdapter.getState());
			print_debug("Bluetooth is not enabled!");
		}
	}

	public synchronized int print_debug(String outString) {

		if (!Constants.DEBUG)
			return Constants.SUCCESS;
		// Create buffer for string to be converted to bytes to be
		// displayed by the UI thread
		byte[] buffer = new byte[1024];
		int bytes;

		buffer = outString.getBytes();

		// Check size of input string
		if (buffer.length > 1024)
			return Constants.ERR_STRING_TO_LARGE;

		bytes = buffer.length;
		mHandler.obtainMessage(Constants.MSG_DEBUG, bytes, -1, buffer)
				.sendToTarget();

		return Constants.SUCCESS;
	}

}
