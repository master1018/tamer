public class PIHandlerImpl implements PIHandler
{
    boolean printPushPopEnabled = false;
    int pushLevel = 0;
    private void printPush()
    {
        if (! printPushPopEnabled) return;
        printSpaces(pushLevel);
        pushLevel++;
        System.out.println("PUSH");
    }
    private void printPop()
    {
        if (! printPushPopEnabled) return;
        pushLevel--;
        printSpaces(pushLevel);
        System.out.println("POP");
    }
    private void printSpaces(int n)
    {
        for (int i = 0; i < n; i++) {
            System.out.print(" ");
        }
    }
    private ORB orb ;
    InterceptorsSystemException wrapper ;
    ORBUtilSystemException orbutilWrapper ;
    OMGSystemException omgWrapper ;
    private int serverRequestIdCounter = 0;
    CodecFactory codecFactory = null;
    String[] arguments = null;
    private InterceptorList interceptorList;
    private boolean hasIORInterceptors;
    private boolean hasClientInterceptors;  
    private boolean hasServerInterceptors;
    private InterceptorInvoker interceptorInvoker;
    private PICurrent current;
    private HashMap policyFactoryTable;
    private final static short REPLY_MESSAGE_TO_PI_REPLY_STATUS[] = {
        SUCCESSFUL.value,       
        USER_EXCEPTION.value,   
        SYSTEM_EXCEPTION.value, 
        LOCATION_FORWARD.value, 
        LOCATION_FORWARD.value, 
        TRANSPORT_RETRY.value   
    };
    private ThreadLocal threadLocalClientRequestInfoStack =
        new ThreadLocal() {
            protected Object initialValue() {
                return new RequestInfoStack();
            }
        };
    private ThreadLocal threadLocalServerRequestInfoStack =
        new ThreadLocal() {
            protected Object initialValue() {
                return new RequestInfoStack();
            }
        };
    private final class RequestInfoStack extends Stack {
        public int disableCount = 0;
    }
    public PIHandlerImpl( ORB orb, String[] args ) {
        this.orb = orb ;
        wrapper = InterceptorsSystemException.get( orb,
            CORBALogDomains.RPC_PROTOCOL ) ;
        orbutilWrapper = ORBUtilSystemException.get( orb,
            CORBALogDomains.RPC_PROTOCOL ) ;
        omgWrapper = OMGSystemException.get( orb,
            CORBALogDomains.RPC_PROTOCOL ) ;
        arguments = args ;
        codecFactory = new CodecFactoryImpl( orb );
        interceptorList = new InterceptorList( wrapper );
        current = new PICurrent( orb );
        interceptorInvoker = new InterceptorInvoker( orb, interceptorList,
                                                     current );
        orb.getLocalResolver().register( ORBConstants.PI_CURRENT_NAME,
            ClosureFactory.makeConstant( current ) ) ;
        orb.getLocalResolver().register( ORBConstants.CODEC_FACTORY_NAME,
            ClosureFactory.makeConstant( codecFactory ) ) ;
    }
    public void initialize() {
        if( orb.getORBData().getORBInitializers() != null ) {
            ORBInitInfoImpl orbInitInfo = createORBInitInfo();
            current.setORBInitializing( true );
            preInitORBInitializers( orbInitInfo );
            postInitORBInitializers( orbInitInfo );
            interceptorList.sortInterceptors();
            current.setORBInitializing( false );
            orbInitInfo.setStage( ORBInitInfoImpl.STAGE_CLOSED );
            hasIORInterceptors = interceptorList.hasInterceptorsOfType(
                InterceptorList.INTERCEPTOR_TYPE_IOR );
            hasClientInterceptors = true;
            hasServerInterceptors = interceptorList.hasInterceptorsOfType(
                InterceptorList.INTERCEPTOR_TYPE_SERVER );
            interceptorInvoker.setEnabled( true );
        }
    }
    public void destroyInterceptors() {
        interceptorList.destroyAll();
    }
    public void objectAdapterCreated( ObjectAdapter oa )
    {
        if (!hasIORInterceptors)
            return ;
        interceptorInvoker.objectAdapterCreated( oa ) ;
    }
    public void adapterManagerStateChanged( int managerId,
        short newState )
    {
        if (!hasIORInterceptors)
            return ;
        interceptorInvoker.adapterManagerStateChanged( managerId, newState ) ;
    }
    public void adapterStateChanged( ObjectReferenceTemplate[]
        templates, short newState )
    {
        if (!hasIORInterceptors)
            return ;
        interceptorInvoker.adapterStateChanged( templates, newState ) ;
    }
    public void disableInterceptorsThisThread() {
        if( !hasClientInterceptors ) return;
        RequestInfoStack infoStack =
            (RequestInfoStack)threadLocalClientRequestInfoStack.get();
        infoStack.disableCount++;
    }
    public void enableInterceptorsThisThread() {
        if( !hasClientInterceptors ) return;
        RequestInfoStack infoStack =
            (RequestInfoStack)threadLocalClientRequestInfoStack.get();
        infoStack.disableCount--;
    }
    public void invokeClientPIStartingPoint()
        throws RemarshalException
    {
        if( !hasClientInterceptors ) return;
        if( !isClientPIEnabledForThisThread() ) return;
        ClientRequestInfoImpl info = peekClientRequestInfoImplStack();
        interceptorInvoker.invokeClientInterceptorStartingPoint( info );
        short replyStatus = info.getReplyStatus();
        if( (replyStatus == SYSTEM_EXCEPTION.value) ||
            (replyStatus == LOCATION_FORWARD.value) )
        {
            Exception exception = invokeClientPIEndingPoint(
                convertPIReplyStatusToReplyMessage( replyStatus ),
                info.getException() );
            if( exception == null ) {
            } if( exception instanceof SystemException ) {
                throw (SystemException)exception;
            } else if( exception instanceof RemarshalException ) {
                throw (RemarshalException)exception;
            } else if( (exception instanceof UserException) ||
                     (exception instanceof ApplicationException) ) {
                throw wrapper.exceptionInvalid() ;
            }
        }
        else if( replyStatus != ClientRequestInfoImpl.UNINITIALIZED ) {
            throw wrapper.replyStatusNotInit() ;
        }
    }
    public Exception makeCompletedClientRequest( int replyStatus,
        Exception exception ) {
        return handleClientPIEndingPoint( replyStatus, exception, false ) ;
    }
    public Exception invokeClientPIEndingPoint( int replyStatus,
        Exception exception ) {
        return handleClientPIEndingPoint( replyStatus, exception, true ) ;
    }
    public Exception handleClientPIEndingPoint(
        int replyStatus, Exception exception, boolean invokeEndingPoint ) {
        if( !hasClientInterceptors ) return exception;
        if( !isClientPIEnabledForThisThread() ) return exception;
        short piReplyStatus = REPLY_MESSAGE_TO_PI_REPLY_STATUS[replyStatus];
        ClientRequestInfoImpl info = peekClientRequestInfoImplStack();
        info.setReplyStatus( piReplyStatus );
        info.setException( exception );
        if (invokeEndingPoint) {
            interceptorInvoker.invokeClientInterceptorEndingPoint( info );
            piReplyStatus = info.getReplyStatus();
        }
        if( (piReplyStatus == LOCATION_FORWARD.value) ||
            (piReplyStatus == TRANSPORT_RETRY.value) ) {
            info.reset();
            if (invokeEndingPoint) {
                info.setRetryRequest( RetryType.AFTER_RESPONSE ) ;
            } else {
                info.setRetryRequest( RetryType.BEFORE_RESPONSE ) ;
            }
            exception = new RemarshalException();
        } else if( (piReplyStatus == SYSTEM_EXCEPTION.value) ||
                 (piReplyStatus == USER_EXCEPTION.value) ) {
            exception = info.getException();
        }
        return exception;
    }
    public void initiateClientPIRequest( boolean diiRequest ) {
        if( !hasClientInterceptors ) return;
        if( !isClientPIEnabledForThisThread() ) return;
        RequestInfoStack infoStack =
            (RequestInfoStack)threadLocalClientRequestInfoStack.get();
        ClientRequestInfoImpl info = null;
        if (!infoStack.empty() ) {
            info = (ClientRequestInfoImpl)infoStack.peek();
        }
        if (!diiRequest && (info != null) && info.isDIIInitiate() ) {
            info.setDIIInitiate( false );
        } else {
            if( (info == null) || !info.getRetryRequest().isRetry() ) {
                info = new ClientRequestInfoImpl( orb );
                infoStack.push( info );
                printPush();
            }
            info.setRetryRequest( RetryType.NONE );
            info.incrementEntryCount();
            info.setReplyStatus( RequestInfoImpl.UNINITIALIZED ) ;
            if( diiRequest ) {
                info.setDIIInitiate( true );
            }
        }
    }
    public void cleanupClientPIRequest() {
        if( !hasClientInterceptors ) return;
        if( !isClientPIEnabledForThisThread() ) return;
        ClientRequestInfoImpl info = peekClientRequestInfoImplStack();
        RetryType rt = info.getRetryRequest() ;
        if (!rt.equals( RetryType.BEFORE_RESPONSE )) {
            short replyStatus = info.getReplyStatus();
            if (replyStatus == info.UNINITIALIZED ) {
                invokeClientPIEndingPoint( ReplyMessage.SYSTEM_EXCEPTION,
                    wrapper.unknownRequestInvoke(
                        CompletionStatus.COMPLETED_MAYBE ) ) ;
            }
        }
        info.decrementEntryCount();
        if (info.getEntryCount() == 0 && !info.getRetryRequest().isRetry()) {
            RequestInfoStack infoStack =
                (RequestInfoStack)threadLocalClientRequestInfoStack.get();
            infoStack.pop();
            printPop();
        }
    }
    public void setClientPIInfo(CorbaMessageMediator messageMediator)
    {
        if( !hasClientInterceptors ) return;
        if( !isClientPIEnabledForThisThread() ) return;
        peekClientRequestInfoImplStack().setInfo(messageMediator);
    }
    public void setClientPIInfo( RequestImpl requestImpl ) {
        if( !hasClientInterceptors ) return;
        if( !isClientPIEnabledForThisThread() ) return;
        peekClientRequestInfoImplStack().setDIIRequest( requestImpl );
    }
    public void invokeServerPIStartingPoint()
    {
        if( !hasServerInterceptors ) return;
        ServerRequestInfoImpl info = peekServerRequestInfoImplStack();
        interceptorInvoker.invokeServerInterceptorStartingPoint( info );
        serverPIHandleExceptions( info );
    }
    public void invokeServerPIIntermediatePoint()
    {
        if( !hasServerInterceptors ) return;
        ServerRequestInfoImpl info = peekServerRequestInfoImplStack();
        interceptorInvoker.invokeServerInterceptorIntermediatePoint( info );
        info.releaseServant();
        serverPIHandleExceptions( info );
    }
    public void invokeServerPIEndingPoint( ReplyMessage replyMessage )
    {
        if( !hasServerInterceptors ) return;
        ServerRequestInfoImpl info = peekServerRequestInfoImplStack();
        info.setReplyMessage( replyMessage );
        info.setCurrentExecutionPoint( info.EXECUTION_POINT_ENDING );
        if( !info.getAlreadyExecuted() ) {
            int replyStatus = replyMessage.getReplyStatus();
            short piReplyStatus =
                REPLY_MESSAGE_TO_PI_REPLY_STATUS[replyStatus];
            if( ( piReplyStatus == LOCATION_FORWARD.value ) ||
                ( piReplyStatus == TRANSPORT_RETRY.value ) )
            {
                info.setForwardRequest( replyMessage.getIOR() );
            }
            Exception prevException = info.getException();
            if( !info.isDynamic() &&
                (piReplyStatus == USER_EXCEPTION.value) )
            {
                info.setException( omgWrapper.unknownUserException(
                    CompletionStatus.COMPLETED_MAYBE ) ) ;
            }
            info.setReplyStatus( piReplyStatus );
            interceptorInvoker.invokeServerInterceptorEndingPoint( info );
            short newPIReplyStatus = info.getReplyStatus();
            Exception newException = info.getException();
            if( ( newPIReplyStatus == SYSTEM_EXCEPTION.value ) &&
                ( newException != prevException ) )
            {
                throw (SystemException)newException;
            }
            if( newPIReplyStatus == LOCATION_FORWARD.value ) {
                if( piReplyStatus != LOCATION_FORWARD.value ) {
                    IOR ior = info.getForwardRequestIOR();
                    throw new ForwardException( orb, ior ) ;
                }
                else if( info.isForwardRequestRaisedInEnding() ) {
                    replyMessage.setIOR( info.getForwardRequestIOR() );
                }
            }
        }
    }
    public void setServerPIInfo( Exception exception ) {
        if( !hasServerInterceptors ) return;
        ServerRequestInfoImpl info = peekServerRequestInfoImplStack();
        info.setException( exception );
    }
    public void setServerPIInfo( NVList arguments )
    {
        if( !hasServerInterceptors ) return;
        ServerRequestInfoImpl info = peekServerRequestInfoImplStack();
        info.setDSIArguments( arguments );
    }
    public void setServerPIExceptionInfo( Any exception )
    {
        if( !hasServerInterceptors ) return;
        ServerRequestInfoImpl info = peekServerRequestInfoImplStack();
        info.setDSIException( exception );
    }
    public void setServerPIInfo( Any result )
    {
        if( !hasServerInterceptors ) return;
        ServerRequestInfoImpl info = peekServerRequestInfoImplStack();
        info.setDSIResult( result );
    }
    public void initializeServerPIInfo( CorbaMessageMediator request,
        ObjectAdapter oa, byte[] objectId, ObjectKeyTemplate oktemp )
    {
        if( !hasServerInterceptors ) return;
        RequestInfoStack infoStack =
            (RequestInfoStack)threadLocalServerRequestInfoStack.get();
        ServerRequestInfoImpl info = new ServerRequestInfoImpl( orb );
        infoStack.push( info );
        printPush();
        request.setExecutePIInResponseConstructor( true );
        info.setInfo( request, oa, objectId, oktemp );
    }
    public void setServerPIInfo( java.lang.Object servant,
                                          String targetMostDerivedInterface )
    {
        if( !hasServerInterceptors ) return;
        ServerRequestInfoImpl info = peekServerRequestInfoImplStack();
        info.setInfo( servant, targetMostDerivedInterface );
    }
    public void cleanupServerPIRequest() {
        if( !hasServerInterceptors ) return;
        RequestInfoStack infoStack =
            (RequestInfoStack)threadLocalServerRequestInfoStack.get();
        infoStack.pop();
        printPop();
    }
    private void serverPIHandleExceptions( ServerRequestInfoImpl info )
    {
        int endingPointCall = info.getEndingPointCall();
        if(endingPointCall == ServerRequestInfoImpl.CALL_SEND_EXCEPTION) {
            throw (SystemException)info.getException();
        }
        else if( (endingPointCall == ServerRequestInfoImpl.CALL_SEND_OTHER) &&
                 (info.getForwardRequestException() != null) )
        {
            IOR ior = info.getForwardRequestIOR();
            throw new ForwardException( orb, ior );
        }
    }
    private int convertPIReplyStatusToReplyMessage( short replyStatus ) {
        int result = 0;
        for( int i = 0; i < REPLY_MESSAGE_TO_PI_REPLY_STATUS.length; i++ ) {
            if( REPLY_MESSAGE_TO_PI_REPLY_STATUS[i] == replyStatus ) {
                result = i;
                break;
            }
        }
        return result;
    }
    private ClientRequestInfoImpl peekClientRequestInfoImplStack() {
        RequestInfoStack infoStack =
            (RequestInfoStack)threadLocalClientRequestInfoStack.get();
        ClientRequestInfoImpl info = null;
        if( !infoStack.empty() ) {
            info = (ClientRequestInfoImpl)infoStack.peek();
        } else {
            throw wrapper.clientInfoStackNull() ;
        }
        return info;
    }
    private ServerRequestInfoImpl peekServerRequestInfoImplStack() {
        RequestInfoStack infoStack =
            (RequestInfoStack)threadLocalServerRequestInfoStack.get();
        ServerRequestInfoImpl info = null;
        if( !infoStack.empty() ) {
            info = (ServerRequestInfoImpl)infoStack.peek();
        } else {
            throw wrapper.serverInfoStackNull() ;
        }
        return info;
    }
    private boolean isClientPIEnabledForThisThread() {
        RequestInfoStack infoStack =
            (RequestInfoStack)threadLocalClientRequestInfoStack.get();
        return (infoStack.disableCount == 0);
    }
    private void preInitORBInitializers( ORBInitInfoImpl info ) {
        info.setStage( ORBInitInfoImpl.STAGE_PRE_INIT );
        for( int i = 0; i < orb.getORBData().getORBInitializers().length;
            i++ ) {
            ORBInitializer init = orb.getORBData().getORBInitializers()[i];
            if( init != null ) {
                try {
                    init.pre_init( info );
                }
                catch( Exception e ) {
                }
            }
        }
    }
    private void postInitORBInitializers( ORBInitInfoImpl info ) {
        info.setStage( ORBInitInfoImpl.STAGE_POST_INIT );
        for( int i = 0; i < orb.getORBData().getORBInitializers().length;
            i++ ) {
            ORBInitializer init = orb.getORBData().getORBInitializers()[i];
            if( init != null ) {
                try {
                    init.post_init( info );
                }
                catch( Exception e ) {
                }
            }
        }
    }
    private ORBInitInfoImpl createORBInitInfo() {
        ORBInitInfoImpl result = null;
        String orbId = orb.getORBData().getORBId() ;
        result = new ORBInitInfoImpl( orb, arguments, orbId, codecFactory );
        return result;
    }
    public void register_interceptor( Interceptor interceptor, int type )
        throws DuplicateName
    {
        if( (type >= InterceptorList.NUM_INTERCEPTOR_TYPES) || (type < 0) ) {
            throw wrapper.typeOutOfRange( new Integer( type ) ) ;
        }
        String interceptorName = interceptor.name();
        if( interceptorName == null ) {
            throw wrapper.nameNull() ;
        }
        interceptorList.register_interceptor( interceptor, type );
    }
    public Current getPICurrent( ) {
        return current;
    }
    private void nullParam()
        throws BAD_PARAM
    {
        throw orbutilWrapper.nullParam() ;
    }
    public org.omg.CORBA.Policy create_policy(int type, org.omg.CORBA.Any val)
        throws org.omg.CORBA.PolicyError
    {
        if( val == null ) {
            nullParam( );
        }
        if( policyFactoryTable == null ) {
            throw new org.omg.CORBA.PolicyError(
                "There is no PolicyFactory Registered for type " + type,
                BAD_POLICY.value );
        }
        PolicyFactory factory = (PolicyFactory)policyFactoryTable.get(
            new Integer(type) );
        if( factory == null ) {
            throw new org.omg.CORBA.PolicyError(
                " Could Not Find PolicyFactory for the Type " + type,
                BAD_POLICY.value);
        }
        org.omg.CORBA.Policy policy = factory.create_policy( type, val );
        return policy;
    }
    public void registerPolicyFactory( int type, PolicyFactory factory ) {
        if( policyFactoryTable == null ) {
            policyFactoryTable = new HashMap();
        }
        Integer key = new Integer( type );
        java.lang.Object val = policyFactoryTable.get( key );
        if( val == null ) {
            policyFactoryTable.put( key, factory );
        }
        else {
            throw omgWrapper.policyFactoryRegFailed( new Integer( type ) ) ;
        }
    }
    public synchronized int allocateServerRequestId ()
    {
        return serverRequestIdCounter++;
    }
}
