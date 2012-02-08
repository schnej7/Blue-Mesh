package blue.mesh;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;


public class RouterObject {

	private Handler handler;
	private BluetoothAdapter adapter;
	
	RouterObject( Handler mHandler, BluetoothAdapter mAdapter)
	{
		handler = mHandler;
		adapter= mAdapter;
	}
	
	public int BeginConnection(BluetoothSocket mSocket)
	{
		handler.notify();
		adapter.enable();
		
		//Great Success!
		return 1;
	}
	
	public int WriteAll(byte bytes[])
	{
		//Great Success!
		return 1;
	}
	
	public byte [] GetUserMessage()
	{
		byte arr[] = null;
		return arr;
	}
	
	public int stop()
	{
		//Great Success!
		return 1;
	}
}

