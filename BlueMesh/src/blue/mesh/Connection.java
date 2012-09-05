package blue.mesh;

public abstract class Connection {
    public abstract void close();
    public abstract void write( byte[] b );
    public abstract int read( byte[] b );
    public abstract String getID();
}
