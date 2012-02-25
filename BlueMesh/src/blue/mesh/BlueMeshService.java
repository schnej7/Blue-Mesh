package blue.mesh;

import android.bluetooth.BluetoothAdapter;
import android.os.Looper;
import android.util.Log;



public class BlueMeshService {
	
	private BluetoothAdapter adapter;
	private RouterObject router;
	private ServerThread serverThread;
	private ClientThread clientThread;
	private static final String TAG = "BlueMesh Service";
	
	///Trevor wrote this:
	//BMS constructor
	public BlueMeshService() throws NullPointerException{
		Looper.myLooper();
		Looper.prepare();
		//Gets bluetooth hardware from phone and makes sure that it is non-null;
		adapter = BluetoothAdapter.getDefaultAdapter();

		if (adapter == null) {
			if(Constants.DEBUG) Log.d(TAG, "BluetoothAdapter is null");
			throw new NullPointerException("BluetoothAdapter is null");
		} else {
			if(Constants.DEBUG) Log.d(TAG, "BluetoothAdapter is is non-null");
		}
		
		router = new RouterObject();
		try{
			serverThread = new ServerThread(adapter, router);
		}
		catch(NullPointerException e){
			throw e;
		}
		clientThread = new ClientThread(adapter, router);
	}
	
	//TODO: Implement later if needed
	public int config(){
		
		return Constants.SUCCESS;
	}
	
	public int launch(){
		
		//TODO: check to see if the service is already running
		serverThread.start();
		clientThread.start();
		
		return Constants.SUCCESS;
	}
	
	//Trevor wrote this:
	//function that writes message to devices
	public int write( byte [] buffer){
		router.write(buffer);
		return Constants.SUCCESS;
	}
	
	//Trevor wrote this:
	//function to grab most recent message off of message queue
	//(message stack actually a linked list but is used like a queue)
	public byte [] pull(){
		
		return router.getNextMessage();
	}
	
	public int disconnect(){
		this.clientThread.kill();
		
		this.serverThread.kill();

		this.router.stop();
		
		Log.d(TAG, "kill success");
		return Constants.SUCCESS;
	}

}
