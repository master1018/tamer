public class JPopupMenu extends JComponent implements Accessible,MenuElement {
    private static final String uiClassID = "PopupMenuUI";
    private static final Object defaultLWPopupEnabledKey =
        new StringBuffer("JPopupMenu.defaultLWPopupEnabledKey");
    static boolean popupPostionFixDisabled = false;
    static {
        popupPostionFixDisabled = java.security.AccessController.doPrivileged(
                new sun.security.action.GetPropertyAction(
                "javax.swing.adjustPopupLocationToFit","")).equals("false");
    }
    transient  Component invoker;
    transient  Popup popup;
    transient  Frame frame;
    private    int desiredLocationX,desiredLocationY;
    private    String     label                   = null;
    private    boolean   paintBorder              = true;
    private    Insets    margin                   = null;
    private    boolean   lightWeightPopup         = true;
    private SingleSelectionModel selectionModel;
    private static final Object classLock = new Object();
    private static final boolean TRACE =   false; 
    private static final boolean VERBOSE = false; 
    private static final boolean DEBUG =   false;  
    public static void setDefaultLightWeightPopupEnabled(boolean aFlag) {
        SwingUtilities.appContextPut(defaultLWPopupEnabledKey,
                                     Boolean.valueOf(aFlag));
    }
    public static boolean getDefaultLightWeightPopupEnabled() {
        Boolean b = (Boolean)
            SwingUtilities.appContextGet(defaultLWPopupEnabledKey);
        if (b == null) {
            SwingUtilities.appContextPut(defaultLWPopupEnabledKey,
                                         Boolean.TRUE);
            return true;
        }
        return b.booleanValue();
    }
    public JPopupMenu() {
        this(null);
    }
    public JPopupMenu(String label) {
        this.label = label;
        lightWeightPopup = getDefaultLightWeightPopupEnabled();
        setSelectionModel(new DefaultSingleSelectionModel());
        enableEvents(AWTEvent.MOUSE_EVENT_MASK);
        setFocusTraversalKeysEnabled(false);
        updateUI();
    }
    public PopupMenuUI getUI() {
        return (PopupMenuUI)ui;
    }
    public void setUI(PopupMenuUI ui) {
        super.setUI(ui);
    }
    public void updateUI() {
        setUI((PopupMenuUI)UIManager.getUI(this));
    }
    public String getUIClassID() {
        return uiClassID;
    }
    protected void processFocusEvent(FocusEvent evt) {
        super.processFocusEvent(evt);
    }
    protected void processKeyEvent(KeyEvent evt) {
        MenuSelectionManager.defaultManager().processKeyEvent(evt);
        if (evt.isConsumed()) {
            return;
        }
        super.processKeyEvent(evt);
    }
    public SingleSelectionModel getSelectionModel() {
        return selectionModel;
    }
    public void setSelectionModel(SingleSelectionModel model) {
        selectionModel = model;
    }
    public JMenuItem add(JMenuItem menuItem) {
        super.add(menuItem);
        return menuItem;
    }
    public JMenuItem add(String s) {
        return add(new JMenuItem(s));
    }
    public JMenuItem add(Action a) {
        JMenuItem mi = createActionComponent(a);
        mi.setAction(a);
        add(mi);
        return mi;
    }
    Point adjustPopupLocationToFitScreen(int xPosition, int yPosition) {
        Point popupLocation = new Point(xPosition, yPosition);
        if(popupPostionFixDisabled == true || GraphicsEnvironment.isHeadless()) {
            return popupLocation;
        }
        Rectangle scrBounds;
        GraphicsConfiguration gc = getCurrentGraphicsConfiguration(popupLocation);
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        if(gc != null) {
            scrBounds = gc.getBounds();
        } else {
            scrBounds = new Rectangle(toolkit.getScreenSize());
        }
        Dimension popupSize = JPopupMenu.this.getPreferredSize();
        long popupRightX = (long)popupLocation.x + (long)popupSize.width;
        long popupBottomY = (long)popupLocation.y + (long)popupSize.height;
        int scrWidth = scrBounds.width;
        int scrHeight = scrBounds.height;
        if (!canPopupOverlapTaskBar()) {
            Insets scrInsets = toolkit.getScreenInsets(gc);
            scrBounds.x += scrInsets.left;
            scrBounds.y += scrInsets.top;
            scrWidth -= scrInsets.left + scrInsets.right;
            scrHeight -= scrInsets.top + scrInsets.bottom;
        }
        int scrRightX = scrBounds.x + scrWidth;
        int scrBottomY = scrBounds.y + scrHeight;
        if (popupRightX > (long)scrRightX) {
            popupLocation.x = scrRightX - popupSize.width;
            if( popupLocation.x < scrBounds.x ) {
                popupLocation.x = scrBounds.x ;
            }
        }
        if (popupBottomY > (long)scrBottomY) {
            popupLocation.y = scrBottomY - popupSize.height;
            if( popupLocation.y < scrBounds.y ) {
                popupLocation.y = scrBounds.y;
            }
        }
        return popupLocation;
    }
    private GraphicsConfiguration getCurrentGraphicsConfiguration(
            Point popupLocation) {
        GraphicsConfiguration gc = null;
        GraphicsEnvironment ge =
            GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] gd = ge.getScreenDevices();
        for(int i = 0; i < gd.length; i++) {
            if(gd[i].getType() == GraphicsDevice.TYPE_RASTER_SCREEN) {
                GraphicsConfiguration dgc =
                    gd[i].getDefaultConfiguration();
                if(dgc.getBounds().contains(popupLocation)) {
                    gc = dgc;
                    break;
                }
            }
        }
        if(gc == null && getInvoker() != null) {
            gc = getInvoker().getGraphicsConfiguration();
        }
        return gc;
    }
    static boolean canPopupOverlapTaskBar() {
        boolean result = true;
        try {
            SecurityManager sm = System.getSecurityManager();
            if (sm != null) {
                sm.checkPermission(
                    SecurityConstants.AWT.SET_WINDOW_ALWAYS_ON_TOP_PERMISSION);
            }
        } catch (SecurityException se) {
            result = false;
        }
        return result;
    }
    protected JMenuItem createActionComponent(Action a) {
        JMenuItem mi = new JMenuItem() {
            protected PropertyChangeListener createActionPropertyChangeListener(Action a) {
                PropertyChangeListener pcl = createActionChangeListener(this);
                if (pcl == null) {
                    pcl = super.createActionPropertyChangeListener(a);
                }
                return pcl;
            }
        };
        mi.setHorizontalTextPosition(JButton.TRAILING);
        mi.setVerticalTextPosition(JButton.CENTER);
        return mi;
    }
    protected PropertyChangeListener createActionChangeListener(JMenuItem b) {
        return b.createActionPropertyChangeListener0(b.getAction());
    }
    public void remove(int pos) {
        if (pos < 0) {
            throw new IllegalArgumentException("index less than zero.");
        }
        if (pos > getComponentCount() -1) {
            throw new IllegalArgumentException("index greater than the number of items.");
        }
        super.remove(pos);
    }
    public void setLightWeightPopupEnabled(boolean aFlag) {
        lightWeightPopup = aFlag;
    }
    public boolean isLightWeightPopupEnabled() {
        return lightWeightPopup;
    }
    public String getLabel() {
        return label;
    }
    public void setLabel(String label) {
        String oldValue = this.label;
        this.label = label;
        firePropertyChange("label", oldValue, label);
        if (accessibleContext != null) {
            accessibleContext.firePropertyChange(
                AccessibleContext.ACCESSIBLE_VISIBLE_DATA_PROPERTY,
                oldValue, label);
        }
        invalidate();
        repaint();
    }
    public void addSeparator() {
        add( new JPopupMenu.Separator() );
    }
    public void insert(Action a, int index) {
        JMenuItem mi = createActionComponent(a);
        mi.setAction(a);
        insert(mi, index);
    }
    public void insert(Component component, int index) {
        if (index < 0) {
            throw new IllegalArgumentException("index less than zero.");
        }
        int nitems = getComponentCount();
        Vector<Component> tempItems = new Vector<Component>();
        for (int i = index ; i < nitems; i++) {
            tempItems.addElement(getComponent(index));
            remove(index);
        }
        add(component);
        for (Component tempItem : tempItems) {
            add(tempItem);
        }
    }
    public void addPopupMenuListener(PopupMenuListener l) {
        listenerList.add(PopupMenuListener.class,l);
    }
    public void removePopupMenuListener(PopupMenuListener l) {
        listenerList.remove(PopupMenuListener.class,l);
    }
    public PopupMenuListener[] getPopupMenuListeners() {
        return listenerList.getListeners(PopupMenuListener.class);
    }
    public void addMenuKeyListener(MenuKeyListener l) {
        listenerList.add(MenuKeyListener.class, l);
    }
    public void removeMenuKeyListener(MenuKeyListener l) {
        listenerList.remove(MenuKeyListener.class, l);
    }
    public MenuKeyListener[] getMenuKeyListeners() {
        return listenerList.getListeners(MenuKeyListener.class);
    }
    protected void firePopupMenuWillBecomeVisible() {
        Object[] listeners = listenerList.getListenerList();
        PopupMenuEvent e=null;
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==PopupMenuListener.class) {
                if (e == null)
                    e = new PopupMenuEvent(this);
                ((PopupMenuListener)listeners[i+1]).popupMenuWillBecomeVisible(e);
            }
        }
    }
    protected void firePopupMenuWillBecomeInvisible() {
        Object[] listeners = listenerList.getListenerList();
        PopupMenuEvent e=null;
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==PopupMenuListener.class) {
                if (e == null)
                    e = new PopupMenuEvent(this);
                ((PopupMenuListener)listeners[i+1]).popupMenuWillBecomeInvisible(e);
            }
        }
    }
    protected void firePopupMenuCanceled() {
        Object[] listeners = listenerList.getListenerList();
        PopupMenuEvent e=null;
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==PopupMenuListener.class) {
                if (e == null)
                    e = new PopupMenuEvent(this);
                ((PopupMenuListener)listeners[i+1]).popupMenuCanceled(e);
            }
        }
    }
    boolean alwaysOnTop() {
        return true;
    }
    public void pack() {
        if(popup != null) {
            Dimension pref = getPreferredSize();
            if (pref == null || pref.width != getWidth() ||
                                pref.height != getHeight()) {
                popup = getPopup();
            } else {
                validate();
            }
        }
    }
    public void setVisible(boolean b) {
        if (DEBUG) {
            System.out.println("JPopupMenu.setVisible " + b);
        }
        if (b == isVisible())
            return;
        if (b == false) {
            Boolean doCanceled = (Boolean)getClientProperty("JPopupMenu.firePopupMenuCanceled");
            if (doCanceled != null && doCanceled == Boolean.TRUE) {
                putClientProperty("JPopupMenu.firePopupMenuCanceled", Boolean.FALSE);
                firePopupMenuCanceled();
            }
            getSelectionModel().clearSelection();
        } else {
            if (isPopupMenu()) {
                MenuElement me[] = new MenuElement[1];
                me[0] = this;
                MenuSelectionManager.defaultManager().setSelectedPath(me);
            }
        }
        if(b) {
            firePopupMenuWillBecomeVisible();
            popup = getPopup();
            firePropertyChange("visible", Boolean.FALSE, Boolean.TRUE);
        } else if(popup != null) {
            firePopupMenuWillBecomeInvisible();
            popup.hide();
            popup = null;
            firePropertyChange("visible", Boolean.TRUE, Boolean.FALSE);
            if (isPopupMenu()) {
                MenuSelectionManager.defaultManager().clearSelectedPath();
            }
        }
    }
    private Popup getPopup() {
        Popup oldPopup = popup;
        if (oldPopup != null) {
            oldPopup.hide();
        }
        PopupFactory popupFactory = PopupFactory.getSharedInstance();
        if (isLightWeightPopupEnabled()) {
            popupFactory.setPopupType(PopupFactory.LIGHT_WEIGHT_POPUP);
        }
        else {
            popupFactory.setPopupType(PopupFactory.MEDIUM_WEIGHT_POPUP);
        }
        Point p = adjustPopupLocationToFitScreen(desiredLocationX,desiredLocationY);
        desiredLocationX = p.x;
        desiredLocationY = p.y;
        Popup newPopup = getUI().getPopup(this, desiredLocationX,
                                          desiredLocationY);
        popupFactory.setPopupType(PopupFactory.LIGHT_WEIGHT_POPUP);
        newPopup.show();
        return newPopup;
    }
    public boolean isVisible() {
        return popup != null;
    }
    public void setLocation(int x, int y) {
        int oldX = desiredLocationX;
        int oldY = desiredLocationY;
        desiredLocationX = x;
        desiredLocationY = y;
        if(popup != null && (x != oldX || y != oldY)) {
            popup = getPopup();
        }
    }
    private boolean isPopupMenu() {
        return  ((invoker != null) && !(invoker instanceof JMenu));
    }
    public Component getInvoker() {
        return this.invoker;
    }
    public void setInvoker(Component invoker) {
        Component oldInvoker = this.invoker;
        this.invoker = invoker;
        if ((oldInvoker != this.invoker) && (ui != null)) {
            ui.uninstallUI(this);
            ui.installUI(this);
        }
        invalidate();
    }
    public void show(Component invoker, int x, int y) {
        if (DEBUG) {
            System.out.println("in JPopupMenu.show " );
        }
        setInvoker(invoker);
        Frame newFrame = getFrame(invoker);
        if (newFrame != frame) {
            if (newFrame!=null) {
                this.frame = newFrame;
                if(popup != null) {
                    setVisible(false);
                }
            }
        }
        Point invokerOrigin;
        if (invoker != null) {
            invokerOrigin = invoker.getLocationOnScreen();
            long lx, ly;
            lx = ((long) invokerOrigin.x) +
                 ((long) x);
            ly = ((long) invokerOrigin.y) +
                 ((long) y);
            if(lx > Integer.MAX_VALUE) lx = Integer.MAX_VALUE;
            if(lx < Integer.MIN_VALUE) lx = Integer.MIN_VALUE;
            if(ly > Integer.MAX_VALUE) ly = Integer.MAX_VALUE;
            if(ly < Integer.MIN_VALUE) ly = Integer.MIN_VALUE;
            setLocation((int) lx, (int) ly);
        } else {
            setLocation(x, y);
        }
        setVisible(true);
    }
    JPopupMenu getRootPopupMenu() {
        JPopupMenu mp = this;
        while((mp!=null) && (mp.isPopupMenu()!=true) &&
              (mp.getInvoker() != null) &&
              (mp.getInvoker().getParent() != null) &&
              (mp.getInvoker().getParent() instanceof JPopupMenu)
              ) {
            mp = (JPopupMenu) mp.getInvoker().getParent();
        }
        return mp;
    }
    @Deprecated
    public Component getComponentAtIndex(int i) {
        return getComponent(i);
    }
    public int getComponentIndex(Component c) {
        int ncomponents = this.getComponentCount();
        Component[] component = this.getComponents();
        for (int i = 0 ; i < ncomponents ; i++) {
            Component comp = component[i];
            if (comp == c)
                return i;
        }
        return -1;
    }
    public void setPopupSize(Dimension d) {
        Dimension oldSize = getPreferredSize();
        setPreferredSize(d);
        if (popup != null) {
            Dimension newSize = getPreferredSize();
            if (!oldSize.equals(newSize)) {
                popup = getPopup();
            }
        }
    }
    public void setPopupSize(int width, int height) {
        setPopupSize(new Dimension(width, height));
    }
    public void setSelected(Component sel) {
        SingleSelectionModel model = getSelectionModel();
        int index = getComponentIndex(sel);
        model.setSelectedIndex(index);
    }
    public boolean isBorderPainted() {
        return paintBorder;
    }
    public void setBorderPainted(boolean b) {
        paintBorder = b;
        repaint();
    }
    protected void paintBorder(Graphics g) {
        if (isBorderPainted()) {
            super.paintBorder(g);
        }
    }
    public Insets getMargin() {
        if(margin == null) {
            return new Insets(0,0,0,0);
        } else {
            return margin;
        }
    }
    boolean isSubPopupMenu(JPopupMenu popup) {
        int ncomponents = this.getComponentCount();
        Component[] component = this.getComponents();
        for (int i = 0 ; i < ncomponents ; i++) {
            Component comp = component[i];
            if (comp instanceof JMenu) {
                JMenu menu = (JMenu)comp;
                JPopupMenu subPopup = menu.getPopupMenu();
                if (subPopup == popup)
                    return true;
                if (subPopup.isSubPopupMenu(popup))
                    return true;
            }
        }
        return false;
    }
    private static Frame getFrame(Component c) {
        Component w = c;
        while(!(w instanceof Frame) && (w!=null)) {
            w = w.getParent();
        }
        return (Frame)w;
    }
    protected String paramString() {
        String labelString = (label != null ?
                              label : "");
        String paintBorderString = (paintBorder ?
                                    "true" : "false");
        String marginString = (margin != null ?
                              margin.toString() : "");
        String lightWeightPopupEnabledString = (isLightWeightPopupEnabled() ?
                                                "true" : "false");
        return super.paramString() +
            ",desiredLocationX=" + desiredLocationX +
            ",desiredLocationY=" + desiredLocationY +
        ",label=" + labelString +
        ",lightWeightPopupEnabled=" + lightWeightPopupEnabledString +
        ",margin=" + marginString +
        ",paintBorder=" + paintBorderString;
    }
    public AccessibleContext getAccessibleContext() {
        if (accessibleContext == null) {
            accessibleContext = new AccessibleJPopupMenu();
        }
        return accessibleContext;
    }
    protected class AccessibleJPopupMenu extends AccessibleJComponent
        implements PropertyChangeListener {
        protected AccessibleJPopupMenu() {
            JPopupMenu.this.addPropertyChangeListener(this);
        }
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.POPUP_MENU;
        }
        public void propertyChange(PropertyChangeEvent e) {
            String propertyName = e.getPropertyName();
            if (propertyName == "visible") {
                if (e.getOldValue() == Boolean.FALSE &&
                    e.getNewValue() == Boolean.TRUE) {
                    handlePopupIsVisibleEvent(true);
                } else if (e.getOldValue() == Boolean.TRUE &&
                           e.getNewValue() == Boolean.FALSE) {
                    handlePopupIsVisibleEvent(false);
                }
            }
        }
        private void handlePopupIsVisibleEvent(boolean visible) {
            if (visible) {
                firePropertyChange(ACCESSIBLE_STATE_PROPERTY,
                                   null, AccessibleState.VISIBLE);
                fireActiveDescendant();
            } else {
                firePropertyChange(ACCESSIBLE_STATE_PROPERTY,
                                   AccessibleState.VISIBLE, null);
            }
        }
        private void fireActiveDescendant() {
            if (JPopupMenu.this instanceof BasicComboPopup) {
                JList popupList = ((BasicComboPopup)JPopupMenu.this).getList();
                if (popupList == null) {
                    return;
                }
                AccessibleContext ac = popupList.getAccessibleContext();
                AccessibleSelection selection = ac.getAccessibleSelection();
                if (selection == null) {
                    return;
                }
                Accessible a = selection.getAccessibleSelection(0);
                if (a == null) {
                    return;
                }
                AccessibleContext selectedItem = a.getAccessibleContext();
                if (selectedItem != null && invoker != null) {
                    AccessibleContext invokerContext = invoker.getAccessibleContext();
                    if (invokerContext != null) {
                        invokerContext.firePropertyChange(
                            ACCESSIBLE_ACTIVE_DESCENDANT_PROPERTY,
                            null, selectedItem);
                    }
                }
            }
        }
    } 
    private void writeObject(ObjectOutputStream s) throws IOException {
        Vector<Object> values = new Vector<Object>();
        s.defaultWriteObject();
        if(invoker != null && invoker instanceof Serializable) {
            values.addElement("invoker");
            values.addElement(invoker);
        }
        if(popup != null && popup instanceof Serializable) {
            values.addElement("popup");
            values.addElement(popup);
        }
        s.writeObject(values);
        if (getUIClassID().equals(uiClassID)) {
            byte count = JComponent.getWriteObjCounter(this);
            JComponent.setWriteObjCounter(this, --count);
            if (count == 0 && ui != null) {
                ui.installUI(this);
            }
        }
    }
    private void readObject(ObjectInputStream s)
        throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        Vector          values = (Vector)s.readObject();
        int             indexCounter = 0;
        int             maxCounter = values.size();
        if(indexCounter < maxCounter && values.elementAt(indexCounter).
           equals("invoker")) {
            invoker = (Component)values.elementAt(++indexCounter);
            indexCounter++;
        }
        if(indexCounter < maxCounter && values.elementAt(indexCounter).
           equals("popup")) {
            popup = (Popup)values.elementAt(++indexCounter);
            indexCounter++;
        }
    }
    public void processMouseEvent(MouseEvent event,MenuElement path[],MenuSelectionManager manager) {}
    public void processKeyEvent(KeyEvent e, MenuElement path[],
                                MenuSelectionManager manager) {
        MenuKeyEvent mke = new MenuKeyEvent(e.getComponent(), e.getID(),
                                             e.getWhen(), e.getModifiers(),
                                             e.getKeyCode(), e.getKeyChar(),
                                             path, manager);
        processMenuKeyEvent(mke);
        if (mke.isConsumed())  {
            e.consume();
    }
    }
    private void processMenuKeyEvent(MenuKeyEvent e) {
        switch (e.getID()) {
        case KeyEvent.KEY_PRESSED:
            fireMenuKeyPressed(e); break;
        case KeyEvent.KEY_RELEASED:
            fireMenuKeyReleased(e); break;
        case KeyEvent.KEY_TYPED:
            fireMenuKeyTyped(e); break;
        default:
            break;
        }
    }
    private void fireMenuKeyPressed(MenuKeyEvent event) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==MenuKeyListener.class) {
                ((MenuKeyListener)listeners[i+1]).menuKeyPressed(event);
            }
        }
    }
    private void fireMenuKeyReleased(MenuKeyEvent event) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==MenuKeyListener.class) {
                ((MenuKeyListener)listeners[i+1]).menuKeyReleased(event);
            }
        }
    }
    private void fireMenuKeyTyped(MenuKeyEvent event) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==MenuKeyListener.class) {
                ((MenuKeyListener)listeners[i+1]).menuKeyTyped(event);
            }
        }
    }
    public void menuSelectionChanged(boolean isIncluded) {
        if (DEBUG) {
            System.out.println("In JPopupMenu.menuSelectionChanged " + isIncluded);
        }
        if(invoker instanceof JMenu) {
            JMenu m = (JMenu) invoker;
            if(isIncluded)
                m.setPopupMenuVisible(true);
            else
                m.setPopupMenuVisible(false);
        }
        if (isPopupMenu() && !isIncluded)
          setVisible(false);
    }
    public MenuElement[] getSubElements() {
        MenuElement result[];
        Vector<MenuElement> tmp = new Vector<MenuElement>();
        int c = getComponentCount();
        int i;
        Component m;
        for(i=0 ; i < c ; i++) {
            m = getComponent(i);
            if(m instanceof MenuElement)
                tmp.addElement((MenuElement) m);
        }
        result = new MenuElement[tmp.size()];
        for(i=0,c=tmp.size() ; i < c ; i++)
            result[i] = tmp.elementAt(i);
        return result;
    }
    public Component getComponent() {
        return this;
    }
    static public class Separator extends JSeparator
    {
        public Separator( )
        {
            super( JSeparator.HORIZONTAL );
        }
        public String getUIClassID()
        {
            return "PopupMenuSeparatorUI";
        }
    }
    public boolean isPopupTrigger(MouseEvent e) {
        return getUI().isPopupTrigger(e);
    }
}
