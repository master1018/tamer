abstract class XDragSourceProtocol {
    private final XDragSourceProtocolListener listener;
    private boolean initialized = false;
    private long targetWindow = 0;
    private long targetProxyWindow = 0;
    private int targetProtocolVersion = 0;
    private long targetWindowMask = 0;
    static long getDragSourceWindow() {
        return XWindow.getXAWTRootWindow().getWindow();
    }
    protected XDragSourceProtocol(XDragSourceProtocolListener listener) {
        if (listener == null) {
            throw new NullPointerException("Null XDragSourceProtocolListener");
        }
        this.listener = listener;
    }
    protected final XDragSourceProtocolListener getProtocolListener() {
        return listener;
    }
    public abstract String getProtocolName();
    public final void initializeDrag(int actions, Transferable contents,
                                     Map formatMap, long[] formats)
      throws InvalidDnDOperationException,
             IllegalArgumentException, XException {
        XToolkit.awtLock();
        try {
            try {
                if (initialized) {
                    throw new InvalidDnDOperationException("Already initialized");
                }
                initializeDragImpl(actions, contents, formatMap, formats);
                initialized = true;
            } finally {
                if (!initialized) {
                    cleanup();
                }
            }
        } finally {
            XToolkit.awtUnlock();
        }
    }
    protected abstract void initializeDragImpl(int actions,
                                               Transferable contents,
                                               Map formatMap, long[] formats)
      throws InvalidDnDOperationException, IllegalArgumentException, XException;
    public void cleanup() {
        initialized = false;
        cleanupTargetInfo();
    }
    public void cleanupTargetInfo() {
        targetWindow = 0;
        targetProxyWindow = 0;
        targetProtocolVersion = 0;
    }
    public abstract boolean processClientMessage(XClientMessageEvent xclient)
      throws XException;
    public final boolean attachTargetWindow(long window, long time) {
        assert XToolkit.isAWTLockHeldByCurrentThread();
        TargetWindowInfo info = getTargetWindowInfo(window);
        if (info == null) {
            return false;
        } else {
            targetWindow = window;
            targetProxyWindow = info.getProxyWindow();
            targetProtocolVersion = info.getProtocolVersion();
            return true;
        }
    }
    public abstract TargetWindowInfo getTargetWindowInfo(long window);
    public abstract void sendEnterMessage(long[] formats, int sourceAction,
                                          int sourceActions, long time);
    public abstract void sendMoveMessage(int xRoot, int yRoot,
                                         int sourceAction, int sourceActions,
                                         long time);
    public abstract void sendLeaveMessage(long time);
    protected abstract void sendDropMessage(int xRoot, int yRoot,
                                            int sourceAction, int sourceActions,
                                            long time);
    public final void initiateDrop(int xRoot, int yRoot,
                                   int sourceAction, int sourceActions,
                                   long time) {
        XWindowAttributes wattr = new XWindowAttributes();
        try {
            XToolkit.WITH_XERROR_HANDLER(XErrorHandler.IgnoreBadWindowHandler.getInstance());
            int status = XlibWrapper.XGetWindowAttributes(XToolkit.getDisplay(),
                                                          targetWindow, wattr.pData);
            XToolkit.RESTORE_XERROR_HANDLER();
            if (status == 0 ||
                (XToolkit.saved_error != null &&
                 XToolkit.saved_error.get_error_code() != XConstants.Success)) {
                throw new XException("XGetWindowAttributes failed");
            }
            targetWindowMask = wattr.get_your_event_mask();
        } finally {
            wattr.dispose();
        }
        XToolkit.WITH_XERROR_HANDLER(XErrorHandler.IgnoreBadWindowHandler.getInstance());
        XlibWrapper.XSelectInput(XToolkit.getDisplay(), targetWindow,
                                 targetWindowMask |
                                 XConstants.StructureNotifyMask);
        XToolkit.RESTORE_XERROR_HANDLER();
        if (XToolkit.saved_error != null &&
            XToolkit.saved_error.get_error_code() != XConstants.Success) {
            throw new XException("XSelectInput failed");
        }
        sendDropMessage(xRoot, yRoot, sourceAction, sourceActions, time);
    }
    protected final void finalizeDrop() {
        XToolkit.WITH_XERROR_HANDLER(XErrorHandler.IgnoreBadWindowHandler.getInstance());
        XlibWrapper.XSelectInput(XToolkit.getDisplay(), targetWindow,
                                 targetWindowMask);
        XToolkit.RESTORE_XERROR_HANDLER();
    }
    public abstract boolean processProxyModeEvent(XClientMessageEvent xclient,
                                                  long sourceWindow);
    protected final long getTargetWindow() {
        return targetWindow;
    }
    protected final long getTargetProxyWindow() {
        if (targetProxyWindow != 0) {
            return targetProxyWindow;
        } else {
            return targetWindow;
        }
    }
    protected final int getTargetProtocolVersion() {
        return targetProtocolVersion;
    }
    public static class TargetWindowInfo {
        private final long proxyWindow;
        private final int protocolVersion;
        public TargetWindowInfo(long proxy, int version) {
            proxyWindow = proxy;
            protocolVersion = version;
        }
        public long getProxyWindow() {
            return proxyWindow;
        }
        public int getProtocolVersion() {
            return protocolVersion;
        }
    }
}
