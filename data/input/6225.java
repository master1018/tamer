public class DefaultListModel<E> extends AbstractListModel<E>
{
    private Vector<E> delegate = new Vector<E>();
    public int getSize() {
        return delegate.size();
    }
    public E getElementAt(int index) {
        return delegate.elementAt(index);
    }
    public void copyInto(Object anArray[]) {
        delegate.copyInto(anArray);
    }
    public void trimToSize() {
        delegate.trimToSize();
    }
    public void ensureCapacity(int minCapacity) {
        delegate.ensureCapacity(minCapacity);
    }
    public void setSize(int newSize) {
        int oldSize = delegate.size();
        delegate.setSize(newSize);
        if (oldSize > newSize) {
            fireIntervalRemoved(this, newSize, oldSize-1);
        }
        else if (oldSize < newSize) {
            fireIntervalAdded(this, oldSize, newSize-1);
        }
    }
    public int capacity() {
        return delegate.capacity();
    }
    public int size() {
        return delegate.size();
    }
    public boolean isEmpty() {
        return delegate.isEmpty();
    }
    public Enumeration<E> elements() {
        return delegate.elements();
    }
    public boolean contains(Object elem) {
        return delegate.contains(elem);
    }
    public int indexOf(Object elem) {
        return delegate.indexOf(elem);
    }
     public int indexOf(Object elem, int index) {
        return delegate.indexOf(elem, index);
    }
    public int lastIndexOf(Object elem) {
        return delegate.lastIndexOf(elem);
    }
    public int lastIndexOf(Object elem, int index) {
        return delegate.lastIndexOf(elem, index);
    }
    public E elementAt(int index) {
        return delegate.elementAt(index);
    }
    public E firstElement() {
        return delegate.firstElement();
    }
    public E lastElement() {
        return delegate.lastElement();
    }
    public void setElementAt(E element, int index) {
        delegate.setElementAt(element, index);
        fireContentsChanged(this, index, index);
    }
    public void removeElementAt(int index) {
        delegate.removeElementAt(index);
        fireIntervalRemoved(this, index, index);
    }
    public void insertElementAt(E element, int index) {
        delegate.insertElementAt(element, index);
        fireIntervalAdded(this, index, index);
    }
    public void addElement(E element) {
        int index = delegate.size();
        delegate.addElement(element);
        fireIntervalAdded(this, index, index);
    }
    public boolean removeElement(Object obj) {
        int index = indexOf(obj);
        boolean rv = delegate.removeElement(obj);
        if (index >= 0) {
            fireIntervalRemoved(this, index, index);
        }
        return rv;
    }
    public void removeAllElements() {
        int index1 = delegate.size()-1;
        delegate.removeAllElements();
        if (index1 >= 0) {
            fireIntervalRemoved(this, 0, index1);
        }
    }
   public String toString() {
        return delegate.toString();
    }
    public Object[] toArray() {
        Object[] rv = new Object[delegate.size()];
        delegate.copyInto(rv);
        return rv;
    }
    public E get(int index) {
        return delegate.elementAt(index);
    }
    public E set(int index, E element) {
        E rv = delegate.elementAt(index);
        delegate.setElementAt(element, index);
        fireContentsChanged(this, index, index);
        return rv;
    }
    public void add(int index, E element) {
        delegate.insertElementAt(element, index);
        fireIntervalAdded(this, index, index);
    }
    public E remove(int index) {
        E rv = delegate.elementAt(index);
        delegate.removeElementAt(index);
        fireIntervalRemoved(this, index, index);
        return rv;
    }
    public void clear() {
        int index1 = delegate.size()-1;
        delegate.removeAllElements();
        if (index1 >= 0) {
            fireIntervalRemoved(this, 0, index1);
        }
    }
    public void removeRange(int fromIndex, int toIndex) {
        if (fromIndex > toIndex) {
            throw new IllegalArgumentException("fromIndex must be <= toIndex");
        }
        for(int i = toIndex; i >= fromIndex; i--) {
            delegate.removeElementAt(i);
        }
        fireIntervalRemoved(this, fromIndex, toIndex);
    }
}
