final class XDropTargetContextPeer extends SunDropTargetContextPeer {
    private static final PlatformLogger logger =
        PlatformLogger.getLogger("sun.awt.X11.xembed.xdnd.XDropTargetContextPeer");
    private static final Unsafe unsafe = XlibWrapper.unsafe;
    private static final Object DTCP_KEY = "DropTargetContextPeer";
    private XDropTargetContextPeer() {}
    static XDropTargetContextPeer getPeer(AppContext appContext) {
        synchronized (_globalLock) {
            XDropTargetContextPeer peer =
                (XDropTargetContextPeer)appContext.get(DTCP_KEY);
            if (peer == null) {
                peer = new XDropTargetContextPeer();
                appContext.put(DTCP_KEY, peer);
            }
            return peer;
        }
    }
    static XDropTargetProtocolListener getXDropTargetProtocolListener() {
        return XDropTargetProtocolListenerImpl.getInstance();
    }
    protected void eventProcessed(SunDropTargetEvent e, int returnValue,
                                  boolean dispatcherDone) {
        long ctxt = getNativeDragContext();
        try {
            if (ctxt != 0 && !e.isConsumed()) {
                Iterator dropTargetProtocols =
                    XDragAndDropProtocols.getDropTargetProtocols();
                while (dropTargetProtocols.hasNext()) {
                    XDropTargetProtocol dropTargetProtocol =
                        (XDropTargetProtocol)dropTargetProtocols.next();
                    if (dropTargetProtocol.sendResponse(ctxt, e.getID(),
                                                        returnValue)) {
                        break;
                    }
                }
            }
        } finally {
            if (dispatcherDone && ctxt != 0) {
                unsafe.freeMemory(ctxt);
            }
        }
    }
    protected void doDropDone(boolean success, int dropAction,
                              boolean isLocal) {
        long ctxt = getNativeDragContext();
        if (ctxt != 0) {
            try {
                Iterator dropTargetProtocols =
                    XDragAndDropProtocols.getDropTargetProtocols();
                while (dropTargetProtocols.hasNext()) {
                    XDropTargetProtocol dropTargetProtocol =
                        (XDropTargetProtocol)dropTargetProtocols.next();
                    if (dropTargetProtocol.sendDropDone(ctxt, success,
                                                        dropAction)) {
                        break;
                    }
                }
            } finally {
                unsafe.freeMemory(ctxt);
            }
        }
    }
    protected Object getNativeData(long format)
      throws IOException {
        long ctxt = getNativeDragContext();
        if (ctxt != 0) {
            Iterator dropTargetProtocols =
                XDragAndDropProtocols.getDropTargetProtocols();
            while (dropTargetProtocols.hasNext()) {
                XDropTargetProtocol dropTargetProtocol =
                    (XDropTargetProtocol)dropTargetProtocols.next();
                try {
                    return dropTargetProtocol.getData(ctxt, format);
                } catch (IllegalArgumentException iae) {
                }
            }
        }
        return null;
    }
    private void cleanup() {
    }
    protected void processEnterMessage(SunDropTargetEvent event) {
        if (!processSunDropTargetEvent(event)) {
            super.processEnterMessage(event);
        }
    }
    protected void processExitMessage(SunDropTargetEvent event) {
        if (!processSunDropTargetEvent(event)) {
            super.processExitMessage(event);
        }
    }
    protected void processMotionMessage(SunDropTargetEvent event,
                                        boolean operationChanged) {
        if (!processSunDropTargetEvent(event)) {
            super.processMotionMessage(event, operationChanged);
        }
    }
    protected void processDropMessage(SunDropTargetEvent event) {
        if (!processSunDropTargetEvent(event)) {
            super.processDropMessage(event);
        }
    }
    private boolean processSunDropTargetEvent(SunDropTargetEvent event) {
        Object source = event.getSource();
        if (source instanceof Component) {
            ComponentPeer peer = ((Component)source).getPeer();
            if (peer instanceof XEmbedCanvasPeer) {
                XEmbedCanvasPeer xEmbedCanvasPeer = (XEmbedCanvasPeer)peer;
                long ctxt = getNativeDragContext();
                if (logger.isLoggable(PlatformLogger.FINER)) {
                    logger.finer("        processing " + event + " ctxt=" + ctxt +
                                 " consumed=" + event.isConsumed());
                }
                if (!event.isConsumed()) {
                    if (xEmbedCanvasPeer.processXEmbedDnDEvent(ctxt,
                                                               event.getID())) {
                        event.consume();
                        return true;
                    }
                }
            }
        }
        return false;
    }
    public void forwardEventToEmbedded(long embedded, long ctxt,
                                       int eventID) {
        Iterator dropTargetProtocols =
            XDragAndDropProtocols.getDropTargetProtocols();
        while (dropTargetProtocols.hasNext()) {
            XDropTargetProtocol dropTargetProtocol =
                (XDropTargetProtocol)dropTargetProtocols.next();
            if (dropTargetProtocol.forwardEventToEmbedded(embedded, ctxt,
                                                          eventID)) {
                break;
            }
        }
    }
    static final class XDropTargetProtocolListenerImpl
        implements XDropTargetProtocolListener {
        private final static XDropTargetProtocolListener theInstance =
            new XDropTargetProtocolListenerImpl();
        private XDropTargetProtocolListenerImpl() {}
        static XDropTargetProtocolListener getInstance() {
            return theInstance;
        }
        public void handleDropTargetNotification(XWindow xwindow, int x, int y,
                                                 int dropAction, int actions,
                                                 long[] formats, long nativeCtxt,
                                                 int eventID) {
            Object target = xwindow.getTarget();
            assert target instanceof Component;
            Component component = (Component)target;
            AppContext appContext = SunToolkit.targetToAppContext(target);
            assert appContext != null;
            XDropTargetContextPeer peer = XDropTargetContextPeer.getPeer(appContext);
            peer.postDropTargetEvent(component, x, y, dropAction, actions, formats,
                                     nativeCtxt, eventID,
                                     !SunDropTargetContextPeer.DISPATCH_SYNC);
        }
    }
}
