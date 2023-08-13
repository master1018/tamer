public class XInputMethod extends X11InputMethod {
    private static final PlatformLogger log = PlatformLogger.getLogger("sun.awt.X11.XInputMethod");
    public XInputMethod() throws AWTException {
        super();
    }
    public void setInputMethodContext(InputMethodContext context) {
        context.enableClientWindowNotification(this, true);
    }
    public void notifyClientWindowChange(Rectangle location) {
        XComponentPeer peer = (XComponentPeer)getPeer(clientComponentWindow);
        if (peer != null) {
            adjustStatusWindow(peer.getContentWindow());
        }
    }
    protected boolean openXIM() {
        return openXIMNative(XToolkit.getDisplay());
    }
    protected boolean createXIC() {
        XComponentPeer peer = (XComponentPeer)getPeer(clientComponentWindow);
        if (peer == null) {
            return false;
        }
        return createXICNative(peer.getContentWindow());
    }
    private static volatile long xicFocus = 0;
    protected void setXICFocus(ComponentPeer peer,
                                    boolean value, boolean active) {
        if (peer == null) {
            return;
        }
        xicFocus = ((XComponentPeer)peer).getContentWindow();
        setXICFocusNative(((XComponentPeer)peer).getContentWindow(),
                          value,
                          active);
    }
    public static long getXICFocus() {
        return xicFocus;
    }
    protected Container getParent(Component client) {
        return client.getParent();
    }
    protected ComponentPeer getPeer(Component client) {
        XComponentPeer peer;
        if (log.isLoggable(PlatformLogger.FINE)) log.fine("Client is " + client);
        peer = (XComponentPeer)XToolkit.targetToPeer(client);
        while (client != null && peer == null) {
            client = getParent(client);
            peer = (XComponentPeer)XToolkit.targetToPeer(client);
        }
        log.fine("Peer is {0}, client is {1}", peer, client);
        if (peer != null)
            return peer;
        return null;
    }
    protected synchronized void disposeImpl() {
        super.disposeImpl();
        clientComponentWindow = null;
    }
    protected void awtLock() {
        XToolkit.awtLock();
    }
    protected void awtUnlock() {
        XToolkit.awtUnlock();
    }
    long getCurrentParentWindow() {
        return (long)((XWindow)clientComponentWindow.getPeer()).getContentWindow();
    }
    private native boolean openXIMNative(long display);
    private native boolean createXICNative(long window);
    private native void setXICFocusNative(long window,
                                    boolean value, boolean active);
    private native void adjustStatusWindow(long window);
}
