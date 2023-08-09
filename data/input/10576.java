public class BufferManagerWriteGrow extends BufferManagerWrite
{
    BufferManagerWriteGrow( ORB orb )
    {
        super(orb) ;
    }
    public boolean sentFragment() {
        return false;
    }
    public int getBufferSize() {
        return orb.getORBData().getGIOPBufferSize();
    }
    public void overflow (ByteBufferWithInfo bbwi)
    {
        bbwi.growBuffer(orb);
        bbwi.fragmented = false;
    }
    public void sendMessage ()
    {
        Connection conn =
              ((OutputObject)outputObject).getMessageMediator().getConnection();
        conn.writeLock();
        try {
            conn.sendWithoutLock((OutputObject)outputObject);
            sentFullMessage = true;
        } finally {
            conn.writeUnlock();
        }
    }
    public void close() {}
}
