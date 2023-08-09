public class AccelDeviceEventNotifier {
    private static AccelDeviceEventNotifier theInstance;
    public static final int DEVICE_RESET = 0;
    public static final int DEVICE_DISPOSED = 1;
    private final Map<AccelDeviceEventListener, Integer> listeners;
    private AccelDeviceEventNotifier() {
        listeners = Collections.synchronizedMap(
            new HashMap<AccelDeviceEventListener, Integer>(1));
    }
    private static synchronized
        AccelDeviceEventNotifier getInstance(boolean create)
    {
        if (theInstance == null && create) {
            theInstance = new AccelDeviceEventNotifier();
        }
        return theInstance;
    }
    public static final void eventOccured(int screen, int eventType) {
        AccelDeviceEventNotifier notifier = getInstance(false);
        if (notifier != null) {
            notifier.notifyListeners(eventType, screen);
        }
    }
    public static final void addListener(AccelDeviceEventListener l,int screen){
        getInstance(true).add(l, screen);
    }
    public static final void removeListener(AccelDeviceEventListener l) {
        getInstance(true).remove(l);
    }
    private final void add(AccelDeviceEventListener theListener, int screen) {
        listeners.put(theListener, screen);
    }
    private final void remove(AccelDeviceEventListener theListener) {
        listeners.remove(theListener);
    }
    private final void notifyListeners(int deviceEventType, int screen) {
        HashMap<AccelDeviceEventListener, Integer> listClone;
        Set<AccelDeviceEventListener> cloneSet;
        synchronized(listeners) {
            listClone =
                new HashMap<AccelDeviceEventListener, Integer>(listeners);
        }
        cloneSet = listClone.keySet();
        Iterator<AccelDeviceEventListener> itr = cloneSet.iterator();
        while (itr.hasNext()) {
            AccelDeviceEventListener current = itr.next();
            Integer i = listClone.get(current);
            if (i != null && i.intValue() != screen) {
                continue;
            }
            if (deviceEventType == DEVICE_RESET) {
                current.onDeviceReset();
            } else if (deviceEventType == DEVICE_DISPOSED) {
                current.onDeviceDispose();
            }
        }
    }
}
