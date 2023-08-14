public class NameService
{
    private NamingContext rootContext = null;
    private POA nsPOA = null;
    private ServantManagerImpl contextMgr;
    private ORB theorb;
    public NameService(ORB orb, File logDir)
        throws Exception
    {
        theorb = orb;
        POA rootPOA = (POA)orb.resolve_initial_references(
            ORBConstants.ROOT_POA_NAME ) ;
        rootPOA.the_POAManager().activate();
        int i=0;
        Policy[] poaPolicy = new Policy[4];
        poaPolicy[i++] = rootPOA.create_lifespan_policy(
                         LifespanPolicyValue.PERSISTENT);
        poaPolicy[i++] = rootPOA.create_request_processing_policy(
                         RequestProcessingPolicyValue.USE_SERVANT_MANAGER);
        poaPolicy[i++] = rootPOA.create_id_assignment_policy(
                         IdAssignmentPolicyValue.USER_ID);
        poaPolicy[i++] = rootPOA.create_servant_retention_policy(
                         ServantRetentionPolicyValue.NON_RETAIN);
        nsPOA = rootPOA.create_POA("NameService", null, poaPolicy);
        nsPOA.the_POAManager().activate( );
        contextMgr = new
            ServantManagerImpl(orb, logDir, this );
        String rootKey = contextMgr.getRootObjectKey( );
        NamingContextImpl nc =
                new NamingContextImpl( orb, rootKey, this, contextMgr );
        nc = contextMgr.addContext( rootKey, nc );
        nc.setServantManagerImpl( contextMgr );
        nc.setORB( orb );
        nc.setRootNameService( this );
        nsPOA.set_servant_manager(contextMgr);
        rootContext = NamingContextHelper.narrow(
        nsPOA.create_reference_with_id( rootKey.getBytes( ),
        NamingContextHelper.id( ) ) );
    }
    public NamingContext initialNamingContext()
    {
        return rootContext;
    }
    POA getNSPOA( ) {
        return nsPOA;
    }
    public NamingContext NewContext( ) throws org.omg.CORBA.SystemException
    {
        try
        {
                String newKey =
                contextMgr.getNewObjectKey( );
                NamingContextImpl theContext =
                new NamingContextImpl( theorb, newKey,
                    this, contextMgr );
                NamingContextImpl tempContext = contextMgr.addContext( newKey,
                                                 theContext );
                if( tempContext != null )
                {
                        theContext = tempContext;
                }
                theContext.setServantManagerImpl( contextMgr );
                theContext.setORB( theorb );
                theContext.setRootNameService( this );
                NamingContext theNewContext =
                NamingContextHelper.narrow(
                nsPOA.create_reference_with_id( newKey.getBytes( ),
                NamingContextHelper.id( )) );
                return theNewContext;
        }
        catch( org.omg.CORBA.SystemException e )
        {
                throw e;
        }
        catch( java.lang.Exception e )
        {
        }
        return null;
    }
    org.omg.CORBA.Object getObjectReferenceFromKey( String key )
    {
        org.omg.CORBA.Object theObject = null;
        try
        {
                theObject = nsPOA.create_reference_with_id( key.getBytes( ), NamingContextHelper.id( ) );
        }
        catch (Exception e )
        {
                theObject = null;
        }
        return theObject;
    }
    String getObjectKey( org.omg.CORBA.Object reference )
    {
        byte theId[];
        try
        {
                theId = nsPOA.reference_to_id( reference );
        }
        catch( org.omg.PortableServer.POAPackage.WrongAdapter e )
        {
                return null;
        }
        catch( org.omg.PortableServer.POAPackage.WrongPolicy e )
        {
                return null;
        }
        catch( Exception e )
        {
                return null;
        }
        String theKey = new String( theId );
        return theKey;
    }
}
