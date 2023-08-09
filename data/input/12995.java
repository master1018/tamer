class XWINProtocol extends XProtocol implements XStateProtocol, XLayerProtocol {
    final static PlatformLogger log = PlatformLogger.getLogger("sun.awt.X11.XWINProtocol");
    XAtom XA_WIN_SUPPORTING_WM_CHECK = XAtom.get("_WIN_SUPPORTING_WM_CHECK");
    XAtom XA_WIN_PROTOCOLS = XAtom.get("_WIN_PROTOCOLS");
    XAtom XA_WIN_STATE = XAtom.get("_WIN_STATE");
    public boolean supportsState(int state) {
        return doStateProtocol();   
    }
    public void setState(XWindowPeer window, int state) {
        if (window.isShowing()) {
            long win_state = 0;
            if ( (state & Frame.MAXIMIZED_VERT) != 0) {
                win_state |= WIN_STATE_MAXIMIZED_VERT;
            }
            if ( (state & Frame.MAXIMIZED_HORIZ) != 0) {
                win_state |= WIN_STATE_MAXIMIZED_HORIZ;
            }
            XClientMessageEvent req = new XClientMessageEvent();
            req.set_type(XConstants.ClientMessage);
            req.set_window(window.getWindow());
            req.set_message_type(XA_WIN_STATE.getAtom());
            req.set_format(32);
            req.set_data(0, (WIN_STATE_MAXIMIZED_HORIZ | WIN_STATE_MAXIMIZED_VERT));
            req.set_data(1, win_state);
            if (log.isLoggable(PlatformLogger.FINE)) log.fine("Sending WIN_STATE to root to change the state to " + win_state);
            try {
                XToolkit.awtLock();
                XlibWrapper.XSendEvent(XToolkit.getDisplay(),
                        XlibWrapper.RootWindow(XToolkit.getDisplay(),
                            window.getScreenNumber()),
                        false,
                        XConstants.SubstructureRedirectMask | XConstants.SubstructureNotifyMask,
                        req.pData);
            }
            finally {
                XToolkit.awtUnlock();
            }
            req.dispose();
        } else {
            long win_state = XA_WIN_STATE.getCard32Property(window);
            long old_win_state = win_state;
            if ((state & Frame.ICONIFIED) != 0) {
                win_state |= WIN_STATE_MINIMIZED;
            } else {
                win_state &= ~WIN_STATE_MINIMIZED;
            }
            if ((state & Frame.MAXIMIZED_VERT) != 0) {
                win_state |= WIN_STATE_MAXIMIZED_VERT;
            } else {
                win_state &= ~WIN_STATE_MAXIMIZED_VERT;
            }
            if ((state & Frame.MAXIMIZED_HORIZ) != 0) {
                win_state |= WIN_STATE_MAXIMIZED_HORIZ;
            } else {
                win_state &= ~WIN_STATE_MAXIMIZED_HORIZ;
            }
            if ((old_win_state ^ win_state) != 0) {
                if (log.isLoggable(PlatformLogger.FINE)) log.fine("Setting WIN_STATE on " + window + " to change the state to " + win_state);
                XA_WIN_STATE.setCard32Property(window, win_state);
            }
        }
    }
    public int getState(XWindowPeer window) {
        long win_state = XA_WIN_STATE.getCard32Property(window);
        int java_state = Frame.NORMAL;
        if ((win_state & WIN_STATE_MAXIMIZED_VERT) != 0) {
            java_state |= Frame.MAXIMIZED_VERT;
        }
        if ((win_state & WIN_STATE_MAXIMIZED_HORIZ) != 0) {
            java_state |= Frame.MAXIMIZED_HORIZ;
        }
        return java_state;
    }
    public boolean isStateChange(XPropertyEvent e) {
        return doStateProtocol() && e.get_atom() == XA_WIN_STATE.getAtom();
    }
    public void unshadeKludge(XWindowPeer window) {
        long win_state = XA_WIN_STATE.getCard32Property(window);
        if ((win_state & WIN_STATE_SHADED) == 0) {
            return;
        }
        win_state &= ~WIN_STATE_SHADED;
        XA_WIN_STATE.setCard32Property(window, win_state);
    }
    public boolean supportsLayer(int layer) {
        return ((layer == LAYER_ALWAYS_ON_TOP) || (layer == LAYER_NORMAL)) && doLayerProtocol();
    }
    public void setLayer(XWindowPeer window, int layer) {
        if (window.isShowing()) {
            XClientMessageEvent req = new XClientMessageEvent();
            req.set_type(XConstants.ClientMessage);
            req.set_window(window.getWindow());
            req.set_message_type(XA_WIN_LAYER.getAtom());
            req.set_format(32);
            req.set_data(0, layer == LAYER_NORMAL ? WIN_LAYER_NORMAL : WIN_LAYER_ONTOP);
            req.set_data(1, 0);
            req.set_data(2, 0);
            if (log.isLoggable(PlatformLogger.FINE)) log.fine("Setting layer " + layer + " by root message : " + req);
            XToolkit.awtLock();
            try {
                XlibWrapper.XSendEvent(XToolkit.getDisplay(),
                        XlibWrapper.RootWindow(XToolkit.getDisplay(),
                            window.getScreenNumber()),
                        false,
                        XConstants.SubstructureNotifyMask,
                        req.pData);
            }
            finally {
                XToolkit.awtUnlock();
            }
            req.dispose();
        } else {
            if (log.isLoggable(PlatformLogger.FINE)) log.fine("Setting layer property to " + layer);
            XA_WIN_LAYER.setCard32Property(window, layer == LAYER_NORMAL ? WIN_LAYER_NORMAL : WIN_LAYER_ONTOP);
        }
    }
    XAtom XA_WIN_LAYER = XAtom.get("_WIN_LAYER");
    final static int WIN_STATE_STICKY          =(1<<0); 
    final static int WIN_STATE_MINIMIZED       =(1<<1); 
    final static int WIN_STATE_MAXIMIZED_VERT  =(1<<2); 
    final static int WIN_STATE_MAXIMIZED_HORIZ =(1<<3); 
    final static int WIN_STATE_HIDDEN          =(1<<4); 
    final static int WIN_STATE_SHADED          =(1<<5); 
    final static int WIN_LAYER_ONTOP = 6;
    final static int WIN_LAYER_NORMAL = 4;
    long WinWindow = 0;
    boolean supportChecked = false;
    void detect() {
        if (supportChecked) {
            return;
        }
        WinWindow = checkAnchor(XA_WIN_SUPPORTING_WM_CHECK, XAtom.XA_CARDINAL);
        supportChecked = true;
        if (log.isLoggable(PlatformLogger.FINE)) log.fine("### " + this + " is active: " + (WinWindow != 0));
    }
    boolean active() {
        detect();
        return WinWindow != 0;
    }
    boolean doStateProtocol() {
        boolean res = active() && checkProtocol(XA_WIN_PROTOCOLS, XA_WIN_STATE);
        if (log.isLoggable(PlatformLogger.FINE)) log.fine("### " + this + " supports state: " + res);
        return res;
    }
    boolean doLayerProtocol() {
        boolean res = active() && checkProtocol(XA_WIN_PROTOCOLS, XA_WIN_LAYER);
        if (log.isLoggable(PlatformLogger.FINE)) log.fine("### " + this + " supports layer: " + res);
        return res;
    }
}
