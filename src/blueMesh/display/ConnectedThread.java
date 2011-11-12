package blueMesh.display;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;

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

	public void clean_up() {
		try {
			in.close();
			out.close();
		} catch (IOException e) {
			print_debug("failed to close() input output streams");
		}
		try {
			mySocket.close();
		} catch (IOException e) {
			print_debug("failed to close() mySocket");
		}
	}

	public ConnectedThread(Handler aHandler, RouterThread parent, int ID,
			BluetoothSocket socket) {

		mHandler = aHandler;
		myParent = parent;
		myConnectionID = ID;
		print_debug("create ConnectedThread:");
		mySocket = socket;
		InputStream tmpIn = null;
		OutputStream tmpOut = null;

		// Get the BluetoothSocket input and output streams
		try {
			tmpIn = socket.getInputStream();
			tmpOut = socket.getOutputStream();
		} catch (IOException e) {
			print_debug("temp sockets not created");
		}

		in = tmpIn;
		out = tmpOut;
	}

	public void run() {

		print_debug("BEGIN ConnectedThread");
		byte[] buffer = new byte[1024];
		int bytes;

		// Keep listening to the InputStream while connected
		while (true) {
			try {
				// Read from the InputStream
				bytes = in.read(buffer);

				// Send the obtained bytes to the UI Activity
				mHandler.obtainMessage(Constants.MSG_NORMAL, bytes, -1, buffer)
						.sendToTarget();

				// ////////////////////
				// TODO
				// Use myParent to rout
				// the data to all of the
				// other connections
				// ////////////////////
				myParent.route_bytes(myConnectionID, buffer, bytes);

			} catch (IOException e) {
				print_debug("connection " + myConnectionID + " disconnected");

				break;
			}
		}

		this.clean_up();

		myParent.kill_connection(myConnectionID);

	}

	public void write(byte[] buffer) {
		try {
			out.write(buffer);

			// Share the sent message back to the UI Activity
			mHandler.obtainMessage(Constants.MSG_SENT, -1, -1, buffer)
					.sendToTarget();
		} catch (IOException e) {
			print_debug("Exception during write");
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
