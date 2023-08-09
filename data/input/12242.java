public abstract class LocalClientRequestDispatcherBase implements LocalClientRequestDispatcher
{
    protected ORB orb;
    int scid;
    protected boolean servantIsLocal ;
    protected ObjectAdapterFactory oaf ;
    protected ObjectAdapterId oaid ;
    protected byte[] objectId ;
    protected static ThreadLocal isNextCallValid = new ThreadLocal() {
            protected synchronized Object initialValue() {
                return Boolean.TRUE;
            }
        };
    protected LocalClientRequestDispatcherBase(ORB orb, int scid, IOR ior)
    {
        this.orb = orb ;
        TaggedProfile prof = ior.getProfile() ;
        servantIsLocal = orb.getORBData().isLocalOptimizationAllowed() &&
            prof.isLocal();
        ObjectKeyTemplate oktemp = prof.getObjectKeyTemplate() ;
        this.scid = oktemp.getSubcontractId() ;
        RequestDispatcherRegistry sreg = orb.getRequestDispatcherRegistry() ;
        oaf = sreg.getObjectAdapterFactory( scid ) ;
        oaid = oktemp.getObjectAdapterId() ;
        ObjectId oid = prof.getObjectId() ;
        objectId = oid.getId() ;
    }
    public byte[] getObjectId()
    {
        return objectId ;
    }
    public boolean is_local(org.omg.CORBA.Object self)
    {
        return false;
    }
    public boolean useLocalInvocation( org.omg.CORBA.Object self )
    {
        if (isNextCallValid.get() == Boolean.TRUE)
            return servantIsLocal ;
        else
            isNextCallValid.set( Boolean.TRUE ) ;
        return false ;
    }
    protected boolean checkForCompatibleServant( ServantObject so,
        Class expectedType )
    {
        if (so == null)
            return false ;
        if (!expectedType.isInstance( so.servant )) {
            isNextCallValid.set( Boolean.FALSE ) ;
            return false ;
        }
        return true ;
    }
}
