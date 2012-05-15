package blue.mesh;

import java.io.IOException;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

public class ServerThread extends Thread {

    private static final String   TAG = "ServerThread";
    private BluetoothAdapter      adapter;
    private RouterObject          router;
    private BluetoothServerSocket serverSocket;
    private UUID                  uuid;

    protected ServerThread(BluetoothAdapter mAdapter,
            RouterObject mRouterObject, UUID a_uuid)
            throws NullPointerException {

        uuid = a_uuid;
        adapter = mAdapter;
        router = mRouterObject;

        // Attempt to listen on ServerSocket for incoming requests
        BluetoothServerSocket tmp = null;

        if (Constants.DEBUG)
            Log.d(TAG, "Attempting to listen");

        // Create a new listening server socket
        try {
            tmp = adapter.listenUsingRfcommWithServiceRecord(Constants.NAME,
                    uuid);
        } catch (IOException e) {
            Log.e(TAG, "listenUsingRfcommWithServiceRecord() failed", e);
            throw new NullPointerException("Bluetooth is not enabeled");
        }

        serverSocket = tmp;
    }

    public void run() {

        BluetoothSocket socket = null;

        // Listen to the server socket if we're not connected
        while (true) {

            // Exit while loop if interrupted
            if (this.isInterrupted()) {
                if (Constants.DEBUG)
                    Log.d(TAG, "interrupted");
                break;
            }

            // Try to accept a client socket and connect to it
            try {
                socket = serverSocket.accept();
            } catch (IOException e) {
                Log.e(TAG, "accept() failed", e);
            }

            // If a connection was accepted, pass socket to router
            if (socket != null) {
                Log.d(TAG, "Socket connected, calling router.beginConnection()");
                router.beginConnection(socket);
            }

            socket = null;
        }
        return;
    }

    protected int closeSocket() {
        try {
            this.serverSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Could not close serverSocket", e);
        }
        return Constants.SUCCESS;
    }

    protected int kill() {
        this.closeSocket();
        this.interrupt();
        Log.d(TAG, "kill success");
        return Constants.SUCCESS;
    }
}
