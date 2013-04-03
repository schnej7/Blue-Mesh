package blue.mesh;

import java.io.IOException;

public abstract class Connection {
    public abstract void close();
    public abstract void write( byte[] b );
    public abstract int read( byte[] b ) throws IOException;
    public abstract int read( byte[] b, int offset, int len) throws IOException;
    public abstract String getID();
}
