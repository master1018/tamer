class WKeyboardFocusManagerPeer extends KeyboardFocusManagerPeerImpl {
    static native void setNativeFocusOwner(ComponentPeer peer);
    static native Component getNativeFocusOwner();
    static native Window getNativeFocusedWindow();
    WKeyboardFocusManagerPeer(KeyboardFocusManager manager) {
        super(manager);
    }
    @Override
    public void setCurrentFocusOwner(Component comp) {
        setNativeFocusOwner(comp != null ? comp.getPeer() : null);
    }
    @Override
    public Component getCurrentFocusOwner() {
        return getNativeFocusOwner();
    }
    @Override
    public Window getCurrentFocusedWindow() {
        return getNativeFocusedWindow();
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
                                                         getNativeFocusOwner());
    }
}
