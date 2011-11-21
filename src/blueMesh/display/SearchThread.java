package blueMesh.display;

import java.io.IOException;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;

/**
 * Class used to search for other devices
 * @author schnej7
 *
 */
public class SearchThread extends Thread {

	// The local server socket
	private final BluetoothServerSocket mmServerSocket;
	private final BluetoothAdapter myBluetoothAdapter;
	private Handler mHandler;
	//private int devicesConnected = 0;
	private RouterThread routerThread;

	/**
	 * Constructor
	 * @param a_mHandler used to pass messages to user
	 * @param mBluetoothAdapter the default BluetoothAdapter
	 */
	public SearchThread(
			Handler a_mHandler, 
			BluetoothAdapter mBluetoothAdapter) {
		
		BluetoothServerSocket tmp = null;
		mHandler = a_mHandler;
		myBluetoothAdapter = mBluetoothAdapter;

		print(Constants.MSG_DEBUG, "Creating SearchThread");

		// Create a new listening server socket
		try {
			tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(
					Constants.NAME, Constants.MY_UUID_SECURE);
		} catch (IOException e) {
			print(Constants.MSG_DEBUG, e.toString());
		}

		mmServerSocket = tmp;

		routerThread = new RouterThread(mHandler);

	}

	/**
	 * Begins looking for other devices
	 * when they are found a connection is attempted
	 */
	public void run() {

		print(Constants.MSG_DEBUG, "BEGIN SearchThread");

		setName("SearchThread" + Constants.mSocketType);

		BluetoothSocket socket = null;

		// Listen to the server socket if we're not connected
		while (true) {
			
			if(this.isInterrupted()){
				routerThread.interrupt();
				print(Constants.MSG_DEBUG, "SearchThread Done");
				return;
			}
			
			if(!myBluetoothAdapter.isEnabled()){
				this.kill();
				print(Constants.MSG_DEBUG, "SearchThread Done, Bluetooth disabled unexpectedly");
				return;
			} 
			
			socket = null;
			
			try {
				// This is a blocking call and will only return on a
				// successful connection or an exception
				print(Constants.MSG_DEBUG, "Trying to accept()");
				socket = mmServerSocket.accept();
			} catch (IOException e) {
				print(Constants.MSG_DEBUG, "accept() failed");
			}

			// If a connection was accepted
			if (socket != null) {
				switch (routerThread.get_connection_state()) {
				case Constants.STATE_READY:
					// Situation normal. Start the connected thread.
					obtain_connection(socket, socket.getRemoteDevice());
					break;
				case Constants.STATE_NONE:
					break;
				case Constants.STATE_FULL:
					try {
						socket.close();
					} catch (IOException e) {
						print(Constants.MSG_DEBUG, "Could not close unwanted socket");
					}
					break;
				case Constants.STATE_BUSY:
					try {
						socket.close();
					} catch (IOException e) {
						print(Constants.MSG_DEBUG, "Could not close unwanted socket");
					}
					break;
				}
			}
		}
	}
	
	/**
	 * Attempts to stop the thread
	 */
	public synchronized void kill(){
		this.cancel();
		this.interrupt();
		print(Constants.MSG_DEBUG, "SearchThread done()");
		////////////////////
		//TODO
		//Stop child threads
		////////////////////
	}

	/**
	 * Attempts to close the serverSocket
	 */
	public void cancel() {
		print(Constants.MSG_DEBUG, "cancel " + this);

		try {
			mmServerSocket.close();
		} catch (IOException e) {
			print(Constants.MSG_DEBUG, "close() of server failed");
		}
	}

	/**
	 * Attempt to use the router thread to obtain a connection
	 * @param socket socket to communicate over
	 * @param device device to connect to
	 */
	public void obtain_connection(
			BluetoothSocket socket, 
			BluetoothDevice device) {

		if (routerThread.get_connection_state() != Constants.STATE_READY) {
			cancel();
		} else {
			routerThread.make_connection(socket, device);
		}

	}
	
	/**
	 * Send a message
	 * @param message message
	 * @param mType message type
	 */
	private synchronized void print(int mType, String message){
		MessageSenderThread msThread = 
				new MessageSenderThread(mHandler, message, mType);
		msThread.start();
	}

}
