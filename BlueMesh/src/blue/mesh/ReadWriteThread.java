package blue.mesh;

import java.io.IOException;

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

    public void run() {
        byte[] buffer = new byte[Constants.MAX_MESSAGE_LEN];

        // Keep listening to the InputStream while connected
        while (!this.isInterrupted()) {

            int bytes = 0;
            try{
                bytes = connection.read( buffer );
            }
            catch( IOException e ){
            	if(Constants.DEBUG) Log.e(TAG, "read failed", e);
                disconnect();
                break;
            }

            
            if( bytes > 0 ){
            	if(Constants.DEBUG) Log.d(TAG, "Got something");
            }
            if( bytes > 0 && buffer != null ){
            	if(Constants.DEBUG) Log.d(TAG, buffer.toString());
                byte[] returnBuffer = new byte[bytes];
                for( int i = 0; i < bytes; i++ ){
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
        connection.write( buffer );
        return Constants.SUCCESS;
    }

}
