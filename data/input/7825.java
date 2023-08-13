public class XKeyboardFocusManagerPeer extends KeyboardFocusManagerPeerImpl {
    private static final PlatformLogger focusLog = PlatformLogger.getLogger("sun.awt.X11.focus.XKeyboardFocusManagerPeer");
    private static Object lock = new Object() {};
    private static Component currentFocusOwner;
    private static Window currentFocusedWindow;
    XKeyboardFocusManagerPeer(KeyboardFocusManager manager) {
        super(manager);
    }
    @Override
    public void setCurrentFocusOwner(Component comp) {
        setCurrentNativeFocusOwner(comp);
    }
    @Override
    public Component getCurrentFocusOwner() {
        return getCurrentNativeFocusOwner();
    }
    @Override
    public Window getCurrentFocusedWindow() {
        return getCurrentNativeFocusedWindow();
    }
    public static void setCurrentNativeFocusOwner(Component comp) {
        synchronized (lock) {
            currentFocusOwner = comp;
        }
    }
    public static Component getCurrentNativeFocusOwner() {
        synchronized(lock) {
            return currentFocusOwner;
        }
    }
    public static void setCurrentNativeFocusedWindow(Window win) {
        if (focusLog.isLoggable(PlatformLogger.FINER)) focusLog.finer("Setting current native focused window " + win);
        XWindowPeer from = null, to = null;
        synchronized(lock) {
            if (currentFocusedWindow != null) {
                from = (XWindowPeer)currentFocusedWindow.getPeer();
            }
            currentFocusedWindow = win;
            if (currentFocusedWindow != null) {
                to = (XWindowPeer)currentFocusedWindow.getPeer();
            }
        }
        if (from != null) {
            from.updateSecurityWarningVisibility();
        }
        if (to != null) {
            to.updateSecurityWarningVisibility();
        }
    }
    public static Window getCurrentNativeFocusedWindow() {
        synchronized(lock) {
            return currentFocusedWindow;
        }
    }
    public static boolean deliverFocus(Component lightweightChild,
                                       Component target,
                                       boolean temporary,
                                       boolean focusedWindowChangeAllowed,
                                       long time,
                                       CausedFocusEvent.Cause cause)
    {
        return KeyboardFocusManagerPeerImpl.deliverFocus(lightweightChild,
                                                         target,
                                                         temporary,
                                                         focusedWindowChangeAllowed,
                                                         time,
                                                         cause,
                                                         getCurrentNativeFocusOwner());
    }
}
