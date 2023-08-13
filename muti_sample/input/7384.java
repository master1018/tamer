public class DefaultSingleSelectionModel implements SingleSelectionModel,
Serializable {
    protected transient ChangeEvent changeEvent = null;
    protected EventListenerList listenerList = new EventListenerList();
    private int index = -1;
    public int getSelectedIndex() {
        return index;
    }
    public void setSelectedIndex(int index) {
        if (this.index != index) {
            this.index = index;
            fireStateChanged();
        }
    }
    public void clearSelection() {
        setSelectedIndex(-1);
    }
    public boolean isSelected() {
        boolean ret = false;
        if (getSelectedIndex() != -1) {
            ret = true;
        }
        return ret;
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
    protected void fireStateChanged() {
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==ChangeListener.class) {
                if (changeEvent == null)
                    changeEvent = new ChangeEvent(this);
                ((ChangeListener)listeners[i+1]).stateChanged(changeEvent);
            }
        }
    }
    public <T extends EventListener> T[] getListeners(Class<T> listenerType) {
        return listenerList.getListeners(listenerType);
    }
}
