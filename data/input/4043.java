public abstract class KeyboardFocusManagerPeerImpl implements KeyboardFocusManagerPeer {
    private static final PlatformLogger focusLog = PlatformLogger.getLogger("sun.awt.focus.KeyboardFocusManagerPeerImpl");
    private static AWTAccessor.KeyboardFocusManagerAccessor kfmAccessor =
        AWTAccessor.getKeyboardFocusManagerAccessor();
    public static final int SNFH_FAILURE         = 0;
    public static final int SNFH_SUCCESS_HANDLED = 1;
    public static final int SNFH_SUCCESS_PROCEED = 2;
    protected KeyboardFocusManager manager;
    public KeyboardFocusManagerPeerImpl(KeyboardFocusManager manager) {
        this.manager = manager;
    }
    @Override
    public void clearGlobalFocusOwner(Window activeWindow) {
        if (activeWindow != null) {
            Component focusOwner = activeWindow.getFocusOwner();
            if (focusLog.isLoggable(PlatformLogger.FINE))
                focusLog.fine("Clearing global focus owner " + focusOwner);
            if (focusOwner != null) {
                FocusEvent fl = new CausedFocusEvent(focusOwner, FocusEvent.FOCUS_LOST, false, null,
                                                     CausedFocusEvent.Cause.CLEAR_GLOBAL_FOCUS_OWNER);
                SunToolkit.postPriorityEvent(fl);
            }
        }
    }
    public static boolean shouldFocusOnClick(Component component) {
        boolean acceptFocusOnClick = false;
        if (component instanceof Canvas ||
            component instanceof Scrollbar)
        {
            acceptFocusOnClick = true;
        } else if (component instanceof Panel) {
            acceptFocusOnClick = (((Panel)component).getComponentCount() == 0);
        } else {
            ComponentPeer peer = (component != null ? component.getPeer() : null);
            acceptFocusOnClick = (peer != null ? peer.isFocusable() : false);
        }
        return acceptFocusOnClick &&
               AWTAccessor.getComponentAccessor().canBeFocusOwner(component);
    }
    public static boolean deliverFocus(Component lightweightChild,
                                       Component target,
                                       boolean temporary,
                                       boolean focusedWindowChangeAllowed,
                                       long time,
                                       CausedFocusEvent.Cause cause,
                                       Component currentFocusOwner) 
    {
        if (lightweightChild == null) {
            lightweightChild = (Component)target;
        }
        Component currentOwner = currentFocusOwner;
        if (currentOwner != null && currentOwner.getPeer() == null) {
            currentOwner = null;
        }
        if (currentOwner != null) {
            FocusEvent fl = new CausedFocusEvent(currentOwner, FocusEvent.FOCUS_LOST,
                                                 false, lightweightChild, cause);
            if (focusLog.isLoggable(PlatformLogger.FINER))
                focusLog.finer("Posting focus event: " + fl);
            SunToolkit.postPriorityEvent(fl);
        }
        FocusEvent fg = new CausedFocusEvent(lightweightChild, FocusEvent.FOCUS_GAINED,
                                             false, currentOwner, cause);
        if (focusLog.isLoggable(PlatformLogger.FINER))
            focusLog.finer("Posting focus event: " + fg);
        SunToolkit.postPriorityEvent(fg);
        return true;
    }
    public static boolean requestFocusFor(Component target, CausedFocusEvent.Cause cause) {
        return AWTAccessor.getComponentAccessor().requestFocus(target, cause);
    }
    public static int shouldNativelyFocusHeavyweight(Component heavyweight,
                                                     Component descendant,
                                                     boolean temporary,
                                                     boolean focusedWindowChangeAllowed,
                                                     long time,
                                                     CausedFocusEvent.Cause cause)
    {
        return kfmAccessor.shouldNativelyFocusHeavyweight(
            heavyweight, descendant, temporary, focusedWindowChangeAllowed, time, cause);
    }
    public static void removeLastFocusRequest(Component heavyweight) {
        kfmAccessor.removeLastFocusRequest(heavyweight);
    }
    public static boolean processSynchronousLightweightTransfer(Component heavyweight,
                                                                Component descendant,
                                                                boolean temporary,
                                                                boolean focusedWindowChangeAllowed,
                                                                long time)
    {
        return kfmAccessor.processSynchronousLightweightTransfer(
            heavyweight, descendant, temporary, focusedWindowChangeAllowed, time);
    }
}
