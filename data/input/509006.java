public class PropertyEditorSupport implements PropertyEditor {
    Object source = null;
    List<PropertyChangeListener> listeners = new ArrayList<PropertyChangeListener>();
    Object oldValue = null;
    Object newValue = null;
    public PropertyEditorSupport(Object source) {
        if (source == null) {
            throw new NullPointerException(Messages.getString("beans.0C")); 
        }
        this.source = source;
    }
    public PropertyEditorSupport() {
        source = this;
    }
    public void paintValue(Graphics gfx, Rectangle box) {
    }
    public void setAsText(String text) throws IllegalArgumentException {
        if (newValue instanceof String) {
            setValue(text);
        } else {
            throw new IllegalArgumentException(text);
        }
    }
    public String[] getTags() {
        return null;
    }
    public String getJavaInitializationString() {
        return "???"; 
    }
    public String getAsText() {
        return newValue == null ? "null" : newValue.toString(); 
    }
    public void setValue(Object value) {
        this.oldValue = this.newValue;
        this.newValue = value;
        firePropertyChange();
    }
    public Object getValue() {
        return newValue;
    }
    public void setSource(Object source) {
        if (source == null) {
            throw new NullPointerException(Messages.getString("beans.0C")); 
        }
        this.source = source;
    }
    public Object getSource() {
        return source;
    }
    public synchronized void removePropertyChangeListener(
            PropertyChangeListener listener) {
        if (listeners != null) {
            listeners.remove(listener);
        }
    }
    public synchronized void addPropertyChangeListener(
            PropertyChangeListener listener) {
        listeners.add(listener);
    }
    public Component getCustomEditor() {
        return null;
    }
    public boolean supportsCustomEditor() {
        return false;
    }
    public boolean isPaintable() {
        return false;
    }
    public void firePropertyChange() {
        if (listeners.size() > 0) {
            PropertyChangeEvent event = new PropertyChangeEvent(source, null,
                    oldValue, newValue);
            Iterator<PropertyChangeListener> iterator = listeners.iterator();
            while (iterator.hasNext()) {
                PropertyChangeListener listener = iterator.next();
                listener.propertyChange(event);
            }
        }
    }
}
