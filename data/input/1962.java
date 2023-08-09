public class XMenuWindow extends XBaseMenuWindow {
    private static PlatformLogger log = PlatformLogger.getLogger("sun.awt.X11.XMenuWindow");
    private XMenuPeer menuPeer;
    private final static int WINDOW_SPACING_LEFT = 2;
    private final static int WINDOW_SPACING_RIGHT = 2;
    private final static int WINDOW_SPACING_TOP = 2;
    private final static int WINDOW_SPACING_BOTTOM = 2;
    private final static int WINDOW_ITEM_INDENT = 15;
    private final static int WINDOW_ITEM_MARGIN_LEFT = 2;
    private final static int WINDOW_ITEM_MARGIN_RIGHT = 2;
    private final static int WINDOW_ITEM_MARGIN_TOP = 2;
    private final static int WINDOW_ITEM_MARGIN_BOTTOM = 2;
    private final static int WINDOW_SHORTCUT_SPACING = 10;
    private static final int CHECKMARK_SIZE = 128;
    private static final int[] CHECKMARK_X = new int[] {1, 25,56,124,124,85, 64};  
    private static final int[] CHECKMARK_Y = new int[] {59,35,67,  0, 12,66,123};  
    static class MappingData extends XBaseMenuWindow.MappingData {
        private Rectangle captionRect;
        private Dimension desiredSize;
        private int leftMarkWidth;
        private int shortcutOrigin;
        private int rightMarkOrigin;
        MappingData(XMenuItemPeer[] items, Rectangle captionRect, Dimension desiredSize, int leftMarkWidth, int shortcutOrigin, int rightMarkOrigin) {
            super(items);
            this.captionRect = captionRect;
            this.desiredSize = desiredSize;
            this.leftMarkWidth = leftMarkWidth;
            this.shortcutOrigin = shortcutOrigin;
            this.rightMarkOrigin = rightMarkOrigin;
        }
        MappingData() {
            this.desiredSize = new Dimension(0, 0);
            this.leftMarkWidth = 0;
            this.shortcutOrigin = 0;
            this.rightMarkOrigin = 0;
        }
        public Rectangle getCaptionRect() {
            return this.captionRect;
        }
        public Dimension getDesiredSize() {
            return this.desiredSize;
        }
        public int getShortcutOrigin() {
            return this.shortcutOrigin;
        }
        public int getLeftMarkWidth() {
            return this.leftMarkWidth;
        }
        public int getRightMarkOrigin() {
            return this.rightMarkOrigin;
        }
    }
    XMenuWindow(XMenuPeer menuPeer) {
        if (menuPeer != null) {
            this.menuPeer = menuPeer;
            this.target = menuPeer.getContainer().target;
            Vector targetItemVector = null;
            targetItemVector = getMenuTargetItems();
            reloadItems(targetItemVector);
        }
    }
    void postInit(XCreateWindowParams params) {
        super.postInit(params);
    }
    protected XBaseMenuWindow getParentMenuWindow() {
        return (menuPeer != null) ? menuPeer.getContainer() : null;
    }
    protected MappingData map() {
        int itemCnt;
        if (!isCreated()) {
            MappingData mappingData = new MappingData(new XMenuItemPeer[0], new Rectangle(0, 0, 0, 0), new Dimension(0, 0), 0, 0, 0);
            return mappingData;
        }
        XMenuItemPeer[] itemVector = copyItems();
        itemCnt = itemVector.length;
        Dimension captionSize = getCaptionSize();
        int maxWidth = (captionSize != null) ? captionSize.width : 0;
        int maxLeftIndent = 0;
        int maxRightIndent = 0;
        int maxShortcutWidth = 0;
        XMenuItemPeer.TextMetrics[] itemMetrics = new XMenuItemPeer.TextMetrics[itemCnt];
        for (int i = 0; i < itemCnt; i++) {
            XMenuItemPeer item = itemVector[i];
            itemMetrics[i] = itemVector[i].getTextMetrics();
            Dimension dim = itemMetrics[i].getTextDimension();
            if (dim != null) {
                if (itemVector[i] instanceof XCheckboxMenuItemPeer) {
                    maxLeftIndent = Math.max(maxLeftIndent, dim.height);
                } else if (itemVector[i] instanceof XMenuPeer) {
                    maxRightIndent = Math.max(maxRightIndent, dim.height);
                }
                maxWidth = Math.max(maxWidth, dim.width);
                maxShortcutWidth = Math.max(maxShortcutWidth, itemMetrics[i].getShortcutWidth());
            }
        }
        int nextOffset = WINDOW_SPACING_TOP;
        int shortcutOrigin = WINDOW_SPACING_LEFT + WINDOW_ITEM_MARGIN_LEFT + maxLeftIndent + maxWidth;
        if (maxShortcutWidth > 0) {
            shortcutOrigin = shortcutOrigin + WINDOW_SHORTCUT_SPACING;
        }
        int rightMarkOrigin = shortcutOrigin + maxShortcutWidth;
        int itemWidth = rightMarkOrigin + maxRightIndent + WINDOW_ITEM_MARGIN_RIGHT;
        int width = WINDOW_SPACING_LEFT + itemWidth + WINDOW_SPACING_RIGHT;
        Rectangle captionRect = null;
        if (captionSize != null) {
            captionRect = new Rectangle(WINDOW_SPACING_LEFT, nextOffset, itemWidth, captionSize.height);
            nextOffset += captionSize.height;
        } else {
            captionRect = new Rectangle(WINDOW_SPACING_LEFT, nextOffset, maxWidth, 0);
        }
        for (int i = 0; i < itemCnt; i++) {
            XMenuItemPeer item = (XMenuItemPeer)itemVector[i];
            XMenuItemPeer.TextMetrics metrics = itemMetrics[i];
            Dimension dim = metrics.getTextDimension();
            if (dim != null) {
                int itemHeight = WINDOW_ITEM_MARGIN_TOP + dim.height + WINDOW_ITEM_MARGIN_BOTTOM;
                Rectangle bounds = new Rectangle(WINDOW_SPACING_LEFT, nextOffset, itemWidth, itemHeight);
                int y = (itemHeight + dim.height) / 2  - metrics.getTextBaseline();
                Point textOrigin = new Point(WINDOW_SPACING_LEFT + WINDOW_ITEM_MARGIN_LEFT + maxLeftIndent, nextOffset + y);
                nextOffset += itemHeight;
                item.map(bounds, textOrigin);
            } else {
                Rectangle bounds = new Rectangle(WINDOW_SPACING_LEFT, nextOffset, 0, 0);
                Point textOrigin = new Point(WINDOW_SPACING_LEFT + WINDOW_ITEM_MARGIN_LEFT + maxLeftIndent, nextOffset);
                item.map(bounds, textOrigin);
            }
        }
        int height = nextOffset + WINDOW_SPACING_BOTTOM;
        MappingData mappingData = new MappingData(itemVector, captionRect, new Dimension(width, height), maxLeftIndent, shortcutOrigin, rightMarkOrigin);
        return mappingData;
    }
    protected Rectangle getSubmenuBounds(Rectangle itemBounds, Dimension windowSize) {
        Rectangle globalBounds = toGlobal(itemBounds);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Rectangle res;
        res = fitWindowRight(globalBounds, windowSize, screenSize);
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
        res = fitWindowLeft(globalBounds, windowSize, screenSize);
        if (res != null) {
            return res;
        }
        return fitWindowToScreen(windowSize, screenSize);
   }
    protected void updateSize() {
        resetMapping();
        if (isShowing()) {
            XToolkit.executeOnEventHandlerThread(target, new Runnable() {
                    public void run() {
                        Dimension dim = getDesiredSize();
                        reshape(x, y, dim.width, dim.height);
                    }
                });
        }
    }
    protected Dimension getCaptionSize() {
        return null;
    }
    protected void paintCaption(Graphics g, Rectangle rect) {
    }
    XMenuPeer getMenuPeer() {
        return menuPeer;
    }
    Vector getMenuTargetItems() {
        return menuPeer.getTargetItems();
    }
    Dimension getDesiredSize() {
        MappingData mappingData = (MappingData)getMappingData();
        return mappingData.getDesiredSize();
    }
    boolean isCreated() {
        return getWindow() != 0;
    }
    boolean ensureCreated() {
        if (!isCreated()) {
            XCreateWindowParams params = getDelayedParams();
            params.remove(DELAYED);
            params.add(OVERRIDE_REDIRECT, Boolean.TRUE);
            params.add(XWindow.TARGET, target);
            init(params);
        }
        return true;
    }
    void show(Rectangle bounds) {
        if (!isCreated()) {
            return;
        }
        if (log.isLoggable(PlatformLogger.FINER)) {
            log.finer("showing menu window + " + getWindow() + " at " + bounds);
        }
        XToolkit.awtLock();
        try {
            reshape(bounds.x, bounds.y, bounds.width, bounds.height);
            xSetVisible(true);
            toFront();
            selectItem(getFirstSelectableItem(), false);
        } finally {
            XToolkit.awtUnlock();
        }
    }
    void hide() {
        selectItem(null, false);
        xSetVisible(false);
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
        paintCaption(g, mappingData.getCaptionRect());
        XMenuItemPeer[] itemVector = mappingData.getItems();
        Dimension windowSize =  mappingData.getDesiredSize();
        XMenuItemPeer selectedItem = getSelectedItem();
        for (int i = 0; i < itemVector.length; i++) {
            XMenuItemPeer item = itemVector[i];
            XMenuItemPeer.TextMetrics metrics = item.getTextMetrics();
            Rectangle bounds = item.getBounds();
            if (item.isSeparator()) {
                draw3DRect(g, bounds.x, bounds.y + bounds.height / 2,  bounds.width, 2, false);
            } else {
                g.setFont(item.getTargetFont());
                Point textOrigin = item.getTextOrigin();
                Dimension textDim = metrics.getTextDimension();
                if (item == selectedItem) {
                    g.setColor(getSelectedColor());
                    g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
                    draw3DRect(g, bounds.x, bounds.y, bounds.width, bounds.height, false);
                }
                g.setColor(item.isTargetItemEnabled() ? getForegroundColor() : getDisabledColor());
                g.drawString(item.getTargetLabel(), textOrigin.x, textOrigin.y);
                String shortcutText = item.getShortcutText();
                if (shortcutText != null) {
                    g.drawString(shortcutText, mappingData.getShortcutOrigin(), textOrigin.y);
                }
                if (item instanceof XMenuPeer) {
                    int markWidth = textDim.height * 4 / 5;
                    int markHeight = textDim.height * 4 / 5;
                    int markX = bounds.x + bounds.width - markWidth - WINDOW_SPACING_RIGHT - WINDOW_ITEM_MARGIN_RIGHT;
                    int markY = bounds.y + (bounds.height - markHeight) / 2;
                    g.setColor(item.isTargetItemEnabled() ? getDarkShadowColor() : getDisabledColor());
                    g.drawLine(markX, markY + markHeight, markX + markWidth, markY + markHeight / 2);
                    g.setColor(item.isTargetItemEnabled() ? getLightShadowColor() : getDisabledColor());
                    g.drawLine(markX, markY, markX + markWidth, markY + markHeight / 2);
                    g.drawLine(markX, markY, markX, markY + markHeight);
                } else if (item instanceof XCheckboxMenuItemPeer) {
                    int markWidth = textDim.height * 4 / 5;
                    int markHeight = textDim.height * 4 / 5;
                    int markX = WINDOW_SPACING_LEFT + WINDOW_ITEM_MARGIN_LEFT;
                    int markY = bounds.y + (bounds.height - markHeight) / 2;
                    boolean checkState = ((XCheckboxMenuItemPeer)item).getTargetState();
                    if (checkState) {
                        g.setColor(getSelectedColor());
                        g.fillRect(markX, markY, markWidth, markHeight);
                        draw3DRect(g, markX, markY, markWidth, markHeight, false);
                        int[] px = new int[CHECKMARK_X.length];
                        int[] py = new int[CHECKMARK_X.length];
                        for (int j = 0; j < CHECKMARK_X.length; j++) {
                            px[j] = markX + CHECKMARK_X[j] * markWidth / CHECKMARK_SIZE;
                            py[j] = markY + CHECKMARK_Y[j] * markHeight / CHECKMARK_SIZE;
                        }
                        g.setColor(item.isTargetItemEnabled() ? getForegroundColor() : getDisabledColor());
                        g.fillPolygon(px, py, CHECKMARK_X.length);
                    } else {
                        g.setColor(getBackgroundColor());
                        g.fillRect(markX, markY, markWidth, markHeight);
                        draw3DRect(g, markX, markY, markWidth, markHeight, true);
                    }
                }
            }
        }
        flush();
    }
}
