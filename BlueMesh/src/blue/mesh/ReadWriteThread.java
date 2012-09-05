package blue.mesh;

import android.util.Log;

public class ReadWriteThread extends Thread {

    private final RouterObject    router;
    private final Connection      connection;
    private static final String   TAG = "ReadWriteThread";

    protected ReadWriteThread(RouterObject a_router, Connection a_connection){
        Log.d(TAG, "RWTHREAD CONNECTED!");

        router = a_router;
        connection = a_connection;
    }

    public void run() {
        byte[] buffer = null;

        // Keep listening to the InputStream while connected
        while (true) {

            if (this.isInterrupted()) {
                break;
            }

            int bytes = connection.read( buffer );
            if( bytes > 0 ){
                router.route(buffer, Constants.SRC_OTHER);
            }
        }

        disconnect();

        return;
    }
    
    private int disconnect(){
        // On exit close the in and out sockets and the Bluetooth socket
        router.notifyDisconnected(connection.getID(), this);
        connection.close();
        
        Log.d(TAG, "ReadWriteThread returned");
        return Constants.SUCCESS;
    }

    protected int write(byte[] buffer) {
        Log.d(TAG, "Writing bytes");
        connection.write( buffer );
        return Constants.SUCCESS;
    }

}
