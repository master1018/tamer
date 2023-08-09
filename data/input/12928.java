public class FullServantCacheLocalCRDImpl extends ServantCacheLocalCRDBase
{
    public FullServantCacheLocalCRDImpl( ORB orb, int scid, IOR ior )
    {
        super( (com.sun.corba.se.spi.orb.ORB)orb, scid, ior ) ;
    }
    public ServantObject servant_preinvoke( org.omg.CORBA.Object self,
        String operation, Class expectedType )
    {
        OAInvocationInfo cachedInfo = getCachedInfo() ;
        if (!checkForCompatibleServant( cachedInfo, expectedType ))
            return null ;
        OAInvocationInfo info = new OAInvocationInfo( cachedInfo, operation ) ;
        orb.pushInvocationInfo( info ) ;
        try {
            info.oa().enter() ;
        } catch (OADestroyed pdes) {
            throw wrapper.preinvokePoaDestroyed( pdes ) ;
        }
        return info ;
    }
    public void servant_postinvoke(org.omg.CORBA.Object self,
                                   ServantObject servantobj)
    {
        OAInvocationInfo cachedInfo = getCachedInfo() ;
        cachedInfo.oa().exit() ;
        orb.popInvocationInfo() ;
    }
}
