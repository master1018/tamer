class VMThread
{
    Thread thread;
    int vmData;
    VMThread(Thread t) {
        thread = t;
    }
    native static void create(Thread t, long stacksize);
    static native Thread currentThread();
    static native boolean interrupted();
    static native void sleep (long msec, int nsec) throws InterruptedException;
    static native void yield();
    native void interrupt();
    native boolean isInterrupted();
    void start(long stacksize) {
        VMThread.create(thread, stacksize);
    }
    private static final String UNSUPPORTED_THREAD_METHOD
            = "Deprecated Thread methods are not supported.";
    @SuppressWarnings("ThrowableInstanceNeverThrown")
    void suspend() {
        Logger.global.log(Level.SEVERE, UNSUPPORTED_THREAD_METHOD,
                new UnsupportedOperationException());
    }
    @SuppressWarnings("ThrowableInstanceNeverThrown")
    void resume() {
        Logger.global.log(Level.SEVERE, UNSUPPORTED_THREAD_METHOD,
                new UnsupportedOperationException());
    }
    native boolean holdsLock(Object object);
    @SuppressWarnings("ThrowableInstanceNeverThrown")
    void stop(Throwable throwable) {
        Logger.global.log(Level.SEVERE, UNSUPPORTED_THREAD_METHOD,
                new UnsupportedOperationException());
    }
    native void setPriority(int newPriority);
    native int getStatus();
    static final Thread.State[] STATE_MAP = new Thread.State[] {
        Thread.State.TERMINATED,     
        Thread.State.RUNNABLE,       
        Thread.State.TIMED_WAITING,  
        Thread.State.BLOCKED,        
        Thread.State.WAITING,        
        Thread.State.NEW,            
        Thread.State.NEW,            
        Thread.State.RUNNABLE,       
        Thread.State.WAITING         
    };
    native void nameChanged(String newName);
}
