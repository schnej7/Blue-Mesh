package blue.mesh;

import android.bluetooth.BluetoothAdapter;
import android.os.Handler;

public class RouterObject {

	Handler handler;
	BluetoothAdapter adapter;
	
	RouterObject( Handler mHandler, BluetoothAdapter mAdapter)
	{
		handler = mHandler;
		adapter= mAdapter;
	}
}

