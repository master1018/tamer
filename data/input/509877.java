public class ListenerList<T extends EventListener> implements Serializable {
    private static final long serialVersionUID = 9180703263299648154L;
    private transient ArrayList<T> systemList;
    private transient ArrayList<T> userList;
    public ListenerList() {
        super();
    }
    public void addSystemListener(T listener) {
        if (systemList == null) {
            systemList = new ArrayList<T>();
        }
        systemList.add(listener);
    }
    public void addUserListener(T listener) {
        if (listener == null) {
            return;
        }
        synchronized (this) {
            if (userList == null) {
                userList = new ArrayList<T>();
                userList.add(listener);
                return;
            }
            ArrayList<T> newList = new ArrayList<T>(userList);
            newList.add(listener);
            userList = newList;
        }
    }
    public void removeUserListener(Object listener) {
        if (listener == null) {
            return;
        }
        synchronized (this) {
            if (userList == null || !userList.contains(listener)) {
                return;
            }
            ArrayList<T> newList = new ArrayList<T>(userList);
            newList.remove(listener);
            userList = (newList.size() > 0 ? newList : null);
        }
    }
    public <AT> AT[] getUserListeners(AT[] emptyArray){
        synchronized (this) {
            return (userList != null ? userList.toArray(emptyArray) : emptyArray);
        }
    }
    public List<T> getUserListeners() {
        synchronized (this) {
            if (userList == null || userList.isEmpty()) {
                return Collections.emptyList();
            }
            return new ArrayList<T>(userList);
        }
    }
    public List<T> getSystemListeners() {
        synchronized (this) {
            if (systemList == null || systemList.isEmpty()) {
                return Collections.emptyList();
            }
            return new ArrayList<T>(systemList);
        }
    }
    public Iterator<T> getUserIterator() {
        synchronized (this) {
            if (userList == null) {
                List<T> emptyList = Collections.emptyList();
                return emptyList.iterator();
            }
            return new ReadOnlyIterator<T>(userList.iterator());
        }
    }
    public Iterator<T> getSystemIterator() {
        return systemList.iterator();
    }
    private static ArrayList<?> getOnlySerializable(ArrayList<?> list) {
        if (list == null) {
            return null;
        }
        ArrayList<Object> result = new ArrayList<Object>();
        for (Iterator<?> it = list.iterator(); it.hasNext();) {
            Object obj = it.next();
            if (obj instanceof Serializable) {
                result.add(obj);
            }
        }
        return (result.size() != 0) ? result : null;
    }
    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
        stream.writeObject(getOnlySerializable(systemList));
        stream.writeObject(getOnlySerializable(userList));
    }
    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream stream)
            throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        systemList = (ArrayList<T>)stream.readObject();
        userList = (ArrayList<T>)stream.readObject();
    }
}
