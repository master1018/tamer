class XDnDDragSourceProtocol extends XDragSourceProtocol {
    private static final PlatformLogger logger =
        PlatformLogger.getLogger("sun.awt.X11.xembed.xdnd.XDnDDragSourceProtocol");
    private static final Unsafe unsafe = XlibWrapper.unsafe;
    protected XDnDDragSourceProtocol(XDragSourceProtocolListener listener) {
        super(listener);
    }
    static XDragSourceProtocol createInstance(XDragSourceProtocolListener listener) {
        return new XDnDDragSourceProtocol(listener);
    }
    public String getProtocolName() {
        return XDragAndDropProtocols.XDnD;
    }
    protected void initializeDragImpl(int actions, Transferable contents,
                                         Map formatMap, long[] formats)
      throws InvalidDnDOperationException,
        IllegalArgumentException, XException {
        assert XToolkit.isAWTLockHeldByCurrentThread();
        long window = XDragSourceProtocol.getDragSourceWindow();
        long data = Native.allocateLongArray(3);
        int action_count = 0;
        try {
            if ((actions & DnDConstants.ACTION_COPY) != 0) {
                Native.putLong(data, action_count,
                               XDnDConstants.XA_XdndActionCopy.getAtom());
                action_count++;
            }
            if ((actions & DnDConstants.ACTION_MOVE) != 0) {
                Native.putLong(data, action_count,
                               XDnDConstants.XA_XdndActionMove.getAtom());
                action_count++;
            }
            if ((actions & DnDConstants.ACTION_LINK) != 0) {
                Native.putLong(data, action_count,
                               XDnDConstants.XA_XdndActionLink.getAtom());
                action_count++;
            }
            XToolkit.WITH_XERROR_HANDLER(XErrorHandler.VerifyChangePropertyHandler.getInstance());
            XDnDConstants.XA_XdndActionList.setAtomData(window,
                                                        XAtom.XA_ATOM,
                                                        data, action_count);
            XToolkit.RESTORE_XERROR_HANDLER();
            if (XToolkit.saved_error != null &&
                XToolkit.saved_error.get_error_code() != XConstants.Success) {
                cleanup();
                throw new XException("Cannot write XdndActionList property");
            }
        } finally {
            unsafe.freeMemory(data);
            data = 0;
        }
        data = Native.allocateLongArray(formats.length);
        try {
            Native.put(data, formats);
            XToolkit.WITH_XERROR_HANDLER(XErrorHandler.VerifyChangePropertyHandler.getInstance());
            XDnDConstants.XA_XdndTypeList.setAtomData(window,
                                                      XAtom.XA_ATOM,
                                                      data, formats.length);
            XToolkit.RESTORE_XERROR_HANDLER();
            if (XToolkit.saved_error != null &&
                XToolkit.saved_error.get_error_code() != XConstants.Success) {
                cleanup();
                throw new XException("Cannot write XdndActionList property");
            }
        } finally {
            unsafe.freeMemory(data);
            data = 0;
        }
        if (!XDnDConstants.XDnDSelection.setOwner(contents, formatMap, formats,
                                                  XConstants.CurrentTime)) {
            cleanup();
            throw new InvalidDnDOperationException("Cannot acquire selection ownership");
        }
    }
    private boolean processXdndStatus(XClientMessageEvent xclient) {
        int action = DnDConstants.ACTION_NONE;
        if (xclient.get_data(0) != getTargetWindow()) {
            return true;
        }
        if ((xclient.get_data(1) & XDnDConstants.XDND_ACCEPT_DROP_FLAG) != 0) {
            action = XDnDConstants.getJavaActionForXDnDAction(xclient.get_data(4));
        }
        getProtocolListener().handleDragReply(action);
        return true;
    }
    private boolean processXdndFinished(XClientMessageEvent xclient) {
        if (xclient.get_data(0) != getTargetWindow()) {
            return true;
        }
        if (getTargetProtocolVersion() >= 5) {
            boolean success = (xclient.get_data(1) & XDnDConstants.XDND_ACCEPT_DROP_FLAG) != 0;
            int action = XDnDConstants.getJavaActionForXDnDAction(xclient.get_data(2));
            getProtocolListener().handleDragFinished(success, action);
        } else {
            getProtocolListener().handleDragFinished();
        }
        finalizeDrop();
        return true;
    }
    public boolean processClientMessage(XClientMessageEvent xclient) {
        if (xclient.get_message_type() == XDnDConstants.XA_XdndStatus.getAtom()) {
            return processXdndStatus(xclient);
        } else if (xclient.get_message_type() == XDnDConstants.XA_XdndFinished.getAtom()) {
            return processXdndFinished(xclient);
        } else {
            return false;
        }
    }
    public TargetWindowInfo getTargetWindowInfo(long window) {
        assert XToolkit.isAWTLockHeldByCurrentThread();
        WindowPropertyGetter wpg1 =
            new WindowPropertyGetter(window, XDnDConstants.XA_XdndAware, 0, 1,
                                     false, XConstants.AnyPropertyType);
        int status = wpg1.execute(XErrorHandler.IgnoreBadWindowHandler.getInstance());
        if (status == XConstants.Success &&
            wpg1.getData() != 0 && wpg1.getActualType() == XAtom.XA_ATOM) {
            int targetVersion = (int)Native.getLong(wpg1.getData());
            wpg1.dispose();
            if (targetVersion >= XDnDConstants.XDND_MIN_PROTOCOL_VERSION) {
                long proxy = 0;
                int protocolVersion =
                    targetVersion < XDnDConstants.XDND_PROTOCOL_VERSION ?
                    targetVersion : XDnDConstants.XDND_PROTOCOL_VERSION;
                WindowPropertyGetter wpg2 =
                    new WindowPropertyGetter(window, XDnDConstants.XA_XdndProxy,
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
                return new TargetWindowInfo(proxy, protocolVersion);
            }
        } else {
            wpg1.dispose();
        }
        return null;
    }
    public void sendEnterMessage(long[] formats,
                                 int sourceAction, int sourceActions, long time) {
        assert XToolkit.isAWTLockHeldByCurrentThread();
        assert getTargetWindow() != 0;
        assert formats != null;
        XClientMessageEvent msg = new XClientMessageEvent();
        try {
            msg.set_type((int)XConstants.ClientMessage);
            msg.set_window(getTargetWindow());
            msg.set_format(32);
            msg.set_message_type(XDnDConstants.XA_XdndEnter.getAtom());
            msg.set_data(0, XDragSourceProtocol.getDragSourceWindow());
            long data1 =
                getTargetProtocolVersion() << XDnDConstants.XDND_PROTOCOL_SHIFT;
            data1 |= formats.length > 3 ? XDnDConstants.XDND_DATA_TYPES_BIT : 0;
            msg.set_data(1, data1);
            msg.set_data(2, formats.length > 0 ? formats[0] : 0);
            msg.set_data(3, formats.length > 1 ? formats[1] : 0);
            msg.set_data(4, formats.length > 2 ? formats[2] : 0);
            XlibWrapper.XSendEvent(XToolkit.getDisplay(),
                                   getTargetProxyWindow(),
                                   false, XConstants.NoEventMask,
                                   msg.pData);
        } finally {
            msg.dispose();
        }
    }
    public void sendMoveMessage(int xRoot, int yRoot,
                                int sourceAction, int sourceActions, long time) {
        assert XToolkit.isAWTLockHeldByCurrentThread();
        assert getTargetWindow() != 0;
        XClientMessageEvent msg = new XClientMessageEvent();
        try {
            msg.set_type((int)XConstants.ClientMessage);
            msg.set_window(getTargetWindow());
            msg.set_format(32);
            msg.set_message_type(XDnDConstants.XA_XdndPosition.getAtom());
            msg.set_data(0, XDragSourceProtocol.getDragSourceWindow());
            msg.set_data(1, 0); 
            msg.set_data(2, xRoot << 16 | yRoot);
            msg.set_data(3, time);
            msg.set_data(4, XDnDConstants.getXDnDActionForJavaAction(sourceAction));
            XlibWrapper.XSendEvent(XToolkit.getDisplay(),
                                   getTargetProxyWindow(),
                                   false, XConstants.NoEventMask,
                                   msg.pData);
        } finally {
            msg.dispose();
        }
    }
    public void sendLeaveMessage(long time) {
        assert XToolkit.isAWTLockHeldByCurrentThread();
        assert getTargetWindow() != 0;
        XClientMessageEvent msg = new XClientMessageEvent();
        try {
            msg.set_type((int)XConstants.ClientMessage);
            msg.set_window(getTargetWindow());
            msg.set_format(32);
            msg.set_message_type(XDnDConstants.XA_XdndLeave.getAtom());
            msg.set_data(0, XDragSourceProtocol.getDragSourceWindow());
            msg.set_data(1, 0);
            msg.set_data(2, 0);
            msg.set_data(3, 0);
            msg.set_data(4, 0);
            XlibWrapper.XSendEvent(XToolkit.getDisplay(),
                                   getTargetProxyWindow(),
                                   false, XConstants.NoEventMask,
                                   msg.pData);
        } finally {
            msg.dispose();
        }
    }
    public void sendDropMessage(int xRoot, int yRoot,
                                int sourceAction, int sourceActions,
                                long time) {
        assert XToolkit.isAWTLockHeldByCurrentThread();
        assert getTargetWindow() != 0;
        XClientMessageEvent msg = new XClientMessageEvent();
        try {
            msg.set_type((int)XConstants.ClientMessage);
            msg.set_window(getTargetWindow());
            msg.set_format(32);
            msg.set_message_type(XDnDConstants.XA_XdndDrop.getAtom());
            msg.set_data(0, XDragSourceProtocol.getDragSourceWindow());
            msg.set_data(1, 0); 
            msg.set_data(2, time);
            msg.set_data(3, 0);
            msg.set_data(4, 0);
            XlibWrapper.XSendEvent(XToolkit.getDisplay(),
                                   getTargetProxyWindow(),
                                   false, XConstants.NoEventMask,
                                   msg.pData);
        } finally {
            msg.dispose();
        }
    }
    public boolean processProxyModeEvent(XClientMessageEvent xclient,
                                         long sourceWindow) {
        if (xclient.get_message_type() == XDnDConstants.XA_XdndStatus.getAtom() ||
            xclient.get_message_type() == XDnDConstants.XA_XdndFinished.getAtom()) {
            if (xclient.get_message_type() == XDnDConstants.XA_XdndFinished.getAtom()) {
                XDragSourceContextPeer.setProxyModeSourceWindow(0);
            }
            if (xclient.get_window() == sourceWindow) {
                return false;
            }
            if (logger.isLoggable(PlatformLogger.FINEST)) {
                logger.finest("        sourceWindow=" + sourceWindow +
                              " get_window=" + xclient.get_window() +
                              " xclient=" + xclient);
            }
            xclient.set_data(0, xclient.get_window());
            xclient.set_window(sourceWindow);
            assert XToolkit.isAWTLockHeldByCurrentThread();
            XlibWrapper.XSendEvent(XToolkit.getDisplay(), sourceWindow,
                                   false, XConstants.NoEventMask,
                                   xclient.pData);
            return true;
        }
        return false;
    }
    public void run() {
        cleanup();
    }
}
