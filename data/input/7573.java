public class ReentrantMutex implements Sync  {
    protected Thread holder_ = null;
    protected int counter_ = 0 ;
    protected boolean debug = false ;
    public ReentrantMutex()
    {
        this( false ) ;
    }
    public ReentrantMutex( boolean debug )
    {
        this.debug = debug ;
    }
    public void acquire() throws InterruptedException {
        if (Thread.interrupted())
            throw new InterruptedException();
        synchronized(this) {
            try {
                if (debug)
                    ORBUtility.dprintTrace( this,
                        "acquire enter: holder_=" +
                        ORBUtility.getThreadName(holder_) +
                        " counter_=" + counter_ ) ;
                Thread thr = Thread.currentThread();
                if (holder_ != thr) {
                    try {
                        while (counter_ > 0)
                            wait();
                        if (counter_ != 0)
                            throw new INTERNAL(
                                "counter not 0 when first acquiring mutex" ) ;
                        holder_ = thr;
                    } catch (InterruptedException ex) {
                        notify();
                        throw ex;
                    }
                }
                counter_ ++ ;
            } finally {
                if (debug)
                    ORBUtility.dprintTrace( this, "acquire exit: holder_=" +
                    ORBUtility.getThreadName(holder_) + " counter_=" +
                    counter_ ) ;
            }
        }
    }
    void acquireAll( int count ) throws InterruptedException
    {
        if (Thread.interrupted())
            throw new InterruptedException();
        synchronized(this) {
            try {
                if (debug)
                    ORBUtility.dprintTrace( this,
                        "acquireAll enter: count=" + count + " holder_=" +
                        ORBUtility.getThreadName(holder_) + " counter_=" +
                        counter_ ) ;
                Thread thr = Thread.currentThread();
                if (holder_ == thr) {
                    throw new INTERNAL(
                        "Cannot acquireAll while holding the mutex" ) ;
                } else {
                    try {
                        while (counter_ > 0)
                            wait();
                        if (counter_ != 0)
                            throw new INTERNAL(
                                "counter not 0 when first acquiring mutex" ) ;
                        holder_ = thr;
                    } catch (InterruptedException ex) {
                        notify();
                        throw ex;
                    }
                }
                counter_ = count ;
            } finally {
                if (debug)
                    ORBUtility.dprintTrace( this, "acquireAll exit: count=" +
                    count + " holder_=" + ORBUtility.getThreadName(holder_) +
                    " counter_=" + counter_ ) ;
            }
        }
    }
    public synchronized void release()
    {
        try {
            if (debug)
                ORBUtility.dprintTrace( this, "release enter: " +
                    " holder_=" + ORBUtility.getThreadName(holder_) +
                    " counter_=" + counter_ ) ;
            Thread thr = Thread.currentThread();
            if (thr != holder_)
                throw new INTERNAL(
                    "Attempt to release Mutex by thread not holding the Mutex" ) ;
            else
                counter_ -- ;
            if (counter_ == 0) {
                holder_ = null;
                notify();
            }
        } finally {
            if (debug)
                ORBUtility.dprintTrace( this, "release exit: " +
                    " holder_=" + ORBUtility.getThreadName(holder_) +
                    " counter_=" + counter_ ) ;
        }
    }
    synchronized int releaseAll()
    {
        try {
            if (debug)
                ORBUtility.dprintTrace( this, "releaseAll enter: " +
                    " holder_=" + ORBUtility.getThreadName(holder_) +
                    " counter_=" + counter_ ) ;
            Thread thr = Thread.currentThread();
            if (thr != holder_)
                throw new INTERNAL(
                    "Attempt to releaseAll Mutex by thread not holding the Mutex" ) ;
            int result = counter_ ;
            counter_ = 0 ;
            holder_ = null ;
            notify() ;
            return result ;
        } finally {
            if (debug)
                ORBUtility.dprintTrace( this, "releaseAll exit: " +
                    " holder_=" + ORBUtility.getThreadName(holder_) +
                    " counter_=" + counter_ ) ;
        }
    }
    public boolean attempt(long msecs) throws InterruptedException {
        if (Thread.interrupted())
            throw new InterruptedException();
        synchronized(this) {
            try {
                if (debug)
                    ORBUtility.dprintTrace( this, "attempt enter: msecs=" +
                        msecs + " holder_=" +
                        ORBUtility.getThreadName(holder_) +
                        " counter_=" + counter_ ) ;
                Thread thr = Thread.currentThread() ;
                if (counter_==0) {
                    holder_ = thr;
                    counter_ = 1 ;
                    return true;
                } else if (msecs <= 0) {
                    return false;
                } else {
                    long waitTime = msecs;
                    long start = System.currentTimeMillis();
                    try {
                        for (;;) {
                            wait(waitTime);
                            if (counter_==0) {
                                holder_ = thr;
                                counter_ = 1 ;
                                return true;
                            } else {
                                waitTime = msecs -
                                    (System.currentTimeMillis() - start);
                                if (waitTime <= 0)
                                    return false;
                            }
                        }
                    } catch (InterruptedException ex) {
                        notify();
                        throw ex;
                    }
                }
            } finally {
                if (debug)
                    ORBUtility.dprintTrace( this, "attempt exit: " +
                        " holder_=" + ORBUtility.getThreadName(holder_) +
                        " counter_=" + counter_ ) ;
            }
        }
    }
}
