package blueMesh.display;

public class ConnectionSetupThread extends Thread{

	private static int connectionState;
	
	synchronized void set_connection_state(int a_connectionState){
		connectionState = a_connectionState;
	}
	
	synchronized int get_connection_state(){return connectionState;}
	
}
