package blue.mesh;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;



public class ClientThread extends Thread{
	private static final String TAG = "ClientThread";
	private BluetoothAdapter adapter;
	private RouterObject router;
	//TODO: Remove this line
	//private ArrayList <BluetoothSocket> openSockets;
	private boolean stop = false;

	protected ClientThread(   
			BluetoothAdapter mAdapter, 
			RouterObject mRouter )  {
		//TODO: Remove this line
        //openSockets = new ArrayList<BluetoothSocket>();
		adapter = mAdapter;
		router = mRouter;
	}

	//function run gets list of paired devices, and attempts to 
	//open and connect a socket for that device, which is then 
	//passed to the router object
	public void run() {
		
		while (!stop)
		{
			if(this.isInterrupted()){
				if(Constants.DEBUG) Log.d(TAG, "interrupted");
				return;
			}
			//get list of all paired devices
			Set <BluetoothDevice> pairedDevices = adapter.getBondedDevices();

			for (BluetoothDevice d : pairedDevices)
			{

				//Check to stop
				if(stop){
					Log.d(TAG, "Thread stopped");
					return;
				}
				
				BluetoothSocket clientSocket = null;
				try {
					Log.d(TAG,  "Device: " + d.getName() );
					
					if( router.getDeviceState(d) == Constants.STATE_CONNECTED) 
						continue;

					clientSocket = d.createRfcommSocketToServiceRecord(
							Constants.MY_UUID);
				}

				catch (IOException e) {
					Log.e(TAG, "Socket create() failed", e);
					//TODO: throw exception
					return;
				}

				//once a socet is opened, try to connect and then pass to router
				try {
					clientSocket.connect();
					Log.d(TAG, "Socket connected, calling router.beginConnection()");
					router.beginConnection(clientSocket);
					//TODO: Remove this line
					//openSockets.add(clientSocket);
				}

				catch (IOException e) {
					if(this.isInterrupted()){
						if(Constants.DEBUG) Log.d(TAG, "interrupted");
						return;
					}
					Log.e(TAG, "Socket connect() failed", e);
				}
			}
		}
	}
	
	protected int closeSocket(){
		
		//TODO use this function to close any socket that is in a blocking
		//call in order to kill this thread
		//TODO Redo this function
		/*
		for (BluetoothSocket socket : openSockets){
		  if (socket != null){
			  try{
				  socket.close();
				  openSockets.remove(socket);
			  }
			  catch (IOException e){
				  Log.e(TAG, "Socket close failed", e);
			  }
		  }
		}*/
		return Constants.SUCCESS;
	}
	
	protected int kill(){
		this.stop = true;
		this.closeSocket();
		//TODO: this thread does not get interrupted correctly
		this.interrupt();

		Log.d(TAG, "kill success");
		return Constants.SUCCESS;
	}
};


