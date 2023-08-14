public class CorbaClientRequestDispatcherImpl
    implements
        ClientRequestDispatcher
{
    public OutputObject beginRequest(Object self, String opName,
                                     boolean isOneWay, ContactInfo contactInfo)
    {
      ORB orb = null;
      try {
        CorbaContactInfo corbaContactInfo = (CorbaContactInfo) contactInfo;
        orb =  (ORB)contactInfo.getBroker();
        if (orb.subcontractDebugFlag) {
            dprint(".beginRequest->: op/" + opName);
        }
        orb.getPIHandler().initiateClientPIRequest( false );
        CorbaConnection connection = null;
        synchronized (contactInfo) {
            if (contactInfo.isConnectionBased()) {
                if (contactInfo.shouldCacheConnection()) {
                    connection = (CorbaConnection)
                        orb.getTransportManager()
                        .getOutboundConnectionCache(contactInfo).get(contactInfo);
                }
                if (connection != null) {
                    if (orb.subcontractDebugFlag) {
                        dprint(".beginRequest: op/" + opName
                               + ": Using cached connection: " + connection);
                    }
                } else {
                    try {
                        connection = (CorbaConnection)
                            contactInfo.createConnection();
                        if (orb.subcontractDebugFlag) {
                            dprint(".beginRequest: op/" + opName
                                   + ": Using created connection: " + connection);
                        }
                    } catch (RuntimeException e) {
                        if (orb.subcontractDebugFlag) {
                            dprint(".beginRequest: op/" + opName
                                   + ": failed to create connection: " + e);
                        }
                        boolean retry = getContactInfoListIterator(orb)
                                           .reportException(contactInfo, e);
                        if (retry) {
                            if(getContactInfoListIterator(orb).hasNext()) {
                                contactInfo = (ContactInfo)
                                   getContactInfoListIterator(orb).next();
                                unregisterWaiter(orb);
                                return beginRequest(self, opName,
                                                    isOneWay, contactInfo);
                            } else {
                                throw e;
                            }
                        } else {
                            throw e;
                        }
                    }
                    if (connection.shouldRegisterReadEvent()) {
                        orb.getTransportManager().getSelector(0)
                            .registerForEvent(connection.getEventHandler());
                        connection.setState("ESTABLISHED");
                    }
                    if (contactInfo.shouldCacheConnection()) {
                        OutboundConnectionCache connectionCache =
                         orb.getTransportManager()
                            .getOutboundConnectionCache(contactInfo);
                        connectionCache.stampTime(connection);
                        connectionCache.put(contactInfo, connection);
                    }
                }
            }
        }
        CorbaMessageMediator messageMediator = (CorbaMessageMediator)
            contactInfo.createMessageMediator(
                orb, contactInfo, connection, opName, isOneWay);
        if (orb.subcontractDebugFlag) {
            dprint(".beginRequest: " + opAndId(messageMediator)
                   + ": created message mediator: " +  messageMediator);
        }
        orb.getInvocationInfo().setMessageMediator(messageMediator);
        if (connection != null && connection.getCodeSetContext() == null) {
            performCodeSetNegotiation(messageMediator);
        }
        addServiceContexts(messageMediator);
        OutputObject outputObject =
            contactInfo.createOutputObject(messageMediator);
        if (orb.subcontractDebugFlag) {
            dprint(".beginRequest: " + opAndId(messageMediator)
                   + ": created output object: " + outputObject);
        }
        registerWaiter(messageMediator);
        synchronized (contactInfo) {
            if (contactInfo.isConnectionBased()) {
                if (contactInfo.shouldCacheConnection()) {
                    OutboundConnectionCache connectionCache =
                             orb.getTransportManager()
                                .getOutboundConnectionCache(contactInfo);
                    connectionCache.reclaim();
                }
            }
        }
        orb.getPIHandler().setClientPIInfo(messageMediator);
        try {
            orb.getPIHandler().invokeClientPIStartingPoint();
        } catch( RemarshalException e ) {
            if (orb.subcontractDebugFlag) {
                dprint(".beginRequest: " + opAndId(messageMediator)
                       + ": Remarshal");
            }
            if (getContactInfoListIterator(orb).hasNext()) {
                contactInfo = (ContactInfo)getContactInfoListIterator(orb).next();
                if (orb.subcontractDebugFlag) {
                    dprint( "RemarshalException: hasNext true\ncontact info " + contactInfo );
                }
                orb.getPIHandler().makeCompletedClientRequest(
                    ReplyMessage.LOCATION_FORWARD, null ) ;
                unregisterWaiter(orb);
                orb.getPIHandler().cleanupClientPIRequest() ;
                return beginRequest(self, opName, isOneWay, contactInfo);
            } else {
                if (orb.subcontractDebugFlag) {
                    dprint( "RemarshalException: hasNext false" );
                }
                ORBUtilSystemException wrapper =
                    ORBUtilSystemException.get(orb,
                                               CORBALogDomains.RPC_PROTOCOL);
                throw wrapper.remarshalWithNowhereToGo();
            }
        }
        messageMediator.initializeMessage();
        if (orb.subcontractDebugFlag) {
            dprint(".beginRequest: " + opAndId(messageMediator)
                   + ": initialized message");
        }
        return outputObject;
      } finally {
        if (orb.subcontractDebugFlag) {
            dprint(".beginRequest<-: op/" + opName);
        }
      }
    }
    public InputObject marshalingComplete(java.lang.Object self,
                                          OutputObject outputObject)
        throws
            ApplicationException,
            org.omg.CORBA.portable.RemarshalException
    {
        ORB orb = null;
        CorbaMessageMediator messageMediator = null;
        try {
            messageMediator = (CorbaMessageMediator)
                outputObject.getMessageMediator();
            orb = (ORB) messageMediator.getBroker();
            if (orb.subcontractDebugFlag) {
                dprint(".marshalingComplete->: " + opAndId(messageMediator));
            }
            InputObject inputObject =
                marshalingComplete1(orb, messageMediator);
            return processResponse(orb, messageMediator, inputObject);
        } finally {
            if (orb.subcontractDebugFlag) {
                dprint(".marshalingComplete<-: " + opAndId(messageMediator));
            }
        }
    }
    public InputObject marshalingComplete1(
            ORB orb, CorbaMessageMediator messageMediator)
        throws
            ApplicationException,
            org.omg.CORBA.portable.RemarshalException
    {
        try {
            messageMediator.finishSendingRequest();
            if (orb.subcontractDebugFlag) {
                dprint(".marshalingComplete: " + opAndId(messageMediator)
                       + ": finished sending request");
            }
            return messageMediator.waitForResponse();
        } catch (RuntimeException e) {
            if (orb.subcontractDebugFlag) {
                dprint(".marshalingComplete: " + opAndId(messageMediator)
                       + ": exception: " + e.toString());
            }
            boolean retry  =
                getContactInfoListIterator(orb)
                    .reportException(messageMediator.getContactInfo(), e);
            Exception newException =
                    orb.getPIHandler().invokeClientPIEndingPoint(
                    ReplyMessage.SYSTEM_EXCEPTION, e);
            if (retry) {
                if (newException == e) {
                    continueOrThrowSystemOrRemarshal(messageMediator,
                                                     new RemarshalException());
                } else {
                    continueOrThrowSystemOrRemarshal(messageMediator,
                                                     newException);
                }
            } else {
                if (newException instanceof RuntimeException){
                    throw (RuntimeException)newException;
                }
                else if (newException instanceof RemarshalException)
                {
                    throw (RemarshalException)newException;
                }
                throw e;
            }
            return null; 
        }
    }
    protected InputObject processResponse(ORB orb,
                                          CorbaMessageMediator messageMediator,
                                          InputObject inputObject)
        throws
            ApplicationException,
            org.omg.CORBA.portable.RemarshalException
    {
        ORBUtilSystemException wrapper =
            ORBUtilSystemException.get( orb,
                CORBALogDomains.RPC_PROTOCOL ) ;
        if (orb.subcontractDebugFlag) {
            dprint(".processResponse: " + opAndId(messageMediator)
                   + ": response received");
        }
        if (messageMediator.getConnection() != null) {
            ((CorbaConnection)messageMediator.getConnection())
                .setPostInitialContexts();
        }
        Exception exception = null;
        if (messageMediator.isOneWay()) {
            getContactInfoListIterator(orb)
                .reportSuccess(messageMediator.getContactInfo());
            exception = orb.getPIHandler().invokeClientPIEndingPoint(
                ReplyMessage.NO_EXCEPTION, exception );
            continueOrThrowSystemOrRemarshal(messageMediator, exception);
            return null;
        }
        consumeServiceContexts(orb, messageMediator);
        ((CDRInputObject)inputObject).performORBVersionSpecificInit();
        if (messageMediator.isSystemExceptionReply()) {
            SystemException se = messageMediator.getSystemExceptionReply();
            if (orb.subcontractDebugFlag) {
                dprint(".processResponse: " + opAndId(messageMediator)
                       + ": received system exception: " + se);
            }
            boolean doRemarshal =
                getContactInfoListIterator(orb)
                    .reportException(messageMediator.getContactInfo(), se);
            if (doRemarshal) {
                exception = orb.getPIHandler().invokeClientPIEndingPoint(
                    ReplyMessage.SYSTEM_EXCEPTION, se );
                if( se == exception ) {
                    exception = null;
                    continueOrThrowSystemOrRemarshal(messageMediator,
                                                     new RemarshalException());
                    throw wrapper.statementNotReachable1() ;
                } else {
                    continueOrThrowSystemOrRemarshal(messageMediator,
                                                     exception);
                    throw wrapper.statementNotReachable2() ;
                }
            }
            ServiceContexts contexts =
                messageMediator.getReplyServiceContexts();
            if (contexts != null) {
                UEInfoServiceContext usc =
                    (UEInfoServiceContext)
                    contexts.get(UEInfoServiceContext.SERVICE_CONTEXT_ID);
                if (usc != null) {
                    Throwable unknown = usc.getUE() ;
                    UnknownException ue = new UnknownException(unknown);
                    exception = orb.getPIHandler().invokeClientPIEndingPoint(
                        ReplyMessage.SYSTEM_EXCEPTION, ue );
                    continueOrThrowSystemOrRemarshal(messageMediator, exception);
                    throw wrapper.statementNotReachable3() ;
                }
            }
            exception = orb.getPIHandler().invokeClientPIEndingPoint(
                ReplyMessage.SYSTEM_EXCEPTION, se );
            continueOrThrowSystemOrRemarshal(messageMediator, exception);
            throw wrapper.statementNotReachable4() ;
        } else if (messageMediator.isUserExceptionReply()) {
            if (orb.subcontractDebugFlag) {
                dprint(".processResponse: " + opAndId(messageMediator)
                       + ": received user exception");
            }
            getContactInfoListIterator(orb)
                .reportSuccess(messageMediator.getContactInfo());
            String exceptionRepoId = peekUserExceptionId(inputObject);
            Exception newException = null;
            if (messageMediator.isDIIRequest()) {
                exception = messageMediator.unmarshalDIIUserException(
                                exceptionRepoId, (InputStream)inputObject);
                newException = orb.getPIHandler().invokeClientPIEndingPoint(
                                   ReplyMessage.USER_EXCEPTION, exception );
                messageMediator.setDIIException(newException);
            } else {
                ApplicationException appException =
                    new ApplicationException(
                        exceptionRepoId,
                        (org.omg.CORBA.portable.InputStream)inputObject);
                exception = appException;
                newException = orb.getPIHandler().invokeClientPIEndingPoint(
                                   ReplyMessage.USER_EXCEPTION, appException );
            }
            if (newException != exception) {
                continueOrThrowSystemOrRemarshal(messageMediator,newException);
            }
            if (newException instanceof ApplicationException) {
                throw (ApplicationException)newException;
            }
            return inputObject;
        } else if (messageMediator.isLocationForwardReply()) {
            if (orb.subcontractDebugFlag) {
                dprint(".processResponse: " + opAndId(messageMediator)
                       + ": received location forward");
            }
            getContactInfoListIterator(orb).reportRedirect(
                (CorbaContactInfo)messageMediator.getContactInfo(),
                messageMediator.getForwardedIOR());
            Exception newException = orb.getPIHandler().invokeClientPIEndingPoint(
                ReplyMessage.LOCATION_FORWARD, null );
            if( !(newException instanceof RemarshalException) ) {
                exception = newException;
            }
            if( exception != null ) {
                continueOrThrowSystemOrRemarshal(messageMediator, exception);
            }
            continueOrThrowSystemOrRemarshal(messageMediator,
                                             new RemarshalException());
            throw wrapper.statementNotReachable5() ;
        } else if (messageMediator.isDifferentAddrDispositionRequestedReply()){
            if (orb.subcontractDebugFlag) {
                dprint(".processResponse: " + opAndId(messageMediator)
                       + ": received different addressing dispostion request");
            }
            getContactInfoListIterator(orb).reportAddrDispositionRetry(
                (CorbaContactInfo)messageMediator.getContactInfo(),
                messageMediator.getAddrDispositionReply());
            Exception newException = orb.getPIHandler().invokeClientPIEndingPoint(
                ReplyMessage.NEEDS_ADDRESSING_MODE, null);
            if( !(newException instanceof RemarshalException) ) {
                exception = newException;
            }
            if( exception != null ) {
                continueOrThrowSystemOrRemarshal(messageMediator, exception);
            }
            continueOrThrowSystemOrRemarshal(messageMediator,
                                             new RemarshalException());
            throw wrapper.statementNotReachable6() ;
        } else  {
            if (orb.subcontractDebugFlag) {
                dprint(".processResponse: " + opAndId(messageMediator)
                       + ": received normal response");
            }
            getContactInfoListIterator(orb)
                .reportSuccess(messageMediator.getContactInfo());
            messageMediator.handleDIIReply((InputStream)inputObject);
            exception = orb.getPIHandler().invokeClientPIEndingPoint(
                ReplyMessage.NO_EXCEPTION, null );
            continueOrThrowSystemOrRemarshal(messageMediator, exception);
            return inputObject;
        }
    }
    protected void continueOrThrowSystemOrRemarshal(
        CorbaMessageMediator messageMediator, Exception exception)
        throws
            SystemException, RemarshalException
    {
        ORB orb = (ORB) messageMediator.getBroker();
        if( exception == null ) {
        } else if( exception instanceof RemarshalException ) {
            orb.getInvocationInfo().setIsRetryInvocation(true);
            unregisterWaiter(orb);
            if (orb.subcontractDebugFlag) {
                dprint(".continueOrThrowSystemOrRemarshal: "
                       + opAndId(messageMediator)
                       + ": throwing Remarshal");
            }
            throw (RemarshalException)exception;
        } else {
            if (orb.subcontractDebugFlag) {
                dprint(".continueOrThrowSystemOrRemarshal: "
                       + opAndId(messageMediator)
                       + ": throwing sex:"
                       + exception);
            }
            throw (SystemException)exception;
        }
    }
    protected CorbaContactInfoListIterator  getContactInfoListIterator(ORB orb)
    {
        return (CorbaContactInfoListIterator)
            ((CorbaInvocationInfo)orb.getInvocationInfo())
                .getContactInfoListIterator();
    }
    protected void registerWaiter(CorbaMessageMediator messageMediator)
    {
        if (messageMediator.getConnection() != null) {
            messageMediator.getConnection().registerWaiter(messageMediator);
        }
    }
    protected void unregisterWaiter(ORB orb)
    {
        MessageMediator messageMediator =
            orb.getInvocationInfo().getMessageMediator();
        if (messageMediator!=null && messageMediator.getConnection() != null) {
            messageMediator.getConnection().unregisterWaiter(messageMediator);
        }
    }
    protected void addServiceContexts(CorbaMessageMediator messageMediator)
    {
        ORB orb = (ORB)messageMediator.getBroker();
        CorbaConnection c = (CorbaConnection) messageMediator.getConnection();
        GIOPVersion giopVersion = messageMediator.getGIOPVersion();
        ServiceContexts contexts = messageMediator.getRequestServiceContexts();
        addCodeSetServiceContext(c, contexts, giopVersion);
        contexts.put(MaxStreamFormatVersionServiceContext.singleton);
        ORBVersionServiceContext ovsc = new ORBVersionServiceContext(
                        ORBVersionFactory.getORBVersion() ) ;
        contexts.put( ovsc ) ;
        if ((c != null) && !c.isPostInitialContexts()) {
            SendingContextServiceContext scsc =
                new SendingContextServiceContext( orb.getFVDCodeBaseIOR() ) ; 
            contexts.put( scsc ) ;
        }
    }
    protected void consumeServiceContexts(ORB orb,
                                        CorbaMessageMediator messageMediator)
    {
        ServiceContexts ctxts = messageMediator.getReplyServiceContexts();
        ServiceContext sc ;
        ORBUtilSystemException wrapper = ORBUtilSystemException.get( orb,
                CORBALogDomains.RPC_PROTOCOL ) ;
        if (ctxts == null) {
            return; 
        }
        sc = ctxts.get( SendingContextServiceContext.SERVICE_CONTEXT_ID ) ;
        if (sc != null) {
            SendingContextServiceContext scsc =
                (SendingContextServiceContext)sc ;
            IOR ior = scsc.getIOR() ;
            try {
                if (messageMediator.getConnection() != null) {
                    ((CorbaConnection)messageMediator.getConnection()).setCodeBaseIOR(ior);
                }
            } catch (ThreadDeath td) {
                throw td ;
            } catch (Throwable t) {
                throw wrapper.badStringifiedIor( t ) ;
            }
        }
        sc = ctxts.get( ORBVersionServiceContext.SERVICE_CONTEXT_ID ) ;
        if (sc != null) {
            ORBVersionServiceContext ovsc =
               (ORBVersionServiceContext) sc;
            ORBVersion version = ovsc.getVersion();
            orb.setORBVersion( version ) ;
        }
        getExceptionDetailMessage(messageMediator, wrapper);
    }
    protected void getExceptionDetailMessage(
        CorbaMessageMediator  messageMediator,
        ORBUtilSystemException wrapper)
    {
        ServiceContext sc = messageMediator.getReplyServiceContexts()
            .get(ExceptionDetailMessage.value);
        if (sc == null)
            return ;
        if (! (sc instanceof UnknownServiceContext)) {
            throw wrapper.badExceptionDetailMessageServiceContextType();
        }
        byte[] data = ((UnknownServiceContext)sc).getData();
        EncapsInputStream in =
            new EncapsInputStream((ORB)messageMediator.getBroker(),
                                  data, data.length);
        in.consumeEndian();
        String msg =
              "----------BEGIN server-side stack trace----------\n"
            + in.read_wstring() + "\n"
            + "----------END server-side stack trace----------";
        messageMediator.setReplyExceptionDetailMessage(msg);
    }
    public void endRequest(Broker broker, Object self, InputObject inputObject)
    {
        ORB orb = (ORB)broker ;
        try {
            if (orb.subcontractDebugFlag) {
                dprint(".endRequest->");
            }
            MessageMediator messageMediator =
                orb.getInvocationInfo().getMessageMediator();
            if (messageMediator != null)
            {
                if (messageMediator.getConnection() != null)
                {
                    ((CorbaMessageMediator)messageMediator)
                              .sendCancelRequestIfFinalFragmentNotSent();
                }
                InputObject inputObj = messageMediator.getInputObject();
                if (inputObj != null) {
                    inputObj.close();
                }
                OutputObject outputObj = messageMediator.getOutputObject();
                if (outputObj != null) {
                    outputObj.close();
                }
            }
            unregisterWaiter(orb);
            orb.getPIHandler().cleanupClientPIRequest();
        } catch (IOException ex) {
            if (orb.subcontractDebugFlag)
            {
                dprint(".endRequest: ignoring IOException - " + ex.toString());
            }
        } finally {
            if (orb.subcontractDebugFlag) {
                dprint(".endRequest<-");
            }
        }
    }
    protected void performCodeSetNegotiation(CorbaMessageMediator messageMediator)
    {
        CorbaConnection conn =
            (CorbaConnection) messageMediator.getConnection();
        IOR ior =
            ((CorbaContactInfo)messageMediator.getContactInfo())
            .getEffectiveTargetIOR();
        GIOPVersion giopVersion = messageMediator.getGIOPVersion();
        if (conn != null &&
            conn.getCodeSetContext() == null &&
            !giopVersion.equals(GIOPVersion.V1_0)) {
            synchronized(conn) {
                if (conn.getCodeSetContext() != null)
                    return;
                IIOPProfileTemplate temp =
                    (IIOPProfileTemplate)ior.getProfile().
                    getTaggedProfileTemplate();
                Iterator iter = temp.iteratorById(TAG_CODE_SETS.value);
                if (!iter.hasNext()) {
                    return;
                }
                CodeSetComponentInfo serverCodeSets
                    = ((CodeSetsComponent)iter.next()).getCodeSetComponentInfo();
                CodeSetComponentInfo.CodeSetContext result
                    = CodeSetConversion.impl().negotiate(
                          conn.getBroker().getORBData().getCodeSetComponentInfo(),
                          serverCodeSets);
                conn.setCodeSetContext(result);
            }
        }
    }
    protected void addCodeSetServiceContext(CorbaConnection conn,
                                          ServiceContexts ctxs,
                                          GIOPVersion giopVersion) {
        if (giopVersion.equals(GIOPVersion.V1_0) || conn == null)
            return;
        CodeSetComponentInfo.CodeSetContext codeSetCtx = null;
        if (conn.getBroker().getORBData().alwaysSendCodeSetServiceContext() ||
            !conn.isPostInitialContexts()) {
            codeSetCtx = conn.getCodeSetContext();
        }
        if (codeSetCtx == null)
            return;
        CodeSetServiceContext cssc = new CodeSetServiceContext(codeSetCtx);
        ctxs.put(cssc);
    }
    protected String peekUserExceptionId(InputObject inputObject)
    {
        CDRInputObject cdrInputObject = (CDRInputObject) inputObject;
        cdrInputObject.mark(Integer.MAX_VALUE);
        String result = cdrInputObject.read_string();
        cdrInputObject.reset();
        return result;
    }
    protected void dprint(String msg)
    {
        ORBUtility.dprint("CorbaClientRequestDispatcherImpl", msg);
    }
    protected String opAndId(CorbaMessageMediator mediator)
    {
        return ORBUtility.operationNameAndRequestId(mediator);
    }
}
