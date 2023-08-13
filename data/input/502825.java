public class PropertyChangeSupport implements Serializable {
    private static final long serialVersionUID = 6401253773779951803l;
    private transient Object sourceBean;
    private transient List<PropertyChangeListener> allPropertiesChangeListeners =
            new ArrayList<PropertyChangeListener>();
    private transient Map<String, List<PropertyChangeListener>>
            selectedPropertiesChangeListeners =
            new HashMap<String, List<PropertyChangeListener>>();
    private Hashtable<String, List<PropertyChangeListener>> children;
    private Object source;
    private int propertyChangeSupportSerializedDataVersion = 1;
    public PropertyChangeSupport(Object sourceBean) {
        if (sourceBean == null) {
            throw new NullPointerException();
        }
        this.sourceBean = sourceBean;
    }
    public void firePropertyChange(String propertyName, Object oldValue,
            Object newValue) {
        PropertyChangeEvent event = createPropertyChangeEvent(propertyName,
                oldValue, newValue);
        doFirePropertyChange(event);
    }
    public void fireIndexedPropertyChange(String propertyName, int index,
            Object oldValue, Object newValue) {
        doFirePropertyChange(new IndexedPropertyChangeEvent(sourceBean,
                propertyName, oldValue, newValue, index));
    }
    public synchronized void removePropertyChangeListener(String propertyName,
            PropertyChangeListener listener) {
        if ((propertyName != null) && (listener != null)) {
            List<PropertyChangeListener> listeners =
                    selectedPropertiesChangeListeners.get(propertyName);
            if (listeners != null) {
                listeners.remove(listener);
            }
        }
    }
    public synchronized void addPropertyChangeListener(String propertyName,
            PropertyChangeListener listener) {
        if ((listener != null) && (propertyName != null)) {
            List<PropertyChangeListener> listeners =
                    selectedPropertiesChangeListeners.get(propertyName);
            if (listeners == null) {
                listeners = new ArrayList<PropertyChangeListener>();
                selectedPropertiesChangeListeners.put(propertyName, listeners);
            }
            if (listener instanceof PropertyChangeListenerProxy) {
                PropertyChangeListenerProxy proxy =
                        (PropertyChangeListenerProxy) listener;
                listeners.add(new PropertyChangeListenerProxy(
                        proxy.getPropertyName(),
                        (PropertyChangeListener) proxy.getListener()));
            } else {
                listeners.add(listener);
            }
        }
    }
    public synchronized PropertyChangeListener[] getPropertyChangeListeners(
            String propertyName) {
        List<PropertyChangeListener> listeners = null;
        if (propertyName != null) {
            listeners = selectedPropertiesChangeListeners.get(propertyName);
        }
        return (listeners == null) ? new PropertyChangeListener[] {}
                : listeners.toArray(
                        new PropertyChangeListener[listeners.size()]);
    }
    public void firePropertyChange(String propertyName, boolean oldValue,
            boolean newValue) {
        PropertyChangeEvent event = createPropertyChangeEvent(propertyName,
                oldValue, newValue);
        doFirePropertyChange(event);
    }
    public void fireIndexedPropertyChange(String propertyName, int index,
            boolean oldValue, boolean newValue) {
        if (oldValue != newValue) {
            fireIndexedPropertyChange(propertyName, index, Boolean
                    .valueOf(oldValue), Boolean.valueOf(newValue));
        }
    }
    public void firePropertyChange(String propertyName, int oldValue,
            int newValue) {
        PropertyChangeEvent event = createPropertyChangeEvent(propertyName,
                oldValue, newValue);
        doFirePropertyChange(event);
    }
    public void fireIndexedPropertyChange(String propertyName, int index,
            int oldValue, int newValue) {
        if (oldValue != newValue) {
            fireIndexedPropertyChange(propertyName, index,
                    new Integer(oldValue), new Integer(newValue));
        }
    }
    public synchronized boolean hasListeners(String propertyName) {
        boolean result = allPropertiesChangeListeners.size() > 0;
        if (!result && (propertyName != null)) {
            List<PropertyChangeListener> listeners =
                    selectedPropertiesChangeListeners.get(propertyName);
            if (listeners != null) {
                result = listeners.size() > 0;
            }
        }
        return result;
    }
    public synchronized void removePropertyChangeListener(
            PropertyChangeListener listener) {
        if (listener != null) {
            if (listener instanceof PropertyChangeListenerProxy) {
                String name = ((PropertyChangeListenerProxy) listener)
                        .getPropertyName();
                PropertyChangeListener lst = (PropertyChangeListener)
                        ((PropertyChangeListenerProxy) listener).getListener();
                removePropertyChangeListener(name, lst);
            } else {
                allPropertiesChangeListeners.remove(listener);
            }
        }
    }
    public synchronized void addPropertyChangeListener(
            PropertyChangeListener listener) {
        if (listener != null) {
            if (listener instanceof PropertyChangeListenerProxy) {
                String name = ((PropertyChangeListenerProxy) listener)
                        .getPropertyName();
                PropertyChangeListener lst = (PropertyChangeListener)
                        ((PropertyChangeListenerProxy) listener).getListener();
                addPropertyChangeListener(name, lst);
            } else {
                allPropertiesChangeListeners.add(listener);
            }
        }
    }
    public synchronized PropertyChangeListener[] getPropertyChangeListeners() {
        ArrayList<PropertyChangeListener> result =
                new ArrayList<PropertyChangeListener>(
                        allPropertiesChangeListeners);
        for (String propertyName : selectedPropertiesChangeListeners.keySet()) {
            List<PropertyChangeListener> selectedListeners =
                    selectedPropertiesChangeListeners.get(propertyName);
            if (selectedListeners != null) {
                for (PropertyChangeListener listener : selectedListeners) {
                    result.add(new PropertyChangeListenerProxy(propertyName,
                            listener));
                }
            }
        }
        return result.toArray(new PropertyChangeListener[result.size()]);
    }
    private void writeObject(ObjectOutputStream oos) throws IOException {
        List<PropertyChangeListener> allSerializedPropertiesChangeListeners =
                new ArrayList<PropertyChangeListener>();
        for (PropertyChangeListener pcl : allPropertiesChangeListeners) {
            if (pcl instanceof Serializable) {
                allSerializedPropertiesChangeListeners.add(pcl);
            }
        }
        Map<String, List<PropertyChangeListener>>
                selectedSerializedPropertiesChangeListeners =
                        new HashMap<String, List<PropertyChangeListener>>();
        for (String propertyName : selectedPropertiesChangeListeners.keySet()) {
            List<PropertyChangeListener> keyValues =
                    selectedPropertiesChangeListeners.get(propertyName);
            if (keyValues != null) {
                List<PropertyChangeListener> serializedPropertiesChangeListeners
                        = new ArrayList<PropertyChangeListener>();
                for (PropertyChangeListener pcl : keyValues) {
                    if (pcl instanceof Serializable) {
                        serializedPropertiesChangeListeners.add(pcl);
                    }
                }
                if (!serializedPropertiesChangeListeners.isEmpty()) {
                    selectedSerializedPropertiesChangeListeners.put(
                            propertyName, serializedPropertiesChangeListeners);
                }
            }
        }
        children = new Hashtable<String, List<PropertyChangeListener>>(
                selectedSerializedPropertiesChangeListeners);
        children.put("", allSerializedPropertiesChangeListeners); 
        oos.writeObject(children);
        Object source = null;
        if (sourceBean instanceof Serializable) {
            source = sourceBean;
        }
        oos.writeObject(source);
        oos.writeInt(propertyChangeSupportSerializedDataVersion);
    }
    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream ois) throws IOException,
            ClassNotFoundException {
        children = (Hashtable<String, List<PropertyChangeListener>>) ois
                .readObject();
        selectedPropertiesChangeListeners = new HashMap<String, List<PropertyChangeListener>>(
                children);
        allPropertiesChangeListeners = selectedPropertiesChangeListeners
                .remove(""); 
        if (allPropertiesChangeListeners == null) {
            allPropertiesChangeListeners = new ArrayList<PropertyChangeListener>();
        }
        sourceBean = ois.readObject();
        propertyChangeSupportSerializedDataVersion = ois.readInt();
    }
    public void firePropertyChange(PropertyChangeEvent event) {
        doFirePropertyChange(event);
    }
    private PropertyChangeEvent createPropertyChangeEvent(String propertyName,
            Object oldValue, Object newValue) {
        return new PropertyChangeEvent(sourceBean, propertyName, oldValue,
                newValue);
    }
    private PropertyChangeEvent createPropertyChangeEvent(String propertyName,
            boolean oldValue, boolean newValue) {
        return new PropertyChangeEvent(sourceBean, propertyName, oldValue,
                newValue);
    }
    private PropertyChangeEvent createPropertyChangeEvent(String propertyName,
            int oldValue, int newValue) {
        return new PropertyChangeEvent(sourceBean, propertyName, oldValue,
                newValue);
    }
    private void doFirePropertyChange(PropertyChangeEvent event) {
        String propertyName = event.getPropertyName();
        Object oldValue = event.getOldValue();
        Object newValue = event.getNewValue();
        if ((newValue != null) && (oldValue != null)
                && newValue.equals(oldValue)) {
            return;
        }
        PropertyChangeListener[] listensToAll;
        PropertyChangeListener[] listensToOne = null;
        synchronized (this) {
            listensToAll = allPropertiesChangeListeners
                    .toArray(new PropertyChangeListener[allPropertiesChangeListeners
                            .size()]);
            List<PropertyChangeListener> listeners = selectedPropertiesChangeListeners
                    .get(propertyName);
            if (listeners != null) {
                listensToOne = listeners
                        .toArray(new PropertyChangeListener[listeners.size()]);
            }
        }
        for (PropertyChangeListener listener : listensToAll) {
            listener.propertyChange(event);
        }
        if (listensToOne != null) {
            for (PropertyChangeListener listener : listensToOne) {
                listener.propertyChange(event);
            }
        }
    }
}
