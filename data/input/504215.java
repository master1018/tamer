public class AndroidNativeEventQueue extends NativeEventQueue {
    private Object eventMonitor;
    public AndroidNativeEventQueue() {
        super();
        eventMonitor = getEventMonitor();
    }
    @Override
    public void awake() {
        synchronized (eventMonitor) {
            eventMonitor.notify();
        }
    }
    @Override
    public void dispatchEvent() {
        System.out.println(getClass()+": empty method called");
    }
    @Override
    public long getJavaWindow() {
        System.out.println(getClass()+": empty method called");
        return 0;
    }
    @Override
    public void performLater(Task task) {
        System.out.println(getClass()+": empty method called");
    }
    @Override
    public void performTask(Task task) {
        System.out.println(getClass()+": empty method called");
    }
    @Override
    public boolean waitEvent() {
        while (isEmpty() ) {
            synchronized (eventMonitor) {
                try {
                    eventMonitor.wait(1000);
                } catch (InterruptedException ignore) {
                }
            }
        }
        return false;
    }
}
