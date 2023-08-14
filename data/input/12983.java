public class SunDisplayChanger {
    private static final PlatformLogger log = PlatformLogger.getLogger("sun.awt.multiscreen.SunDisplayChanger");
    private Map listeners = Collections.synchronizedMap(new WeakHashMap(1));
    public SunDisplayChanger() {}
    public void add(DisplayChangedListener theListener) {
        if (log.isLoggable(PlatformLogger.FINE)) {
            if (theListener == null) {
                log.fine("Assertion (theListener != null) failed");
            }
        }
        if (log.isLoggable(PlatformLogger.FINER)) {
            log.finer("Adding listener: " + theListener);
        }
        listeners.put(theListener, null);
    }
    public void remove(DisplayChangedListener theListener) {
        if (log.isLoggable(PlatformLogger.FINE)) {
            if (theListener == null) {
                log.fine("Assertion (theListener != null) failed");
            }
        }
        if (log.isLoggable(PlatformLogger.FINER)) {
            log.finer("Removing listener: " + theListener);
        }
        listeners.remove(theListener);
    }
    public void notifyListeners() {
        if (log.isLoggable(PlatformLogger.FINEST)) {
            log.finest("notifyListeners");
        }
        HashMap listClone;
        Set cloneSet;
        synchronized(listeners) {
            listClone = new HashMap(listeners);
        }
        cloneSet = listClone.keySet();
        Iterator itr = cloneSet.iterator();
        while (itr.hasNext()) {
            DisplayChangedListener current =
             (DisplayChangedListener) itr.next();
            try {
                if (log.isLoggable(PlatformLogger.FINEST)) {
                    log.finest("displayChanged for listener: " + current);
                }
                current.displayChanged();
            } catch (IllegalComponentStateException e) {
                listeners.remove(current);
            }
        }
    }
    public void notifyPaletteChanged() {
        if (log.isLoggable(PlatformLogger.FINEST)) {
            log.finest("notifyPaletteChanged");
        }
        HashMap listClone;
        Set cloneSet;
        synchronized (listeners) {
            listClone = new HashMap(listeners);
        }
        cloneSet = listClone.keySet();
        Iterator itr = cloneSet.iterator();
        while (itr.hasNext()) {
            DisplayChangedListener current =
             (DisplayChangedListener) itr.next();
            try {
                if (log.isLoggable(PlatformLogger.FINEST)) {
                    log.finest("paletteChanged for listener: " + current);
                }
                current.paletteChanged();
            } catch (IllegalComponentStateException e) {
                listeners.remove(current);
            }
        }
    }
}
