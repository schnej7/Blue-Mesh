package blue.mesh;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

public class RouterObject {

	private List<String> connectedDevices;
	private List<ReadWriteThread> rwThreads;
	private List<byte[]> messageIDs;
	private final String TAG = "RouterObject";
	private List<byte[]> messages;

	protected RouterObject() {
		connectedDevices = new ArrayList<String>();
		rwThreads = new ArrayList<ReadWriteThread>();
		messageIDs = new ArrayList<byte[]>();
		messages = new ArrayList<byte[]>();
	}

	protected synchronized int beginConnection(BluetoothSocket socket) {

		Log.d(TAG, "beginConnection");
		// Don't let another thread touch connectedDevices while
		// I read and write it
		synchronized (this.connectedDevices) {
			Log.d(TAG, "test if devices contains the device name");
			// Check if the device is already connected to
			if (connectedDevices.contains(socket.getRemoteDevice().getName())) {
				try {
					Log.d(TAG, "trying to close socket, already"
							+ "connected to device");
					socket.close();
				} catch (IOException e) {
					Log.e(TAG, "could not close() socket", e);
				}
				return Constants.SUCCESS;
			}
			// Add device name to list of connected devices
			connectedDevices.add(socket.getRemoteDevice().getName());
		}

		// Don't let another thread touch rwThreads while I add to it
		ReadWriteThread aReadWriteThread = new ReadWriteThread(this, socket);
		aReadWriteThread.start();
		synchronized (this.rwThreads) {
			rwThreads.add(aReadWriteThread);
		}
		
		String tmp = new String("Connected to " + socket.getRemoteDevice().getName());
		this.messages.add(tmp.getBytes());

		return Constants.SUCCESS;
	}

	protected int route(byte buffer[], int source) {

		// get the messageID
		byte messageID[] = new byte[Constants.MESSAGE_ID_LEN];
		for (int i = 0; i < Constants.MESSAGE_ID_LEN; i++) {
			messageID[i] = buffer[i];
		}

		// Check that the message was not received before
		synchronized (this.messageIDs) {
			if (messageIDs.contains(messageID)) {
				Log.d(TAG,
						"Message already recieved, ID: "
								+ Integer.toHexString(messageID[0]));
				return Constants.SUCCESS;
			} else {
				Log.d(TAG,
						"New Message, ID: " + Integer.toHexString(messageID[0]));
				messageIDs.add(messageID);
				// Remove oldest message ID if too many are stored
				if (messageIDs.size() > Constants.MSG_HISTORY_LEN) {
					Log.d(TAG, "Removing Message from History");
					messageIDs.remove(0);
				}
			}
		}

		// Send the message all the threads
		synchronized (this.rwThreads) {
			for (ReadWriteThread aThread : rwThreads) {
				Log.d(TAG, "Writing to device: "
						+ aThread.getSocket().getRemoteDevice().getName());
				aThread.write(buffer);
			}
		}

		// If I am not the sender of the message
		// add it to the message queue
		if (source != Constants.SRC_ME) {
			// Add message to message queue
			synchronized (this.messages) {
				byte message[] = new byte[buffer.length
						- Constants.MESSAGE_ID_LEN];
				for (int i = Constants.MESSAGE_ID_LEN; i < buffer.length; i++) {
					message[i - Constants.MESSAGE_ID_LEN] = buffer[i];
				}
				messages.add(buffer);
			}
		}

		return Constants.SUCCESS;
	}

	protected byte[] getNextMessage() {

		if (messages.size() > 0) {
			byte message[] = messages.get(0).clone();
			messages.remove(0);
			return message;
		} else {
			return null;
		}

	}

	protected int getDeviceState(BluetoothDevice device) {
		synchronized (this.connectedDevices) {
			if (connectedDevices.contains(device.getName())) {
				return Constants.STATE_CONNECTED;
			}
		}
		return Constants.STATE_NONE;
	}

	protected int stop() {

		for (ReadWriteThread aThread : rwThreads) {
			aThread.interrupt();
			try {
				aThread.getSocket().close();
			} catch (IOException e) {
				Log.e(TAG, "could not close socket", e);
			}
		}

		return Constants.SUCCESS;
	}

	protected int write(byte[] buffer) {

		Random rand = new Random();
		byte messageID[] = new byte[Constants.MESSAGE_ID_LEN];
		rand.nextBytes(messageID);

		byte new_buffer[] = new byte[Constants.MESSAGE_ID_LEN + buffer.length];

		for (int i = 0; i < Constants.MESSAGE_ID_LEN; i++) {
			new_buffer[i] = messageID[i];
		}

		for (int i = 0; i < buffer.length; i++) {
			new_buffer[Constants.MESSAGE_ID_LEN + i] = buffer[i];
		}

		this.route(new_buffer, Constants.SRC_ME);

		return Constants.SUCCESS;
	}
}
