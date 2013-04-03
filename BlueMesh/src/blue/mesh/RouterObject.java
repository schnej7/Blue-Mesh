package blue.mesh;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import android.bluetooth.BluetoothDevice;
import android.util.Log;

public class RouterObject {

    private List<String> connectedDevices;
    private HashSet<ReadWriteThread> rwThreads;
    private List<byte[]>          messageIDs;		//List of recently acquired messageIDs (Max size of Constants.MSG_HISTORY_LEN)
    private final String          TAG                      = "RouterObject";
    private List<byte[]>          messages;			//List of accepted messages to be read to the BlueMeshService.
    private byte[]				  address;			//This device's address, as it appears on incoming messages (Generated & Truncated)

    protected RouterObject(String m_address) {
        connectedDevices = new ArrayList<String>();
        rwThreads = new HashSet<ReadWriteThread>();
        messageIDs = new ArrayList<byte[]>();
        messages = new ArrayList<byte[]>();
        address = new byte[Constants.TARGET_ID_LEN];
        byte[] temp = m_address.getBytes();
        for(int i=0; i<Constants.TARGET_ID_LEN; i++){
        	address[i] = temp[i];
        }
    }

    protected synchronized int beginConnection(Connection connection) {

    	if(Constants.DEBUG) Log.d(TAG, "beginConnection");
        // Don't let another thread touch connectedDevices while
        // I read and write it
        synchronized (this.connectedDevices) {
        	if(Constants.DEBUG) Log.d(TAG, "test if devices contains the device name: " + connection.getID());
            // Check if the device is already connected to
            if (connectedDevices.contains(connection.getID())) {
            	if(Constants.DEBUG) Log.d(TAG, "trying to close socket, already connected to device");
                connection.close();
                return Constants.SUCCESS;
            }
            // Add device name to list of connected devices
            connectedDevices.add(connection.getID());
        }

        ReadWriteThread aReadWriteThread = new ReadWriteThread(this, connection);
        aReadWriteThread.start();
        
        synchronized (this.rwThreads) {
            rwThreads.add(aReadWriteThread); //Does not allow multiples (set)
        }

        // TODO: it would be nice if this worked
        // String toastMsg = "Connected to " +
        // socket.getRemoteDevice().getName();
        // Toast.makeText(context, toastMsg.toString(),
        // Toast.LENGTH_SHORT).show();
        if(Constants.DEBUG) Log.d(TAG, "Finished setting up connection");

        return Constants.SUCCESS;
    }
    
    private byte getMessageLevel( byte buffer[] ){
    	return buffer[0];
    }
    
    private byte[] getMessageID( byte buffer[] ){
    	byte messageID[] = new byte[Constants.MESSAGE_ID_LEN]; 
    	for (int i = 0; i < Constants.MESSAGE_ID_LEN; i++) {
            messageID[i] = buffer[i + 1];
        }
        return messageID;
    }
    
    //Checks if the messageID is in messageIDs
    private boolean messageIsNew( byte[] messageID ){
    	synchronized( this.messageIDs ){
    		for (int i = 0; i < messageIDs.size(); i++) {
                if (Arrays.equals(messageIDs.get(i), messageID)) {
                	if(Constants.DEBUG) Log.d(TAG, "Message already recieved, ID: " + messageID[0]);
                    return false;
                }
            }
    	}
    	return true;
    }
    
    //Adds messageID to messageIDs and removes the oldest ID if
    //messageIDs.size() is larger than MSG_HISTORY_LEN
    private void recordMessageID( byte[] messageID ){
    	synchronized( this.messageIDs ){
    		if(Constants.DEBUG) Log.d(TAG, "New Message, ID: " + messageID.toString());
	        messageIDs.add(messageID);
	        // Remove oldest message ID if too many are stored
	        if (messageIDs.size() > Constants.MSG_HISTORY_LEN) {
	        	if(Constants.DEBUG) Log.d(TAG, "Removing Message from History");
	            messageIDs.remove(0);
	        }
    	}
    }
    
    //Adds the message to the queue to be processed by the app
    private void addMessageToQueue( byte[] buffer ){
    	synchronized (this.messages) {
            byte message[] = new byte[buffer.length
                    - Constants.MESSAGE_ID_LEN - 1];
            for (int i = Constants.MESSAGE_ID_LEN + 1; i < buffer.length; i++) {
                message[i - Constants.MESSAGE_ID_LEN - 1] = buffer[i];
            }
            messages.add(message);
        }
    }
    
    //Gets the target of the message if there is one
    private byte[] getTarget( byte[] buffer ){
    	byte target[] = new byte[Constants.TARGET_ID_LEN];
    	for (int i=0; i<Constants.TARGET_ID_LEN; i++){
    		target[i] = buffer[i + Constants.MESSAGE_ID_LEN + 1];
    	}
    	return target;
    }

