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
    private RouterObject        router;
    private UUID                uuid;

    protected ClientThread(BluetoothAdapter mAdapter, RouterObject mRouter,
            UUID a_uuid) {
        uuid = a_uuid;
        adapter = mAdapter;
        router = mRouter;
    }

    // function run gets list of paired devices, and attempts to
    // open and connect a socket for that device, which is then
    // passed to the router object
    public void run() {

        while (!this.isInterrupted()) {
            // get list of all paired devices
            Set<BluetoothDevice> pairedDevices = adapter.getBondedDevices();

            // Loop through paired devices and attempt to connect
            for (BluetoothDevice d : pairedDevices) {

                BluetoothSocket clientSocket = null;
                try {

                    if (router.getDeviceState(d) == Constants.STATE_CONNECTED)
                        continue;

                    clientSocket = d.createRfcommSocketToServiceRecord(uuid);
                }

                catch (IOException e) {
                    Log.e(TAG, "Socket create() failed", e);
                    // TODO: throw exception
                    return;
                }

                // once a socket is opened, try to connect and then pass to
                // router
                try {
                    clientSocket.connect();
                    Log.d(TAG,
                            "Socket connected, calling router.beginConnection()");
                    router.beginConnection(clientSocket);
                    // TODO: Remove this line
                    // openSockets.add(clientSocket);
                }

                catch (IOException e) {
                    Log.e(TAG, "Socket connect() failed", e);
                    // TODO: throw exception
                    return;
                }
            }
        }
        Log.d(TAG, "Thread interrupted");
        return;
    }

    protected int closeSocket() {

        // TODO use this function to close any socket that is in a blocking
        // call in order to kill this thread
        // TODO Redo this function
        /*
         * for (BluetoothSocket socket : openSockets){ if (socket != null){ try{
         * socket.close(); openSockets.remove(socket); } catch (IOException e){
         * Log.e(TAG, "Socket close failed", e); } } }
         */
        return Constants.SUCCESS;
    }

    protected int kill() {
        this.interrupt();
        this.closeSocket();
        // TODO: this thread does not get interrupted correctly

        Log.d(TAG, "kill success");
        return Constants.SUCCESS;
    }
};
