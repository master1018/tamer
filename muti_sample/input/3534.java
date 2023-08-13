public class IORTemplateImpl extends IdentifiableContainerBase implements IORTemplate
{
    private ObjectKeyTemplate oktemp ;
    public boolean equals( Object obj )
    {
        if (obj == null)
            return false ;
        if (!(obj instanceof IORTemplateImpl))
            return false ;
        IORTemplateImpl other = (IORTemplateImpl)obj ;
        return super.equals( obj ) && oktemp.equals( other.getObjectKeyTemplate() ) ;
    }
    public int hashCode()
    {
        return super.hashCode() ^ oktemp.hashCode() ;
    }
    public ObjectKeyTemplate getObjectKeyTemplate()
    {
        return oktemp ;
    }
    public IORTemplateImpl( ObjectKeyTemplate oktemp )
    {
        this.oktemp = oktemp ;
    }
    public IOR makeIOR( ORB orb, String typeid, ObjectId oid )
    {
        return new IORImpl( orb, typeid, this, oid ) ;
    }
    public boolean isEquivalent( IORFactory other )
    {
        if (!(other instanceof IORTemplate))
            return false ;
        IORTemplate list = (IORTemplate)other ;
        Iterator thisIterator = iterator() ;
        Iterator listIterator = list.iterator() ;
        while (thisIterator.hasNext() && listIterator.hasNext()) {
            TaggedProfileTemplate thisTemplate =
                (TaggedProfileTemplate)thisIterator.next() ;
            TaggedProfileTemplate listTemplate =
                (TaggedProfileTemplate)listIterator.next() ;
            if (!thisTemplate.isEquivalent( listTemplate ))
                return false ;
        }
        return (thisIterator.hasNext() == listIterator.hasNext()) &&
            getObjectKeyTemplate().equals( list.getObjectKeyTemplate() ) ;
    }
    public void makeImmutable()
    {
        makeElementsImmutable() ;
        super.makeImmutable() ;
    }
    public void write( OutputStream os )
    {
        oktemp.write( os ) ;
        EncapsulationUtility.writeIdentifiableSequence( this, os ) ;
    }
    public IORTemplateImpl( InputStream is )
    {
        ORB orb = (ORB)(is.orb()) ;
        IdentifiableFactoryFinder finder =
            orb.getTaggedProfileTemplateFactoryFinder() ;
        oktemp = orb.getObjectKeyFactory().createTemplate( is ) ;
        EncapsulationUtility.readIdentifiableSequence( this, finder, is ) ;
        makeImmutable() ;
    }
}
