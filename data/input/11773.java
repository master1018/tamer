public class EventListenerList implements Serializable {
    private final static Object[] NULL_ARRAY = new Object[0];
    protected transient Object[] listenerList = NULL_ARRAY;
    public Object[] getListenerList() {
        return listenerList;
    }
    public <T extends EventListener> T[] getListeners(Class<T> t) {
        Object[] lList = listenerList;
        int n = getListenerCount(lList, t);
        T[] result = (T[])Array.newInstance(t, n);
        int j = 0;
        for (int i = lList.length-2; i>=0; i-=2) {
            if (lList[i] == t) {
                result[j++] = (T)lList[i+1];
            }
        }
        return result;
    }
    public int getListenerCount() {
        return listenerList.length/2;
    }
    public int getListenerCount(Class<?> t) {
        Object[] lList = listenerList;
        return getListenerCount(lList, t);
    }
    private int getListenerCount(Object[] list, Class t) {
        int count = 0;
        for (int i = 0; i < list.length; i+=2) {
            if (t == (Class)list[i])
                count++;
        }
        return count;
    }
    public synchronized <T extends EventListener> void add(Class<T> t, T l) {
        if (l==null) {
            return;
        }
        if (!t.isInstance(l)) {
            throw new IllegalArgumentException("Listener " + l +
                                         " is not of type " + t);
        }
        if (listenerList == NULL_ARRAY) {
            listenerList = new Object[] { t, l };
        } else {
            int i = listenerList.length;
            Object[] tmp = new Object[i+2];
            System.arraycopy(listenerList, 0, tmp, 0, i);
            tmp[i] = t;
            tmp[i+1] = l;
            listenerList = tmp;
        }
    }
    public synchronized <T extends EventListener> void remove(Class<T> t, T l) {
        if (l ==null) {
            return;
        }
        if (!t.isInstance(l)) {
            throw new IllegalArgumentException("Listener " + l +
                                         " is not of type " + t);
        }
        int index = -1;
        for (int i = listenerList.length-2; i>=0; i-=2) {
            if ((listenerList[i]==t) && (listenerList[i+1].equals(l) == true)) {
                index = i;
                break;
            }
        }
        if (index != -1) {
            Object[] tmp = new Object[listenerList.length-2];
            System.arraycopy(listenerList, 0, tmp, 0, index);
            if (index < tmp.length)
                System.arraycopy(listenerList, index+2, tmp, index,
                                 tmp.length - index);
            listenerList = (tmp.length == 0) ? NULL_ARRAY : tmp;
            }
    }
    private void writeObject(ObjectOutputStream s) throws IOException {
        Object[] lList = listenerList;
        s.defaultWriteObject();
        for (int i = 0; i < lList.length; i+=2) {
            Class t = (Class)lList[i];
            EventListener l = (EventListener)lList[i+1];
            if ((l!=null) && (l instanceof Serializable)) {
                s.writeObject(t.getName());
                s.writeObject(l);
            }
        }
        s.writeObject(null);
    }
    private void readObject(ObjectInputStream s)
        throws IOException, ClassNotFoundException {
        listenerList = NULL_ARRAY;
        s.defaultReadObject();
        Object listenerTypeOrNull;
        while (null != (listenerTypeOrNull = s.readObject())) {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            EventListener l = (EventListener)s.readObject();
            add((Class<EventListener>)Class.forName((String)listenerTypeOrNull, true, cl), l);
        }
    }
    public String toString() {
        Object[] lList = listenerList;
        String s = "EventListenerList: ";
        s += lList.length/2 + " listeners: ";
        for (int i = 0 ; i <= lList.length-2 ; i+=2) {
            s += " type " + ((Class)lList[i]).getName();
            s += " listener " + lList[i+1];
        }
        return s;
    }
}
