class XDnDDropTargetProtocol extends XDropTargetProtocol {
    private static final PlatformLogger logger =
        PlatformLogger.getLogger("sun.awt.X11.xembed.xdnd.XDnDDropTargetProtocol");
    private static final Unsafe unsafe = XlibWrapper.unsafe;
    private long sourceWindow = 0;
    private long sourceWindowMask = 0;
    private int sourceProtocolVersion = 0;
    private int sourceActions = DnDConstants.ACTION_NONE;
    private long[] sourceFormats = null;
    private boolean trackSourceActions = false;
    private int userAction = DnDConstants.ACTION_NONE;
    private int sourceX = 0;
    private int sourceY = 0;
    private XWindow targetXWindow = null;
    private long prevCtxt = 0;
    private boolean overXEmbedClient = false;
    protected XDnDDropTargetProtocol(XDropTargetProtocolListener listener) {
        super(listener);
    }
    static XDropTargetProtocol createInstance(XDropTargetProtocolListener listener) {
        return new XDnDDropTargetProtocol(listener);
    }
    public String getProtocolName() {
        return XDragAndDropProtocols.XDnD;
    }
    public void registerDropTarget(long window) {
        assert XToolkit.isAWTLockHeldByCurrentThread();
        long data = Native.allocateLongArray(1);
        try {
            Native.putLong(data, 0, XDnDConstants.XDND_PROTOCOL_VERSION);
            XToolkit.WITH_XERROR_HANDLER(XErrorHandler.VerifyChangePropertyHandler.getInstance());
            XDnDConstants.XA_XdndAware.setAtomData(window, XAtom.XA_ATOM, data, 1);
            XToolkit.RESTORE_XERROR_HANDLER();
            if (XToolkit.saved_error != null &&
                XToolkit.saved_error.get_error_code() != XConstants.Success) {
                throw new XException("Cannot write XdndAware property");
            }
        } finally {
            unsafe.freeMemory(data);
            data = 0;
        }
    }
    public void unregisterDropTarget(long window) {
        assert XToolkit.isAWTLockHeldByCurrentThread();
        XDnDConstants.XA_XdndAware.DeleteProperty(window);
    }
    public void registerEmbedderDropSite(long embedder) {
        assert XToolkit.isAWTLockHeldByCurrentThread();
        boolean overriden = false;
        int version = 0;
        long proxy = 0;
        long newProxy = XDropTargetRegistry.getDnDProxyWindow();
        int status = 0;
        WindowPropertyGetter wpg1 =
            new WindowPropertyGetter(embedder, XDnDConstants.XA_XdndAware, 0, 1,
                                     false, XConstants.AnyPropertyType);
        try {
            status = wpg1.execute(XErrorHandler.IgnoreBadWindowHandler.getInstance());
            if (status == XConstants.Success &&
                wpg1.getData() != 0 && wpg1.getActualType() == XAtom.XA_ATOM) {
                overriden = true;
                version = (int)Native.getLong(wpg1.getData());
            }
        } finally {
            wpg1.dispose();
        }
        if (overriden && version >= 4) {
            WindowPropertyGetter wpg2 =
                new WindowPropertyGetter(embedder, XDnDConstants.XA_XdndProxy,
                                         0, 1, false, XAtom.XA_WINDOW);
            try {
                status = wpg2.execute(XErrorHandler.IgnoreBadWindowHandler.getInstance());
                if (status == XConstants.Success &&
                    wpg2.getData() != 0 &&
                    wpg2.getActualType() == XAtom.XA_WINDOW) {
                    proxy = Native.getLong(wpg2.getData());
                }
            } finally {
                wpg2.dispose();
            }
            if (proxy != 0) {
                WindowPropertyGetter wpg3 =
                    new WindowPropertyGetter(proxy, XDnDConstants.XA_XdndProxy,
                                             0, 1, false, XAtom.XA_WINDOW);
                try {
                    status = wpg3.execute(XErrorHandler.IgnoreBadWindowHandler.getInstance());
                    if (status != XConstants.Success ||
                        wpg3.getData() == 0 ||
                        wpg3.getActualType() != XAtom.XA_WINDOW ||
                        Native.getLong(wpg3.getData()) != proxy) {
                        proxy = 0;
                    } else {
                        WindowPropertyGetter wpg4 =
                            new WindowPropertyGetter(proxy,
                                                     XDnDConstants.XA_XdndAware,
                                                     0, 1, false,
                                                     XConstants.AnyPropertyType);
                        try {
                            status = wpg4.execute(XErrorHandler.IgnoreBadWindowHandler.getInstance());
                            if (status != XConstants.Success ||
                                wpg4.getData() == 0 ||
                                wpg4.getActualType() != XAtom.XA_ATOM) {
                                proxy = 0;
                            }
                        } finally {
                            wpg4.dispose();
                        }
                    }
                } finally {
                    wpg3.dispose();
                }
            }
        }
        if (proxy == newProxy) {
            return;
        }
        long data = Native.allocateLongArray(1);
        try {
            Native.putLong(data, 0, XDnDConstants.XDND_PROTOCOL_VERSION);
            XToolkit.WITH_XERROR_HANDLER(XErrorHandler.VerifyChangePropertyHandler.getInstance());
            XDnDConstants.XA_XdndAware.setAtomData(newProxy, XAtom.XA_ATOM,
                                                   data, 1);
            XToolkit.RESTORE_XERROR_HANDLER();
            if (XToolkit.saved_error != null &&
                XToolkit.saved_error.get_error_code() !=
                XConstants.Success) {
                throw new XException("Cannot write XdndAware property");
            }
            Native.putLong(data, 0, newProxy);
            XToolkit.WITH_XERROR_HANDLER(XErrorHandler.VerifyChangePropertyHandler.getInstance());
            XDnDConstants.XA_XdndProxy.setAtomData(newProxy, XAtom.XA_WINDOW,
                                                   data, 1);
            XToolkit.RESTORE_XERROR_HANDLER();
            if (XToolkit.saved_error != null &&
                XToolkit.saved_error.get_error_code() !=
                XConstants.Success) {
                throw new XException("Cannot write XdndProxy property");
            }
            Native.putLong(data, 0, XDnDConstants.XDND_PROTOCOL_VERSION);
            XToolkit.WITH_XERROR_HANDLER(XErrorHandler.VerifyChangePropertyHandler.getInstance());
            XDnDConstants.XA_XdndAware.setAtomData(embedder, XAtom.XA_ATOM,
                                                   data, 1);
            XToolkit.RESTORE_XERROR_HANDLER();
            if (XToolkit.saved_error != null &&
                XToolkit.saved_error.get_error_code() !=
                XConstants.Success) {
                throw new XException("Cannot write XdndAware property");
            }
            Native.putLong(data, 0, newProxy);
            XToolkit.WITH_XERROR_HANDLER(XErrorHandler.VerifyChangePropertyHandler.getInstance());
            XDnDConstants.XA_XdndProxy.setAtomData(embedder, XAtom.XA_WINDOW,
                                                   data, 1);
            XToolkit.RESTORE_XERROR_HANDLER();
            if (XToolkit.saved_error != null &&
                XToolkit.saved_error.get_error_code() !=
                XConstants.Success) {
                throw new XException("Cannot write XdndProxy property");
            }
        } finally {
            unsafe.freeMemory(data);
            data = 0;
        }
        putEmbedderRegistryEntry(embedder, overriden, version, proxy);
    }
    public void unregisterEmbedderDropSite(long embedder) {
        assert XToolkit.isAWTLockHeldByCurrentThread();
        EmbedderRegistryEntry entry = getEmbedderRegistryEntry(embedder);
        if (entry == null) {
            return;
        }
        if (entry.isOverriden()) {
            long data = Native.allocateLongArray(1);
            try {
                Native.putLong(data, 0, entry.getVersion());
                XToolkit.WITH_XERROR_HANDLER(XErrorHandler.VerifyChangePropertyHandler.getInstance());
                XDnDConstants.XA_XdndAware.setAtomData(embedder, XAtom.XA_ATOM,
                                                       data, 1);
                XToolkit.RESTORE_XERROR_HANDLER();
                if (XToolkit.saved_error != null &&
                    XToolkit.saved_error.get_error_code() !=
                    XConstants.Success) {
                    throw new XException("Cannot write XdndAware property");
                }
                Native.putLong(data, 0, (int)entry.getProxy());
                XToolkit.WITH_XERROR_HANDLER(XErrorHandler.VerifyChangePropertyHandler.getInstance());
                XDnDConstants.XA_XdndProxy.setAtomData(embedder, XAtom.XA_WINDOW,
                                                       data, 1);
                XToolkit.RESTORE_XERROR_HANDLER();
                if (XToolkit.saved_error != null &&
                    XToolkit.saved_error.get_error_code() !=
                    XConstants.Success) {
                    throw new XException("Cannot write XdndProxy property");
                }
            } finally {
                unsafe.freeMemory(data);
                data = 0;
            }
        } else {
            XDnDConstants.XA_XdndAware.DeleteProperty(embedder);
            XDnDConstants.XA_XdndProxy.DeleteProperty(embedder);
        }
    }
    public void registerEmbeddedDropSite(long embedded) {
        assert XToolkit.isAWTLockHeldByCurrentThread();
        boolean overriden = false;
        int version = 0;
        long proxy = 0;
        long newProxy = XDropTargetRegistry.getDnDProxyWindow();
        int status = 0;
        WindowPropertyGetter wpg1 =
            new WindowPropertyGetter(embedded, XDnDConstants.XA_XdndAware, 0, 1,
                                     false, XConstants.AnyPropertyType);
        try {
            status = wpg1.execute(XErrorHandler.IgnoreBadWindowHandler.getInstance());
            if (status == XConstants.Success &&
                wpg1.getData() != 0 && wpg1.getActualType() == XAtom.XA_ATOM) {
                overriden = true;
                version = (int)Native.getLong(wpg1.getData());
            }
        } finally {
            wpg1.dispose();
        }
        if (overriden && version >= 4) {
            WindowPropertyGetter wpg2 =
                new WindowPropertyGetter(embedded, XDnDConstants.XA_XdndProxy,
                                         0, 1, false, XAtom.XA_WINDOW);
            try {
                status = wpg2.execute(XErrorHandler.IgnoreBadWindowHandler.getInstance());
                if (status == XConstants.Success &&
                    wpg2.getData() != 0 &&
                    wpg2.getActualType() == XAtom.XA_WINDOW) {
                    proxy = Native.getLong(wpg2.getData());
                }
            } finally {
                wpg2.dispose();
            }
            if (proxy != 0) {
                WindowPropertyGetter wpg3 =
                    new WindowPropertyGetter(proxy, XDnDConstants.XA_XdndProxy,
                                             0, 1, false, XAtom.XA_WINDOW);
                try {
                    status = wpg3.execute(XErrorHandler.IgnoreBadWindowHandler.getInstance());
                    if (status != XConstants.Success ||
                        wpg3.getData() == 0 ||
                        wpg3.getActualType() != XAtom.XA_WINDOW ||
                        Native.getLong(wpg3.getData()) != proxy) {
                        proxy = 0;
                    } else {
                        WindowPropertyGetter wpg4 =
                            new WindowPropertyGetter(proxy,
                                                     XDnDConstants.XA_XdndAware,
                                                     0, 1, false,
                                                     XConstants.AnyPropertyType);
                        try {
                            status = wpg4.execute(XErrorHandler.IgnoreBadWindowHandler.getInstance());
                            if (status != XConstants.Success ||
                                wpg4.getData() == 0 ||
                                wpg4.getActualType() != XAtom.XA_ATOM) {
                                proxy = 0;
                            }
                        } finally {
                            wpg4.dispose();
                        }
                    }
                } finally {
                    wpg3.dispose();
                }
            }
        }
        putEmbedderRegistryEntry(embedded, overriden, version, proxy);
    }
    public boolean isProtocolSupported(long window) {
        assert XToolkit.isAWTLockHeldByCurrentThread();
        WindowPropertyGetter wpg1 =
            new WindowPropertyGetter(window, XDnDConstants.XA_XdndAware, 0, 1,
                                     false, XConstants.AnyPropertyType);
        try {
            int status = wpg1.execute(XErrorHandler.IgnoreBadWindowHandler.getInstance());
            if (status == XConstants.Success &&
                wpg1.getData() != 0 && wpg1.getActualType() == XAtom.XA_ATOM) {
                return true;
            } else {
                return false;
            }
        } finally {
            wpg1.dispose();
        }
    }
    private boolean processXdndEnter(XClientMessageEvent xclient) {
        long source_win = 0;
        long source_win_mask = 0;
        int protocol_version = 0;
        int actions = DnDConstants.ACTION_NONE;
        boolean track = true;
        long[] formats = null;
        if (getSourceWindow() != 0) {
            return false;
        }
        if (!(XToolkit.windowToXWindow(xclient.get_window()) instanceof XWindow)
            && getEmbedderRegistryEntry(xclient.get_window()) == null) {
            return false;
        }
        if (xclient.get_message_type() != XDnDConstants.XA_XdndEnter.getAtom()){
            return false;
        }
        protocol_version =
            (int)((xclient.get_data(1) & XDnDConstants.XDND_PROTOCOL_MASK) >>
                  XDnDConstants.XDND_PROTOCOL_SHIFT);
        if (protocol_version < XDnDConstants.XDND_MIN_PROTOCOL_VERSION) {
            return false;
        }
        if (protocol_version > XDnDConstants.XDND_PROTOCOL_VERSION) {
            return false;
        }
        source_win = xclient.get_data(0);
        if (protocol_version < 2) {
            actions = DnDConstants.ACTION_COPY;
        } else {
            WindowPropertyGetter wpg =
                new WindowPropertyGetter(source_win,
                                         XDnDConstants.XA_XdndActionList,
                                         0, 0xFFFF, false,
                                         XAtom.XA_ATOM);
            try {
                wpg.execute(XErrorHandler.IgnoreBadWindowHandler.getInstance());
                if (wpg.getActualType() == XAtom.XA_ATOM &&
                    wpg.getActualFormat() == 32) {
                    long data = wpg.getData();
                    for (int i = 0; i < wpg.getNumberOfItems(); i++) {
                        actions |=
                            XDnDConstants.getJavaActionForXDnDAction(Native.getLong(data, i));
                    }
                } else {
                    actions = DnDConstants.ACTION_COPY;
                    track = true;
                }
            } finally {
                wpg.dispose();
            }
        }
        if ((xclient.get_data(1) & XDnDConstants.XDND_DATA_TYPES_BIT) != 0) {
            WindowPropertyGetter wpg =
                new WindowPropertyGetter(source_win,
                                         XDnDConstants.XA_XdndTypeList,
                                         0, 0xFFFF, false,
                                         XAtom.XA_ATOM);
            try {
                wpg.execute(XErrorHandler.IgnoreBadWindowHandler.getInstance());
                if (wpg.getActualType() == XAtom.XA_ATOM &&
                    wpg.getActualFormat() == 32) {
                    formats = Native.toLongs(wpg.getData(),
                                             wpg.getNumberOfItems());
                } else {
                    formats = new long[0];
                }
            } finally {
                wpg.dispose();
            }
        } else {
            int countFormats = 0;
            long[] formats3 = new long[3];
            for (int i = 0; i < 3; i++) {
                long j;
                if ((j = xclient.get_data(2 + i)) != XConstants.None) {
                    formats3[countFormats++] = j;
                }
            }
            formats = new long[countFormats];
            System.arraycopy(formats3, 0, formats, 0, countFormats);
        }
        assert XToolkit.isAWTLockHeldByCurrentThread();
        XWindowAttributes wattr = new XWindowAttributes();
        try {
            XToolkit.WITH_XERROR_HANDLER(XErrorHandler.IgnoreBadWindowHandler.getInstance());
            int status = XlibWrapper.XGetWindowAttributes(XToolkit.getDisplay(),
                                                          source_win, wattr.pData);
            XToolkit.RESTORE_XERROR_HANDLER();
            if (status == 0 ||
                (XToolkit.saved_error != null &&
                 XToolkit.saved_error.get_error_code() != XConstants.Success)) {
                throw new XException("XGetWindowAttributes failed");
            }
            source_win_mask = wattr.get_your_event_mask();
        } finally {
            wattr.dispose();
        }
        XToolkit.WITH_XERROR_HANDLER(XErrorHandler.IgnoreBadWindowHandler.getInstance());
        XlibWrapper.XSelectInput(XToolkit.getDisplay(), source_win,
                                 source_win_mask |
                                 XConstants.StructureNotifyMask);
        XToolkit.RESTORE_XERROR_HANDLER();
        if (XToolkit.saved_error != null &&
            XToolkit.saved_error.get_error_code() != XConstants.Success) {
            throw new XException("XSelectInput failed");
        }
        sourceWindow = source_win;
        sourceWindowMask = source_win_mask;
        sourceProtocolVersion = protocol_version;
        sourceActions = actions;
        sourceFormats = formats;
        trackSourceActions = track;
        return true;
    }
    private boolean processXdndPosition(XClientMessageEvent xclient) {
        long time_stamp = (int)XConstants.CurrentTime;
        long xdnd_action = 0;
        int java_action = DnDConstants.ACTION_NONE;
        int x = 0;
        int y = 0;
        if (sourceWindow != xclient.get_data(0)) {
            return false;
        }
        XWindow xwindow = null;
        {
            XBaseWindow xbasewindow = XToolkit.windowToXWindow(xclient.get_window());
            if (xbasewindow instanceof XWindow) {
                xwindow = (XWindow)xbasewindow;
            }
        }
        x = (int)(xclient.get_data(2) >> 16);
        y = (int)(xclient.get_data(2) & 0xFFFF);
        if (xwindow == null) {
            long receiver =
                XDropTargetRegistry.getRegistry().getEmbeddedDropSite(
                    xclient.get_window(), x, y);
            if (receiver != 0) {
                XBaseWindow xbasewindow = XToolkit.windowToXWindow(receiver);
                if (xbasewindow instanceof XWindow) {
                    xwindow = (XWindow)xbasewindow;
                }
            }
        }
        if (xwindow != null) {
            Point p = xwindow.toLocal(x, y);
            x = p.x;
            y = p.y;
        }
        if (sourceProtocolVersion > 0) {
            time_stamp = xclient.get_data(3);
        }
        if (sourceProtocolVersion > 1) {
            xdnd_action = xclient.get_data(4);
        } else {
            xdnd_action = XDnDConstants.XA_XdndActionCopy.getAtom();
        }
        java_action = XDnDConstants.getJavaActionForXDnDAction(xdnd_action);
        if (trackSourceActions) {
            sourceActions |= java_action;
        }
        if (xwindow == null) {
            if (targetXWindow != null) {
                notifyProtocolListener(targetXWindow, x, y,
                                       DnDConstants.ACTION_NONE, xclient,
                                       MouseEvent.MOUSE_EXITED);
            }
        } else {
            int java_event_id = 0;
            if (targetXWindow == null) {
                java_event_id = MouseEvent.MOUSE_ENTERED;
            } else {
                java_event_id = MouseEvent.MOUSE_DRAGGED;
            }
            notifyProtocolListener(xwindow, x, y, java_action, xclient,
                                   java_event_id);
        }
        userAction = java_action;
        sourceX = x;
        sourceY = y;
        targetXWindow = xwindow;
        return true;
    }
    private boolean processXdndLeave(XClientMessageEvent xclient) {
        if (sourceWindow != xclient.get_data(0)) {
            return false;
        }
        cleanup();
        return true;
    }
    private boolean processXdndDrop(XClientMessageEvent xclient) {
        if (sourceWindow != xclient.get_data(0)) {
            return false;
        }
        if (targetXWindow != null) {
            notifyProtocolListener(targetXWindow, sourceX, sourceY, userAction,
                                   xclient, MouseEvent.MOUSE_RELEASED);
        }
        return true;
    }
    public int getMessageType(XClientMessageEvent xclient) {
        long messageType = xclient.get_message_type();
        if (messageType == XDnDConstants.XA_XdndEnter.getAtom()) {
            return ENTER_MESSAGE;
        } else if (messageType == XDnDConstants.XA_XdndPosition.getAtom()) {
            return MOTION_MESSAGE;
        } else if (messageType == XDnDConstants.XA_XdndLeave.getAtom()) {
            return LEAVE_MESSAGE;
        } else if (messageType == XDnDConstants.XA_XdndDrop.getAtom()) {
            return DROP_MESSAGE;
        } else {
            return UNKNOWN_MESSAGE;
        }
    }
    protected boolean processClientMessageImpl(XClientMessageEvent xclient) {
        long messageType = xclient.get_message_type();
        if (messageType == XDnDConstants.XA_XdndEnter.getAtom()) {
            return processXdndEnter(xclient);
        } else if (messageType == XDnDConstants.XA_XdndPosition.getAtom()) {
            return processXdndPosition(xclient);
        } else if (messageType == XDnDConstants.XA_XdndLeave.getAtom()) {
            return processXdndLeave(xclient);
        } else if (messageType == XDnDConstants.XA_XdndDrop.getAtom()) {
            return processXdndDrop(xclient);
        } else {
            return false;
        }
    }
    protected void sendEnterMessageToToplevel(long toplevel,
                                              XClientMessageEvent xclient) {
        long data1 = sourceProtocolVersion << XDnDConstants.XDND_PROTOCOL_SHIFT;
        if (sourceFormats != null && sourceFormats.length > 3) {
            data1 |= XDnDConstants.XDND_DATA_TYPES_BIT;
        }
        long data2 = sourceFormats.length > 0 ? sourceFormats[0] : 0;
        long data3 = sourceFormats.length > 1 ? sourceFormats[1] : 0;
        long data4 = sourceFormats.length > 2 ? sourceFormats[2] : 0;
        sendEnterMessageToToplevelImpl(toplevel, xclient.get_data(0),
                                       data1, data2, data3, data4);
    }
    private void sendEnterMessageToToplevelImpl(long toplevel,
                                                long sourceWindow,
                                                long data1, long data2,
                                                long data3, long data4) {
        XClientMessageEvent enter = new XClientMessageEvent();
        try {
            enter.set_type((int)XConstants.ClientMessage);
            enter.set_window(toplevel);
            enter.set_format(32);
            enter.set_message_type(XDnDConstants.XA_XdndEnter.getAtom());
            enter.set_data(0, sourceWindow);
            enter.set_data(1, data1);
            enter.set_data(2, data2);
            enter.set_data(3, data3);
            enter.set_data(4, data4);
            forwardClientMessageToToplevel(toplevel, enter);
        } finally {
            enter.dispose();
        }
    }
    protected void sendLeaveMessageToToplevel(long toplevel,
                                              XClientMessageEvent xclient) {
        sendLeaveMessageToToplevelImpl(toplevel, xclient.get_data(0));
    }
    protected void sendLeaveMessageToToplevelImpl(long toplevel,
                                                  long sourceWindow) {
        XClientMessageEvent leave = new XClientMessageEvent();
        try {
            leave.set_type((int)XConstants.ClientMessage);
            leave.set_window(toplevel);
            leave.set_format(32);
            leave.set_message_type(XDnDConstants.XA_XdndLeave.getAtom());
            leave.set_data(0, sourceWindow);
            leave.set_data(1, 0);
            forwardClientMessageToToplevel(toplevel, leave);
        } finally {
            leave.dispose();
        }
    }
    public boolean sendResponse(long ctxt, int eventID, int action) {
        XClientMessageEvent xclient = new XClientMessageEvent(ctxt);
        if (xclient.get_message_type() !=
            XDnDConstants.XA_XdndPosition.getAtom()) {
            return false;
        }
        if (eventID == MouseEvent.MOUSE_EXITED) {
            action = DnDConstants.ACTION_NONE;
        }
        XClientMessageEvent msg = new XClientMessageEvent();
        try {
            msg.set_type((int)XConstants.ClientMessage);
            msg.set_window(xclient.get_data(0));
            msg.set_format(32);
            msg.set_message_type(XDnDConstants.XA_XdndStatus.getAtom());
            msg.set_data(0, xclient.get_window());
            long flags = 0;
            if (action != DnDConstants.ACTION_NONE) {
                flags |= XDnDConstants.XDND_ACCEPT_DROP_FLAG;
            }
            msg.set_data(1, flags);
            msg.set_data(2, 0); 
            msg.set_data(3, 0); 
            msg.set_data(4, XDnDConstants.getXDnDActionForJavaAction(action));
            XToolkit.awtLock();
            try {
                XlibWrapper.XSendEvent(XToolkit.getDisplay(),
                                       xclient.get_data(0),
                                       false, XConstants.NoEventMask,
                                       msg.pData);
            } finally {
                XToolkit.awtUnlock();
            }
        } finally {
            msg.dispose();
        }
        return true;
    }
    public Object getData(long ctxt, long format)
      throws IllegalArgumentException, IOException {
        XClientMessageEvent xclient = new XClientMessageEvent(ctxt);
        long message_type = xclient.get_message_type();
        long time_stamp = XConstants.CurrentTime;
        if (message_type == XDnDConstants.XA_XdndPosition.getAtom()) {
            time_stamp = xclient.get_data(3) & 0xFFFFFFFFL;
        } else if (message_type == XDnDConstants.XA_XdndDrop.getAtom()) {
            time_stamp = xclient.get_data(2) & 0xFFFFFFFFL;
        } else {
            throw new IllegalArgumentException();
        }
        return XDnDConstants.XDnDSelection.getData(format, time_stamp);
    }
    public boolean sendDropDone(long ctxt, boolean success, int dropAction) {
        XClientMessageEvent xclient = new XClientMessageEvent(ctxt);
        if (xclient.get_message_type() !=
            XDnDConstants.XA_XdndDrop.getAtom()) {
            return false;
        }
        if (dropAction == DnDConstants.ACTION_MOVE && success) {
            long time_stamp = xclient.get_data(2);
            long xdndSelectionAtom =
                XDnDConstants.XDnDSelection.getSelectionAtom().getAtom();
            XToolkit.awtLock();
            try {
                XlibWrapper.XConvertSelection(XToolkit.getDisplay(),
                                              xdndSelectionAtom,
                                              XAtom.get("DELETE").getAtom(),
                                              XAtom.get("XAWT_SELECTION").getAtom(),
                                              XWindow.getXAWTRootWindow().getWindow(),
                                              time_stamp);
            } finally {
                XToolkit.awtUnlock();
            }
        }
        XClientMessageEvent msg = new XClientMessageEvent();
        try {
            msg.set_type((int)XConstants.ClientMessage);
            msg.set_window(xclient.get_data(0));
            msg.set_format(32);
            msg.set_message_type(XDnDConstants.XA_XdndFinished.getAtom());
            msg.set_data(0, xclient.get_window()); 
            msg.set_data(1, 0); 
            msg.set_data(2, 0);
            if (sourceProtocolVersion >= 5) {
                if (success) {
                    msg.set_data(1, XDnDConstants.XDND_ACCEPT_DROP_FLAG);
                }
                msg.set_data(2, XDnDConstants.getXDnDActionForJavaAction(dropAction));
            }
            msg.set_data(3, 0);
            msg.set_data(4, 0);
            XToolkit.awtLock();
            try {
                XlibWrapper.XSendEvent(XToolkit.getDisplay(),
                                       xclient.get_data(0),
                                       false, XConstants.NoEventMask,
                                       msg.pData);
            } finally {
                XToolkit.awtUnlock();
            }
        } finally {
            msg.dispose();
        }
        XToolkit.awtLock();
        try {
            XlibWrapper.XFlush(XToolkit.getDisplay());
        } finally {
            XToolkit.awtUnlock();
        }
        targetXWindow = null;
        cleanup();
        return true;
    }
    public final long getSourceWindow() {
        return sourceWindow;
    }
    public void cleanup() {
        XDropTargetEventProcessor.reset();
        if (targetXWindow != null) {
            notifyProtocolListener(targetXWindow, 0, 0,
                                   DnDConstants.ACTION_NONE, null,
                                   MouseEvent.MOUSE_EXITED);
        }
        if (sourceWindow != 0) {
            XToolkit.awtLock();
            try {
                XToolkit.WITH_XERROR_HANDLER(XErrorHandler.IgnoreBadWindowHandler.getInstance());
                XlibWrapper.XSelectInput(XToolkit.getDisplay(), sourceWindow,
                                         sourceWindowMask);
                XToolkit.RESTORE_XERROR_HANDLER();
            } finally {
                XToolkit.awtUnlock();
            }
        }
        sourceWindow = 0;
        sourceWindowMask = 0;
        sourceProtocolVersion = 0;
        sourceActions = DnDConstants.ACTION_NONE;
        sourceFormats = null;
        trackSourceActions = false;
        userAction = DnDConstants.ACTION_NONE;
        sourceX = 0;
        sourceY = 0;
        targetXWindow = null;
    }
    public boolean isDragOverComponent() {
        return targetXWindow != null;
    }
    public void adjustEventForForwarding(XClientMessageEvent xclient,
                                         EmbedderRegistryEntry entry) {
        int version = entry.getVersion();
        if (xclient.get_message_type() == XDnDConstants.XA_XdndEnter.getAtom()) {
            int min_version = sourceProtocolVersion < version ?
                sourceProtocolVersion : version;
            long data1 = min_version << XDnDConstants.XDND_PROTOCOL_SHIFT;
            if (sourceFormats != null && sourceFormats.length > 3) {
                data1 |= XDnDConstants.XDND_DATA_TYPES_BIT;
            }
            if (logger.isLoggable(PlatformLogger.FINEST)) {
                logger.finest("         "
                              + " entryVersion=" + version
                              + " sourceProtocolVersion=" +
                              sourceProtocolVersion
                              + " sourceFormats.length=" +
                              (sourceFormats != null ? sourceFormats.length : 0));
            }
            xclient.set_data(1, data1);
        }
    }
    private void notifyProtocolListener(XWindow xwindow, int x, int y,
                                        int dropAction,
                                        XClientMessageEvent xclient,
                                        int eventID) {
        long nativeCtxt = 0;
        if (xclient != null) {
            int size = new XClientMessageEvent(nativeCtxt).getSize();
            nativeCtxt = unsafe.allocateMemory(size + 4 * Native.getLongSize());
            unsafe.copyMemory(xclient.pData, nativeCtxt, size);
            long data1 = sourceProtocolVersion << XDnDConstants.XDND_PROTOCOL_SHIFT;
            if (sourceFormats != null && sourceFormats.length > 3) {
                data1 |= XDnDConstants.XDND_DATA_TYPES_BIT;
            }
            Native.putLong(nativeCtxt + size, data1);
            Native.putLong(nativeCtxt + size + Native.getLongSize(),
                           sourceFormats.length > 0 ? sourceFormats[0] : 0);
            Native.putLong(nativeCtxt + size + 2 * Native.getLongSize(),
                           sourceFormats.length > 1 ? sourceFormats[1] : 0);
            Native.putLong(nativeCtxt + size + 3 * Native.getLongSize(),
                           sourceFormats.length > 2 ? sourceFormats[2] : 0);
        }
        getProtocolListener().handleDropTargetNotification(xwindow, x, y,
                                                           dropAction,
                                                           sourceActions,
                                                           sourceFormats,
                                                           nativeCtxt,
                                                           eventID);
    }
    public boolean forwardEventToEmbedded(long embedded, long ctxt,
                                          int eventID) {
        if (logger.isLoggable(PlatformLogger.FINEST)) {
            logger.finest("        ctxt=" + ctxt +
                          " type=" + (ctxt != 0 ?
                                      getMessageType(new
                                          XClientMessageEvent(ctxt)) : 0) +
                          " prevCtxt=" + prevCtxt +
                          " prevType=" + (prevCtxt != 0 ?
                                      getMessageType(new
                                          XClientMessageEvent(prevCtxt)) : 0));
        }
        if ((ctxt == 0 ||
             getMessageType(new XClientMessageEvent(ctxt)) == UNKNOWN_MESSAGE) &&
            (prevCtxt == 0 ||
             getMessageType(new XClientMessageEvent(prevCtxt)) == UNKNOWN_MESSAGE)) {
            return false;
        }
        int size = XClientMessageEvent.getSize();
        if (ctxt != 0) {
            XClientMessageEvent xclient = new XClientMessageEvent(ctxt);
            if (!overXEmbedClient) {
                long data1 = Native.getLong(ctxt + size);
                long data2 = Native.getLong(ctxt + size + Native.getLongSize());
                long data3 = Native.getLong(ctxt + size + 2 * Native.getLongSize());
                long data4 = Native.getLong(ctxt + size + 3 * Native.getLongSize());
                if (logger.isLoggable(PlatformLogger.FINEST)) {
                    logger.finest("         1 "
                                  + " embedded=" + embedded
                                  + " source=" + xclient.get_data(0)
                                  + " data1=" + data1
                                  + " data2=" + data2
                                  + " data3=" + data3
                                  + " data4=" + data4);
                }
                if ((data1 & XDnDConstants.XDND_DATA_TYPES_BIT) != 0) {
                    WindowPropertyGetter wpg =
                        new WindowPropertyGetter(xclient.get_data(0),
                                                 XDnDConstants.XA_XdndTypeList,
                                                 0, 0xFFFF, false,
                                                 XAtom.XA_ATOM);
                    try {
                        wpg.execute(XErrorHandler.IgnoreBadWindowHandler.getInstance());
                        if (wpg.getActualType() == XAtom.XA_ATOM &&
                            wpg.getActualFormat() == 32) {
                            XToolkit.awtLock();
                            try {
                                XToolkit.WITH_XERROR_HANDLER(XErrorHandler.VerifyChangePropertyHandler.getInstance());
                                XDnDConstants.XA_XdndTypeList.setAtomData(xclient.get_window(),
                                                                          XAtom.XA_ATOM,
                                                                          wpg.getData(),
                                                                          wpg.getNumberOfItems());
                                XToolkit.RESTORE_XERROR_HANDLER();
                                if (XToolkit.saved_error != null &&
                                    XToolkit.saved_error.get_error_code() != XConstants.Success) {
                                    if (logger.isLoggable(PlatformLogger.WARNING)) {
                                        logger.warning("Cannot set XdndTypeList on the proxy window");
                                    }
                                }
                            } finally {
                                XToolkit.awtUnlock();
                            }
                        } else {
                            if (logger.isLoggable(PlatformLogger.WARNING)) {
                                logger.warning("Cannot read XdndTypeList from the source window");
                            }
                        }
                    } finally {
                        wpg.dispose();
                    }
                }
                XDragSourceContextPeer.setProxyModeSourceWindow(xclient.get_data(0));
                sendEnterMessageToToplevelImpl(embedded, xclient.get_window(),
                                               data1, data2, data3, data4);
                overXEmbedClient = true;
            }
            if (logger.isLoggable(PlatformLogger.FINEST)) {
                logger.finest("         2 "
                              + " embedded=" + embedded
                              + " xclient=" + xclient);
            }
            {
                XClientMessageEvent copy = new XClientMessageEvent();
                unsafe.copyMemory(xclient.pData, copy.pData, copy.getSize());
                copy.set_data(0, xclient.get_window());
                forwardClientMessageToToplevel(embedded, copy);
            }
        }
        if (eventID == MouseEvent.MOUSE_EXITED) {
            if (overXEmbedClient) {
                if (ctxt != 0 || prevCtxt != 0) {
                    XClientMessageEvent xclient = ctxt != 0 ?
                        new XClientMessageEvent(ctxt) :
                        new XClientMessageEvent(prevCtxt);
                    sendLeaveMessageToToplevelImpl(embedded, xclient.get_window());
                }
                overXEmbedClient = false;
                XDragSourceContextPeer.setProxyModeSourceWindow(0);
            }
        }
        if (eventID == MouseEvent.MOUSE_RELEASED) {
            overXEmbedClient = false;
            cleanup();
        }
        if (prevCtxt != 0) {
            unsafe.freeMemory(prevCtxt);
            prevCtxt = 0;
        }
        if (ctxt != 0 && overXEmbedClient) {
            prevCtxt = unsafe.allocateMemory(size + 4 * Native.getLongSize());
            unsafe.copyMemory(ctxt, prevCtxt, size + 4 * Native.getLongSize());
        }
        return true;
    }
    public boolean isXEmbedSupported() {
        return true;
    }
}
