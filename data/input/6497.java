public class JMenuItem extends AbstractButton implements Accessible,MenuElement  {
    private static final String uiClassID = "MenuItemUI";
    private static final boolean TRACE =   false; 
    private static final boolean VERBOSE = false; 
    private static final boolean DEBUG =   false;  
    private boolean isMouseDragged = false;
    public JMenuItem() {
        this(null, (Icon)null);
    }
    public JMenuItem(Icon icon) {
        this(null, icon);
    }
    public JMenuItem(String text) {
        this(text, (Icon)null);
    }
    public JMenuItem(Action a) {
        this();
        setAction(a);
    }
    public JMenuItem(String text, Icon icon) {
        setModel(new DefaultButtonModel());
        init(text, icon);
        initFocusability();
    }
    public JMenuItem(String text, int mnemonic) {
        setModel(new DefaultButtonModel());
        init(text, null);
        setMnemonic(mnemonic);
        initFocusability();
    }
    public void setModel(ButtonModel newModel) {
        super.setModel(newModel);
        if(newModel instanceof DefaultButtonModel) {
            ((DefaultButtonModel)newModel).setMenuItem(true);
        }
    }
    void initFocusability() {
        setFocusable(false);
    }
    protected void init(String text, Icon icon) {
        if(text != null) {
            setText(text);
        }
        if(icon != null) {
            setIcon(icon);
        }
        addFocusListener(new MenuItemFocusListener());
        setUIProperty("borderPainted", Boolean.FALSE);
        setFocusPainted(false);
        setHorizontalTextPosition(JButton.TRAILING);
        setHorizontalAlignment(JButton.LEADING);
        updateUI();
    }
    private static class MenuItemFocusListener implements FocusListener,
        Serializable {
        public void focusGained(FocusEvent event) {}
        public void focusLost(FocusEvent event) {
            JMenuItem mi = (JMenuItem)event.getSource();
            if(mi.isFocusPainted()) {
                mi.repaint();
            }
        }
    }
    public void setUI(MenuItemUI ui) {
        super.setUI(ui);
    }
    public void updateUI() {
        setUI((MenuItemUI)UIManager.getUI(this));
    }
    public String getUIClassID() {
        return uiClassID;
    }
    public void setArmed(boolean b) {
        ButtonModel model = getModel();
        boolean oldValue = model.isArmed();
        if(model.isArmed() != b) {
            model.setArmed(b);
        }
    }
    public boolean isArmed() {
        ButtonModel model = getModel();
        return model.isArmed();
    }
    public void setEnabled(boolean b) {
        if (!b && !UIManager.getBoolean("MenuItem.disabledAreNavigable")) {
            setArmed(false);
        }
        super.setEnabled(b);
    }
    boolean alwaysOnTop() {
        if (SwingUtilities.getAncestorOfClass(JInternalFrame.class, this) !=
                null) {
            return false;
        }
        return true;
    }
    private KeyStroke accelerator;
    public void setAccelerator(KeyStroke keyStroke) {
        KeyStroke oldAccelerator = accelerator;
        this.accelerator = keyStroke;
        repaint();
        revalidate();
        firePropertyChange("accelerator", oldAccelerator, accelerator);
    }
    public KeyStroke getAccelerator() {
        return this.accelerator;
    }
    protected void configurePropertiesFromAction(Action a) {
        super.configurePropertiesFromAction(a);
        configureAcceleratorFromAction(a);
    }
    void setIconFromAction(Action a) {
        Icon icon = null;
        if (a != null) {
            icon = (Icon)a.getValue(Action.SMALL_ICON);
        }
        setIcon(icon);
    }
    void largeIconChanged(Action a) {
    }
    void smallIconChanged(Action a) {
        setIconFromAction(a);
    }
    void configureAcceleratorFromAction(Action a) {
        KeyStroke ks = (a==null) ? null :
            (KeyStroke)a.getValue(Action.ACCELERATOR_KEY);
        setAccelerator(ks);
    }
    protected void actionPropertyChanged(Action action, String propertyName) {
        if (propertyName == Action.ACCELERATOR_KEY) {
            configureAcceleratorFromAction(action);
        }
        else {
            super.actionPropertyChanged(action, propertyName);
        }
    }
    public void processMouseEvent(MouseEvent e,MenuElement path[],MenuSelectionManager manager) {
        processMenuDragMouseEvent(
                 new MenuDragMouseEvent(e.getComponent(), e.getID(),
                                        e.getWhen(),
                                        e.getModifiers(), e.getX(), e.getY(),
                                        e.getXOnScreen(), e.getYOnScreen(),
                                        e.getClickCount(), e.isPopupTrigger(),
                                        path, manager));
    }
    public void processKeyEvent(KeyEvent e,MenuElement path[],MenuSelectionManager manager) {
        if (DEBUG) {
            System.out.println("in JMenuItem.processKeyEvent/3 for " + getText() +
                                   "  " + KeyStroke.getKeyStrokeForEvent(e));
        }
        MenuKeyEvent mke = new MenuKeyEvent(e.getComponent(), e.getID(),
                                             e.getWhen(), e.getModifiers(),
                                             e.getKeyCode(), e.getKeyChar(),
                                             path, manager);
        processMenuKeyEvent(mke);
        if (mke.isConsumed())  {
            e.consume();
        }
    }
    public void processMenuDragMouseEvent(MenuDragMouseEvent e) {
        switch (e.getID()) {
        case MouseEvent.MOUSE_ENTERED:
            isMouseDragged = false; fireMenuDragMouseEntered(e); break;
        case MouseEvent.MOUSE_EXITED:
            isMouseDragged = false; fireMenuDragMouseExited(e); break;
        case MouseEvent.MOUSE_DRAGGED:
            isMouseDragged = true; fireMenuDragMouseDragged(e); break;
        case MouseEvent.MOUSE_RELEASED:
            if(isMouseDragged) fireMenuDragMouseReleased(e); break;
        default:
            break;
        }
    }
    public void processMenuKeyEvent(MenuKeyEvent e) {
        if (DEBUG) {
            System.out.println("in JMenuItem.processMenuKeyEvent for " + getText()+
                                   "  " + KeyStroke.getKeyStrokeForEvent(e));
        }
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
    protected void fireMenuDragMouseEntered(MenuDragMouseEvent event) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==MenuDragMouseListener.class) {
                ((MenuDragMouseListener)listeners[i+1]).menuDragMouseEntered(event);
            }
        }
    }
    protected void fireMenuDragMouseExited(MenuDragMouseEvent event) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==MenuDragMouseListener.class) {
                ((MenuDragMouseListener)listeners[i+1]).menuDragMouseExited(event);
            }
        }
    }
    protected void fireMenuDragMouseDragged(MenuDragMouseEvent event) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==MenuDragMouseListener.class) {
                ((MenuDragMouseListener)listeners[i+1]).menuDragMouseDragged(event);
            }
        }
    }
    protected void fireMenuDragMouseReleased(MenuDragMouseEvent event) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==MenuDragMouseListener.class) {
                ((MenuDragMouseListener)listeners[i+1]).menuDragMouseReleased(event);
            }
        }
    }
    protected void fireMenuKeyPressed(MenuKeyEvent event) {
        if (DEBUG) {
            System.out.println("in JMenuItem.fireMenuKeyPressed for " + getText()+
                                   "  " + KeyStroke.getKeyStrokeForEvent(event));
        }
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==MenuKeyListener.class) {
                ((MenuKeyListener)listeners[i+1]).menuKeyPressed(event);
            }
        }
    }
    protected void fireMenuKeyReleased(MenuKeyEvent event) {
        if (DEBUG) {
            System.out.println("in JMenuItem.fireMenuKeyReleased for " + getText()+
                                   "  " + KeyStroke.getKeyStrokeForEvent(event));
        }
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==MenuKeyListener.class) {
                ((MenuKeyListener)listeners[i+1]).menuKeyReleased(event);
            }
        }
    }
    protected void fireMenuKeyTyped(MenuKeyEvent event) {
        if (DEBUG) {
            System.out.println("in JMenuItem.fireMenuKeyTyped for " + getText()+
                                   "  " + KeyStroke.getKeyStrokeForEvent(event));
        }
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==MenuKeyListener.class) {
                ((MenuKeyListener)listeners[i+1]).menuKeyTyped(event);
            }
        }
    }
    public void menuSelectionChanged(boolean isIncluded) {
        setArmed(isIncluded);
    }
    public MenuElement[] getSubElements() {
        return new MenuElement[0];
    }
    public Component getComponent() {
        return this;
    }
    public void addMenuDragMouseListener(MenuDragMouseListener l) {
        listenerList.add(MenuDragMouseListener.class, l);
    }
    public void removeMenuDragMouseListener(MenuDragMouseListener l) {
        listenerList.remove(MenuDragMouseListener.class, l);
    }
    public MenuDragMouseListener[] getMenuDragMouseListeners() {
        return listenerList.getListeners(MenuDragMouseListener.class);
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
    private void readObject(ObjectInputStream s)
        throws IOException, ClassNotFoundException
    {
        s.defaultReadObject();
        if (getUIClassID().equals(uiClassID)) {
            updateUI();
        }
    }
    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        if (getUIClassID().equals(uiClassID)) {
            byte count = JComponent.getWriteObjCounter(this);
            JComponent.setWriteObjCounter(this, --count);
            if (count == 0 && ui != null) {
                ui.installUI(this);
            }
        }
    }
    protected String paramString() {
        return super.paramString();
    }
    public AccessibleContext getAccessibleContext() {
        if (accessibleContext == null) {
            accessibleContext = new AccessibleJMenuItem();
        }
        return accessibleContext;
    }
    protected class AccessibleJMenuItem extends AccessibleAbstractButton implements ChangeListener {
        private boolean isArmed = false;
        private boolean hasFocus = false;
        private boolean isPressed = false;
        private boolean isSelected = false;
        AccessibleJMenuItem() {
            super();
            JMenuItem.this.addChangeListener(this);
        }
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.MENU_ITEM;
        }
        private void fireAccessibilityFocusedEvent(JMenuItem toCheck) {
            MenuElement [] path =
                MenuSelectionManager.defaultManager().getSelectedPath();
            if (path.length > 0) {
                Object menuItem = path[path.length - 1];
                if (toCheck == menuItem) {
                    firePropertyChange(
                        AccessibleContext.ACCESSIBLE_STATE_PROPERTY,
                        null, AccessibleState.FOCUSED);
                }
            }
        }
        public void stateChanged(ChangeEvent e) {
            firePropertyChange(AccessibleContext.ACCESSIBLE_VISIBLE_DATA_PROPERTY,
                               Boolean.valueOf(false), Boolean.valueOf(true));
            if (JMenuItem.this.getModel().isArmed()) {
                if (!isArmed) {
                    isArmed = true;
                    firePropertyChange(
                        AccessibleContext.ACCESSIBLE_STATE_PROPERTY,
                        null, AccessibleState.ARMED);
                    fireAccessibilityFocusedEvent(JMenuItem.this);
                }
            } else {
                if (isArmed) {
                    isArmed = false;
                    firePropertyChange(
                        AccessibleContext.ACCESSIBLE_STATE_PROPERTY,
                        AccessibleState.ARMED, null);
                }
            }
            if (JMenuItem.this.isFocusOwner()) {
                if (!hasFocus) {
                    hasFocus = true;
                    firePropertyChange(
                        AccessibleContext.ACCESSIBLE_STATE_PROPERTY,
                        null, AccessibleState.FOCUSED);
                }
            } else {
                if (hasFocus) {
                    hasFocus = false;
                    firePropertyChange(
                        AccessibleContext.ACCESSIBLE_STATE_PROPERTY,
                        AccessibleState.FOCUSED, null);
                }
            }
            if (JMenuItem.this.getModel().isPressed()) {
                if (!isPressed) {
                    isPressed = true;
                    firePropertyChange(
                        AccessibleContext.ACCESSIBLE_STATE_PROPERTY,
                        null, AccessibleState.PRESSED);
                }
            } else {
                if (isPressed) {
                    isPressed = false;
                    firePropertyChange(
                        AccessibleContext.ACCESSIBLE_STATE_PROPERTY,
                        AccessibleState.PRESSED, null);
                }
            }
            if (JMenuItem.this.getModel().isSelected()) {
                if (!isSelected) {
                    isSelected = true;
                    firePropertyChange(
                        AccessibleContext.ACCESSIBLE_STATE_PROPERTY,
                        null, AccessibleState.CHECKED);
                    fireAccessibilityFocusedEvent(JMenuItem.this);
                }
            } else {
                if (isSelected) {
                    isSelected = false;
                    firePropertyChange(
                        AccessibleContext.ACCESSIBLE_STATE_PROPERTY,
                        AccessibleState.CHECKED, null);
                }
            }
        }
    } 
}
