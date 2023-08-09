public final class XDragSourceContextPeer
    extends SunDragSourceContextPeer implements XDragSourceProtocolListener {
    private static final PlatformLogger logger =
        PlatformLogger.getLogger("sun.awt.X11.xembed.xdnd.XDragSourceContextPeer");
    private static final int ROOT_EVENT_MASK = (int)XConstants.ButtonMotionMask |
        (int)XConstants.KeyPressMask | (int)XConstants.KeyReleaseMask;
    private static final int GRAB_EVENT_MASK = (int)XConstants.ButtonPressMask |
        (int)XConstants.ButtonMotionMask | (int)XConstants.ButtonReleaseMask;
    private long rootEventMask = 0;
    private boolean dndInProgress = false;
    private boolean dragInProgress = false;
    private long dragRootWindow = 0;
    private XDragSourceProtocol dragProtocol = null;
    private int targetAction = DnDConstants.ACTION_NONE;
    private int sourceActions = DnDConstants.ACTION_NONE;
    private int sourceAction = DnDConstants.ACTION_NONE;
    private long[] sourceFormats = null;
    private long targetRootSubwindow = 0;
    private int xRoot = 0;
    private int yRoot = 0;
    private int eventState = 0;
    private long proxyModeSourceWindow = 0;
    private static final XDragSourceContextPeer theInstance =
        new XDragSourceContextPeer(null);
    private XDragSourceContextPeer(DragGestureEvent dge) {
        super(dge);
    }
    static XDragSourceProtocolListener getXDragSourceProtocolListener() {
        return theInstance;
    }
    static XDragSourceContextPeer createDragSourceContextPeer(DragGestureEvent dge)
      throws InvalidDnDOperationException {
    theInstance.setTrigger(dge);
        return theInstance;
    }
    protected void startDrag(Transferable transferable,
                             long[] formats, Map formatMap) {
        Component component = getTrigger().getComponent();
        Component c = null;
        XWindowPeer wpeer = null;
        for (c = component; c != null && !(c instanceof Window);
             c = AWTAccessor.getComponentAccessor().getParent(c));
        if (c instanceof Window) {
            wpeer = (XWindowPeer)c.getPeer();
        }
        if (wpeer == null) {
            throw new InvalidDnDOperationException(
                "Cannot find top-level for the drag source component");
        }
        long xcursor = 0;
        long rootWindow = 0;
        long dragWindow = 0;
        long timeStamp = 0;
        {
            Cursor cursor = getCursor();
            if (cursor != null) {
                xcursor = XGlobalCursorManager.getCursor(cursor);
            }
        }
        XToolkit.awtLock();
        try {
            if (proxyModeSourceWindow != 0) {
                throw new InvalidDnDOperationException("Proxy drag in progress");
            }
            if (dndInProgress) {
                throw new InvalidDnDOperationException("Drag in progress");
            }
            {
                long screen = XlibWrapper.XScreenNumberOfScreen(wpeer.getScreen());
                rootWindow = XlibWrapper.RootWindow(XToolkit.getDisplay(), screen);
            }
            dragWindow = XWindow.getXAWTRootWindow().getWindow();
            timeStamp = XToolkit.getCurrentServerTime();
            int dropActions = getDragSourceContext().getSourceActions();
            Iterator dragProtocols = XDragAndDropProtocols.getDragSourceProtocols();
            while (dragProtocols.hasNext()) {
                XDragSourceProtocol dragProtocol = (XDragSourceProtocol)dragProtocols.next();
                try {
                    dragProtocol.initializeDrag(dropActions, transferable,
                                                formatMap, formats);
                } catch (XException xe) {
                    throw (InvalidDnDOperationException)
                        new InvalidDnDOperationException().initCause(xe);
                }
            }
            {
                int status;
                XWindowAttributes wattr = new XWindowAttributes();
                try {
                    status = XlibWrapper.XGetWindowAttributes(XToolkit.getDisplay(),
                                                              rootWindow, wattr.pData);
                    if (status == 0) {
                        throw new InvalidDnDOperationException("XGetWindowAttributes failed");
                    }
                    rootEventMask = wattr.get_your_event_mask();
                    XlibWrapper.XSelectInput(XToolkit.getDisplay(), rootWindow,
                                             rootEventMask | ROOT_EVENT_MASK);
                } finally {
                    wattr.dispose();
                }
                XBaseWindow.ungrabInput();
                status = XlibWrapper.XGrabPointer(XToolkit.getDisplay(), rootWindow,
                                                  0, GRAB_EVENT_MASK,
                                                  XConstants.GrabModeAsync,
                                                  XConstants.GrabModeAsync,
                                                  XConstants.None, xcursor, timeStamp);
                if (status != XConstants.GrabSuccess) {
                    cleanup(timeStamp);
                    throwGrabFailureException("Cannot grab pointer", status);
                    return;
                }
                status = XlibWrapper.XGrabKeyboard(XToolkit.getDisplay(), rootWindow,
                                                   0,
                                                   XConstants.GrabModeAsync,
                                                   XConstants.GrabModeAsync,
                                                   timeStamp);
                if (status != XConstants.GrabSuccess) {
                    cleanup(timeStamp);
                    throwGrabFailureException("Cannot grab keyboard", status);
                    return;
                }
            }
            dndInProgress = true;
            dragInProgress = true;
            dragRootWindow = rootWindow;
            sourceActions = dropActions;
            sourceFormats = formats;
        } finally {
            XToolkit.awtUnlock();
        }
        setNativeContext(0);
        SunDropTargetContextPeer.setCurrentJVMLocalSourceTransferable(transferable);
    }
    public long getProxyModeSourceWindow() {
        return proxyModeSourceWindow;
    }
    private void setProxyModeSourceWindowImpl(long window) {
        proxyModeSourceWindow = window;
    }
    public static void setProxyModeSourceWindow(long window) {
        theInstance.setProxyModeSourceWindowImpl(window);
    }
    public void setCursor(Cursor c) throws InvalidDnDOperationException {
        XToolkit.awtLock();
        try {
            super.setCursor(c);
        } finally {
            XToolkit.awtUnlock();
        }
    }
    protected void setNativeCursor(long nativeCtxt, Cursor c, int cType) {
        assert XToolkit.isAWTLockHeldByCurrentThread();
        if (c == null) {
            return;
        }
        long xcursor = XGlobalCursorManager.getCursor(c);
        if (xcursor == 0) {
            return;
        }
        XlibWrapper.XChangeActivePointerGrab(XToolkit.getDisplay(),
                                             GRAB_EVENT_MASK,
                                             xcursor,
                                             XConstants.CurrentTime);
    }
    protected boolean needsBogusExitBeforeDrop() {
        return false;
    }
    private void throwGrabFailureException(String msg, int grabStatus)
      throws InvalidDnDOperationException {
        String msgCause = "";
        switch (grabStatus) {
        case XConstants.GrabNotViewable:  msgCause = "not viewable";    break;
        case XConstants.AlreadyGrabbed:   msgCause = "already grabbed"; break;
        case XConstants.GrabInvalidTime:  msgCause = "invalid time";    break;
        case XConstants.GrabFrozen:       msgCause = "grab frozen";     break;
        default:                           msgCause = "unknown failure"; break;
        }
        throw new InvalidDnDOperationException(msg + ": " + msgCause);
    }
    public void cleanup(long time) {
        if (dndInProgress) {
            if (dragProtocol != null) {
                dragProtocol.sendLeaveMessage(time);
            }
            if (targetAction != DnDConstants.ACTION_NONE) {
                dragExit(xRoot, yRoot);
            }
            dragDropFinished(false, DnDConstants.ACTION_NONE, xRoot, yRoot);
        }
        Iterator dragProtocols = XDragAndDropProtocols.getDragSourceProtocols();
        while (dragProtocols.hasNext()) {
            XDragSourceProtocol dragProtocol = (XDragSourceProtocol)dragProtocols.next();
            try {
                dragProtocol.cleanup();
            } catch (XException xe) {
            }
        }
        dndInProgress = false;
        dragInProgress = false;
        dragRootWindow = 0;
        sourceFormats = null;
        sourceActions = DnDConstants.ACTION_NONE;
        sourceAction = DnDConstants.ACTION_NONE;
        eventState = 0;
        xRoot = 0;
        yRoot = 0;
        cleanupTargetInfo();
        removeDnDGrab(time);
    }
    private void cleanupTargetInfo() {
        targetAction = DnDConstants.ACTION_NONE;
        dragProtocol = null;
        targetRootSubwindow = 0;
    }
    private void removeDnDGrab(long time) {
        assert XToolkit.isAWTLockHeldByCurrentThread();
        XlibWrapper.XUngrabPointer(XToolkit.getDisplay(), time);
        XlibWrapper.XUngrabKeyboard(XToolkit.getDisplay(), time);
        if ((rootEventMask | ROOT_EVENT_MASK) != rootEventMask &&
            dragRootWindow != 0) {
            XlibWrapper.XSelectInput(XToolkit.getDisplay(),
                                     dragRootWindow,
                                     rootEventMask);
        }
        rootEventMask = 0;
        dragRootWindow = 0;
    }
    private boolean processClientMessage(XClientMessageEvent xclient) {
        if (dragProtocol != null) {
            return dragProtocol.processClientMessage(xclient);
        }
        return false;
    }
    private boolean updateSourceAction(int state) {
        int action = SunDragSourceContextPeer.convertModifiersToDropAction(XWindow.getModifiers(state, 0, 0),
                                                                           sourceActions);
        if (sourceAction == action) {
            return false;
        }
        sourceAction = action;
        return true;
    }
    private static long findClientWindow(long window) {
        if (XlibUtil.isTrueToplevelWindow(window)) {
            return window;
        }
        Set<Long> children = XlibUtil.getChildWindows(window);
        for (Long child : children) {
            long win = findClientWindow(child);
            if (win != 0) {
                return win;
            }
        }
        return 0;
    }
    private void doUpdateTargetWindow(long subwindow, long time) {
        long clientWindow = 0;
        long proxyWindow = 0;
        XDragSourceProtocol protocol = null;
        boolean isReceiver = false;
        if (subwindow != 0) {
            clientWindow = findClientWindow(subwindow);
        }
        if (clientWindow != 0) {
            Iterator dragProtocols = XDragAndDropProtocols.getDragSourceProtocols();
            while (dragProtocols.hasNext()) {
                XDragSourceProtocol dragProtocol = (XDragSourceProtocol)dragProtocols.next();
                if (dragProtocol.attachTargetWindow(clientWindow, time)) {
                    protocol = dragProtocol;
                    break;
                }
            }
        }
        dragProtocol = protocol;
        targetAction = DnDConstants.ACTION_NONE;
        targetRootSubwindow = subwindow;
    }
    private void updateTargetWindow(XMotionEvent xmotion) {
        assert XToolkit.isAWTLockHeldByCurrentThread();
        int x = xmotion.get_x_root();
        int y = xmotion.get_y_root();
        long time = xmotion.get_time();
        long subwindow = xmotion.get_subwindow();
        if (xmotion.get_window() != xmotion.get_root()) {
            XlibWrapper.XQueryPointer(XToolkit.getDisplay(),
                                      xmotion.get_root(),
                                      XlibWrapper.larg1,  
                                      XlibWrapper.larg2,  
                                      XlibWrapper.larg3,  
                                      XlibWrapper.larg4,  
                                      XlibWrapper.larg5,  
                                      XlibWrapper.larg6,  
                                      XlibWrapper.larg7); 
            subwindow = Native.getLong(XlibWrapper.larg2);
        }
        if (targetRootSubwindow != subwindow) {
            if (dragProtocol != null) {
                dragProtocol.sendLeaveMessage(time);
                if (targetAction != DnDConstants.ACTION_NONE) {
                    dragExit(x, y);
                }
            }
            doUpdateTargetWindow(subwindow, time);
            if (dragProtocol != null) {
                dragProtocol.sendEnterMessage(sourceFormats,
                                              sourceAction,
                                              sourceActions,
                                              time);
            }
        }
    }
    private void processMouseMove(XMotionEvent xmotion) {
        if (!dragInProgress) {
            return;
        }
        if (xRoot != xmotion.get_x_root() || yRoot != xmotion.get_y_root()) {
            xRoot = xmotion.get_x_root();
            yRoot = xmotion.get_y_root();
            postDragSourceDragEvent(targetAction,
                                    XWindow.getModifiers(xmotion.get_state(),0,0),
                                    xRoot, yRoot, DISPATCH_MOUSE_MOVED);
        }
        if (eventState != xmotion.get_state()) {
            if (updateSourceAction(xmotion.get_state()) && dragProtocol != null) {
                postDragSourceDragEvent(targetAction,
                                        XWindow.getModifiers(xmotion.get_state(),0,0),
                                        xRoot, yRoot, DISPATCH_CHANGED);
            }
            eventState = xmotion.get_state();
        }
        updateTargetWindow(xmotion);
        if (dragProtocol != null) {
            dragProtocol.sendMoveMessage(xmotion.get_x_root(),
                                         xmotion.get_y_root(),
                                         sourceAction, sourceActions,
                                         xmotion.get_time());
        }
    }
    private void processDrop(XButtonEvent xbutton) {
        try {
            dragProtocol.initiateDrop(xbutton.get_x_root(),
                                      xbutton.get_y_root(),
                                      sourceAction, sourceActions,
                                      xbutton.get_time());
        } catch (XException e) {
            cleanup(xbutton.get_time());
        }
    }
    private boolean processProxyModeEvent(XEvent ev) {
        if (getProxyModeSourceWindow() == 0) {
            return false;
        }
        if (ev.get_type() != (int)XConstants.ClientMessage) {
            return false;
        }
        if (logger.isLoggable(PlatformLogger.FINEST)) {
            logger.finest("        proxyModeSourceWindow=" +
                          getProxyModeSourceWindow() +
                          " ev=" + ev);
        }
        XClientMessageEvent xclient = ev.get_xclient();
        Iterator dragProtocols = XDragAndDropProtocols.getDragSourceProtocols();
        while (dragProtocols.hasNext()) {
            XDragSourceProtocol dragProtocol =
                (XDragSourceProtocol)dragProtocols.next();
            if (dragProtocol.processProxyModeEvent(xclient,
                                                   getProxyModeSourceWindow())) {
                return true;
            }
        }
        return false;
    }
    private boolean doProcessEvent(XEvent ev) {
        assert XToolkit.isAWTLockHeldByCurrentThread();
        if (processProxyModeEvent(ev)) {
            return true;
        }
        if (!dndInProgress) {
            return false;
        }
        switch (ev.get_type()) {
        case XConstants.ClientMessage: {
            XClientMessageEvent xclient = ev.get_xclient();
            return processClientMessage(xclient);
        }
        case XConstants.DestroyNotify: {
            XDestroyWindowEvent xde = ev.get_xdestroywindow();
            if (!dragInProgress &&
                dragProtocol != null &&
                xde.get_window() == dragProtocol.getTargetWindow()) {
                cleanup(XConstants.CurrentTime);
                return true;
            }
            return false;
        }
        }
        if (!dragInProgress) {
            return false;
        }
        switch (ev.get_type()) {
        case XConstants.KeyRelease:
        case XConstants.KeyPress: {
            XKeyEvent xkey = ev.get_xkey();
            long keysym = XlibWrapper.XKeycodeToKeysym(XToolkit.getDisplay(),
                                                       xkey.get_keycode(), 0);
            switch ((int)keysym) {
            case (int)XKeySymConstants.XK_Escape: {
                if (ev.get_type() == (int)XConstants.KeyRelease) {
                    cleanup(xkey.get_time());
                }
                break;
            }
            case (int)XKeySymConstants.XK_Control_R:
            case (int)XKeySymConstants.XK_Control_L:
            case (int)XKeySymConstants.XK_Shift_R:
            case (int)XKeySymConstants.XK_Shift_L: {
                XlibWrapper.XQueryPointer(XToolkit.getDisplay(),
                                          xkey.get_root(),
                                          XlibWrapper.larg1,  
                                          XlibWrapper.larg2,  
                                          XlibWrapper.larg3,  
                                          XlibWrapper.larg4,  
                                          XlibWrapper.larg5,  
                                          XlibWrapper.larg6,  
                                          XlibWrapper.larg7); 
                XMotionEvent xmotion = new XMotionEvent();
                try {
                    xmotion.set_type(XConstants.MotionNotify);
                    xmotion.set_serial(xkey.get_serial());
                    xmotion.set_send_event(xkey.get_send_event());
                    xmotion.set_display(xkey.get_display());
                    xmotion.set_window(xkey.get_window());
                    xmotion.set_root(xkey.get_root());
                    xmotion.set_subwindow(xkey.get_subwindow());
                    xmotion.set_time(xkey.get_time());
                    xmotion.set_x(xkey.get_x());
                    xmotion.set_y(xkey.get_y());
                    xmotion.set_x_root(xkey.get_x_root());
                    xmotion.set_y_root(xkey.get_y_root());
                    xmotion.set_state((int)Native.getLong(XlibWrapper.larg7));
                    xmotion.set_same_screen(xkey.get_same_screen());
                    processMouseMove(xmotion);
                } finally {
                    xmotion.dispose();
                }
                break;
            }
            }
            return true;
        }
        case XConstants.ButtonPress:
            return true;
        case XConstants.MotionNotify:
            processMouseMove(ev.get_xmotion());
            return true;
        case XConstants.ButtonRelease: {
            XButtonEvent xbutton = ev.get_xbutton();
            if (xbutton.get_button() > SunToolkit.MAX_BUTTONS_SUPPORTED) {
                return true;
            }
            XMotionEvent xmotion = new XMotionEvent();
            try {
                xmotion.set_type(XConstants.MotionNotify);
                xmotion.set_serial(xbutton.get_serial());
                xmotion.set_send_event(xbutton.get_send_event());
                xmotion.set_display(xbutton.get_display());
                xmotion.set_window(xbutton.get_window());
                xmotion.set_root(xbutton.get_root());
                xmotion.set_subwindow(xbutton.get_subwindow());
                xmotion.set_time(xbutton.get_time());
                xmotion.set_x(xbutton.get_x());
                xmotion.set_y(xbutton.get_y());
                xmotion.set_x_root(xbutton.get_x_root());
                xmotion.set_y_root(xbutton.get_y_root());
                xmotion.set_state(xbutton.get_state());
                xmotion.set_same_screen(xbutton.get_same_screen());
                processMouseMove(xmotion);
            } finally {
                xmotion.dispose();
            }
            if (xbutton.get_button() == XConstants.buttons[0]
                || xbutton.get_button() == XConstants.buttons[1]) {
                removeDnDGrab(xbutton.get_time());
                dragInProgress = false;
                if (dragProtocol != null && targetAction != DnDConstants.ACTION_NONE) {
                    processDrop(xbutton);
                } else {
                    cleanup(xbutton.get_time());
                }
            }
            return true;
        }
        }
        return false;
    }
    static boolean processEvent(XEvent ev) {
        XToolkit.awtLock();
        try {
            try {
                return theInstance.doProcessEvent(ev);
            } catch (XException e) {
                e.printStackTrace();
                return false;
            }
        } finally {
            XToolkit.awtUnlock();
        }
    }
    public void handleDragReply(int action) {
        handleDragReply(action, xRoot, yRoot);
    }
    public void handleDragReply(int action, int x, int y) {
        handleDragReply(action, xRoot, yRoot, XWindow.getModifiers(eventState,0,0));
    }
    public void handleDragReply(int action, int x, int y, int modifiers) {
        if (action == DnDConstants.ACTION_NONE &&
            targetAction != DnDConstants.ACTION_NONE) {
            dragExit(x, y);
        } else if (action != DnDConstants.ACTION_NONE) {
            int type = 0;
            if (targetAction == DnDConstants.ACTION_NONE) {
                type = SunDragSourceContextPeer.DISPATCH_ENTER;
            } else {
                type = SunDragSourceContextPeer.DISPATCH_MOTION;
            }
            postDragSourceDragEvent(action, modifiers, x, y, type);
        }
        targetAction = action;
    }
    public void handleDragFinished() {
        handleDragFinished(true);
    }
    public void handleDragFinished(boolean success) {
        handleDragFinished(true, targetAction);
    }
    public void handleDragFinished(boolean success, int action) {
        handleDragFinished(success, action, xRoot, yRoot);
    }
    public void handleDragFinished(boolean success, int action, int x, int y) {
        dragDropFinished(success, action, x, y);
        dndInProgress = false;
        cleanup(XConstants.CurrentTime);
    }
}
