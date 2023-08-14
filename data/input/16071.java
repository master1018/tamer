public class ByteBufferWithInfo
{
    private ORB orb;
    private boolean debug;
    private int     index;     
    public ByteBuffer byteBuffer;
    public int     buflen;     
    public int     needed;     
    public boolean fragmented; 
    public ByteBufferWithInfo(org.omg.CORBA.ORB orb,
                              ByteBuffer byteBuffer,
                              int index)
    {
        this.orb = (com.sun.corba.se.spi.orb.ORB)orb;
        debug = this.orb.transportDebugFlag;
        this.byteBuffer = byteBuffer;
        if (byteBuffer != null)
        {
            this.buflen = byteBuffer.limit();
        }
        position(index);
        this.needed = 0;
        this.fragmented = false;
    }
    public ByteBufferWithInfo(org.omg.CORBA.ORB orb, ByteBuffer byteBuffer)
    {
        this(orb, byteBuffer, 0);
    }
    public ByteBufferWithInfo(org.omg.CORBA.ORB orb,
                              BufferManagerWrite bufferManager)
    {
        this(orb, bufferManager, true);
    }
    public ByteBufferWithInfo(org.omg.CORBA.ORB orb,
                              BufferManagerWrite bufferManager,
                              boolean usePooledByteBuffers)
    {
        this.orb = (com.sun.corba.se.spi.orb.ORB)orb;
        debug = this.orb.transportDebugFlag;
        int bufferSize = bufferManager.getBufferSize();
        if (usePooledByteBuffers)
        {
            ByteBufferPool byteBufferPool = this.orb.getByteBufferPool();
            this.byteBuffer = byteBufferPool.getByteBuffer(bufferSize);
            if (debug)
            {
                int bbAddress = System.identityHashCode(byteBuffer);
                StringBuffer sb = new StringBuffer(80);
                sb.append("constructor (ORB, BufferManagerWrite) - got ")
                  .append("ByteBuffer id (").append(bbAddress)
                  .append(") from ByteBufferPool.");
                String msgStr = sb.toString();
                dprint(msgStr);
            }
        }
        else
        {
             this.byteBuffer = ByteBuffer.allocate(bufferSize);
        }
        position(0);
        this.buflen = bufferSize;
        this.byteBuffer.limit(this.buflen);
        this.needed = 0;
        this.fragmented = false;
    }
    public ByteBufferWithInfo (ByteBufferWithInfo bbwi)
    {
        this.orb = bbwi.orb;
        this.debug = bbwi.debug;
        this.byteBuffer = bbwi.byteBuffer;
        this.buflen = bbwi.buflen;
        this.byteBuffer.limit(this.buflen);
        position(bbwi.position());
        this.needed = bbwi.needed;
        this.fragmented = bbwi.fragmented;
    }
    public int getSize()
    {
        return position();
    }
    public int getLength()
    {
         return buflen;
    }
    public int position()
    {
        return index;
    }
    public void position(int newPosition)
    {
        byteBuffer.position(newPosition);
        index = newPosition;
    }
    public void setLength(int theLength)
    {
        buflen = theLength;
        byteBuffer.limit(buflen);
    }
    public void growBuffer(com.sun.corba.se.spi.orb.ORB orb)
    {
        int newLength = byteBuffer.limit() * 2;
        while (position() + needed >= newLength)
            newLength = newLength * 2;
        ByteBufferPool byteBufferPool = orb.getByteBufferPool();
        ByteBuffer newBB = byteBufferPool.getByteBuffer(newLength);
        if (debug)
        {
            int newbbAddress = System.identityHashCode(newBB);
            StringBuffer sb = new StringBuffer(80);
            sb.append("growBuffer() - got ByteBuffer id (");
            sb.append(newbbAddress).append(") from ByteBufferPool.");
            String msgStr = sb.toString();
            dprint(msgStr);
        }
        byteBuffer.position(0);
        newBB.put(byteBuffer);
        if (debug)
        {
            int bbAddress = System.identityHashCode(byteBuffer);
            StringBuffer sb = new StringBuffer(80);
            sb.append("growBuffer() - releasing ByteBuffer id (");
            sb.append(bbAddress).append(") to ByteBufferPool.");
            String msgStr2 = sb.toString();
            dprint(msgStr2);
        }
        byteBufferPool.releaseByteBuffer(byteBuffer);
        byteBuffer = newBB;
        buflen = newLength;
        byteBuffer.limit(buflen);
    }
    public String toString()
    {
        StringBuffer str = new StringBuffer("ByteBufferWithInfo:");
        str.append(" buflen = " + buflen);
        str.append(" byteBuffer.limit = " + byteBuffer.limit());
        str.append(" index = " + index);
        str.append(" position = " + position());
        str.append(" needed = " + needed);
        str.append(" byteBuffer = " + (byteBuffer == null ? "null" : "not null"));
        str.append(" fragmented = " + fragmented);
        return str.toString();
    }
    protected void dprint(String msg)
    {
        ORBUtility.dprint("ByteBufferWithInfo", msg);
    }
}
