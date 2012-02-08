package blue.mesh;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;



public class ReadWriteThread extends Thread{
	
	RouterObject router;
	BluetoothSocket socket;
	BluetoothDevice device;
	
	ReadWriteThread(
			RouterObject mRouter, 
			BluetoothSocket mSocket, 
			BluetoothDevice mDevice ) {
		
		router = mRouter;
		socket = mSocket;
		device = mDevice;
	}
	
	public void run()
	{
		
	}
}
