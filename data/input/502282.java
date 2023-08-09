abstract class DataChannel {
    protected ImpsConnection mConnection;
    protected PrimitiveParser mParser;
    protected PrimitiveSerializer mSerializer;
    protected long mMinPollMillis;
    protected DataChannel(ImpsConnection connection) throws ImException {
        mConnection = connection;
    }
    public abstract void connect() throws ImException;
    public abstract void suspend();
    public abstract boolean resume();
    public abstract void shutdown();
    public abstract void sendPrimitive(Primitive p);
    public abstract Primitive receivePrimitive() throws InterruptedException;
    public abstract long getLastActiveTime();
    public abstract boolean isSendingQueueEmpty();
    public abstract void startKeepAlive(long interval);
    public void setServerMinPoll(long interval)
    {
        mMinPollMillis = interval * 1000;
    }
}
