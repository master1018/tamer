public class ByteBufferPoolImpl implements ByteBufferPool
{
    private ORB itsOrb;
    private int itsByteBufferSize;
    private ArrayList itsPool;
    private int itsObjectCounter = 0;
    private boolean debug;
    public ByteBufferPoolImpl(ORB theORB)
    {
        itsByteBufferSize = theORB.getORBData().getGIOPFragmentSize();
        itsPool = new ArrayList();
        itsOrb = theORB;
        debug = theORB.transportDebugFlag;
    }
    public ByteBuffer getByteBuffer(int theAskSize)
    {
        ByteBuffer abb = null;
        if ((theAskSize <= itsByteBufferSize) &&
            !itsOrb.getORBData().disableDirectByteBufferUse())
        {
            int poolSize;
            synchronized (itsPool)
            {
                poolSize = itsPool.size();
                if (poolSize > 0)
                {
                    abb = (ByteBuffer)itsPool.remove(poolSize - 1);
                    abb.clear();
                }
            }
            if (poolSize <= 0)
            {
                abb = ByteBuffer.allocateDirect(itsByteBufferSize);
            }
            itsObjectCounter++;
        }
        else
        {
            abb = ByteBuffer.allocate(theAskSize);
        }
        return abb;
    }
    public void releaseByteBuffer(ByteBuffer thebb)
    {
        if (thebb.isDirect())
        {
            synchronized (itsPool)
            {
                boolean refInPool = false;
                int bbAddr = 0;
                if (debug)
                {
                    for (int i = 0; i < itsPool.size() && refInPool == false; i++)
                    {
                         ByteBuffer tmpbb = (ByteBuffer)itsPool.get(i);
                         if (thebb == tmpbb)
                         {
                             refInPool = true;
                             bbAddr = System.identityHashCode(thebb);
                         }
                    }
                }
                if (refInPool == false || debug == false)
                {
                    itsPool.add(thebb);
                }
                else 
                {
                    String threadName = Thread.currentThread().getName();
                    Throwable t =
                            new Throwable(threadName +
                                         ": Duplicate ByteBuffer reference (" +
                                         bbAddr + ")");
                    t.printStackTrace(System.out);
                }
            }
            itsObjectCounter--;
        }
        else
        {
            thebb = null;
        }
    }
    public int activeCount()
    {
         return itsObjectCounter;
    }
}
