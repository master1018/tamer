public class XChoicePeer extends XComponentPeer implements ChoicePeer, ToplevelStateListener {
    private static final PlatformLogger log = PlatformLogger.getLogger("sun.awt.X11.XChoicePeer");
    private static final int MAX_UNFURLED_ITEMS = 10;  
    public final static int TEXT_SPACE = 1;
    public final static int BORDER_WIDTH = 1;
    public final static int ITEM_MARGIN = 1;
    public final static int SCROLLBAR_WIDTH = 15;
    private static final Insets focusInsets = new Insets(0,0,0,0);
    static final int WIDGET_OFFSET = 18;
    static final int            TEXT_XPAD = 8;
    static final int            TEXT_YPAD = 6;
    static final Color focusColor = Color.black;
    private boolean unfurled = false;        
    private boolean dragging = false;        
    private boolean mouseInSB = false;       
    private boolean firstPress = false;      
    private boolean wasDragged = false;
    private ListHelper helper;
    private UnfurledChoice unfurledChoice;
    private boolean drawSelectedItem = true;
    private Component alignUnder;
    private int dragStartIdx = -1;
    private XChoicePeerListener choiceListener;
    XChoicePeer(Choice target) {
        super(target);
    }
    void preInit(XCreateWindowParams params) {
        super.preInit(params);
        Choice target = (Choice)this.target;
        int numItems = target.getItemCount();
        unfurledChoice = new UnfurledChoice(target);
        getToplevelXWindow().addToplevelStateListener(this);
        helper = new ListHelper(unfurledChoice,
                                getGUIcolors(),
                                numItems,
                                false,
                                true,
                                false,
                                target.getFont(),
                                MAX_UNFURLED_ITEMS,
                                TEXT_SPACE,
                                ITEM_MARGIN,
                                BORDER_WIDTH,
                                SCROLLBAR_WIDTH);
    }
    void postInit(XCreateWindowParams params) {
        super.postInit(params);
        Choice target = (Choice)this.target;
        int numItems = target.getItemCount();
        for (int i = 0; i < numItems; i++) {
            helper.add(target.getItem(i));
        }
        if (!helper.isEmpty()) {
            helper.select(target.getSelectedIndex());
            helper.setFocusedIndex(target.getSelectedIndex());
        }
        helper.updateColors(getGUIcolors());
        updateMotifColors(getPeerBackground());
    }
    public boolean isFocusable() { return true; }
    public void setBounds(int x, int y, int width, int height, int op) {
        int oldX = this.x;
        int oldY = this.y;
        int oldWidth = this.width;
        int oldHeight = this.height;
        super.setBounds(x, y, width, height, op);
        if (unfurled && (oldX != this.x || oldY != this.y || oldWidth != this.width || oldHeight != this.height) ) {
            hidePopdownMenu();
        }
    }
    public void focusGained(FocusEvent e) {
        super.focusGained(e);
        repaint();
    }
    public void setEnabled(boolean value) {
        super.setEnabled(value);
        helper.updateColors(getGUIcolors());
        if (!value && unfurled){
            hidePopdownMenu();
        }
    }
    public void focusLost(FocusEvent e) {
        super.focusLost(e);
        repaint();
    }
    void ungrabInputImpl() {
        if (unfurled) {
            unfurled = false;
            dragging = false;
            mouseInSB = false;
            unfurledChoice.setVisible(false);
        }
        super.ungrabInputImpl();
    }
    void handleJavaKeyEvent(KeyEvent e) {
        if (e.getID() == KeyEvent.KEY_PRESSED) {
            keyPressed(e);
        }
    }
    public void keyPressed(KeyEvent e) {
        switch(e.getKeyCode()) {
          case KeyEvent.VK_DOWN:
          case KeyEvent.VK_KP_DOWN: {
              if (helper.getItemCount() > 1) {
                  helper.down();
                  int newIdx = helper.getSelectedIndex();
                  ((Choice)target).select(newIdx);
                  postEvent(new ItemEvent((Choice)target,
                                          ItemEvent.ITEM_STATE_CHANGED,
                                          ((Choice)target).getItem(newIdx),
                                          ItemEvent.SELECTED));
                  repaint();
              }
              break;
          }
          case KeyEvent.VK_UP:
          case KeyEvent.VK_KP_UP: {
              if (helper.getItemCount() > 1) {
                  helper.up();
                  int newIdx = helper.getSelectedIndex();
                  ((Choice)target).select(newIdx);
                  postEvent(new ItemEvent((Choice)target,
                                          ItemEvent.ITEM_STATE_CHANGED,
                                          ((Choice)target).getItem(newIdx),
                                          ItemEvent.SELECTED));
                  repaint();
              }
              break;
          }
          case KeyEvent.VK_PAGE_DOWN:
              if (unfurled && !dragging) {
                  int oldIdx = helper.getSelectedIndex();
                  helper.pageDown();
                  int newIdx = helper.getSelectedIndex();
                  if (oldIdx != newIdx) {
                      ((Choice)target).select(newIdx);
                      postEvent(new ItemEvent((Choice)target,
                                              ItemEvent.ITEM_STATE_CHANGED,
                                              ((Choice)target).getItem(newIdx),
                                              ItemEvent.SELECTED));
                      repaint();
                  }
              }
              break;
          case KeyEvent.VK_PAGE_UP:
              if (unfurled && !dragging) {
                  int oldIdx = helper.getSelectedIndex();
                  helper.pageUp();
                  int newIdx = helper.getSelectedIndex();
                  if (oldIdx != newIdx) {
                      ((Choice)target).select(newIdx);
                      postEvent(new ItemEvent((Choice)target,
                                              ItemEvent.ITEM_STATE_CHANGED,
                                              ((Choice)target).getItem(newIdx),
                                              ItemEvent.SELECTED));
                      repaint();
                  }
              }
              break;
          case KeyEvent.VK_ESCAPE:
          case KeyEvent.VK_ENTER:
              if (unfurled) {
                  if (dragging){
                      if (e.getKeyCode() == KeyEvent.VK_ESCAPE){
                          helper.select(dragStartIdx);
                      } else { 
                          int newIdx = helper.getSelectedIndex();
                          ((Choice)target).select(newIdx);
                          postEvent(new ItemEvent((Choice)target,
                                                  ItemEvent.ITEM_STATE_CHANGED,
                                                  ((Choice)target).getItem(newIdx),
                                                  ItemEvent.SELECTED));
                      }
                  }
                  hidePopdownMenu();
                  dragging = false;
                  wasDragged = false;
                  mouseInSB = false;
                  if (choiceListener != null){
                      choiceListener.unfurledChoiceClosing();
                  }
              }
              break;
          default:
              if (unfurled) {
                  Toolkit.getDefaultToolkit().beep();
              }
              break;
        }
    }
    public boolean handlesWheelScrolling() { return true; }
    void handleJavaMouseWheelEvent(MouseWheelEvent e) {
        if (unfurled && helper.isVSBVisible()) {
            if (ListHelper.doWheelScroll(helper.getVSB(), null, e)) {
                repaint();
            }
        }
    }
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
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1){
            dragStartIdx = helper.getSelectedIndex();
            if (unfurled) {
                if (! (isMouseEventInChoice(e) ||
                       unfurledChoice.isMouseEventInside(e)))
                {
                    hidePopdownMenu();
                }
                unfurledChoice.trackMouse(e);
            }
            else {
                grabInput();
                unfurledChoice.toFront();
                firstPress = true;
                wasDragged = false;
                unfurled = true;
            }
        }
    }
    void hidePopdownMenu(){
        ungrabInput();
        unfurledChoice.setVisible(false);
        unfurled = false;
    }
    public void mouseReleased(MouseEvent e) {
        if (unfurled) {
            if (mouseInSB) {
                unfurledChoice.trackMouse(e);
            }
            else {
                boolean isMouseEventInside = unfurledChoice.isMouseEventInside( e );
                boolean isMouseInListArea = unfurledChoice.isMouseInListArea( e );
                if (!helper.isEmpty() && !isMouseInListArea && dragging) {
                    ((Choice)target).select(dragStartIdx);
                }
                if ( !firstPress && isMouseInListArea) {
                    hidePopdownMenu();
                }
                if ( !firstPress && !isMouseEventInside) {
                    hidePopdownMenu();
                }
                if ( firstPress && dragging) {
                    hidePopdownMenu();
                }
                if ( !firstPress && !isMouseInListArea &&
                     isMouseEventInside && dragging)
                {
                    hidePopdownMenu();
                }
                if (!helper.isEmpty()) {
                    if (unfurledChoice.isMouseInListArea(e)) {
                        int newIdx = helper.getSelectedIndex();
                        if (newIdx >= 0) {
                            if (newIdx != dragStartIdx) {
                                ((Choice)target).select(newIdx);
                            }
                            if (wasDragged && e.getButton() != MouseEvent.BUTTON1){
                                ((Choice)target).select(dragStartIdx);
                            }
                            if (e.getButton() == MouseEvent.BUTTON1 &&
                                (!firstPress || wasDragged ))
                            {
                                postEvent(new ItemEvent((Choice)target,
                                                        ItemEvent.ITEM_STATE_CHANGED,
                                                        ((Choice)target).getItem(newIdx),
                                                        ItemEvent.SELECTED));
                            }
                            if (choiceListener != null) {
                                choiceListener.unfurledChoiceClosing();
                            }
                        }
                    }
                }
                unfurledChoice.trackMouse(e);
            }
        }
        dragging = false;
        wasDragged = false;
        firstPress = false;
        dragStartIdx = -1;
    }
    public void mouseDragged(MouseEvent e) {
        if ( e.getModifiers() == MouseEvent.BUTTON1_MASK ){
            dragging = true;
            wasDragged = true;
            unfurledChoice.trackMouse(e);
        }
    }
    public Dimension getMinimumSize() {
        FontMetrics fm = getFontMetrics(target.getFont());
        Choice c = (Choice)target;
        int w = 0;
        for (int i = c.countItems() ; i-- > 0 ;) {
            w = Math.max(fm.stringWidth(c.getItem(i)), w);
        }
        return new Dimension(w + TEXT_XPAD + WIDGET_OFFSET,
                             fm.getMaxAscent() + fm.getMaxDescent() + TEXT_YPAD);
    }
    public void layout() {
    }
    public void paint(Graphics g) {
        flush();
        Dimension size = getPeerSize();
        g.setColor(getPeerBackground());
        g.fillRect(0, 0, width, height);
        drawMotif3DRect(g, 1, 1, width-2, height-2, false);
        drawMotif3DRect(g, width - WIDGET_OFFSET, (height / 2) - 3, 12, 6, false);
        if (!helper.isEmpty() && helper.getSelectedIndex() != -1) {
            g.setFont(getPeerFont());
            FontMetrics fm = g.getFontMetrics();
            String lbl = helper.getItem(helper.getSelectedIndex());
            if (lbl != null && drawSelectedItem) {
                g.setClip(1, 1, width - WIDGET_OFFSET - 2, height);
                if (isEnabled()) {
                    g.setColor(getPeerForeground());
                    g.drawString(lbl, 5, (height + fm.getMaxAscent()-fm.getMaxDescent())/2);
                }
                else {
                    g.setColor(getPeerBackground().brighter());
                    g.drawString(lbl, 5, (height + fm.getMaxAscent()-fm.getMaxDescent())/2);
                    g.setColor(getPeerBackground().darker());
                    g.drawString(lbl, 4, ((height + fm.getMaxAscent()-fm.getMaxDescent())/2)-1);
                }
                g.setClip(0, 0, width, height);
            }
        }
        if (hasFocus()) {
            paintFocus(g,focusInsets.left,focusInsets.top,size.width-(focusInsets.left+focusInsets.right)-1,size.height-(focusInsets.top+focusInsets.bottom)-1);
        }
        if (unfurled) {
            unfurledChoice.repaint();
        }
        flush();
    }
    protected void paintFocus(Graphics g,
                              int x, int y, int w, int h) {
        g.setColor(focusColor);
        g.drawRect(x,y,w,h);
    }
    public void select(int index) {
        helper.select(index);
        helper.setFocusedIndex(index);
        repaint();
    }
    public void add(String item, int index) {
        helper.add(item, index);
        repaint();
    }
    public void remove(int index) {
        boolean selected = (index == helper.getSelectedIndex());
        boolean visibled = (index >= helper.firstDisplayedIndex() && index <= helper.lastDisplayedIndex());
        helper.remove(index);
        if (selected) {
            if (helper.isEmpty()) {
                helper.select(-1);
            }
            else {
                helper.select(0);
            }
        }
        if (!unfurled) {
            if (helper.isEmpty()) {
                repaint();
            }
            return;
        }
        if (visibled){
            Rectangle r = unfurledChoice.placeOnScreen();
            unfurledChoice.reshape(r.x, r.y, r.width, r.height);
            return;
        }
        if (visibled || selected){
            repaint();
        }
    }
    public void removeAll() {
        helper.removeAll();
        helper.select(-1);
        Rectangle r = unfurledChoice.placeOnScreen();
        unfurledChoice.reshape(r.x, r.y, r.width, r.height);
        repaint();
    }
    public void addItem(String item, int index) {
        add(item, index);
    }
    public void setFont(Font font) {
        super.setFont(font);
        helper.setFont(this.font);
    }
    public void setForeground(Color c) {
        super.setForeground(c);
        helper.updateColors(getGUIcolors());
    }
    public void setBackground(Color c) {
        super.setBackground(c);
        unfurledChoice.setBackground(c);
        helper.updateColors(getGUIcolors());
        updateMotifColors(c);
    }
    public void setDrawSelectedItem(boolean value) {
        drawSelectedItem = value;
    }
    public void setAlignUnder(Component comp) {
        alignUnder = comp;
    }
    public void addXChoicePeerListener(XChoicePeerListener l){
        choiceListener = l;
    }
    public void removeXChoicePeerListener(){
        choiceListener = null;
    }
    public boolean isUnfurled(){
        return unfurled;
    }
    public void stateChangedICCCM(int oldState, int newState) {
        if (unfurled && oldState != newState){
                hidePopdownMenu();
        }
    }
    public void stateChangedJava(int oldState, int newState) {
        if (unfurled && oldState != newState){
            hidePopdownMenu();
        }
    }
    class UnfurledChoice extends XWindow  {
        public UnfurledChoice(Component target) {
            super(target);
        }
        public void preInit(XCreateWindowParams params) {
            params.delete(PARENT_WINDOW);
            super.preInit(params);
            params.remove(BOUNDS);
            params.add(OVERRIDE_REDIRECT, Boolean.TRUE);
        }
        Rectangle placeOnScreen() {
            int numItemsDisplayed;
            if (helper.isEmpty()) {
                numItemsDisplayed = 1;
            }
            else {
                int numItems = helper.getItemCount();
                numItemsDisplayed = Math.min(MAX_UNFURLED_ITEMS, numItems);
            }
            Point global = XChoicePeer.this.toGlobal(0,0);
            Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
            if (alignUnder != null) {
                Rectangle choiceRec = XChoicePeer.this.getBounds();
                choiceRec.setLocation(0, 0);
                choiceRec = XChoicePeer.this.toGlobal(choiceRec);
                Rectangle alignUnderRec = new Rectangle(alignUnder.getLocationOnScreen(), alignUnder.getSize()); 
                Rectangle result = choiceRec.union(alignUnderRec);
                width = result.width;
                x = result.x;
                y = result.y + result.height;
                height = 2*BORDER_WIDTH +
                    numItemsDisplayed*(helper.getItemHeight()+2*ITEM_MARGIN);
            } else {
                x = global.x;
                y = global.y + XChoicePeer.this.height;
                width = Math.max(XChoicePeer.this.width,
                                 helper.getMaxItemWidth() + 2 * (BORDER_WIDTH + ITEM_MARGIN + TEXT_SPACE) + (helper.isVSBVisible() ? SCROLLBAR_WIDTH : 0));
                height = 2*BORDER_WIDTH +
                    numItemsDisplayed*(helper.getItemHeight()+2*ITEM_MARGIN);
            }
            if (x < 0) {
                x = 0;
            }
            else if (x + width > screen.width) {
                x = screen.width - width;
            }
            if (y < 0) {
                y = 0;
            }
            else if (y + height > screen.height) {
                y = screen.height - height;
            }
            return new Rectangle(x, y, width, height);
        }
        public void toFront() {
            if (choiceListener != null)
                choiceListener.unfurledChoiceOpening(helper);
            Rectangle r = placeOnScreen();
            reshape(r.x, r.y, r.width, r.height);
            super.toFront();
            setVisible(true);
        }
        public void trackMouse(MouseEvent e) {
            Point local = toLocalCoords(e);
            switch (e.getID()) {
              case MouseEvent.MOUSE_PRESSED:
                  if (helper.isInVertSB(getBounds(), local.x, local.y)) {
                      mouseInSB = true;
                      helper.handleVSBEvent(e, getBounds(), local.x, local.y);
                  }
                  else {
                      trackSelection(local.x, local.y);
                  }
                  break;
              case MouseEvent.MOUSE_RELEASED:
                  if (mouseInSB) {
                      mouseInSB = false;
                      helper.handleVSBEvent(e, getBounds(), local.x, local.y);
                  }else{
                      helper.trackMouseReleasedScroll();
                  }
                  break;
              case MouseEvent.MOUSE_DRAGGED:
                  if (mouseInSB) {
                      helper.handleVSBEvent(e, getBounds(), local.x, local.y);
                  }
                  else {
                      helper.trackMouseDraggedScroll(local.x, local.y, width, height);
                      trackSelection(local.x, local.y);
                  }
                  break;
            }
        }
        private void trackSelection(int transX, int transY) {
            if (!helper.isEmpty()) {
                if (transX > 0 && transX < width &&
                    transY > 0 && transY < height) {
                    int newIdx = helper.y2index(transY);
                    if (log.isLoggable(PlatformLogger.FINE)) {
                        log.fine("transX=" + transX + ", transY=" + transY
                                 + ",width=" + width + ", height=" + height
                                 + ", newIdx=" + newIdx + " on " + target);
                    }
                    if ((newIdx >=0) && (newIdx < helper.getItemCount())
                        && (newIdx != helper.getSelectedIndex()))
                    {
                        helper.select(newIdx);
                        unfurledChoice.repaint();
                    }
                }
            }
        }
        public void paintBackground(){
            Graphics g = getGraphics();
            g.setColor(getPeerBackground());
            g.fillRect(0, 0, width, height);
        }
        public void repaint() {
            if (!isVisible()) {
                return;
            }
            if (helper.checkVsbVisibilityChangedAndReset()){
                paintBackground();
            }
            super.repaint();
        }
        public void paint(Graphics g) {
            Choice choice = (Choice)target;
            Color colors[] = XChoicePeer.this.getGUIcolors();
            draw3DRect(g, getSystemColors(), 0, 0, width - 1, height - 1, true);
            draw3DRect(g, getSystemColors(), 1, 1, width - 3, height - 3, true);
            helper.paintAllItems(g,
                                 colors,
                                 getBounds());
        }
        public void setVisible(boolean vis) {
            xSetVisible(vis);
            if (!vis && alignUnder != null) {
                alignUnder.requestFocusInWindow();
            }
        }
        private Point toLocalCoords(MouseEvent e) {
            Point global = e.getLocationOnScreen();
            global.x -= x;
            global.y -= y;
            return global;
        }
        private boolean isMouseEventInside(MouseEvent e) {
            Point local = toLocalCoords(e);
            if (local.x > 0 && local.x < width &&
                local.y > 0 && local.y < height) {
                return true;
            }
            return false;
        }
        private boolean isMouseInListArea(MouseEvent e) {
            if (isMouseEventInside(e)) {
                Point local = toLocalCoords(e);
                Rectangle bounds = getBounds();
                if (!helper.isInVertSB(bounds, local.x, local.y)) {
                    return true;
                }
            }
            return false;
        }
        public void handleConfigureNotifyEvent(XEvent xev) {}
        public void handleMapNotifyEvent(XEvent xev) {}
        public void handleUnmapNotifyEvent(XEvent xev) {}
    } 
    public void dispose() {
        if (unfurledChoice != null) {
            unfurledChoice.destroy();
        }
        super.dispose();
    }
    boolean prePostEvent(final AWTEvent e) {
        if (unfurled){
            if (e instanceof MouseWheelEvent){
                return super.prePostEvent(e);
            }
            if (e instanceof KeyEvent){
                EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            if(target.isFocusable() &&
                               getParentTopLevel().isFocusableWindow() )
                            {
                                handleJavaKeyEvent((KeyEvent)e);
                            }
                        }
                    });
                return true;
            } else {
                if (e instanceof MouseEvent){
                    MouseEvent me = (MouseEvent)e;
                    int eventId = e.getID();
                    if ((unfurledChoice.isMouseEventInside(me) ||
                         (!firstPress && eventId == MouseEvent.MOUSE_DRAGGED)))
                    {
                        return handleMouseEventByChoice(me);
                    }
                    if (eventId == MouseEvent.MOUSE_MOVED){
                        return handleMouseEventByChoice(me);
                    }
                    if (  !firstPress && !( isMouseEventInChoice(me) ||
                             unfurledChoice.isMouseEventInside(me)) &&
                             ( eventId == MouseEvent.MOUSE_PRESSED ||
                               eventId == MouseEvent.MOUSE_RELEASED ||
                               eventId == MouseEvent.MOUSE_CLICKED )
                          )
                    {
                        return handleMouseEventByChoice(me);
                    }
                }
            }
        }
        return super.prePostEvent(e);
    }
    public boolean handleMouseEventByChoice(final MouseEvent me){
        EventQueue.invokeLater(new Runnable() {
                public void run() {
                    handleJavaMouseEvent(me);
                }
            });
        return true;
    }
    private boolean isMouseEventInChoice(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        Rectangle choiceRect = getBounds();
        if (x < 0 || x > choiceRect.width ||
            y < 0 || y > choiceRect.height)
        {
            return false;
        }
        return true;
    }
}
interface XChoicePeerListener{
    public void unfurledChoiceOpening(ListHelper choiceHelper);
    public void unfurledChoiceClosing();
}
