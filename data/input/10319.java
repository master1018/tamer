public class JToggleButton extends AbstractButton implements Accessible {
    private static final String uiClassID = "ToggleButtonUI";
    public JToggleButton () {
        this(null, null, false);
    }
    public JToggleButton(Icon icon) {
        this(null, icon, false);
    }
    public JToggleButton(Icon icon, boolean selected) {
        this(null, icon, selected);
    }
    public JToggleButton (String text) {
        this(text, null, false);
    }
    public JToggleButton (String text, boolean selected) {
        this(text, null, selected);
    }
    public JToggleButton(Action a) {
        this();
        setAction(a);
    }
    public JToggleButton(String text, Icon icon) {
        this(text, icon, false);
    }
    public JToggleButton (String text, Icon icon, boolean selected) {
        setModel(new ToggleButtonModel());
        model.setSelected(selected);
        init(text, icon);
    }
    public void updateUI() {
        setUI((ButtonUI)UIManager.getUI(this));
    }
    public String getUIClassID() {
        return uiClassID;
    }
    boolean shouldUpdateSelectedStateFromAction() {
        return true;
    }
    public static class ToggleButtonModel extends DefaultButtonModel {
        public ToggleButtonModel () {
        }
        public boolean isSelected() {
                return (stateMask & SELECTED) != 0;
        }
        public void setSelected(boolean b) {
            ButtonGroup group = getGroup();
            if (group != null) {
                group.setSelected(this, b);
                b = group.isSelected(this);
            }
            if (isSelected() == b) {
                return;
            }
            if (b) {
                stateMask |= SELECTED;
            } else {
                stateMask &= ~SELECTED;
            }
            fireStateChanged();
            fireItemStateChanged(
                    new ItemEvent(this,
                                  ItemEvent.ITEM_STATE_CHANGED,
                                  this,
                                  this.isSelected() ?  ItemEvent.SELECTED : ItemEvent.DESELECTED));
        }
        public void setPressed(boolean b) {
            if ((isPressed() == b) || !isEnabled()) {
                return;
            }
            if (b == false && isArmed()) {
                setSelected(!this.isSelected());
            }
            if (b) {
                stateMask |= PRESSED;
            } else {
                stateMask &= ~PRESSED;
            }
            fireStateChanged();
            if(!isPressed() && isArmed()) {
                int modifiers = 0;
                AWTEvent currentEvent = EventQueue.getCurrentEvent();
                if (currentEvent instanceof InputEvent) {
                    modifiers = ((InputEvent)currentEvent).getModifiers();
                } else if (currentEvent instanceof ActionEvent) {
                    modifiers = ((ActionEvent)currentEvent).getModifiers();
                }
                fireActionPerformed(
                    new ActionEvent(this, ActionEvent.ACTION_PERFORMED,
                                    getActionCommand(),
                                    EventQueue.getMostRecentEventTime(),
                                    modifiers));
            }
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
            accessibleContext = new AccessibleJToggleButton();
        }
        return accessibleContext;
    }
    protected class AccessibleJToggleButton extends AccessibleAbstractButton
            implements ItemListener {
        public AccessibleJToggleButton() {
            super();
            JToggleButton.this.addItemListener(this);
        }
        public void itemStateChanged(ItemEvent e) {
            JToggleButton tb = (JToggleButton) e.getSource();
            if (JToggleButton.this.accessibleContext != null) {
                if (tb.isSelected()) {
                    JToggleButton.this.accessibleContext.firePropertyChange(
                            AccessibleContext.ACCESSIBLE_STATE_PROPERTY,
                            null, AccessibleState.CHECKED);
                } else {
                    JToggleButton.this.accessibleContext.firePropertyChange(
                            AccessibleContext.ACCESSIBLE_STATE_PROPERTY,
                            AccessibleState.CHECKED, null);
                }
            }
        }
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.TOGGLE_BUTTON;
        }
    } 
}
