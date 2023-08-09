final class XDropTargetRegistry {
    private static final PlatformLogger logger =
        PlatformLogger.getLogger("sun.awt.X11.xembed.xdnd.XDropTargetRegistry");
    private static final long DELAYED_REGISTRATION_PERIOD = 200;
    private static final XDropTargetRegistry theInstance =
        new XDropTargetRegistry();
    private final HashMap<Long, Runnable> delayedRegistrationMap =
        new HashMap<Long, Runnable>();
    private XDropTargetRegistry() {}
    static XDropTargetRegistry getRegistry() {
        return theInstance;
    }
    private long getToplevelWindow(long window) {
        XBaseWindow candWindow = XToolkit.windowToXWindow(window);
        if (candWindow != null) {
            XWindowPeer toplevel = candWindow.getToplevelXWindow();
            if (toplevel != null && !(toplevel instanceof XEmbeddedFramePeer)) {
                return toplevel.getWindow();
            }
        }
        do {
            if (XlibUtil.isTrueToplevelWindow(window)) {
                return window;
            }
            window = XlibUtil.getParentWindow(window);
        } while (window != 0);
        return window;
    }
    static final long getDnDProxyWindow() {
        return XWindow.getXAWTRootWindow().getWindow();
    }
    private static final class EmbeddedDropSiteEntry {
        private final long root;
        private final long event_mask;
        private List<XDropTargetProtocol> supportedProtocols;
        private final HashSet<Long> nonXEmbedClientSites = new HashSet<Long>();
        private final List<Long> sites = new ArrayList<Long>();
        public EmbeddedDropSiteEntry(long root, long event_mask,
                                     List<XDropTargetProtocol> supportedProtocols) {
            if (supportedProtocols == null) {
                throw new NullPointerException("Null supportedProtocols");
            }
            this.root = root;
            this.event_mask = event_mask;
            this.supportedProtocols = supportedProtocols;
        }
        public long getRoot() {
            return root;
        }
        public long getEventMask() {
            return event_mask;
        }
        public boolean hasNonXEmbedClientSites() {
            return !nonXEmbedClientSites.isEmpty();
        }
        public synchronized void addSite(long window, boolean isXEmbedClient) {
            Long lWindow = Long.valueOf(window);
            if (!sites.contains(lWindow)) {
                sites.add(lWindow);
            }
            if (!isXEmbedClient) {
                nonXEmbedClientSites.add(lWindow);
            }
        }
        public synchronized void removeSite(long window) {
            Long lWindow = Long.valueOf(window);
            sites.remove(lWindow);
            nonXEmbedClientSites.remove(lWindow);
        }
        public void setSupportedProtocols(List<XDropTargetProtocol> list) {
            supportedProtocols = list;
        }
        public List<XDropTargetProtocol> getSupportedProtocols() {
            return supportedProtocols;
        }
        public boolean hasSites() {
            return !sites.isEmpty();
        }
        public long[] getSites() {
            long[] ret = new long[sites.size()];
            Iterator iter = sites.iterator();
            int index = 0;
            while (iter.hasNext()) {
                Long l = (Long)iter.next();
                ret[index++] = l.longValue();
            }
            return ret;
        }
        public long getSite(int x, int y) {
            assert XToolkit.isAWTLockHeldByCurrentThread();
            Iterator<Long> iter = sites.iterator();
            while (iter.hasNext()) {
                Long l = iter.next();
                long window = l.longValue();
                Point p = XBaseWindow.toOtherWindow(getRoot(), window, x, y);
                if (p == null) {
                    continue;
                }
                int dest_x = p.x;
                int dest_y = p.y;
                if (dest_x >= 0 && dest_y >= 0) {
                    XWindowAttributes wattr = new XWindowAttributes();
                    try {
                        XToolkit.WITH_XERROR_HANDLER(XErrorHandler.IgnoreBadWindowHandler.getInstance());
                        int status = XlibWrapper.XGetWindowAttributes(XToolkit.getDisplay(),
                                                                      window, wattr.pData);
                        XToolkit.RESTORE_XERROR_HANDLER();
                        if (status == 0 ||
                            (XToolkit.saved_error != null &&
                             XToolkit.saved_error.get_error_code() != XConstants.Success)) {
                            continue;
                        }
                        if (wattr.get_map_state() != XConstants.IsUnmapped
                            && dest_x < wattr.get_width()
                            && dest_y < wattr.get_height()) {
                            return window;
                        }
                    } finally {
                        wattr.dispose();
                    }
                }
            }
            return 0;
        }
    }
    private final HashMap<Long, EmbeddedDropSiteEntry> embeddedDropSiteRegistry =
        new HashMap<Long, EmbeddedDropSiteEntry>();
    private EmbeddedDropSiteEntry registerEmbedderDropSite(long embedder) {
        assert XToolkit.isAWTLockHeldByCurrentThread();
        Iterator dropTargetProtocols =
            XDragAndDropProtocols.getDropTargetProtocols();
        List<XDropTargetProtocol> embedderProtocols = new ArrayList();
        while (dropTargetProtocols.hasNext()) {
            XDropTargetProtocol dropTargetProtocol =
                (XDropTargetProtocol)dropTargetProtocols.next();
            if (dropTargetProtocol.isProtocolSupported(embedder)) {
                embedderProtocols.add(dropTargetProtocol);
            }
        }
        embedderProtocols = Collections.unmodifiableList(embedderProtocols);
        XlibWrapper.XGrabServer(XToolkit.getDisplay());
        try {
            long root = 0;
            long event_mask = 0;
            XWindowAttributes wattr = new XWindowAttributes();
            try {
                XToolkit.WITH_XERROR_HANDLER(XErrorHandler.IgnoreBadWindowHandler.getInstance());
                int status = XlibWrapper.XGetWindowAttributes(XToolkit.getDisplay(),
                                                              embedder, wattr.pData);
                XToolkit.RESTORE_XERROR_HANDLER();
                if (status == 0 ||
                    (XToolkit.saved_error != null &&
                     XToolkit.saved_error.get_error_code() != XConstants.Success)) {
                    throw new XException("XGetWindowAttributes failed");
                }
                event_mask = wattr.get_your_event_mask();
                root = wattr.get_root();
            } finally {
                wattr.dispose();
            }
            if ((event_mask & XConstants.PropertyChangeMask) == 0) {
                XToolkit.WITH_XERROR_HANDLER(XErrorHandler.IgnoreBadWindowHandler.getInstance());
                XlibWrapper.XSelectInput(XToolkit.getDisplay(), embedder,
                                         event_mask | XConstants.PropertyChangeMask);
                XToolkit.RESTORE_XERROR_HANDLER();
                if (XToolkit.saved_error != null &&
                    XToolkit.saved_error.get_error_code() != XConstants.Success) {
                    throw new XException("XSelectInput failed");
                }
            }
            return new EmbeddedDropSiteEntry(root, event_mask, embedderProtocols);
        } finally {
            XlibWrapper.XUngrabServer(XToolkit.getDisplay());
        }
    }
    private static final boolean XEMBED_PROTOCOLS = true;
    private static final boolean NON_XEMBED_PROTOCOLS = false;
    private void registerProtocols(long embedder, boolean protocols,
                                   List<XDropTargetProtocol> supportedProtocols) {
        Iterator dropTargetProtocols = null;
        if (!supportedProtocols.isEmpty()) {
            dropTargetProtocols = supportedProtocols.iterator();
        } else {
            dropTargetProtocols =
                XDragAndDropProtocols.getDropTargetProtocols();
        }
        XlibWrapper.XGrabServer(XToolkit.getDisplay());
        try {
            while (dropTargetProtocols.hasNext()) {
                XDropTargetProtocol dropTargetProtocol =
                    (XDropTargetProtocol)dropTargetProtocols.next();
                if ((protocols == XEMBED_PROTOCOLS) ==
                    dropTargetProtocol.isXEmbedSupported()) {
                    dropTargetProtocol.registerEmbedderDropSite(embedder);
                }
            }
        } finally {
            XlibWrapper.XUngrabServer(XToolkit.getDisplay());
        }
    }
    public void updateEmbedderDropSite(long embedder) {
        XBaseWindow xbaseWindow = XToolkit.windowToXWindow(embedder);
        if (xbaseWindow != null) {
            return;
        }
        assert XToolkit.isAWTLockHeldByCurrentThread();
        Iterator dropTargetProtocols =
            XDragAndDropProtocols.getDropTargetProtocols();
        List<XDropTargetProtocol> embedderProtocols = new ArrayList();
        while (dropTargetProtocols.hasNext()) {
            XDropTargetProtocol dropTargetProtocol =
                (XDropTargetProtocol)dropTargetProtocols.next();
            if (dropTargetProtocol.isProtocolSupported(embedder)) {
                embedderProtocols.add(dropTargetProtocol);
            }
        }
        embedderProtocols = Collections.unmodifiableList(embedderProtocols);
        Long lToplevel = Long.valueOf(embedder);
        boolean isXEmbedServer = false;
        synchronized (this) {
            EmbeddedDropSiteEntry entry =
                (EmbeddedDropSiteEntry)embeddedDropSiteRegistry.get(lToplevel);
            if (entry == null) {
                return;
            }
            entry.setSupportedProtocols(embedderProtocols);
            isXEmbedServer = !entry.hasNonXEmbedClientSites();
        }
        if (!embedderProtocols.isEmpty()) {
            dropTargetProtocols = embedderProtocols.iterator();
        } else {
            dropTargetProtocols =
                XDragAndDropProtocols.getDropTargetProtocols();
        }
        XlibWrapper.XGrabServer(XToolkit.getDisplay());
        try {
            while (dropTargetProtocols.hasNext()) {
                XDropTargetProtocol dropTargetProtocol =
                    (XDropTargetProtocol)dropTargetProtocols.next();
                if (!isXEmbedServer || !dropTargetProtocol.isXEmbedSupported()) {
                    dropTargetProtocol.registerEmbedderDropSite(embedder);
                }
            }
        } finally {
            XlibWrapper.XUngrabServer(XToolkit.getDisplay());
        }
    }
    private void unregisterEmbedderDropSite(long embedder,
                                            EmbeddedDropSiteEntry entry) {
        assert XToolkit.isAWTLockHeldByCurrentThread();
        Iterator dropTargetProtocols =
            XDragAndDropProtocols.getDropTargetProtocols();
        XlibWrapper.XGrabServer(XToolkit.getDisplay());
        try {
            while (dropTargetProtocols.hasNext()) {
                XDropTargetProtocol dropTargetProtocol =
                    (XDropTargetProtocol)dropTargetProtocols.next();
                dropTargetProtocol.unregisterEmbedderDropSite(embedder);
            }
            long event_mask = entry.getEventMask();
            if ((event_mask & XConstants.PropertyChangeMask) == 0) {
                XToolkit.WITH_XERROR_HANDLER(XErrorHandler.IgnoreBadWindowHandler.getInstance());
                XlibWrapper.XSelectInput(XToolkit.getDisplay(), embedder,
                                         event_mask);
                XToolkit.RESTORE_XERROR_HANDLER();
                if (XToolkit.saved_error != null &&
                    XToolkit.saved_error.get_error_code() != XConstants.Success) {
                    throw new XException("XSelectInput failed");
                }
            }
        } finally {
            XlibWrapper.XUngrabServer(XToolkit.getDisplay());
        }
    }
    private void registerEmbeddedDropSite(long toplevel, long window) {
        XBaseWindow xBaseWindow = XToolkit.windowToXWindow(window);
        boolean isXEmbedClient =
            (xBaseWindow instanceof XEmbeddedFramePeer) &&
            ((XEmbeddedFramePeer)xBaseWindow).isXEmbedActive();
        XEmbedCanvasPeer peer = null;
        {
            XBaseWindow xbaseWindow = XToolkit.windowToXWindow(toplevel);
            if (xbaseWindow != null) {
                if (xbaseWindow instanceof XEmbedCanvasPeer) {
                    peer = (XEmbedCanvasPeer)xbaseWindow;
                } else {
                    throw new UnsupportedOperationException();
                }
            }
        }
        Long lToplevel = Long.valueOf(toplevel);
        EmbeddedDropSiteEntry entry = null;
        synchronized (this) {
            entry =
                (EmbeddedDropSiteEntry)embeddedDropSiteRegistry.get(lToplevel);
            if (entry == null) {
                if (peer != null) {
                    peer.setXEmbedDropTarget();
                    entry = new EmbeddedDropSiteEntry(0, 0,
                                                      Collections.<XDropTargetProtocol>emptyList());
                } else {
                    entry = registerEmbedderDropSite(toplevel);
                    registerProtocols(toplevel, NON_XEMBED_PROTOCOLS,
                                      entry.getSupportedProtocols());
                }
                embeddedDropSiteRegistry.put(lToplevel, entry);
            }
        }
        assert entry != null;
        synchronized (entry) {
            if (peer == null) {
                if (!isXEmbedClient) {
                    registerProtocols(toplevel, XEMBED_PROTOCOLS,
                                      entry.getSupportedProtocols());
                } else {
                    Iterator dropTargetProtocols =
                        XDragAndDropProtocols.getDropTargetProtocols();
                    while (dropTargetProtocols.hasNext()) {
                        XDropTargetProtocol dropTargetProtocol =
                            (XDropTargetProtocol)dropTargetProtocols.next();
                        if (dropTargetProtocol.isXEmbedSupported()) {
                            dropTargetProtocol.registerEmbedderDropSite(window);
                        }
                    }
                }
            }
            entry.addSite(window, isXEmbedClient);
        }
    }
    private void unregisterEmbeddedDropSite(long toplevel, long window) {
        Long lToplevel = Long.valueOf(toplevel);
        EmbeddedDropSiteEntry entry = null;
        synchronized (this) {
            entry =
                (EmbeddedDropSiteEntry)embeddedDropSiteRegistry.get(lToplevel);
            if (entry == null) {
                return;
            }
            entry.removeSite(window);
            if (!entry.hasSites()) {
                embeddedDropSiteRegistry.remove(lToplevel);
                XBaseWindow xbaseWindow = XToolkit.windowToXWindow(toplevel);
                if (xbaseWindow != null) {
                    if (xbaseWindow instanceof XEmbedCanvasPeer) {
                        XEmbedCanvasPeer peer = (XEmbedCanvasPeer)xbaseWindow;
                        peer.removeXEmbedDropTarget();
                    } else {
                        throw new UnsupportedOperationException();
                    }
                } else {
                    unregisterEmbedderDropSite(toplevel, entry);
                }
            }
        }
    }
    public long getEmbeddedDropSite(long embedder, int x, int y) {
        Long lToplevel = Long.valueOf(embedder);
        EmbeddedDropSiteEntry entry =
            (EmbeddedDropSiteEntry)embeddedDropSiteRegistry.get(lToplevel);
        if (entry == null) {
            return 0;
        }
        return entry.getSite(x, y);
    }
    public void registerDropSite(long window) {
        assert XToolkit.isAWTLockHeldByCurrentThread();
        if (window == 0) {
            throw new IllegalArgumentException();
        }
        XDropTargetEventProcessor.activate();
        long toplevel = getToplevelWindow(window);
        if (toplevel == 0) {
            addDelayedRegistrationEntry(window);
            return;
        }
        if (toplevel == window) {
            Iterator dropTargetProtocols =
                XDragAndDropProtocols.getDropTargetProtocols();
            while (dropTargetProtocols.hasNext()) {
                XDropTargetProtocol dropTargetProtocol =
                    (XDropTargetProtocol)dropTargetProtocols.next();
                dropTargetProtocol.registerDropTarget(toplevel);
            }
        } else {
            registerEmbeddedDropSite(toplevel, window);
        }
    }
    public void unregisterDropSite(long window) {
        assert XToolkit.isAWTLockHeldByCurrentThread();
        if (window == 0) {
            throw new IllegalArgumentException();
        }
        long toplevel = getToplevelWindow(window);
        if (toplevel == window) {
            Iterator dropProtocols =
                XDragAndDropProtocols.getDropTargetProtocols();
            removeDelayedRegistrationEntry(window);
            while (dropProtocols.hasNext()) {
                XDropTargetProtocol dropProtocol = (XDropTargetProtocol)dropProtocols.next();
                dropProtocol.unregisterDropTarget(window);
            }
        } else {
            unregisterEmbeddedDropSite(toplevel, window);
        }
    }
    public void registerXEmbedClient(long canvasWindow, long clientWindow) {
        XDragSourceProtocol xdndDragProtocol =
            XDragAndDropProtocols.getDragSourceProtocol(XDragAndDropProtocols.XDnD);
        XDragSourceProtocol.TargetWindowInfo info =
            xdndDragProtocol.getTargetWindowInfo(clientWindow);
        if (info != null &&
            info.getProtocolVersion() >= XDnDConstants.XDND_MIN_PROTOCOL_VERSION) {
            if (logger.isLoggable(PlatformLogger.FINE)) {
                logger.fine("        XEmbed drop site will be registered for " + Long.toHexString(clientWindow));
            }
            registerEmbeddedDropSite(canvasWindow, clientWindow);
            Iterator dropTargetProtocols =
                XDragAndDropProtocols.getDropTargetProtocols();
            while (dropTargetProtocols.hasNext()) {
                XDropTargetProtocol dropTargetProtocol =
                    (XDropTargetProtocol)dropTargetProtocols.next();
                dropTargetProtocol.registerEmbeddedDropSite(clientWindow);
            }
            if (logger.isLoggable(PlatformLogger.FINE)) {
                logger.fine("        XEmbed drop site has been registered for " + Long.toHexString(clientWindow));
            }
        }
    }
    public void unregisterXEmbedClient(long canvasWindow, long clientWindow) {
        if (logger.isLoggable(PlatformLogger.FINE)) {
            logger.fine("        XEmbed drop site will be unregistered for " + Long.toHexString(clientWindow));
        }
        Iterator dropTargetProtocols =
            XDragAndDropProtocols.getDropTargetProtocols();
        while (dropTargetProtocols.hasNext()) {
            XDropTargetProtocol dropTargetProtocol =
                (XDropTargetProtocol)dropTargetProtocols.next();
            dropTargetProtocol.unregisterEmbeddedDropSite(clientWindow);
        }
        unregisterEmbeddedDropSite(canvasWindow, clientWindow);
        if (logger.isLoggable(PlatformLogger.FINE)) {
            logger.fine("        XEmbed drop site has beed unregistered for " + Long.toHexString(clientWindow));
        }
    }
    private void addDelayedRegistrationEntry(final long window) {
        Long lWindow = Long.valueOf(window);
        Runnable runnable = new Runnable() {
                public void run() {
                    removeDelayedRegistrationEntry(window);
                    registerDropSite(window);
                }
            };
        XToolkit.awtLock();
        try {
            removeDelayedRegistrationEntry(window);
            delayedRegistrationMap.put(lWindow, runnable);
            XToolkit.schedule(runnable, DELAYED_REGISTRATION_PERIOD);
        } finally {
            XToolkit.awtUnlock();
        }
    }
    private void removeDelayedRegistrationEntry(long window) {
        Long lWindow = Long.valueOf(window);
        XToolkit.awtLock();
        try {
            Runnable runnable = delayedRegistrationMap.remove(lWindow);
            if (runnable != null) {
                XToolkit.remove(runnable);
            }
        } finally {
            XToolkit.awtUnlock();
        }
    }
}
