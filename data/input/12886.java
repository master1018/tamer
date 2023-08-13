public class XPopupMenuPeer extends XMenuWindow implements PopupMenuPeer {
    private static PlatformLogger log = PlatformLogger.getLogger("sun.awt.X11.XBaseMenuWindow");
    private XComponentPeer componentPeer;
    private PopupMenu popupMenuTarget;
    private XMenuPeer showingMousePressedSubmenu = null;
    private final static int CAPTION_MARGIN_TOP = 4;
    private final static int CAPTION_SEPARATOR_HEIGHT = 6;
    private final static Field f_enabled;
    private final static Field f_label;
    private final static Method m_getFont;
    private final static Field f_items;
    static {
        f_enabled = SunToolkit.getField(MenuItem.class, "enabled");
        f_label = SunToolkit.getField(MenuItem.class, "label");
        f_items = SunToolkit.getField(Menu.class, "items");
        m_getFont = SunToolkit.getMethod(MenuComponent.class, "getFont_NoClientCode", null);
    }
    XPopupMenuPeer(PopupMenu target) {
        super(null);
        this.popupMenuTarget = target;
    }
    public void setFont(Font f) {
        resetMapping();
        setItemsFont(f);
        postPaintEvent();
    }
    public void setLabel(String label) {
        resetMapping();
        postPaintEvent();
    }
    public void setEnabled(boolean enabled) {
        postPaintEvent();
    }
    public void enable() {
        setEnabled( true );
    }
    public void disable() {
        setEnabled( false );
    }
    public void addSeparator() {
        if (log.isLoggable(PlatformLogger.FINER)) log.finer("addSeparator is not implemented");
    }
    public void show(Event e) {
        target = (Component)e.target;
        Vector targetItemVector = getMenuTargetItems();
        if (targetItemVector != null) {
            reloadItems(targetItemVector);
            Point tl = target.getLocationOnScreen();
            Point pt = new Point(tl.x + e.x, tl.y + e.y);
            if (!ensureCreated()) {
                return;
            }
            Dimension dim = getDesiredSize();
            Rectangle bounds = getWindowBounds(pt, dim);
            reshape(bounds);
            xSetVisible(true);
            toFront();
            selectItem(null, false);
            grabInput();
        }
    }
    Font getTargetFont() {
        if (popupMenuTarget == null) {
            return XWindow.getDefaultFont();
        }
        try {
            return (Font)m_getFont.invoke(popupMenuTarget, new Object[0]);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return XWindow.getDefaultFont();
    }
    String getTargetLabel() {
        if (target == null) {
            return "";
        }
        try {
            String label = (String)f_label.get(popupMenuTarget);
            return (label == null) ? "" : label;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return "";
    }
    boolean isTargetEnabled() {
        if (popupMenuTarget == null) {
            return false;
        }
        try {
            return f_enabled.getBoolean(popupMenuTarget);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return false;
    }
    Vector getMenuTargetItems() {
        try {
            return (Vector)f_items.get(popupMenuTarget);
        } catch (IllegalAccessException iae) {
            iae.printStackTrace();
            return null;
        }
    }
    protected Rectangle getWindowBounds(Point origin, Dimension windowSize) {
        Rectangle globalBounds = new Rectangle(origin.x, origin.y, 0, 0);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Rectangle res;
        res = fitWindowRight(globalBounds, windowSize, screenSize);
        if (res != null) {
            return res;
        }
        res = fitWindowLeft(globalBounds, windowSize, screenSize);
        if (res != null) {
            return res;
        }
        res = fitWindowBelow(globalBounds, windowSize, screenSize);
        if (res != null) {
            return res;
        }
        res = fitWindowAbove(globalBounds, windowSize, screenSize);
        if (res != null) {
            return res;
        }
        return fitWindowToScreen(windowSize, screenSize);
   }
    protected Dimension getCaptionSize() {
        String s = getTargetLabel();
        if (s.equals("")) {
            return null;
        }
        Graphics g = getGraphics();
        if (g == null) {
            return null;
        }
        try {
            g.setFont(getTargetFont());
            FontMetrics fm = g.getFontMetrics();
            String str = getTargetLabel();
            int width = fm.stringWidth(str);
            int height = CAPTION_MARGIN_TOP + fm.getHeight() + CAPTION_SEPARATOR_HEIGHT;
            Dimension textDimension = new Dimension(width, height);
            return textDimension;
        } finally {
            g.dispose();
        }
    }
    protected void paintCaption(Graphics g, Rectangle rect) {
        String s = getTargetLabel();
        if (s.equals("")) {
            return;
        }
        g.setFont(getTargetFont());
        FontMetrics fm = g.getFontMetrics();
        String str = getTargetLabel();
        int width = fm.stringWidth(str);
        int textx = rect.x + (rect.width - width) / 2;
        int texty = rect.y + CAPTION_MARGIN_TOP + fm.getAscent();
        int sepy = rect.y + rect.height - CAPTION_SEPARATOR_HEIGHT / 2;
        g.setColor(isTargetEnabled() ? getForegroundColor() : getDisabledColor());
        g.drawString(s, textx, texty);
        draw3DRect(g, rect.x, sepy,  rect.width, 2, false);
    }
    protected void doDispose() {
        super.doDispose();
        XToolkit.targetDisposedPeer(popupMenuTarget, this);
    }
    protected void handleEvent(AWTEvent event) {
        switch(event.getID()) {
        case MouseEvent.MOUSE_PRESSED:
        case MouseEvent.MOUSE_RELEASED:
        case MouseEvent.MOUSE_CLICKED:
        case MouseEvent.MOUSE_MOVED:
        case MouseEvent.MOUSE_ENTERED:
        case MouseEvent.MOUSE_EXITED:
        case MouseEvent.MOUSE_DRAGGED:
            doHandleJavaMouseEvent((MouseEvent)event);
            break;
        case KeyEvent.KEY_PRESSED:
        case KeyEvent.KEY_RELEASED:
            doHandleJavaKeyEvent((KeyEvent)event);
            break;
        default:
            super.handleEvent(event);
            break;
        }
    }
    void ungrabInputImpl() {
        hide();
    }
    public void handleKeyPress(XEvent xev) {
        XKeyEvent xkey = xev.get_xkey();
        if (log.isLoggable(PlatformLogger.FINE)) {
            log.fine(xkey.toString());
        }
        if (isEventDisabled(xev)) {
            return;
        }
        final Component currentSource = (Component)getEventSource();
        handleKeyPress(xkey);
    }
}
