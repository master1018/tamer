public class INSServerRequestDispatcher
    implements CorbaServerRequestDispatcher
{
    private ORB orb = null;
    private ORBUtilSystemException wrapper ;
    public INSServerRequestDispatcher( ORB orb ) {
        this.orb = orb;
        this.wrapper = ORBUtilSystemException.get( orb,
            CORBALogDomains.RPC_PROTOCOL ) ;
    }
    public IOR locate(ObjectKey okey) {
        String insKey = new String( okey.getBytes(orb) );
        return getINSReference( insKey );
    }
    public void dispatch(MessageMediator mediator)
    {
        CorbaMessageMediator request = (CorbaMessageMediator) mediator;
        String insKey = new String( request.getObjectKey().getBytes(orb) );
        request.getProtocolHandler()
            .createLocationForward(request, getINSReference( insKey ), null);
        return;
    }
    private IOR getINSReference( String insKey ) {
        IOR entry = ORBUtility.getIOR( orb.getLocalResolver().resolve( insKey ) ) ;
        if( entry != null ) {
            return entry;
        }
        throw wrapper.servantNotFound() ;
    }
}
