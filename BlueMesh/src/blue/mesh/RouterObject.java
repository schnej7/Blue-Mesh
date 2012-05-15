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
    private List<byte[]>          messageIDs;
    private final String          TAG                      = "RouterObject";
    private List<byte[]>          messages;
    private ReadWriteThread 	  aReadWriteThread; //Temporary pointer used for addition of r/w threads to rwThreads

    protected RouterObject() {
        connectedDevices = new ArrayList<BluetoothDevice>();
        rwThreads = new HashSet<ReadWriteThread>();
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
    	// TODO: Change parsing of message below so this byte isn't allocated.
    	// TODO: Change message creation function so this byte isn't allocated.
        // byte messageLevel = buffer[0];

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

        // Send the message all the threads
        synchronized (this.rwThreads) {
            for (ReadWriteThread aThread : rwThreads) {
                Log.d(TAG, "Writing to device: "
                        + aThread.getSocket().getRemoteDevice().getName());
                aThread.write(buffer);
            }
        }

        // In this implementation, all messages are user-level.

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
        return Constants.SUCCESS;
    }

    // Completed: Clean out this code and references to System Messages, as well as their related constants, elsewhere.

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
            try {
                aThread.getSocket().close();
            } catch (IOException e) {
                Log.e(TAG, "could not close socket", e);
            }
        	
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

    protected int write(byte[] buffer, byte messageLevel) {

        Random rand = new Random();
        byte messageID[] = new byte[Constants.MESSAGE_ID_LEN];

        // Check that the message was not received before
        synchronized (this.messageIDs) {
            boolean uniqueID = false;
            while (!uniqueID) {
                rand.nextBytes(messageID);
                uniqueID = true;
                for (int i = 0; i < messageIDs.size(); i++) {
                    if (Arrays.equals(messageIDs.get(i), messageID)) {
                        Log.d(TAG, "Message already recieved, ID: "
                                + messageID[0]);
                        uniqueID = false;
                        break;
                    }
                }
            }
        }

        byte new_buffer[] = new byte[Constants.MESSAGE_ID_LEN + buffer.length
                + 1];

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
