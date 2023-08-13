final class XNETProtocol extends XProtocol implements XStateProtocol, XLayerProtocol
{
    private final static PlatformLogger log = PlatformLogger.getLogger("sun.awt.X11.XNETProtocol");
    private final static PlatformLogger iconLog = PlatformLogger.getLogger("sun.awt.X11.icon.XNETProtocol");
    private static PlatformLogger stateLog = PlatformLogger.getLogger("sun.awt.X11.states.XNETProtocol");
    public boolean supportsState(int state) {
        return doStateProtocol() ; 
    }
    public void setState(XWindowPeer window, int state) {
        if (log.isLoggable(PlatformLogger.FINE)) log.fine("Setting state of " + window + " to " + state);
        if (window.isShowing()) {
            requestState(window, state);
        } else {
            setInitialState(window, state);
        }
    }
    private void setInitialState(XWindowPeer window, int state) {
        XAtomList old_state = window.getNETWMState();
        log.fine("Current state of the window {0} is {1}", window, old_state);
        if ((state & Frame.MAXIMIZED_VERT) != 0) {
            old_state.add(XA_NET_WM_STATE_MAXIMIZED_VERT);
        } else {
            old_state.remove(XA_NET_WM_STATE_MAXIMIZED_VERT);
        }
        if ((state & Frame.MAXIMIZED_HORIZ) != 0) {
            old_state.add(XA_NET_WM_STATE_MAXIMIZED_HORZ);
        } else {
            old_state.remove(XA_NET_WM_STATE_MAXIMIZED_HORZ);
        }
        log.fine("Setting initial state of the window {0} to {1}", window, old_state);
        window.setNETWMState(old_state);
    }
    private void requestState(XWindowPeer window, int state) {
        int old_net_state = getState(window);
        int max_changed = (state ^ old_net_state) & (Frame.MAXIMIZED_BOTH);
        XClientMessageEvent req = new XClientMessageEvent();
        try {
            switch(max_changed) {
              case 0:
                  return;
              case Frame.MAXIMIZED_HORIZ:
                  req.set_data(1, XA_NET_WM_STATE_MAXIMIZED_HORZ.getAtom());
                  req.set_data(2, 0);
                  break;
              case Frame.MAXIMIZED_VERT:
                  req.set_data(1, XA_NET_WM_STATE_MAXIMIZED_VERT.getAtom());
                  req.set_data(2, 0);
                  break;
              case Frame.MAXIMIZED_BOTH:
                  req.set_data(1, XA_NET_WM_STATE_MAXIMIZED_HORZ.getAtom());
                  req.set_data(2, XA_NET_WM_STATE_MAXIMIZED_VERT.getAtom());
                  break;
              default:
                  return;
            }
            if (log.isLoggable(PlatformLogger.FINE)) log.fine("Requesting state on " + window + " for " + state);
            req.set_type((int)XConstants.ClientMessage);
            req.set_window(window.getWindow());
            req.set_message_type(XA_NET_WM_STATE.getAtom());
            req.set_format(32);
            req.set_data(0, _NET_WM_STATE_TOGGLE);
            XToolkit.awtLock();
            try {
                XlibWrapper.XSendEvent(XToolkit.getDisplay(),
                        XlibWrapper.RootWindow(XToolkit.getDisplay(), window.getScreenNumber()),
                        false,
                        XConstants.SubstructureRedirectMask | XConstants.SubstructureNotifyMask,
                        req.pData);
            }
            finally {
                XToolkit.awtUnlock();
            }
        } finally {
            req.dispose();
        }
    }
    public int getState(XWindowPeer window) {
        return getStateImpl(window);
    }
    int getStateImpl(XWindowPeer window) {
        XAtomList net_wm_state = window.getNETWMState();
        if (net_wm_state.size() == 0) {
            return Frame.NORMAL;
        }
        int java_state = Frame.NORMAL;
        if (net_wm_state.contains(XA_NET_WM_STATE_MAXIMIZED_VERT)) {
            java_state |= Frame.MAXIMIZED_VERT;
        }
        if (net_wm_state.contains(XA_NET_WM_STATE_MAXIMIZED_HORZ)) {
            java_state |= Frame.MAXIMIZED_HORIZ;
        }
        return java_state;
    }
    public boolean isStateChange(XPropertyEvent e) {
        boolean res = doStateProtocol() && (e.get_atom() == XA_NET_WM_STATE.getAtom()) ;
        if (res) {
            XWindowPeer wpeer = (XWindowPeer)XToolkit.windowToXWindow(e.get_window());
            wpeer.setNETWMState(null);
        }
        return res;
    }
    public void unshadeKludge(XWindowPeer window) {
        XAtomList net_wm_state = window.getNETWMState();
        net_wm_state.remove(XA_NET_WM_STATE_SHADED);
        window.setNETWMState(net_wm_state);
    }
    public boolean supportsLayer(int layer) {
        return ((layer == LAYER_ALWAYS_ON_TOP) || (layer == LAYER_NORMAL)) && doLayerProtocol();
    }
    public void requestState(XWindow window, XAtom state, boolean isAdd) {
        XClientMessageEvent req = new XClientMessageEvent();
        try {
            req.set_type((int)XConstants.ClientMessage);
            req.set_window(window.getWindow());
            req.set_message_type(XA_NET_WM_STATE.getAtom());
            req.set_format(32);
            req.set_data(0, isAdd ? _NET_WM_STATE_ADD : _NET_WM_STATE_REMOVE);
            req.set_data(1, state.getAtom());
            req.set_data(2, 0);
            log.fine("Setting _NET_STATE atom {0} on {1} for {2}", state, window, Boolean.valueOf(isAdd));
            XToolkit.awtLock();
            try {
                XlibWrapper.XSendEvent(XToolkit.getDisplay(),
                        XlibWrapper.RootWindow(XToolkit.getDisplay(), window.getScreenNumber()),
                        false,
                        XConstants.SubstructureRedirectMask | XConstants.SubstructureNotifyMask,
                        req.pData);
            }
            finally {
                XToolkit.awtUnlock();
            }
        } finally {
            req.dispose();
        }
    }
    private void setStateHelper(XWindowPeer window, XAtom state, boolean set) {
        log.finer("Window visibility is: withdrawn={0}, visible={1}, mapped={2} showing={3}",
                  Boolean.valueOf(window.isWithdrawn()), Boolean.valueOf(window.isVisible()),
                  Boolean.valueOf(window.isMapped()), Boolean.valueOf(window.isShowing()));
        if (window.isShowing()) {
            requestState(window, state, set);
        } else {
            XAtomList net_wm_state = window.getNETWMState();
            log.finer("Current state on {0} is {1}", window, net_wm_state);
            if (!set) {
                net_wm_state.remove(state);
            } else {
                net_wm_state.add(state);
            }
            log.fine("Setting states on {0} to {1}", window, net_wm_state);
            window.setNETWMState(net_wm_state);
        }
        XToolkit.XSync();
    }
    public void setLayer(XWindowPeer window, int layer) {
        setStateHelper(window, XA_NET_WM_STATE_ABOVE, layer == LAYER_ALWAYS_ON_TOP);
    }
    XAtom XA_UTF8_STRING = XAtom.get("UTF8_STRING");   
    XAtom XA_NET_SUPPORTING_WM_CHECK = XAtom.get("_NET_SUPPORTING_WM_CHECK");
    XAtom XA_NET_SUPPORTED = XAtom.get("_NET_SUPPORTED");      
    XAtom XA_NET_WM_NAME = XAtom.get("_NET_WM_NAME");  
    XAtom XA_NET_WM_STATE = XAtom.get("_NET_WM_STATE");
    XAtom XA_NET_WM_STATE_MAXIMIZED_HORZ = XAtom.get("_NET_WM_STATE_MAXIMIZED_HORZ");
    XAtom XA_NET_WM_STATE_MAXIMIZED_VERT = XAtom.get("_NET_WM_STATE_MAXIMIZED_VERT");
    XAtom XA_NET_WM_STATE_SHADED = XAtom.get("_NET_WM_STATE_SHADED");
    XAtom XA_NET_WM_STATE_ABOVE = XAtom.get("_NET_WM_STATE_ABOVE");
    XAtom XA_NET_WM_STATE_MODAL = XAtom.get("_NET_WM_STATE_MODAL");
    XAtom XA_NET_WM_STATE_FULLSCREEN = XAtom.get("_NET_WM_STATE_FULLSCREEN");
    XAtom XA_NET_WM_STATE_BELOW = XAtom.get("_NET_WM_STATE_BELOW");
    XAtom XA_NET_WM_STATE_HIDDEN = XAtom.get("_NET_WM_STATE_HIDDEN");
    XAtom XA_NET_WM_STATE_SKIP_TASKBAR = XAtom.get("_NET_WM_STATE_SKIP_TASKBAR");
    XAtom XA_NET_WM_STATE_SKIP_PAGER = XAtom.get("_NET_WM_STATE_SKIP_PAGER");
    public final XAtom XA_NET_WM_WINDOW_TYPE = XAtom.get("_NET_WM_WINDOW_TYPE");
    public final XAtom XA_NET_WM_WINDOW_TYPE_NORMAL = XAtom.get("_NET_WM_WINDOW_TYPE_NORMAL");
    public final XAtom XA_NET_WM_WINDOW_TYPE_DIALOG = XAtom.get("_NET_WM_WINDOW_TYPE_DIALOG");
    public final XAtom XA_NET_WM_WINDOW_TYPE_UTILITY = XAtom.get("_NET_WM_WINDOW_TYPE_UTILITY");
    public final XAtom XA_NET_WM_WINDOW_TYPE_POPUP_MENU = XAtom.get("_NET_WM_WINDOW_TYPE_POPUP_MENU");
    XAtom XA_NET_WM_WINDOW_OPACITY = XAtom.get("_NET_WM_WINDOW_OPACITY");
    final static int _NET_WM_STATE_REMOVE      =0; 
    final static int _NET_WM_STATE_ADD         =1; 
    final static int _NET_WM_STATE_TOGGLE      =2; 
    boolean supportChecked = false;
    long NetWindow = 0;
    void detect() {
        if (supportChecked) {
            return;
        }
        NetWindow = checkAnchor(XA_NET_SUPPORTING_WM_CHECK, XAtom.XA_WINDOW);
        supportChecked = true;
        if (log.isLoggable(PlatformLogger.FINE)) log.fine("### " + this + " is active: " + (NetWindow != 0));
    }
    boolean active() {
        detect();
        return NetWindow != 0;
    }
    boolean doStateProtocol() {
        boolean res = active() && checkProtocol(XA_NET_SUPPORTED, XA_NET_WM_STATE);
        stateLog.finer("doStateProtocol() returns " + res);
        return res;
    }
    boolean doLayerProtocol() {
        boolean res = active() && checkProtocol(XA_NET_SUPPORTED, XA_NET_WM_STATE_ABOVE);
        return res;
    }
    boolean doModalityProtocol() {
        boolean res = active() && checkProtocol(XA_NET_SUPPORTED, XA_NET_WM_STATE_MODAL);
        return res;
    }
    boolean doOpacityProtocol() {
        boolean res = active() && checkProtocol(XA_NET_SUPPORTED, XA_NET_WM_WINDOW_OPACITY);
        return res;
    }
    boolean isWMName(String name) {
        if (!active()) {
            return false;
        }
        String net_wm_name_string = getWMName();
        if (net_wm_name_string == null) {
            return false;
        }
        if (log.isLoggable(PlatformLogger.FINE)) log.fine("### WM_NAME = " + net_wm_name_string);
        return net_wm_name_string.startsWith(name);
    }
    String net_wm_name_cache;
    public String getWMName() {
        if (!active()) {
            return null;
        }
        if (net_wm_name_cache != null) {
            return net_wm_name_cache;
        }
        String charSet = "UTF8";
        byte[] net_wm_name = XA_NET_WM_NAME.getByteArrayProperty(NetWindow, XA_UTF8_STRING.getAtom());
        if (net_wm_name == null) {
            net_wm_name = XA_NET_WM_NAME.getByteArrayProperty(NetWindow, XAtom.XA_STRING);
            charSet = "ASCII";
        }
        if (net_wm_name == null) {
            return null;
        }
        try {
            net_wm_name_cache = new String(net_wm_name, charSet);
            return net_wm_name_cache;
        } catch (java.io.UnsupportedEncodingException uex) {
            return null;
        }
    }
    public void setWMIcons(XWindowPeer window, java.util.List<XIconInfo> icons) {
        if (window == null) return;
        XAtom iconsAtom = XAtom.get("_NET_WM_ICON");
        if (icons == null) {
            iconsAtom.DeleteProperty(window);
            return;
        }
        int length = 0;
        for (XIconInfo ii : icons) {
            length += ii.getRawLength();
        }
        int cardinalSize = (XlibWrapper.dataModel == 32) ? 4 : 8;
        int bufferSize = length * cardinalSize;
        if (bufferSize != 0) {
            long buffer = XlibWrapper.unsafe.allocateMemory(bufferSize);
            try {
                long ptr = buffer;
                for (XIconInfo ii : icons) {
                    int size = ii.getRawLength() * cardinalSize;
                    if (XlibWrapper.dataModel == 32) {
                        XlibWrapper.copyIntArray(ptr, ii.getIntData(), size);
                    } else {
                        XlibWrapper.copyLongArray(ptr, ii.getLongData(), size);
                    }
                    ptr += size;
                }
                iconsAtom.setAtomData(window.getWindow(), XAtom.XA_CARDINAL, buffer, bufferSize/Native.getCard32Size());
            } finally {
                XlibWrapper.unsafe.freeMemory(buffer);
            }
        } else {
            iconsAtom.DeleteProperty(window);
        }
    }
    public boolean isWMStateNetHidden(XWindowPeer window) {
        if (!doStateProtocol()) {
            return false;
        }
        XAtomList state = window.getNETWMState();
        return (state != null && state.size() != 0 && state.contains(XA_NET_WM_STATE_HIDDEN));
    }
}
