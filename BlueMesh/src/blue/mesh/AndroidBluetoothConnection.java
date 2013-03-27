package blue.mesh;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.bluetooth.BluetoothSocket;
import android.util.Log;


public class AndroidBluetoothConnection extends Connection{
    
    private static final String TAG = "AndroidBluetoothConnection";
    private byte[] incommingBuffer = new byte[Constants.MAX_MESSAGE_LEN];
    private String type = Constants.TYPE_BLUETOOTH;
    
    private InputStream input;
    private OutputStream output;
    private BluetoothSocket socket;
    
    //Must not use default constructor
    @SuppressWarnings("unused")
    private AndroidBluetoothConnection(){
    }
    
    public AndroidBluetoothConnection( BluetoothSocket a_socket ) throws IOException{
        input = a_socket.getInputStream();
        output = a_socket.getOutputStream();
        socket = a_socket;
    }
    
    @Override
    public void close() {
        try {
            input.close();
        } catch (IOException e) {
            if(Constants.DEBUG) Log.e(TAG, "Problem closing input", e);
        }
        try {
            output.close();
        } catch (IOException e) {
        	if(Constants.DEBUG) Log.e(TAG, "Problem closing output", e);
        }
        try {
            socket.close();
        } catch (IOException e) {
        	if(Constants.DEBUG) Log.e(TAG, "Problem closing BluetoothSocket", e);
        }
    }

    @Override
    public void write(byte[] b) {
        try {
            output.write( b );
        } catch (IOException e) {
        	if(Constants.DEBUG) Log.e( TAG, "failed to write", e );
        }
    }

    @Override
    public int read(byte[] b) throws IOException {
        int bytes = 0;
        try {
            bytes = input.read(incommingBuffer);
        } catch (IOException e) {
        	if(Constants.DEBUG) Log.e(TAG, "Error reading", e);
            throw new IOException();
        }

        for (int i = 0; i < bytes; i++) {
            b[i] = incommingBuffer[i];
        }
        return bytes;
    }
    
    public String getID(){
        return type + '@' + socket.getRemoteDevice().toString();
    }
    
}
