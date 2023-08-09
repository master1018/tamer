public class FreezableList extends AbstractList {
    private List delegate = null ;
    private boolean immutable = false ;
    public boolean equals( Object obj )
    {
        if (obj == null)
            return false ;
        if (!(obj instanceof FreezableList))
            return false ;
        FreezableList other = (FreezableList)obj ;
        return delegate.equals( other.delegate ) &&
            (immutable == other.immutable) ;
    }
    public int hashCode()
    {
        return delegate.hashCode() ;
    }
    public FreezableList( List delegate, boolean immutable  )
    {
        this.delegate = delegate ;
        this.immutable = immutable ;
    }
    public FreezableList( List delegate )
    {
        this( delegate, false ) ;
    }
    public void makeImmutable()
    {
        immutable = true ;
    }
    public boolean isImmutable()
    {
        return immutable ;
    }
    public void makeElementsImmutable()
    {
        Iterator iter = iterator() ;
        while (iter.hasNext()) {
            Object obj = iter.next() ;
            if (obj instanceof MakeImmutable) {
                MakeImmutable element = (MakeImmutable)obj ;
                element.makeImmutable() ;
            }
        }
    }
    public int size()
    {
        return delegate.size() ;
    }
    public Object get(int index)
    {
        return delegate.get(index) ;
    }
    public Object set(int index, Object element)
    {
        if (immutable)
            throw new UnsupportedOperationException() ;
        return delegate.set(index, element) ;
    }
    public void add(int index, Object element)
    {
        if (immutable)
            throw new UnsupportedOperationException() ;
        delegate.add(index, element) ;
    }
    public Object remove(int index)
    {
        if (immutable)
            throw new UnsupportedOperationException() ;
        return delegate.remove(index) ;
    }
    public List subList(int fromIndex, int toIndex)
    {
        List list = delegate.subList(fromIndex, toIndex) ;
        List result = new FreezableList( list, immutable ) ;
        return result ;
    }
}
