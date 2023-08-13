public class BufferManagerReadStream
    implements BufferManagerRead, MarkAndResetHandler
{
    private boolean receivedCancel = false;
    private int cancelReqId = 0;
    private boolean endOfStream = true;
    private BufferQueue fragmentQueue = new BufferQueue();
    private long FRAGMENT_TIMEOUT = 60000;
    private ORB orb ;
    private ORBUtilSystemException wrapper ;
    private boolean debug = false;
    BufferManagerReadStream( ORB orb )
    {
        this.orb = orb ;
        this.wrapper = ORBUtilSystemException.get( orb,
            CORBALogDomains.RPC_ENCODING ) ;
        debug = orb.transportDebugFlag;
    }
    public void cancelProcessing(int requestId) {
        synchronized(fragmentQueue) {
            receivedCancel = true;
            cancelReqId = requestId;
            fragmentQueue.notify();
        }
    }
    public void processFragment(ByteBuffer byteBuffer, FragmentMessage msg)
    {
        ByteBufferWithInfo bbwi =
            new ByteBufferWithInfo(orb, byteBuffer, msg.getHeaderLength());
        synchronized (fragmentQueue) {
            if (debug)
            {
                int bbAddress = System.identityHashCode(byteBuffer);
                StringBuffer sb = new StringBuffer(80);
                sb.append("processFragment() - queueing ByteBuffer id (");
                sb.append(bbAddress).append(") to fragment queue.");
                String strMsg = sb.toString();
                dprint(strMsg);
            }
            fragmentQueue.enqueue(bbwi);
            endOfStream = !msg.moreFragmentsToFollow();
            fragmentQueue.notify();
        }
    }
    public ByteBufferWithInfo underflow (ByteBufferWithInfo bbwi)
    {
      ByteBufferWithInfo result = null;
      try {
        synchronized (fragmentQueue) {
            if (receivedCancel) {
                throw new RequestCanceledException(cancelReqId);
            }
            while (fragmentQueue.size() == 0) {
                if (endOfStream) {
                    throw wrapper.endOfStream() ;
                }
                boolean interrupted = false;
                try {
                    fragmentQueue.wait(FRAGMENT_TIMEOUT);
                } catch (InterruptedException e) {
                    interrupted = true;
                }
                if (!interrupted && fragmentQueue.size() == 0) {
                    throw wrapper.bufferReadManagerTimeout();
                }
                if (receivedCancel) {
                    throw new RequestCanceledException(cancelReqId);
                }
            }
            result = fragmentQueue.dequeue();
            result.fragmented = true;
            if (debug)
            {
                int bbAddr = System.identityHashCode(result.byteBuffer);
                StringBuffer sb1 = new StringBuffer(80);
                sb1.append("underflow() - dequeued ByteBuffer id (");
                sb1.append(bbAddr).append(") from fragment queue.");
                String msg1 = sb1.toString();
                dprint(msg1);
            }
            if (markEngaged == false && bbwi != null && bbwi.byteBuffer != null)
            {
                ByteBufferPool byteBufferPool = getByteBufferPool();
                if (debug)
                {
                    int bbAddress = System.identityHashCode(bbwi.byteBuffer);
                    StringBuffer sb = new StringBuffer(80);
                    sb.append("underflow() - releasing ByteBuffer id (");
                    sb.append(bbAddress).append(") to ByteBufferPool.");
                    String msg = sb.toString();
                    dprint(msg);
                }
                byteBufferPool.releaseByteBuffer(bbwi.byteBuffer);
                bbwi.byteBuffer = null;
                bbwi = null;
            }
        }
        return result;
      } finally {
      }
    }
    public void init(Message msg) {
        if (msg != null)
            endOfStream = !msg.moreFragmentsToFollow();
    }
    public void close(ByteBufferWithInfo bbwi)
    {
        int inputBbAddress = 0;
        if (fragmentQueue != null)
        {
            synchronized (fragmentQueue)
            {
                if (bbwi != null)
                {
                    inputBbAddress = System.identityHashCode(bbwi.byteBuffer);
                }
                ByteBufferWithInfo abbwi = null;
                ByteBufferPool byteBufferPool = getByteBufferPool();
                while (fragmentQueue.size() != 0)
                {
                    abbwi = fragmentQueue.dequeue();
                    if (abbwi != null && abbwi.byteBuffer != null)
                    {
                        int bbAddress = System.identityHashCode(abbwi.byteBuffer);
                        if (inputBbAddress != bbAddress)
                        {
                            if (debug)
                            {
                                 StringBuffer sb = new StringBuffer(80);
                                 sb.append("close() - fragmentQueue is ")
                                   .append("releasing ByteBuffer id (")
                                   .append(bbAddress).append(") to ")
                                   .append("ByteBufferPool.");
                                 String msg = sb.toString();
                                 dprint(msg);
                            }
                        }
                        byteBufferPool.releaseByteBuffer(abbwi.byteBuffer);
                    }
                }
            }
            fragmentQueue = null;
        }
        if (fragmentStack != null && fragmentStack.size() != 0)
        {
            if (bbwi != null)
            {
                inputBbAddress = System.identityHashCode(bbwi.byteBuffer);
            }
            ByteBufferWithInfo abbwi = null;
            ByteBufferPool byteBufferPool = getByteBufferPool();
            ListIterator itr = fragmentStack.listIterator();
            while (itr.hasNext())
            {
                abbwi = (ByteBufferWithInfo)itr.next();
                if (abbwi != null && abbwi.byteBuffer != null)
                {
                   int bbAddress = System.identityHashCode(abbwi.byteBuffer);
                   if (inputBbAddress != bbAddress)
                   {
                       if (debug)
                       {
                            StringBuffer sb = new StringBuffer(80);
                            sb.append("close() - fragmentStack - releasing ")
                              .append("ByteBuffer id (" + bbAddress + ") to ")
                              .append("ByteBufferPool.");
                            String msg = sb.toString();
                            dprint(msg);
                       }
                       byteBufferPool.releaseByteBuffer(abbwi.byteBuffer);
                   }
                }
            }
            fragmentStack = null;
        }
    }
    protected ByteBufferPool getByteBufferPool()
    {
        return orb.getByteBufferPool();
    }
    private void dprint(String msg)
    {
        ORBUtility.dprint("BufferManagerReadStream", msg);
    }
    private boolean markEngaged = false;
    private LinkedList fragmentStack = null;
    private RestorableInputStream inputStream = null;
    private Object streamMemento = null;
    public void mark(RestorableInputStream inputStream)
    {
        this.inputStream = inputStream;
        markEngaged = true;
        streamMemento = inputStream.createStreamMemento();
        if (fragmentStack != null) {
            fragmentStack.clear();
        }
    }
    public void fragmentationOccured(ByteBufferWithInfo newFragment)
    {
        if (!markEngaged)
            return;
        if (fragmentStack == null)
            fragmentStack = new LinkedList();
        fragmentStack.addFirst(new ByteBufferWithInfo(newFragment));
    }
    public void reset()
    {
        if (!markEngaged) {
            return;
        }
        markEngaged = false;
        if (fragmentStack != null && fragmentStack.size() != 0) {
            ListIterator iter = fragmentStack.listIterator();
            synchronized(fragmentQueue) {
                while (iter.hasNext()) {
                    fragmentQueue.push((ByteBufferWithInfo)iter.next());
                }
            }
            fragmentStack.clear();
        }
        inputStream.restoreInternalState(streamMemento);
    }
    public MarkAndResetHandler getMarkAndResetHandler() {
        return this;
    }
}
