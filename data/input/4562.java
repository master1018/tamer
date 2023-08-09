class TimerQueue implements Runnable
{
    private static final Object sharedInstanceKey =
        new StringBuffer("TimerQueue.sharedInstanceKey");
    private static final Object expiredTimersKey =
        new StringBuffer("TimerQueue.expiredTimersKey");
    private final DelayQueue<DelayedTimer> queue;
    private volatile boolean running;
    private final Lock runningLock;
    private static final Object classLock = new Object();
    private static final long NANO_ORIGIN = System.nanoTime();
    public TimerQueue() {
        super();
        queue = new DelayQueue<DelayedTimer>();
        runningLock = new ReentrantLock();
        startIfNeeded();
    }
    public static TimerQueue sharedInstance() {
        synchronized (classLock) {
            TimerQueue sharedInst = (TimerQueue)
                                    SwingUtilities.appContextGet(
                                                        sharedInstanceKey);
            if (sharedInst == null) {
                sharedInst = new TimerQueue();
                SwingUtilities.appContextPut(sharedInstanceKey, sharedInst);
            }
            return sharedInst;
        }
    }
    void startIfNeeded() {
        if (! running) {
            runningLock.lock();
            try {
                final ThreadGroup threadGroup =
                    AppContext.getAppContext().getThreadGroup();
                java.security.AccessController.doPrivileged(
                    new java.security.PrivilegedAction<Object>() {
                    public Object run() {
                        Thread timerThread = new Thread(threadGroup, TimerQueue.this,
                                                        "TimerQueue");
                        timerThread.setDaemon(true);
                        timerThread.setPriority(Thread.NORM_PRIORITY);
                        timerThread.start();
                        return null;
                    }
                });
                running = true;
            } finally {
                runningLock.unlock();
            }
        }
    }
    void addTimer(Timer timer, long delayMillis) {
        timer.getLock().lock();
        try {
            if (! containsTimer(timer)) {
                addTimer(new DelayedTimer(timer,
                                      TimeUnit.MILLISECONDS.toNanos(delayMillis)
                                      + now()));
            }
        } finally {
            timer.getLock().unlock();
        }
    }
    private void addTimer(DelayedTimer delayedTimer) {
        assert delayedTimer != null && ! containsTimer(delayedTimer.getTimer());
        Timer timer = delayedTimer.getTimer();
        timer.getLock().lock();
        try {
            timer.delayedTimer = delayedTimer;
            queue.add(delayedTimer);
        } finally {
            timer.getLock().unlock();
        }
    }
    void removeTimer(Timer timer) {
        timer.getLock().lock();
        try {
            if (timer.delayedTimer != null) {
                queue.remove(timer.delayedTimer);
                timer.delayedTimer = null;
            }
        } finally {
            timer.getLock().unlock();
        }
    }
    boolean containsTimer(Timer timer) {
        timer.getLock().lock();
        try {
            return timer.delayedTimer != null;
        } finally {
            timer.getLock().unlock();
        }
    }
    public void run() {
        runningLock.lock();
        try {
            while (running) {
                try {
                    Timer timer = queue.take().getTimer();
                    timer.getLock().lock();
                    try {
                        DelayedTimer delayedTimer = timer.delayedTimer;
                        if (delayedTimer != null) {
                            timer.post(); 
                            timer.delayedTimer = null;
                            if (timer.isRepeats()) {
                                delayedTimer.setTime(now()
                                    + TimeUnit.MILLISECONDS.toNanos(
                                          timer.getDelay()));
                                addTimer(delayedTimer);
                            }
                        }
                    } catch (SecurityException ignore) {
                    } finally {
                        timer.getLock().unlock();
                    }
                } catch (InterruptedException ie) {
                    if (AppContext.getAppContext().isDisposed()) {
                        break;
                    }
                }
            }
        }
        catch (ThreadDeath td) {
            for (DelayedTimer delayedTimer : queue) {
                delayedTimer.getTimer().cancelEvent();
            }
            throw td;
        } finally {
            running = false;
            runningLock.unlock();
        }
    }
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("TimerQueue (");
        boolean isFirst = true;
        for (DelayedTimer delayedTimer : queue) {
            if (! isFirst) {
                buf.append(", ");
            }
            buf.append(delayedTimer.getTimer().toString());
            isFirst = false;
        }
        buf.append(")");
        return buf.toString();
    }
    private static long now() {
        return System.nanoTime() - NANO_ORIGIN;
    }
    static class DelayedTimer implements Delayed {
        private static final AtomicLong sequencer = new AtomicLong(0);
        private final long sequenceNumber;
        private volatile long time;
        private final Timer timer;
        DelayedTimer(Timer timer, long nanos) {
            this.timer = timer;
            time = nanos;
            sequenceNumber = sequencer.getAndIncrement();
        }
        final public long getDelay(TimeUnit unit) {
            return  unit.convert(time - now(), TimeUnit.NANOSECONDS);
        }
        final void setTime(long nanos) {
            time = nanos;
        }
        final Timer getTimer() {
            return timer;
        }
        public int compareTo(Delayed other) {
            if (other == this) { 
                return 0;
            }
            if (other instanceof DelayedTimer) {
                DelayedTimer x = (DelayedTimer)other;
                long diff = time - x.time;
                if (diff < 0) {
                    return -1;
                } else if (diff > 0) {
                    return 1;
                } else if (sequenceNumber < x.sequenceNumber) {
                    return -1;
                }  else {
                    return 1;
                }
            }
            long d = (getDelay(TimeUnit.NANOSECONDS) -
                      other.getDelay(TimeUnit.NANOSECONDS));
            return (d == 0) ? 0 : ((d < 0) ? -1 : 1);
        }
    }
}
