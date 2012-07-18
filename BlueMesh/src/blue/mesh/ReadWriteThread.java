package blue.mesh;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

public class ReadWriteThread extends Thread {

    private final InputStream     in;
    private final OutputStream    out;
    private final RouterObject    router;
    private final BluetoothSocket socket;
    private static final String   TAG = "ReadWriteThread";

    protected ReadWriteThread(RouterObject mRouter, BluetoothSocket mSocket) throws IOException {

        Log.d(TAG, "RWTHREAD CONNECTED!");

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
            throw e;
        }

        in = tmpIn;
        out = tmpOut;
    }

    public void run() {
        byte[] buffer = new byte[Constants.MAX_MESSAGE_LEN];

        // Keep listening to the InputStream while connected
        while (true) {

            if (this.isInterrupted()) {
                break;
            }

            try {
                // Read from the InputStream
                int bytes = in.read(buffer);
                byte[] newBuffer = new byte[bytes];

                for (int i = 0; i < bytes; i++) {
                    newBuffer[i] = buffer[i];
                }

                if (bytes < 1) {
                    Log.d(TAG, "DATA READ!1");
                    // Send the obtained bytes to the RouterThread
                    router.route(newBuffer, Constants.SRC_OTHER);
                    Log.d(TAG, "DATA READ!2");
                } else {
                    Log.d(TAG, "No data read");
                }

            } catch (IOException e) {
                Log.e(TAG, "disconnected", e);
                break;
            }
        }

        disconnect();

        return;
    }
    
    private int disconnect(){
        // On exit close the in and out sockets and the Bluetooth socket
        router.notifyDisconnected(this.socket.getRemoteDevice());
        try {
            in.close();
            Log.d(TAG, "in closed");
        } catch (IOException e) {
            Log.e(TAG, "could not close in", e);
        }
        try {
            out.close();
            Log.d(TAG, "out closed");
        } catch (IOException e) {
            Log.e(TAG, "could not close out", e);
        }
        try {
        	socket.close();
        } catch (IOException e){
        	Log.e(TAG, "could not close socket", e);
        }
        Log.d(TAG, "ReadWriteThread returned");
        return Constants.SUCCESS;
    }

    protected int write(byte[] buffer) {
        Log.d(TAG, "Writing bytes");
        try {
            out.write(buffer);
        } catch (IOException e) {
            Log.e(TAG, "Exception during write", e);
            disconnect();
        }
        return Constants.SUCCESS;
    }

    protected BluetoothSocket getSocket() {
        return socket;
    }
}
