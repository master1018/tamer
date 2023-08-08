public class EventListenerList<T> {
    private HashMap<Class<T>, ArrayList<T>> listenerList;
    public EventListenerList() {
        listenerList = new HashMap<Class<T>, ArrayList<T>>();
    }
    public void add(Class<T> theClass, T theListener) {
        if (!listenerList.containsKey(theClass)) {
            listenerList.put(theClass, new ArrayList<T>());
        }
        listenerList.get(theClass).add(theListener);
    }
    public void remove(Class<T> theClass, T theListener) {
        if (!listenerList.containsKey(theClass)) return;
        listenerList.get(theClass).remove(theListener);
    }
    public Object[] getListenerList() {
        ArrayList<T> listeners = new ArrayList<T>();
        for (Class<T> listenerClass : listenerList.keySet()) {
            listeners.addAll(listenerList.get(listenerClass));
        }
        return listeners.toArray();
    }
    public Object[] getListenerList(Class<T> theListenerClass) {
        ArrayList<T> listeners = new ArrayList<T>();
        for (Class<T> listenerClass : listenerList.keySet()) {
            if (listenerClass.equals(theListenerClass)) {
                listeners.addAll(listenerList.get(listenerClass));
            }
        }
        return listeners.toArray();
    }
    public int getListenerCount() {
        int listenerCount = 0;
        for (Class<T> listenerClass : listenerList.keySet()) {
            listenerCount += listenerList.get(listenerClass).size();
        }
        return listenerCount;
    }
    public int getListenerCount(Class<T> theListenerClass) {
        int listenerCount = 0;
        for (Class<T> listenerClass : listenerList.keySet()) {
            if (listenerClass.equals(theListenerClass)) {
                listenerCount += listenerList.get(listenerClass).size();
            }
        }
        return listenerCount;
    }
}
