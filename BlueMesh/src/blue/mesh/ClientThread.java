package blue.mesh;
import android.bluetooth.BluetoothAdapter;
import android.os.Handler;



public class ClientThread {
    private Handler handler;
    private BluetoothAdapter adapter;
    private RouterObject routerObject;
	
	ClientThread( Handler mHandler, BluetoothAdapter mAdapter, 
			      RouterObject mRouterObject ) {
		handler = mHandler;
		adapter = mAdapter;
		routerObject = mRouterObject;
	}
};


