public class XMenuBarPeer extends XBaseMenuWindow implements MenuBarPeer {
    private static PlatformLogger log = PlatformLogger.getLogger("sun.awt.X11.XMenuBarPeer");
    private XFramePeer framePeer;
    private MenuBar menuBarTarget;
    private XMenuPeer helpMenu = null;
    private final static int BAR_SPACING_TOP = 3;
    private final static int BAR_SPACING_BOTTOM = 3;
    private final static int BAR_SPACING_LEFT = 3;
    private final static int BAR_SPACING_RIGHT = 3;
    private final static int BAR_ITEM_SPACING = 2;
    private final static int BAR_ITEM_MARGIN_LEFT = 10;
    private final static int BAR_ITEM_MARGIN_RIGHT = 10;
    private final static int BAR_ITEM_MARGIN_TOP = 2;
    private final static int BAR_ITEM_MARGIN_BOTTOM = 2;
    private static Field f_helpMenu;
    private static Field f_menus;
    static {
        f_helpMenu = SunToolkit.getField(MenuBar.class, "helpMenu");
        f_menus = SunToolkit.getField(MenuBar.class, "menus");
    }
    static class MappingData extends XBaseMenuWindow.MappingData {
        int desiredHeight;
        MappingData(XMenuItemPeer[] items, int desiredHeight) {
            super(items);
            this.desiredHeight = desiredHeight;
        }
        MappingData() {
            this.desiredHeight = 0;
        }
        public int getDesiredHeight() {
            return this.desiredHeight;
        }
    }
    XMenuBarPeer(MenuBar menuBarTarget) {
        this.menuBarTarget = menuBarTarget;
    }
    public void setFont(Font f) {
        resetMapping();
        setItemsFont(f);
        postPaintEvent();
    }
    public void addMenu(Menu m) {
        addItem(m);
        postPaintEvent();
    }
    public void delMenu(int index) {
        synchronized(getMenuTreeLock()) {
            XMenuItemPeer item = getItem(index);
            if (item != null && item == helpMenu) {
                helpMenu = null;
            }
            delItem(index);
        }
        postPaintEvent();
    }
    public void addHelpMenu(Menu m) {
        XMenuPeer mp = (XMenuPeer)m.getPeer();
        synchronized(getMenuTreeLock()) {
            helpMenu = mp;
        }
        postPaintEvent();
    }
    public void init(Frame frame) {
        this.target = frame;
        this.framePeer = (XFramePeer)frame.getPeer();
        XCreateWindowParams params = getDelayedParams();
        params.remove(DELAYED);
        params.add(PARENT_WINDOW, framePeer.getShell());
        params.add(TARGET, frame);
        init(params);
    }
    void postInit(XCreateWindowParams params) {
        super.postInit(params);
        Vector targetMenuVector = null;
        Menu targetHelpMenu = null;
        try {
            targetMenuVector = (Vector)f_menus.get(menuBarTarget);
            targetHelpMenu = (Menu)f_helpMenu.get(menuBarTarget);
            reloadItems(targetMenuVector);
        } catch (IllegalAccessException iae) {
            iae.printStackTrace();
        }
        if (targetHelpMenu != null) {
            addHelpMenu(targetHelpMenu);
        }
        xSetVisible(true);
        toFront();
    }
    protected XBaseMenuWindow getParentMenuWindow() {
        return null;
    }
    protected MappingData map() {
        XMenuItemPeer[] itemVector = copyItems();
        int itemCnt = itemVector.length;
        XMenuItemPeer helpMenu = this.helpMenu;
        int helpMenuPos = -1;
        if (helpMenu != null) {
            for (int i = 0; i < itemCnt; i++) {
                if (itemVector[i] == helpMenu) {
                    helpMenuPos = i;
                    break;
                }
            }
            if (helpMenuPos != -1 && helpMenuPos != itemCnt - 1) {
                System.arraycopy(itemVector, helpMenuPos + 1, itemVector, helpMenuPos, itemCnt - 1 - helpMenuPos);
                itemVector[itemCnt - 1] = helpMenu;
            }
        }
        int maxHeight = 0;
        XMenuItemPeer.TextMetrics[] itemMetrics = new XMenuItemPeer.TextMetrics[itemCnt];
        for (int i = 0; i < itemCnt; i++) {
            itemMetrics[i] = itemVector[i].getTextMetrics();
            Dimension dim = itemMetrics[i].getTextDimension();
            if (dim != null) {
                maxHeight = Math.max(maxHeight, dim.height);
            }
        }
        int nextOffset = 0;
        int itemHeight = BAR_ITEM_MARGIN_TOP + maxHeight + BAR_ITEM_MARGIN_BOTTOM;
        int mappedCnt = itemCnt;
        for (int i = 0; i < itemCnt; i++) {
            XMenuItemPeer item = itemVector[i];
            XMenuItemPeer.TextMetrics metrics = itemMetrics[i];
            Dimension dim = metrics.getTextDimension();
            if (dim != null) {
                int itemWidth = BAR_ITEM_MARGIN_LEFT + dim.width + BAR_ITEM_MARGIN_RIGHT;
                if ((nextOffset + itemWidth > this.width) && (i > 0)) {
                    mappedCnt = i;
                    break;
                }
                if ((i == itemCnt - 1) && helpMenuPos != -1) {
                    nextOffset = Math.max(nextOffset, this.width - itemWidth - BAR_SPACING_RIGHT);
                }
                Rectangle bounds = new Rectangle(nextOffset, BAR_SPACING_TOP, itemWidth, itemHeight);
                int y = (maxHeight + dim.height) / 2  - metrics.getTextBaseline();
                Point textOrigin = new Point(nextOffset + BAR_ITEM_MARGIN_LEFT, BAR_SPACING_TOP + BAR_ITEM_MARGIN_TOP + y);
                nextOffset += itemWidth + BAR_ITEM_SPACING;
                item.map(bounds, textOrigin);
            } else {
                Rectangle bounds = new Rectangle(nextOffset, BAR_SPACING_TOP, 0, 0);
                Point textOrigin = new Point(nextOffset + BAR_ITEM_MARGIN_LEFT, BAR_SPACING_TOP + BAR_ITEM_MARGIN_TOP);
            }
        }
        XMenuItemPeer mappedVector[] = new XMenuItemPeer[mappedCnt];
        System.arraycopy(itemVector, 0, mappedVector, 0, mappedCnt);
        MappingData mappingData = new MappingData(mappedVector, BAR_SPACING_TOP + itemHeight + BAR_SPACING_BOTTOM);
        return mappingData;
    }
    protected Rectangle getSubmenuBounds(Rectangle itemBounds, Dimension windowSize) {
        Rectangle globalBounds = toGlobal(itemBounds);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Rectangle res;
        res = fitWindowBelow(globalBounds, windowSize, screenSize);
        if (res != null) {
            return res;
        }
        res = fitWindowAbove(globalBounds, windowSize, screenSize);
        if (res != null) {
            return res;
        }
        res = fitWindowRight(globalBounds, windowSize, screenSize);
        if (res != null) {
            return res;
        }
        res = fitWindowLeft(globalBounds, windowSize, screenSize);
        if (res != null) {
            return res;
        }
        return fitWindowToScreen(windowSize, screenSize);
    }
    protected void updateSize() {
        resetMapping();
        if (framePeer != null) {
            framePeer.reshapeMenubarPeer();
        }
    }
    int getDesiredHeight() {
        MappingData mappingData = (MappingData)getMappingData();
        return mappingData.getDesiredHeight();
    }
    boolean isFramePeerEnabled() {
        if (framePeer != null) {
            return framePeer.isEnabled();
        }
        return false;
    }
    protected void doDispose() {
        super.doDispose();
        XToolkit.targetDisposedPeer(menuBarTarget, this);
    }
    public void reshape(int x, int y, int width, int height) {
        if ((width != this.width) || (height != this.height)) {
            resetMapping();
        }
        super.reshape(x, y, width, height);
    }
    void ungrabInputImpl() {
        selectItem(null, false);
        super.ungrabInputImpl();
        postPaintEvent();
    }
    public void paint(Graphics g) {
        resetColors();
        int width = getWidth();
        int height = getHeight();
        flush();
        g.setColor(getBackgroundColor());
        g.fillRect(1, 1, width - 2, height - 2);
        draw3DRect(g, 0, 0, width, height, true);
        MappingData mappingData = (MappingData)getMappingData();
        XMenuItemPeer[] itemVector = mappingData.getItems();
        XMenuItemPeer selectedItem = getSelectedItem();
        for (int i = 0; i < itemVector.length; i++) {
            XMenuItemPeer item = itemVector[i];
            g.setFont(item.getTargetFont());
            Rectangle bounds = item.getBounds();
            Point textOrigin = item.getTextOrigin();
            if (item == selectedItem) {
                g.setColor(getSelectedColor());
                g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
                draw3DRect(g, bounds.x, bounds.y, bounds.width, bounds.height, false);
            }
            if (isFramePeerEnabled() && item.isTargetItemEnabled()) {
                g.setColor(getForegroundColor());
            } else {
                g.setColor(getDisabledColor());
            }
            g.drawString(item.getTargetLabel(), textOrigin.x, textOrigin.y);
        }
        flush();
    }
    static final int W_DIFF = (XFramePeer.CROSSHAIR_INSET + 1) * 2;
    static final int H_DIFF = XFramePeer.BUTTON_Y + XFramePeer.BUTTON_H;
    void print(Graphics g) {
    }
    protected void handleEvent(AWTEvent event) {
        if ((framePeer != null) &&
            (event.getID() != PaintEvent.PAINT))
        {
            if (framePeer.isModalBlocked()) {
                return;
            }
        }
        switch(event.getID()) {
        case MouseEvent.MOUSE_PRESSED:
        case MouseEvent.MOUSE_RELEASED:
        case MouseEvent.MOUSE_CLICKED:
        case MouseEvent.MOUSE_MOVED:
        case MouseEvent.MOUSE_ENTERED:
        case MouseEvent.MOUSE_EXITED:
        case MouseEvent.MOUSE_DRAGGED:
            if (isFramePeerEnabled()) {
                doHandleJavaMouseEvent((MouseEvent)event);
            }
            break;
        case KeyEvent.KEY_PRESSED:
        case KeyEvent.KEY_RELEASED:
            if (isFramePeerEnabled()) {
                doHandleJavaKeyEvent((KeyEvent)event);
            }
            break;
        default:
            super.handleEvent(event);
            break;
        }
    }
    void handleF10KeyPress(KeyEvent event) {
        int keyState = event.getModifiers();
        if (((keyState & InputEvent.ALT_MASK) != 0) ||
            ((keyState & InputEvent.SHIFT_MASK) != 0) ||
            ((keyState & InputEvent.CTRL_MASK) != 0)) {
            return;
        }
        grabInput();
        selectItem(getFirstSelectableItem(), true);
    }
    public void handleKeyPress(XEvent xev) {
        XKeyEvent xkey = xev.get_xkey();
        if (log.isLoggable(PlatformLogger.FINE)) log.fine(xkey.toString());
        if (isEventDisabled(xev)) {
            return;
        }
        final Component currentSource = (Component)getEventSource();
        handleKeyPress(xkey);
    }
} 
