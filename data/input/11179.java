public final class ORBInitInfoImpl
    extends org.omg.CORBA.LocalObject
    implements ORBInitInfo, ORBInitInfoExt
{
    private ORB orb;
    private InterceptorsSystemException wrapper ;
    private ORBUtilSystemException orbutilWrapper ;
    private OMGSystemException omgWrapper ;
    private String[] args;
    private String orbId;
    private CodecFactory codecFactory;
    private int stage = STAGE_PRE_INIT;
    public static final int STAGE_PRE_INIT = 0;
    public static final int STAGE_POST_INIT = 1;
    public static final int STAGE_CLOSED = 2;
    private static final String MESSAGE_ORBINITINFO_INVALID =
        "ORBInitInfo object is only valid during ORB_init";
    ORBInitInfoImpl( ORB orb, String[] args,
        String orbId, CodecFactory codecFactory )
    {
        this.orb = orb;
        wrapper = InterceptorsSystemException.get( orb,
            CORBALogDomains.RPC_PROTOCOL ) ;
        orbutilWrapper = ORBUtilSystemException.get( orb,
            CORBALogDomains.RPC_PROTOCOL ) ;
        omgWrapper = OMGSystemException.get( orb,
            CORBALogDomains.RPC_PROTOCOL ) ;
        this.args = args;
        this.orbId = orbId;
        this.codecFactory = codecFactory;
    }
    public ORB getORB()
    {
        return orb ;
    }
    void setStage( int stage ) {
        this.stage = stage;
    }
    private void checkStage() {
        if( stage == STAGE_CLOSED ) {
            throw wrapper.orbinitinfoInvalid() ;
        }
    }
    public String[] arguments () {
        checkStage();
        return args;
    }
    public String orb_id () {
        checkStage();
        return orbId;
    }
    public CodecFactory codec_factory () {
        checkStage();
        return codecFactory;
    }
    public void register_initial_reference( String id,
                                            org.omg.CORBA.Object obj )
        throws InvalidName
    {
        checkStage();
        if( id == null ) nullParam();
        if( obj == null ) {
            throw omgWrapper.rirWithNullObject() ;
        }
        try {
            orb.register_initial_reference( id, obj );
        } catch( org.omg.CORBA.ORBPackage.InvalidName e ) {
            InvalidName exc = new InvalidName( e.getMessage() );
            exc.initCause( e ) ;
            throw exc ;
        }
    }
    public org.omg.CORBA.Object resolve_initial_references (String id)
        throws InvalidName
    {
        checkStage();
        if( id == null ) nullParam();
        if( stage == STAGE_PRE_INIT ) {
            throw wrapper.rirInvalidPreInit() ;
        }
        org.omg.CORBA.Object objRef = null;
        try {
            objRef = orb.resolve_initial_references( id );
        }
        catch( org.omg.CORBA.ORBPackage.InvalidName e ) {
            throw new InvalidName();
        }
        return objRef;
    }
    public void add_client_request_interceptor_with_policy (
        ClientRequestInterceptor interceptor, Policy[] policies )
        throws DuplicateName
    {
        add_client_request_interceptor( interceptor ) ;
    }
    public void add_client_request_interceptor (
        ClientRequestInterceptor interceptor)
        throws DuplicateName
    {
        checkStage();
        if( interceptor == null ) nullParam();
        orb.getPIHandler().register_interceptor( interceptor,
            InterceptorList.INTERCEPTOR_TYPE_CLIENT );
    }
    public void add_server_request_interceptor_with_policy (
        ServerRequestInterceptor interceptor, Policy[] policies )
        throws DuplicateName, PolicyError
    {
        add_server_request_interceptor( interceptor ) ;
    }
    public void add_server_request_interceptor (
        ServerRequestInterceptor interceptor)
        throws DuplicateName
    {
        checkStage();
        if( interceptor == null ) nullParam();
        orb.getPIHandler().register_interceptor( interceptor,
            InterceptorList.INTERCEPTOR_TYPE_SERVER );
    }
    public void add_ior_interceptor_with_policy (
        IORInterceptor interceptor, Policy[] policies )
        throws DuplicateName, PolicyError
    {
        add_ior_interceptor( interceptor ) ;
    }
    public void add_ior_interceptor (
        IORInterceptor interceptor )
        throws DuplicateName
    {
        checkStage();
        if( interceptor == null ) nullParam();
        orb.getPIHandler().register_interceptor( interceptor,
            InterceptorList.INTERCEPTOR_TYPE_IOR );
    }
    public int allocate_slot_id () {
        checkStage();
        return ((PICurrent)orb.getPIHandler().getPICurrent()).allocateSlotId( );
    }
    public void register_policy_factory( int type,
                                         PolicyFactory policy_factory )
    {
        checkStage();
        if( policy_factory == null ) nullParam();
        orb.getPIHandler().registerPolicyFactory( type, policy_factory );
    }
    private void nullParam()
        throws BAD_PARAM
    {
        throw orbutilWrapper.nullParam() ;
    }
}
