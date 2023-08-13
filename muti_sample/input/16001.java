public abstract class ObjectReferenceProducerBase {
    transient protected ORB orb ;
    public abstract IORFactory getIORFactory() ;
    public abstract IORTemplateList getIORTemplateList() ;
    public ObjectReferenceProducerBase( ORB orb )
    {
        this.orb = orb ;
    }
    public org.omg.CORBA.Object make_object (String repositoryId,
        byte[] objectId)
    {
        ObjectId oid = IORFactories.makeObjectId( objectId ) ;
        IOR ior = getIORFactory().makeIOR( orb, repositoryId, oid ) ;
        return ORBUtility.makeObjectReference( ior ) ;
    }
}
