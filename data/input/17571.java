class WaitDispatchSupport implements SecondaryLoop {
    private final static PlatformLogger log =
        PlatformLogger.getLogger("java.awt.event.WaitDispatchSupport");
    private EventDispatchThread dispatchThread;
    private EventFilter filter;
    private volatile Conditional extCondition;
    private volatile Conditional condition;
    private long interval;
    private static Timer timer;
    private TimerTask timerTask;
    private AtomicBoolean keepBlockingEDT = new AtomicBoolean(false);
    private AtomicBoolean keepBlockingCT = new AtomicBoolean(false);
    private static synchronized void initializeTimer() {
        if (timer == null) {
            timer = new Timer("AWT-WaitDispatchSupport-Timer", true);
        }
    }
    public WaitDispatchSupport(EventDispatchThread dispatchThread) {
        this(dispatchThread, null);
    }
    public WaitDispatchSupport(EventDispatchThread dispatchThread,
                               Conditional extCond)
    {
        if (dispatchThread == null) {
            throw new IllegalArgumentException("The dispatchThread can not be null");
        }
        this.dispatchThread = dispatchThread;
        this.extCondition = extCond;
        this.condition = new Conditional() {
            @Override
            public boolean evaluate() {
                if (log.isLoggable(PlatformLogger.FINEST)) {
                    log.finest("evaluate(): blockingEDT=" + keepBlockingEDT.get() +
                               ", blockingCT=" + keepBlockingCT.get());
                }
                boolean extEvaluate =
                    (extCondition != null) ? extCondition.evaluate() : true;
                if (!keepBlockingEDT.get() || !extEvaluate) {
                    if (timerTask != null) {
                        timerTask.cancel();
                        timerTask = null;
                    }
                    return false;
                }
                return true;
            }
        };
    }
    public WaitDispatchSupport(EventDispatchThread dispatchThread,
                               Conditional extCondition,
                               EventFilter filter, long interval)
    {
        this(dispatchThread, extCondition);
        this.filter = filter;
        if (interval < 0) {
            throw new IllegalArgumentException("The interval value must be >= 0");
        }
        this.interval = interval;
        if (interval != 0) {
            initializeTimer();
        }
    }
    @Override
    public boolean enter() {
        log.fine("enter(): blockingEDT=" + keepBlockingEDT.get() +
                 ", blockingCT=" + keepBlockingCT.get());
        if (!keepBlockingEDT.compareAndSet(false, true)) {
            log.fine("The secondary loop is already running, aborting");
            return false;
        }
        final Runnable run = new Runnable() {
            public void run() {
                log.fine("Starting a new event pump");
                if (filter == null) {
                    dispatchThread.pumpEvents(condition);
                } else {
                    dispatchThread.pumpEventsForFilter(condition, filter);
                }
            }
        };
        Thread currentThread = Thread.currentThread();
        if (currentThread == dispatchThread) {
            log.finest("On dispatch thread: " + dispatchThread);
            if (interval != 0) {
                log.finest("scheduling the timer for " + interval + " ms");
                timer.schedule(timerTask = new TimerTask() {
                    @Override
                    public void run() {
                        if (keepBlockingEDT.compareAndSet(true, false)) {
                            wakeupEDT();
                        }
                    }
                }, interval);
            }
            SequencedEvent currentSE = KeyboardFocusManager.
                getCurrentKeyboardFocusManager().getCurrentSequencedEvent();
            if (currentSE != null) {
                log.fine("Dispose current SequencedEvent: " + currentSE);
                currentSE.dispose();
            }
            AccessController.doPrivileged(new PrivilegedAction() {
                public Object run() {
                    run.run();
                    return null;
                }
            });
        } else {
            log.finest("On non-dispatch thread: " + currentThread);
            synchronized (getTreeLock()) {
                if (filter != null) {
                    dispatchThread.addEventFilter(filter);
                }
                try {
                    EventQueue eq = dispatchThread.getEventQueue();
                    eq.postEvent(new PeerEvent(this, run, PeerEvent.PRIORITY_EVENT));
                    keepBlockingCT.set(true);
                    if (interval > 0) {
                        long currTime = System.currentTimeMillis();
                        while (keepBlockingCT.get() &&
                               ((extCondition != null) ? extCondition.evaluate() : true) &&
                               (currTime + interval > System.currentTimeMillis()))
                        {
                            getTreeLock().wait(interval);
                        }
                    } else {
                        while (keepBlockingCT.get() &&
                               ((extCondition != null) ? extCondition.evaluate() : true))
                        {
                            getTreeLock().wait();
                        }
                    }
                    log.fine("waitDone " + keepBlockingEDT.get() + " " + keepBlockingCT.get());
                } catch (InterruptedException e) {
                    log.fine("Exception caught while waiting: " + e);
                } finally {
                    if (filter != null) {
                        dispatchThread.removeEventFilter(filter);
                    }
                }
                keepBlockingEDT.set(false);
                keepBlockingCT.set(false);
            }
        }
        return true;
    }
    public boolean exit() {
        log.fine("exit(): blockingEDT=" + keepBlockingEDT.get() +
                 ", blockingCT=" + keepBlockingCT.get());
        if (keepBlockingEDT.compareAndSet(true, false)) {
            wakeupEDT();
            return true;
        }
        return false;
    }
    private final static Object getTreeLock() {
        return Component.LOCK;
    }
    private final Runnable wakingRunnable = new Runnable() {
        public void run() {
            log.fine("Wake up EDT");
            synchronized (getTreeLock()) {
                keepBlockingCT.set(false);
                getTreeLock().notifyAll();
            }
            log.fine("Wake up EDT done");
        }
    };
    private void wakeupEDT() {
        log.finest("wakeupEDT(): EDT == " + dispatchThread);
        EventQueue eq = dispatchThread.getEventQueue();
        eq.postEvent(new PeerEvent(this, wakingRunnable, PeerEvent.PRIORITY_EVENT));
    }
}
