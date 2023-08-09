public final class ServerRequestInfoImpl
    extends RequestInfoImpl
    implements ServerRequestInfo
{
    static final int CALL_RECEIVE_REQUEST_SERVICE_CONTEXT = 0;
    static final int CALL_RECEIVE_REQUEST = 0;
    static final int CALL_INTERMEDIATE_NONE = 1;
    static final int CALL_SEND_REPLY = 0;
    static final int CALL_SEND_EXCEPTION = 1;
    static final int CALL_SEND_OTHER = 2;
    private boolean forwardRequestRaisedInEnding;
    private CorbaMessageMediator request;
    private java.lang.Object servant;
    private byte[] objectId;
    private ObjectKeyTemplate oktemp ;
    private byte[] adapterId;
    private String[] adapterName;
    private ArrayList addReplyServiceContextQueue;
    private ReplyMessage replyMessage;
    private String targetMostDerivedInterface;
    private NVList dsiArguments;
    private Any dsiResult;
    private Any dsiException;
    private boolean isDynamic;
    private ObjectAdapter objectAdapter;
    private int serverRequestId;
    private Parameter[] cachedArguments;
    private Any cachedSendingException;
    private HashMap cachedRequestServiceContexts;
    private HashMap cachedReplyServiceContexts;
    void reset() {
        super.reset();
        forwardRequestRaisedInEnding = false;
        request = null;
        servant = null;
        objectId = null;
        oktemp = null;
        adapterId = null;
        adapterName = null;
        addReplyServiceContextQueue = null;
        replyMessage = null;
        targetMostDerivedInterface = null;
        dsiArguments = null;
        dsiResult = null;
        dsiException = null;
        isDynamic = false;
        objectAdapter = null;
        serverRequestId = myORB.getPIHandler().allocateServerRequestId();
        cachedArguments = null;
        cachedSendingException = null;
        cachedRequestServiceContexts = null;
        cachedReplyServiceContexts = null;
        startingPointCall = CALL_RECEIVE_REQUEST_SERVICE_CONTEXT;
        intermediatePointCall = CALL_RECEIVE_REQUEST;
        endingPointCall = CALL_SEND_REPLY;
    }
    protected static final int MID_SENDING_EXCEPTION       = MID_RI_LAST +  1;
    protected static final int MID_OBJECT_ID               = MID_RI_LAST +  2;
    protected static final int MID_ADAPTER_ID              = MID_RI_LAST +  3;
    protected static final int MID_TARGET_MOST_DERIVED_INTERFACE
                                                           = MID_RI_LAST +  4;
    protected static final int MID_GET_SERVER_POLICY       = MID_RI_LAST +  5;
    protected static final int MID_SET_SLOT                = MID_RI_LAST +  6;
    protected static final int MID_TARGET_IS_A             = MID_RI_LAST +  7;
    protected static final int MID_ADD_REPLY_SERVICE_CONTEXT
                                                           = MID_RI_LAST +  8;
    protected static final int MID_SERVER_ID               = MID_RI_LAST +  9;
    protected static final int MID_ORB_ID                  = MID_RI_LAST +  10;
    protected static final int MID_ADAPTER_NAME            = MID_RI_LAST +  11;
    protected static final boolean validCall[][] = {
                          { true , true , true , true , true  },
                           { true , true , true , true , true  },
                           { false, true , true , false, false },
                          { false, true , true , true , true  },
                            { false, true , true , true , true  },
                   { false, true , true , false, false },
                              { false, false, true , false, false },
                   { true , true , true , true , true  },
                          { true , true , true , true , true  },
                        { false, false, true , true , true  },
                   { false, false, false, false, true  },
                            { true , true , true , true , true  },
         { true , true , true , true , true  },
           { false, false, true , true , true  },
                   { false, false, false, true , false },
                           { false, true , true , true , true  },
                          { false, true , true , true , true  },
         { false, true , false, false, false },
                   { true , true , true , true , true  },
                            { true , true , true , true , true  },
                         { false, true , false, false, false },
           { true , true , true , true , true  },
                              { false, true , true , true , true  },
                           { false, true , true , true , true  },
                        { false, true , true , true , true  }
    };
    ServerRequestInfoImpl( ORB myORB ) {
        super( myORB );
        startingPointCall = CALL_RECEIVE_REQUEST_SERVICE_CONTEXT;
        intermediatePointCall = CALL_RECEIVE_REQUEST;
        endingPointCall = CALL_SEND_REPLY;
        serverRequestId = myORB.getPIHandler().allocateServerRequestId();
    }
    public Any sending_exception () {
        checkAccess( MID_SENDING_EXCEPTION );
        if( cachedSendingException == null ) {
            Any result = null ;
            if( dsiException != null ) {
                result = dsiException;
            } else if( exception != null ) {
                result = exceptionToAny( exception );
            } else {
                throw wrapper.exceptionUnavailable() ;
            }
            cachedSendingException = result;
        }
        return cachedSendingException;
    }
    public byte[] object_id () {
        checkAccess( MID_OBJECT_ID );
        if( objectId == null ) {
            throw stdWrapper.piOperationNotSupported6() ;
        }
        return objectId;
    }
    private void checkForNullTemplate()
    {
        if (oktemp == null) {
            throw stdWrapper.piOperationNotSupported7() ;
        }
    }
    public String server_id()
    {
        checkAccess( MID_SERVER_ID ) ;
        checkForNullTemplate() ;
        return Integer.toString( oktemp.getServerId() ) ;
    }
    public String orb_id()
    {
        checkAccess( MID_ORB_ID ) ;
        return myORB.getORBData().getORBId() ;
    }
    synchronized public String[] adapter_name()
    {
        checkAccess( MID_ADAPTER_NAME ) ;
        if (adapterName == null) {
            checkForNullTemplate() ;
            ObjectAdapterId oaid = oktemp.getObjectAdapterId() ;
            adapterName = oaid.getAdapterName() ;
        }
        return adapterName ;
    }
    synchronized public byte[] adapter_id ()
    {
        checkAccess( MID_ADAPTER_ID );
        if( adapterId == null ) {
            checkForNullTemplate() ;
            adapterId = oktemp.getAdapterId() ;
        }
        return adapterId;
    }
    public String target_most_derived_interface () {
        checkAccess( MID_TARGET_MOST_DERIVED_INTERFACE );
        return targetMostDerivedInterface;
    }
    public Policy get_server_policy (int type) {
        Policy result = null;
        if( objectAdapter != null ) {
            result = objectAdapter.getEffectivePolicy( type );
        }
        return result;
    }
    public void set_slot (int id, Any data) throws InvalidSlot {
        slotTable.set_slot( id, data );
    }
    public boolean target_is_a (String id) {
        checkAccess( MID_TARGET_IS_A );
        boolean result = false ;
        if( servant instanceof Servant ) {
            result = ((Servant)servant)._is_a( id );
        } else if (StubAdapter.isStub( servant )) {
            result = ((org.omg.CORBA.Object)servant)._is_a( id );
        } else {
            throw wrapper.servantInvalid() ;
        }
        return result;
    }
    public void add_reply_service_context ( ServiceContext service_context,
                                            boolean replace )
    {
        if( currentExecutionPoint == EXECUTION_POINT_ENDING ) {
            ServiceContexts scs = replyMessage.getServiceContexts();
            if( scs == null ) {
                scs = new ServiceContexts( myORB );
                replyMessage.setServiceContexts( scs );
            }
            if( cachedReplyServiceContexts == null ) {
                cachedReplyServiceContexts = new HashMap();
            }
            addServiceContext( cachedReplyServiceContexts, scs,
                               service_context, replace );
        }
        AddReplyServiceContextCommand addReply =
            new AddReplyServiceContextCommand();
        addReply.service_context = service_context;
        addReply.replace = replace;
        if( addReplyServiceContextQueue == null ) {
            addReplyServiceContextQueue = new ArrayList();
        }
        enqueue( addReply );
    }
    public int request_id (){
        return serverRequestId;
    }
    public String operation (){
        return request.getOperationName();
    }
    public Parameter[] arguments (){
        checkAccess( MID_ARGUMENTS );
        if( cachedArguments == null ) {
            if( !isDynamic ) {
                throw stdWrapper.piOperationNotSupported1() ;
            }
            if( dsiArguments == null ) {
                throw stdWrapper.piOperationNotSupported8() ;
            }
            cachedArguments = nvListToParameterArray( dsiArguments );
        }
        return cachedArguments;
    }
    public TypeCode[] exceptions (){
        checkAccess( MID_EXCEPTIONS );
        throw stdWrapper.piOperationNotSupported2() ;
    }
    public String[] contexts (){
        checkAccess( MID_CONTEXTS );
        throw stdWrapper.piOperationNotSupported3() ;
    }
    public String[] operation_context (){
        checkAccess( MID_OPERATION_CONTEXT );
        throw stdWrapper.piOperationNotSupported4() ;
    }
    public Any result (){
        checkAccess( MID_RESULT );
        if( !isDynamic ) {
            throw stdWrapper.piOperationNotSupported5() ;
        }
        if( dsiResult == null ) {
            throw wrapper.piDsiResultIsNull() ;
        }
        return dsiResult;
    }
    public boolean response_expected (){
        return !request.isOneWay();
    }
    public Object forward_reference (){
        checkAccess( MID_FORWARD_REFERENCE );
        if( replyStatus != LOCATION_FORWARD.value ) {
            throw stdWrapper.invalidPiCall1() ;
        }
        return getForwardRequestException().forward;
    }
    public org.omg.IOP.ServiceContext get_request_service_context( int id ) {
        checkAccess( MID_GET_REQUEST_SERVICE_CONTEXT );
        if( cachedRequestServiceContexts == null ) {
            cachedRequestServiceContexts = new HashMap();
        }
        return getServiceContext( cachedRequestServiceContexts,
                                  request.getRequestServiceContexts(), id );
    }
    public org.omg.IOP.ServiceContext get_reply_service_context( int id ) {
        checkAccess( MID_GET_REPLY_SERVICE_CONTEXT );
        if( cachedReplyServiceContexts == null ) {
            cachedReplyServiceContexts = new HashMap();
        }
        return getServiceContext( cachedReplyServiceContexts,
                                  replyMessage.getServiceContexts(), id );
    }
    private class AddReplyServiceContextCommand {
        ServiceContext service_context;
        boolean replace;
    }
    private void enqueue( AddReplyServiceContextCommand addReply ) {
        int size = addReplyServiceContextQueue.size();
        boolean found = false;
        for( int i = 0; i < size; i++ ) {
            AddReplyServiceContextCommand cmd =
                (AddReplyServiceContextCommand)
                addReplyServiceContextQueue.get( i );
            if( cmd.service_context.context_id ==
                addReply.service_context.context_id )
            {
                found = true;
                if( addReply.replace ) {
                    addReplyServiceContextQueue.set( i, addReply );
                } else {
                    throw stdWrapper.serviceContextAddFailed(
                        new Integer( cmd.service_context.context_id ) ) ;
                }
                break;
            }
        }
        if( !found ) {
            addReplyServiceContextQueue.add( addReply );
        }
    }
    protected void setCurrentExecutionPoint( int executionPoint ) {
        super.setCurrentExecutionPoint( executionPoint );
        if( (executionPoint == EXECUTION_POINT_ENDING) &&
            (addReplyServiceContextQueue != null) )
        {
            int size = addReplyServiceContextQueue.size();
            for( int i = 0; i < size; i++ ) {
                AddReplyServiceContextCommand addReply =
                    (AddReplyServiceContextCommand)
                    addReplyServiceContextQueue.get( i );
                try {
                    add_reply_service_context( addReply.service_context,
                                               addReply.replace );
                }
                catch( BAD_INV_ORDER e ) {
                }
            }
        }
    }
    protected void setInfo( CorbaMessageMediator request, ObjectAdapter oa,
        byte[] objectId, ObjectKeyTemplate oktemp )
    {
        this.request = request;
        this.objectId = objectId;
        this.oktemp = oktemp;
        this.objectAdapter = oa ;
        this.connection = (com.sun.corba.se.spi.legacy.connection.Connection)
            request.getConnection();
    }
    protected void setDSIArguments( NVList arguments ) {
        this.dsiArguments = arguments;
    }
    protected void setDSIException( Any exception ) {
        this.dsiException = exception;
        cachedSendingException = null;
    }
    protected void setDSIResult( Any result ) {
        this.dsiResult = result;
    }
    protected void setException( Exception exception ) {
        super.setException( exception );
        this.dsiException = null;
        cachedSendingException = null;
    }
    protected void setInfo( java.lang.Object servant,
                            String targetMostDerivedInterface )
    {
        this.servant = servant;
        this.targetMostDerivedInterface = targetMostDerivedInterface;
        this.isDynamic =
            (servant instanceof
            org.omg.PortableServer.DynamicImplementation) ||
            (servant instanceof org.omg.CORBA.DynamicImplementation);
    }
    void setReplyMessage( ReplyMessage replyMessage ) {
        this.replyMessage = replyMessage;
    }
    protected void setReplyStatus( short replyStatus ) {
        super.setReplyStatus( replyStatus );
        switch( replyStatus ) {
        case SUCCESSFUL.value:
            endingPointCall = CALL_SEND_REPLY;
            break;
        case SYSTEM_EXCEPTION.value:
        case USER_EXCEPTION.value:
            endingPointCall = CALL_SEND_EXCEPTION;
            break;
        case LOCATION_FORWARD.value:
        case TRANSPORT_RETRY.value:
            endingPointCall = CALL_SEND_OTHER;
            break;
        }
    }
    void releaseServant() {
        this.servant = null;
    }
    void setForwardRequestRaisedInEnding() {
        this.forwardRequestRaisedInEnding = true;
    }
    boolean isForwardRequestRaisedInEnding() {
        return this.forwardRequestRaisedInEnding;
    }
    boolean isDynamic() {
      return this.isDynamic;
    }
    protected void checkAccess( int methodID )
    {
        int validCallIndex = 0;
        switch( currentExecutionPoint ) {
        case EXECUTION_POINT_STARTING:
            validCallIndex = 0;
            break;
        case EXECUTION_POINT_INTERMEDIATE:
            validCallIndex = 1;
            break;
        case EXECUTION_POINT_ENDING:
            switch( endingPointCall ) {
            case CALL_SEND_REPLY:
                validCallIndex = 2;
                break;
            case CALL_SEND_EXCEPTION:
                validCallIndex = 3;
                break;
            case CALL_SEND_OTHER:
                validCallIndex = 4;
                break;
            }
            break;
        }
        if( !validCall[methodID][validCallIndex] ) {
            throw stdWrapper.invalidPiCall2() ;
        }
    }
}
