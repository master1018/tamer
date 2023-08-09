public abstract class NativeEventQueue {
    private ShutdownWatchdog shutdownWatchdog;
    private class EventMonitor {}
    private final Object eventMonitor = new EventMonitor();
    private final LinkedList<NativeEvent> eventQueue = new LinkedList<NativeEvent>();
    public static abstract class Task {
        public volatile Object returnValue;
        public abstract void perform();
    }
    public abstract boolean waitEvent();
    public boolean isEmpty() {
        synchronized(eventQueue) {
            return eventQueue.isEmpty();
        }
    }
    public NativeEvent getNextEvent() {
        synchronized (eventQueue) {
            if (eventQueue.isEmpty()) {
                shutdownWatchdog.setNativeQueueEmpty(true);
                return null;
            }
            return eventQueue.remove(0);
        }
    }
    protected void addEvent(NativeEvent event) {
        synchronized (eventQueue) {
            eventQueue.add(event);
            shutdownWatchdog.setNativeQueueEmpty(false);
        }
        synchronized (eventMonitor) {
            eventMonitor.notify();
        }
    }
    public final Object getEventMonitor() {
        return eventMonitor;
    }
    public abstract void awake();
    public abstract long getJavaWindow();
    public abstract void dispatchEvent();
    public abstract void performTask(Task task);
    public abstract void performLater(Task task);
    public final void setShutdownWatchdog(ShutdownWatchdog watchdog) {
        synchronized (eventQueue) {
            shutdownWatchdog = watchdog;
        }
    }
}
