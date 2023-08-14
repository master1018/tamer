public class Synchronizer {
    protected int acquestCounter;
    protected Thread owner;
    protected final LinkedList<Thread> waitQueue = new LinkedList<Thread>();
    protected Thread dispatchThread;
    private final Hashtable<Thread, Integer> storedStates = new Hashtable<Thread, Integer>();
    public void lock() {
        synchronized (this) {
            Thread curThread = Thread.currentThread();
            if (acquestCounter == 0) {
                acquestCounter = 1;
                owner = curThread;
            } else {
                if (owner == curThread) {
                    acquestCounter++;
                } else {
                    if (curThread == dispatchThread) {
                        waitQueue.addFirst(curThread);
                    } else {
                        waitQueue.addLast(curThread);
                    }
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        if (owner != curThread) {
                            waitQueue.remove(curThread);
                            throw new RuntimeException(Messages
                                    .getString("awt.1F")); 
                        }
                    }
                }
            }
        }
    }
    public void unlock() {
        synchronized (this) {
            if (acquestCounter == 0) {
                throw new RuntimeException(Messages.getString("awt.20")); 
            }
            if (owner != Thread.currentThread()) {
                throw new RuntimeException(Messages.getString("awt.21")); 
            }
            acquestCounter--;
            if (acquestCounter == 0) {
                if (waitQueue.size() > 0) {
                    acquestCounter = 1;
                    owner = waitQueue.removeFirst();
                    owner.interrupt();
                } else {
                    owner = null;
                }
            }
        }
    }
    public void storeStateAndFree() {
        synchronized (this) {
            Thread curThread = Thread.currentThread();
            if (owner != curThread) {
                throw new RuntimeException(Messages.getString("awt.22")); 
            }
            if (storedStates.containsKey(curThread)) {
                throw new RuntimeException(Messages.getString("awt.23")); 
            }
            storedStates.put(curThread, new Integer(acquestCounter));
            acquestCounter = 1;
            unlock();
        }
    }
    public void lockAndRestoreState() {
        synchronized (this) {
            Thread curThread = Thread.currentThread();
            if (owner == curThread) {
                throw new RuntimeException(
                        Messages.getString("awt.24")); 
            }
            if (!storedStates.containsKey(curThread)) {
                throw new RuntimeException(Messages.getString("awt.25")); 
            }
            lock();
            acquestCounter = storedStates.get(curThread).intValue();
            storedStates.remove(curThread);
        }
    }
    public void setEnvironment(WTK wtk, Thread dispatchThread) {
        synchronized (this) {
            this.dispatchThread = dispatchThread;
        }
    }
}
