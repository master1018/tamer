public class IORFactories {
    private IORFactories() {}
    public static ObjectId makeObjectId( byte[] id )
    {
        return new ObjectIdImpl( id ) ;
    }
    public static ObjectKey makeObjectKey( ObjectKeyTemplate oktemp, ObjectId oid )
    {
        return new ObjectKeyImpl( oktemp, oid ) ;
    }
    public static IOR makeIOR( ORB orb, String typeid )
    {
        return new IORImpl( orb, typeid ) ;
    }
    public static IOR makeIOR( ORB orb )
    {
        return new IORImpl( orb ) ;
    }
    public static IOR makeIOR( InputStream is )
    {
        return new IORImpl( is ) ;
    }
    public static IORTemplate makeIORTemplate( ObjectKeyTemplate oktemp )
    {
        return new IORTemplateImpl( oktemp ) ;
    }
    public static IORTemplate makeIORTemplate( InputStream is )
    {
        return new IORTemplateImpl( is ) ;
    }
    public static IORTemplateList makeIORTemplateList()
    {
        return new IORTemplateListImpl() ;
    }
    public static IORTemplateList makeIORTemplateList( InputStream is )
    {
        return new IORTemplateListImpl( is ) ;
    }
    public static IORFactory getIORFactory( ObjectReferenceTemplate ort )
    {
        if (ort instanceof ObjectReferenceTemplateImpl) {
            ObjectReferenceTemplateImpl orti =
                (ObjectReferenceTemplateImpl)ort ;
            return orti.getIORFactory() ;
        }
        throw new BAD_PARAM() ;
    }
    public static IORTemplateList getIORTemplateList( ObjectReferenceFactory orf )
    {
        if (orf instanceof ObjectReferenceProducerBase) {
            ObjectReferenceProducerBase base =
                (ObjectReferenceProducerBase)orf ;
            return base.getIORTemplateList() ;
        }
        throw new BAD_PARAM() ;
    }
    public static ObjectReferenceTemplate makeObjectReferenceTemplate( ORB orb,
        IORTemplate iortemp )
    {
        return new ObjectReferenceTemplateImpl( orb, iortemp ) ;
    }
    public static ObjectReferenceFactory makeObjectReferenceFactory( ORB orb,
        IORTemplateList iortemps )
    {
        return new ObjectReferenceFactoryImpl( orb, iortemps ) ;
    }
    public static ObjectKeyFactory makeObjectKeyFactory( ORB orb )
    {
        return new ObjectKeyFactoryImpl( orb ) ;
    }
    public static IOR getIOR( org.omg.CORBA.Object obj )
    {
        return ORBUtility.getIOR( obj ) ;
    }
    public static org.omg.CORBA.Object makeObjectReference( IOR ior )
    {
        return ORBUtility.makeObjectReference( ior ) ;
    }
    public static void registerValueFactories( ORB orb )
    {
        ValueFactory vf = new ValueFactory() {
            public Serializable read_value( InputStream is )
            {
                return new ObjectReferenceTemplateImpl( is ) ;
            }
        } ;
        orb.register_value_factory( ObjectReferenceTemplateImpl.repositoryId, vf ) ;
        vf = new ValueFactory() {
            public Serializable read_value( InputStream is )
            {
                return new ObjectReferenceFactoryImpl( is ) ;
            }
        } ;
        orb.register_value_factory( ObjectReferenceFactoryImpl.repositoryId, vf ) ;
    }
}
