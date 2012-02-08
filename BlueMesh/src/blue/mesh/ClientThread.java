package blue.mesh;
import java.io.IOException;
import java.util.Set;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.ParcelUuid;
import android.util.Log;



public class ClientThread {
	private static final String TAG = "BluetoothChatService";
    private Handler handler;
    private BluetoothAdapter adapter;
    private RouterObject routerObject;
	
	ClientThread( Handler mHandler, BluetoothAdapter mAdapter, 
			      RouterObject mRouterObject ) {
		handler = mHandler;
		adapter = mAdapter;
		routerObject = mRouterObject;
	}
	
	//function run gets list of paired devices, and attempts to 
	//open and connect a socket for that device, which is then 
	//passed to the router object
	public void run() {
		
		//get list of all paired devices
		Set <BluetoothDevice> pairedDevices = adapter.getBondedDevices();
		
		for (BluetoothDevice d : pairedDevices)
		{
			//for each paired device, get uuids
			ParcelUuid[] uuids = d.getUuids();
			
			for (ParcelUuid u : uuids)
			{
				//for each uuid, try to open a socket on it
				BluetoothSocket clientSocket = null;
				try {
					 clientSocket = d.createRfcommSocketToServiceRecord(u.getUuid());
				}
			
				catch (IOException e) {
				Log.e(TAG, "Socket create() failed", e);
				}
				
				//once a socet is opened, try to connect and then pass to router
				try {
					clientSocket.connect();
					routerObject.BeginConnection(clientSocket);
				}
				
				catch (IOException e) {
					Log.e(TAG, "Socket connect() failed", e);
				}
				
			}
		}
		return;
	}
};


