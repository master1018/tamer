public class IORTemplateListImpl extends FreezableList implements IORTemplateList
{
    public Object set( int index, Object element )
    {
        if (element instanceof IORTemplate) {
            return super.set( index, element ) ;
        } else if (element instanceof IORTemplateList) {
            Object result = remove( index ) ;
            add( index, element ) ;
            return result ;
        } else
            throw new IllegalArgumentException() ;
    }
    public void add( int index, Object element )
    {
        if (element instanceof IORTemplate) {
            super.add( index, element ) ;
        } else if (element instanceof IORTemplateList) {
            IORTemplateList tl = (IORTemplateList)element ;
            addAll( index, tl ) ;
        } else
            throw new IllegalArgumentException() ;
    }
    public IORTemplateListImpl()
    {
        super( new ArrayList() ) ;
    }
    public IORTemplateListImpl( InputStream is )
    {
        this() ;
        int size = is.read_long() ;
        for (int ctr=0; ctr<size; ctr++) {
            IORTemplate iortemp = IORFactories.makeIORTemplate( is ) ;
            add( iortemp ) ;
        }
        makeImmutable() ;
    }
    public void makeImmutable()
    {
        makeElementsImmutable() ;
        super.makeImmutable() ;
    }
    public void write( OutputStream os )
    {
        os.write_long( size() ) ;
        Iterator iter = iterator() ;
        while (iter.hasNext()) {
            IORTemplate iortemp = (IORTemplate)(iter.next()) ;
            iortemp.write( os ) ;
        }
    }
    public IOR makeIOR( ORB orb, String typeid, ObjectId oid )
    {
        return new IORImpl( orb, typeid, this, oid ) ;
    }
    public boolean isEquivalent( IORFactory other )
    {
        if (!(other instanceof IORTemplateList))
            return false ;
        IORTemplateList list = (IORTemplateList)other ;
        Iterator thisIterator = iterator() ;
        Iterator listIterator = list.iterator() ;
        while (thisIterator.hasNext() && listIterator.hasNext()) {
            IORTemplate thisTemplate = (IORTemplate)thisIterator.next() ;
            IORTemplate listTemplate = (IORTemplate)listIterator.next() ;
            if (!thisTemplate.isEquivalent( listTemplate ))
                return false ;
        }
        return thisIterator.hasNext() == listIterator.hasNext() ;
    }
}
