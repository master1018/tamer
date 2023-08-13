public class MinimalServantCacheLocalCRDImpl extends ServantCacheLocalCRDBase
{
    public MinimalServantCacheLocalCRDImpl( ORB orb, int scid, IOR ior )
    {
        super( (com.sun.corba.se.spi.orb.ORB)orb, scid, ior ) ;
    }
    public ServantObject servant_preinvoke( org.omg.CORBA.Object self,
        String operation, Class expectedType )
    {
        OAInvocationInfo cachedInfo = getCachedInfo() ;
        if (checkForCompatibleServant( cachedInfo, expectedType ))
            return cachedInfo ;
        else
            return null ;
    }
    public void servant_postinvoke(org.omg.CORBA.Object self,
                                   ServantObject servantobj)
    {
    }
}
