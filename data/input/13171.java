public class BufferManagerWriteCollect extends BufferManagerWrite
{
    private BufferQueue queue = new BufferQueue();
    private boolean sentFragment = false;
    private boolean debug = false;
    BufferManagerWriteCollect(ORB orb)
    {
        super(orb);
         if (orb != null)
            debug = orb.transportDebugFlag;
    }
    public boolean sentFragment() {
        return sentFragment;
    }
    public int getBufferSize() {
        return orb.getORBData().getGIOPFragmentSize();
    }
    public void overflow (ByteBufferWithInfo bbwi)
    {
        MessageBase.setFlag(bbwi.byteBuffer, Message.MORE_FRAGMENTS_BIT);
        queue.enqueue(bbwi);
        ByteBufferWithInfo newBbwi = new ByteBufferWithInfo(orb, this);
        newBbwi.fragmented = true;
        ((CDROutputObject)outputObject).setByteBufferWithInfo(newBbwi);
        FragmentMessage header =
              ((CDROutputObject)outputObject).getMessageHeader()
                                             .createFragmentMessage();
        header.write((CDROutputObject)outputObject);
    }
    public void sendMessage ()
    {
        queue.enqueue(((CDROutputObject)outputObject).getByteBufferWithInfo());
        Iterator bufs = iterator();
        Connection conn =
                          ((OutputObject)outputObject).getMessageMediator().
                                                       getConnection();
        conn.writeLock();
        try {
            ByteBufferPool byteBufferPool = orb.getByteBufferPool();
            while (bufs.hasNext()) {
                ByteBufferWithInfo bbwi = (ByteBufferWithInfo)bufs.next();
                ((CDROutputObject)outputObject).setByteBufferWithInfo(bbwi);
                conn.sendWithoutLock(((CDROutputObject)outputObject));
                sentFragment = true;
                if (debug)
                {
                    int bbAddress = System.identityHashCode(bbwi.byteBuffer);
                    StringBuffer sb = new StringBuffer(80);
                    sb.append("sendMessage() - releasing ByteBuffer id (");
                    sb.append(bbAddress).append(") to ByteBufferPool.");
                    String msg = sb.toString();
                    dprint(msg);
                }
                byteBufferPool.releaseByteBuffer(bbwi.byteBuffer);
                bbwi.byteBuffer = null;
                bbwi = null;
            }
            sentFullMessage = true;
        } finally {
            conn.writeUnlock();
        }
    }
    public void close()
    {
        Iterator bufs = iterator();
        ByteBufferPool byteBufferPool = orb.getByteBufferPool();
        while (bufs.hasNext())
        {
            ByteBufferWithInfo bbwi = (ByteBufferWithInfo)bufs.next();
            if (bbwi != null && bbwi.byteBuffer != null)
            {
                if (debug)
                {
                    int bbAddress = System.identityHashCode(bbwi.byteBuffer);
                    StringBuffer sb = new StringBuffer(80);
                    sb.append("close() - releasing ByteBuffer id (");
                    sb.append(bbAddress).append(") to ByteBufferPool.");
                    String msg = sb.toString();
                    dprint(msg);
                }
                 byteBufferPool.releaseByteBuffer(bbwi.byteBuffer);
                 bbwi.byteBuffer = null;
                 bbwi = null;
            }
        }
    }
    private void dprint(String msg)
    {
        ORBUtility.dprint("BufferManagerWriteCollect", msg);
    }
    private Iterator iterator ()
    {
        return new BufferManagerWriteCollectIterator();
    }
    private class BufferManagerWriteCollectIterator implements Iterator
    {
        public boolean hasNext ()
        {
            return queue.size() != 0;
        }
        public Object next ()
        {
            return queue.dequeue();
        }
        public void remove ()
        {
            throw new UnsupportedOperationException();
        }
    }
}
