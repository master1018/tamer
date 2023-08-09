public class InterceptorInvoker {
    private ORB orb;
    private InterceptorList interceptorList;
    private boolean enabled = false;
    private PICurrent current;
    InterceptorInvoker( ORB orb, InterceptorList interceptorList,
                        PICurrent piCurrent )
    {
        this.orb = orb;
        this.interceptorList = interceptorList;
        this.enabled = false;
        this.current = piCurrent;
    }
    void setEnabled( boolean enabled ) {
        this.enabled = enabled;
    }
    void objectAdapterCreated( ObjectAdapter oa ) {
        if( enabled ) {
            IORInfoImpl info = new IORInfoImpl( oa );
            IORInterceptor[] iorInterceptors =
                (IORInterceptor[])interceptorList.getInterceptors(
                InterceptorList.INTERCEPTOR_TYPE_IOR );
            int size = iorInterceptors.length;
            for( int i = (size - 1); i >= 0; i-- ) {
                IORInterceptor interceptor = iorInterceptors[i];
                try {
                    interceptor.establish_components( info );
                }
                catch( Exception e ) {
                }
            }
            info.makeStateEstablished() ;
            for( int i = (size - 1); i >= 0; i-- ) {
                IORInterceptor interceptor = iorInterceptors[i];
                if (interceptor instanceof IORInterceptor_3_0) {
                    IORInterceptor_3_0 interceptor30 = (IORInterceptor_3_0)interceptor ;
                    interceptor30.components_established( info );
                }
            }
            info.makeStateDone() ;
        }
    }
    void adapterManagerStateChanged( int managerId, short newState )
    {
        if (enabled) {
            IORInterceptor[] interceptors =
                (IORInterceptor[])interceptorList.getInterceptors(
                InterceptorList.INTERCEPTOR_TYPE_IOR );
            int size = interceptors.length;
            for( int i = (size - 1); i >= 0; i-- ) {
                try {
                    IORInterceptor interceptor = interceptors[i];
                    if (interceptor instanceof IORInterceptor_3_0) {
                        IORInterceptor_3_0 interceptor30 = (IORInterceptor_3_0)interceptor ;
                        interceptor30.adapter_manager_state_changed( managerId,
                            newState );
                    }
                } catch (Exception exc) {
                }
            }
        }
    }
    void adapterStateChanged( ObjectReferenceTemplate[] templates,
        short newState )
    {
        if (enabled) {
            IORInterceptor[] interceptors =
                (IORInterceptor[])interceptorList.getInterceptors(
                InterceptorList.INTERCEPTOR_TYPE_IOR );
            int size = interceptors.length;
            for( int i = (size - 1); i >= 0; i-- ) {
                try {
                    IORInterceptor interceptor = interceptors[i];
                    if (interceptor instanceof IORInterceptor_3_0) {
                        IORInterceptor_3_0 interceptor30 = (IORInterceptor_3_0)interceptor ;
                        interceptor30.adapter_state_changed( templates, newState );
                    }
                } catch (Exception exc) {
                }
            }
        }
    }
    void invokeClientInterceptorStartingPoint( ClientRequestInfoImpl info ) {
        if( enabled ) {
            try {
                current.pushSlotTable( );
                info.setPICurrentPushed( true );
                info.setCurrentExecutionPoint( info.EXECUTION_POINT_STARTING );
                ClientRequestInterceptor[] clientInterceptors =
                    (ClientRequestInterceptor[])interceptorList.
                    getInterceptors( InterceptorList.INTERCEPTOR_TYPE_CLIENT );
                int size = clientInterceptors.length;
                int flowStackIndex = size;
                boolean continueProcessing = true;
                for( int i = 0; continueProcessing && (i < size); i++ ) {
                    try {
                        clientInterceptors[i].send_request( info );
                    }
                    catch( ForwardRequest e ) {
                        flowStackIndex = i;
                        info.setForwardRequest( e );
                        info.setEndingPointCall(
                            ClientRequestInfoImpl.CALL_RECEIVE_OTHER );
                        info.setReplyStatus( LOCATION_FORWARD.value );
                        updateClientRequestDispatcherForward( info );
                        continueProcessing = false;
                    }
                    catch( SystemException e ) {
                        flowStackIndex = i;
                        info.setEndingPointCall(
                            ClientRequestInfoImpl.CALL_RECEIVE_EXCEPTION );
                        info.setReplyStatus( SYSTEM_EXCEPTION.value );
                        info.setException( e );
                        continueProcessing = false;
                    }
                }
                info.setFlowStackIndex( flowStackIndex );
            }
            finally {
                current.resetSlotTable( );
            }
        } 
    }
    void invokeClientInterceptorEndingPoint( ClientRequestInfoImpl info ) {
        if( enabled ) {
            try {
                info.setCurrentExecutionPoint( info.EXECUTION_POINT_ENDING );
                ClientRequestInterceptor[] clientInterceptors =
                    (ClientRequestInterceptor[])interceptorList.
                    getInterceptors( InterceptorList.INTERCEPTOR_TYPE_CLIENT );
                int flowStackIndex = info.getFlowStackIndex();
                int endingPointCall = info.getEndingPointCall();
                if( ( endingPointCall ==
                      ClientRequestInfoImpl.CALL_RECEIVE_REPLY ) &&
                    info.getIsOneWay() )
                {
                    endingPointCall = ClientRequestInfoImpl.CALL_RECEIVE_OTHER;
                    info.setEndingPointCall( endingPointCall );
                }
                for( int i = (flowStackIndex - 1); i >= 0; i-- ) {
                    try {
                        switch( endingPointCall ) {
                        case ClientRequestInfoImpl.CALL_RECEIVE_REPLY:
                            clientInterceptors[i].receive_reply( info );
                            break;
                        case ClientRequestInfoImpl.CALL_RECEIVE_EXCEPTION:
                            clientInterceptors[i].receive_exception( info );
                            break;
                        case ClientRequestInfoImpl.CALL_RECEIVE_OTHER:
                            clientInterceptors[i].receive_other( info );
                            break;
                        }
                    }
                    catch( ForwardRequest e ) {
                        endingPointCall =
                            ClientRequestInfoImpl.CALL_RECEIVE_OTHER;
                        info.setEndingPointCall( endingPointCall );
                        info.setReplyStatus( LOCATION_FORWARD.value );
                        info.setForwardRequest( e );
                        updateClientRequestDispatcherForward( info );
                    }
                    catch( SystemException e ) {
                        endingPointCall =
                            ClientRequestInfoImpl.CALL_RECEIVE_EXCEPTION;
                        info.setEndingPointCall( endingPointCall );
                        info.setReplyStatus( SYSTEM_EXCEPTION.value );
                        info.setException( e );
                    }
                }
            }
            finally {
                if (info != null && info.isPICurrentPushed()) {
                    current.popSlotTable( );
                }
            }
        } 
    }
    void invokeServerInterceptorStartingPoint( ServerRequestInfoImpl info ) {
        if( enabled ) {
            try {
                current.pushSlotTable();
                info.setSlotTable(current.getSlotTable());
                current.pushSlotTable( );
                info.setCurrentExecutionPoint( info.EXECUTION_POINT_STARTING );
                ServerRequestInterceptor[] serverInterceptors =
                    (ServerRequestInterceptor[])interceptorList.
                    getInterceptors( InterceptorList.INTERCEPTOR_TYPE_SERVER );
                int size = serverInterceptors.length;
                int flowStackIndex = size;
                boolean continueProcessing = true;
                for( int i = 0; continueProcessing && (i < size); i++ ) {
                    try {
                        serverInterceptors[i].
                            receive_request_service_contexts( info );
                    }
                    catch( ForwardRequest e ) {
                        flowStackIndex = i;
                        info.setForwardRequest( e );
                        info.setIntermediatePointCall(
                            ServerRequestInfoImpl.CALL_INTERMEDIATE_NONE );
                        info.setEndingPointCall(
                            ServerRequestInfoImpl.CALL_SEND_OTHER );
                        info.setReplyStatus( LOCATION_FORWARD.value );
                        continueProcessing = false;
                    }
                    catch( SystemException e ) {
                        flowStackIndex = i;
                        info.setException( e );
                        info.setIntermediatePointCall(
                            ServerRequestInfoImpl.CALL_INTERMEDIATE_NONE );
                        info.setEndingPointCall(
                            ServerRequestInfoImpl.CALL_SEND_EXCEPTION );
                        info.setReplyStatus( SYSTEM_EXCEPTION.value );
                        continueProcessing = false;
                    }
                }
                info.setFlowStackIndex( flowStackIndex );
            }
            finally {
                current.popSlotTable( );
            }
        } 
    }
    void invokeServerInterceptorIntermediatePoint(
        ServerRequestInfoImpl info )
    {
        int intermediatePointCall = info.getIntermediatePointCall();
        if( enabled && ( intermediatePointCall !=
                         ServerRequestInfoImpl.CALL_INTERMEDIATE_NONE ) )
        {
            info.setCurrentExecutionPoint( info.EXECUTION_POINT_INTERMEDIATE );
            ServerRequestInterceptor[] serverInterceptors =
                (ServerRequestInterceptor[])
                interceptorList.getInterceptors(
                InterceptorList.INTERCEPTOR_TYPE_SERVER );
            int size = serverInterceptors.length;
            for( int i = 0; i < size; i++ ) {
                try {
                    serverInterceptors[i].receive_request( info );
                }
                catch( ForwardRequest e ) {
                    info.setForwardRequest( e );
                    info.setEndingPointCall(
                        ServerRequestInfoImpl.CALL_SEND_OTHER );
                    info.setReplyStatus( LOCATION_FORWARD.value );
                    break;
                }
                catch( SystemException e ) {
                    info.setException( e );
                    info.setEndingPointCall(
                        ServerRequestInfoImpl.CALL_SEND_EXCEPTION );
                    info.setReplyStatus( SYSTEM_EXCEPTION.value );
                    break;
                }
            }
        } 
    }
    void invokeServerInterceptorEndingPoint( ServerRequestInfoImpl info ) {
        if( enabled ) {
            try {
                ServerRequestInterceptor[] serverInterceptors =
                    (ServerRequestInterceptor[])interceptorList.
                    getInterceptors( InterceptorList.INTERCEPTOR_TYPE_SERVER );
                int flowStackIndex = info.getFlowStackIndex();
                int endingPointCall = info.getEndingPointCall();
                for( int i = (flowStackIndex - 1); i >= 0; i-- ) {
                    try {
                        switch( endingPointCall ) {
                        case ServerRequestInfoImpl.CALL_SEND_REPLY:
                            serverInterceptors[i].send_reply( info );
                            break;
                        case ServerRequestInfoImpl.CALL_SEND_EXCEPTION:
                            serverInterceptors[i].send_exception( info );
                            break;
                        case ServerRequestInfoImpl.CALL_SEND_OTHER:
                            serverInterceptors[i].send_other( info );
                            break;
                        }
                    }
                    catch( ForwardRequest e ) {
                        endingPointCall =
                            ServerRequestInfoImpl.CALL_SEND_OTHER;
                        info.setEndingPointCall( endingPointCall );
                        info.setForwardRequest( e );
                        info.setReplyStatus( LOCATION_FORWARD.value );
                        info.setForwardRequestRaisedInEnding();
                    }
                    catch( SystemException e ) {
                        endingPointCall =
                            ServerRequestInfoImpl.CALL_SEND_EXCEPTION;
                        info.setEndingPointCall( endingPointCall );
                        info.setException( e );
                        info.setReplyStatus( SYSTEM_EXCEPTION.value );
                    }
                }
                info.setAlreadyExecuted( true );
            }
            finally {
                current.popSlotTable();
            }
        } 
    }
    private void updateClientRequestDispatcherForward(
        ClientRequestInfoImpl info )
    {
        ForwardRequest forwardRequest = info.getForwardRequestException();
        if( forwardRequest != null ) {
            org.omg.CORBA.Object object = forwardRequest.forward;
            IOR ior = ORBUtility.getIOR( object ) ;
            info.setLocatedIOR( ior );
        }
    }
}
