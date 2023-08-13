abstract public class XBaseMenuWindow extends XWindow {
    private static PlatformLogger log = PlatformLogger.getLogger("sun.awt.X11.XBaseMenuWindow");
    private Color backgroundColor;
    private Color foregroundColor;
    private Color lightShadowColor;
    private Color darkShadowColor;
    private Color selectedColor;
    private Color disabledColor;
    private ArrayList<XMenuItemPeer> items;
    private int selectedIndex = -1;
    private XMenuPeer showingSubmenu = null;
    static private Object menuTreeLock = new Object();
    private XMenuPeer showingMousePressedSubmenu = null;
    protected Point grabInputPoint = null;
    protected boolean hasPointerMoved = false;
    private MappingData mappingData;
    static class MappingData implements Cloneable {
        private XMenuItemPeer[] items;
        MappingData(XMenuItemPeer[] items) {
            this.items = items;
        }
        MappingData() {
            this.items = new XMenuItemPeer[0];
        }
        public Object clone() {
            try {
                return super.clone();
            } catch (CloneNotSupportedException ex) {
                throw new InternalError();
            }
        }
        public XMenuItemPeer[] getItems() {
            return this.items;
        }
    }
    XBaseMenuWindow() {
        super(new XCreateWindowParams(new Object[] {
            DELAYED, Boolean.TRUE}));
    }
    protected abstract XBaseMenuWindow getParentMenuWindow();
    protected abstract MappingData map();
    protected abstract Rectangle getSubmenuBounds(Rectangle itemBounds, Dimension windowSize);
    protected abstract void updateSize();
    void instantPreInit(XCreateWindowParams params) {
        super.instantPreInit(params);
        items = new ArrayList();
    }
    static Object getMenuTreeLock() {
        return menuTreeLock;
    }
    protected void resetMapping() {
        mappingData = null;
    }
    void postPaintEvent() {
        if (isShowing()) {
            PaintEvent pe = new PaintEvent(target, PaintEvent.PAINT,
                                           new Rectangle(0, 0, width, height));
            postEvent(pe);
        }
    }
    XMenuItemPeer getItem(int index) {
        if (index >= 0) {
            synchronized(getMenuTreeLock()) {
                if (items.size() > index) {
                    return items.get(index);
                }
            }
        }
        return null;
    }
    XMenuItemPeer[] copyItems() {
        synchronized(getMenuTreeLock()) {
            return (XMenuItemPeer[])items.toArray(new XMenuItemPeer[] {});
        }
    }
    XMenuItemPeer getSelectedItem() {
        synchronized(getMenuTreeLock()) {
            if (selectedIndex >= 0) {
                if (items.size() > selectedIndex) {
                    return items.get(selectedIndex);
                }
            }
            return null;
        }
    }
    XMenuPeer getShowingSubmenu() {
        synchronized(getMenuTreeLock()) {
            return showingSubmenu;
        }
    }
    public void addItem(MenuItem item) {
        XMenuItemPeer mp = (XMenuItemPeer)item.getPeer();
        if (mp != null) {
            mp.setContainer(this);
            synchronized(getMenuTreeLock()) {
                items.add(mp);
            }
        } else {
            if (log.isLoggable(PlatformLogger.FINE)) {
                log.fine("WARNING: Attempt to add menu item without a peer");
            }
        }
        updateSize();
    }
    public void delItem(int index) {
        synchronized(getMenuTreeLock()) {
            if (selectedIndex == index) {
                selectItem(null, false);
            } else if (selectedIndex > index) {
                selectedIndex--;
            }
            if (index < items.size()) {
                items.remove(index);
            } else {
                if (log.isLoggable(PlatformLogger.FINE)) {
                    log.fine("WARNING: Attempt to remove non-existing menu item, index : " + index + ", item count : " + items.size());
                }
            }
        }
        updateSize();
    }
    public void reloadItems(Vector items) {
        synchronized(getMenuTreeLock()) {
            this.items.clear();
            MenuItem[] itemArray = (MenuItem[])items.toArray(new MenuItem[] {});
            int itemCnt = itemArray.length;
            for(int i = 0; i < itemCnt; i++) {
                addItem(itemArray[i]);
            }
        }
    }
    void selectItem(XMenuItemPeer item, boolean showWindowIfMenu) {
        synchronized(getMenuTreeLock()) {
            XMenuPeer showingSubmenu = getShowingSubmenu();
            int newSelectedIndex = (item != null) ? items.indexOf(item) : -1;
            if (this.selectedIndex != newSelectedIndex) {
                if (log.isLoggable(PlatformLogger.FINEST)) {
                    log.finest("Selected index changed, was : " + this.selectedIndex + ", new : " + newSelectedIndex);
                }
                this.selectedIndex = newSelectedIndex;
                postPaintEvent();
            }
            final XMenuPeer submenuToShow = (showWindowIfMenu && (item instanceof XMenuPeer)) ? (XMenuPeer)item : null;
            if (submenuToShow != showingSubmenu) {
                XToolkit.executeOnEventHandlerThread(target, new Runnable() {
                        public void run() {
                            doShowSubmenu(submenuToShow);
                        }
                    });
            }
        }
    }
    private void doShowSubmenu(XMenuPeer submenuToShow) {
        XMenuWindow menuWindowToShow = (submenuToShow != null) ? submenuToShow.getMenuWindow() : null;
        Dimension dim = null;
        Rectangle bounds = null;
        if (menuWindowToShow != null) {
            menuWindowToShow.ensureCreated();
        }
        XToolkit.awtLock();
        try {
            synchronized(getMenuTreeLock()) {
                if (showingSubmenu != submenuToShow) {
                    if (log.isLoggable(PlatformLogger.FINER)) {
                        log.finest("Changing showing submenu");
                    }
                    if (showingSubmenu != null) {
                        XMenuWindow showingSubmenuWindow = showingSubmenu.getMenuWindow();
                        if (showingSubmenuWindow != null) {
                            showingSubmenuWindow.hide();
                        }
                    }
                    if (submenuToShow != null) {
                        dim = menuWindowToShow.getDesiredSize();
                        bounds = menuWindowToShow.getParentMenuWindow().getSubmenuBounds(submenuToShow.getBounds(), dim);
                        menuWindowToShow.show(bounds);
                    }
                    showingSubmenu = submenuToShow;
                }
            }
        } finally {
            XToolkit.awtUnlock();
        }
    }
    final void setItemsFont( Font font ) {
        XMenuItemPeer[] items = copyItems();
        int itemCnt = items.length;
        for (int i = 0; i < itemCnt; i++) {
            items[i].setFont(font);
        }
    }
    MappingData getMappingData() {
        MappingData mappingData = this.mappingData;
        if (mappingData == null) {
            mappingData = map();
            this.mappingData = mappingData;
        }
        return (MappingData)mappingData.clone();
    }
    XMenuItemPeer getItemFromPoint(Point pt) {
        XMenuItemPeer[] items = getMappingData().getItems();
        int cnt = items.length;
        for (int i = 0; i < cnt; i++) {
            if (items[i].getBounds().contains(pt)) {
                return items[i];
            }
        }
        return null;
    }
    XMenuItemPeer getNextSelectableItem() {
        XMenuItemPeer[] mappedItems = getMappingData().getItems();
        XMenuItemPeer selectedItem = getSelectedItem();
        int cnt = mappedItems.length;
        int selIdx = -1;
        for (int i = 0; i < cnt; i++) {
            if (mappedItems[i] == selectedItem) {
                selIdx = i;
                break;
            }
        }
        int idx = (selIdx == cnt - 1) ? 0 : selIdx + 1;
        for (int i = 0; i < cnt; i++) {
            XMenuItemPeer item = mappedItems[idx];
            if (!item.isSeparator() && item.isTargetItemEnabled()) {
                return item;
            }
            idx++;
            if (idx >= cnt) {
                idx = 0;
            }
        }
        return null;
    }
    XMenuItemPeer getPrevSelectableItem() {
        XMenuItemPeer[] mappedItems = getMappingData().getItems();
        XMenuItemPeer selectedItem = getSelectedItem();
        int cnt = mappedItems.length;
        int selIdx = -1;
        for (int i = 0; i < cnt; i++) {
            if (mappedItems[i] == selectedItem) {
                selIdx = i;
                break;
            }
        }
        int idx = (selIdx <= 0) ? cnt - 1 : selIdx - 1;
        for (int i = 0; i < cnt; i++) {
            XMenuItemPeer item = mappedItems[idx];
            if (!item.isSeparator() && item.isTargetItemEnabled()) {
                return item;
            }
            idx--;
            if (idx < 0) {
                idx = cnt - 1;
            }
        }
        return null;
    }
    XMenuItemPeer getFirstSelectableItem() {
        XMenuItemPeer[] mappedItems = getMappingData().getItems();
        int cnt = mappedItems.length;
        for (int i = 0; i < cnt; i++) {
            XMenuItemPeer item = mappedItems[i];
            if (!item.isSeparator() && item.isTargetItemEnabled()) {
                return item;
            }
        }
        return null;
    }
    XBaseMenuWindow getShowingLeaf() {
        synchronized(getMenuTreeLock()) {
            XBaseMenuWindow leaf = this;
            XMenuPeer leafchild = leaf.getShowingSubmenu();
            while (leafchild != null) {
                leaf = leafchild.getMenuWindow();
                leafchild = leaf.getShowingSubmenu();
            }
            return leaf;
        }
    }
    XBaseMenuWindow getRootMenuWindow() {
        synchronized(getMenuTreeLock()) {
            XBaseMenuWindow t = this;
            XBaseMenuWindow tparent = t.getParentMenuWindow();
            while (tparent != null) {
                t = tparent;
                tparent = t.getParentMenuWindow();
            }
            return t;
        }
    }
    XBaseMenuWindow getMenuWindowFromPoint(Point pt) {
        synchronized(getMenuTreeLock()) {
            XBaseMenuWindow t = getShowingLeaf();
            while (t != null) {
                Rectangle r = new Rectangle(t.toGlobal(new Point(0, 0)), t.getSize());
                if (r.contains(pt)) {
                    return t;
                }
                t = t.getParentMenuWindow();
            }
            return null;
        }
    }
    Rectangle fitWindowBelow(Rectangle itemBounds, Dimension windowSize, Dimension screenSize) {
        int width = windowSize.width;
        int height = windowSize.height;
        int x = (itemBounds.x > 0) ? itemBounds.x : 0;
        int y = (itemBounds.y + itemBounds.height > 0) ? itemBounds.y + itemBounds.height : 0;
        if (y + height <= screenSize.height) {
            if (width > screenSize.width) {
                width = screenSize.width;
            }
            if (x + width > screenSize.width) {
                x = screenSize.width - width;
            }
            return new Rectangle(x, y, width, height);
        } else {
            return null;
        }
    }
    Rectangle fitWindowAbove(Rectangle itemBounds, Dimension windowSize, Dimension screenSize) {
        int width = windowSize.width;
        int height = windowSize.height;
        int x = (itemBounds.x > 0) ? itemBounds.x : 0;
        int y = (itemBounds.y > screenSize.height) ? screenSize.height - height : itemBounds.y - height;
        if (y >= 0) {
            if (width > screenSize.width) {
                width = screenSize.width;
            }
            if (x + width > screenSize.width) {
                x = screenSize.width - width;
            }
            return new Rectangle(x, y, width, height);
        } else {
            return null;
        }
    }
    Rectangle fitWindowRight(Rectangle itemBounds, Dimension windowSize, Dimension screenSize) {
        int width = windowSize.width;
        int height = windowSize.height;
        int x = (itemBounds.x + itemBounds.width > 0) ? itemBounds.x + itemBounds.width : 0;
        int y = (itemBounds.y > 0) ? itemBounds.y : 0;
        if (x + width <= screenSize.width) {
            if (height > screenSize.height) {
                height = screenSize.height;
            }
            if (y + height > screenSize.height) {
                y = screenSize.height - height;
            }
            return new Rectangle(x, y, width, height);
        } else {
            return null;
        }
    }
    Rectangle fitWindowLeft(Rectangle itemBounds, Dimension windowSize, Dimension screenSize) {
        int width = windowSize.width;
        int height = windowSize.height;
        int x = (itemBounds.x < screenSize.width) ? itemBounds.x - width : screenSize.width - width;
        int y = (itemBounds.y > 0) ? itemBounds.y : 0;
        if (x >= 0) {
            if (height > screenSize.height) {
                height = screenSize.height;
            }
            if (y + height > screenSize.height) {
                y = screenSize.height - height;
            }
            return new Rectangle(x, y, width, height);
        } else {
            return null;
        }
    }
    Rectangle fitWindowToScreen(Dimension windowSize, Dimension screenSize) {
        int width = (windowSize.width < screenSize.width) ? windowSize.width : screenSize.width;
        int height = (windowSize.height < screenSize.height) ? windowSize.height : screenSize.height;
        return new Rectangle(0, 0, width, height);
    }
    void resetColors() {
        replaceColors((target == null) ? SystemColor.window : target.getBackground());
    }
    void replaceColors(Color backgroundColor) {
        if (backgroundColor != this.backgroundColor) {
            this.backgroundColor = backgroundColor;
            int red = backgroundColor.getRed();
            int green = backgroundColor.getGreen();
            int blue = backgroundColor.getBlue();
            foregroundColor = new Color(MotifColorUtilities.calculateForegroundFromBackground(red,green,blue));
            lightShadowColor = new Color(MotifColorUtilities.calculateTopShadowFromBackground(red,green,blue));
            darkShadowColor = new Color(MotifColorUtilities.calculateBottomShadowFromBackground(red,green,blue));
            selectedColor = new Color(MotifColorUtilities.calculateSelectFromBackground(red,green,blue));
            disabledColor = (backgroundColor.equals(Color.BLACK)) ? foregroundColor.darker() : backgroundColor.darker();
        }
    }
    Color getBackgroundColor() {
        return backgroundColor;
    }
    Color getForegroundColor() {
        return foregroundColor;
    }
    Color getLightShadowColor() {
        return lightShadowColor;
    }
    Color getDarkShadowColor() {
        return darkShadowColor;
    }
    Color getSelectedColor() {
        return selectedColor;
    }
    Color getDisabledColor() {
        return disabledColor;
    }
    void draw3DRect(Graphics g, int x, int y, int width, int height, boolean raised) {
        if ((width <= 0) || (height <= 0)) {
            return;
        }
        Color c = g.getColor();
        g.setColor(raised ? getLightShadowColor() : getDarkShadowColor());
        g.drawLine(x, y, x, y + height - 1);
        g.drawLine(x + 1, y, x + width - 1, y);
        g.setColor(raised ? getDarkShadowColor() : getLightShadowColor());
        g.drawLine(x + 1, y + height - 1, x + width - 1, y + height - 1);
        g.drawLine(x + width - 1, y + 1, x + width - 1, y + height - 1);
        g.setColor(c);
    }
     protected boolean isEventDisabled(XEvent e) {
        switch (e.get_type()) {
          case XConstants.Expose :
          case XConstants.GraphicsExpose :
          case XConstants.ButtonPress:
          case XConstants.ButtonRelease:
          case XConstants.MotionNotify:
          case XConstants.KeyPress:
          case XConstants.KeyRelease:
          case XConstants.DestroyNotify:
              return super.isEventDisabled(e);
          default:
              return true;
        }
    }
    public void dispose() {
        setDisposed(true);
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                doDispose();
            }
        });
    }
    protected void doDispose() {
        xSetVisible(false);
        SurfaceData oldData = surfaceData;
        surfaceData = null;
        if (oldData != null) {
            oldData.invalidate();
        }
        XToolkit.targetDisposedPeer(target, this);
        destroy();
    }
    void postEvent(final AWTEvent event) {
        EventQueue.invokeLater(new Runnable() {
                public void run() {
                    handleEvent(event);
                }
            });
    }
    protected void handleEvent(AWTEvent event) {
        switch(event.getID()) {
        case PaintEvent.PAINT:
            doHandleJavaPaintEvent((PaintEvent)event);
            break;
        }
    }
    public boolean grabInput() {
        int rootX;
        int rootY;
        boolean res;
        XToolkit.awtLock();
        try {
            long root = XlibWrapper.RootWindow(XToolkit.getDisplay(),
                    getScreenNumber());
            res = XlibWrapper.XQueryPointer(XToolkit.getDisplay(), root,
                                            XlibWrapper.larg1, 
                                            XlibWrapper.larg2, 
                                            XlibWrapper.larg3, 
                                            XlibWrapper.larg4, 
                                            XlibWrapper.larg5, 
                                            XlibWrapper.larg6, 
                                            XlibWrapper.larg7);
            rootX = Native.getInt(XlibWrapper.larg3);
            rootY = Native.getInt(XlibWrapper.larg4);
            res &= super.grabInput();
        } finally {
            XToolkit.awtUnlock();
        }
        if (res) {
            this.grabInputPoint = new Point(rootX, rootY);
            this.hasPointerMoved = false;
        } else {
            this.grabInputPoint = null;
            this.hasPointerMoved = true;
        }
        return res;
    }
    void doHandleJavaPaintEvent(PaintEvent event) {
        Rectangle rect = event.getUpdateRect();
        repaint(rect.x, rect.y, rect.width, rect.height);
    }
    void doHandleJavaMouseEvent( MouseEvent mouseEvent ) {
        if (!XToolkit.isLeftMouseButton(mouseEvent) && !XToolkit.isRightMouseButton(mouseEvent)) {
            return;
        }
        XBaseWindow grabWindow = XAwtState.getGrabWindow();
        Point ptGlobal = mouseEvent.getLocationOnScreen();
        if (!hasPointerMoved) {
            if (grabInputPoint == null ||
                (Math.abs(ptGlobal.x - grabInputPoint.x) > getMouseMovementSmudge()) ||
                (Math.abs(ptGlobal.y - grabInputPoint.y) > getMouseMovementSmudge())) {
                hasPointerMoved = true;
            }
        }
        XBaseMenuWindow wnd = getMenuWindowFromPoint(ptGlobal);
        XMenuItemPeer item = (wnd != null) ? wnd.getItemFromPoint(wnd.toLocal(ptGlobal)) : null;
        XBaseMenuWindow cwnd = getShowingLeaf();
        switch (mouseEvent.getID()) {
          case MouseEvent.MOUSE_PRESSED:
              showingMousePressedSubmenu = null;
              if ((grabWindow == this) && (wnd == null)) {
                  ungrabInput();
              } else {
                  grabInput();
                  if (item != null && !item.isSeparator() && item.isTargetItemEnabled()) {
                      if (wnd.getShowingSubmenu() == item) {
                          showingMousePressedSubmenu = (XMenuPeer)item;
                      }
                      wnd.selectItem(item, true);
                  } else {
                      if (wnd != null) {
                          wnd.selectItem(null, false);
                      }
                  }
              }
              break;
          case MouseEvent.MOUSE_RELEASED:
              if (item != null && !item.isSeparator() && item.isTargetItemEnabled()) {
                  if  (item instanceof XMenuPeer) {
                      if (showingMousePressedSubmenu == item) {
                          if (wnd instanceof XMenuBarPeer) {
                              ungrabInput();
                          } else {
                              wnd.selectItem(item, false);
                          }
                      }
                  } else {
                      item.action(mouseEvent.getWhen());
                      ungrabInput();
                  }
              } else {
                  if (hasPointerMoved || (wnd instanceof XMenuBarPeer)) {
                      ungrabInput();
                  }
              }
              showingMousePressedSubmenu = null;
              break;
          case MouseEvent.MOUSE_DRAGGED:
              if (wnd != null) {
                  if (item != null && !item.isSeparator() && item.isTargetItemEnabled()) {
                      if (grabWindow == this){
                          wnd.selectItem(item, true);
                      }
                  } else {
                      wnd.selectItem(null, false);
                  }
              } else {
                  if (cwnd != null) {
                      cwnd.selectItem(null, false);
                  }
              }
              break;
        }
    }
    void doHandleJavaKeyEvent(KeyEvent event) {
        if (log.isLoggable(PlatformLogger.FINER)) log.finer(event.toString());
        if (event.getID() != KeyEvent.KEY_PRESSED) {
            return;
        }
        final int keyCode = event.getKeyCode();
        XBaseMenuWindow cwnd = getShowingLeaf();
        XMenuItemPeer citem = cwnd.getSelectedItem();
        switch(keyCode) {
          case KeyEvent.VK_UP:
          case KeyEvent.VK_KP_UP:
              if (!(cwnd instanceof XMenuBarPeer)) {
                  cwnd.selectItem(cwnd.getPrevSelectableItem(), false);
              }
              break;
          case KeyEvent.VK_DOWN:
          case KeyEvent.VK_KP_DOWN:
              if (cwnd instanceof XMenuBarPeer) {
                  selectItem(getSelectedItem(), true);
              } else {
                  cwnd.selectItem(cwnd.getNextSelectableItem(), false);
              }
              break;
          case KeyEvent.VK_LEFT:
          case KeyEvent.VK_KP_LEFT:
              if (cwnd instanceof XMenuBarPeer) {
                  selectItem(getPrevSelectableItem(), false);
              } else if (cwnd.getParentMenuWindow() instanceof XMenuBarPeer) {
                  selectItem(getPrevSelectableItem(), true);
              } else {
                  XBaseMenuWindow pwnd = cwnd.getParentMenuWindow();
                  if (pwnd != null) {
                      pwnd.selectItem(pwnd.getSelectedItem(), false);
                  }
              }
              break;
          case KeyEvent.VK_RIGHT:
          case KeyEvent.VK_KP_RIGHT:
              if (cwnd instanceof XMenuBarPeer) {
                  selectItem(getNextSelectableItem(), false);
              } else if (citem instanceof XMenuPeer) {
                  cwnd.selectItem(citem, true);
              } else if (this instanceof XMenuBarPeer) {
                  selectItem(getNextSelectableItem(), true);
              }
              break;
          case KeyEvent.VK_SPACE:
          case KeyEvent.VK_ENTER:
              if (citem instanceof XMenuPeer) {
                  cwnd.selectItem(citem, true);
              } else if (citem != null) {
                  citem.action(event.getWhen());
                  ungrabInput();
              }
              break;
          case KeyEvent.VK_ESCAPE:
              if ((cwnd instanceof XMenuBarPeer) || (cwnd.getParentMenuWindow() instanceof XMenuBarPeer)) {
                  ungrabInput();
              } else if (cwnd instanceof XPopupMenuPeer) {
                  ungrabInput();
              } else {
                  XBaseMenuWindow pwnd = cwnd.getParentMenuWindow();
                  pwnd.selectItem(pwnd.getSelectedItem(), false);
              }
              break;
          case KeyEvent.VK_F10:
              ungrabInput();
              break;
          default:
              break;
        }
    }
} 
