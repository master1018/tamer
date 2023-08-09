abstract class XDropTargetProtocol {
    private static final PlatformLogger logger =
        PlatformLogger.getLogger("sun.awt.X11.xembed.xdnd.XDropTargetProtocol");
    private final XDropTargetProtocolListener listener;
    public static final int EMBEDDER_ALREADY_REGISTERED = 0;
    public static final int UNKNOWN_MESSAGE = 0;
    public static final int ENTER_MESSAGE   = 1;
    public static final int MOTION_MESSAGE  = 2;
    public static final int LEAVE_MESSAGE   = 3;
    public static final int DROP_MESSAGE    = 4;
    protected XDropTargetProtocol(XDropTargetProtocolListener listener) {
        if (listener == null) {
            throw new NullPointerException("Null XDropTargetProtocolListener");
        }
        this.listener = listener;
    }
    protected final XDropTargetProtocolListener getProtocolListener() {
        return listener;
    }
    public abstract String getProtocolName();
    public abstract void registerDropTarget(long window);
    public abstract void unregisterDropTarget(long window);
    public abstract void registerEmbedderDropSite(long window);
    public abstract void unregisterEmbedderDropSite(long window);
    public abstract void registerEmbeddedDropSite(long embedded);
    public final void unregisterEmbeddedDropSite(long embedded) {
        removeEmbedderRegistryEntry(embedded);
    }
    public abstract boolean isProtocolSupported(long window);
    public abstract int getMessageType(XClientMessageEvent xclient);
    public final boolean processClientMessage(XClientMessageEvent xclient) {
        int type = getMessageType(xclient);
        boolean processed = processClientMessageImpl(xclient);
        postProcessClientMessage(xclient, processed, type);
        return processed;
    }
    protected abstract boolean processClientMessageImpl(XClientMessageEvent xclient);
    protected final boolean forwardClientMessageToToplevel(long toplevel,
                                                           XClientMessageEvent xclient) {
        EmbedderRegistryEntry entry = getEmbedderRegistryEntry(toplevel);
        if (logger.isLoggable(PlatformLogger.FINEST)) {
            logger.finest("        entry={0}", entry);
        }
        if (entry == null) {
            return false;
        }
        if (logger.isLoggable(PlatformLogger.FINEST)) {
            logger.finest("        entry.isOverriden()={0}", entry.isOverriden());
        }
        if (!entry.isOverriden()) {
            return false;
        }
        adjustEventForForwarding(xclient, entry);
        long proxy = entry.getProxy();
        if (logger.isLoggable(PlatformLogger.FINEST)) {
            logger.finest("        proxy={0} toplevel={1}", proxy, toplevel);
        }
        if (proxy == 0) {
            proxy = toplevel;
        }
        xclient.set_window(toplevel);
        XToolkit.awtLock();
        try {
            XlibWrapper.XSendEvent(XToolkit.getDisplay(), proxy, false,
                                   XConstants.NoEventMask, xclient.pData);
        } finally {
            XToolkit.awtUnlock();
        }
        return true;
    }
    private boolean motionPassedAlong = false;
    protected abstract void sendEnterMessageToToplevel(long toplevel,
                                                       XClientMessageEvent xclient);
    protected abstract void sendLeaveMessageToToplevel(long toplevel,
                                                       XClientMessageEvent xclient);
    private void postProcessClientMessage(XClientMessageEvent xclient,
                                          boolean processed,
                                          int type) {
        long toplevel = xclient.get_window();
        if (getEmbedderRegistryEntry(toplevel) != null) {
            if (!processed) {
                forwardClientMessageToToplevel(toplevel, xclient);
            } else {
                boolean motifProtocol =
                    xclient.get_message_type() ==
                    MotifDnDConstants.XA_MOTIF_DRAG_AND_DROP_MESSAGE.getAtom();
                switch (type) {
                case XDropTargetProtocol.MOTION_MESSAGE:
                    if (!isDragOverComponent()) {
                        if (!motionPassedAlong && !motifProtocol) {
                            sendEnterMessageToToplevel(toplevel, xclient);
                        }
                        forwardClientMessageToToplevel(toplevel, xclient);
                        motionPassedAlong = true;
                    } else {
                        if (motionPassedAlong && !motifProtocol) {
                            sendLeaveMessageToToplevel(toplevel, xclient);
                        }
                        motionPassedAlong = false;
                    }
                    break;
                case XDropTargetProtocol.DROP_MESSAGE:
                    if (!isDragOverComponent()) {
                        forwardClientMessageToToplevel(toplevel, xclient);
                    }
                    motionPassedAlong = false;
                    break;
                case XDropTargetProtocol.ENTER_MESSAGE:
                case XDropTargetProtocol.LEAVE_MESSAGE:
                    if (motifProtocol) {
                        forwardClientMessageToToplevel(toplevel, xclient);
                    }
                    motionPassedAlong = false;
                    break;
                }
            }
        }
    }
    public abstract boolean sendResponse(long ctxt, int eventID, int action);
    public abstract Object getData(long ctxt, long format)
      throws IllegalArgumentException, IOException;
    public abstract boolean sendDropDone(long ctxt, boolean success,
                                         int dropAction);
    public abstract long getSourceWindow();
    public abstract void cleanup();
    public abstract boolean isDragOverComponent();
    public void adjustEventForForwarding(XClientMessageEvent xclient,
        EmbedderRegistryEntry entry) {}
    public abstract boolean forwardEventToEmbedded(long embedded, long ctxt,
                                                   int eventID);
    public abstract boolean isXEmbedSupported();
    protected static final class EmbedderRegistryEntry {
        private final boolean overriden;
        private final int version;
        private final long proxy;
        EmbedderRegistryEntry(boolean overriden, int version, long proxy) {
            this.overriden = overriden;
            this.version = version;
            this.proxy = proxy;
        }
        public boolean isOverriden() {
            return overriden;
        }
        public int getVersion() {
            return version;
        }
        public long getProxy() {
            return proxy;
        }
    }
    private final HashMap embedderRegistry = new HashMap();
    protected final void putEmbedderRegistryEntry(long embedder,
                                                  boolean overriden,
                                                  int version,
                                                  long proxy) {
        synchronized (this) {
            embedderRegistry.put(Long.valueOf(embedder),
                                 new EmbedderRegistryEntry(overriden, version,
                                                           proxy));
        }
    }
    protected final EmbedderRegistryEntry getEmbedderRegistryEntry(long embedder) {
        synchronized (this) {
            return
                (EmbedderRegistryEntry)embedderRegistry.get(Long.valueOf(embedder));
        }
    }
    protected final void removeEmbedderRegistryEntry(long embedder) {
        synchronized (this) {
            embedderRegistry.remove(Long.valueOf(embedder));
        }
    }
}
