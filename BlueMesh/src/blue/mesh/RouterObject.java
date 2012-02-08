package blue.mesh;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;


public class RouterObject {

	private Handler handler;
	private BluetoothAdapter adapter;
	
	public RouterObject( Handler mHandler, BluetoothAdapter mAdapter) {
		handler = mHandler;
		adapter= mAdapter;
	}
	
	//TODO: MAKE THIS CALL SYNCHRONIZED
	public int beginConnection(BluetoothSocket mSocket) {
		
		//Great Success!
		return 1;
	}
	
	public int route(byte buff[]) {
		//Great Success!
		return Constants.SUCCESS;
	}
	
	public byte [] getNextMessage() {
		byte arr[] = null;
		return arr;
	}
	
	int getDeviceState( BluetoothDevice device ){
		
		return Constants.STATE_NONE;
	}
	
	public int stop() {
		//Great Success!
		return Constants.SUCCESS;
	}
}

