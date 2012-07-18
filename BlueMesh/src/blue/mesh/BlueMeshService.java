package blue.mesh;

import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.util.Log;

public class BlueMeshService {

    private BluetoothAdapter    adapter;
    private RouterObject        router;
    private ServerThread        serverThread;
    private ClientThread        clientThread;
    private static final String TAG = "BlueMesh Service";

    // BMS constructor
    public BlueMeshService(UUID a_uuid) throws NullPointerException {

        // Gets bluetooth hardware from phone and makes sure that it is
        // non-null;
        adapter = BluetoothAdapter.getDefaultAdapter();
        
        // If bluetooth hardware does not exist...
        if (adapter == null) {
            if (Constants.DEBUG)
                Log.d(TAG, "BluetoothAdapter is null");
            throw new NullPointerException("BluetoothAdapter is null");
        } else {
            if (Constants.DEBUG)
                Log.d(TAG, "BluetoothAdapter is is non-null");
        }
        
        //Try to restart bluetooth
        if(adapter.isEnabled()){
            Log.d(TAG, "disable");
            if(adapter.disable()){
                Log.d(TAG, "waiting...");
                while( adapter.isEnabled()){}
            }
            else{
                Log.d(TAG, "failed");
            }
        }
        
        Log.d(TAG, "enable");
        if(adapter.enable()){
            Log.d(TAG, "waiting...");
            while( !adapter.isEnabled()){}
        }
        else{
            Log.d(TAG, "failed");
        }
        
        // Create a new router object
        router = new RouterObject(adapter.getAddress());
        if (Constants.DEBUG)
            Log.d(TAG, "Router Object Created");
        // Try to create a new ServerThread
        try {
            serverThread = new ServerThread(adapter, router, a_uuid);
        } catch (NullPointerException e) {
            throw e;
        }
        if (Constants.DEBUG)
            Log.d(TAG, "Sever Thread Created");
        // Create a new clientThread
        clientThread = new ClientThread(adapter, router, a_uuid);
        if (Constants.DEBUG)
            Log.d(TAG, "Client Thread Created");
    }

    // TODO: Implement later if needed
    public int config() {
        return Constants.SUCCESS;
    }

    public int launch() {

        // TODO: Conditionals are untested
        if (!serverThread.isAlive()) {
            serverThread.start();
        }
        if (!clientThread.isAlive()) {
            clientThread.start();
        }
        return Constants.SUCCESS;
    }

    // function that writes message to all devices
    public int write(byte[] buffer) {
        router.write(buffer, Constants.MESSAGE_ALL, null);
        return Constants.SUCCESS;
    }
    
    public int write(byte[] buffer, BluetoothDevice target) {
    	router.write(buffer, Constants.MESSAGE_TARGET, target); //TODO: Insert Handling in router for alternate messages.
    	return Constants.SUCCESS;
    }

    // function to grab most recent message off of message queue
    // (message stack actually a linked list but is used like a queue)
    public byte[] pull() {

        return router.getNextMessage();
    }

    public int getNumberOfDevicesOnNetwork() {
    	//Not Implemented
        return 0;//router.getNumberOfDevicesOnNetwork();
    }

    // Returns the Bluetooth name of the device
    public String getMyDeviceName() {
        return adapter.getName();
    }

    // Kills threads and stops all communications
    public int disconnect() {
        Log.d(TAG, "kill start");

        // TODO: check if conditionals fixes bug
        // disconnecting when bluetooth not enabeled
        if (this.clientThread != null) {
            this.clientThread.kill();
            this.clientThread = null;
        }

        if (this.serverThread != null) {
            this.serverThread.kill();
            this.serverThread = null;
        }

        if (this.router != null) {
            this.router.stop();
            this.router = null;
        }

        Log.d(TAG, "kill success");
        return Constants.SUCCESS;
    }

}
