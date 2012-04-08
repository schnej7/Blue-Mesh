package blue.mesh;

import java.util.UUID;

public final class Constants {

    //Return codes
    public static final int     SUCCESS                          = 0;
    public static final int     FAILURE                          = -1;

    //State of a device
    public static final int     STATE_NONE                       = 1;
    public static final int     STATE_CONNECTED                  = 2;

    //Maximum length of message to be sent
    public static final int     MAX_MESSAGE_LEN                  = 1024;

    //Length of the unique message id
    public static final int     MESSAGE_ID_LEN                   = 4;

    //Number of messages to be remembered
    public static final int     MSG_HISTORY_LEN                  = 100;

    //Source of a message
    public static final int     SRC_ME                           = -1;
    public static final int     SRC_OTHER                        = 0;

    //Unique ID for BlueMesh applications
    public static final UUID    MY_UUID                          = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
    public static final String  NAME                             = "bluemesh";

    //Debug flag
    public static final Boolean DEBUG                            = true;

    //Used to determine at what lever a message originated
    public static final byte    BYTE_LEVEL_USER                  = 1;
    public static final byte    BYTE_LEVEL_SYSTEM                = 2;

    //Used to determine the type of a system level message
    public static final byte    SYSTEM_MSG_TOTAL_DEVICE_QUERY    = 1;
    public static final byte    SYSTEM_MSG_TOTAL_DEVICE_CHECK_IN = 2;

}