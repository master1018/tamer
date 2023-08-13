public abstract class BufferManagerWrite
{
    protected ORB orb ;
    protected ORBUtilSystemException wrapper ;
    BufferManagerWrite( ORB orb )
    {
        this.orb = orb ;
        this.wrapper = ORBUtilSystemException.get( orb,
            CORBALogDomains.RPC_ENCODING ) ;
    }
    public abstract boolean sentFragment();
    public boolean sentFullMessage() {
        return sentFullMessage;
    }
    public abstract int getBufferSize();
    public abstract void overflow (ByteBufferWithInfo bbwi);
    public abstract void sendMessage ();
    public void setOutputObject(Object outputObject) {
        this.outputObject = outputObject;
    }
     abstract public void close();
    protected Object outputObject;
    protected boolean sentFullMessage = false;
}
