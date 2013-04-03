package blue.mesh;

import java.io.IOException;
import java.nio.ByteBuffer;

import android.util.Log;

public class ReadWriteThread extends Thread {

    private final RouterObject    router;
    private final Connection      connection;
    private static final String   TAG = "ReadWriteThread";

    protected ReadWriteThread(RouterObject a_router, Connection a_connection){
    	if(Constants.DEBUG) Log.d(TAG, "RWTHREAD CONNECTED!");

        router = a_router;
        connection = a_connection;
    }
    
    
    private byte[] readData() throws IOException {
    	// Initially allocate 4 byte buffer to read the size of the message
    	int header_size = Constants.HEADER_SIZE;
    	byte[] header = new byte[header_size];
    	
    	try {
    		connection.read(header, 0, header_size);
    	} catch (IOException e) {
    		Log.e(TAG, "Header read failed", e);
    		throw e;
    	}
    	ByteBuffer bb = ByteBuffer.wrap(header);
    	int msg_size = bb.getInt();
    	// Hopefully we now have the size
    	
    	byte[] message = new byte[msg_size];
    	try {
    		connection.read(message, 0, msg_size);
    	} catch (IOException e) {
    		Log.e(TAG, "Message read failed", e);
    		throw e;
    	}
    	return message;
    }
    
    private int writeData( byte[] buffer ) {
    	// Format message as follows: 
    	// Header: [4 bytes: num chunks|2 bytes size of last chunk|2 bytes message id]
    	// Message: [2 bytes message id|1024 bytes data]
    	// End: [2 bytes message id|16 byte checksum]
    	// No wait
    	// Message: [4 bytes message size|message]
    	int header_size = Constants.HEADER_SIZE;
    	int size = buffer.length;
    	byte[] header = ByteBuffer.allocate(header_size).putInt(size).array();
    	byte[] message = new byte[header_size + size];
    	System.arraycopy(header, 0, message, 0, header_size);
    	System.arraycopy(buffer, 0, message, header_size, size);
    	
    	connection.write(message);
    	
    	return Constants.SUCCESS;
    }

    public void run() {
        byte[] buffer = null;


        // Keep listening to the InputStream while connected
        while (!this.isInterrupted()) {

            try{
                buffer = readData();
            }
            catch( IOException e ){
            	if(Constants.DEBUG) Log.e(TAG, "read failed", e);
                disconnect();
                break;
            }

            

            if( buffer != null ){
            	if(Constants.DEBUG) Log.d(TAG, buffer.toString());
                byte[] returnBuffer = new byte[buffer.length];
                for( int i = 0; i < buffer.length; i++ ){
                    returnBuffer[i] = buffer[i];
                }
                router.route(returnBuffer, Constants.SRC_OTHER);
            }

        }

        return;
    }
    
    protected int disconnect(){
        // On exit close the in and out sockets and the Bluetooth socket
        router.notifyDisconnected(connection.getID(), this);
        connection.close();
        
        if(Constants.DEBUG) Log.d(TAG, "ReadWriteThread returned");
        return Constants.SUCCESS;
    }

    protected int write(byte[] buffer) {
    	if(Constants.DEBUG) Log.d(TAG, "Writing bytes: " + buffer.toString() );
        int stat = writeData( buffer );
        return stat;
    }

}
