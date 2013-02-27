package blue.mesh;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

public class ClientThread extends BluetoothConnectionThread {
    private static final String TAG = "ClientThread";
    private BluetoothAdapter    adapter;
    private BlueMeshService		bms;
    private RouterObject        router;
    private UUID                uuid;
    private BluetoothSocket 	clientSocket = null;
    volatile private Boolean	killed = false;


    protected ClientThread(BluetoothAdapter mAdapter, BlueMeshService mBms, RouterObject mRouter,
            UUID a_uuid) {
        uuid = a_uuid;
        adapter = mAdapter;
        router = mRouter;
        bms = mBms;
    }

    // function run gets list of paired devices, and attempts to
    // open and connect a socket for that device, which is then
    // passed to the router object
    public void run(){

        while ( !this.killed ) {
            // get list of all paired devices
            Set<BluetoothDevice> pairedDevices = adapter.getBondedDevices();
            
            // Loop through paired devices and attempt to connect
            for (BluetoothDevice d : pairedDevices) {
                Log.d(TAG, "Trying to connect to " + d.getName());

                clientSocket = null;
                try {

                    if (router.getDeviceState( Constants.TYPE_BLUETOOTH + '@' + d.toString()) == Constants.STATE_CONNECTED)
                        continue;

                    clientSocket = d.createRfcommSocketToServiceRecord(uuid);
                }

                catch (IOException e) {
                    Log.e(TAG, "Socket create() failed", e);
                    bms.disconnect();
                }

                // once a socket is opened, try to connect and then pass to
                // router
                try {
                    clientSocket.connect();
                    Connection bluetoothConnection = new AndroidBluetoothConnection( clientSocket );
                    Log.d(TAG,
                            "Connection Created, calling router.beginConnection()");
                    router.beginConnection(bluetoothConnection);
                }

                catch (IOException e) {
                    Log.e(TAG, "Connection constructor failed", e);
                    Log.d(TAG, "isInterrupted() == " + this.killed );
                    if( this.killed ){
                        Log.d(TAG, "Thread interrupted");
                    	return;
                    }
                }
            }
        }
        Log.d(TAG, "Thread interrupted");
        return;
    }

    protected int kill() {
    	Log.d(TAG, "trying to kill");
        this.killed = true;
        Log.d(TAG, "kill success");
        return Constants.SUCCESS;
    }
};
