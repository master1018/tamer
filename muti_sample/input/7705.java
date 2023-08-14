public class ListHelper implements XScrollbarClient {
    private static final PlatformLogger log = PlatformLogger.getLogger("sun.awt.X11.ListHelper");
    private final int FOCUS_INSET = 1;
    private final int BORDER_WIDTH; 
    private final int ITEM_MARGIN;  
    private final int TEXT_SPACE;   
    private final int SCROLLBAR_WIDTH;  
    private java.util.List items;        
    private java.util.List selected;     
    private boolean multiSelect;         
    private int focusedIndex;
    private int maxVisItems;             
    private XVerticalScrollbar vsb;      
    private boolean vsbVis;
    private XHorizontalScrollbar hsb;    
    private boolean hsbVis;
    private Font font;
    private FontMetrics fm;
    private XWindow peer;   
    private Color[] colors; 
    boolean mouseDraggedOutVertically = false;
    private volatile boolean vsbVisibilityChanged = false;
    public ListHelper(XWindow peer,
                      Color[] colors,
                      int initialSize,
                      boolean multiSelect,
                      boolean scrollVert,
                      boolean scrollHoriz,
                      Font font,
                      int maxVisItems,
                      int SPACE,
                      int MARGIN,
                      int BORDER,
                      int SCROLLBAR) {
        this.peer = peer;
        this.colors = colors;
        this.multiSelect = multiSelect;
        items = new ArrayList(initialSize);
        selected = new ArrayList(1);
        selected.add(Integer.valueOf(-1));
        this.maxVisItems = maxVisItems;
        if (scrollVert) {
            vsb = new XVerticalScrollbar(this);
            vsb.setValues(0, 0, 0, 0, 1, maxVisItems - 1);
        }
        if (scrollHoriz) {
            hsb = new XHorizontalScrollbar(this);
            hsb.setValues(0, 0, 0, 0, 1, 1);
        }
        setFont(font);
        TEXT_SPACE = SPACE;
        ITEM_MARGIN = MARGIN;
        BORDER_WIDTH = BORDER;
        SCROLLBAR_WIDTH = SCROLLBAR;
    }
    public Component getEventSource() {
        return peer.getEventSource();
    }
    public void add(String item) {
        items.add(item);
        updateScrollbars();
    }
    public void add(String item, int index) {
        items.add(index, item);
        updateScrollbars();
    }
    public void remove(String item) {
        items.remove(item);
        updateScrollbars();
    }
    public void remove(int index) {
        items.remove(index);
        updateScrollbars();
    }
    public void removeAll() {
        items.removeAll(items);
        updateScrollbars();
    }
    public void setMultiSelect(boolean ms) {
        multiSelect = ms;
    }
    public void select(int index) {
        if (index > getItemCount() - 1) {
            index = (isEmpty() ? -1 : 0);
        }
        if (multiSelect) {
            assert false : "Implement ListHelper.select() for multiselect";
        }
        else if (getSelectedIndex() != index) {
            selected.remove(0);
            selected.add(Integer.valueOf(index));
            makeVisible(index);
        }
    }
    public void deselect(int index) {
        assert(false);
    }
    public int getSelectedIndex() {
        if (!multiSelect) {
            Integer val = (Integer)selected.get(0);
            return val.intValue();
        }
        return -1;
    }
    int[] getSelectedIndexes() { assert(false); return null;}
    public boolean checkVsbVisibilityChangedAndReset(){
        boolean returnVal = vsbVisibilityChanged;
        vsbVisibilityChanged = false;
        return returnVal;
    }
    public boolean isEmpty() {
        return items.isEmpty();
    }
    public int getItemCount() {
        return items.size();
    }
    public String getItem(int index) {
        return (String) items.get(index);
    }
    public void setFocusedIndex(int index) {
        focusedIndex = index;
    }
    public boolean isFocusedIndex(int index) {
        return index == focusedIndex;
    }
    public void setFont(Font newFont) {
        if (newFont != font) {
            font = newFont;
            fm = Toolkit.getDefaultToolkit().getFontMetrics(font);
        }
    }
    public int getMaxItemWidth() {
        int m = 0;
        int end = getItemCount();
        for(int i = 0 ; i < end ; i++) {
            int l = fm.stringWidth(getItem(i));
            m = Math.max(m, l);
        }
        return m;
    }
    int getItemHeight() {
        return fm.getHeight() + (2*TEXT_SPACE);
    }
    public int y2index(int y) {
        if (log.isLoggable(PlatformLogger.FINE)) {
            log.fine("y=" + y +", firstIdx=" + firstDisplayedIndex() +", itemHeight=" + getItemHeight()
                     + ",item_margin=" + ITEM_MARGIN);
        }
        int newIdx = firstDisplayedIndex() + ((y - 2*ITEM_MARGIN) / (getItemHeight() + 2*ITEM_MARGIN));
        return newIdx;
    }
    public int firstDisplayedIndex() {
        if (vsbVis) {
            return vsb.getValue();
        }
        return 0;
    }
    public int lastDisplayedIndex() {
        if (hsbVis) {
            assert false : "Implement for horiz scroll bar";
        }
        return vsbVis ? vsb.getValue() + maxVisItems - 1: getItemCount() - 1;
    }
    public void makeVisible(int index) {
        if (vsbVis) {
            if (index < firstDisplayedIndex()) {
                vsb.setValue(index);
            }
            else if (index > lastDisplayedIndex()) {
                vsb.setValue(index - maxVisItems + 1);
            }
        }
    }
    public void up() {
        int curIdx = getSelectedIndex();
        int numItems = getItemCount();
        int newIdx;
        assert curIdx >= 0;
        if (curIdx == 0) {
            newIdx = numItems - 1;
        }
        else {
            newIdx = --curIdx;
        }
        select(newIdx);
    }
    public void down() {
        int newIdx = (getSelectedIndex() + 1) % getItemCount();
        select(newIdx);
    }
    public void pageUp() {
        if (vsbVis && firstDisplayedIndex() > 0) {
            if (multiSelect) {
                assert false : "Implement pageUp() for multiSelect";
            }
            else {
                int selectionOffset = getSelectedIndex() - firstDisplayedIndex();
                int newIdx = firstDisplayedIndex() - vsb.getBlockIncrement();
                vsb.setValue(newIdx);
                select(firstDisplayedIndex() + selectionOffset);
            }
        }
    }
    public void pageDown() {
        if (vsbVis && lastDisplayedIndex() < getItemCount() - 1) {
            if (multiSelect) {
                assert false : "Implement pageDown() for multiSelect";
            }
            else {
                int selectionOffset = getSelectedIndex() - firstDisplayedIndex();
                int newIdx = lastDisplayedIndex();
                vsb.setValue(newIdx);
                select(firstDisplayedIndex() + selectionOffset);
            }
        }
    }
    public void home() {}
    public void end() {}
    public boolean isVSBVisible() { return vsbVis; }
    public boolean isHSBVisible() { return hsbVis; }
    public XVerticalScrollbar getVSB() { return vsb; }
    public XHorizontalScrollbar getHSB() { return hsb; }
    public boolean isInVertSB(Rectangle bounds, int x, int y) {
        if (vsbVis) {
            assert vsb != null : "Vert scrollbar is visible, yet is null?";
            int sbHeight = hsbVis ? bounds.height - SCROLLBAR_WIDTH : bounds.height;
            return (x <= bounds.width) &&
                   (x >= bounds.width - SCROLLBAR_WIDTH) &&
                   (y >= 0) &&
                   (y <= sbHeight);
        }
        return false;
    }
    public boolean isInHorizSB(Rectangle bounds, int x, int y) {
        if (hsbVis) {
            assert hsb != null : "Horiz scrollbar is visible, yet is null?";
            int sbWidth = vsbVis ? bounds.width - SCROLLBAR_WIDTH : bounds.width;
            return (x <= sbWidth) &&
                   (x >= 0) &&
                   (y >= bounds.height - SCROLLBAR_WIDTH) &&
                   (y <= bounds.height);
        }
        return false;
    }
    public void handleVSBEvent(MouseEvent e, Rectangle bounds, int x, int y) {
        int sbHeight = hsbVis ? bounds.height - SCROLLBAR_WIDTH : bounds.height;
        vsb.handleMouseEvent(e.getID(),
                             e.getModifiers(),
                             x - (bounds.width - SCROLLBAR_WIDTH),
                             y);
    }
    void updateScrollbars() {
        boolean oldVsbVis = vsbVis;
        vsbVis = vsb != null && items.size() > maxVisItems;
        if (vsbVis) {
            vsb.setValues(vsb.getValue(), getNumItemsDisplayed(),
                          vsb.getMinimum(), items.size());
        }
        vsbVisibilityChanged = (vsbVis != oldVsbVis);
    }
    public int getNumItemsDisplayed() {
        return items.size() > maxVisItems ? maxVisItems : items.size();
    }
    public void repaintScrollbarRequest(XScrollbar sb) {
        Graphics g = peer.getGraphics();
        Rectangle bounds = peer.getBounds();
        if ((sb == vsb) && vsbVis) {
            paintVSB(g, XComponentPeer.getSystemColors(), bounds);
        }
        else if ((sb == hsb) && hsbVis) {
            paintHSB(g, XComponentPeer.getSystemColors(), bounds);
        }
        g.dispose();
    }
    public void notifyValue(XScrollbar obj, int type, int v, boolean isAdjusting) {
        if (obj == vsb) {
            int oldScrollValue = vsb.getValue();
            vsb.setValue(v);
            boolean needRepaint = (oldScrollValue != vsb.getValue());
            if (mouseDraggedOutVertically){
                int oldItemValue = getSelectedIndex();
                int newItemValue = getSelectedIndex() + v - oldScrollValue;
                select(newItemValue);
                needRepaint = needRepaint || (getSelectedIndex() != oldItemValue);
            }
            Graphics g = peer.getGraphics();
            Rectangle bounds = peer.getBounds();
            int first = v;
            int last = Math.min(getItemCount() - 1,
                                v + maxVisItems);
            if (needRepaint) {
                paintItems(g, colors, bounds, first, last);
            }
            g.dispose();
        }
        else if ((XHorizontalScrollbar)obj == hsb) {
            hsb.setValue(v);
        }
    }
    public void updateColors(Color[] newColors) {
        colors = newColors;
    }
    public void paintItems(Graphics g,
                           Color[] colors,
                           Rectangle bounds) {
    }
    public void paintAllItems(Graphics g,
                           Color[] colors,
                           Rectangle bounds) {
        paintItems(g, colors, bounds,
                   firstDisplayedIndex(), lastDisplayedIndex());
    }
    public void paintItems(Graphics g,
                           Color[] colors,
                           Rectangle bounds,
                           int first,
                           int last) {
        peer.flush();
        int x = BORDER_WIDTH + ITEM_MARGIN;
        int width = bounds.width - 2*ITEM_MARGIN - 2*BORDER_WIDTH - (vsbVis ? SCROLLBAR_WIDTH : 0);
        int height = getItemHeight();
        int y = BORDER_WIDTH + ITEM_MARGIN;
        for (int i = first; i <= last ; i++) {
            paintItem(g, colors, getItem(i),
                      x, y, width, height,
                      isItemSelected(i),
                      isFocusedIndex(i));
            y += height + 2*ITEM_MARGIN;
        }
        if (vsbVis) {
            paintVSB(g, XComponentPeer.getSystemColors(), bounds);
        }
        if (hsbVis) {
            paintHSB(g, XComponentPeer.getSystemColors(), bounds);
        }
        peer.flush();
    }
    public void paintItem(Graphics g,
                          Color[] colors,
                          String string,
                          int x, int y, int width, int height,
                          boolean selected,
                          boolean focused) {
        if (selected) {
            g.setColor(colors[XComponentPeer.FOREGROUND_COLOR]);
        }
        else {
            g.setColor(colors[XComponentPeer.BACKGROUND_COLOR]);
        }
        g.fillRect(x, y, width, height);
        if (focused) {
            g.setColor(Color.BLACK);
            g.drawRect(x + FOCUS_INSET,
                       y + FOCUS_INSET,
                       width - 2*FOCUS_INSET,
                       height - 2*FOCUS_INSET);
        }
        if (selected) {
            g.setColor(colors[XComponentPeer.BACKGROUND_COLOR]);
        }
        else {
            g.setColor(colors[XComponentPeer.FOREGROUND_COLOR]);
        }
        g.setFont(font);
        int fontAscent = fm.getAscent();
        int fontDescent = fm.getDescent();
        g.drawString(string, x + TEXT_SPACE, y + (height + fm.getMaxAscent() - fm.getMaxDescent())/2);
    }
    boolean isItemSelected(int index) {
        Iterator itr = selected.iterator();
        while (itr.hasNext()) {
            Integer val = (Integer)itr.next();
            if (val.intValue() == index) {
                return true;
            }
        }
        return false;
    }
    public void paintVSB(Graphics g, Color colors[], Rectangle bounds) {
        int height = bounds.height - 2*BORDER_WIDTH - (hsbVis ? (SCROLLBAR_WIDTH-2) : 0);
        Graphics ng = g.create();
        g.setColor(colors[XComponentPeer.BACKGROUND_COLOR]);
        try {
            ng.translate(bounds.width - BORDER_WIDTH - SCROLLBAR_WIDTH,
                         BORDER_WIDTH);
            vsb.setSize(SCROLLBAR_WIDTH, bounds.height);
            vsb.paint(ng, colors, true);
        } finally {
            ng.dispose();
        }
    }
    public void paintHSB(Graphics g, Color colors[], Rectangle bounds) {
    }
    static boolean doWheelScroll(XVerticalScrollbar vsb,
                                     XHorizontalScrollbar hsb,
                                     MouseWheelEvent e) {
        XScrollbar scroll = null;
        int wheelRotation;
        if (vsb != null) {
            scroll = vsb;
        }
        else if (hsb != null) {
            scroll = hsb;
        }
        else { 
            return false;
        }
        wheelRotation = e.getWheelRotation();
        if ((wheelRotation < 0 && scroll.getValue() > scroll.getMinimum()) ||
            (wheelRotation > 0 && scroll.getValue() < scroll.getMaximum()) ||
            wheelRotation != 0) {
            int type = e.getScrollType();
            int incr;
            if (type == MouseWheelEvent.WHEEL_BLOCK_SCROLL) {
                incr = wheelRotation * scroll.getBlockIncrement();
            }
            else { 
                incr = e.getUnitsToScroll() * scroll.getUnitIncrement();
            }
            scroll.setValue(scroll.getValue() + incr);
            return true;
        }
        return false;
    }
    void trackMouseDraggedScroll(int mouseX, int mouseY, int listWidth, int listHeight){
        if (!mouseDraggedOutVertically){
            if (vsb.beforeThumb(mouseX, mouseY)) {
                vsb.setMode(AdjustmentEvent.UNIT_DECREMENT);
            } else {
                vsb.setMode(AdjustmentEvent.UNIT_INCREMENT);
            }
        }
        if(!mouseDraggedOutVertically && (mouseY < 0 || mouseY >= listHeight)){
            mouseDraggedOutVertically = true;
            vsb.startScrollingInstance();
        }
        if (mouseDraggedOutVertically && mouseY >= 0 && mouseY < listHeight && mouseX >= 0 && mouseX < listWidth){
            mouseDraggedOutVertically = false;
            vsb.stopScrollingInstance();
        }
    }
    void trackMouseReleasedScroll(){
        if (mouseDraggedOutVertically){
            mouseDraggedOutVertically = false;
            vsb.stopScrollingInstance();
        }
    }
}
