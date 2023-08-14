public class BasicComboPopup extends JPopupMenu implements ComboPopup {
    private static class EmptyListModelClass implements ListModel,
                                                        Serializable {
        public int getSize() { return 0; }
        public Object getElementAt(int index) { return null; }
        public void addListDataListener(ListDataListener l) {}
        public void removeListDataListener(ListDataListener l) {}
    };
    static final ListModel EmptyListModel = new EmptyListModelClass();
    private static Border LIST_BORDER = new LineBorder(Color.BLACK, 1);
    protected JComboBox                comboBox;
    protected JList                    list;
    protected JScrollPane              scroller;
    protected boolean                  valueIsAdjusting = false;
    private Handler handler;
    protected MouseMotionListener      mouseMotionListener;
    protected MouseListener            mouseListener;
    protected KeyListener              keyListener;
    protected ListSelectionListener    listSelectionListener;
    protected MouseListener            listMouseListener;
    protected MouseMotionListener      listMouseMotionListener;
    protected PropertyChangeListener   propertyChangeListener;
    protected ListDataListener         listDataListener;
    protected ItemListener             itemListener;
    protected Timer                    autoscrollTimer;
    protected boolean                  hasEntered = false;
    protected boolean                  isAutoScrolling = false;
    protected int                      scrollDirection = SCROLL_UP;
    protected static final int         SCROLL_UP = 0;
    protected static final int         SCROLL_DOWN = 1;
    public void show() {
        comboBox.firePopupMenuWillBecomeVisible();
        setListSelection(comboBox.getSelectedIndex());
        Point location = getPopupLocation();
        show( comboBox, location.x, location.y );
    }
    public void hide() {
        MenuSelectionManager manager = MenuSelectionManager.defaultManager();
        MenuElement [] selection = manager.getSelectedPath();
        for ( int i = 0 ; i < selection.length ; i++ ) {
            if ( selection[i] == this ) {
                manager.clearSelectedPath();
                break;
            }
        }
        if (selection.length > 0) {
            comboBox.repaint();
        }
    }
    public JList getList() {
        return list;
    }
    public MouseListener getMouseListener() {
        if (mouseListener == null) {
            mouseListener = createMouseListener();
        }
        return mouseListener;
    }
    public MouseMotionListener getMouseMotionListener() {
        if (mouseMotionListener == null) {
            mouseMotionListener = createMouseMotionListener();
        }
        return mouseMotionListener;
    }
    public KeyListener getKeyListener() {
        if (keyListener == null) {
            keyListener = createKeyListener();
        }
        return keyListener;
    }
    public void uninstallingUI() {
        if (propertyChangeListener != null) {
            comboBox.removePropertyChangeListener( propertyChangeListener );
        }
        if (itemListener != null) {
            comboBox.removeItemListener( itemListener );
        }
        uninstallComboBoxModelListeners(comboBox.getModel());
        uninstallKeyboardActions();
        uninstallListListeners();
        list.setModel(EmptyListModel);
    }
    protected void uninstallComboBoxModelListeners( ComboBoxModel model ) {
        if (model != null && listDataListener != null) {
            model.removeListDataListener(listDataListener);
        }
    }
    protected void uninstallKeyboardActions() {
    }
    public BasicComboPopup( JComboBox combo ) {
        super();
        setName("ComboPopup.popup");
        comboBox = combo;
        setLightWeightPopupEnabled( comboBox.isLightWeightPopupEnabled() );
        list = createList();
        list.setName("ComboBox.list");
        configureList();
        scroller = createScroller();
        scroller.setName("ComboBox.scrollPane");
        configureScroller();
        configurePopup();
        installComboBoxListeners();
        installKeyboardActions();
    }
    protected void firePopupMenuWillBecomeVisible() {
        super.firePopupMenuWillBecomeVisible();
    }
    protected void firePopupMenuWillBecomeInvisible() {
        super.firePopupMenuWillBecomeInvisible();
        comboBox.firePopupMenuWillBecomeInvisible();
    }
    protected void firePopupMenuCanceled() {
        super.firePopupMenuCanceled();
        comboBox.firePopupMenuCanceled();
    }
    protected MouseListener createMouseListener() {
        return getHandler();
    }
    protected MouseMotionListener createMouseMotionListener() {
        return getHandler();
    }
    protected KeyListener createKeyListener() {
        return null;
    }
    protected ListSelectionListener createListSelectionListener() {
        return null;
    }
    protected ListDataListener createListDataListener() {
        return null;
    }
    protected MouseListener createListMouseListener() {
        return getHandler();
    }
    protected MouseMotionListener createListMouseMotionListener() {
        return getHandler();
    }
    protected PropertyChangeListener createPropertyChangeListener() {
        return getHandler();
    }
    protected ItemListener createItemListener() {
        return getHandler();
    }
    private Handler getHandler() {
        if (handler == null) {
            handler = new Handler();
        }
        return handler;
    }
    protected JList createList() {
        return new JList( comboBox.getModel() ) {
            public void processMouseEvent(MouseEvent e)  {
                if (BasicGraphicsUtils.isMenuShortcutKeyDown(e))  {
                    Toolkit toolkit = Toolkit.getDefaultToolkit();
                    e = new MouseEvent((Component)e.getSource(), e.getID(), e.getWhen(),
                                       e.getModifiers() ^ toolkit.getMenuShortcutKeyMask(),
                                       e.getX(), e.getY(),
                                       e.getXOnScreen(), e.getYOnScreen(),
                                       e.getClickCount(),
                                       e.isPopupTrigger(),
                                       MouseEvent.NOBUTTON);
                }
                super.processMouseEvent(e);
            }
        };
    }
    protected void configureList() {
        list.setFont( comboBox.getFont() );
        list.setForeground( comboBox.getForeground() );
        list.setBackground( comboBox.getBackground() );
        list.setSelectionForeground( UIManager.getColor( "ComboBox.selectionForeground" ) );
        list.setSelectionBackground( UIManager.getColor( "ComboBox.selectionBackground" ) );
        list.setBorder( null );
        list.setCellRenderer( comboBox.getRenderer() );
        list.setFocusable( false );
        list.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
        setListSelection( comboBox.getSelectedIndex() );
        installListListeners();
    }
    protected void installListListeners() {
        if ((listMouseListener = createListMouseListener()) != null) {
            list.addMouseListener( listMouseListener );
        }
        if ((listMouseMotionListener = createListMouseMotionListener()) != null) {
            list.addMouseMotionListener( listMouseMotionListener );
        }
        if ((listSelectionListener = createListSelectionListener()) != null) {
            list.addListSelectionListener( listSelectionListener );
        }
    }
    void uninstallListListeners() {
        if (listMouseListener != null) {
            list.removeMouseListener(listMouseListener);
            listMouseListener = null;
        }
        if (listMouseMotionListener != null) {
            list.removeMouseMotionListener(listMouseMotionListener);
            listMouseMotionListener = null;
        }
        if (listSelectionListener != null) {
            list.removeListSelectionListener(listSelectionListener);
            listSelectionListener = null;
        }
        handler = null;
    }
    protected JScrollPane createScroller() {
        JScrollPane sp = new JScrollPane( list,
                                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER );
        sp.setHorizontalScrollBar(null);
        return sp;
    }
    protected void configureScroller() {
        scroller.setFocusable( false );
        scroller.getVerticalScrollBar().setFocusable( false );
        scroller.setBorder( null );
    }
    protected void configurePopup() {
        setLayout( new BoxLayout( this, BoxLayout.Y_AXIS ) );
        setBorderPainted( true );
        setBorder(LIST_BORDER);
        setOpaque( false );
        add( scroller );
        setDoubleBuffered( true );
        setFocusable( false );
    }
    protected void installComboBoxListeners() {
        if ((propertyChangeListener = createPropertyChangeListener()) != null) {
            comboBox.addPropertyChangeListener(propertyChangeListener);
        }
        if ((itemListener = createItemListener()) != null) {
            comboBox.addItemListener(itemListener);
        }
        installComboBoxModelListeners(comboBox.getModel());
    }
    protected void installComboBoxModelListeners( ComboBoxModel model ) {
        if (model != null && (listDataListener = createListDataListener()) != null) {
            model.addListDataListener(listDataListener);
        }
    }
    protected void installKeyboardActions() {
    }
    protected class InvocationMouseHandler extends MouseAdapter {
        public void mousePressed( MouseEvent e ) {
            getHandler().mousePressed(e);
        }
        public void mouseReleased( MouseEvent e ) {
            getHandler().mouseReleased(e);
        }
    }
    protected class InvocationMouseMotionHandler extends MouseMotionAdapter {
        public void mouseDragged( MouseEvent e ) {
            getHandler().mouseDragged(e);
        }
    }
    public class InvocationKeyHandler extends KeyAdapter {
        public void keyReleased( KeyEvent e ) {}
    }
    protected class ListSelectionHandler implements ListSelectionListener {
        public void valueChanged( ListSelectionEvent e ) {}
    }
    public class ListDataHandler implements ListDataListener {
        public void contentsChanged( ListDataEvent e ) {}
        public void intervalAdded( ListDataEvent e ) {
        }
        public void intervalRemoved( ListDataEvent e ) {
        }
    }
    protected class ListMouseHandler extends MouseAdapter {
        public void mousePressed( MouseEvent e ) {
        }
        public void mouseReleased(MouseEvent anEvent) {
            getHandler().mouseReleased(anEvent);
        }
    }
    protected class ListMouseMotionHandler extends MouseMotionAdapter {
        public void mouseMoved( MouseEvent anEvent ) {
            getHandler().mouseMoved(anEvent);
        }
    }
    protected class ItemHandler implements ItemListener {
        public void itemStateChanged( ItemEvent e ) {
            getHandler().itemStateChanged(e);
        }
    }
    protected class PropertyChangeHandler implements PropertyChangeListener {
        public void propertyChange( PropertyChangeEvent e ) {
            getHandler().propertyChange(e);
        }
    }
    private class AutoScrollActionHandler implements ActionListener {
        private int direction;
        AutoScrollActionHandler(int direction) {
            this.direction = direction;
        }
        public void actionPerformed(ActionEvent e) {
            if (direction == SCROLL_UP) {
                autoScrollUp();
            }
            else {
                autoScrollDown();
            }
        }
    }
    private class Handler implements ItemListener, MouseListener,
                          MouseMotionListener, PropertyChangeListener,
                          Serializable {
        public void mouseClicked(MouseEvent e) {
        }
        public void mousePressed(MouseEvent e) {
            if (e.getSource() == list) {
                return;
            }
            if (!SwingUtilities.isLeftMouseButton(e) || !comboBox.isEnabled())
                return;
            if ( comboBox.isEditable() ) {
                Component comp = comboBox.getEditor().getEditorComponent();
                if ((!(comp instanceof JComponent)) || ((JComponent)comp).isRequestFocusEnabled()) {
                    comp.requestFocus();
                }
            }
            else if (comboBox.isRequestFocusEnabled()) {
                comboBox.requestFocus();
            }
            togglePopup();
        }
        public void mouseReleased(MouseEvent e) {
            if (e.getSource() == list) {
                if (list.getModel().getSize() > 0) {
                    if (comboBox.getSelectedIndex() == list.getSelectedIndex()) {
                        comboBox.getEditor().setItem(list.getSelectedValue());
                    }
                    comboBox.setSelectedIndex(list.getSelectedIndex());
                }
                comboBox.setPopupVisible(false);
                if (comboBox.isEditable() && comboBox.getEditor() != null) {
                    comboBox.configureEditor(comboBox.getEditor(),
                                             comboBox.getSelectedItem());
                }
                return;
            }
            Component source = (Component)e.getSource();
            Dimension size = source.getSize();
            Rectangle bounds = new Rectangle( 0, 0, size.width - 1, size.height - 1 );
            if ( !bounds.contains( e.getPoint() ) ) {
                MouseEvent newEvent = convertMouseEvent( e );
                Point location = newEvent.getPoint();
                Rectangle r = new Rectangle();
                list.computeVisibleRect( r );
                if ( r.contains( location ) ) {
                    if (comboBox.getSelectedIndex() == list.getSelectedIndex()) {
                        comboBox.getEditor().setItem(list.getSelectedValue());
                    }
                    comboBox.setSelectedIndex(list.getSelectedIndex());
                }
                comboBox.setPopupVisible(false);
            }
            hasEntered = false;
            stopAutoScrolling();
        }
        public void mouseEntered(MouseEvent e) {
        }
        public void mouseExited(MouseEvent e) {
        }
        public void mouseMoved(MouseEvent anEvent) {
            if (anEvent.getSource() == list) {
                Point location = anEvent.getPoint();
                Rectangle r = new Rectangle();
                list.computeVisibleRect( r );
                if ( r.contains( location ) ) {
                    updateListBoxSelectionForEvent( anEvent, false );
                }
            }
        }
        public void mouseDragged( MouseEvent e ) {
            if (e.getSource() == list) {
                return;
            }
            if ( isVisible() ) {
                MouseEvent newEvent = convertMouseEvent( e );
                Rectangle r = new Rectangle();
                list.computeVisibleRect( r );
                if ( newEvent.getPoint().y >= r.y && newEvent.getPoint().y <= r.y + r.height - 1 ) {
                    hasEntered = true;
                    if ( isAutoScrolling ) {
                        stopAutoScrolling();
                    }
                    Point location = newEvent.getPoint();
                    if ( r.contains( location ) ) {
                        updateListBoxSelectionForEvent( newEvent, false );
                    }
                }
                else {
                    if ( hasEntered ) {
                        int directionToScroll = newEvent.getPoint().y < r.y ? SCROLL_UP : SCROLL_DOWN;
                        if ( isAutoScrolling && scrollDirection != directionToScroll ) {
                            stopAutoScrolling();
                            startAutoScrolling( directionToScroll );
                        }
                        else if ( !isAutoScrolling ) {
                            startAutoScrolling( directionToScroll );
                        }
                    }
                    else {
                        if ( e.getPoint().y < 0 ) {
                            hasEntered = true;
                            startAutoScrolling( SCROLL_UP );
                        }
                    }
                }
            }
        }
        public void propertyChange(PropertyChangeEvent e) {
            JComboBox comboBox = (JComboBox)e.getSource();
            String propertyName = e.getPropertyName();
            if ( propertyName == "model" ) {
                ComboBoxModel oldModel = (ComboBoxModel)e.getOldValue();
                ComboBoxModel newModel = (ComboBoxModel)e.getNewValue();
                uninstallComboBoxModelListeners(oldModel);
                installComboBoxModelListeners(newModel);
                list.setModel(newModel);
                if ( isVisible() ) {
                    hide();
                }
            }
            else if ( propertyName == "renderer" ) {
                list.setCellRenderer( comboBox.getRenderer() );
                if ( isVisible() ) {
                    hide();
                }
            }
            else if (propertyName == "componentOrientation") {
                ComponentOrientation o =(ComponentOrientation)e.getNewValue();
                JList list = getList();
                if (list!=null && list.getComponentOrientation()!=o) {
                    list.setComponentOrientation(o);
                }
                if (scroller!=null && scroller.getComponentOrientation()!=o) {
                    scroller.setComponentOrientation(o);
                }
                if (o!=getComponentOrientation()) {
                    setComponentOrientation(o);
                }
            }
            else if (propertyName == "lightWeightPopupEnabled") {
                setLightWeightPopupEnabled(comboBox.isLightWeightPopupEnabled());
            }
        }
        public void itemStateChanged( ItemEvent e ) {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                JComboBox comboBox = (JComboBox)e.getSource();
                setListSelection(comboBox.getSelectedIndex());
            }
        }
    }
    public boolean isFocusTraversable() {
        return false;
    }
    protected void startAutoScrolling( int direction ) {
        if ( isAutoScrolling ) {
            autoscrollTimer.stop();
        }
        isAutoScrolling = true;
        if ( direction == SCROLL_UP ) {
            scrollDirection = SCROLL_UP;
            Point convertedPoint = SwingUtilities.convertPoint( scroller, new Point( 1, 1 ), list );
            int top = list.locationToIndex( convertedPoint );
            list.setSelectedIndex( top );
            autoscrollTimer = new Timer( 100, new AutoScrollActionHandler(
                                             SCROLL_UP) );
        }
        else if ( direction == SCROLL_DOWN ) {
            scrollDirection = SCROLL_DOWN;
            Dimension size = scroller.getSize();
            Point convertedPoint = SwingUtilities.convertPoint( scroller,
                                                                new Point( 1, (size.height - 1) - 2 ),
                                                                list );
            int bottom = list.locationToIndex( convertedPoint );
            list.setSelectedIndex( bottom );
            autoscrollTimer = new Timer(100, new AutoScrollActionHandler(
                                            SCROLL_DOWN));
        }
        autoscrollTimer.start();
    }
    protected void stopAutoScrolling() {
        isAutoScrolling = false;
        if ( autoscrollTimer != null ) {
            autoscrollTimer.stop();
            autoscrollTimer = null;
        }
    }
    protected void autoScrollUp() {
        int index = list.getSelectedIndex();
        if ( index > 0 ) {
            list.setSelectedIndex( index - 1 );
            list.ensureIndexIsVisible( index - 1 );
        }
    }
    protected void autoScrollDown() {
        int index = list.getSelectedIndex();
        int lastItem = list.getModel().getSize() - 1;
        if ( index < lastItem ) {
            list.setSelectedIndex( index + 1 );
            list.ensureIndexIsVisible( index + 1 );
        }
    }
    public AccessibleContext getAccessibleContext() {
        AccessibleContext context = super.getAccessibleContext();
        context.setAccessibleParent(comboBox);
        return context;
    }
    protected void delegateFocus( MouseEvent e ) {
        if ( comboBox.isEditable() ) {
            Component comp = comboBox.getEditor().getEditorComponent();
            if ((!(comp instanceof JComponent)) || ((JComponent)comp).isRequestFocusEnabled()) {
                comp.requestFocus();
            }
        }
        else if (comboBox.isRequestFocusEnabled()) {
            comboBox.requestFocus();
        }
    }
    protected void togglePopup() {
        if ( isVisible() ) {
            hide();
        }
        else {
            show();
        }
    }
    private void setListSelection(int selectedIndex) {
        if ( selectedIndex == -1 ) {
            list.clearSelection();
        }
        else {
            list.setSelectedIndex( selectedIndex );
            list.ensureIndexIsVisible( selectedIndex );
        }
    }
    protected MouseEvent convertMouseEvent( MouseEvent e ) {
        Point convertedPoint = SwingUtilities.convertPoint( (Component)e.getSource(),
                                                            e.getPoint(), list );
        MouseEvent newEvent = new MouseEvent( (Component)e.getSource(),
                                              e.getID(),
                                              e.getWhen(),
                                              e.getModifiers(),
                                              convertedPoint.x,
                                              convertedPoint.y,
                                              e.getXOnScreen(),
                                              e.getYOnScreen(),
                                              e.getClickCount(),
                                              e.isPopupTrigger(),
                                              MouseEvent.NOBUTTON );
        return newEvent;
    }
    protected int getPopupHeightForRowCount(int maxRowCount) {
        int minRowCount = Math.min( maxRowCount, comboBox.getItemCount() );
        int height = 0;
        ListCellRenderer renderer = list.getCellRenderer();
        Object value = null;
        for ( int i = 0; i < minRowCount; ++i ) {
            value = list.getModel().getElementAt( i );
            Component c = renderer.getListCellRendererComponent( list, value, i, false, false );
            height += c.getPreferredSize().height;
        }
        if (height == 0) {
            height = comboBox.getHeight();
        }
        Border border = scroller.getViewportBorder();
        if (border != null) {
            Insets insets = border.getBorderInsets(null);
            height += insets.top + insets.bottom;
        }
        border = scroller.getBorder();
        if (border != null) {
            Insets insets = border.getBorderInsets(null);
            height += insets.top + insets.bottom;
        }
        return height;
    }
    protected Rectangle computePopupBounds(int px,int py,int pw,int ph) {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Rectangle screenBounds;
        GraphicsConfiguration gc = comboBox.getGraphicsConfiguration();
        Point p = new Point();
        SwingUtilities.convertPointFromScreen(p, comboBox);
        if (gc != null) {
            Insets screenInsets = toolkit.getScreenInsets(gc);
            screenBounds = gc.getBounds();
            screenBounds.width -= (screenInsets.left + screenInsets.right);
            screenBounds.height -= (screenInsets.top + screenInsets.bottom);
            screenBounds.x += (p.x + screenInsets.left);
            screenBounds.y += (p.y + screenInsets.top);
        }
        else {
            screenBounds = new Rectangle(p, toolkit.getScreenSize());
        }
        Rectangle rect = new Rectangle(px,py,pw,ph);
        if (py+ph > screenBounds.y+screenBounds.height
            && ph < screenBounds.height) {
            rect.y = -rect.height;
        }
        return rect;
    }
    private Point getPopupLocation() {
        Dimension popupSize = comboBox.getSize();
        Insets insets = getInsets();
        popupSize.setSize(popupSize.width - (insets.right + insets.left),
                          getPopupHeightForRowCount( comboBox.getMaximumRowCount()));
        Rectangle popupBounds = computePopupBounds( 0, comboBox.getBounds().height,
                                                    popupSize.width, popupSize.height);
        Dimension scrollSize = popupBounds.getSize();
        Point popupLocation = popupBounds.getLocation();
        scroller.setMaximumSize( scrollSize );
        scroller.setPreferredSize( scrollSize );
        scroller.setMinimumSize( scrollSize );
        list.revalidate();
        return popupLocation;
    }
    protected void updateListBoxSelectionForEvent(MouseEvent anEvent,boolean shouldScroll) {
        Point location = anEvent.getPoint();
        if ( list == null )
            return;
        int index = list.locationToIndex(location);
        if ( index == -1 ) {
            if ( location.y < 0 )
                index = 0;
            else
                index = comboBox.getModel().getSize() - 1;
        }
        if ( list.getSelectedIndex() != index ) {
            list.setSelectedIndex(index);
            if ( shouldScroll )
                list.ensureIndexIsVisible(index);
        }
    }
}
