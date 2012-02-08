package blue.mesh;

import java.io.IOException;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.os.Handler;
import android.util.Log;


public class ServerThread extends Thread{
	
	private static final String TAG = "ServerThread";
	private Handler handler;
    private BluetoothAdapter adapter;
    private RouterObject routerObject;
    private BluetoothServerSocket serverSocket;
    
	ServerThread( 
			Handler mHandler, 
			BluetoothAdapter mAdapter, 
			RouterObject mRouterObject ) {
		
		handler = mHandler;
		adapter = mAdapter;
		routerObject = mRouterObject;

		//Attempt to listen on ServerSocket for incoming requests
		BluetoothServerSocket tmp = null;

		Log.d(TAG, "Creating SearchThread");

		// Create a new listening server socket
		try {
			tmp = adapter.listenUsingRfcommWithServiceRecord(
					Constants.NAME, Constants.MY_UUID_SECURE);
		} catch (IOException e) {
			Log.e(TAG, "listenUsingRfcommWithServiceRecord() failed", e);
		}

		serverSocket = tmp;
	}
	
	public void run() {

		
		return;
	}
	
}
