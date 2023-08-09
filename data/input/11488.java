public class TransientNameService
{
    public TransientNameService(com.sun.corba.se.spi.orb.ORB orb )
        throws org.omg.CORBA.INITIALIZE
    {
        initialize( orb, "NameService" );
    }
    public TransientNameService(com.sun.corba.se.spi.orb.ORB orb,
        String serviceName ) throws org.omg.CORBA.INITIALIZE
    {
        initialize( orb, serviceName );
    }
    private void initialize( com.sun.corba.se.spi.orb.ORB orb,
        String nameServiceName )
        throws org.omg.CORBA.INITIALIZE
    {
        NamingSystemException wrapper = NamingSystemException.get( orb,
            CORBALogDomains.NAMING ) ;
        try {
            POA rootPOA = (POA) orb.resolve_initial_references(
                ORBConstants.ROOT_POA_NAME );
            rootPOA.the_POAManager().activate();
            int i = 0;
            Policy[] poaPolicy = new Policy[3];
            poaPolicy[i++] = rootPOA.create_lifespan_policy(
                LifespanPolicyValue.TRANSIENT);
            poaPolicy[i++] = rootPOA.create_id_assignment_policy(
                IdAssignmentPolicyValue.SYSTEM_ID);
            poaPolicy[i++] = rootPOA.create_servant_retention_policy(
                ServantRetentionPolicyValue.RETAIN);
            POA nsPOA = rootPOA.create_POA( "TNameService", null, poaPolicy );
            nsPOA.the_POAManager().activate();
            TransientNamingContext initialContext =
                new TransientNamingContext(orb, null, nsPOA);
            byte[] rootContextId = nsPOA.activate_object( initialContext );
            initialContext.localRoot =
                nsPOA.id_to_reference( rootContextId );
            theInitialNamingContext = initialContext.localRoot;
            orb.register_initial_reference( nameServiceName,
                theInitialNamingContext );
        } catch (org.omg.CORBA.SystemException e) {
            throw wrapper.transNsCannotCreateInitialNcSys( e ) ;
        } catch (Exception e) {
            throw wrapper.transNsCannotCreateInitialNc( e ) ;
        }
    }
    public org.omg.CORBA.Object initialNamingContext()
    {
        return theInitialNamingContext;
    }
    private org.omg.CORBA.Object theInitialNamingContext;
}
