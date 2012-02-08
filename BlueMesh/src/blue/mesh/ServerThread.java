package blue.mesh;

import java.io.IOException;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;


public class ServerThread extends Thread{
	
	private static final String TAG = "ServerThread";
	private Handler handler;
    private BluetoothAdapter adapter;
    private RouterObject router;
    private BluetoothServerSocket serverSocket;
    
	ServerThread( 
			Handler mHandler, 
			BluetoothAdapter mAdapter, 
			RouterObject mRouterObject ) {
		
		handler = mHandler;
		adapter = mAdapter;
		router = mRouterObject;

		//Attempt to listen on ServerSocket for incoming requests
		BluetoothServerSocket tmp = null;

		if(Constants.DEBUG) Log.d(TAG, "Creating SearchThread");

		// Create a new listening server socket
		try {
			tmp = adapter.listenUsingRfcommWithServiceRecord(
					Constants.NAME, Constants.MY_UUID);
		} catch (IOException e) {
			Log.e(TAG, "listenUsingRfcommWithServiceRecord() failed", e);
		}

		serverSocket = tmp;
	}
	
	public void run() {

        BluetoothSocket socket = null;

        // Listen to the server socket if we're not connected
        while (true) {
        	
        	//Exit while loop if interrupted
			if(this.isInterrupted()){
				if(Constants.DEBUG) Log.d(TAG, "interrupted");
				break;
			}
        	
			//Try to accept a client socket and connect to it
            try {
                socket = serverSocket.accept();
            } catch (IOException e) {
                Log.e(TAG, "accept() failed", e);
            }

            // If a connection was accepted, pass socket to router
            if (socket != null) {
				router.beginConnection(socket);
            }
            
            socket = null;
        }
        return;
	}
	
}
