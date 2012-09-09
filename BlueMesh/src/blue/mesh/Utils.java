package blue.mesh;

public final class Utils {
    
    //No Constructor
    private Utils(){
    }
    
    protected enum OS { 
        ANDROID ( "linux", "android" ),
        WINDOWS ( "win",   "oracle" ), 
        OSX     ( "xxxxx", "xxxxx"),
        LINUX   ( "linux", "oracle");

        OS(String a_os_name, String a_vendor_url) {
            this.os_name = a_os_name;
            this.vendor_url = a_vendor_url;
        }

        private final String os_name;
        private final String vendor_url;
    };

    protected static boolean isOS( OS os ) {
        String os_string = System.getProperty("os.name").toLowerCase();
        String vendor_url_string = System.getProperty("java.vendor.url").toLowerCase();
        return ( os_string.contains( os.os_name ) && vendor_url_string.contains( os.vendor_url ) );
    }
}
