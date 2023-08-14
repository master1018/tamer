public final class XGlobalCursorManager extends GlobalCursorManager {
    private static Field  field_pData;
    private static Field  field_type;
    private static Class  cursorClass;
    private static Method method_setPData;
    static {
        cursorClass = java.awt.Cursor.class;
        field_pData = SunToolkit.getField(cursorClass, "pData");
        field_type  = SunToolkit.getField(cursorClass, "type");
        method_setPData = SunToolkit.getMethod(cursorClass, "setPData", new Class[] {long.class});
        if (field_pData == null || field_type == null || method_setPData == null) {
            System.out.println("Unable to initialize XGlobalCursorManager: ");
            Thread.dumpStack();
        }
    }
    private WeakReference<Component> nativeContainer;
    private static XGlobalCursorManager manager;
    static GlobalCursorManager getCursorManager() {
        if (manager == null) {
            manager = new XGlobalCursorManager();
        }
        return manager;
    }
    static void nativeUpdateCursor(Component heavy) {
        XGlobalCursorManager.getCursorManager().updateCursorLater(heavy);
    }
    protected void setCursor(Component comp, Cursor cursor, boolean useCache) {
        if (comp == null) {
            return;
        }
        Cursor cur = useCache ? cursor : getCapableCursor(comp);
        Component nc = null;
        if (useCache) {
            synchronized (this) {
                nc = nativeContainer.get();
            }
        } else {
           nc = SunToolkit.getHeavyweightComponent(comp);
        }
        if (nc != null) {
            ComponentPeer nc_peer = AWTAccessor.getComponentAccessor().getPeer(nc);
            if (nc_peer instanceof XComponentPeer) {
                synchronized (this) {
                    nativeContainer = new WeakReference<Component>(nc);
                }
                ((XComponentPeer)nc_peer).pSetCursor(cur, false);
                updateGrabbedCursor(cur);
            }
        }
    }
    private static void updateGrabbedCursor(Cursor cur) {
        XBaseWindow target = XAwtState.getGrabWindow();
        if (target instanceof XWindowPeer) {
            XWindowPeer grabber = (XWindowPeer) target;
            grabber.pSetCursor(cur);
        }
    }
    protected void updateCursorOutOfJava() {
        updateGrabbedCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }
    protected void getCursorPos(Point p) {
        if (!((XToolkit)Toolkit.getDefaultToolkit()).getLastCursorPos(p)) {
            XToolkit.awtLock();
            try {
                long display = XToolkit.getDisplay();
                long root_window = XlibWrapper.RootWindow(display,
                                                          XlibWrapper.DefaultScreen(display));
                XlibWrapper.XQueryPointer(display, root_window,
                                          XlibWrapper.larg1,
                                          XlibWrapper.larg2,
                                          XlibWrapper.larg3,
                                          XlibWrapper.larg4,
                                          XlibWrapper.larg5,
                                          XlibWrapper.larg6,
                                          XlibWrapper.larg7);
                p.x = (int) XlibWrapper.unsafe.getInt(XlibWrapper.larg3);
                p.y = (int) XlibWrapper.unsafe.getInt(XlibWrapper.larg4);
            } finally {
                XToolkit.awtUnlock();
            }
        }
    }
    protected  Component findHeavyweightUnderCursor() {
        return XAwtState.getComponentMouseEntered();
    }
    protected  Component findComponentAt(Container con, int x, int y) {
        return con.findComponentAt(x,y);
    }
    protected  Point getLocationOnScreen(Component c) {
        return c.getLocationOnScreen();
    }
    protected Component findHeavyweightUnderCursor(boolean useCache) {
        return findHeavyweightUnderCursor();
    }
    private Cursor getCapableCursor(Component comp) {
        AWTAccessor.ComponentAccessor compAccessor = AWTAccessor.getComponentAccessor();
        Component c = comp;
        while ((c != null) && !(c instanceof Window)
               && compAccessor.isEnabled(c)
               && compAccessor.isVisible(c)
               && compAccessor.isDisplayable(c))
        {
            c = compAccessor.getParent(c);
        }
        if (c instanceof Window) {
            return (compAccessor.isEnabled(c)
                    && compAccessor.isVisible(c)
                    && compAccessor.isDisplayable(c)
                    && compAccessor.isEnabled(comp))
                   ?
                    compAccessor.getCursor(comp)
                   :
                    Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
        } else if (c == null) {
            return null;
        }
        return getCapableCursor(compAccessor.getParent(c));
    }
    static long getCursor(Cursor c) {
        long pData = 0;
        int type = 0;
        try {
            pData = field_pData.getLong(c);
            type = field_type.getInt(c);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        if (pData != 0) return pData;
        int cursorType = 0;
        switch (type) {
          case Cursor.DEFAULT_CURSOR:
              cursorType = XCursorFontConstants.XC_left_ptr;
              break;
          case Cursor.CROSSHAIR_CURSOR:
              cursorType = XCursorFontConstants.XC_crosshair;
              break;
          case Cursor.TEXT_CURSOR:
              cursorType = XCursorFontConstants.XC_xterm;
              break;
          case Cursor.WAIT_CURSOR:
              cursorType = XCursorFontConstants.XC_watch;
              break;
          case Cursor.SW_RESIZE_CURSOR:
              cursorType = XCursorFontConstants.XC_bottom_left_corner;
              break;
          case Cursor.NW_RESIZE_CURSOR:
              cursorType = XCursorFontConstants.XC_top_left_corner;
              break;
          case Cursor.SE_RESIZE_CURSOR:
              cursorType = XCursorFontConstants.XC_bottom_right_corner;
              break;
          case Cursor.NE_RESIZE_CURSOR:
              cursorType = XCursorFontConstants.XC_top_right_corner;
              break;
          case Cursor.S_RESIZE_CURSOR:
              cursorType = XCursorFontConstants.XC_bottom_side;
              break;
          case Cursor.N_RESIZE_CURSOR:
              cursorType = XCursorFontConstants.XC_top_side;
              break;
          case Cursor.W_RESIZE_CURSOR:
              cursorType = XCursorFontConstants.XC_left_side;
              break;
          case Cursor.E_RESIZE_CURSOR:
              cursorType = XCursorFontConstants.XC_right_side;
              break;
          case Cursor.HAND_CURSOR:
              cursorType = XCursorFontConstants.XC_hand2;
              break;
          case Cursor.MOVE_CURSOR:
              cursorType = XCursorFontConstants.XC_fleur;
              break;
        }
        XToolkit.awtLock();
        try {
            pData =(long) XlibWrapper.XCreateFontCursor(XToolkit.getDisplay(), cursorType);
        }
        finally {
            XToolkit.awtUnlock();
        }
        setPData(c,pData);
        return pData;
    }
    static void setPData(Cursor c, long pData) {
        try {
            method_setPData.invoke(c, pData);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
