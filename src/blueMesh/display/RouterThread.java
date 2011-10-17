package blueMesh.display;

import java.io.IOException;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;

public class RouterThread extends Thread {

	private ConnectedThread connectedThreads[];
	private Handler mHandler;
	private static int connectionState;
	private ConnectionSetupThread connectionSetupThread;
	private static int radiosInUse;

	public synchronized void kill_connection( int connectionNumber ){
		connectedThreads[connectionNumber] = null;
	}
	
	public synchronized void set_connection_state(int a_connectionState) {
		connectionState = a_connectionState;
	}

	public synchronized int get_connection_state() {
		return connectionState;
	}

	public RouterThread(Handler aHandler) {
		mHandler = aHandler;
		connectionState = Constants.STATE_NONE;
		connectionSetupThread = null;
		radiosInUse = 0;
		connectedThreads = 
				new ConnectedThread[Constants.NUMBER_OF_AVAILABLE_RADIOS];
	}
	
	public void run(){
		
		///////////////////////////
		//TODO
		//This thread needs to rout data coming in
		//and going out
		//////////////////////////
	}
	
	public synchronized int make_connection( BluetoothSocket socket,
			BluetoothDevice device ){
		
		if( radiosInUse >= Constants.NUMBER_OF_AVAILABLE_RADIOS){
			return Constants.FAIL;
		}
		
		//The connection setup thread is busy
		if( connectionState != Constants.STATE_READY){
			return Constants.FAIL;
		}
		
		connectionState = Constants.STATE_BUSY;
		
		if( connectionSetupThread != null ){
			connectionSetupThread.cancel();
		}
		
		connectionSetupThread = new ConnectionSetupThread(device, this);
		connectionSetupThread.start();
		
		return Constants.SUCCESS;
	}

	private class ConnectionSetupThread extends Thread {

		private RouterThread myParent;
		private BluetoothSocket mmSocket;

		public ConnectionSetupThread(BluetoothDevice device, RouterThread parent) {
			myParent = parent;
			BluetoothSocket tmp = null;

			// Get a BluetoothSocket for a connection with the
			// given BluetoothDevice
			try {
				tmp = device.createRfcommSocketToServiceRecord(
								Constants.MY_UUID_SECURE);
			} catch (IOException e) {
				print_debug("create() failed");
			}
			mmSocket = tmp;
		}

		public void run() {
			print_debug("BEGIN ConnectionSetupThread");
			setName("ConnectThread");

			// Make a connection to the BluetoothSocket
			try {
				// This is a blocking call and will only return on a
				// successful connection or an exception
				mmSocket.connect();
			} catch (IOException e) {
				// Close the socket
				try {
					mmSocket.close();
				} catch (IOException e2) {
					print_debug("unable to close() socket during " +
							"connection failure");
				}
				/////////////////
				//TODO
				//connectionFailed();
				//Do something on a failed connection
				/////////////////
				synchronized (RouterThread.this) {
					connectionState = Constants.STATE_READY;
				}
				return;
			}

			// Reset the ConnectThread because we're done
			synchronized (RouterThread.this) {
				connectionSetupThread = null;
			}

			////////////////////////
			//TODO
			// Start the connected thread
			// connected(mmSocket, mmDevice, mSocketType);
			////////////////////////
			
			for( int i = 0; i < Constants.NUMBER_OF_AVAILABLE_RADIOS; i++){
				if (connectedThreads[i] == null){
					connectedThreads[i] = 
							new ConnectedThread(myParent, i);
					connectedThreads[i].start();
					break;
				}
			}
			
			synchronized (RouterThread.this) {
				connectionState = Constants.STATE_READY;
			}
			
			
		}
		
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                print_debug("connectionSetupThread close() failed");
            }
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
		mHandler.obtainMessage(BlueMeshDisplayActivity.MSG_DEBUG, bytes, -1,
				buffer).sendToTarget();

		return Constants.SUCCESS;
	}

}
