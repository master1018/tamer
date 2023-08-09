public class JButton extends AbstractButton implements Accessible {
    private static final String uiClassID = "ButtonUI";
    public JButton() {
        this(null, null);
    }
    public JButton(Icon icon) {
        this(null, icon);
    }
    @ConstructorProperties({"text"})
    public JButton(String text) {
        this(text, null);
    }
    public JButton(Action a) {
        this();
        setAction(a);
    }
    public JButton(String text, Icon icon) {
        setModel(new DefaultButtonModel());
        init(text, icon);
    }
    public void updateUI() {
        setUI((ButtonUI)UIManager.getUI(this));
    }
    public String getUIClassID() {
        return uiClassID;
    }
    public boolean isDefaultButton() {
        JRootPane root = SwingUtilities.getRootPane(this);
        if (root != null) {
            return root.getDefaultButton() == this;
        }
        return false;
    }
    public boolean isDefaultCapable() {
        return defaultCapable;
    }
    public void setDefaultCapable(boolean defaultCapable) {
        boolean oldDefaultCapable = this.defaultCapable;
        this.defaultCapable = defaultCapable;
        firePropertyChange("defaultCapable", oldDefaultCapable, defaultCapable);
    }
    public void removeNotify() {
        JRootPane root = SwingUtilities.getRootPane(this);
        if (root != null && root.getDefaultButton() == this) {
            root.setDefaultButton(null);
        }
        super.removeNotify();
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
        String defaultCapableString = (defaultCapable ? "true" : "false");
        return super.paramString() +
            ",defaultCapable=" + defaultCapableString;
    }
    public AccessibleContext getAccessibleContext() {
        if (accessibleContext == null) {
            accessibleContext = new AccessibleJButton();
        }
        return accessibleContext;
    }
    protected class AccessibleJButton extends AccessibleAbstractButton {
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.PUSH_BUTTON;
        }
    } 
}
