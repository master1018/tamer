public class IdentifiableContainerBase extends FreezableList
{
    public IdentifiableContainerBase()
    {
        super( new ArrayList() ) ;
    }
    public Iterator iteratorById( final int id)
    {
        return new Iterator() {
            Iterator iter = IdentifiableContainerBase.this.iterator() ;
            Object current = advance() ;
            private Object advance()
            {
                while (iter.hasNext()) {
                    Identifiable ide = (Identifiable)(iter.next()) ;
                    if (ide.getId() == id)
                        return ide ;
                }
                return null ;
            }
            public boolean hasNext()
            {
                return current != null ;
            }
            public Object next()
            {
                Object result = current ;
                current = advance() ;
                return result ;
            }
            public void remove()
            {
                iter.remove() ;
            }
        } ;
    }
}
