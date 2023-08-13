public abstract class UIAction implements Action {
    private String name;
    public UIAction(String name) {
        this.name = name;
    }
    public final String getName() {
        return name;
    }
    public Object getValue(String key) {
        if (key == NAME) {
            return name;
        }
        return null;
    }
    public void putValue(String key, Object value) {
    }
    public void setEnabled(boolean b) {
    }
    public final boolean isEnabled() {
        return isEnabled(null);
    }
    public boolean isEnabled(Object sender) {
        return true;
    }
    public void addPropertyChangeListener(PropertyChangeListener listener) {
    }
    public void removePropertyChangeListener(PropertyChangeListener listener) {
    }
}
