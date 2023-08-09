class XListPeer extends XComponentPeer implements ListPeer, XScrollbarClient {
    private static final PlatformLogger log = PlatformLogger.getLogger("sun.awt.X11.XListPeer");
    public final static int     MARGIN = 2;
    public final static int     SPACE = 1;
    public final static int     SCROLLBAR_AREA = 17;  
    public final static int     SCROLLBAR_WIDTH = 13; 
    public final static int     NONE = -1;
    public final static int     WINDOW = 0;
    public final static int     VERSCROLLBAR = 1;
    public final static int     HORSCROLLBAR = 2;
    public final static int     DEFAULT_VISIBLE_ROWS = 4; 
    public final static int     HORIZ_SCROLL_AMT = 10;
    private final static int    PAINT_VSCROLL = 2;
    private final static int    PAINT_HSCROLL = 4;
    private final static int    PAINT_ITEMS = 8;
    private final static int    PAINT_FOCUS = 16;
    private final static int    PAINT_BACKGROUND = 32;
    private final static int    PAINT_HIDEFOCUS = 64;
    private final static int    PAINT_ALL =
        PAINT_VSCROLL | PAINT_HSCROLL | PAINT_ITEMS | PAINT_FOCUS | PAINT_BACKGROUND;
    private final static int    COPY_AREA = 128;
    XVerticalScrollbar       vsb;
    XHorizontalScrollbar     hsb;
    ListPainter painter;
    Vector                      items;
    boolean                     multipleSelections;
    int                         active = NONE;
    int                         selected[];
    int                         fontHeight;
    int                         fontAscent;
    int                         fontLeading;
    int                         currentIndex = -1;
    int                         eventIndex = -1;
    int                         eventType = NONE;
    int                         focusIndex;
    int                         maxLength;
    boolean                     vsbVis;  
    boolean                     hsbVis;
    int                         listWidth;  
    int                         listHeight; 
    private int firstTimeVisibleIndex = 0;
    boolean bgColorSet;
    boolean fgColorSet;
    boolean mouseDraggedOutHorizontally = false;
    boolean mouseDraggedOutVertically = false;
    boolean isScrollBarOriginated = false;
    boolean isMousePressed = false;
    XListPeer(List target) {
        super(target);
    }
    public void preInit(XCreateWindowParams params) {
        super.preInit(params);
        items = new Vector();
        createVerScrollbar();
        createHorScrollbar();
        painter = new ListPainter();
        bgColorSet = target.isBackgroundSet();
        fgColorSet = target.isForegroundSet();
    }
    public void postInit(XCreateWindowParams params) {
        super.postInit(params);
        initFontMetrics();
        List l = (List)target;
        int stop = l.getItemCount();
        for (int i = 0 ; i < stop; i++) {
            items.addElement(l.getItem(i));
        }
        int index = l.getVisibleIndex();
        if (index >= 0) {
            vsb.setValues(index, 0, 0, items.size());
        }
        maxLength = maxLength();
        int sel[] = l.getSelectedIndexes();
        selected = new int[sel.length];
        for (int i = 0 ; i < sel.length ; i ++) {
            selected[i] = sel[i];
        }
        if (sel.length > 0) {
            setFocusIndex(sel[sel.length - 1]);
        }
        else {
            setFocusIndex(0);
        }
        multipleSelections = l.isMultipleMode();
    }
    void createVerScrollbar() {
        vsb = new XVerticalScrollbar(this);
        vsb.setValues(0, 0, 0, 0, 1, 1);
    }
    void createHorScrollbar() {
        hsb = new XHorizontalScrollbar(this);
        hsb.setValues(0, 0, 0, 0, HORIZ_SCROLL_AMT, HORIZ_SCROLL_AMT);
    }
    public void add(String item, int index) {
        addItem(item, index);
    }
    public void removeAll() {
        clear();
        maxLength = 0;
    }
    public void setMultipleMode (boolean b) {
        setMultipleSelections(b);
    }
    public Dimension getPreferredSize(int rows) {
        return preferredSize(rows);
    }
    public Dimension getMinimumSize(int rows) {
        return minimumSize(rows);
    }
    public Dimension minimumSize() {
        return minimumSize(DEFAULT_VISIBLE_ROWS);
    }
    public Dimension preferredSize(int v) {
        return minimumSize(v);
    }
    public Dimension minimumSize(int v) {
        FontMetrics fm = getFontMetrics(getFont());
        initFontMetrics();
        return new Dimension(20 + fm.stringWidth("0123456789abcde"),
                             getItemHeight() * v + (2*MARGIN));
    }
    void initFontMetrics() {
        FontMetrics fm = getFontMetrics(getFont());
        fontHeight = fm.getHeight();
        fontAscent = fm.getAscent();
        fontLeading = fm.getLeading();
    }
    int maxLength() {
        FontMetrics fm = getFontMetrics(getFont());
        int m = 0;
        int end = items.size();
        for(int i = 0 ; i < end ; i++) {
            int l = fm.stringWidth(((String)items.elementAt(i)));
            m = Math.max(m, l);
        }
        return m;
    }
    int getItemWidth(int i) {
        FontMetrics fm = getFontMetrics(getFont());
        return fm.stringWidth((String)items.elementAt(i));
    }
    int stringLength(String str) {
        FontMetrics fm = getFontMetrics(target.getFont());
        return fm.stringWidth(str);
    }
    public void setForeground(Color c) {
        fgColorSet = true;
        super.setForeground(c);
    }
    public void setBackground(Color c) {
        bgColorSet = true;
        super.setBackground(c);
    }
    private Color getListBackground(Color[] colors) {
        if (bgColorSet) {
            return colors[BACKGROUND_COLOR];
        }
        else {
            return SystemColor.text;
        }
    }
    private Color getListForeground(Color[] colors) {
        if (fgColorSet) {
            return colors[FOREGROUND_COLOR];
        }
        else {
            return SystemColor.textText;
        }
    }
    Rectangle getVScrollBarRec() {
        return new Rectangle(width - (SCROLLBAR_WIDTH), 0, SCROLLBAR_WIDTH+1, height);
    }
    Rectangle getHScrollBarRec() {
        return new Rectangle(0, height - SCROLLBAR_WIDTH, width, SCROLLBAR_WIDTH);
    }
    int getFirstVisibleItem() {
        if (vsbVis) {
            return vsb.getValue();
        } else {
            return 0;
        }
    }
    int getLastVisibleItem() {
        if (vsbVis) {
            return Math.min(items.size()-1, vsb.getValue() + itemsInWindow() -1);
        } else {
            return Math.min(items.size()-1, itemsInWindow()-1);
        }
    }
    public void repaintScrollbarRequest(XScrollbar scrollbar) {
        Graphics g = getGraphics();
        if (scrollbar == hsb)  {
            repaint(PAINT_HSCROLL);
        }
        else if (scrollbar == vsb) {
            repaint(PAINT_VSCROLL);
        }
    }
    public void repaint() {
        repaint(getFirstVisibleItem(), getLastVisibleItem(), PAINT_ALL);
    }
    private void repaint(int options) {
        repaint(getFirstVisibleItem(), getLastVisibleItem(), options);
    }
    private void repaint(int firstItem, int lastItem, int options) {
        repaint(firstItem, lastItem, options, null, null);
    }
    private void repaint(int firstItem, int lastItem, int options, Rectangle source, Point distance) {
        Graphics g = getGraphics();
        try {
            painter.paint(g, firstItem, lastItem, options, source, distance);
        } finally {
            g.dispose();
        }
    }
    public void paint(Graphics g) {
        painter.paint(g, getFirstVisibleItem(), getLastVisibleItem(), PAINT_ALL);
    }
    public boolean isFocusable() { return true; }
    public void focusGained(FocusEvent e) {
        super.focusGained(e);
        repaint(PAINT_FOCUS);
    }
    public void focusLost(FocusEvent e) {
        super.focusLost(e);
        repaint(PAINT_FOCUS);
    }
    public void layout() {
        int vis, maximum;
        boolean vsbWasVisible;
        int origVSBVal;
        assert(target != null);
        origVSBVal = vsb.getValue();
        vis = itemsInWindow(false);
        maximum = items.size() < vis ? vis : items.size();
        vsb.setValues(vsb.getValue(), vis, vsb.getMinimum(), maximum);
        vsbVis = vsbWasVisible = vsbIsVisible(false);
        listHeight = height;
        listWidth = getListWidth();
        vis = listWidth - ((2 * SPACE) + (2 * MARGIN));
        maximum = maxLength < vis ? vis : maxLength;
        hsb.setValues(hsb.getValue(), vis, hsb.getMinimum(), maximum);
        hsbVis = hsbIsVisible(vsbVis);
        if (hsbVis) {
            listHeight = height - SCROLLBAR_AREA;
            vis = itemsInWindow(true);
            maximum = items.size() < vis ? vis : items.size();
            vsb.setValues(origVSBVal, vis, vsb.getMinimum(), maximum);
            vsbVis = vsbIsVisible(true);
        }
        if (vsbWasVisible != vsbVis) {
            listWidth = getListWidth();
            vis = listWidth - ((2 * SPACE) + (2 * MARGIN));
            maximum = maxLength < vis ? 0 : maxLength;
            hsb.setValues(hsb.getValue(), vis, hsb.getMinimum(), maximum);
            hsbVis = hsbIsVisible(vsbVis);
        }
        vsb.setSize(SCROLLBAR_WIDTH, listHeight);
        hsb.setSize(listWidth, SCROLLBAR_WIDTH);
        vsb.setBlockIncrement(itemsInWindow());
        hsb.setBlockIncrement(width - ((2 * SPACE) + (2 * MARGIN) + (vsbVis ? SCROLLBAR_AREA : 0)));
    }
    int getItemWidth() {
        return width - ((2 * MARGIN) + (vsbVis ? SCROLLBAR_AREA : 0));
    }
    int getItemHeight() {
        return (fontHeight - fontLeading) + (2*SPACE);
    }
    int getItemX() {
        return MARGIN + SPACE;
    }
    int getItemY(int item) {
        return index2y(item);
    }
    int getFocusIndex() {
        return focusIndex;
    }
    void setFocusIndex(int value) {
        focusIndex = value;
    }
    Rectangle getFocusRect() {
        Rectangle focusRect = new Rectangle();
        focusRect.x = 1;
        focusRect.width = getListWidth() - 3;
        if (isIndexDisplayed(getFocusIndex())) {
            focusRect.y = index2y(getFocusIndex()) - 2;
            focusRect.height = getItemHeight()+1;
        } else {
            focusRect.y = 1;
            focusRect.height = hsbVis ? height - SCROLLBAR_AREA : height;
            focusRect.height -= 3;
        }
        return focusRect;
    }
    public void handleConfigureNotifyEvent(XEvent xev) {
        super.handleConfigureNotifyEvent(xev);
        painter.invalidate();
    }
    public boolean handlesWheelScrolling() { return true; }
    void handleJavaMouseEvent(MouseEvent e) {
        super.handleJavaMouseEvent(e);
        int i = e.getID();
        switch (i) {
          case MouseEvent.MOUSE_PRESSED:
              mousePressed(e);
              break;
          case MouseEvent.MOUSE_RELEASED:
              mouseReleased(e);
              break;
          case MouseEvent.MOUSE_DRAGGED:
              mouseDragged(e);
              break;
        }
    }
    void handleJavaMouseWheelEvent(MouseWheelEvent e) {
        if (ListHelper.doWheelScroll(vsbVis ? vsb : null,
                                     hsbVis ? hsb : null, e)) {
            repaint();
        }
    }
    void mousePressed(MouseEvent mouseEvent) {
        if (log.isLoggable(PlatformLogger.FINER)) log.finer(mouseEvent.toString() + ", hsb " + hsbVis + ", vsb " + vsbVis);
        if (isEnabled() && mouseEvent.getButton() == MouseEvent.BUTTON1) {
            if (inWindow(mouseEvent.getX(), mouseEvent.getY())) {
                if (log.isLoggable(PlatformLogger.FINE)) log.fine("Mouse press in items area");
                active = WINDOW;
                int i = y2index(mouseEvent.getY());
                if (i >= 0) {
                    if (multipleSelections) {
                        if (isSelected(i)) {
                            deselectItem(i);
                            eventIndex = i;
                            eventType = ItemEvent.DESELECTED;
                        }
                        else {
                            selectItem(i);
                            eventIndex = i;
                            eventType = ItemEvent.SELECTED;
                        }
                    }
                    else {
                        selectItem(i);
                        eventIndex = i;
                        eventType = ItemEvent.SELECTED;
                    }
                    setFocusIndex(i);
                    repaint(PAINT_FOCUS);
                } else {
                    currentIndex = -1;
                }
            } else if (inVerticalScrollbar(mouseEvent.getX(), mouseEvent.getY())) {
                if (log.isLoggable(PlatformLogger.FINE)) log.fine("Mouse press in vertical scrollbar");
                active = VERSCROLLBAR;
                vsb.handleMouseEvent(mouseEvent.getID(),
                                     mouseEvent.getModifiers(),
                                     mouseEvent.getX() - (width - SCROLLBAR_WIDTH),
                                     mouseEvent.getY());
            } else if (inHorizontalScrollbar(mouseEvent.getX(), mouseEvent.getY())) {
                if (log.isLoggable(PlatformLogger.FINE)) log.fine("Mouse press in horizontal scrollbar");
                active = HORSCROLLBAR;
                hsb.handleMouseEvent(mouseEvent.getID(),
                                     mouseEvent.getModifiers(),
                                     mouseEvent.getX(),
                                     mouseEvent.getY() - (height - SCROLLBAR_WIDTH));
            }
            isMousePressed = true;
        }
    }
    void mouseReleased(MouseEvent mouseEvent) {
        if (isEnabled() && mouseEvent.getButton() == MouseEvent.BUTTON1) {
            int clickCount = mouseEvent.getClickCount();
            if (active == VERSCROLLBAR) {
                vsb.handleMouseEvent(mouseEvent.getID(),
                                     mouseEvent.getModifiers(),
                                     mouseEvent.getX()-(width-SCROLLBAR_WIDTH),
                                     mouseEvent.getY());
            } else if(active == HORSCROLLBAR) {
                hsb.handleMouseEvent(mouseEvent.getID(),
                                     mouseEvent.getModifiers(),
                                     mouseEvent.getX(),
                                     mouseEvent.getY()-(height-SCROLLBAR_WIDTH));
            } else if ( ( currentIndex >= 0 ) && ( clickCount >= 2 ) &&
                        ( clickCount % 2 == 0 ) ) {
                postEvent(new ActionEvent(target,
                                          ActionEvent.ACTION_PERFORMED,
                                          (String)items.elementAt(currentIndex),
                                          mouseEvent.getWhen(),
                                          mouseEvent.getModifiers()));  
            } else if (active == WINDOW) {
                trackMouseReleasedScroll();
                if (eventType == ItemEvent.DESELECTED) {
                    assert multipleSelections : "Shouldn't get a deselect for a single-select List";
                    deselectItem(eventIndex);
                }
                if (eventType != NONE) {
                    postEvent(new ItemEvent((List)target,
                                ItemEvent.ITEM_STATE_CHANGED,
                                Integer.valueOf(eventIndex),
                                eventType));
                }
            }
            active = NONE;
            eventIndex = -1;
            eventType = NONE;
            isMousePressed = false;
        }
    }
    void mouseDragged(MouseEvent mouseEvent) {
        if (isEnabled() &&
            (mouseEvent.getModifiersEx() & InputEvent.BUTTON1_DOWN_MASK) != 0) {
            if ((active == VERSCROLLBAR)) {
                vsb.handleMouseEvent(mouseEvent.getID(),
                                     mouseEvent.getModifiers(),
                                     mouseEvent.getX()-(width-SCROLLBAR_WIDTH),
                                     mouseEvent.getY());
            } else if ((active == HORSCROLLBAR)) {
                hsb.handleMouseEvent(mouseEvent.getID(),
                                     mouseEvent.getModifiers(),
                                     mouseEvent.getX(),
                                     mouseEvent.getY()-(height-SCROLLBAR_WIDTH));
            } else if (active == WINDOW) {
                int i = y2index(mouseEvent.getY());
                if (multipleSelections) {
                    if (eventType == ItemEvent.DESELECTED) {
                        if (i != eventIndex) {
                            eventType = NONE;
                            eventIndex = -1;
                        }
                    }
                }
                else if (eventType == ItemEvent.SELECTED) {
                    trackMouseDraggedScroll(mouseEvent);
                    if (i >= 0 && !isSelected(i)) {
                        int oldSel = eventIndex;
                        selectItem(i);
                        eventIndex = i;
                        repaint(oldSel, eventIndex, PAINT_ITEMS);
                    }
                }
                if (i >= 0) {
                    setFocusIndex(i);
                    repaint(PAINT_FOCUS);
                }
            }
        }
    }
    void trackMouseDraggedScroll(MouseEvent mouseEvent){
        if (vsb.beforeThumb(mouseEvent.getX(), mouseEvent.getY())) {
            vsb.setMode(AdjustmentEvent.UNIT_DECREMENT);
        } else {
            vsb.setMode(AdjustmentEvent.UNIT_INCREMENT);
        }
        if(mouseEvent.getY() < 0 || mouseEvent.getY() >= listHeight){
            if (!mouseDraggedOutVertically){
                mouseDraggedOutVertically = true;
                vsb.startScrollingInstance();
            }
        }else{
            if (mouseDraggedOutVertically){
                mouseDraggedOutVertically = false;
                vsb.stopScrollingInstance();
            }
        }
        if (hsb.beforeThumb(mouseEvent.getX(), mouseEvent.getY())) {
            hsb.setMode(AdjustmentEvent.UNIT_DECREMENT);
        } else {
            hsb.setMode(AdjustmentEvent.UNIT_INCREMENT);
        }
        if (mouseEvent.getX() < 0 || mouseEvent.getX() >= listWidth) {
            if (!mouseDraggedOutHorizontally){
                mouseDraggedOutHorizontally = true;
                hsb.startScrollingInstance();
            }
        }else{
            if (mouseDraggedOutHorizontally){
                mouseDraggedOutHorizontally = false;
                hsb.stopScrollingInstance();
            }
        }
    }
    void trackMouseReleasedScroll(){
        if (mouseDraggedOutVertically){
            mouseDraggedOutVertically = false;
            vsb.stopScrollingInstance();
        }
        if (mouseDraggedOutHorizontally){
            mouseDraggedOutHorizontally = false;
            hsb.stopScrollingInstance();
        }
    }
    void handleJavaKeyEvent(KeyEvent e) {
        switch(e.getID()) {
          case KeyEvent.KEY_PRESSED:
              if (!isMousePressed){
                  keyPressed(e);
              }
              break;
        }
    }
    void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (log.isLoggable(PlatformLogger.FINE)) log.fine(e.toString());
        switch(keyCode) {
          case KeyEvent.VK_UP:
          case KeyEvent.VK_KP_UP: 
              if (getFocusIndex() > 0) {
                  setFocusIndex(getFocusIndex()-1);
                  repaint(PAINT_HIDEFOCUS);
                  if (!multipleSelections) {
                      selectItem(getFocusIndex());
                      postEvent(new ItemEvent((List)target,
                                              ItemEvent.ITEM_STATE_CHANGED,
                                              Integer.valueOf(getFocusIndex()),
                                              ItemEvent.SELECTED));
                  }
                  if (isItemHidden(getFocusIndex())) {
                      makeVisible(getFocusIndex());
                  }
                  else {
                      repaint(PAINT_FOCUS);
                  }
              }
              break;
          case KeyEvent.VK_DOWN:
          case KeyEvent.VK_KP_DOWN: 
              if (getFocusIndex() < items.size() - 1) {
                  setFocusIndex(getFocusIndex()+1);
                  repaint(PAINT_HIDEFOCUS);
                  if (!multipleSelections) {
                      selectItem(getFocusIndex());
                      postEvent(new ItemEvent((List)target,
                                              ItemEvent.ITEM_STATE_CHANGED,
                                              Integer.valueOf(getFocusIndex()),
                                              ItemEvent.SELECTED));
                  }
                  if (isItemHidden(getFocusIndex())) {
                      makeVisible(getFocusIndex());
                  }
                  else {
                      repaint(PAINT_FOCUS);
                  }
              }
              break;
          case KeyEvent.VK_PAGE_UP: {
              int previousValue = vsb.getValue();
              vsb.setValue(vsb.getValue() - vsb.getBlockIncrement());
              int currentValue = vsb.getValue();
              if (previousValue!=currentValue) {
                  setFocusIndex(Math.max(getFocusIndex()-itemsInWindow(), 0));
                  if (!multipleSelections){
                      selectItem(getFocusIndex());
                      postEvent(new ItemEvent((List)target,
                                              ItemEvent.ITEM_STATE_CHANGED,
                                              Integer.valueOf(getFocusIndex()),
                                              ItemEvent.SELECTED));
                  }
              }
              repaint();
              break;
          }
          case KeyEvent.VK_PAGE_DOWN: {
              int previousValue = vsb.getValue();
              vsb.setValue(vsb.getValue() + vsb.getBlockIncrement());
              int currentValue = vsb.getValue();
              if (previousValue!=currentValue) {
                  setFocusIndex(Math.min(getFocusIndex() + itemsInWindow(), items.size()-1));
                  if (!multipleSelections){
                      selectItem(getFocusIndex());
                      postEvent(new ItemEvent((List)target,
                                              ItemEvent.ITEM_STATE_CHANGED,
                                              Integer.valueOf(getFocusIndex()),
                                              ItemEvent.SELECTED));
                  }
              }
              repaint();
              break;
          }
          case KeyEvent.VK_LEFT:
          case KeyEvent.VK_KP_LEFT:
              if (hsbVis & hsb.getValue() > 0) {
                  hsb.setValue(hsb.getValue() - HORIZ_SCROLL_AMT);
                  repaint();
              }
              break;
          case KeyEvent.VK_RIGHT:
          case KeyEvent.VK_KP_RIGHT:
              if (hsbVis) { 
                  hsb.setValue(hsb.getValue() + HORIZ_SCROLL_AMT);
                  repaint();
              }
              break;
          case KeyEvent.VK_HOME:
              if (!e.isControlDown() || ((List)target).getItemCount() <= 0)
                  break;
              if (vsbVis) {
                  vsb.setValue(vsb.getMinimum());
              }
              setFocusIndex(0);
              if (!multipleSelections) {
                  selectItem(getFocusIndex());
                  postEvent(new ItemEvent((List)target,
                                          ItemEvent.ITEM_STATE_CHANGED,
                                          Integer.valueOf(getFocusIndex()),
                                          ItemEvent.SELECTED));
              }
              repaint();
              break;
          case KeyEvent.VK_END:
              if (!e.isControlDown() || ((List)target).getItemCount() <= 0)
                  break;
              if (vsbVis) {
                  vsb.setValue(vsb.getMaximum());
              }
              setFocusIndex(items.size()-1);
              if (!multipleSelections) {
                  selectItem(getFocusIndex());
                  postEvent(new ItemEvent((List)target,
                                          ItemEvent.ITEM_STATE_CHANGED,
                                          Integer.valueOf(getFocusIndex()),
                                          ItemEvent.SELECTED));
              }
              repaint();
              break;
          case KeyEvent.VK_SPACE:
              if (getFocusIndex() < 0 || ((List)target).getItemCount() <= 0) {
                  break;
              }
              boolean isSelected = isSelected(getFocusIndex());
              if (multipleSelections && isSelected) {
                  deselectItem(getFocusIndex());
                  postEvent(new ItemEvent((List)target,
                                          ItemEvent.ITEM_STATE_CHANGED,
                                          Integer.valueOf(getFocusIndex()),
                                          ItemEvent.DESELECTED));
              }
              else if (!isSelected) { 
                  selectItem(getFocusIndex());
                  postEvent(new ItemEvent((List)target,
                                          ItemEvent.ITEM_STATE_CHANGED,
                                          Integer.valueOf(getFocusIndex()),
                                          ItemEvent.SELECTED));
              }
              break;
          case KeyEvent.VK_ENTER:
              if (selected.length > 0) {
                  postEvent(new ActionEvent((List)target,
                                            ActionEvent.ACTION_PERFORMED,
                                            (String)items.elementAt(getFocusIndex()),
                                            e.getWhen(),
                                            e.getModifiers()));  
              }
              break;
        }
    }
    public void notifyValue(XScrollbar obj, int type, int v, boolean isAdjusting) {
        if (log.isLoggable(PlatformLogger.FINE)) log.fine("Notify value changed on " + obj + " to " + v);
        int value = obj.getValue();
        if (obj == vsb) {
            scrollVertical(v - value);
            int oldSel = eventIndex;
            int newSel = eventIndex+v-value;
            if (mouseDraggedOutVertically && !isSelected(newSel)){
                selectItem(newSel);
                eventIndex = newSel;
                repaint(oldSel, eventIndex, PAINT_ITEMS);
                setFocusIndex(newSel);
                repaint(PAINT_FOCUS);
            }
        } else if ((XHorizontalScrollbar)obj == hsb) {
            scrollHorizontal(v - value);
        }
    }
    private void deselectAllItems() {
        selected = new int [0];
        repaint(PAINT_ITEMS);
    }
    public void setMultipleSelections(boolean v) {
        if (multipleSelections != v) {
            if ( !v) {
                int selPos = ( isSelected( focusIndex )) ? focusIndex: -1;
                deselectAllItems();
                if (selPos != -1){
                    selectItem(selPos);
                }
            }
            multipleSelections = v;
        }
    }
    public void addItem(String item, int i) {
        int oldMaxLength = maxLength;
        boolean hsbWasVis = hsbVis;
        boolean vsbWasVis = vsbVis;
        int addedIndex = 0; 
        if (i < 0 || i >= items.size()) {
            i = -1;
        }
        currentIndex = -1;
        if (i == -1) {
            items.addElement(item);
            i = 0;              
            addedIndex = items.size() - 1;
        } else {
            items.insertElementAt(item, i);
            addedIndex = i;
            for (int j = 0 ; j < selected.length ; j++) {
                if (selected[j] >= i) {
                    selected[j] += 1;
                }
            }
        }
        if (log.isLoggable(PlatformLogger.FINER)) log.finer("Adding item '" + item + "' to " + addedIndex);
        boolean repaintItems = !isItemHidden(addedIndex);
        maxLength = Math.max(maxLength, getItemWidth(addedIndex));
        layout();
        int options = 0;
        if (vsbVis != vsbWasVis || hsbVis != hsbWasVis) {
            options = PAINT_ALL;
        }
        else {
            options = (repaintItems ? (PAINT_ITEMS):0)
                | ((maxLength != oldMaxLength || (hsbWasVis ^ hsbVis))?(PAINT_HSCROLL):0)
                | ((vsb.needsRepaint())?(PAINT_VSCROLL):0);
        }
        if (log.isLoggable(PlatformLogger.FINEST)) log.finest("Last visible: " + getLastVisibleItem() +
                                                     ", hsb changed : " + (hsbWasVis ^ hsbVis) + ", items changed " + repaintItems);
        repaint(addedIndex, getLastVisibleItem(), options);
    }
    public void delItems(int s, int e) {
        boolean hsbWasVisible = hsbVis;
        boolean vsbWasVisible = vsbVis;
        int oldLastDisplayed = lastItemDisplayed();
        if (log.isLoggable(PlatformLogger.FINE)) log.fine("Deleting from " + s + " to " + e);
        if (log.isLoggable(PlatformLogger.FINEST)) log.finest("Last displayed item: " + oldLastDisplayed + ", items in window " + itemsInWindow() +
                                                     ", size " + items.size());
        if (items.size() == 0) {
            return;
        }
        if (s > e) {
            int tmp = s;
            s = e;
            e = tmp;
        }
        if (s < 0) {
            s = 0;
        }
        if (e >= items.size()) {
            e = items.size() - 1;
        }
        boolean repaintNeeded = (s >= getFirstVisibleItem() && s <= getLastVisibleItem());
        for (int i = s ; i <= e ; i++) {
            items.removeElementAt(s);
            int j = posInSel(i);
            if (j != -1) {
                int newsel[] = new int[selected.length - 1];
                System.arraycopy(selected, 0, newsel, 0, j);
                System.arraycopy(selected, j + 1, newsel, j, selected.length - (j + 1));
                selected = newsel;
            }
        }
        int diff = (e - s) + 1;
        for (int i = 0 ; i < selected.length ; i++) {
            if (selected[i] > e) {
                selected[i] -= diff;
            }
        }
        int options = PAINT_VSCROLL;
        if (getFocusIndex() > e) {
            setFocusIndex(getFocusIndex() - (e - s + 1));
            options |= PAINT_FOCUS;
        } else if (getFocusIndex() >= s && getFocusIndex() <= e) {
            int focusBound = (items.size() > 0) ? 0 : -1;
            setFocusIndex(Math.max(s-1, focusBound));
            options |= PAINT_FOCUS;
        }
        if (log.isLoggable(PlatformLogger.FINEST)) log.finest("Multiple selections: " + multipleSelections);
        if (vsb.getValue() >= s) {
            if (vsb.getValue() <= e) {
                vsb.setValue(e+1 - diff);
            } else {
                vsb.setValue(vsb.getValue() - diff);
            }
        }
        int oldMaxLength = maxLength;
        maxLength = maxLength();
        if (maxLength != oldMaxLength) {
            options |= PAINT_HSCROLL;
        }
        layout();
        repaintNeeded |= (vsbWasVisible ^ vsbVis) || (hsbWasVisible ^ hsbVis); 
        if (repaintNeeded) {
            options |= PAINT_ALL;
        }
        repaint(s, oldLastDisplayed, options);
    }
    public void select(int index) {
        setFocusIndex(index);
        repaint(PAINT_FOCUS);
        selectItem(index);
    }
    void selectItem(int index) {
        currentIndex = index;
        if (isSelected(index)) {
            return;
        }
        if (!multipleSelections) {
            if (selected.length == 0) { 
                selected = new int[1];
                selected[0] = index;
            }
            else {
                int oldSel = selected[0];
                selected[0] = index;
                if (!isItemHidden(oldSel)) {
                    repaint(oldSel, oldSel, PAINT_ITEMS);
                }
            }
        } else {
            int newsel[] = new int[selected.length + 1];
            int i = 0;
            while (i < selected.length && index > selected[i]) {
                newsel[i] = selected[i];
                i++;
            }
            newsel[i] = index;
            System.arraycopy(selected, i, newsel, i+1, selected.length - i);
            selected = newsel;
        }
        if (!isItemHidden(index)) {
            repaint(index, index, PAINT_ITEMS);
        }
    }
    public void deselect(int index) {
        deselectItem(index);
    }
    void deselectItem(int index) {
        if (!isSelected(index)) {
            return;
        }
        if (!multipleSelections) {
            selected = new int[0];
        } else {
            int i = posInSel(index);
            int newsel[] = new int[selected.length - 1];
            System.arraycopy(selected, 0, newsel, 0, i);
            System.arraycopy(selected, i+1, newsel, i, selected.length - (i+1));
            selected = newsel;
        }
        currentIndex = index;
        if (!isItemHidden(index)) {
            repaint(index, index, PAINT_ITEMS);
        }
    }
    public void makeVisible(int index) {
        if (index < 0 || index >= items.size()) {
            return;
        }
        if (isItemHidden(index)) {  
            if (index < vsb.getValue()) {
                scrollVertical(index - vsb.getValue());
            }
            else if (index > lastItemDisplayed()) {
                int val = index - lastItemDisplayed();
                scrollVertical(val);
            }
        }
    }
    public void clear() {
        selected = new int[0];
        items = new Vector();
        currentIndex = -1;
        setFocusIndex(-1);
        vsb.setValue(0);
        maxLength = 0;
        layout();
        repaint();
    }
    public int[] getSelectedIndexes() {
        return selected;
    }
    int index2y(int index) {
        int h = getItemHeight();
        return MARGIN + ((index - vsb.getValue()) * h) + SPACE;
    }
    boolean validY(int y) {
        int shown = itemsDisplayed();
        int lastY = shown * getItemHeight() + MARGIN;
        if (shown == itemsInWindow()) {
            lastY += MARGIN;
        }
        if (y < 0 || y >= lastY) {
            return false;
        }
        return true;
    }
    int posInSel(int index) {
        for (int i = 0 ; i < selected.length ; i++) {
            if (index == selected[i]) {
                return i;
            }
        }
        return -1;
    }
    boolean isIndexDisplayed(int idx) {
        int lastDisplayed = lastItemDisplayed();
        return idx <= lastDisplayed &&
            idx >= Math.max(0, lastDisplayed - itemsInWindow() + 1);
    }
    int lastItemDisplayed() {
        int n = itemsInWindow();
        return (Math.min(items.size() - 1, (vsb.getValue() + n) - 1));
    }
    boolean isItemHidden(int index) {
        return index < vsb.getValue() ||
            index >= vsb.getValue() + itemsInWindow();
    }
    int getListWidth() {
        return vsbVis ? width - SCROLLBAR_AREA : width;
    }
    int itemsDisplayed() {
        return (Math.min(items.size()-vsb.getValue(), itemsInWindow()));
    }
    void scrollVertical(int y) {
        if (log.isLoggable(PlatformLogger.FINE)) log.fine("Scrolling vertically by " + y);
        int itemsInWin = itemsInWindow();
        int h = getItemHeight();
        int pixelsToScroll = y * h;
        if (vsb.getValue() < -y) {
            y = -vsb.getValue();
        }
        vsb.setValue(vsb.getValue() + y);
        Rectangle source = null;
        Point distance = null;
        int firstItem = 0, lastItem = 0;
        int options = PAINT_HIDEFOCUS | PAINT_ITEMS | PAINT_VSCROLL | PAINT_FOCUS;
        if (y > 0) {
            if (y < itemsInWin) {
                source = new Rectangle(MARGIN, MARGIN + pixelsToScroll, width - SCROLLBAR_AREA, h * (itemsInWin - y - 1)-1);
                distance = new Point(0, -pixelsToScroll);
                options |= COPY_AREA;
            }
            firstItem = vsb.getValue() + itemsInWin - y - 1;
            lastItem = vsb.getValue() + itemsInWin - 1;
        } else if (y < 0) {
            if (y + itemsInWindow() > 0) {
                source = new Rectangle(MARGIN, MARGIN, width - SCROLLBAR_AREA, h * (itemsInWin + y));
                distance = new Point(0, -pixelsToScroll);
                options |= COPY_AREA;
            }
            firstItem = vsb.getValue();
            lastItem = Math.min(getLastVisibleItem(), vsb.getValue() + -y);
        }
        repaint(firstItem, lastItem, options, source, distance);
    }
    void scrollHorizontal(int x) {
        if (log.isLoggable(PlatformLogger.FINE)) log.fine("Scrolling horizontally by " + y);
        int w = getListWidth();
        w -= ((2 * SPACE) + (2 * MARGIN));
        int h = height - (SCROLLBAR_AREA + (2 * MARGIN));
        hsb.setValue(hsb.getValue() + x);
        int options = PAINT_ITEMS | PAINT_HSCROLL;
        Rectangle source = null;
        Point distance = null;
        if (x < 0) {
            source = new Rectangle(MARGIN + SPACE, MARGIN, w + x, h);
            distance = new Point(-x, 0);
            options |= COPY_AREA;
        } else if (x > 0) {
            source = new Rectangle(MARGIN + SPACE + x, MARGIN, w - x, h);
            distance = new Point(-x, 0);
            options |= COPY_AREA;
        }
        repaint(vsb.getValue(), lastItemDisplayed(), options, source, distance);
    }
    int y2index(int y) {
        if (!validY(y)) {
            return -1;
        }
        int i = (y - MARGIN) / getItemHeight() + vsb.getValue();
        int last = lastItemDisplayed();
        if (i > last) {
            i = last;
        }
        return i;
    }
    boolean isSelected(int index) {
        if (eventType == ItemEvent.SELECTED && index == eventIndex) {
            return true;
        }
        for (int i = 0 ; i < selected.length ; i++) {
            if (selected[i] == index) {
                return true;
            }
        }
        return false;
    }
    int itemsInWindow(boolean scrollbarVisible) {
        int h;
        if (scrollbarVisible) {
            h = height - ((2 * MARGIN) + SCROLLBAR_AREA);
        } else {
            h = height - 2*MARGIN;
        }
        return (h / getItemHeight());
    }
    int itemsInWindow() {
        return itemsInWindow(hsbVis);
    }
    boolean inHorizontalScrollbar(int x, int y) {
        int w = getListWidth();
        int h = height - SCROLLBAR_WIDTH;
        return (hsbVis &&  (x >= 0) && (x <= w) && (y > h));
    }
    boolean inVerticalScrollbar(int x, int y) {
        int w = width - SCROLLBAR_WIDTH;
        int h = hsbVis ? height - SCROLLBAR_AREA : height;
        return (vsbVis && (x > w) && (y >= 0) && (y <= h));
    }
    boolean inWindow(int x, int y) {
        int w = getListWidth();
        int h = hsbVis ? height - SCROLLBAR_AREA : height;
        return ((x >= 0) && (x <= w)) && ((y >= 0) && (y <= h));
    }
    boolean vsbIsVisible(boolean hsbVisible){
        return (items.size() > itemsInWindow(hsbVisible));
    }
    boolean hsbIsVisible(boolean vsbVisible){
        int w = width - ((2*SPACE) + (2*MARGIN) + (vsbVisible ? SCROLLBAR_AREA : 0));
        return (maxLength > w);
    }
    boolean prePostEvent(final AWTEvent e) {
        if (e instanceof MouseEvent) {
            return prePostMouseEvent((MouseEvent)e);
        }
        return super.prePostEvent(e);
    }
    boolean prePostMouseEvent(final MouseEvent me){
        if (getToplevelXWindow().isModalBlocked()) {
            return false;
        }
        int eventId = me.getID();
        if (eventId == MouseEvent.MOUSE_MOVED)
        {
        }else if((eventId == MouseEvent.MOUSE_DRAGGED ||
                  eventId == MouseEvent.MOUSE_RELEASED) &&
                 isScrollBarOriginated)
        {
            if (eventId == MouseEvent.MOUSE_RELEASED) {
                isScrollBarOriginated = false;
            }
            handleJavaMouseEventOnEDT(me);
            return true;
        }else if ((eventId == MouseEvent.MOUSE_PRESSED ||
                   eventId == MouseEvent.MOUSE_CLICKED) &&
                  (inVerticalScrollbar(me.getX(), me.getY()) ||
                   inHorizontalScrollbar(me.getX(), me.getY())))
        {
            if (eventId == MouseEvent.MOUSE_PRESSED) {
                isScrollBarOriginated = true;
            }
            handleJavaMouseEventOnEDT(me);
            return true;
        }
        return false;
    }
    void handleJavaMouseEventOnEDT(final MouseEvent me){
        EventQueue.invokeLater(new Runnable() {
                public void run() {
                    handleJavaMouseEvent(me);
                }
            });
    }
    public void setFont(Font f){
        super.setFont(f);
        initFontMetrics();
        layout();
        repaint();
    }
    class ListPainter {
        VolatileImage buffer;
        Color[] colors;
        private Color getListForeground() {
            if (fgColorSet) {
                return colors[FOREGROUND_COLOR];
            }
            else {
            return SystemColor.textText;
            }
        }
        private Color getListBackground() {
            if (bgColorSet) {
                return colors[BACKGROUND_COLOR];
            }
            else {
                return SystemColor.text;
            }
        }
        private Color getDisabledColor() {
            Color backgroundColor = getListBackground();
            Color foregroundColor = getListForeground();
            return (backgroundColor.equals(Color.BLACK)) ? foregroundColor.darker() : backgroundColor.darker();
        }
        private boolean createBuffer() {
            VolatileImage localBuffer = null;
            XToolkit.awtLock();
            try {
                localBuffer = buffer;
            } finally {
                XToolkit.awtUnlock();
            }
            if (localBuffer == null) {
                if (log.isLoggable(PlatformLogger.FINE)) log.fine("Creating buffer " + width + "x" + height);
                localBuffer =
                    graphicsConfig.createCompatibleVolatileImage(width+1,
                                                                 height+1);
            }
            XToolkit.awtLock();
            try {
                if (buffer == null) {
                    buffer = localBuffer;
                    return true;
                }
            } finally {
                XToolkit.awtUnlock();
            }
            return false;
        }
        public void invalidate() {
            XToolkit.awtLock();
            try {
                if (buffer != null) {
                    buffer.flush();
                }
                buffer = null;
            } finally {
                XToolkit.awtUnlock();
            }
        }
        private void paint(Graphics listG, int firstItem, int lastItem, int options) {
            paint(listG, firstItem, lastItem, options, null, null);
        }
        private void paint(Graphics listG, int firstItem, int lastItem, int options,
                           Rectangle source, Point distance) {
            if (log.isLoggable(PlatformLogger.FINER)) log.finer("Repaint from " + firstItem + " to " + lastItem + " options " + options);
            if (firstItem > lastItem) {
                int t = lastItem;
                lastItem = firstItem;
                firstItem = t;
            }
            if (firstItem < 0) {
                firstItem = 0;
            }
            colors = getGUIcolors();
            VolatileImage localBuffer = null;
            do {
                XToolkit.awtLock();
                try {
                    if (createBuffer()) {
                        options = PAINT_ALL;
                    }
                    localBuffer = buffer;
                } finally {
                    XToolkit.awtUnlock();
                }
                switch (localBuffer.validate(getGraphicsConfiguration())) {
                  case VolatileImage.IMAGE_INCOMPATIBLE:
                      invalidate();
                      options = PAINT_ALL;
                      continue;
                  case VolatileImage.IMAGE_RESTORED:
                      options = PAINT_ALL;
                }
                Graphics g = localBuffer.createGraphics();
                try {
                    g.setFont(getFont());
                    if ((options & (PAINT_HIDEFOCUS)) != 0) {
                        paintFocus(g, PAINT_HIDEFOCUS);
                    }
                    if ((options & COPY_AREA) != 0) {
                        g.copyArea(source.x, source.y, source.width, source.height,
                            distance.x, distance.y);
                    }
                    if ((options & PAINT_BACKGROUND) != 0) {
                        paintBackground(g);
                        firstItem = getFirstVisibleItem();
                        lastItem = getLastVisibleItem();
                    }
                    if ((options & PAINT_ITEMS) != 0) {
                        paintItems(g, firstItem, lastItem, options);
                    }
                    if ((options & PAINT_VSCROLL) != 0 && vsbVis) {
                        g.setClip(getVScrollBarRec());
                        paintVerScrollbar(g, true);
                    }
                    if ((options & PAINT_HSCROLL) != 0 && hsbVis) {
                        g.setClip(getHScrollBarRec());
                        paintHorScrollbar(g, true);
                    }
                    if ((options & (PAINT_FOCUS)) != 0) {
                        paintFocus(g, PAINT_FOCUS);
                    }
                } finally {
                    g.dispose();
                }
            } while (localBuffer.contentsLost());
            listG.drawImage(localBuffer, 0, 0, null);
        }
        private void paintBackground(Graphics g) {
            g.setColor(SystemColor.window);
            g.fillRect(0, 0, width, height);
            g.setColor(getListBackground());
            g.fillRect(0, 0, listWidth, listHeight);
            draw3DRect(g, getSystemColors(), 0, 0, listWidth - 1, listHeight - 1, false);
        }
        private void paintItems(Graphics g, int firstItem, int lastItem, int options) {
            if (log.isLoggable(PlatformLogger.FINER)) log.finer("Painting items from " + firstItem + " to " + lastItem + ", focused " + focusIndex + ", first " + getFirstVisibleItem() + ", last " + getLastVisibleItem());
            firstItem = Math.max(getFirstVisibleItem(), firstItem);
            if (firstItem > lastItem) {
                int t = lastItem;
                lastItem = firstItem;
                firstItem = t;
            }
            firstItem = Math.max(getFirstVisibleItem(), firstItem);
            lastItem = Math.min(lastItem, items.size()-1);
            if (log.isLoggable(PlatformLogger.FINER)) log.finer("Actually painting items from " + firstItem + " to " + lastItem +
                                                       ", items in window " + itemsInWindow());
            for (int i = firstItem; i <= lastItem; i++) {
                paintItem(g, i);
            }
        }
        private void paintItem(Graphics g, int index) {
            if (log.isLoggable(PlatformLogger.FINEST)) log.finest("Painting item " + index);
            if (!isItemHidden(index)) {
                Shape clip = g.getClip();
                int w = getItemWidth();
                int h = getItemHeight();
                int y = getItemY(index);
                int x = getItemX();
                if (log.isLoggable(PlatformLogger.FINEST)) log.finest("Setting clip " + new Rectangle(x, y, w - (SPACE*2), h-(SPACE*2)));
                g.setClip(x, y, w - (SPACE*2), h-(SPACE*2));
                if (isSelected(index)) {
                    if (log.isLoggable(PlatformLogger.FINEST)) log.finest("Painted item is selected");
                    g.setColor(getListForeground());
                } else {
                    g.setColor(getListBackground());
                }
                if (log.isLoggable(PlatformLogger.FINEST)) log.finest("Filling " + new Rectangle(x, y, w, h));
                g.fillRect(x, y, w, h);
                if (index <= getLastVisibleItem() && index < items.size()) {
                    if (!isEnabled()){
                        g.setColor(getDisabledColor());
                    } else if (isSelected(index)) {
                        g.setColor(getListBackground());
                    } else {
                        g.setColor(getListForeground());
                    }
                    String str = (String)items.elementAt(index);
                    g.drawString(str, x - hsb.getValue(), y + fontAscent);
                } else {
                    g.setClip(x, y, listWidth, h);
                    g.setColor(getListBackground());
                    g.fillRect(x, y, listWidth, h);
                }
                g.setClip(clip);
            }
        }
        void paintScrollBar(XScrollbar scr, Graphics g, int x, int y, int width, int height, boolean paintAll) {
            if (log.isLoggable(PlatformLogger.FINEST)) log.finest("Painting scrollbar " + scr + " width " +
                                                         width + " height " + height + ", paintAll " + paintAll);
            g.translate(x, y);
            scr.paint(g, getSystemColors(), paintAll);
            g.translate(-x, -y);
        }
        void paintHorScrollbar(Graphics g, boolean paintAll) {
            int w = getListWidth();
            paintScrollBar(hsb, g, 0, height - (SCROLLBAR_WIDTH), w, SCROLLBAR_WIDTH, paintAll);
        }
        void paintVerScrollbar(Graphics g, boolean paintAll) {
            int h = height - (hsbVis ? (SCROLLBAR_AREA-2) : 0);
            paintScrollBar(vsb, g, width - SCROLLBAR_WIDTH, 0, SCROLLBAR_WIDTH - 2, h, paintAll);
        }
        private Rectangle prevFocusRect;
        private void paintFocus(Graphics g, int options) {
            boolean paintFocus = (options & PAINT_FOCUS) != 0;
            if (paintFocus && !hasFocus()) {
                paintFocus = false;
            }
            if (log.isLoggable(PlatformLogger.FINE)) log.fine("Painting focus, focus index " + getFocusIndex() + ", focus is " +
                                                     (isItemHidden(getFocusIndex())?("invisible"):("visible")) + ", paint focus is " + paintFocus);
            Shape clip = g.getClip();
            g.setClip(0, 0, listWidth, listHeight);
            if (log.isLoggable(PlatformLogger.FINEST)) log.finest("Setting focus clip " + new Rectangle(0, 0, listWidth, listHeight));
            Rectangle rect = getFocusRect();
            if (prevFocusRect != null) {
                if (log.isLoggable(PlatformLogger.FINEST)) log.finest("Erasing previous focus rect " + prevFocusRect);
                g.setColor(getListBackground());
                g.drawRect(prevFocusRect.x, prevFocusRect.y, prevFocusRect.width, prevFocusRect.height);
                prevFocusRect = null;
            }
            if (paintFocus) {
                if (log.isLoggable(PlatformLogger.FINEST)) log.finest("Painting focus rect " + rect);
                g.setColor(getListForeground());  
                g.drawRect(rect.x, rect.y, rect.width, rect.height);
                prevFocusRect = rect;
            }
            g.setClip(clip);
        }
    }
}
