package blue.mesh;

import java.io.IOException;
import java.util.List;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;


public class RouterObject {

	private Handler handler;
	private BluetoothAdapter adapter;
	private List <String> connectedDevices;
	private List <ReadWriteThread> rwThreads;
	private final String TAG = "RouterObject";
	
	protected RouterObject( Handler mHandler, BluetoothAdapter mAdapter) {
		handler = mHandler;
		adapter= mAdapter;
	}
	
	protected synchronized int beginConnection(BluetoothSocket socket) {
		
		//Don't let another thread touch connectedDevices while
		//I read and write it
		synchronized( this.connectedDevices ){
			//Check if the device is already connected to
			if( connectedDevices.contains(socket.getRemoteDevice().getName())){
				try {
					socket.close();
				} catch (IOException e) {
					Log.e(TAG, "could not close() socket", e);
				}
				return Constants.SUCCESS;
			}
			//Add device name to list of connected devices
			connectedDevices.add(socket.getRemoteDevice().getName());
		}
		
		//Don't let another thread touch rwThreads while I add to it
		ReadWriteThread aReadWriteThread = new ReadWriteThread(this, socket);
		aReadWriteThread.start();
		synchronized( this.rwThreads ){
			rwThreads.add(aReadWriteThread);
		}
		
		return Constants.SUCCESS;
	}
	
	protected int route(byte buff[]) {
		//Great Success!
		return Constants.SUCCESS;
	}
	
	protected byte [] getNextMessage() {
		byte arr[] = null;
		return arr;
	}
	
	protected int getDeviceState( BluetoothDevice device ){
		
		return Constants.STATE_NONE;
	}
	
	protected int stop() {
		//Great Success!
		return Constants.SUCCESS;
	}
}

