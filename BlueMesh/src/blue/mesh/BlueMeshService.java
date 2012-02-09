package blue.mesh;

import android.bluetooth.BluetoothAdapter;



public class BlueMeshService {
	
	private BluetoothAdapter adapter;
	private RouterObject router;
	private ServerThread serverThread;
	private ClientThread clientThread;
	
	public BlueMeshService(){
		
	}
	
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
		
		return Constants.SUCCESS;
	}
	
	public byte [] pull(){
		
		return null;
	}
	
	public int disconnect(){
		this.clientThread.kill();
		this.serverThread.kill();
		this.router.stop();
		return Constants.SUCCESS;
	}

}
