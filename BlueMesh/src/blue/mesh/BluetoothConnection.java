package blue.mesh;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.bluetooth.BluetoothSocket;
import android.util.Log;


public class BluetoothConnection extends Connection{
    
    private static final String TAG = "BluetoothConnection";
    private byte[] incommingBuffer = new byte[Constants.MAX_MESSAGE_LEN];
    private String type = Constants.TYPE_BLUETOOTH;
    
    private InputStream input;
    private OutputStream output;
    private BluetoothSocket socket;
    
    //Must not use default constructor
    @SuppressWarnings("unused")
    private BluetoothConnection(){
    }
    
    public BluetoothConnection( InputStream a_input, OutputStream a_output, BluetoothSocket a_socket ){
        input = a_input;
        output = a_output;
        socket = a_socket;
    }
    
    @Override
    public void close() {
        try {
            input.close();
        } catch (IOException e) {
            Log.e(TAG, "Problem closing input", e);
        }
        try {
            output.close();
        } catch (IOException e) {
            Log.e(TAG, "Problem closing output", e);
        }
        try {
            socket.close();
        } catch (IOException e) {
            Log.e(TAG, "Problem closing BluetoothSocket", e);
        }
    }

    @Override
    public void write(byte[] b) {
        try {
            output.write( b );
        } catch (IOException e) {
            Log.e( TAG, "failed to write", e );
        }
    }

    @Override
    public int read(byte[] b) {
        int bytes = 0;
        try {
            bytes = input.read(incommingBuffer);
        } catch (IOException e) {
            Log.e(TAG, "Error reading", e);
        }
        b = new byte[bytes];

        for (int i = 0; i < bytes; i++) {
            b[i] = incommingBuffer[i];
        }
        return bytes;
    }
    
    public String getID(){
        return type + '@' + socket.toString();
    }
    
}
