public class DebugMutex implements Sync  {
  protected boolean inuse_ = false;
  protected Thread holder_ = null;
  public void acquire() throws InterruptedException {
    if (Thread.interrupted()) throw new InterruptedException();
    synchronized(this) {
      Thread thr = Thread.currentThread();
      if (holder_ == thr)
        throw new INTERNAL(
            "Attempt to acquire Mutex by thread holding the Mutex" ) ;
      try {
        while (inuse_) wait();
        inuse_ = true;
        holder_ = Thread.currentThread();
      }
      catch (InterruptedException ex) {
        notify();
        throw ex;
      }
    }
  }
  public synchronized void release()  {
    Thread thr = Thread.currentThread();
    if (thr != holder_)
        throw new INTERNAL(
            "Attempt to release Mutex by thread not holding the Mutex" ) ;
    holder_ = null;
    inuse_ = false;
    notify();
  }
  public boolean attempt(long msecs) throws InterruptedException {
    if (Thread.interrupted()) throw new InterruptedException();
    synchronized(this) {
      Thread thr = Thread.currentThread() ;
      if (!inuse_) {
        inuse_ = true;
        holder_ = thr;
        return true;
      } else if (msecs <= 0)
        return false;
      else {
        long waitTime = msecs;
        long start = System.currentTimeMillis();
        try {
          for (;;) {
            wait(waitTime);
            if (!inuse_) {
              inuse_ = true;
              holder_ = thr;
              return true;
            }
            else {
              waitTime = msecs - (System.currentTimeMillis() - start);
              if (waitTime <= 0)
                return false;
            }
          }
        }
        catch (InterruptedException ex) {
          notify();
          throw ex;
        }
      }
    }
  }
}
