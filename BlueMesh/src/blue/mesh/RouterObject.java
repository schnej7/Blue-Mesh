package blue.mesh;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

public class RouterObject {

    private List<BluetoothDevice> connectedDevices;
    private HashSet<ReadWriteThread> rwThreads;
    private List<byte[]>          messageIDs;		//List of recently acquired messageIDs (Max size of Constants.MSG_HISTORY_LEN)
    private final String          TAG                      = "RouterObject";
    private List<byte[]>          messages;			//List of accepted messages to be read to the BlueMeshService.
    private ReadWriteThread 	  aReadWriteThread; //Temporary pointer used for addition of r/w threads to rwThreads
    private byte[]				  address;			//This device's address, as it appears on incoming messages (Generated & Truncated)

    protected RouterObject(String m_address) {
        connectedDevices = new ArrayList<BluetoothDevice>();
        rwThreads = new HashSet<ReadWriteThread>();
        messageIDs = new ArrayList<byte[]>();
        messages = new ArrayList<byte[]>();
        address = new byte[Constants.TARGET_ID_LEN];
        byte[] temp = m_address.getBytes();
        for(int i=0; i<Constants.TARGET_ID_LEN; i++){
        	address[i] = temp[i];
        }
    }

    protected synchronized int beginConnection(BluetoothSocket socket) {

        Log.d(TAG, "beginConnection");
        // Don't let another thread touch connectedDevices while
        // I read and write it
        synchronized (this.connectedDevices) {
            Log.d(TAG, "test if devices contains the device name");
            // Check if the device is already connected to
            if (connectedDevices.contains(socket.getRemoteDevice())) {
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
            connectedDevices.add(socket.getRemoteDevice());
        }

        // Don't let another thread touch rwThreads while I add to it
        try {
        	aReadWriteThread = new ReadWriteThread(this, socket);
        } catch (IOException e){
        	Log.e(TAG, "Router has no R/W thread (failed initialize)", e);
        }
        aReadWriteThread.start();
        synchronized (this.rwThreads) {
            rwThreads.add(aReadWriteThread); //Does not allow multiples (set)
        }
        aReadWriteThread = null;

        // TODO: it would be nice if this worked
        // String toastMsg = "Connected to " +
        // socket.getRemoteDevice().getName();
        // Toast.makeText(context, toastMsg.toString(),
        // Toast.LENGTH_SHORT).show();

        return Constants.SUCCESS;
    }

    protected int route(byte buffer[], int source) {

        // Get the message level
        byte messageLevel = buffer[0];

        // get the messageID
        byte messageID[] = new byte[Constants.MESSAGE_ID_LEN]; //TODO: Memory Leak? Should it be nullified?
        for (int i = 0; i < Constants.MESSAGE_ID_LEN; i++) {
            messageID[i] = buffer[i + 1];
        }

        // Check that the message was not received before
        synchronized (this.messageIDs) {
            for (int i = 0; i < messageIDs.size(); i++) {
                if (Arrays.equals(messageIDs.get(i), messageID)) {
                    Log.d(TAG, "Message already recieved, ID: " + messageID[0]);
                    return Constants.SUCCESS;
                }
            }

            Log.d(TAG, "New Message, ID: " + messageID.toString());
            messageIDs.add(messageID);
            // Remove oldest message ID if too many are stored
            if (messageIDs.size() > Constants.MSG_HISTORY_LEN) {
                Log.d(TAG, "Removing Message from History");
                messageIDs.remove(0);
            }
        }

        if (messageLevel == Constants.MESSAGE_ALL){        
	        // If I am not the sender of the message
	        // add it to the message queue
	        if (source != Constants.SRC_ME) {
	            // Add message to message queue
	            synchronized (this.messages) {
	                byte message[] = new byte[buffer.length
	                        - Constants.MESSAGE_ID_LEN - 1];
	                for (int i = Constants.MESSAGE_ID_LEN + 1; i < buffer.length; i++) {
	                    message[i - Constants.MESSAGE_ID_LEN - 1] = buffer[i];
	                }
	                messages.add(message);
	            }
	        }
        }
        
        else if (messageLevel == Constants.MESSAGE_TARGET){
        	byte target[] = new byte[Constants.TARGET_ID_LEN];
        	for (int i=0; i<Constants.TARGET_ID_LEN; i++){
        		target[i] = buffer[i + Constants.MESSAGE_ID_LEN + 1];
        	}
        	if (target == this.address) {
        		//Add to the message queue
        		return Constants.SUCCESS; //DO NOT send to all threads.
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
            if (connectedDevices.contains(device)) {
                return Constants.STATE_CONNECTED;
            }
        }
        return Constants.STATE_NONE;
    }

    protected int stop() {

        for (ReadWriteThread aThread : rwThreads) {
            aThread.interrupt();
            
            //Sockets are closed in ReadWriteThread's Interrupt
            /*try {
                aThread.getSocket().close();
            } catch (IOException e) {
                Log.e(TAG, "could not close socket", e);
            }*/
        	
        }

        return Constants.SUCCESS;
    }

    //Deprecated: Uses System Message Level, not yet implemented
    /*
    protected int getNumberOfDevicesOnNetwork() {
    	return 0;//rwThreads.size(); Devices on NETWORK, not directly connected to this device
    }*/

    //NOTE: This is called once through the ReadWriteThread object.
    //This is used when a R/W thread discovers its device can no longer be written to.
    protected int notifyDisconnected(BluetoothDevice device) {
        // If the device name is in the list of connected devices
        // then search for the ReadWriteThread associated with it
        // and set it's pointer to null while it finishes execution
        Log.d(TAG, "removing device: " + device.getName() + " from devices");
        if (connectedDevices.remove(device)) {
            Log.d(TAG, "Device removed");
            for (ReadWriteThread rwThread : rwThreads) {
                if (rwThread.getSocket().getRemoteDevice() == device) {
                    //== is appropriate in the above statement, assuming .getRemoteDevice() returns
                    //a pointer to the actual device, not a copy.
                    rwThread = null;
                }
            }
        } else {
            Log.d(TAG, "Device not removed");
        }
        return Constants.SUCCESS;
    }

    protected int write(byte[] buffer, byte messageLevel, BluetoothDevice target) {
        Random rand = new Random();
        byte messageID[] = new byte[Constants.MESSAGE_ID_LEN];

        // Generates a messageID that was not received before
        synchronized (this.messageIDs) {
            boolean uniqueID = false;
            while (!uniqueID) {
                rand.nextBytes(messageID); //Fills messageID with random bytes
                uniqueID = true;
                for (byte[] i : messageIDs){
                    if (Arrays.equals(i, messageID)) {
                        Log.d(TAG, "Message already recieved, ID starts with: " //Message exists in messageIDs
                                + messageID[0]);
                        uniqueID = false;
                        break; //Regenerates a random ID and tries again.
                    }
                }
            }
        }
        
    	int middle_field_length = 0;
    	byte middle_field[] = null;
    	
    	if (messageLevel == Constants.MESSAGE_ALL){
    		middle_field_length = Constants.MESSAGE_ID_LEN;
    		middle_field = messageID;
    	}
    	else if (messageLevel == Constants.MESSAGE_TARGET){
    		middle_field_length = (Constants.MESSAGE_ID_LEN + Constants.TARGET_ID_LEN);
    		byte[] targetID = target.toString().getBytes(); //May be longer than Constants.TARGET_ID_LEN
    		//TODO: Write what happens for targeted messages.
    		middle_field = new byte[Constants.MESSAGE_ID_LEN + Constants.TARGET_ID_LEN];
    		for(int i=0; i<Constants.MESSAGE_ID_LEN; i++){
    			middle_field[i] = messageID[i];
    		}
    		for(int i=0; i<Constants.TARGET_ID_LEN; i++){
    			middle_field[i+Constants.MESSAGE_ID_LEN] = targetID[i];
    		}
    	}
    	else {
    		Log.e(TAG, "Message Level is invalid");
    	}

    	//Construct the message in new_buffer and route it out.
        byte new_buffer[] = new byte[middle_field_length + buffer.length
                + 1];

        new_buffer[0] = messageLevel;

        for (int i = 0; i < middle_field_length; i++) {
            new_buffer[i + 1] = middle_field[i];
        }

        for (int i = 0; i < buffer.length; i++) {
            new_buffer[middle_field_length + i + 1] = buffer[i];
        }

        this.route(new_buffer, Constants.SRC_ME);

        return Constants.SUCCESS;
    }
}
