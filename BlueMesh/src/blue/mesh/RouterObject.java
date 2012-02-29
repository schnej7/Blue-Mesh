package blue.mesh;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
	private boolean queryingNumberOfDevices = false;
	private int numberOfDevicesOnNetwork = 1;

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

		// get the message level
		byte messageLevel = buffer[0];
		
		// get the messageID
		byte messageID[] = new byte[Constants.MESSAGE_ID_LEN];
		for (int i = 0; i < Constants.MESSAGE_ID_LEN; i++) {
			messageID[i] = buffer[i + 1];
		}

		// Check that the message was not received before
		synchronized (this.messageIDs) {
			for( int i = 0; i < messageIDs.size(); i++ ){
				if (Arrays.equals( messageIDs.get(i), messageID ) ) {
					Log.d(TAG, "Message already recieved, ID: " + messageID[0]);
					return Constants.SUCCESS;
				} 
			}
			
			Log.d(TAG, "New Message, ID: " + messageID[0]);
			messageIDs.add(messageID);
			// Remove oldest message ID if too many are stored
			if (messageIDs.size() > Constants.MSG_HISTORY_LEN) {
				Log.d(TAG, "Removing Message from History");
				messageIDs.remove(0);
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

		//if the message is a system level message
		if( messageLevel == Constants.BYTE_LEVEL_SYSTEM ){
			byte message[] = new byte[buffer.length - Constants.MESSAGE_ID_LEN - 1];
			for (int i = Constants.MESSAGE_ID_LEN + 1; i < buffer.length; i++) {
				message[i - Constants.MESSAGE_ID_LEN - 1] = buffer[i];
			}
			handleSystemMessage( message );
		}
		
		//if the message is a user level message
		else if ( messageLevel == Constants.BYTE_LEVEL_USER ){
			// If I am not the sender of the message
			// add it to the message queue
			if (source != Constants.SRC_ME) {
				// Add message to message queue
				synchronized (this.messages) {
					byte message[] = new byte[buffer.length - Constants.MESSAGE_ID_LEN - 1];
					for (int i = Constants.MESSAGE_ID_LEN + 1; i < buffer.length; i++) {
						message[i - Constants.MESSAGE_ID_LEN - 1] = buffer[i];
					}
					messages.add(message);
				}
			}
		}

		return Constants.SUCCESS;
	}

	private void handleSystemMessage(byte[] message) {
		
		if( message[0] == Constants.SYSTEM_MSG_TOTAL_DEVICE_QUERY ){
			byte[] newMessage = new byte[1];
			newMessage[0] = Constants.SYSTEM_MSG_TOTAL_DEVICE_CHECK_IN;
			this.write(newMessage, Constants.BYTE_LEVEL_SYSTEM);
		}
		
		else if ( message[0] == Constants.SYSTEM_MSG_TOTAL_DEVICE_CHECK_IN ){
			if( queryingNumberOfDevices ){
				numberOfDevicesOnNetwork++;
			}
		}
		
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
	
	protected int getNumberOfDevicesOnNetwork(){
		
		numberOfDevicesOnNetwork = 1;
		byte[] message = new byte[1];
		message[0] = Constants.SYSTEM_MSG_TOTAL_DEVICE_QUERY;
		write(message, Constants.BYTE_LEVEL_SYSTEM);
		
		try {
			wait(100);
		} catch (InterruptedException e) {
			Log.e(TAG, "Wait inturrupted");
		}
		
		return numberOfDevicesOnNetwork;
	}
	
	protected int notifyDisconnected( String deviceName ){
		
		//If the device name is in the list of connected devices
		//then search for the ReadWriteThread associated with it
		//and set it's pointer to null while it finishes execution
		if( connectedDevices.remove(deviceName) ){
			for( ReadWriteThread rwThread : rwThreads ){
				if( rwThread.getSocket().getRemoteDevice().getName()
						== deviceName){
					rwThread = null;
				}
			}
		}
		
		return Constants.SUCCESS;
	}

	protected int write(byte[] buffer, byte messageLevel) {

		
		
		Random rand = new Random();
		byte messageID[] = new byte[Constants.MESSAGE_ID_LEN];
		rand.nextBytes(messageID);

		byte new_buffer[] = new byte[Constants.MESSAGE_ID_LEN + buffer.length + 1];

		new_buffer[0] = messageLevel;
		
		for (int i = 0; i < Constants.MESSAGE_ID_LEN; i++) {
			new_buffer[i + 1] = messageID[i];
		}

		for (int i = 0; i < buffer.length; i++) {
			new_buffer[Constants.MESSAGE_ID_LEN + i + 1] = buffer[i];
		}

		this.route(new_buffer, Constants.SRC_ME);

		return Constants.SUCCESS;
	}
}
