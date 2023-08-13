public class BasicComboBoxUI extends ComboBoxUI {
    protected JComboBox comboBox;
    protected boolean   hasFocus = false;
    private boolean isTableCellEditor = false;
    private static final String IS_TABLE_CELL_EDITOR = "JComboBox.isTableCellEditor";
    protected JList   listBox;
    protected CellRendererPane currentValuePane = new CellRendererPane();
    protected ComboPopup popup;
    protected Component editor;
    protected JButton   arrowButton;
    protected KeyListener keyListener;
    protected FocusListener focusListener;
    protected PropertyChangeListener propertyChangeListener;
    protected ItemListener itemListener;
    protected MouseListener popupMouseListener;
    protected MouseMotionListener popupMouseMotionListener;
    protected KeyListener popupKeyListener;
    protected ListDataListener listDataListener;
    private Handler handler;
    private long timeFactor = 1000L;
    private long lastTime = 0L;
    private long time = 0L;
    JComboBox.KeySelectionManager keySelectionManager;
    protected boolean isMinimumSizeDirty = true;
    protected Dimension cachedMinimumSize = new Dimension( 0, 0 );
    private boolean isDisplaySizeDirty = true;
    private Dimension cachedDisplaySize = new Dimension( 0, 0 );
    private static final Object COMBO_UI_LIST_CELL_RENDERER_KEY =
                        new StringBuffer("DefaultListCellRendererKey");
    static final StringBuffer HIDE_POPUP_KEY
                  = new StringBuffer("HidePopupKey");
    private boolean sameBaseline;
    protected boolean squareButton = true;
    protected Insets padding;
    private static ListCellRenderer getDefaultListCellRenderer() {
        ListCellRenderer renderer = (ListCellRenderer)AppContext.
                         getAppContext().get(COMBO_UI_LIST_CELL_RENDERER_KEY);
        if (renderer == null) {
            renderer = new DefaultListCellRenderer();
            AppContext.getAppContext().put(COMBO_UI_LIST_CELL_RENDERER_KEY,
                                           new DefaultListCellRenderer());
        }
        return renderer;
    }
    static void loadActionMap(LazyActionMap map) {
        map.put(new Actions(Actions.HIDE));
        map.put(new Actions(Actions.PAGE_DOWN));
        map.put(new Actions(Actions.PAGE_UP));
        map.put(new Actions(Actions.HOME));
        map.put(new Actions(Actions.END));
        map.put(new Actions(Actions.DOWN));
        map.put(new Actions(Actions.DOWN_2));
        map.put(new Actions(Actions.TOGGLE));
        map.put(new Actions(Actions.TOGGLE_2));
        map.put(new Actions(Actions.UP));
        map.put(new Actions(Actions.UP_2));
        map.put(new Actions(Actions.ENTER));
    }
    public static ComponentUI createUI(JComponent c) {
        return new BasicComboBoxUI();
    }
    @Override
    public void installUI( JComponent c ) {
        isMinimumSizeDirty = true;
        comboBox = (JComboBox)c;
        installDefaults();
        popup = createPopup();
        listBox = popup.getList();
        Boolean inTable = (Boolean)c.getClientProperty(IS_TABLE_CELL_EDITOR );
        if (inTable != null) {
            isTableCellEditor = inTable.equals(Boolean.TRUE) ? true : false;
        }
        if ( comboBox.getRenderer() == null || comboBox.getRenderer() instanceof UIResource ) {
            comboBox.setRenderer( createRenderer() );
        }
        if ( comboBox.getEditor() == null || comboBox.getEditor() instanceof UIResource ) {
            comboBox.setEditor( createEditor() );
        }
        installListeners();
        installComponents();
        comboBox.setLayout( createLayoutManager() );
        comboBox.setRequestFocusEnabled( true );
        installKeyboardActions();
        comboBox.putClientProperty("doNotCancelPopup", HIDE_POPUP_KEY);
        if (keySelectionManager == null || keySelectionManager instanceof UIResource) {
            keySelectionManager = new DefaultKeySelectionManager();
        }
        comboBox.setKeySelectionManager(keySelectionManager);
    }
    @Override
    public void uninstallUI( JComponent c ) {
        setPopupVisible( comboBox, false);
        popup.uninstallingUI();
        uninstallKeyboardActions();
        comboBox.setLayout( null );
        uninstallComponents();
        uninstallListeners();
        uninstallDefaults();
        if ( comboBox.getRenderer() == null || comboBox.getRenderer() instanceof UIResource ) {
            comboBox.setRenderer( null );
        }
        ComboBoxEditor comboBoxEditor = comboBox.getEditor();
        if (comboBoxEditor instanceof UIResource ) {
            if (comboBoxEditor.getEditorComponent().hasFocus()) {
                comboBox.requestFocusInWindow();
            }
            comboBox.setEditor( null );
        }
        if (keySelectionManager instanceof UIResource) {
            comboBox.setKeySelectionManager(null);
        }
        handler = null;
        keyListener = null;
        focusListener = null;
        listDataListener = null;
        propertyChangeListener = null;
        popup = null;
        listBox = null;
        comboBox = null;
    }
    protected void installDefaults() {
        LookAndFeel.installColorsAndFont( comboBox,
                                          "ComboBox.background",
                                          "ComboBox.foreground",
                                          "ComboBox.font" );
        LookAndFeel.installBorder( comboBox, "ComboBox.border" );
        LookAndFeel.installProperty( comboBox, "opaque", Boolean.TRUE);
        Long l = (Long)UIManager.get("ComboBox.timeFactor");
        timeFactor = l == null ? 1000L : l.longValue();
        Boolean b = (Boolean)UIManager.get("ComboBox.squareButton");
        squareButton = b == null ? true : b;
        padding = UIManager.getInsets("ComboBox.padding");
    }
    protected void installListeners() {
        if ( (itemListener = createItemListener()) != null) {
            comboBox.addItemListener( itemListener );
        }
        if ( (propertyChangeListener = createPropertyChangeListener()) != null ) {
            comboBox.addPropertyChangeListener( propertyChangeListener );
        }
        if ( (keyListener = createKeyListener()) != null ) {
            comboBox.addKeyListener( keyListener );
        }
        if ( (focusListener = createFocusListener()) != null ) {
            comboBox.addFocusListener( focusListener );
        }
        if ((popupMouseListener = popup.getMouseListener()) != null) {
            comboBox.addMouseListener( popupMouseListener );
        }
        if ((popupMouseMotionListener = popup.getMouseMotionListener()) != null) {
            comboBox.addMouseMotionListener( popupMouseMotionListener );
        }
        if ((popupKeyListener = popup.getKeyListener()) != null) {
            comboBox.addKeyListener(popupKeyListener);
        }
        if ( comboBox.getModel() != null ) {
            if ( (listDataListener = createListDataListener()) != null ) {
                comboBox.getModel().addListDataListener( listDataListener );
            }
        }
    }
    protected void uninstallDefaults() {
        LookAndFeel.installColorsAndFont( comboBox,
                                          "ComboBox.background",
                                          "ComboBox.foreground",
                                          "ComboBox.font" );
        LookAndFeel.uninstallBorder( comboBox );
    }
    protected void uninstallListeners() {
        if ( keyListener != null ) {
            comboBox.removeKeyListener( keyListener );
        }
        if ( itemListener != null) {
            comboBox.removeItemListener( itemListener );
        }
        if ( propertyChangeListener != null ) {
            comboBox.removePropertyChangeListener( propertyChangeListener );
        }
        if ( focusListener != null) {
            comboBox.removeFocusListener( focusListener );
        }
        if ( popupMouseListener != null) {
            comboBox.removeMouseListener( popupMouseListener );
        }
        if ( popupMouseMotionListener != null) {
            comboBox.removeMouseMotionListener( popupMouseMotionListener );
        }
        if (popupKeyListener != null) {
            comboBox.removeKeyListener(popupKeyListener);
        }
        if ( comboBox.getModel() != null ) {
            if ( listDataListener != null ) {
                comboBox.getModel().removeListDataListener( listDataListener );
            }
        }
    }
    protected ComboPopup createPopup() {
        return new BasicComboPopup( comboBox );
    }
    protected KeyListener createKeyListener() {
        return getHandler();
    }
    protected FocusListener createFocusListener() {
        return getHandler();
    }
    protected ListDataListener createListDataListener() {
        return getHandler();
    }
    protected ItemListener createItemListener() {
        return null;
    }
    protected PropertyChangeListener createPropertyChangeListener() {
        return getHandler();
    }
    protected LayoutManager createLayoutManager() {
        return getHandler();
    }
    protected ListCellRenderer createRenderer() {
        return new BasicComboBoxRenderer.UIResource();
    }
    protected ComboBoxEditor createEditor() {
        return new BasicComboBoxEditor.UIResource();
    }
    private Handler getHandler() {
        if (handler == null) {
            handler = new Handler();
        }
        return handler;
    }
    public class KeyHandler extends KeyAdapter {
        @Override
        public void keyPressed( KeyEvent e ) {
            getHandler().keyPressed(e);
        }
    }
    public class FocusHandler implements FocusListener {
        public void focusGained( FocusEvent e ) {
            getHandler().focusGained(e);
        }
        public void focusLost( FocusEvent e ) {
            getHandler().focusLost(e);
        }
    }
    public class ListDataHandler implements ListDataListener {
        public void contentsChanged( ListDataEvent e ) {
            getHandler().contentsChanged(e);
        }
        public void intervalAdded( ListDataEvent e ) {
            getHandler().intervalAdded(e);
        }
        public void intervalRemoved( ListDataEvent e ) {
            getHandler().intervalRemoved(e);
        }
    }
    public class ItemHandler implements ItemListener {
        public void itemStateChanged(ItemEvent e) {}
    }
    public class PropertyChangeHandler implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent e) {
            getHandler().propertyChange(e);
        }
    }
    private void updateToolTipTextForChildren() {
        Component[] children = comboBox.getComponents();
        for ( int i = 0; i < children.length; ++i ) {
            if ( children[i] instanceof JComponent ) {
                ((JComponent)children[i]).setToolTipText( comboBox.getToolTipText() );
            }
        }
    }
    public class ComboBoxLayoutManager implements LayoutManager {
        public void addLayoutComponent(String name, Component comp) {}
        public void removeLayoutComponent(Component comp) {}
        public Dimension preferredLayoutSize(Container parent) {
            return getHandler().preferredLayoutSize(parent);
        }
        public Dimension minimumLayoutSize(Container parent) {
            return getHandler().minimumLayoutSize(parent);
        }
        public void layoutContainer(Container parent) {
            getHandler().layoutContainer(parent);
        }
    }
    protected void installComponents() {
        arrowButton = createArrowButton();
        comboBox.add( arrowButton );
        if (arrowButton != null)  {
            configureArrowButton();
        }
        if ( comboBox.isEditable() ) {
            addEditor();
        }
        comboBox.add( currentValuePane );
    }
    protected void uninstallComponents() {
        if ( arrowButton != null ) {
            unconfigureArrowButton();
        }
        if ( editor != null ) {
            unconfigureEditor();
        }
        comboBox.removeAll(); 
        arrowButton = null;
    }
    public void addEditor() {
        removeEditor();
        editor = comboBox.getEditor().getEditorComponent();
        if ( editor != null ) {
            configureEditor();
            comboBox.add(editor);
            if(comboBox.isFocusOwner()) {
                editor.requestFocusInWindow();
            }
        }
    }
    public void removeEditor() {
        if ( editor != null ) {
            unconfigureEditor();
            comboBox.remove( editor );
            editor = null;
        }
    }
    protected void configureEditor() {
        editor.setEnabled(comboBox.isEnabled());
        editor.setFocusable(comboBox.isFocusable());
        editor.setFont( comboBox.getFont() );
        if (focusListener != null) {
            editor.addFocusListener(focusListener);
        }
        editor.addFocusListener( getHandler() );
        comboBox.getEditor().addActionListener(getHandler());
        if(editor instanceof JComponent) {
            ((JComponent)editor).putClientProperty("doNotCancelPopup",
                                                   HIDE_POPUP_KEY);
            ((JComponent)editor).setInheritsPopupMenu(true);
        }
        comboBox.configureEditor(comboBox.getEditor(),comboBox.getSelectedItem());
        editor.addPropertyChangeListener(propertyChangeListener);
    }
    protected void unconfigureEditor() {
        if (focusListener != null) {
            editor.removeFocusListener(focusListener);
        }
        editor.removePropertyChangeListener(propertyChangeListener);
        editor.removeFocusListener(getHandler());
        comboBox.getEditor().removeActionListener(getHandler());
    }
    public void configureArrowButton() {
        if ( arrowButton != null ) {
            arrowButton.setEnabled( comboBox.isEnabled() );
            arrowButton.setFocusable(comboBox.isFocusable());
            arrowButton.setRequestFocusEnabled(false);
            arrowButton.addMouseListener( popup.getMouseListener() );
            arrowButton.addMouseMotionListener( popup.getMouseMotionListener() );
            arrowButton.resetKeyboardActions();
            arrowButton.putClientProperty("doNotCancelPopup", HIDE_POPUP_KEY);
            arrowButton.setInheritsPopupMenu(true);
        }
    }
    public void unconfigureArrowButton() {
        if ( arrowButton != null ) {
            arrowButton.removeMouseListener( popup.getMouseListener() );
            arrowButton.removeMouseMotionListener( popup.getMouseMotionListener() );
        }
    }
    protected JButton createArrowButton() {
        JButton button = new BasicArrowButton(BasicArrowButton.SOUTH,
                                    UIManager.getColor("ComboBox.buttonBackground"),
                                    UIManager.getColor("ComboBox.buttonShadow"),
                                    UIManager.getColor("ComboBox.buttonDarkShadow"),
                                    UIManager.getColor("ComboBox.buttonHighlight"));
        button.setName("ComboBox.arrowButton");
        return button;
    }
    public boolean isPopupVisible( JComboBox c ) {
        return popup.isVisible();
    }
    public void setPopupVisible( JComboBox c, boolean v ) {
        if ( v ) {
            popup.show();
        } else {
            popup.hide();
        }
    }
    public boolean isFocusTraversable( JComboBox c ) {
        return !comboBox.isEditable();
    }
    @Override
    public void paint( Graphics g, JComponent c ) {
        hasFocus = comboBox.hasFocus();
        if ( !comboBox.isEditable() ) {
            Rectangle r = rectangleForCurrentValue();
            paintCurrentValueBackground(g,r,hasFocus);
            paintCurrentValue(g,r,hasFocus);
        }
    }
    @Override
    public Dimension getPreferredSize( JComponent c ) {
        return getMinimumSize(c);
    }
    @Override
    public Dimension getMinimumSize( JComponent c ) {
        if ( !isMinimumSizeDirty ) {
            return new Dimension(cachedMinimumSize);
        }
        Dimension size = getDisplaySize();
        Insets insets = getInsets();
        int buttonHeight = size.height;
        int buttonWidth = squareButton ? buttonHeight : arrowButton.getPreferredSize().width;
        size.height += insets.top + insets.bottom;
        size.width +=  insets.left + insets.right + buttonWidth;
        cachedMinimumSize.setSize( size.width, size.height );
        isMinimumSizeDirty = false;
        return new Dimension(size);
    }
    @Override
    public Dimension getMaximumSize( JComponent c ) {
        return new Dimension(Short.MAX_VALUE, Short.MAX_VALUE);
    }
    @Override
    public int getBaseline(JComponent c, int width, int height) {
        super.getBaseline(c, width, height);
        int baseline = -1;
        getDisplaySize();
        if (sameBaseline) {
            Insets insets = c.getInsets();
            height = height - insets.top - insets.bottom;
            if (!comboBox.isEditable()) {
                ListCellRenderer renderer = comboBox.getRenderer();
                if (renderer == null)  {
                    renderer = new DefaultListCellRenderer();
                }
                Object value = null;
                Object prototypeValue = comboBox.getPrototypeDisplayValue();
                if (prototypeValue != null)  {
                    value = prototypeValue;
                }
                else if (comboBox.getModel().getSize() > 0) {
                    value = comboBox.getModel().getElementAt(0);
                }
                if (value == null) {
                    value = " ";
                } else if (value instanceof String && "".equals(value)) {
                    value = " ";
                }
                Component component = renderer.
                        getListCellRendererComponent(listBox, value, -1,
                                                     false, false);
                if (component instanceof JComponent) {
                    component.setFont(comboBox.getFont());
                }
                baseline = component.getBaseline(width, height);
            }
            else {
                baseline = editor.getBaseline(width, height);
            }
            if (baseline > 0) {
                baseline += insets.top;
            }
        }
        return baseline;
    }
    @Override
    public Component.BaselineResizeBehavior getBaselineResizeBehavior(
            JComponent c) {
        super.getBaselineResizeBehavior(c);
        getDisplaySize();
        if (comboBox.isEditable()) {
            return editor.getBaselineResizeBehavior();
        }
        else if (sameBaseline) {
            ListCellRenderer renderer = comboBox.getRenderer();
            if (renderer == null)  {
                renderer = new DefaultListCellRenderer();
            }
            Object value = null;
            Object prototypeValue = comboBox.getPrototypeDisplayValue();
            if (prototypeValue != null)  {
                value = prototypeValue;
            }
            else if (comboBox.getModel().getSize() > 0) {
                value = comboBox.getModel().getElementAt(0);
            }
            if (value != null) {
                Component component = renderer.
                        getListCellRendererComponent(listBox, value, -1,
                                                     false, false);
                return component.getBaselineResizeBehavior();
            }
        }
        return Component.BaselineResizeBehavior.OTHER;
    }
    @Override
    public int getAccessibleChildrenCount(JComponent c) {
        if ( comboBox.isEditable() ) {
            return 2;
        }
        else {
            return 1;
        }
    }
    @Override
    public Accessible getAccessibleChild(JComponent c, int i) {
        switch ( i ) {
        case 0:
            if ( popup instanceof Accessible ) {
                AccessibleContext ac = ((Accessible) popup).getAccessibleContext();
                ac.setAccessibleParent(comboBox);
                return(Accessible) popup;
            }
            break;
        case 1:
            if ( comboBox.isEditable()
                 && (editor instanceof Accessible) ) {
                AccessibleContext ac = ((Accessible) editor).getAccessibleContext();
                ac.setAccessibleParent(comboBox);
                return(Accessible) editor;
            }
            break;
        }
        return null;
    }
    protected boolean isNavigationKey( int keyCode ) {
        return keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_DOWN ||
               keyCode == KeyEvent.VK_KP_UP || keyCode == KeyEvent.VK_KP_DOWN;
    }
    private boolean isNavigationKey(int keyCode, int modifiers) {
        InputMap inputMap = comboBox.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        KeyStroke key = KeyStroke.getKeyStroke(keyCode, modifiers);
        if (inputMap != null && inputMap.get(key) != null) {
            return true;
        }
        return false;
    }
    protected void selectNextPossibleValue() {
        int si;
        if ( comboBox.isPopupVisible() ) {
            si = listBox.getSelectedIndex();
        }
        else {
            si = comboBox.getSelectedIndex();
        }
        if ( si < comboBox.getModel().getSize() - 1 ) {
            listBox.setSelectedIndex( si + 1 );
            listBox.ensureIndexIsVisible( si + 1 );
            if ( !isTableCellEditor ) {
                comboBox.setSelectedIndex(si+1);
            }
            comboBox.repaint();
        }
    }
    protected void selectPreviousPossibleValue() {
        int si;
        if ( comboBox.isPopupVisible() ) {
            si = listBox.getSelectedIndex();
        }
        else {
            si = comboBox.getSelectedIndex();
        }
        if ( si > 0 ) {
            listBox.setSelectedIndex( si - 1 );
            listBox.ensureIndexIsVisible( si - 1 );
            if ( !isTableCellEditor ) {
                comboBox.setSelectedIndex(si-1);
            }
            comboBox.repaint();
        }
    }
    protected void toggleOpenClose() {
        setPopupVisible(comboBox, !isPopupVisible(comboBox));
    }
    protected Rectangle rectangleForCurrentValue() {
        int width = comboBox.getWidth();
        int height = comboBox.getHeight();
        Insets insets = getInsets();
        int buttonSize = height - (insets.top + insets.bottom);
        if ( arrowButton != null ) {
            buttonSize = arrowButton.getWidth();
        }
        if(BasicGraphicsUtils.isLeftToRight(comboBox)) {
            return new Rectangle(insets.left, insets.top,
                             width - (insets.left + insets.right + buttonSize),
                             height - (insets.top + insets.bottom));
        }
        else {
            return new Rectangle(insets.left + buttonSize, insets.top,
                             width - (insets.left + insets.right + buttonSize),
                             height - (insets.top + insets.bottom));
        }
    }
    protected Insets getInsets() {
        return comboBox.getInsets();
    }
    public void paintCurrentValue(Graphics g,Rectangle bounds,boolean hasFocus) {
        ListCellRenderer renderer = comboBox.getRenderer();
        Component c;
        if ( hasFocus && !isPopupVisible(comboBox) ) {
            c = renderer.getListCellRendererComponent( listBox,
                                                       comboBox.getSelectedItem(),
                                                       -1,
                                                       true,
                                                       false );
        }
        else {
            c = renderer.getListCellRendererComponent( listBox,
                                                       comboBox.getSelectedItem(),
                                                       -1,
                                                       false,
                                                       false );
            c.setBackground(UIManager.getColor("ComboBox.background"));
        }
        c.setFont(comboBox.getFont());
        if ( hasFocus && !isPopupVisible(comboBox) ) {
            c.setForeground(listBox.getSelectionForeground());
            c.setBackground(listBox.getSelectionBackground());
        }
        else {
            if ( comboBox.isEnabled() ) {
                c.setForeground(comboBox.getForeground());
                c.setBackground(comboBox.getBackground());
            }
            else {
                c.setForeground(DefaultLookup.getColor(
                         comboBox, this, "ComboBox.disabledForeground", null));
                c.setBackground(DefaultLookup.getColor(
                         comboBox, this, "ComboBox.disabledBackground", null));
            }
        }
        boolean shouldValidate = false;
        if (c instanceof JPanel)  {
            shouldValidate = true;
        }
        int x = bounds.x, y = bounds.y, w = bounds.width, h = bounds.height;
        if (padding != null) {
            x = bounds.x + padding.left;
            y = bounds.y + padding.top;
            w = bounds.width - (padding.left + padding.right);
            h = bounds.height - (padding.top + padding.bottom);
        }
        currentValuePane.paintComponent(g,c,comboBox,x,y,w,h,shouldValidate);
    }
    public void paintCurrentValueBackground(Graphics g,Rectangle bounds,boolean hasFocus) {
        Color t = g.getColor();
        if ( comboBox.isEnabled() )
            g.setColor(DefaultLookup.getColor(comboBox, this,
                                              "ComboBox.background", null));
        else
            g.setColor(DefaultLookup.getColor(comboBox, this,
                                     "ComboBox.disabledBackground", null));
        g.fillRect(bounds.x,bounds.y,bounds.width,bounds.height);
        g.setColor(t);
    }
    void repaintCurrentValue() {
        Rectangle r = rectangleForCurrentValue();
        comboBox.repaint(r.x,r.y,r.width,r.height);
    }
    protected Dimension getDefaultSize() {
        Dimension d = getSizeForComponent(getDefaultListCellRenderer().getListCellRendererComponent(listBox, " ", -1, false, false));
        return new Dimension(d.width, d.height);
    }
    protected Dimension getDisplaySize() {
        if (!isDisplaySizeDirty)  {
            return new Dimension(cachedDisplaySize);
        }
        Dimension result = new Dimension();
        ListCellRenderer renderer = comboBox.getRenderer();
        if (renderer == null)  {
            renderer = new DefaultListCellRenderer();
        }
        sameBaseline = true;
        Object prototypeValue = comboBox.getPrototypeDisplayValue();
        if (prototypeValue != null)  {
            result = getSizeForComponent(renderer.getListCellRendererComponent(listBox,
                                                                               prototypeValue,
                                                                               -1, false, false));
        } else {
            ComboBoxModel model = comboBox.getModel();
            int modelSize = model.getSize();
            int baseline = -1;
            Dimension d;
            Component cpn;
            if (modelSize > 0 ) {
                for (int i = 0; i < modelSize ; i++ ) {
                    Object value = model.getElementAt(i);
                    Component c = renderer.getListCellRendererComponent(
                            listBox, value, -1, false, false);
                    d = getSizeForComponent(c);
                    if (sameBaseline && value != null &&
                            (!(value instanceof String) || !"".equals(value))) {
                        int newBaseline = c.getBaseline(d.width, d.height);
                        if (newBaseline == -1) {
                            sameBaseline = false;
                        }
                        else if (baseline == -1) {
                            baseline = newBaseline;
                        }
                        else if (baseline != newBaseline) {
                            sameBaseline = false;
                        }
                    }
                    result.width = Math.max(result.width,d.width);
                    result.height = Math.max(result.height,d.height);
                }
            } else {
                result = getDefaultSize();
                if (comboBox.isEditable()) {
                    result.width = 100;
                }
            }
        }
        if ( comboBox.isEditable() ) {
            Dimension d = editor.getPreferredSize();
            result.width = Math.max(result.width,d.width);
            result.height = Math.max(result.height,d.height);
        }
        if (padding != null) {
            result.width += padding.left + padding.right;
            result.height += padding.top + padding.bottom;
        }
        cachedDisplaySize.setSize(result.width, result.height);
        isDisplaySizeDirty = false;
        return result;
    }
    protected Dimension getSizeForComponent(Component comp) {
        currentValuePane.add(comp);
        comp.setFont(comboBox.getFont());
        Dimension d = comp.getPreferredSize();
        currentValuePane.remove(comp);
        return d;
    }
    protected void installKeyboardActions() {
        InputMap km = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        SwingUtilities.replaceUIInputMap(comboBox, JComponent.
                             WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, km);
        LazyActionMap.installLazyActionMap(comboBox, BasicComboBoxUI.class,
                                           "ComboBox.actionMap");
    }
    InputMap getInputMap(int condition) {
        if (condition == JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT) {
            return (InputMap)DefaultLookup.get(comboBox, this,
                                               "ComboBox.ancestorInputMap");
        }
        return null;
    }
    boolean isTableCellEditor() {
        return isTableCellEditor;
    }
    protected void uninstallKeyboardActions() {
        SwingUtilities.replaceUIInputMap(comboBox, JComponent.
                                 WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, null);
        SwingUtilities.replaceUIActionMap(comboBox, null);
    }
    private static class Actions extends UIAction {
        private static final String HIDE = "hidePopup";
        private static final String DOWN = "selectNext";
        private static final String DOWN_2 = "selectNext2";
        private static final String TOGGLE = "togglePopup";
        private static final String TOGGLE_2 = "spacePopup";
        private static final String UP = "selectPrevious";
        private static final String UP_2 = "selectPrevious2";
        private static final String ENTER = "enterPressed";
        private static final String PAGE_DOWN = "pageDownPassThrough";
        private static final String PAGE_UP = "pageUpPassThrough";
        private static final String HOME = "homePassThrough";
        private static final String END = "endPassThrough";
        Actions(String name) {
            super(name);
        }
        public void actionPerformed( ActionEvent e ) {
            String key = getName();
            JComboBox comboBox = (JComboBox)e.getSource();
            BasicComboBoxUI ui = (BasicComboBoxUI)BasicLookAndFeel.getUIOfType(
                                  comboBox.getUI(), BasicComboBoxUI.class);
            if (key == HIDE) {
                comboBox.firePopupMenuCanceled();
                comboBox.setPopupVisible(false);
            }
            else if (key == PAGE_DOWN || key == PAGE_UP ||
                     key == HOME || key == END) {
                int index = getNextIndex(comboBox, key);
                if (index >= 0 && index < comboBox.getItemCount()) {
                    comboBox.setSelectedIndex(index);
                }
            }
            else if (key == DOWN) {
                if (comboBox.isShowing() ) {
                    if ( comboBox.isPopupVisible() ) {
                        if (ui != null) {
                            ui.selectNextPossibleValue();
                        }
                    } else {
                        comboBox.setPopupVisible(true);
                    }
                }
            }
            else if (key == DOWN_2) {
                if (comboBox.isShowing() ) {
                    if ( (comboBox.isEditable() ||
                            (ui != null && ui.isTableCellEditor()))
                         && !comboBox.isPopupVisible() ) {
                        comboBox.setPopupVisible(true);
                    } else {
                        if (ui != null) {
                            ui.selectNextPossibleValue();
                        }
                    }
                }
            }
            else if (key == TOGGLE || key == TOGGLE_2) {
                if (ui != null && (key == TOGGLE || !comboBox.isEditable())) {
                    if ( ui.isTableCellEditor() ) {
                        comboBox.setSelectedIndex(ui.popup.getList().
                                                  getSelectedIndex());
                    }
                    else {
                        comboBox.setPopupVisible(!comboBox.isPopupVisible());
                    }
                }
            }
            else if (key == UP) {
                if (ui != null) {
                    if (ui.isPopupVisible(comboBox)) {
                        ui.selectPreviousPossibleValue();
                    }
                    else if (DefaultLookup.getBoolean(comboBox, ui,
                                    "ComboBox.showPopupOnNavigation", false)) {
                        ui.setPopupVisible(comboBox, true);
                    }
                }
            }
            else if (key == UP_2) {
                 if (comboBox.isShowing() && ui != null) {
                     if ( comboBox.isEditable() && !comboBox.isPopupVisible()) {
                         comboBox.setPopupVisible(true);
                     } else {
                         ui.selectPreviousPossibleValue();
                     }
                 }
             }
            else if (key == ENTER) {
                if (comboBox.isPopupVisible()) {
                    boolean isEnterSelectablePopup =
                            UIManager.getBoolean("ComboBox.isEnterSelectablePopup");
                    if (!comboBox.isEditable() || isEnterSelectablePopup
                            || ui.isTableCellEditor) {
                        Object listItem = ui.popup.getList().getSelectedValue();
                        if (listItem != null) {
                            comboBox.getEditor().setItem(listItem);
                            comboBox.setSelectedItem(listItem);
                        }
                    }
                    comboBox.setPopupVisible(false);
                }
                else {
                    if (ui.isTableCellEditor && !comboBox.isEditable()) {
                        comboBox.setSelectedItem(comboBox.getSelectedItem());
                    }
                    JRootPane root = SwingUtilities.getRootPane(comboBox);
                    if (root != null) {
                        InputMap im = root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
                        ActionMap am = root.getActionMap();
                        if (im != null && am != null) {
                            Object obj = im.get(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,0));
                            if (obj != null) {
                                Action action = am.get(obj);
                                if (action != null) {
                                    action.actionPerformed(new ActionEvent(
                                     root, e.getID(), e.getActionCommand(),
                                     e.getWhen(), e.getModifiers()));
                                }
                            }
                        }
                    }
                }
            }
        }
        private int getNextIndex(JComboBox comboBox, String key) {
            if (key == PAGE_UP) {
                int listHeight = comboBox.getMaximumRowCount();
                int index = comboBox.getSelectedIndex() - listHeight;
                return (index < 0 ? 0: index);
            }
            else if (key == PAGE_DOWN) {
                int listHeight = comboBox.getMaximumRowCount();
                int index = comboBox.getSelectedIndex() + listHeight;
                int max = comboBox.getItemCount();
                return (index < max ? index: max-1);
            }
            else if (key == HOME) {
                return 0;
            }
            else if (key == END) {
                return comboBox.getItemCount() - 1;
            }
            return comboBox.getSelectedIndex();
        }
        public boolean isEnabled(Object c) {
            if (getName() == HIDE) {
                return (c != null && ((JComboBox)c).isPopupVisible());
            }
            return true;
        }
    }
    private class Handler implements ActionListener, FocusListener,
                                     KeyListener, LayoutManager,
                                     ListDataListener, PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent e) {
            String propertyName = e.getPropertyName();
            if (e.getSource() == editor){
                if ("border".equals(propertyName)){
                    isMinimumSizeDirty = true;
                    isDisplaySizeDirty = true;
                    comboBox.revalidate();
                }
            } else {
                JComboBox comboBox = (JComboBox)e.getSource();
                if ( propertyName == "model" ) {
                    ComboBoxModel newModel = (ComboBoxModel)e.getNewValue();
                    ComboBoxModel oldModel = (ComboBoxModel)e.getOldValue();
                    if ( oldModel != null && listDataListener != null ) {
                        oldModel.removeListDataListener( listDataListener );
                    }
                    if ( newModel != null && listDataListener != null ) {
                        newModel.addListDataListener( listDataListener );
                    }
                    if ( editor != null ) {
                        comboBox.configureEditor( comboBox.getEditor(), comboBox.getSelectedItem() );
                    }
                    isMinimumSizeDirty = true;
                    isDisplaySizeDirty = true;
                    comboBox.revalidate();
                    comboBox.repaint();
                }
                else if ( propertyName == "editor" && comboBox.isEditable() ) {
                    addEditor();
                    comboBox.revalidate();
                }
                else if ( propertyName == "editable" ) {
                    if ( comboBox.isEditable() ) {
                        comboBox.setRequestFocusEnabled( false );
                        addEditor();
                    } else {
                        comboBox.setRequestFocusEnabled( true );
                        removeEditor();
                    }
                    updateToolTipTextForChildren();
                    comboBox.revalidate();
                }
                else if ( propertyName == "enabled" ) {
                    boolean enabled = comboBox.isEnabled();
                    if ( editor != null )
                        editor.setEnabled(enabled);
                    if ( arrowButton != null )
                        arrowButton.setEnabled(enabled);
                    comboBox.repaint();
                }
                else if ( propertyName == "focusable" ) {
                    boolean focusable = comboBox.isFocusable();
                    if ( editor != null )
                        editor.setFocusable(focusable);
                    if ( arrowButton != null )
                        arrowButton.setFocusable(focusable);
                    comboBox.repaint();
                }
                else if ( propertyName == "maximumRowCount" ) {
                    if ( isPopupVisible( comboBox ) ) {
                        setPopupVisible(comboBox, false);
                        setPopupVisible(comboBox, true);
                    }
                }
                else if ( propertyName == "font" ) {
                    listBox.setFont( comboBox.getFont() );
                    if ( editor != null ) {
                        editor.setFont( comboBox.getFont() );
                    }
                    isMinimumSizeDirty = true;
                    isDisplaySizeDirty = true;
                    comboBox.validate();
                }
                else if ( propertyName == JComponent.TOOL_TIP_TEXT_KEY ) {
                    updateToolTipTextForChildren();
                }
                else if ( propertyName == BasicComboBoxUI.IS_TABLE_CELL_EDITOR ) {
                    Boolean inTable = (Boolean)e.getNewValue();
                    isTableCellEditor = inTable.equals(Boolean.TRUE) ? true : false;
                }
                else if (propertyName == "prototypeDisplayValue") {
                    isMinimumSizeDirty = true;
                    isDisplaySizeDirty = true;
                    comboBox.revalidate();
                }
                else if (propertyName == "renderer") {
                    isMinimumSizeDirty = true;
                    isDisplaySizeDirty = true;
                    comboBox.revalidate();
                }
            }
        }
        public void keyPressed( KeyEvent e ) {
            if ( isNavigationKey(e.getKeyCode(), e.getModifiers()) ) {
                lastTime = 0L;
            } else if ( comboBox.isEnabled() && comboBox.getModel().getSize()!=0 &&
                        isTypeAheadKey( e ) && e.getKeyChar() != KeyEvent.CHAR_UNDEFINED) {
                time = e.getWhen();
                if ( comboBox.selectWithKeyChar(e.getKeyChar()) ) {
                    e.consume();
                }
            }
        }
        public void keyTyped(KeyEvent e) {
        }
        public void keyReleased(KeyEvent e) {
        }
        private boolean isTypeAheadKey( KeyEvent e ) {
            return !e.isAltDown() && !BasicGraphicsUtils.isMenuShortcutKeyDown(e);
        }
        public void focusGained( FocusEvent e ) {
            ComboBoxEditor comboBoxEditor = comboBox.getEditor();
            if ( (comboBoxEditor != null) &&
                 (e.getSource() == comboBoxEditor.getEditorComponent()) ) {
                return;
            }
            hasFocus = true;
            comboBox.repaint();
            if (comboBox.isEditable() && editor != null) {
                editor.requestFocus();
            }
        }
        public void focusLost( FocusEvent e ) {
            ComboBoxEditor editor = comboBox.getEditor();
            if ( (editor != null) &&
                 (e.getSource() == editor.getEditorComponent()) ) {
                Object item = editor.getItem();
                Object selectedItem = comboBox.getSelectedItem();
                if (!e.isTemporary() && item != null &&
                    !item.equals((selectedItem == null) ? "" : selectedItem )) {
                    comboBox.actionPerformed
                        (new ActionEvent(editor, 0, "",
                                      EventQueue.getMostRecentEventTime(), 0));
                }
            }
            hasFocus = false;
            if (!e.isTemporary()) {
                setPopupVisible(comboBox, false);
            }
            comboBox.repaint();
        }
        public void contentsChanged( ListDataEvent e ) {
            if ( !(e.getIndex0() == -1 && e.getIndex1() == -1) ) {
                isMinimumSizeDirty = true;
                comboBox.revalidate();
            }
            if (comboBox.isEditable() && editor != null) {
                comboBox.configureEditor( comboBox.getEditor(),
                                          comboBox.getSelectedItem() );
            }
            isDisplaySizeDirty = true;
            comboBox.repaint();
        }
        public void intervalAdded( ListDataEvent e ) {
            contentsChanged( e );
        }
        public void intervalRemoved( ListDataEvent e ) {
            contentsChanged( e );
        }
        public void addLayoutComponent(String name, Component comp) {}
        public void removeLayoutComponent(Component comp) {}
        public Dimension preferredLayoutSize(Container parent) {
            return parent.getPreferredSize();
        }
        public Dimension minimumLayoutSize(Container parent) {
            return parent.getMinimumSize();
        }
        public void layoutContainer(Container parent) {
            JComboBox cb = (JComboBox)parent;
            int width = cb.getWidth();
            int height = cb.getHeight();
            Insets insets = getInsets();
            int buttonHeight = height - (insets.top + insets.bottom);
            int buttonWidth = buttonHeight;
            if (arrowButton != null) {
                Insets arrowInsets = arrowButton.getInsets();
                buttonWidth = squareButton ?
                    buttonHeight :
                    arrowButton.getPreferredSize().width + arrowInsets.left + arrowInsets.right;
            }
            Rectangle cvb;
            if (arrowButton != null) {
                if (BasicGraphicsUtils.isLeftToRight(cb)) {
                    arrowButton.setBounds(width - (insets.right + buttonWidth),
                            insets.top, buttonWidth, buttonHeight);
                } else {
                    arrowButton.setBounds(insets.left, insets.top,
                            buttonWidth, buttonHeight);
                }
            }
            if ( editor != null ) {
                cvb = rectangleForCurrentValue();
                editor.setBounds(cvb);
            }
        }
        public void actionPerformed(ActionEvent evt) {
            Object item = comboBox.getEditor().getItem();
            if (item != null) {
             if(!comboBox.isPopupVisible() && !item.equals(comboBox.getSelectedItem())) {
              comboBox.setSelectedItem(comboBox.getEditor().getItem());
             }
             ActionMap am = comboBox.getActionMap();
             if (am != null) {
                Action action = am.get("enterPressed");
                if (action != null) {
                    action.actionPerformed(new ActionEvent(comboBox, evt.getID(),
                                           evt.getActionCommand(),
                                           evt.getModifiers()));
                }
            }
       }
   }
  }
    class DefaultKeySelectionManager implements JComboBox.KeySelectionManager, UIResource {
        private String prefix = "";
        private String typedString = "";
        public int selectionForKey(char aKey,ComboBoxModel aModel) {
            if (lastTime == 0L) {
                prefix = "";
                typedString = "";
            }
            boolean startingFromSelection = true;
            int startIndex = comboBox.getSelectedIndex();
            if (time - lastTime < timeFactor) {
                typedString += aKey;
                if((prefix.length() == 1) && (aKey == prefix.charAt(0))) {
                    startIndex++;
                } else {
                    prefix = typedString;
                }
            } else {
                startIndex++;
                typedString = "" + aKey;
                prefix = typedString;
            }
            lastTime = time;
            if (startIndex < 0 || startIndex >= aModel.getSize()) {
                startingFromSelection = false;
                startIndex = 0;
            }
            int index = listBox.getNextMatch(prefix, startIndex,
                                             Position.Bias.Forward);
            if (index < 0 && startingFromSelection) { 
                index = listBox.getNextMatch(prefix, 0,
                                             Position.Bias.Forward);
            }
            return index;
        }
    }
}
