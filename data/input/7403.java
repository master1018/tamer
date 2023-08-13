public class PINoOpHandlerImpl implements PIHandler
{
    public PINoOpHandlerImpl( ) {
    }
    public void initialize() {
    }
    public void destroyInterceptors() {
    }
    public void objectAdapterCreated( ObjectAdapter oa )
    {
    }
    public void adapterManagerStateChanged( int managerId,
        short newState )
    {
    }
    public void adapterStateChanged( ObjectReferenceTemplate[]
        templates, short newState )
    {
    }
    public void disableInterceptorsThisThread() {
    }
    public void enableInterceptorsThisThread() {
    }
    public void invokeClientPIStartingPoint()
        throws RemarshalException
    {
    }
    public Exception invokeClientPIEndingPoint(
        int replyStatus, Exception exception )
    {
        return null;
    }
    public Exception makeCompletedClientRequest(
        int replyStatus, Exception exception ) {
        return null;
    }
    public void initiateClientPIRequest( boolean diiRequest ) {
    }
    public void cleanupClientPIRequest() {
    }
    public void setClientPIInfo(CorbaMessageMediator messageMediator)
    {
    }
    public void setClientPIInfo( RequestImpl requestImpl )
    {
    }
    final public void sendCancelRequestIfFinalFragmentNotSent()
    {
    }
    public void invokeServerPIStartingPoint()
    {
    }
    public void invokeServerPIIntermediatePoint()
    {
    }
    public void invokeServerPIEndingPoint( ReplyMessage replyMessage )
    {
    }
    public void setServerPIInfo( Exception exception ) {
    }
    public void setServerPIInfo( NVList arguments )
    {
    }
    public void setServerPIExceptionInfo( Any exception )
    {
    }
    public void setServerPIInfo( Any result )
    {
    }
    public void initializeServerPIInfo( CorbaMessageMediator request,
        ObjectAdapter oa, byte[] objectId, ObjectKeyTemplate oktemp )
    {
    }
    public void setServerPIInfo( java.lang.Object servant,
                                          String targetMostDerivedInterface )
    {
    }
    public void cleanupServerPIRequest() {
    }
    public void register_interceptor( Interceptor interceptor, int type )
        throws DuplicateName
    {
    }
    public Current getPICurrent( ) {
        return null;
    }
    public org.omg.CORBA.Policy create_policy(int type, org.omg.CORBA.Any val)
        throws org.omg.CORBA.PolicyError
    {
        return null;
    }
    public void registerPolicyFactory( int type, PolicyFactory factory ) {
    }
    public int allocateServerRequestId ()
    {
        return 0;
    }
}
