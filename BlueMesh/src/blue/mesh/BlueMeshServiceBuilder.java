package blue.mesh;

import java.util.UUID;

public class BlueMeshServiceBuilder {
    
    private boolean bluetoothEnabled;
    private UUID uuid;
    private String deviceId;

    public BlueMeshServiceBuilder(){
        bluetoothEnabled = false;
        uuid = UUID.fromString(Constants.DEFAULT_UUID_STRING);
        deviceId = "";
    }
    
    //This is the app specific ID, only apps with the same ID
    //will be able to communicate
    public BlueMeshServiceBuilder uuid( UUID a_uuid ){
        uuid = a_uuid;
        return this;
    }
    
    //Enable bluetooth communications for this device
    public BlueMeshServiceBuilder bluetooth( boolean enabled ){
        bluetoothEnabled = enabled;
        return this;
    }
    
    //DeviceIds are used to specify who a message is intended for
    public BlueMeshServiceBuilder deviceId( String a_deviceId ){
        deviceId = a_deviceId;
        return this;
    }
    
    //Call this to construct a BlueMeshService object once
    //all the configurations have been specified
    public BlueMeshService build(){
        BlueMeshService bms = new BlueMeshService();
        
        bms.setUUID(uuid);
        bms.setDeviceId(deviceId);
        bms.enableBluetooth(bluetoothEnabled);
        bms.setup();
        
        return bms;
    }
}
