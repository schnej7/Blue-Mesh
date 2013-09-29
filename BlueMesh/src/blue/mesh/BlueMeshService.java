package blue.mesh;

import java.util.ArrayList;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.util.Log;

public class BlueMeshService {

    private RouterObject        router;
    private UUID                uuid;
    private String              uniqueDeviceId;
    private boolean             bluetoothEnabled = false;
    private String              bluetoothName = null;
    private static final String TAG = "BlueMesh Service";
    
    //Used to store all Bluetoot Connection Threads
    private ArrayList <BluetoothConnectionThread> bluetoothConnectionThreads = new ArrayList<BluetoothConnectionThread>();

    private void setupBluetooth() throws NullPointerException{
        
        //Detect the OS and add the connection threads accordingly
        if( Utils.isOS( Utils.OS.ANDROID ) ){
        	
            // Gets bluetooth hardware from phone and makes sure that it is
            // non-null;
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            
            // If bluetooth hardware does not exist...
            if (adapter == null) {
            	if(Constants.DEBUG) Log.d(TAG, "BluetoothAdapter is is null");
                throw new NullPointerException("BluetoothAdapter is null");
            } else {
            	if(Constants.DEBUG) Log.d(TAG, "BluetoothAdapter is is non-null");
            }
            
            bluetoothName = adapter.getName();
        	
            try {
                bluetoothConnectionThreads.add( new ServerThread(adapter, router, uuid) );
            } catch (NullPointerException e) {
            	if(Constants.DEBUG) Log.e(TAG, "Could not add server thread", e);
                throw e;
            }
            if (Constants.DEBUG) Log.d(TAG, "Sever Thread Created");
            // Create a new clientThread
            bluetoothConnectionThreads.add( new ClientThread(adapter, this, router, uuid) );
            if (Constants.DEBUG) Log.d(TAG, "Client Thread Created");
        }
        else if( Utils.isOS( Utils.OS.WINDOWS ) ){
            //TODO: implement this
        	throw new NullPointerException("BlueMesh not supported on windows");
        }
        else if( Utils.isOS( Utils.OS.LINUX ) ){
            //TODO: implement this
        	throw new NullPointerException("BlueMesh not supported on linux");
        }
        else if( Utils.isOS( Utils.OS.OSX ) ){
            //TODO: implement this
        	throw new NullPointerException("BlueMesh not supported on OSX");
        }
        else{
        	throw new NullPointerException("BlueMesh not supported on this device");
        }
    }
    
    private void setupRouter(){
        router = new RouterObject(uniqueDeviceId);
        if (Constants.DEBUG) Log.d(TAG, "Router Object Created");  
    }
    
    //This is called from the builder once all the configurations are set
    protected void setup(){
        setupRouter();

        if( bluetoothEnabled ){
            setupBluetooth();
        }
    }
    
    protected BlueMeshService(){
    }
    
    protected void setUUID( UUID a_uuid ){
        uuid = a_uuid;
    }
    
    protected void enableBluetooth( boolean enabled ){
        bluetoothEnabled = enabled;
    }
    
    protected void setDeviceId( String Id ){
        uniqueDeviceId = Id;
    }

    public void launch() {
        for( BluetoothConnectionThread bct: bluetoothConnectionThreads)
        if (!bct.isAlive()) {
            bct.start();
        }
    }

    // function that writes message to all devices
    @Deprecated
    public void write(byte[] buffer) {
        router.write(buffer, Constants.MESSAGE_ALL, null);
    }
    
    public void send(Message message){
    	
    }
    
    public Message pullMessage(){
    	
    }
    
    @Deprecated
    public void write(byte[] buffer, BluetoothDevice target) {
    	router.write(buffer, Constants.MESSAGE_TARGET, target);
    }

    // function to grab most recent message off of message queue
    // (message stack actually a linked list but is used like a queue)
    @Deprecated
    public byte[] pull() throws NullPointerException{
        if( router == null ){
            throw new NullPointerException("router is null");
        }
        return router.getNextMessage();
    }

    public int getNumberOfDevicesOnNetwork() {
    	//Not Implemented
        return 0;//router.getNumberOfDevicesOnNetwork();
    }

    // Returns the Bluetooth name of the device
    public String getMyDeviceName() {
        return bluetoothName;
    }

    // Kills threads and stops all communications
    public int disconnect() {
    	if(Constants.DEBUG) Log.d(TAG, "kill start");
    	if(Constants.DEBUG) Log.d(TAG, "Killing " + bluetoothConnectionThreads.size());
        for( BluetoothConnectionThread bct: bluetoothConnectionThreads ){
        	if(Constants.DEBUG) Log.d(TAG, "Killing " + bct.getName());
            if (bct != null) {
                bct.kill();
                bct = null;
            }
        }

        if (this.router != null) {
            this.router.stop();
            this.router = null;
        }

        if(Constants.DEBUG) Log.d(TAG, "kill success");
        return Constants.SUCCESS;
    }

}
