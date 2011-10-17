package blueMesh.display;

import java.io.IOException;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;

public class SearchThread extends Thread {

	// The local server socket
	private final BluetoothServerSocket mmServerSocket;
	private Handler mHandler;
	private int devicesConnected = 0;
	private RouterThread routerThread;

	public SearchThread(Handler a_mHandler, BluetoothAdapter mBluetoothAdapter) {
		BluetoothServerSocket tmp = null;
		mHandler = a_mHandler;

		// Create a new listening server socket
		try {
			tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(
					Constants.NAME, Constants.MY_UUID_SECURE);
		} catch (IOException e) {
			print_debug("socket listen() failed");
		}
		mmServerSocket = tmp;
		
		routerThread = new RouterThread(mHandler);
		routerThread.run();
	}

	public void run() {
		if (Constants.DEBUG)
			print_debug("BEGIN mAcceptThread " + this);

		setName("SearchThread" + Constants.mSocketType);

		BluetoothSocket socket = null;

		// Listen to the server socket if we're not connected
		while (devicesConnected < Constants.NUMBER_OF_AVAILABLE_RADIOS) {
			try {
				// This is a blocking call and will only return on a
				// successful connection or an exception
				socket = mmServerSocket.accept();
			} catch (IOException e) {
				print_debug("accept() failed");
				break;
			}

			// If a connection was accepted
			if (socket != null) {
				switch (routerThread.get_connection_state()) {
				case Constants.STATE_READY:
					// Situation normal. Start the connected thread.
					obtain_connection(socket, 
							socket.getRemoteDevice());
					break;
				case Constants.STATE_NONE:
				case Constants.STATE_FULL:
					try {
						socket.close();
					} catch (IOException e) {
						print_debug("Could not close unwanted socket");
					}
					break;
				case Constants.STATE_BUSY:
					try {
						socket.close();
					} catch (IOException e) {
						print_debug("Could not close unwanted socket");
					}
					break;
				}
			}
		}
		if (Constants.DEBUG)
			print_debug("END mAcceptThread");

	}

	public void cancel() {
		if (Constants.DEBUG)
			print_debug("cancel " + this);
		try {
			mmServerSocket.close();
		} catch (IOException e) {
			print_debug( "close() of server failed");
		}
	}

	public synchronized int print_debug(String outString) {

		// Create buffer for string to be converted to bytes to be
		// displayed by the UI thread
		byte[] buffer = new byte[1024];
		int bytes;

		buffer = outString.getBytes();

		// Check size of input string
		if (buffer.length > 1024)
			return Constants.ERR_STRING_TO_LARGE;

		bytes = buffer.length;
		mHandler.obtainMessage(Constants.MSG_DEBUG, bytes, -1,
				buffer).sendToTarget();

		return Constants.SUCCESS;
	}
	
	public void obtain_connection( BluetoothSocket socket,
			BluetoothDevice device ){
		
		if(routerThread.get_connection_state() != Constants.STATE_READY){
			cancel();
		}
		else{
			routerThread.make_connection(socket, device);
		}
		
	}

}
