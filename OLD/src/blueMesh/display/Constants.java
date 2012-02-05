package blueMesh.display;

import java.util.UUID;

public final class Constants {
	
	public static final boolean DEBUG = true;
	
	public static final int MSG_DEBUG = 1;
	public static final int MSG_NORMAL = 2;
	public static final int MSG_SENT = 3;
	
	public static final int FAIL = -1;
	public static final int SUCCESS = 0;
	
	public static final int ERR_STRING_TO_LARGE = 1;
	public static final int STATE_NONE = 2;
	
	public static final int STATE_READY = 3;
	public static final int STATE_BUSY = 4;
	public static final int STATE_FULL = 5;
	
	public static final int AVAILABLE = 6;
	public static final int UNAVAILABLE = 7;
	public static final int INT_OUT_OF_RANGE = 8;
	
	public static final int NUMBER_OF_AVAILABLE_RADIOS = 8;
	
    public static final UUID MY_UUID_SECURE = 
    		UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
    public static final String NAME = "blueMesh";
	public static final String mSocketType = "Secure";
	
	public static final int MESSAGE_QUEUE_SIZE = 100;
}