    //Used to send a message to it's destination
    protected int route(byte buffer[], int source) {

        // Get the message level
        byte messageLevel = getMessageLevel( buffer );
        // get the messageID
        byte messageID[] = getMessageID( buffer );

        // Check that the message was not received before
        if( messageIsNew( messageID )){
        	recordMessageID( messageID );
        }
        else{
        	return Constants.SUCCESS;
        }
        
        // If I am not the sender of the message
        // add it to the message queue
        if (messageLevel == Constants.MESSAGE_ALL && source != Constants.SRC_ME){
            // Add message to message queue
        	addMessageToQueue( buffer );
        }
        else if (messageLevel == Constants.MESSAGE_TARGET){
        	byte[] target = getTarget( buffer );
        	if (target == this.address) {
        		//Add to the message queue
        		return Constants.SUCCESS; //DO NOT send to all threads.
        	}
        }
        
        // Send the message to all the threads
        synchronized (this.rwThreads) {
            for (ReadWriteThread aThread : rwThreads) {
            	if(Constants.DEBUG) Log.d(TAG, "Writing to device on thread: " + aThread.getName());
                aThread.write(buffer);
            }
        }
        
        return Constants.SUCCESS;
    }

    //Used by BlueMeshService to get the next message from the queue
    protected byte[] getNextMessage(){
        if (messages.size() > 0) {
            byte message[] = messages.get(0).clone();
            messages.remove(0);
            return message;
        } else {
            return null;
        }

    }

    protected int getDeviceState(String device) {
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
        	aThread.disconnect();
        }

        return Constants.SUCCESS;
    }

    //NOTE: This is called once through the ReadWriteThread object.
    //This is used when a R/W thread discovers its device can no longer be written to.
    protected int notifyDisconnected(String deviceID, ReadWriteThread deadThread) {
        // If the device ID is in the list of connected devices
        // then search for the ReadWriteThread associated with it
        // and set it's pointer to null while it finishes execution
    	if(Constants.DEBUG) Log.d(TAG, "removing device: " + deviceID + " from devices");
        if (connectedDevices.remove(connectedDevices.indexOf(deviceID)) != null) {
            if( rwThreads.remove(deadThread) ){
            	if(Constants.DEBUG) Log.d(TAG, "Device removed");
            }
            else{
            	if(Constants.DEBUG) Log.d(TAG, "Device not removed from rwThreads");
            }
        } else {
        	if(Constants.DEBUG) Log.d(TAG, "Device not removed from connectedDevices");
        }
        return Constants.SUCCESS;
    }
    
	// Generates and returns a messageID that was not received before
    private byte[] uniqueMessageID(Random rand){
    	byte[] messageID = new byte[Constants.MESSAGE_ID_LEN];
    	
        synchronized (this.messageIDs) {
            boolean uniqueID = false;
            while (!uniqueID) {
                rand.nextBytes(messageID); //Fills messageID with random bytes
                uniqueID = true;
                for (byte[] i : messageIDs){
                    if (Arrays.equals(i, messageID)) {
                    	if(Constants.DEBUG) Log.d(TAG, "Message already recieved, ID starts with: " //Message exists in messageIDs
                                + messageID[0]);
                        uniqueID = false;
                        break; //Regenerates a random ID and tries again.
                    }
                }
            }
        }
        
        return messageID;
    }
    
    //Constructs the middle field (containing message and target IDs, as applicable) of the message buffer
    //Returns length of field
    private byte[] middleField(byte messageLevel, byte[] messageID, BluetoothDevice target){
    	byte[] middle_field = null;
    	
    	if (messageLevel == Constants.MESSAGE_ALL){
    		middle_field = messageID;
    	}
    	
    	else if (messageLevel == Constants.MESSAGE_TARGET){
    		byte[] targetID = target.toString().getBytes(); //May be longer than Constants.TARGET_ID_LEN

    		middle_field = new byte[Constants.MESSAGE_ID_LEN + Constants.TARGET_ID_LEN];
    		for(int i=0; i<Constants.MESSAGE_ID_LEN; i++){
    			middle_field[i] = messageID[i];
    		}
    		if( targetID != null ){
    		    for(int i=0; i<Constants.TARGET_ID_LEN; i++){
                    middle_field[i+Constants.MESSAGE_ID_LEN] = targetID[i];
                }    
    		}
    		else{
    		    for(int i=0; i<Constants.TARGET_ID_LEN; i++){
                    middle_field[i+Constants.MESSAGE_ID_LEN] = 0;
                }
    		}
    	}
    	
    	else {
    		if(Constants.DEBUG) Log.e(TAG, "Message Level is invalid");
    	}
    	
    	return middle_field;
    }

    protected int write(byte[] buffer, byte messageLevel, BluetoothDevice target) {
        Random rand = new Random();
        byte messageID[] = uniqueMessageID(rand);
    	
        byte[] middle_field = middleField(messageLevel, messageID, target);
        int middle_field_length = 0;
        if(middle_field != null)
        	middle_field_length = middle_field.length;

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
