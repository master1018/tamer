abstract class CirChannel {
    protected ImpsConnection mConnection;
    protected CirChannel(ImpsConnection connection) {
        mConnection = connection;
    }
    public abstract void connect() throws ImException;
    public void reconnect(){
    }
    public abstract boolean isShutdown();
    public abstract void shutdown();
}
