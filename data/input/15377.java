public class DefaultColorSelectionModel implements ColorSelectionModel, Serializable {
    protected transient ChangeEvent changeEvent = null;
    protected EventListenerList listenerList = new EventListenerList();
    private Color selectedColor;
    public DefaultColorSelectionModel() {
        selectedColor = Color.white;
    }
    public DefaultColorSelectionModel(Color color) {
        selectedColor = color;
    }
    public Color getSelectedColor() {
        return selectedColor;
    }
    public void setSelectedColor(Color color) {
        if (color != null && !selectedColor.equals(color)) {
            selectedColor = color;
            fireStateChanged();
        }
    }
    public void addChangeListener(ChangeListener l) {
        listenerList.add(ChangeListener.class, l);
    }
    public void removeChangeListener(ChangeListener l) {
        listenerList.remove(ChangeListener.class, l);
    }
    public ChangeListener[] getChangeListeners() {
        return listenerList.getListeners(ChangeListener.class);
    }
    protected void fireStateChanged()
    {
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -=2 ) {
            if (listeners[i] == ChangeListener.class) {
                if (changeEvent == null) {
                    changeEvent = new ChangeEvent(this);
                }
                ((ChangeListener)listeners[i+1]).stateChanged(changeEvent);
            }
        }
    }
}
