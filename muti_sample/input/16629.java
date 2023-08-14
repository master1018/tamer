public class JTextField extends JTextComponent implements SwingConstants {
    public JTextField() {
        this(null, null, 0);
    }
    public JTextField(String text) {
        this(null, text, 0);
    }
    public JTextField(int columns) {
        this(null, null, columns);
    }
    public JTextField(String text, int columns) {
        this(null, text, columns);
    }
    public JTextField(Document doc, String text, int columns) {
        if (columns < 0) {
            throw new IllegalArgumentException("columns less than zero.");
        }
        visibility = new DefaultBoundedRangeModel();
        visibility.addChangeListener(new ScrollRepainter());
        this.columns = columns;
        if (doc == null) {
            doc = createDefaultModel();
        }
        setDocument(doc);
        if (text != null) {
            setText(text);
        }
    }
    public String getUIClassID() {
        return uiClassID;
    }
    public void setDocument(Document doc) {
        if (doc != null) {
            doc.putProperty("filterNewlines", Boolean.TRUE);
        }
        super.setDocument(doc);
    }
    @Override
    public boolean isValidateRoot() {
        return !(SwingUtilities.getUnwrappedParent(this) instanceof JViewport);
    }
    public int getHorizontalAlignment() {
        return horizontalAlignment;
    }
     public void setHorizontalAlignment(int alignment) {
        if (alignment == horizontalAlignment) return;
        int oldValue = horizontalAlignment;
        if ((alignment == LEFT) || (alignment == CENTER) ||
            (alignment == RIGHT)|| (alignment == LEADING) ||
            (alignment == TRAILING)) {
            horizontalAlignment = alignment;
        } else {
            throw new IllegalArgumentException("horizontalAlignment");
        }
        firePropertyChange("horizontalAlignment", oldValue, horizontalAlignment);
        invalidate();
        repaint();
    }
    protected Document createDefaultModel() {
        return new PlainDocument();
    }
    public int getColumns() {
        return columns;
    }
    public void setColumns(int columns) {
        int oldVal = this.columns;
        if (columns < 0) {
            throw new IllegalArgumentException("columns less than zero.");
        }
        if (columns != oldVal) {
            this.columns = columns;
            invalidate();
        }
    }
    protected int getColumnWidth() {
        if (columnWidth == 0) {
            FontMetrics metrics = getFontMetrics(getFont());
            columnWidth = metrics.charWidth('m');
        }
        return columnWidth;
    }
    public Dimension getPreferredSize() {
        Dimension size = super.getPreferredSize();
        if (columns != 0) {
            Insets insets = getInsets();
            size.width = columns * getColumnWidth() +
                insets.left + insets.right;
        }
        return size;
    }
    public void setFont(Font f) {
        super.setFont(f);
        columnWidth = 0;
    }
    public synchronized void addActionListener(ActionListener l) {
        listenerList.add(ActionListener.class, l);
    }
    public synchronized void removeActionListener(ActionListener l) {
        if ((l != null) && (getAction() == l)) {
            setAction(null);
        } else {
            listenerList.remove(ActionListener.class, l);
        }
    }
    public synchronized ActionListener[] getActionListeners() {
        return listenerList.getListeners(ActionListener.class);
    }
    protected void fireActionPerformed() {
        Object[] listeners = listenerList.getListenerList();
        int modifiers = 0;
        AWTEvent currentEvent = EventQueue.getCurrentEvent();
        if (currentEvent instanceof InputEvent) {
            modifiers = ((InputEvent)currentEvent).getModifiers();
        } else if (currentEvent instanceof ActionEvent) {
            modifiers = ((ActionEvent)currentEvent).getModifiers();
        }
        ActionEvent e =
            new ActionEvent(this, ActionEvent.ACTION_PERFORMED,
                            (command != null) ? command : getText(),
                            EventQueue.getMostRecentEventTime(), modifiers);
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==ActionListener.class) {
                ((ActionListener)listeners[i+1]).actionPerformed(e);
            }
        }
    }
    public void setActionCommand(String command) {
        this.command = command;
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
    protected void actionPropertyChanged(Action action, String propertyName) {
        if (propertyName == Action.ACTION_COMMAND_KEY) {
            setActionCommandFromAction(action);
        } else if (propertyName == "enabled") {
            AbstractAction.setEnabledFromAction(this, action);
        } else if (propertyName == Action.SHORT_DESCRIPTION) {
            AbstractAction.setToolTipTextFromAction(this, action);
        }
    }
    private void setActionCommandFromAction(Action action) {
        setActionCommand((action == null) ? null :
                         (String)action.getValue(Action.ACTION_COMMAND_KEY));
    }
    protected PropertyChangeListener createActionPropertyChangeListener(Action a) {
        return new TextFieldActionPropertyChangeListener(this, a);
    }
    private static class TextFieldActionPropertyChangeListener extends
                         ActionPropertyChangeListener<JTextField> {
        TextFieldActionPropertyChangeListener(JTextField tf, Action a) {
            super(tf, a);
        }
        protected void actionPropertyChanged(JTextField textField,
                                             Action action,
                                             PropertyChangeEvent e) {
            if (AbstractAction.shouldReconfigure(e)) {
                textField.configurePropertiesFromAction(action);
            } else {
                textField.actionPropertyChanged(action, e.getPropertyName());
            }
        }
    }
    public Action[] getActions() {
        return TextAction.augmentList(super.getActions(), defaultActions);
    }
    public void postActionEvent() {
        fireActionPerformed();
    }
    public BoundedRangeModel getHorizontalVisibility() {
        return visibility;
    }
    public int getScrollOffset() {
        return visibility.getValue();
    }
    public void setScrollOffset(int scrollOffset) {
        visibility.setValue(scrollOffset);
    }
    public void scrollRectToVisible(Rectangle r) {
        Insets i = getInsets();
        int x0 = r.x + visibility.getValue() - i.left;
        int x1 = x0 + r.width;
        if (x0 < visibility.getValue()) {
            visibility.setValue(x0);
        } else if(x1 > visibility.getValue() + visibility.getExtent()) {
            visibility.setValue(x1 - visibility.getExtent());
        }
    }
    boolean hasActionListener() {
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==ActionListener.class) {
                return true;
            }
        }
        return false;
    }
    public static final String notifyAction = "notify-field-accept";
    private BoundedRangeModel visibility;
    private int horizontalAlignment = LEADING;
    private int columns;
    private int columnWidth;
    private String command;
    private static final Action[] defaultActions = {
        new NotifyAction()
    };
    private static final String uiClassID = "TextFieldUI";
    static class NotifyAction extends TextAction {
        NotifyAction() {
            super(notifyAction);
        }
        public void actionPerformed(ActionEvent e) {
            JTextComponent target = getFocusedComponent();
            if (target instanceof JTextField) {
                JTextField field = (JTextField) target;
                field.postActionEvent();
            }
        }
        public boolean isEnabled() {
            JTextComponent target = getFocusedComponent();
            if (target instanceof JTextField) {
                return ((JTextField)target).hasActionListener();
            }
            return false;
        }
    }
    class ScrollRepainter implements ChangeListener, Serializable {
        public void stateChanged(ChangeEvent e) {
            repaint();
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
        String horizontalAlignmentString;
        if (horizontalAlignment == LEFT) {
            horizontalAlignmentString = "LEFT";
        } else if (horizontalAlignment == CENTER) {
            horizontalAlignmentString = "CENTER";
        } else if (horizontalAlignment == RIGHT) {
            horizontalAlignmentString = "RIGHT";
        } else if (horizontalAlignment == LEADING) {
            horizontalAlignmentString = "LEADING";
        } else if (horizontalAlignment == TRAILING) {
            horizontalAlignmentString = "TRAILING";
        } else horizontalAlignmentString = "";
        String commandString = (command != null ?
                                command : "");
        return super.paramString() +
        ",columns=" + columns +
        ",columnWidth=" + columnWidth +
        ",command=" + commandString +
        ",horizontalAlignment=" + horizontalAlignmentString;
    }
    public AccessibleContext getAccessibleContext() {
        if (accessibleContext == null) {
            accessibleContext = new AccessibleJTextField();
        }
        return accessibleContext;
    }
    protected class AccessibleJTextField extends AccessibleJTextComponent {
        public AccessibleStateSet getAccessibleStateSet() {
            AccessibleStateSet states = super.getAccessibleStateSet();
            states.add(AccessibleState.SINGLE_LINE);
            return states;
        }
    }
}
