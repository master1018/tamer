public class CondVar {
    protected boolean debug_ ;
    protected final Sync mutex_;
    protected final ReentrantMutex remutex_;
    private int releaseMutex()
    {
        int count = 1 ;
        if (remutex_!=null)
            count = remutex_.releaseAll() ;
        else
            mutex_.release() ;
        return count ;
    }
    private void acquireMutex( int count ) throws InterruptedException
    {
        if (remutex_!=null)
            remutex_.acquireAll( count ) ;
        else
            mutex_.acquire() ;
    }
  public CondVar(Sync mutex, boolean debug) {
    debug_ = debug ;
    mutex_ = mutex;
    if (mutex instanceof ReentrantMutex)
        remutex_ = (ReentrantMutex)mutex;
    else
        remutex_ = null;
  }
  public CondVar( Sync mutex ) {
      this( mutex, false ) ;
  }
    public void await() throws InterruptedException {
        int count = 0 ;
        if (Thread.interrupted())
            throw new InterruptedException();
        try {
            if (debug_)
                ORBUtility.dprintTrace( this, "await enter" ) ;
            synchronized(this) {
                count = releaseMutex() ;
                try {
                    wait();
                } catch (InterruptedException ex) {
                    notify();
                    throw ex;
                }
            }
        } finally {
            boolean interrupted = false;
            for (;;) {
                try {
                    acquireMutex( count );
                    break;
                } catch (InterruptedException ex) {
                    interrupted = true;
                }
            }
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
            if (debug_)
                ORBUtility.dprintTrace( this, "await exit" ) ;
        }
    }
    public boolean timedwait(long msecs) throws  InterruptedException {
        if (Thread.interrupted())
            throw new InterruptedException();
        boolean success = false;
        int count = 0;
        try {
            if (debug_)
                ORBUtility.dprintTrace( this, "timedwait enter" ) ;
            synchronized(this) {
                count = releaseMutex() ;
                try {
                    if (msecs > 0) {
                        long start = System.currentTimeMillis();
                        wait(msecs);
                        success = System.currentTimeMillis() - start <= msecs;
                    }
                } catch (InterruptedException ex) {
                    notify();
                    throw ex;
                }
            }
        } finally {
            boolean interrupted = false;
            for (;;) {
                try {
                    acquireMutex( count ) ;
                    break;
                } catch (InterruptedException ex) {
                    interrupted = true;
                }
            }
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
            if (debug_)
                ORBUtility.dprintTrace( this, "timedwait exit" ) ;
        }
        return success;
    }
    public synchronized void signal() {
        notify();
    }
    public synchronized void broadcast() {
        notifyAll();
    }
}
