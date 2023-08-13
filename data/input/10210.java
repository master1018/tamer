public class XEmbedClientHelper extends XEmbedHelper implements XEventDispatcher {
    private static final PlatformLogger xembedLog = PlatformLogger.getLogger("sun.awt.X11.xembed.XEmbedClientHelper");
    private XEmbeddedFramePeer embedded; 
    private long server; 
    private boolean active;
    private boolean applicationActive;
    XEmbedClientHelper() {
        super();
    }
    void setClient(XEmbeddedFramePeer client) {
        if (xembedLog.isLoggable(PlatformLogger.FINE)) {
            xembedLog.fine("XEmbed client: " + client);
        }
        if (embedded != null) {
            XToolkit.removeEventDispatcher(embedded.getWindow(), this);
            active = false;
        }
        embedded = client;
        if (embedded != null) {
            XToolkit.addEventDispatcher(embedded.getWindow(), this);
        }
    }
    void install() {
        if (xembedLog.isLoggable(PlatformLogger.FINE)) {
            xembedLog.fine("Installing xembedder on " + embedded);
        }
        long[] info = new long[] { XEMBED_VERSION, XEMBED_MAPPED };
        long data = Native.card32ToData(info);
        try {
            XEmbedInfo.setAtomData(embedded.getWindow(), data, 2);
        } finally {
            unsafe.freeMemory(data);
        }
        long parentWindow = embedded.getParentWindowHandle();
        if (parentWindow != 0) {
            XToolkit.awtLock();
            try {
                XlibWrapper.XReparentWindow(XToolkit.getDisplay(),
                                            embedded.getWindow(),
                                            parentWindow,
                                            0, 0);
            } finally {
                XToolkit.awtUnlock();
            }
        }
    }
    void handleClientMessage(XEvent xev) {
        XClientMessageEvent msg = xev.get_xclient();
        if (xembedLog.isLoggable(PlatformLogger.FINE)) xembedLog.fine(msg.toString());
        if (msg.get_message_type() == XEmbed.getAtom()) {
            if (xembedLog.isLoggable(PlatformLogger.FINE)) xembedLog.fine("Embedded message: " + msgidToString((int)msg.get_data(1)));
            switch ((int)msg.get_data(1)) {
              case XEMBED_EMBEDDED_NOTIFY: 
                  active = true;
                  server = getEmbedder(embedded, msg);
                  if (!embedded.isReparented()) {
                      embedded.setReparented(true);
                      embedded.updateSizeHints();
                  }
                  embedded.notifyStarted();
                  break;
              case XEMBED_WINDOW_ACTIVATE:
                  applicationActive = true;
                  break;
              case XEMBED_WINDOW_DEACTIVATE:
                  if (applicationActive) {
                      applicationActive = false;
                      handleWindowFocusOut();
                  }
                  break;
              case XEMBED_FOCUS_IN: 
                  handleFocusIn((int)msg.get_data(2));
                  break;
              case XEMBED_FOCUS_OUT:
                  if (applicationActive) {
                      handleWindowFocusOut();
                  }
                  break;
            }
        }
    }
    void handleFocusIn(int detail) {
        if (embedded.focusAllowedFor()) {
            embedded.handleWindowFocusIn(0);
        }
        switch(detail) {
          case XEMBED_FOCUS_CURRENT:
              break;
          case XEMBED_FOCUS_FIRST:
              SunToolkit.executeOnEventHandlerThread(embedded.target, new Runnable() {
                      public void run() {
                          Component comp = ((Container)embedded.target).getFocusTraversalPolicy().getFirstComponent((Container)embedded.target);
                          if (comp != null) {
                              comp.requestFocusInWindow();
                          }
                      }});
              break;
          case XEMBED_FOCUS_LAST:
              SunToolkit.executeOnEventHandlerThread(embedded.target, new Runnable() {
                      public void run() {
                          Component comp = ((Container)embedded.target).getFocusTraversalPolicy().getLastComponent((Container)embedded.target);
                          if (comp != null) {
                              comp.requestFocusInWindow();
                          }
                      }});
              break;
        }
    }
    public void dispatchEvent(XEvent xev) {
        switch(xev.get_type()) {
          case XConstants.ClientMessage:
              handleClientMessage(xev);
              break;
          case XConstants.ReparentNotify:
              handleReparentNotify(xev);
              break;
        }
    }
    public void handleReparentNotify(XEvent xev) {
        XReparentEvent re = xev.get_xreparent();
        long newParent = re.get_parent();
        if (active) {
            embedded.notifyStopped();
            X11GraphicsConfig gc = (X11GraphicsConfig)embedded.getGraphicsConfiguration();
            X11GraphicsDevice gd = (X11GraphicsDevice)gc.getDevice();
            if ((newParent == XlibUtil.getRootWindow(gd.getScreen())) ||
                (newParent == XToolkit.getDefaultRootWindow()))
            {
                active = false;
            } else {
                server = newParent;
                embedded.notifyStarted();
            }
        }
    }
    boolean requestFocus() {
        if (active && embedded.focusAllowedFor()) {
            sendMessage(server, XEMBED_REQUEST_FOCUS);
            return true;
        }
        return false;
    }
    void handleWindowFocusOut() {
        if (XKeyboardFocusManagerPeer.getCurrentNativeFocusedWindow() == embedded.target) {
            embedded.handleWindowFocusOut(null, 0);
        }
    }
    long getEmbedder(XWindowPeer embedded, XClientMessageEvent info) {
        return XlibUtil.getParentWindow(embedded.getWindow());
    }
    boolean isApplicationActive() {
        return applicationActive;
    }
    boolean isActive() {
        return active;
    }
    void traverseOutForward() {
        if (active) {
            sendMessage(server, XEMBED_FOCUS_NEXT);
        }
    }
    void traverseOutBackward() {
        if (active) {
            sendMessage(server, XEMBED_FOCUS_PREV);
        }
    }
    void registerAccelerator(AWTKeyStroke stroke, int id) {
        if (active) {
            long sym = getX11KeySym(stroke);
            long mods = getX11Mods(stroke);
            sendMessage(server, XEMBED_REGISTER_ACCELERATOR, id, sym, mods);
        }
    }
    void unregisterAccelerator(int id) {
        if (active) {
            sendMessage(server, XEMBED_UNREGISTER_ACCELERATOR, id, 0, 0);
        }
    }
    long getX11KeySym(AWTKeyStroke stroke) {
        XToolkit.awtLock();
        try {
            return XWindow.getKeySymForAWTKeyCode(stroke.getKeyCode());
        } finally {
            XToolkit.awtUnlock();
        }
    }
    long getX11Mods(AWTKeyStroke stroke) {
        return XWindow.getXModifiers(stroke);
    }
}
