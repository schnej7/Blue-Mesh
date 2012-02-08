package blue.mesh;

import java.util.UUID;

public final class Constants {

	public static final int SUCCESS = 0;
	public static final int FAILURE = -1;
	
	public static final int STATE_NONE = 1;
	public static final int STATE_CONNECTED = 2;
	
	public static final int MAX_MESSAGE_LEN = 1024;
	
    public static final UUID MY_UUID = 
    		UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
    public static final String NAME = "bluemesh";

    public static final Boolean DEBUG = true;
}