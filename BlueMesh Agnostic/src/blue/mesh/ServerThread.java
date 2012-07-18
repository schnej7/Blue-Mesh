package blue.mesh;

import java.io.IOException;
import java.util.UUID;

public class ServerThread extends Thread {

    private static final String   TAG = "ServerThread";
    private RouterObject          router;
    private ServerInterface		  server;
    private UUID                  uuid;

    protected ServerThread(ServerInterface mServerInterface,
            RouterObject mRouterObject, UUID a_uuid)
            throws NullPointerException {

        uuid = a_uuid;
        router = mRouterObject;
        server = mServerInterface;
    }

    public void run() {
    	
        // Listen to the server socket if we're not connected
        while (true) {

            // Exit while loop if interrupted
            if (this.isInterrupted()) {
                break;
            }
            BluetoothObject[] list = server.ReturnObjects();
            
        }
        return;
    }

    protected void kill() {
        this.interrupt();
    }
}

