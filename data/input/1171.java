public class EventQueueImpl extends MirrorImpl implements EventQueue {
    LinkedList<EventSet> eventSets = new LinkedList<EventSet>();
    TargetVM target;
    boolean closed = false;
    EventQueueImpl(VirtualMachine vm, TargetVM target) {
        super(vm);
        this.target = target;
        target.addEventQueue(this);
    }
    public boolean equals(Object obj) {
        return this == obj;
    }
    public int hashCode() {
        return System.identityHashCode(this);
    }
    synchronized void enqueue(EventSet eventSet) {
        eventSets.add(eventSet);
        notifyAll();
    }
    synchronized int size() {
        return eventSets.size();
    }
    synchronized void close() {
        if (!closed) {
            closed = true; 
            enqueue(new EventSetImpl(vm,
                                     (byte)JDWP.EventKind.VM_DISCONNECTED));
        }
    }
    public EventSet remove() throws InterruptedException {
        return remove(0);
    }
    public EventSet remove(long timeout) throws InterruptedException {
        if (timeout < 0) {
            throw new IllegalArgumentException("Timeout cannot be negative");
        }
        EventSet eventSet;
        while (true) {
            EventSetImpl fullEventSet = removeUnfiltered(timeout);
            if (fullEventSet == null) {
                eventSet = null;  
                break;
            }
            eventSet = fullEventSet.userFilter();
            if (!eventSet.isEmpty()) {
                break;
            }
        }
        if ((eventSet != null) && (eventSet.suspendPolicy() == JDWP.SuspendPolicy.ALL)) {
            vm.notifySuspend();
        }
        return eventSet;
    }
    EventSet removeInternal() throws InterruptedException {
        EventSet eventSet;
        do {
            eventSet = removeUnfiltered(0).internalFilter();
        } while (eventSet == null || eventSet.isEmpty());
        return eventSet;
    }
    private TimerThread startTimerThread(long timeout) {
        TimerThread thread = new TimerThread(timeout);
        thread.setDaemon(true);
        thread.start();
        return thread;
    }
    private boolean shouldWait(TimerThread timerThread) {
        return !closed && eventSets.isEmpty() &&
               ((timerThread == null) ? true : !timerThread.timedOut());
    }
    private EventSetImpl removeUnfiltered(long timeout)
                                               throws InterruptedException {
        EventSetImpl eventSet = null;
        vm.waitInitCompletion();
        synchronized(this) {
            if (!eventSets.isEmpty()) {
                eventSet = (EventSetImpl)eventSets.removeFirst();
            } else {
                TimerThread timerThread = null;
                try {
                    if (timeout > 0) {
                        timerThread = startTimerThread(timeout);
                    }
                    while (shouldWait(timerThread)) {
                        this.wait();
                    }
                } finally {
                    if ((timerThread != null) && !timerThread.timedOut()) {
                        timerThread.interrupt();
                    }
                }
                if (eventSets.isEmpty()) {
                    if (closed) {
                        throw new VMDisconnectedException();
                    }
                } else {
                    eventSet = (EventSetImpl)eventSets.removeFirst();
                }
            }
        }
        if (eventSet != null) {
            target.notifyDequeueEventSet();
            eventSet.build();
        }
        return eventSet;
    }
    private class TimerThread extends Thread {
        private boolean timedOut = false;
        private long timeout;
        TimerThread(long timeout) {
            super(vm.threadGroupForJDI(), "JDI Event Queue Timer");
            this.timeout = timeout;
        }
        boolean timedOut() {
            return timedOut;
        }
        public void run() {
            try {
                Thread.sleep(timeout);
                EventQueueImpl queue = EventQueueImpl.this;
                synchronized(queue) {
                    timedOut = true;
                    queue.notifyAll();
                }
            } catch (InterruptedException e) {
            }
        }
    }
}
