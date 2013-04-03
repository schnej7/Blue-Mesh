package blue.mesh;

public final class Constants {

    // Return codes
    public static final int     SUCCESS                          = 0;
    public static final int     FAILURE                          = -1;

    // State of a device
    public static final int     STATE_NONE                       = 1;
    public static final int     STATE_CONNECTED                  = 2;

    // Maximum length of message to be sent
    public static final int     MAX_MESSAGE_LEN                  = 1024;

    // Length of the unique message id
    public static final int     MESSAGE_ID_LEN                   = 4;
    
    // Length of a Target Identifier (Length of address taken in characters)
    public static final int		TARGET_ID_LEN					 = 8;

    // Number of messages to be remembered
    public static final int     MSG_HISTORY_LEN                  = 100;

    // Source of a message
    public static final int     SRC_ME                           = -1;
    public static final int     SRC_OTHER                        = 0;

    // Unique ID for BlueMesh applications
    public static final String  NAME                             = "bluemesh";
    //TODO: Update DEAFAULT_UUID_STRING before beta launch
    public static final String  DEFAULT_UUID_STRING              = "bbbbbbb1-afac-11de-8a39-0800200c9a66";
    public static final String  DEFAULT_DEVICE_ID                = "bluemesh_device";

    // Debug flag
    public static Boolean DEBUG           		                 = true;

    // The slot previously used for that (first byte of message packet) is now used
    // to differentiate between a message to all and a message to one machine.
    public static final byte	MESSAGE_ALL						 = 1;
    public static final byte	MESSAGE_TARGET					 = 2;

    // Used to determine the type of a system level message
    public static final byte    SYSTEM_MSG_TOTAL_DEVICE_QUERY    = 1;
    public static final byte    SYSTEM_MSG_TOTAL_DEVICE_CHECK_IN = 2;

    // Used to determine the type of connection
    public static final String TYPE_BLUETOOTH                    = "1";
    // Size of the message header in bytes which describes message size
	public static final int HEADER_SIZE 						 =  4;
}