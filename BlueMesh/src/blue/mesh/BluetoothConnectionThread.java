package blue.mesh;

public abstract class BluetoothConnectionThread extends Thread {

    //So that no-one can use the default constructor
    public BluetoothConnectionThread(){
    }
    
    //Used to stop making connections
    protected abstract int kill();
    
}