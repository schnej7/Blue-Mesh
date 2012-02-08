package blue.mesh;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;



public class ReadWriteThread extends Thread{

	private final InputStream in;
	private final OutputStream out;
	private final RouterObject router;
	private final BluetoothSocket socket;
	private static final String TAG = "ReadWriteThread";

	public ReadWriteThread(
			RouterObject mRouter, 
			BluetoothSocket mSocket) {

		router = mRouter;
		socket = mSocket;

		InputStream tmpIn = null;
		OutputStream tmpOut = null;

		// Get the BluetoothSocket input and output streams
		try {
			tmpIn = socket.getInputStream();
			tmpOut = socket.getOutputStream();
		} catch (IOException e) {
			Log.e(TAG, "temp sockets not created", e);
		}

		in = tmpIn;
		out = tmpOut;
	}

	public void run()
	{
        byte[] buffer = new byte[Constants.MAX_MESSAGE_LEN];

        // Keep listening to the InputStream while connected
        while (true) {
        	
        	if( this.isInterrupted() ){
        		//TODO: Do some kind of cleanup
        		return;
        	}
        	
            try {
                // Read from the InputStream
                in.read( buffer );

                // Send the obtained bytes to the RouterThread
               router.route( buffer );
                
            } catch (IOException e) {
                Log.e(TAG, "disconnected", e);
                //TODO: The connection has been lost, handle this!
                break;
            }
        }
	}

	int write(byte [] buffer){
		try {
			out.write(buffer);
		} catch (IOException e) {
			Log.e(TAG, "Exception during write", e);
		}
		return Constants.SUCCESS;
	}
}
