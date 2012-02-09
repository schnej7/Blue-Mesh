package blue.mesh;

import android.bluetooth.BluetoothAdapter;
import android.util.Log;



public class BlueMeshService {
	
	private BluetoothAdapter adapter;
	private RouterObject router;
	private ServerThread serverThread;
	private ClientThread clientThread;
	private static final String TAG = "BlueMesh Service";
	
	public BlueMeshService(){
		adapter = BluetoothAdapter.getDefaultAdapter();

		if (adapter == null) {
			if(Constants.DEBUG) Log.d(TAG, "BluetoothAdapter is null");
		} else {
			if(Constants.DEBUG) Log.d(TAG, "BluetoothAdapter is is non-null");
		}
		
		router = new RouterObject();
		serverThread = new ServerThread(adapter, router);
		clientThread = new ClientThread(adapter, router);
	}
	
	//TODO: Implement later if needed
	public int config(){
		
		return Constants.SUCCESS;
	}
	
	public int launch(){
		
		//TODO: check to see if the service is already running
		
		router = new RouterObject();
		serverThread = new ServerThread( adapter, router );
		clientThread = new ClientThread( adapter, router );
		
		serverThread.start();
		clientThread.start();
		
		return Constants.SUCCESS;
	}
	
	public int write( byte [] buffer){
		router.write(buffer);
		return Constants.SUCCESS;
	}
	
	public byte [] pull(){
		
		byte message[] = new byte[Constants.MAX_MESSAGE_LEN];
		message = router.getNextMessage();
		return message;
	}
	
	public int disconnect(){
		this.clientThread.kill();
		this.serverThread.kill();
		this.router.stop();
		return Constants.SUCCESS;
	}

}
