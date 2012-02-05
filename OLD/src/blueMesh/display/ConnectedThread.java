package blueMesh.display;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;


import android.bluetooth.BluetoothSocket;
import android.os.Handler;

/**
 * Class used to send data over sockets obtained by other Bluetooth devices
 * @author schnej7
 *
 */
public class ConnectedThread extends Thread {

	private Handler mHandler;
	private RouterThread myParent;
	private int myConnectionID;
	private BluetoothSocket mySocket;
	private InputStream in;
	private OutputStream out;
	
	public int getConnectionID(){
		return myConnectionID;
	}

	/**
	 * Used to close input and output streams and close the Bluetooth socket
	 */
	public void clean_up() {
		try {
			in.close();
			out.close();
		} catch (IOException e) {
			print(Constants.MSG_DEBUG, "failed to close() input output streams");
		}
		try {
			mySocket.close();
		} catch (IOException e) {
			print(Constants.MSG_DEBUG, "failed to close() mySocket");
		}
	}

	/**
	 * Constructor
	 * @param aHandler used to send data back to the user
	 * @param parent reference to the parent RouterThread
	 * @param ID the ID of the ConnectedThread assigned by its parent
	 * @param socket the socket use for input and output to another device
	 */
	public ConnectedThread(Handler aHandler, RouterThread parent, int ID,
			BluetoothSocket socket) {

		mHandler = aHandler;
		myParent = parent;
		myConnectionID = ID;
		print(Constants.MSG_DEBUG, "create ConnectedThread:");
		mySocket = socket;
		InputStream tmpIn = null;
		OutputStream tmpOut = null;

		// Get the BluetoothSocket input and output streams
		try {
			tmpIn = socket.getInputStream();
			tmpOut = socket.getOutputStream();
		} catch (IOException e) {
			print(Constants.MSG_DEBUG, "temp sockets not created");
		}

		in = tmpIn;
		out = tmpOut;
	}

	/**
	 * Begins the thread which listens for data coming in through in sends all read bytes to the RouterThread to be handled
	 */
	public void run() {

		print(Constants.MSG_DEBUG, "BEGIN ConnectedThread");
		byte[] buffer = new byte[1024];
		int bytes;

		// Keep listening to the InputStream while connected
		while (true) {
			try {
				// Read from the InputStream
				bytes = in.read(buffer);

				myParent.route_bytes(myConnectionID, buffer, bytes);

			} catch (IOException e) {
				print(Constants.MSG_DEBUG, "connection " + myConnectionID + " disconnected");

				break;
			}
		}

		this.clean_up();

		myParent.kill_connection(myConnectionID);

	}
	
	/**
	 * Attaches a timestamp to the array of bytes to send and then sends it using write()
	 * @param buffer bytes to send generated from this device
	 */
	public void send(byte[] buffer){
		
		//Attach the time to the beginning of the byte array
		//In order to give it a unique time stamp
		
		Integer mili = Calendar.MILLISECOND;
		mili.byteValue();
		
		byte[] message= new byte[buffer.length + 1];
		System.arraycopy(mili, 0, message, 0, 1);
		System.arraycopy(buffer, 0, message, 1, buffer.length);
		
		write(message);
	}

	/**
	 * Writes array of bytes to out
	 * @param buffer bytes to write
	 */
	public void write(byte[] buffer) {
		try {
			out.write(buffer);

			// Share the sent message back to the UI Activity
			mHandler.obtainMessage(Constants.MSG_SENT, -1, -1, buffer)
					.sendToTarget();
		} catch (IOException e) {
			print(Constants.MSG_DEBUG, "Exception during write");
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
