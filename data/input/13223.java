public class JComboBox<E> extends JComponent
implements ItemSelectable,ListDataListener,ActionListener, Accessible {
    private static final String uiClassID = "ComboBoxUI";
    protected ComboBoxModel<E>    dataModel;
    protected ListCellRenderer<? super E> renderer;
    protected ComboBoxEditor       editor;
    protected int maximumRowCount = 8;
    protected boolean isEditable  = false;
    protected KeySelectionManager keySelectionManager = null;
    protected String actionCommand = "comboBoxChanged";
    protected boolean lightWeightPopupEnabled = JPopupMenu.getDefaultLightWeightPopupEnabled();
    protected Object selectedItemReminder = null;
    private E prototypeDisplayValue;
    private boolean firingActionEvent = false;
    private boolean selectingItem = false;
    public JComboBox(ComboBoxModel<E> aModel) {
        super();
        setModel(aModel);
        init();
    }
    public JComboBox(E[] items) {
        super();
        setModel(new DefaultComboBoxModel<E>(items));
        init();
    }
    public JComboBox(Vector<E> items) {
        super();
        setModel(new DefaultComboBoxModel<E>(items));
        init();
    }
    public JComboBox() {
        super();
        setModel(new DefaultComboBoxModel<E>());
        init();
    }
    private void init() {
        installAncestorListener();
        setUIProperty("opaque",true);
        updateUI();
    }
    protected void installAncestorListener() {
        addAncestorListener(new AncestorListener(){
                                public void ancestorAdded(AncestorEvent event){ hidePopup();}
                                public void ancestorRemoved(AncestorEvent event){ hidePopup();}
                                public void ancestorMoved(AncestorEvent event){
                                    if (event.getSource() != JComboBox.this)
                                        hidePopup();
                                }});
    }
    public void setUI(ComboBoxUI ui) {
        super.setUI(ui);
    }
    public void updateUI() {
        setUI((ComboBoxUI)UIManager.getUI(this));
        ListCellRenderer<? super E> renderer = getRenderer();
        if (renderer instanceof Component) {
            SwingUtilities.updateComponentTreeUI((Component)renderer);
        }
    }
    public String getUIClassID() {
        return uiClassID;
    }
    public ComboBoxUI getUI() {
        return(ComboBoxUI)ui;
    }
    public void setModel(ComboBoxModel<E> aModel) {
        ComboBoxModel<E> oldModel = dataModel;
        if (oldModel != null) {
            oldModel.removeListDataListener(this);
        }
        dataModel = aModel;
        dataModel.addListDataListener(this);
        selectedItemReminder = dataModel.getSelectedItem();
        firePropertyChange( "model", oldModel, dataModel);
    }
    public ComboBoxModel<E> getModel() {
        return dataModel;
    }
    public void setLightWeightPopupEnabled(boolean aFlag) {
        boolean oldFlag = lightWeightPopupEnabled;
        lightWeightPopupEnabled = aFlag;
        firePropertyChange("lightWeightPopupEnabled", oldFlag, lightWeightPopupEnabled);
    }
    public boolean isLightWeightPopupEnabled() {
        return lightWeightPopupEnabled;
    }
    public void setEditable(boolean aFlag) {
        boolean oldFlag = isEditable;
        isEditable = aFlag;
        firePropertyChange( "editable", oldFlag, isEditable );
    }
    public boolean isEditable() {
        return isEditable;
    }
    public void setMaximumRowCount(int count) {
        int oldCount = maximumRowCount;
        maximumRowCount = count;
        firePropertyChange( "maximumRowCount", oldCount, maximumRowCount );
    }
    public int getMaximumRowCount() {
        return maximumRowCount;
    }
    public void setRenderer(ListCellRenderer<? super E> aRenderer) {
        ListCellRenderer<? super E> oldRenderer = renderer;
        renderer = aRenderer;
        firePropertyChange( "renderer", oldRenderer, renderer );
        invalidate();
    }
    public ListCellRenderer<? super E> getRenderer() {
        return renderer;
    }
    public void setEditor(ComboBoxEditor anEditor) {
        ComboBoxEditor oldEditor = editor;
        if ( editor != null ) {
            editor.removeActionListener(this);
        }
        editor = anEditor;
        if ( editor != null ) {
            editor.addActionListener(this);
        }
        firePropertyChange( "editor", oldEditor, editor );
    }
    public ComboBoxEditor getEditor() {
        return editor;
    }
    public void setSelectedItem(Object anObject) {
        Object oldSelection = selectedItemReminder;
        Object objectToSelect = anObject;
        if (oldSelection == null || !oldSelection.equals(anObject)) {
            if (anObject != null && !isEditable()) {
                boolean found = false;
                for (int i = 0; i < dataModel.getSize(); i++) {
                    E element = dataModel.getElementAt(i);
                    if (anObject.equals(element)) {
                        found = true;
                        objectToSelect = element;
                        break;
                    }
                }
                if (!found) {
                    return;
                }
            }
            selectingItem = true;
            dataModel.setSelectedItem(objectToSelect);
            selectingItem = false;
            if (selectedItemReminder != dataModel.getSelectedItem()) {
                selectedItemChanged();
            }
        }
        fireActionEvent();
    }
    public Object getSelectedItem() {
        return dataModel.getSelectedItem();
    }
    public void setSelectedIndex(int anIndex) {
        int size = dataModel.getSize();
        if ( anIndex == -1 ) {
            setSelectedItem( null );
        } else if ( anIndex < -1 || anIndex >= size ) {
            throw new IllegalArgumentException("setSelectedIndex: " + anIndex + " out of bounds");
        } else {
            setSelectedItem(dataModel.getElementAt(anIndex));
        }
    }
    @Transient
    public int getSelectedIndex() {
        Object sObject = dataModel.getSelectedItem();
        int i,c;
        E obj;
        for ( i=0,c=dataModel.getSize();i<c;i++ ) {
            obj = dataModel.getElementAt(i);
            if ( obj != null && obj.equals(sObject) )
                return i;
        }
        return -1;
    }
    public E getPrototypeDisplayValue() {
        return prototypeDisplayValue;
    }
    public void setPrototypeDisplayValue(E prototypeDisplayValue) {
        Object oldValue = this.prototypeDisplayValue;
        this.prototypeDisplayValue = prototypeDisplayValue;
        firePropertyChange("prototypeDisplayValue", oldValue, prototypeDisplayValue);
    }
    public void addItem(E item) {
        checkMutableComboBoxModel();
        ((MutableComboBoxModel<E>)dataModel).addElement(item);
    }
    public void insertItemAt(E item, int index) {
        checkMutableComboBoxModel();
        ((MutableComboBoxModel<E>)dataModel).insertElementAt(item,index);
    }
    public void removeItem(Object anObject) {
        checkMutableComboBoxModel();
        ((MutableComboBoxModel)dataModel).removeElement(anObject);
    }
    public void removeItemAt(int anIndex) {
        checkMutableComboBoxModel();
        ((MutableComboBoxModel<E>)dataModel).removeElementAt( anIndex );
    }
    public void removeAllItems() {
        checkMutableComboBoxModel();
        MutableComboBoxModel<E> model = (MutableComboBoxModel<E>)dataModel;
        int size = model.getSize();
        if ( model instanceof DefaultComboBoxModel ) {
            ((DefaultComboBoxModel)model).removeAllElements();
        }
        else {
            for ( int i = 0; i < size; ++i ) {
                E element = model.getElementAt( 0 );
                model.removeElement( element );
            }
        }
        selectedItemReminder = null;
        if (isEditable()) {
            editor.setItem(null);
        }
    }
    void checkMutableComboBoxModel() {
        if ( !(dataModel instanceof MutableComboBoxModel) )
            throw new RuntimeException("Cannot use this method with a non-Mutable data model.");
    }
    public void showPopup() {
        setPopupVisible(true);
    }
    public void hidePopup() {
        setPopupVisible(false);
    }
    public void setPopupVisible(boolean v) {
        getUI().setPopupVisible(this, v);
    }
    public boolean isPopupVisible() {
        return getUI().isPopupVisible(this);
    }
    public void addItemListener(ItemListener aListener) {
        listenerList.add(ItemListener.class,aListener);
    }
    public void removeItemListener(ItemListener aListener) {
        listenerList.remove(ItemListener.class,aListener);
    }
    public ItemListener[] getItemListeners() {
        return listenerList.getListeners(ItemListener.class);
    }
    public void addActionListener(ActionListener l) {
        listenerList.add(ActionListener.class,l);
    }
    public void removeActionListener(ActionListener l) {
        if ((l != null) && (getAction() == l)) {
            setAction(null);
        } else {
            listenerList.remove(ActionListener.class, l);
        }
    }
    public ActionListener[] getActionListeners() {
        return listenerList.getListeners(ActionListener.class);
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
    public void firePopupMenuWillBecomeVisible() {
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
    public void firePopupMenuWillBecomeInvisible() {
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
    public void firePopupMenuCanceled() {
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
    public void setActionCommand(String aCommand) {
        actionCommand = aCommand;
    }
    public String getActionCommand() {
        return actionCommand;
    }
    private Action action;
    private PropertyChangeListener actionPropertyChangeListener;
    public void setAction(Action a) {
        Action oldValue = getAction();
        if (action==null || !action.equals(a)) {
            action = a;
            if (oldValue!=null) {
                removeActionListener(oldValue);
                oldValue.removePropertyChangeListener(actionPropertyChangeListener);
                actionPropertyChangeListener = null;
            }
            configurePropertiesFromAction(action);
            if (action!=null) {
                if (!isListener(ActionListener.class, action)) {
                    addActionListener(action);
                }
                actionPropertyChangeListener = createActionPropertyChangeListener(action);
                action.addPropertyChangeListener(actionPropertyChangeListener);
            }
            firePropertyChange("action", oldValue, action);
        }
    }
    private boolean isListener(Class c, ActionListener a) {
        boolean isListener = false;
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==c && listeners[i+1]==a) {
                    isListener=true;
            }
        }
        return isListener;
    }
    public Action getAction() {
        return action;
    }
    protected void configurePropertiesFromAction(Action a) {
        AbstractAction.setEnabledFromAction(this, a);
        AbstractAction.setToolTipTextFromAction(this, a);
        setActionCommandFromAction(a);
    }
    protected PropertyChangeListener createActionPropertyChangeListener(Action a) {
        return new ComboBoxActionPropertyChangeListener(this, a);
    }
    protected void actionPropertyChanged(Action action, String propertyName) {
        if (propertyName == Action.ACTION_COMMAND_KEY) {
            setActionCommandFromAction(action);
        } else if (propertyName == "enabled") {
            AbstractAction.setEnabledFromAction(this, action);
        } else if (Action.SHORT_DESCRIPTION == propertyName) {
            AbstractAction.setToolTipTextFromAction(this, action);
        }
    }
    private void setActionCommandFromAction(Action a) {
        setActionCommand((a != null) ?
                             (String)a.getValue(Action.ACTION_COMMAND_KEY) :
                             null);
    }
    private static class ComboBoxActionPropertyChangeListener
                 extends ActionPropertyChangeListener<JComboBox<?>> {
        ComboBoxActionPropertyChangeListener(JComboBox<?> b, Action a) {
            super(b, a);
        }
        protected void actionPropertyChanged(JComboBox<?> cb,
                                             Action action,
                                             PropertyChangeEvent e) {
            if (AbstractAction.shouldReconfigure(e)) {
                cb.configurePropertiesFromAction(action);
            } else {
                cb.actionPropertyChanged(action, e.getPropertyName());
            }
        }
    }
    protected void fireItemStateChanged(ItemEvent e) {
        Object[] listeners = listenerList.getListenerList();
        for ( int i = listeners.length-2; i>=0; i-=2 ) {
            if ( listeners[i]==ItemListener.class ) {
                ((ItemListener)listeners[i+1]).itemStateChanged(e);
            }
        }
    }
    protected void fireActionEvent() {
        if (!firingActionEvent) {
            firingActionEvent = true;
            ActionEvent e = null;
            Object[] listeners = listenerList.getListenerList();
            long mostRecentEventTime = EventQueue.getMostRecentEventTime();
            int modifiers = 0;
            AWTEvent currentEvent = EventQueue.getCurrentEvent();
            if (currentEvent instanceof InputEvent) {
                modifiers = ((InputEvent)currentEvent).getModifiers();
            } else if (currentEvent instanceof ActionEvent) {
                modifiers = ((ActionEvent)currentEvent).getModifiers();
            }
            for ( int i = listeners.length-2; i>=0; i-=2 ) {
                if ( listeners[i]==ActionListener.class ) {
                    if ( e == null )
                        e = new ActionEvent(this,ActionEvent.ACTION_PERFORMED,
                                            getActionCommand(),
                                            mostRecentEventTime, modifiers);
                    ((ActionListener)listeners[i+1]).actionPerformed(e);
                }
            }
            firingActionEvent = false;
        }
    }
    protected void selectedItemChanged() {
        if (selectedItemReminder != null ) {
            fireItemStateChanged(new ItemEvent(this,ItemEvent.ITEM_STATE_CHANGED,
                                               selectedItemReminder,
                                               ItemEvent.DESELECTED));
        }
        selectedItemReminder = dataModel.getSelectedItem();
        if (selectedItemReminder != null ) {
            fireItemStateChanged(new ItemEvent(this,ItemEvent.ITEM_STATE_CHANGED,
                                               selectedItemReminder,
                                               ItemEvent.SELECTED));
        }
    }
    public Object[] getSelectedObjects() {
        Object selectedObject = getSelectedItem();
        if ( selectedObject == null )
            return new Object[0];
        else {
            Object result[] = new Object[1];
            result[0] = selectedObject;
            return result;
        }
    }
    public void actionPerformed(ActionEvent e) {
        Object newItem = getEditor().getItem();
        setPopupVisible(false);
        getModel().setSelectedItem(newItem);
        String oldCommand = getActionCommand();
        setActionCommand("comboBoxEdited");
        fireActionEvent();
        setActionCommand(oldCommand);
    }
    public void contentsChanged(ListDataEvent e) {
        Object oldSelection = selectedItemReminder;
        Object newSelection = dataModel.getSelectedItem();
        if (oldSelection == null || !oldSelection.equals(newSelection)) {
            selectedItemChanged();
            if (!selectingItem) {
                fireActionEvent();
            }
        }
    }
    public void intervalAdded(ListDataEvent e) {
        if (selectedItemReminder != dataModel.getSelectedItem()) {
            selectedItemChanged();
        }
    }
    public void intervalRemoved(ListDataEvent e) {
        contentsChanged(e);
    }
    public boolean selectWithKeyChar(char keyChar) {
        int index;
        if ( keySelectionManager == null )
            keySelectionManager = createDefaultKeySelectionManager();
        index = keySelectionManager.selectionForKey(keyChar,getModel());
        if ( index != -1 ) {
            setSelectedIndex(index);
            return true;
        }
        else
            return false;
    }
    public void setEnabled(boolean b) {
        super.setEnabled(b);
        firePropertyChange( "enabled", !isEnabled(), isEnabled() );
    }
    public void configureEditor(ComboBoxEditor anEditor, Object anItem) {
        anEditor.setItem(anItem);
    }
    public void processKeyEvent(KeyEvent e) {
        if ( e.getKeyCode() == KeyEvent.VK_TAB ) {
            hidePopup();
        }
        super.processKeyEvent(e);
    }
    public void setKeySelectionManager(KeySelectionManager aManager) {
        keySelectionManager = aManager;
    }
    public KeySelectionManager getKeySelectionManager() {
        return keySelectionManager;
    }
    public int getItemCount() {
        return dataModel.getSize();
    }
    public E getItemAt(int index) {
        return dataModel.getElementAt(index);
    }
    protected KeySelectionManager createDefaultKeySelectionManager() {
        return new DefaultKeySelectionManager();
    }
    public interface KeySelectionManager {
        int selectionForKey(char aKey,ComboBoxModel aModel);
    }
    class DefaultKeySelectionManager implements KeySelectionManager, Serializable {
        public int selectionForKey(char aKey,ComboBoxModel aModel) {
            int i,c;
            int currentSelection = -1;
            Object selectedItem = aModel.getSelectedItem();
            String v;
            String pattern;
            if ( selectedItem != null ) {
                for ( i=0,c=aModel.getSize();i<c;i++ ) {
                    if ( selectedItem == aModel.getElementAt(i) ) {
                        currentSelection  =  i;
                        break;
                    }
                }
            }
            pattern = ("" + aKey).toLowerCase();
            aKey = pattern.charAt(0);
            for ( i = ++currentSelection, c = aModel.getSize() ; i < c ; i++ ) {
                Object elem = aModel.getElementAt(i);
                if (elem != null && elem.toString() != null) {
                    v = elem.toString().toLowerCase();
                    if ( v.length() > 0 && v.charAt(0) == aKey )
                        return i;
                }
            }
            for ( i = 0 ; i < currentSelection ; i ++ ) {
                Object elem = aModel.getElementAt(i);
                if (elem != null && elem.toString() != null) {
                    v = elem.toString().toLowerCase();
                    if ( v.length() > 0 && v.charAt(0) == aKey )
                        return i;
                }
            }
            return -1;
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
        String selectedItemReminderString = (selectedItemReminder != null ?
                                             selectedItemReminder.toString() :
                                             "");
        String isEditableString = (isEditable ? "true" : "false");
        String lightWeightPopupEnabledString = (lightWeightPopupEnabled ?
                                                "true" : "false");
        return super.paramString() +
        ",isEditable=" + isEditableString +
        ",lightWeightPopupEnabled=" + lightWeightPopupEnabledString +
        ",maximumRowCount=" + maximumRowCount +
        ",selectedItemReminder=" + selectedItemReminderString;
    }
    public AccessibleContext getAccessibleContext() {
        if ( accessibleContext == null ) {
            accessibleContext = new AccessibleJComboBox();
        }
        return accessibleContext;
    }
    protected class AccessibleJComboBox extends AccessibleJComponent
    implements AccessibleAction, AccessibleSelection {
        private JList popupList; 
        private Accessible previousSelectedAccessible = null;
        public AccessibleJComboBox() {
            JComboBox.this.addPropertyChangeListener(new AccessibleJComboBoxPropertyChangeListener());
            setEditorNameAndDescription();
            Accessible a = getUI().getAccessibleChild(JComboBox.this, 0);
            if (a instanceof javax.swing.plaf.basic.ComboPopup) {
                popupList = ((javax.swing.plaf.basic.ComboPopup)a).getList();
                popupList.addListSelectionListener(
                    new AccessibleJComboBoxListSelectionListener());
            }
            JComboBox.this.addPopupMenuListener(
              new AccessibleJComboBoxPopupMenuListener());
        }
        private class AccessibleJComboBoxPropertyChangeListener
            implements PropertyChangeListener {
            public void propertyChange(PropertyChangeEvent e) {
                if (e.getPropertyName() == "editor") {
                    setEditorNameAndDescription();
                }
            }
        }
        private void setEditorNameAndDescription() {
            ComboBoxEditor editor = JComboBox.this.getEditor();
            if (editor != null) {
                Component comp = editor.getEditorComponent();
                if (comp instanceof Accessible) {
                    AccessibleContext ac = comp.getAccessibleContext();
                    if (ac != null) { 
                        ac.setAccessibleName(getAccessibleName());
                        ac.setAccessibleDescription(getAccessibleDescription());
                    }
                }
            }
        }
        private class AccessibleJComboBoxPopupMenuListener
            implements PopupMenuListener {
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                if (popupList == null) {
                    return;
                }
                int selectedIndex = popupList.getSelectedIndex();
                if (selectedIndex < 0) {
                    return;
                }
                previousSelectedAccessible =
                    popupList.getAccessibleContext().getAccessibleChild(selectedIndex);
            }
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            }
            public void popupMenuCanceled(PopupMenuEvent e) {
            }
        }
        private class AccessibleJComboBoxListSelectionListener
            implements ListSelectionListener {
            public void valueChanged(ListSelectionEvent e) {
                if (popupList == null) {
                    return;
                }
                int selectedIndex = popupList.getSelectedIndex();
                if (selectedIndex < 0) {
                    return;
                }
                Accessible selectedAccessible =
                    popupList.getAccessibleContext().getAccessibleChild(selectedIndex);
                if (selectedAccessible == null) {
                    return;
                }
                PropertyChangeEvent pce;
                if (previousSelectedAccessible != null) {
                    pce = new PropertyChangeEvent(previousSelectedAccessible,
                        AccessibleContext.ACCESSIBLE_STATE_PROPERTY,
                        AccessibleState.FOCUSED, null);
                    firePropertyChange(AccessibleContext.ACCESSIBLE_STATE_PROPERTY,
                                       null, pce);
                }
                pce = new PropertyChangeEvent(selectedAccessible,
                    AccessibleContext.ACCESSIBLE_STATE_PROPERTY,
                    null, AccessibleState.FOCUSED);
                firePropertyChange(AccessibleContext.ACCESSIBLE_STATE_PROPERTY,
                                   null, pce);
                firePropertyChange(AccessibleContext.ACCESSIBLE_ACTIVE_DESCENDANT_PROPERTY,
                                   previousSelectedAccessible, selectedAccessible);
                previousSelectedAccessible = selectedAccessible;
            }
        }
        public int getAccessibleChildrenCount() {
            if (ui != null) {
                return ui.getAccessibleChildrenCount(JComboBox.this);
            } else {
                return super.getAccessibleChildrenCount();
            }
        }
        public Accessible getAccessibleChild(int i) {
            if (ui != null) {
                return ui.getAccessibleChild(JComboBox.this, i);
            } else {
               return super.getAccessibleChild(i);
            }
        }
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.COMBO_BOX;
        }
        public AccessibleStateSet getAccessibleStateSet() {
            AccessibleStateSet ass = super.getAccessibleStateSet();
            if (ass == null) {
                ass = new AccessibleStateSet();
            }
            if (JComboBox.this.isPopupVisible()) {
                ass.add(AccessibleState.EXPANDED);
            } else {
                ass.add(AccessibleState.COLLAPSED);
            }
            return ass;
        }
        public AccessibleAction getAccessibleAction() {
            return this;
        }
        public String getAccessibleActionDescription(int i) {
            if (i == 0) {
                return UIManager.getString("ComboBox.togglePopupText");
            }
            else {
                return null;
            }
        }
        public int getAccessibleActionCount() {
            return 1;
        }
        public boolean doAccessibleAction(int i) {
            if (i == 0) {
                setPopupVisible(!isPopupVisible());
                return true;
            }
            else {
                return false;
            }
        }
        public AccessibleSelection getAccessibleSelection() {
            return this;
        }
        public int getAccessibleSelectionCount() {
            Object o = JComboBox.this.getSelectedItem();
            if (o != null) {
                return 1;
            } else {
                return 0;
            }
        }
        public Accessible getAccessibleSelection(int i) {
            Accessible a =
                JComboBox.this.getUI().getAccessibleChild(JComboBox.this, 0);
            if (a != null &&
                a instanceof javax.swing.plaf.basic.ComboPopup) {
                JList list = ((javax.swing.plaf.basic.ComboPopup)a).getList();
                AccessibleContext ac = list.getAccessibleContext();
                if (ac != null) {
                    AccessibleSelection as = ac.getAccessibleSelection();
                    if (as != null) {
                        return as.getAccessibleSelection(i);
                    }
                }
            }
            return null;
        }
        public boolean isAccessibleChildSelected(int i) {
            return JComboBox.this.getSelectedIndex() == i;
        }
        public void addAccessibleSelection(int i) {
            clearAccessibleSelection();
            JComboBox.this.setSelectedIndex(i);
        }
        public void removeAccessibleSelection(int i) {
            if (JComboBox.this.getSelectedIndex() == i) {
                clearAccessibleSelection();
            }
        }
        public void clearAccessibleSelection() {
            JComboBox.this.setSelectedIndex(-1);
        }
        public void selectAllAccessibleSelection() {
        }
        private EditorAccessibleContext editorAccessibleContext = null;
        private class AccessibleEditor implements Accessible {
            public AccessibleContext getAccessibleContext() {
                if (editorAccessibleContext == null) {
                    Component c = JComboBox.this.getEditor().getEditorComponent();
                    if (c instanceof Accessible) {
                        editorAccessibleContext =
                            new EditorAccessibleContext((Accessible)c);
                    }
                }
                return editorAccessibleContext;
            }
        }
        private class EditorAccessibleContext extends AccessibleContext {
            private AccessibleContext ac;
            private EditorAccessibleContext() {
            }
            EditorAccessibleContext(Accessible a) {
                this.ac = a.getAccessibleContext();
            }
            public String getAccessibleName() {
                return ac.getAccessibleName();
            }
            public void setAccessibleName(String s) {
                ac.setAccessibleName(s);
            }
            public String getAccessibleDescription() {
                return ac.getAccessibleDescription();
            }
            public void setAccessibleDescription(String s) {
                ac.setAccessibleDescription(s);
            }
            public AccessibleRole getAccessibleRole() {
                return ac.getAccessibleRole();
            }
            public AccessibleStateSet getAccessibleStateSet() {
                return ac.getAccessibleStateSet();
            }
            public Accessible getAccessibleParent() {
                return ac.getAccessibleParent();
            }
            public void setAccessibleParent(Accessible a) {
                ac.setAccessibleParent(a);
            }
            public int getAccessibleIndexInParent() {
                return JComboBox.this.getSelectedIndex();
            }
            public int getAccessibleChildrenCount() {
                return ac.getAccessibleChildrenCount();
            }
            public Accessible getAccessibleChild(int i) {
                return ac.getAccessibleChild(i);
            }
            public Locale getLocale() throws IllegalComponentStateException {
                return ac.getLocale();
            }
            public void addPropertyChangeListener(PropertyChangeListener listener) {
                ac.addPropertyChangeListener(listener);
            }
            public void removePropertyChangeListener(PropertyChangeListener listener) {
                ac.removePropertyChangeListener(listener);
            }
            public AccessibleAction getAccessibleAction() {
                return ac.getAccessibleAction();
            }
            public AccessibleComponent getAccessibleComponent() {
                return ac.getAccessibleComponent();
            }
            public AccessibleSelection getAccessibleSelection() {
                return ac.getAccessibleSelection();
            }
            public AccessibleText getAccessibleText() {
                return ac.getAccessibleText();
            }
            public AccessibleEditableText getAccessibleEditableText() {
                return ac.getAccessibleEditableText();
            }
            public AccessibleValue getAccessibleValue() {
                return ac.getAccessibleValue();
            }
            public AccessibleIcon [] getAccessibleIcon() {
                return ac.getAccessibleIcon();
            }
            public AccessibleRelationSet getAccessibleRelationSet() {
                return ac.getAccessibleRelationSet();
            }
            public AccessibleTable getAccessibleTable() {
                return ac.getAccessibleTable();
            }
            public void firePropertyChange(String propertyName,
                                           Object oldValue,
                                           Object newValue) {
                ac.firePropertyChange(propertyName, oldValue, newValue);
            }
        }
    } 
}
