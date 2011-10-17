package blueMesh.display;

public class ConnectedThread extends Thread{
	
	private RouterThread myParent;
	private int myConnectionID;
	
	public void clean_up(){
		
	}
	
	public ConnectedThread( RouterThread parent, int ID ){
		myParent = parent;
		myConnectionID = ID;
	}
	
	public void run(){
		
		
		
		
		//////////////////
		//TODO
		//on complete (disconnected)
		//Cleanup self
		/////////////////
		
		myParent.kill_connection(myConnectionID);
		
	}
	
}
