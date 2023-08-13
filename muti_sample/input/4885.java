public class CorbaMessageMediatorImpl
    implements
        CorbaMessageMediator,
        CorbaProtocolHandler,
        MessageHandler
{
    protected ORB orb;
    protected ORBUtilSystemException wrapper ;
    protected InterceptorsSystemException interceptorWrapper ;
    protected CorbaContactInfo contactInfo;
    protected CorbaConnection connection;
    protected short addrDisposition;
    protected CDROutputObject outputObject;
    protected CDRInputObject inputObject;
    protected Message messageHeader;
    protected RequestMessage requestHeader;
    protected LocateReplyOrReplyMessage replyHeader;
    protected String replyExceptionDetailMessage;
    protected IOR replyIOR;
    protected Integer requestIdInteger;
    protected Message dispatchHeader;
    protected ByteBuffer dispatchByteBuffer;
    protected byte streamFormatVersion;
    protected boolean streamFormatVersionSet = false;
    protected org.omg.CORBA.Request diiRequest;
    protected boolean cancelRequestAlreadySent = false;
    protected ProtocolHandler protocolHandler;
    protected boolean _executeReturnServantInResponseConstructor = false;
    protected boolean _executeRemoveThreadInfoInResponseConstructor = false;
    protected boolean _executePIInResponseConstructor = false;
    public CorbaMessageMediatorImpl(ORB orb,
                                    ContactInfo contactInfo,
                                    Connection connection,
                                    GIOPVersion giopVersion,
                                    IOR ior,
                                    int requestId,
                                    short addrDisposition,
                                    String operationName,
                                    boolean isOneWay)
    {
        this( orb, connection ) ;
        this.contactInfo = (CorbaContactInfo) contactInfo;
        this.addrDisposition = addrDisposition;
        streamFormatVersion =
            getStreamFormatVersionForThisRequest(
                ((CorbaContactInfo)this.contactInfo).getEffectiveTargetIOR(),
                giopVersion);
        streamFormatVersionSet = true;
        requestHeader = (RequestMessage) MessageBase.createRequest(
            this.orb,
            giopVersion,
            ORBUtility.getEncodingVersion(orb, ior),
            requestId,
            !isOneWay,
            ((CorbaContactInfo)this.contactInfo).getEffectiveTargetIOR(),
            this.addrDisposition,
            operationName,
            new ServiceContexts(orb),
            null);
    }
    public CorbaMessageMediatorImpl(ORB orb,
                                    Connection connection)
    {
        this.orb = orb;
        this.connection = (CorbaConnection)connection;
        this.wrapper = ORBUtilSystemException.get( orb,
            CORBALogDomains.RPC_PROTOCOL ) ;
        this.interceptorWrapper = InterceptorsSystemException.get( orb,
            CORBALogDomains.RPC_PROTOCOL ) ;
    }
    public CorbaMessageMediatorImpl(ORB orb,
                                    CorbaConnection connection,
                                    Message dispatchHeader,
                                    ByteBuffer byteBuffer)
    {
        this( orb, connection ) ;
        this.dispatchHeader = dispatchHeader;
        this.dispatchByteBuffer = byteBuffer;
    }
    public Broker getBroker()
    {
        return orb;
    }
    public ContactInfo getContactInfo()
    {
        return contactInfo;
    }
    public Connection getConnection()
    {
        return connection;
    }
    public void initializeMessage()
    {
        getRequestHeader().write(outputObject);
    }
    public void finishSendingRequest()
    {
        outputObject.finishSendingMessage();
    }
    public InputObject waitForResponse()
    {
        if (getRequestHeader().isResponseExpected()) {
            return connection.waitForResponse(this);
        }
        return null;
    }
    public void setOutputObject(OutputObject outputObject)
    {
        this.outputObject = (CDROutputObject) outputObject;
    }
    public OutputObject getOutputObject()
    {
        return outputObject;
    }
    public void setInputObject(InputObject inputObject)
    {
        this.inputObject = (CDRInputObject) inputObject;
    }
    public InputObject getInputObject()
    {
        return inputObject;
    }
    public void setReplyHeader(LocateReplyOrReplyMessage header)
    {
        this.replyHeader = header;
        this.replyIOR = header.getIOR(); 
    }
    public LocateReplyMessage getLocateReplyHeader()
    {
        return (LocateReplyMessage) replyHeader;
    }
    public ReplyMessage getReplyHeader()
    {
        return (ReplyMessage) replyHeader;
    }
    public void setReplyExceptionDetailMessage(String message)
    {
        replyExceptionDetailMessage = message;
    }
    public RequestMessage getRequestHeader()
    {
        return requestHeader;
    }
    public GIOPVersion getGIOPVersion()
    {
        if (messageHeader != null) {
            return messageHeader.getGIOPVersion();
        }
        return getRequestHeader().getGIOPVersion();
    }
    public byte getEncodingVersion() {
        if (messageHeader != null) {
            return messageHeader.getEncodingVersion();
        }
        return getRequestHeader().getEncodingVersion();
    }
    public int getRequestId()
    {
        return getRequestHeader().getRequestId();
    }
    public Integer getRequestIdInteger()
    {
        if (requestIdInteger == null) {
            requestIdInteger = new Integer(getRequestHeader().getRequestId());
        }
        return requestIdInteger;
    }
    public boolean isOneWay()
    {
        return ! getRequestHeader().isResponseExpected();
    }
    public short getAddrDisposition()
    {
        return addrDisposition;
    }
    public String getOperationName()
    {
        return getRequestHeader().getOperation();
    }
    public ServiceContexts getRequestServiceContexts()
    {
        return getRequestHeader().getServiceContexts();
    }
    public ServiceContexts getReplyServiceContexts()
    {
        return getReplyHeader().getServiceContexts();
    }
    public void sendCancelRequestIfFinalFragmentNotSent()
    {
        if ((!sentFullMessage()) && sentFragment() &&
            (!cancelRequestAlreadySent))
        {
            try {
                if (orb.subcontractDebugFlag) {
                    dprint(".sendCancelRequestIfFinalFragmentNotSent->: "
                           + opAndId(this));
                }
                connection.sendCancelRequestWithLock(getGIOPVersion(),
                                                     getRequestId());
                cancelRequestAlreadySent = true;
            } catch (IOException e) {
                if (orb.subcontractDebugFlag) {
                    dprint(".sendCancelRequestIfFinalFragmentNotSent: !ERROR : " + opAndId(this),
                           e);
                }
                throw interceptorWrapper.ioexceptionDuringCancelRequest(
                    CompletionStatus.COMPLETED_MAYBE, e );
            } finally {
                if (orb.subcontractDebugFlag) {
                    dprint(".sendCancelRequestIfFinalFragmentNotSent<-: "
                           + opAndId(this));
                }
            }
        }
    }
    public boolean sentFullMessage()
    {
        return outputObject.getBufferManager().sentFullMessage();
    }
    public boolean sentFragment()
    {
        return outputObject.getBufferManager().sentFragment();
    }
    public void setDIIInfo(org.omg.CORBA.Request diiRequest)
    {
        this.diiRequest = diiRequest;
    }
    public boolean isDIIRequest()
    {
        return diiRequest != null;
    }
    public Exception unmarshalDIIUserException(String repoId, InputStream is)
    {
        if (! isDIIRequest()) {
            return null;
        }
        ExceptionList _exceptions = diiRequest.exceptions();
        try {
            for (int i=0; i<_exceptions.count() ; i++) {
                TypeCode tc = _exceptions.item(i);
                if ( tc.id().equals(repoId) ) {
                    Any eany = orb.create_any();
                    eany.read_value(is, (TypeCode)tc);
                    return new UnknownUserException(eany);
                }
            }
        } catch (Exception b) {
            throw wrapper.unexpectedDiiException(b);
        }
        return wrapper.unknownCorbaExc( CompletionStatus.COMPLETED_MAYBE);
    }
    public void setDIIException(Exception exception)
    {
        diiRequest.env().exception(exception);
    }
    public void handleDIIReply(InputStream inputStream)
    {
        if (! isDIIRequest()) {
            return;
        }
        ((RequestImpl)diiRequest).unmarshalReply(inputStream);
    }
    public Message getDispatchHeader()
    {
        return dispatchHeader;
    }
    public void setDispatchHeader(Message msg)
    {
        dispatchHeader = msg;
    }
    public ByteBuffer getDispatchBuffer()
    {
        return dispatchByteBuffer;
    }
    public void setDispatchBuffer(ByteBuffer byteBuffer)
    {
        dispatchByteBuffer = byteBuffer;
    }
    public int getThreadPoolToUse() {
        int poolToUse = 0;
        Message msg = getDispatchHeader();
        if (msg != null) {
            poolToUse = msg.getThreadPoolToUse();
        }
        return poolToUse;
    }
    public byte getStreamFormatVersion()
    {
        if (streamFormatVersionSet) {
            return streamFormatVersion;
        }
        return getStreamFormatVersionForReply();
    }
    public byte getStreamFormatVersionForReply() {
        ServiceContexts svc = getRequestServiceContexts();
        MaxStreamFormatVersionServiceContext msfvsc
            = (MaxStreamFormatVersionServiceContext)svc.get(
                MaxStreamFormatVersionServiceContext.SERVICE_CONTEXT_ID);
        if (msfvsc != null) {
            byte localMaxVersion = ORBUtility.getMaxStreamFormatVersion();
            byte remoteMaxVersion = msfvsc.getMaximumStreamFormatVersion();
            return (byte)Math.min(localMaxVersion, remoteMaxVersion);
        } else {
            if (getGIOPVersion().lessThan(GIOPVersion.V1_3))
                return ORBConstants.STREAM_FORMAT_VERSION_1;
            else
                return ORBConstants.STREAM_FORMAT_VERSION_2;
        }
    }
    public boolean isSystemExceptionReply()
    {
        return replyHeader.getReplyStatus() == ReplyMessage.SYSTEM_EXCEPTION;
    }
    public boolean isUserExceptionReply()
    {
        return replyHeader.getReplyStatus() == ReplyMessage.USER_EXCEPTION;
    }
    public boolean isLocationForwardReply()
    {
        return ( (replyHeader.getReplyStatus() == ReplyMessage.LOCATION_FORWARD) ||
                 (replyHeader.getReplyStatus() == ReplyMessage.LOCATION_FORWARD_PERM) );
    }
    public boolean isDifferentAddrDispositionRequestedReply()
    {
        return replyHeader.getReplyStatus() == ReplyMessage.NEEDS_ADDRESSING_MODE;
    }
    public short getAddrDispositionReply()
    {
        return replyHeader.getAddrDisposition();
    }
    public IOR getForwardedIOR()
    {
        return replyHeader.getIOR();
    }
    public SystemException getSystemExceptionReply()
    {
        return replyHeader.getSystemException(replyExceptionDetailMessage);
    }
    public ObjectKey getObjectKey()
    {
        return getRequestHeader().getObjectKey();
    }
    public void setProtocolHandler(CorbaProtocolHandler protocolHandler)
    {
        throw wrapper.methodShouldNotBeCalled() ;
    }
    public CorbaProtocolHandler getProtocolHandler()
    {
        return this;
    }
    public org.omg.CORBA.portable.OutputStream createReply()
    {
        getProtocolHandler().createResponse(this, (ServiceContexts) null);
        return (OutputStream) getOutputObject();
    }
    public org.omg.CORBA.portable.OutputStream createExceptionReply()
    {
        getProtocolHandler().createUserExceptionResponse(this, (ServiceContexts) null);
        return (OutputStream) getOutputObject();
    }
    public boolean executeReturnServantInResponseConstructor()
    {
        return _executeReturnServantInResponseConstructor;
    }
    public void setExecuteReturnServantInResponseConstructor(boolean b)
    {
        _executeReturnServantInResponseConstructor = b;
    }
    public boolean executeRemoveThreadInfoInResponseConstructor()
    {
        return _executeRemoveThreadInfoInResponseConstructor;
    }
    public void setExecuteRemoveThreadInfoInResponseConstructor(boolean b)
    {
        _executeRemoveThreadInfoInResponseConstructor = b;
    }
    public boolean executePIInResponseConstructor()
    {
        return _executePIInResponseConstructor;
    }
    public void setExecutePIInResponseConstructor( boolean b )
    {
        _executePIInResponseConstructor = b;
    }
    private byte getStreamFormatVersionForThisRequest(IOR ior,
                                                      GIOPVersion giopVersion)
    {
        byte localMaxVersion
            = ORBUtility.getMaxStreamFormatVersion();
        IOR effectiveTargetIOR =
            ((CorbaContactInfo)this.contactInfo).getEffectiveTargetIOR();
        IIOPProfileTemplate temp =
            (IIOPProfileTemplate)effectiveTargetIOR.getProfile().getTaggedProfileTemplate();
        Iterator iter = temp.iteratorById(TAG_RMI_CUSTOM_MAX_STREAM_FORMAT.value);
        if (!iter.hasNext()) {
            if (giopVersion.lessThan(GIOPVersion.V1_3))
                return ORBConstants.STREAM_FORMAT_VERSION_1;
            else
                return ORBConstants.STREAM_FORMAT_VERSION_2;
        }
        byte remoteMaxVersion
            = ((MaxStreamFormatVersionComponent)iter.next()).getMaxStreamFormatVersion();
        return (byte)Math.min(localMaxVersion, remoteMaxVersion);
    }
    protected boolean isThreadDone = false;
    public boolean handleRequest(MessageMediator messageMediator)
    {
        try {
            dispatchHeader.callback(this);
        } catch (IOException e) {
            ;
        }
        return isThreadDone;
    }
    private void setWorkThenPoolOrResumeSelect(Message header)
    {
        if (getConnection().getEventHandler().shouldUseSelectThreadToWait()) {
            resumeSelect(header);
        } else {
            isThreadDone = true;
            orb.getTransportManager().getSelector(0)
                .unregisterForEvent(getConnection().getEventHandler());
            orb.getTransportManager().getSelector(0)
                .registerForEvent(getConnection().getEventHandler());
        }
    }
    private void setWorkThenReadOrResumeSelect(Message header)
    {
        if (getConnection().getEventHandler().shouldUseSelectThreadToWait()) {
            resumeSelect(header);
        } else {
            isThreadDone = false;
        }
    }
    private void resumeSelect(Message header)
    {
        if (transportDebug()) {
            dprint(".resumeSelect:->");
            String requestId = "?";
            if (header instanceof RequestMessage) {
                requestId =
                    new Integer(((RequestMessage)header)
                                .getRequestId()).toString();
            } else if (header instanceof ReplyMessage) {
                requestId =
                    new Integer(((ReplyMessage)header)
                                .getRequestId()).toString();
            } else if (header instanceof FragmentMessage_1_2) {
                requestId =
                    new Integer(((FragmentMessage_1_2)header)
                                .getRequestId()).toString();
            }
            dprint(".resumeSelect: id/"
                   + requestId
                   + " " + getConnection()
                   );
        }
        EventHandler eventHandler = getConnection().getEventHandler();
        orb.getTransportManager().getSelector(0).registerInterestOps(eventHandler);
        if (transportDebug()) {
            dprint(".resumeSelect:<-");
        }
    }
    private void setInputObject()
    {
        if (getConnection().getContactInfo() != null) {
            inputObject = (CDRInputObject)
                getConnection().getContactInfo()
                .createInputObject(orb, this);
        } else if (getConnection().getAcceptor() != null) {
            inputObject = (CDRInputObject)
                getConnection().getAcceptor()
                .createInputObject(orb, this);
        } else {
            throw new RuntimeException("CorbaMessageMediatorImpl.setInputObject");
        }
        inputObject.setMessageMediator(this);
        setInputObject(inputObject);
    }
    private void signalResponseReceived()
    {
        connection.getResponseWaitingRoom()
            .responseReceived((InputObject)inputObject);
    }
    public void handleInput(Message header) throws IOException
    {
        try {
            messageHeader = header;
            if (transportDebug())
                dprint(".handleInput->: "
                       + MessageBase.typeToString(header.getType()));
            setWorkThenReadOrResumeSelect(header);
            switch(header.getType())
            {
            case Message.GIOPCloseConnection:
                if (transportDebug()) {
                    dprint(".handleInput: CloseConnection: purging");
                }
                connection.purgeCalls(wrapper.connectionRebind(), true, false);
                break;
            case Message.GIOPMessageError:
                if (transportDebug()) {
                    dprint(".handleInput: MessageError: purging");
                }
                connection.purgeCalls(wrapper.recvMsgError(), true, false);
                break;
            default:
                if (transportDebug()) {
                    dprint(".handleInput: ERROR: "
                           + MessageBase.typeToString(header.getType()));
                }
                throw wrapper.badGiopRequestType() ;
            }
            releaseByteBufferToPool();
        } finally {
            if (transportDebug()) {
                dprint(".handleInput<-: "
                       + MessageBase.typeToString(header.getType()));
            }
        }
    }
    public void handleInput(RequestMessage_1_0 header) throws IOException
    {
        try {
            if (transportDebug()) dprint(".REQUEST 1.0->: " + header);
            try {
                messageHeader = requestHeader = (RequestMessage) header;
                setInputObject();
            } finally {
                setWorkThenPoolOrResumeSelect(header);
            }
            getProtocolHandler().handleRequest(header, this);
        } catch (Throwable t) {
            if (transportDebug())
                dprint(".REQUEST 1.0: !!ERROR!!: " + header, t);
        } finally {
            if (transportDebug()) dprint(".REQUEST 1.0<-: " + header);
        }
    }
    public void handleInput(RequestMessage_1_1 header) throws IOException
    {
        try {
            if (transportDebug()) dprint(".REQUEST 1.1->: " + header);
            try {
                messageHeader = requestHeader = (RequestMessage) header;
                setInputObject();
                connection.serverRequest_1_1_Put(this);
            } finally {
                setWorkThenPoolOrResumeSelect(header);
            }
            getProtocolHandler().handleRequest(header, this);
        } catch (Throwable t) {
            if (transportDebug())
                dprint(".REQUEST 1.1: !!ERROR!!: " + header, t);
        } finally {
            if (transportDebug()) dprint(".REQUEST 1.1<-: " + header);
        }
    }
    public void handleInput(RequestMessage_1_2 header) throws IOException
    {
        try {
            try {
                messageHeader = requestHeader = (RequestMessage) header;
                header.unmarshalRequestID(dispatchByteBuffer);
                setInputObject();
                if (transportDebug()) dprint(".REQUEST 1.2->: id/"
                                             + header.getRequestId()
                                             + ": "
                                             + header);
                connection.serverRequestMapPut(header.getRequestId(), this);
            } finally {
                setWorkThenPoolOrResumeSelect(header);
            }
            getProtocolHandler().handleRequest(header, this);
        } catch (Throwable t) {
            if (transportDebug()) dprint(".REQUEST 1.2: id/"
                                         + header.getRequestId()
                                         + ": !!ERROR!!: "
                                         + header,
                                         t);
        } finally {
            connection.serverRequestMapRemove(header.getRequestId());
            if (transportDebug()) dprint(".REQUEST 1.2<-: id/"
                                         + header.getRequestId()
                                         + ": "
                                         + header);
        }
    }
    public void handleInput(ReplyMessage_1_0 header) throws IOException
    {
        try {
            try {
                if (transportDebug()) dprint(".REPLY 1.0->: " + header);
                messageHeader = replyHeader = (ReplyMessage) header;
                setInputObject();
                inputObject.unmarshalHeader();
                signalResponseReceived();
            } finally{
                setWorkThenReadOrResumeSelect(header);
            }
        } catch (Throwable t) {
            if (transportDebug())dprint(".REPLY 1.0: !!ERROR!!: " + header, t);
        } finally {
            if (transportDebug()) dprint(".REPLY 1.0<-: " + header);
        }
    }
    public void handleInput(ReplyMessage_1_1 header) throws IOException
    {
        try {
            if (transportDebug()) dprint(".REPLY 1.1->: " + header);
            messageHeader = replyHeader = (ReplyMessage) header;
            setInputObject();
            if (header.moreFragmentsToFollow()) {
                connection.clientReply_1_1_Put(this);
                setWorkThenPoolOrResumeSelect(header);
                inputObject.unmarshalHeader();
                signalResponseReceived();
            } else {
                inputObject.unmarshalHeader();
                signalResponseReceived();
                setWorkThenReadOrResumeSelect(header);
            }
        } catch (Throwable t) {
            if (transportDebug()) dprint(".REPLY 1.1: !!ERROR!!: " + header);
        } finally {
            if (transportDebug()) dprint(".REPLY 1.1<-: " + header);
        }
    }
    public void handleInput(ReplyMessage_1_2 header) throws IOException
    {
        try {
            try {
                messageHeader = replyHeader = (ReplyMessage) header;
                header.unmarshalRequestID(dispatchByteBuffer);
                if (transportDebug()) {
                    dprint(".REPLY 1.2->: id/"
                           + + header.getRequestId()
                           + ": more?: " + header.moreFragmentsToFollow()
                           + ": " + header);
                }
                setInputObject();
                signalResponseReceived();
            } finally {
                setWorkThenReadOrResumeSelect(header);
            }
        } catch (Throwable t) {
            if (transportDebug()) dprint(".REPLY 1.2: id/"
                                         + header.getRequestId()
                                         + ": !!ERROR!!: "
                                         + header, t);
        } finally {
            if (transportDebug()) dprint(".REPLY 1.2<-: id/"
                                         + header.getRequestId()
                                         + ": "
                                         + header);
        }
    }
    public void handleInput(LocateRequestMessage_1_0 header) throws IOException
    {
        try {
            if (transportDebug())
                dprint(".LOCATE_REQUEST 1.0->: " + header);
            try {
                messageHeader = header;
                setInputObject();
            } finally {
                setWorkThenPoolOrResumeSelect(header);
            }
            getProtocolHandler().handleRequest(header, this);
        } catch (Throwable t) {
            if (transportDebug())
                dprint(".LOCATE_REQUEST 1.0: !!ERROR!!: " + header, t);
        } finally {
            if (transportDebug())
                dprint(".LOCATE_REQUEST 1.0<-: " + header);
        }
    }
    public void handleInput(LocateRequestMessage_1_1 header) throws IOException
    {
        try {
            if (transportDebug())
                dprint(".LOCATE_REQUEST 1.1->: " + header);
            try {
                messageHeader = header;
                setInputObject();
            } finally {
                setWorkThenPoolOrResumeSelect(header);
            }
            getProtocolHandler().handleRequest(header, this);
        } catch (Throwable t) {
            if (transportDebug())
                dprint(".LOCATE_REQUEST 1.1: !!ERROR!!: " + header, t);
        } finally {
            if (transportDebug())
                dprint(".LOCATE_REQUEST 1.1<-:" + header);
        }
    }
    public void handleInput(LocateRequestMessage_1_2 header) throws IOException
    {
        try {
            try {
                messageHeader = header;
                header.unmarshalRequestID(dispatchByteBuffer);
                setInputObject();
                if (transportDebug())
                    dprint(".LOCATE_REQUEST 1.2->: id/"
                           + header.getRequestId()
                           + ": "
                           + header);
                if (header.moreFragmentsToFollow()) {
                    connection.serverRequestMapPut(header.getRequestId(),this);
                }
            } finally {
                setWorkThenPoolOrResumeSelect(header);
            }
            getProtocolHandler().handleRequest(header, this);
        } catch (Throwable t) {
            if (transportDebug())
                dprint(".LOCATE_REQUEST 1.2: id/"
                       + header.getRequestId()
                       + ": !!ERROR!!: "
                       + header, t);
        } finally {
            if (transportDebug())
                dprint(".LOCATE_REQUEST 1.2<-: id/"
                       + header.getRequestId()
                       + ": "
                       + header);
        }
    }
    public void handleInput(LocateReplyMessage_1_0 header) throws IOException
    {
        try {
            if (transportDebug())
                dprint(".LOCATE_REPLY 1.0->:" + header);
            try {
                messageHeader = header;
                setInputObject();
                inputObject.unmarshalHeader(); 
                signalResponseReceived();
            } finally {
                setWorkThenReadOrResumeSelect(header);
            }
        } catch (Throwable t) {
            if (transportDebug())
                dprint(".LOCATE_REPLY 1.0: !!ERROR!!: " + header, t);
        } finally {
            if (transportDebug())
                dprint(".LOCATE_REPLY 1.0<-: " + header);
        }
    }
    public void handleInput(LocateReplyMessage_1_1 header) throws IOException
    {
        try {
            if (transportDebug()) dprint(".LOCATE_REPLY 1.1->: " + header);
            try {
                messageHeader = header;
                setInputObject();
                inputObject.unmarshalHeader();
                signalResponseReceived();
            } finally {
                setWorkThenReadOrResumeSelect(header);
            }
        } catch (Throwable t) {
            if (transportDebug())
                dprint(".LOCATE_REPLY 1.1: !!ERROR!!: " + header, t);
        } finally {
            if (transportDebug()) dprint(".LOCATE_REPLY 1.1<-: " + header);
        }
    }
    public void handleInput(LocateReplyMessage_1_2 header) throws IOException
    {
        try {
            try {
                messageHeader = header;
                header.unmarshalRequestID(dispatchByteBuffer);
                setInputObject();
                if (transportDebug()) dprint(".LOCATE_REPLY 1.2->: id/"
                                             + header.getRequestId()
                                             + ": "
                                             + header);
                signalResponseReceived();
            } finally {
                setWorkThenPoolOrResumeSelect(header); 
            }
        } catch (Throwable t) {
            if (transportDebug())
                dprint(".LOCATE_REPLY 1.2: id/"
                       + header.getRequestId()
                       + ": !!ERROR!!: "
                       + header, t);
        } finally {
            if (transportDebug()) dprint(".LOCATE_REPLY 1.2<-: id/"
                                         + header.getRequestId()
                                         + ": "
                                         + header);
        }
    }
    public void handleInput(FragmentMessage_1_1 header) throws IOException
    {
        try {
            if (transportDebug()) {
                dprint(".FRAGMENT 1.1->: "
                       + "more?: " + header.moreFragmentsToFollow()
                       + ": " + header);
            }
            try {
                messageHeader = header;
                MessageMediator mediator = null;
                CDRInputObject inputObject = null;
                if (connection.isServer()) {
                    mediator = connection.serverRequest_1_1_Get();
                } else {
                    mediator = connection.clientReply_1_1_Get();
                }
                if (mediator != null) {
                    inputObject = (CDRInputObject) mediator.getInputObject();
                }
                if (inputObject == null) {
                    if (transportDebug())
                        dprint(".FRAGMENT 1.1: ++++DISCARDING++++: " + header);
                    releaseByteBufferToPool();
                    return;
                }
                inputObject.getBufferManager()
                    .processFragment(dispatchByteBuffer, header);
                if (! header.moreFragmentsToFollow()) {
                    if (connection.isServer()) {
                        connection.serverRequest_1_1_Remove();
                    } else {
                        connection.clientReply_1_1_Remove();
                    }
                }
            } finally {
                setWorkThenReadOrResumeSelect(header);
            }
        } catch (Throwable t) {
            if (transportDebug())
                dprint(".FRAGMENT 1.1: !!ERROR!!: " + header, t);
        } finally {
            if (transportDebug()) dprint(".FRAGMENT 1.1<-: " + header);
        }
    }
    public void handleInput(FragmentMessage_1_2 header) throws IOException
    {
        try {
            try {
                messageHeader = header;
                header.unmarshalRequestID(dispatchByteBuffer);
                if (transportDebug()) {
                    dprint(".FRAGMENT 1.2->: id/"
                           + header.getRequestId()
                           + ": more?: " + header.moreFragmentsToFollow()
                           + ": " + header);
                }
                MessageMediator mediator = null;
                InputObject inputObject = null;
                if (connection.isServer()) {
                    mediator =
                        connection.serverRequestMapGet(header.getRequestId());
                } else {
                    mediator =
                        connection.clientRequestMapGet(header.getRequestId());
                }
                if (mediator != null) {
                    inputObject = mediator.getInputObject();
                }
                if (inputObject == null) {
                    if (transportDebug()) {
                        dprint(".FRAGMENT 1.2: id/"
                               + header.getRequestId()
                               + ": ++++DISCARDING++++: "
                               + header);
                    }
                    releaseByteBufferToPool();
                    return;
                }
                ((CDRInputObject)inputObject)
                    .getBufferManager().processFragment(
                                     dispatchByteBuffer, header);
                if (! connection.isServer()) {
                }
            } finally {
                setWorkThenReadOrResumeSelect(header);
            }
        } catch (Throwable t) {
            if (transportDebug())
                dprint(".FRAGMENT 1.2: id/"
                       + header.getRequestId()
                       + ": !!ERROR!!: "
                       + header, t);
        } finally {
            if (transportDebug()) dprint(".FRAGMENT 1.2<-: id/"
                                         + header.getRequestId()
                                         + ": "
                                         + header);
        }
    }
    public void handleInput(CancelRequestMessage header) throws IOException
    {
        try {
            try {
                messageHeader = header;
                setInputObject();
                inputObject.unmarshalHeader();
                if (transportDebug()) dprint(".CANCEL->: id/"
                                             + header.getRequestId() + ": "
                                             + header.getGIOPVersion() + ": "
                                             + header);
                processCancelRequest(header.getRequestId());
                releaseByteBufferToPool();
            } finally {
                setWorkThenReadOrResumeSelect(header);
            }
        } catch (Throwable t) {
            if (transportDebug()) dprint(".CANCEL: id/"
                                         + header.getRequestId()
                                         + ": !!ERROR!!: "
                                         + header, t);
        } finally {
            if (transportDebug()) dprint(".CANCEL<-: id/"
                                         + header.getRequestId() + ": "
                                         + header.getGIOPVersion() + ": "
                                         + header);
        }
    }
    private void throwNotImplemented()
    {
        isThreadDone = false;
        throwNotImplemented("");
    }
    private void throwNotImplemented(String msg)
    {
        throw new RuntimeException("CorbaMessageMediatorImpl: not implemented " + msg);
    }
    private void dprint(String msg, Throwable t)
    {
        dprint(msg);
        t.printStackTrace(System.out);
    }
    private void dprint(String msg)
    {
        ORBUtility.dprint("CorbaMessageMediatorImpl", msg);
    }
    protected String opAndId(CorbaMessageMediator mediator)
    {
        return ORBUtility.operationNameAndRequestId(mediator);
    }
    private boolean transportDebug()
    {
        return orb.transportDebugFlag;
    }
    private final void processCancelRequest(int cancelReqId) {
        if (!connection.isServer()) {
            return; 
        }
        MessageMediator mediator = connection.serverRequestMapGet(cancelReqId);
        int requestId ;
        if (mediator == null) {
            mediator = connection.serverRequest_1_1_Get();
            if (mediator == null) {
                return; 
            }
            requestId = ((CorbaMessageMediator) mediator).getRequestId();
            if (requestId != cancelReqId) {
                return; 
            }
            if (requestId == 0) { 
                return; 
            }
        } else {
            requestId = ((CorbaMessageMediator) mediator).getRequestId();
        }
        Message msg = ((CorbaMessageMediator)mediator).getRequestHeader();
        if (msg.getType() != Message.GIOPRequest) {
            wrapper.badMessageTypeForCancel() ;
        }
        BufferManagerReadStream bufferManager = (BufferManagerReadStream)
            ((CDRInputObject)mediator.getInputObject()).getBufferManager();
        bufferManager.cancelProcessing(cancelReqId);
    }
    public void handleRequest(RequestMessage msg,
                              CorbaMessageMediator messageMediator)
    {
        try {
            beginRequest(messageMediator);
            try {
                handleRequestRequest(messageMediator);
                if (messageMediator.isOneWay()) {
                    return;
                }
            } catch (Throwable t) {
                if (messageMediator.isOneWay()) {
                    return;
                }
                handleThrowableDuringServerDispatch(
                    messageMediator, t, CompletionStatus.COMPLETED_MAYBE);
            }
            sendResponse(messageMediator);
        } catch (Throwable t) {
            dispatchError(messageMediator, "RequestMessage", t);
        } finally {
            endRequest(messageMediator);
        }
    }
    public void handleRequest(LocateRequestMessage msg,
                              CorbaMessageMediator messageMediator)
    {
        try {
            beginRequest(messageMediator);
            try {
                handleLocateRequest(messageMediator);
            } catch (Throwable t) {
                handleThrowableDuringServerDispatch(
                    messageMediator, t, CompletionStatus.COMPLETED_MAYBE);
            }
            sendResponse(messageMediator);
        } catch (Throwable t) {
            dispatchError(messageMediator, "LocateRequestMessage", t);
        } finally {
            endRequest(messageMediator);
        }
    }
    private void beginRequest(CorbaMessageMediator messageMediator)
    {
        ORB orb = (ORB) messageMediator.getBroker();
        if (orb.subcontractDebugFlag) {
            dprint(".handleRequest->:");
        }
        connection.serverRequestProcessingBegins();
    }
    private void dispatchError(CorbaMessageMediator messageMediator,
                               String msg, Throwable t)
    {
        if (orb.subcontractDebugFlag) {
            dprint(".handleRequest: " + opAndId(messageMediator)
                   + ": !!ERROR!!: "
                   + msg,
                   t);
        }
    }
    private void sendResponse(CorbaMessageMediator messageMediator)
    {
        if (orb.subcontractDebugFlag) {
            dprint(".handleRequest: " + opAndId(messageMediator)
                   + ": sending response");
        }
        CDROutputObject outputObject = (CDROutputObject)
            messageMediator.getOutputObject();
        if (outputObject != null) {
            outputObject.finishSendingMessage();
        }
    }
    private void endRequest(CorbaMessageMediator messageMediator)
    {
        ORB orb = (ORB) messageMediator.getBroker();
        if (orb.subcontractDebugFlag) {
            dprint(".handleRequest<-: " + opAndId(messageMediator));
        }
        try {
            OutputObject outputObj = messageMediator.getOutputObject();
            if (outputObj != null) {
                outputObj.close();
            }
            InputObject inputObj = messageMediator.getInputObject();
            if (inputObj != null) {
                inputObj.close();
            }
        } catch (IOException ex) {
            if (orb.subcontractDebugFlag) {
                dprint(".endRequest: IOException:" + ex.getMessage(), ex);
            }
        } finally {
            ((CorbaConnection)messageMediator.getConnection()).serverRequestProcessingEnds();
        }
    }
    protected void handleRequestRequest(CorbaMessageMediator messageMediator)
    {
        ((CDRInputObject)messageMediator.getInputObject()).unmarshalHeader();
        ORB orb = (ORB)messageMediator.getBroker();
        orb.checkShutdownState();
        ObjectKey okey = messageMediator.getObjectKey();
        if (orb.subcontractDebugFlag) {
            ObjectKeyTemplate oktemp = okey.getTemplate() ;
            dprint( ".handleRequest: " + opAndId(messageMediator)
                    + ": dispatching to scid: " + oktemp.getSubcontractId());
        }
        CorbaServerRequestDispatcher sc = okey.getServerRequestDispatcher(orb);
        if (orb.subcontractDebugFlag) {
            dprint(".handleRequest: " + opAndId(messageMediator)
                   + ": dispatching to sc: " + sc);
        }
        if (sc == null) {
            throw wrapper.noServerScInDispatch() ;
        }
        try {
            orb.startingDispatch();
            sc.dispatch(messageMediator);
        } finally {
            orb.finishedDispatch();
        }
    }
    protected void handleLocateRequest(CorbaMessageMediator messageMediator)
    {
        ORB orb = (ORB)messageMediator.getBroker();
        LocateRequestMessage msg = (LocateRequestMessage)
            messageMediator.getDispatchHeader();
        IOR ior = null;
        LocateReplyMessage reply = null;
        short addrDisp = -1;
        try {
            ((CDRInputObject)messageMediator.getInputObject()).unmarshalHeader();
            CorbaServerRequestDispatcher sc =
                msg.getObjectKey().getServerRequestDispatcher( orb ) ;
            if (sc == null) {
                return;
            }
            ior = sc.locate(msg.getObjectKey());
            if ( ior == null ) {
                reply = MessageBase.createLocateReply(
                            orb, msg.getGIOPVersion(),
                            msg.getEncodingVersion(),
                            msg.getRequestId(),
                            LocateReplyMessage.OBJECT_HERE, null);
            } else {
                reply = MessageBase.createLocateReply(
                            orb, msg.getGIOPVersion(),
                            msg.getEncodingVersion(),
                            msg.getRequestId(),
                            LocateReplyMessage.OBJECT_FORWARD, ior);
            }
        } catch (AddressingDispositionException ex) {
            reply = MessageBase.createLocateReply(
                        orb, msg.getGIOPVersion(),
                        msg.getEncodingVersion(),
                        msg.getRequestId(),
                        LocateReplyMessage.LOC_NEEDS_ADDRESSING_MODE, null);
            addrDisp = ex.expectedAddrDisp();
        } catch (RequestCanceledException ex) {
            return; 
        } catch ( Exception ex ) {
            reply = MessageBase.createLocateReply(
                        orb, msg.getGIOPVersion(),
                        msg.getEncodingVersion(),
                        msg.getRequestId(),
                        LocateReplyMessage.UNKNOWN_OBJECT, null);
        }
        CDROutputObject outputObject =
            createAppropriateOutputObject(messageMediator,
                                          msg, reply);
        messageMediator.setOutputObject(outputObject);
        outputObject.setMessageMediator(messageMediator);
        reply.write(outputObject);
        if (ior != null) {
            ior.write(outputObject);
        }
        if (addrDisp != -1) {
            AddressingDispositionHelper.write(outputObject, addrDisp);
        }
    }
    private CDROutputObject createAppropriateOutputObject(
        CorbaMessageMediator messageMediator,
        Message msg, LocateReplyMessage reply)
    {
        CDROutputObject outputObject;
        if (msg.getGIOPVersion().lessThan(GIOPVersion.V1_2)) {
            outputObject = new CDROutputObject(
                             (ORB) messageMediator.getBroker(),
                             this,
                             GIOPVersion.V1_0,
                             (CorbaConnection) messageMediator.getConnection(),
                             reply,
                             ORBConstants.STREAM_FORMAT_VERSION_1);
        } else {
            outputObject = new CDROutputObject(
                             (ORB) messageMediator.getBroker(),
                             messageMediator,
                             reply,
                             ORBConstants.STREAM_FORMAT_VERSION_1);
        }
        return outputObject;
    }
    public void handleThrowableDuringServerDispatch(
        CorbaMessageMediator messageMediator,
        Throwable throwable,
        CompletionStatus completionStatus)
    {
        if (((ORB)messageMediator.getBroker()).subcontractDebugFlag) {
            dprint(".handleThrowableDuringServerDispatch: "
                   + opAndId(messageMediator) + ": "
                   + throwable);
        }
        handleThrowableDuringServerDispatch(messageMediator,
                                            throwable,
                                            completionStatus,
                                            1);
    }
    protected void handleThrowableDuringServerDispatch(
        CorbaMessageMediator messageMediator,
        Throwable throwable,
        CompletionStatus completionStatus,
        int iteration)
    {
        if (iteration > 10) {
            if (((ORB)messageMediator.getBroker()).subcontractDebugFlag) {
                dprint(".handleThrowableDuringServerDispatch: "
                       + opAndId(messageMediator)
                       + ": cannot handle: "
                       + throwable);
            }
            RuntimeException rte =
                new RuntimeException("handleThrowableDuringServerDispatch: " +
                                     "cannot create response.");
            rte.initCause(throwable);
            throw rte;
        }
        try {
            if (throwable instanceof ForwardException) {
                ForwardException fex = (ForwardException)throwable ;
                createLocationForward( messageMediator, fex.getIOR(), null ) ;
                return;
            }
            if (throwable instanceof AddressingDispositionException) {
                handleAddressingDisposition(
                    messageMediator,
                    (AddressingDispositionException)throwable);
                return;
            }
            SystemException sex =
                convertThrowableToSystemException(throwable, completionStatus);
            createSystemExceptionResponse(messageMediator, sex, null);
            return;
        } catch (Throwable throwable2) {
            handleThrowableDuringServerDispatch(messageMediator,
                                                throwable2,
                                                completionStatus,
                                                iteration + 1);
            return;
        }
    }
    protected SystemException convertThrowableToSystemException(
        Throwable throwable,
        CompletionStatus completionStatus)
    {
        if (throwable instanceof SystemException) {
            return (SystemException)throwable;
        }
        if (throwable instanceof RequestCanceledException) {
            return wrapper.requestCanceled( throwable ) ;
        }
        return wrapper.runtimeexception( CompletionStatus.COMPLETED_MAYBE, throwable ) ;
    }
    protected void handleAddressingDisposition(
        CorbaMessageMediator messageMediator,
        AddressingDispositionException ex)
    {
        short addrDisp = -1;
        switch (messageMediator.getRequestHeader().getType()) {
        case Message.GIOPRequest :
            ReplyMessage replyHeader = MessageBase.createReply(
                          (ORB)messageMediator.getBroker(),
                          messageMediator.getGIOPVersion(),
                          messageMediator.getEncodingVersion(),
                          messageMediator.getRequestId(),
                          ReplyMessage.NEEDS_ADDRESSING_MODE,
                          null, null);
            CDROutputObject outputObject = new CDROutputObject(
                (ORB)messageMediator.getBroker(),
                this,
                messageMediator.getGIOPVersion(),
                (CorbaConnection)messageMediator.getConnection(),
                replyHeader,
                ORBConstants.STREAM_FORMAT_VERSION_1);
            messageMediator.setOutputObject(outputObject);
            outputObject.setMessageMediator(messageMediator);
            replyHeader.write(outputObject);
            AddressingDispositionHelper.write(outputObject,
                                              ex.expectedAddrDisp());
            return;
        case Message.GIOPLocateRequest :
            LocateReplyMessage locateReplyHeader = MessageBase.createLocateReply(
                (ORB)messageMediator.getBroker(),
                messageMediator.getGIOPVersion(),
                messageMediator.getEncodingVersion(),
                messageMediator.getRequestId(),
                LocateReplyMessage.LOC_NEEDS_ADDRESSING_MODE,
                null);
            addrDisp = ex.expectedAddrDisp();
            outputObject =
                createAppropriateOutputObject(messageMediator,
                                              messageMediator.getRequestHeader(),
                                              locateReplyHeader);
            messageMediator.setOutputObject(outputObject);
            outputObject.setMessageMediator(messageMediator);
            locateReplyHeader.write(outputObject);
            IOR ior = null;
            if (ior != null) {
                ior.write(outputObject);
            }
            if (addrDisp != -1) {
                AddressingDispositionHelper.write(outputObject, addrDisp);
            }
            return;
        }
    }
    public CorbaMessageMediator createResponse(
        CorbaMessageMediator messageMediator,
        ServiceContexts svc)
    {
        return createResponseHelper(
            messageMediator,
            getServiceContextsForReply(messageMediator, null));
    }
    public CorbaMessageMediator createUserExceptionResponse(
        CorbaMessageMediator messageMediator, ServiceContexts svc)
    {
        return createResponseHelper(
            messageMediator,
            getServiceContextsForReply(messageMediator, null),
            true);
    }
    public CorbaMessageMediator createUnknownExceptionResponse(
        CorbaMessageMediator messageMediator, UnknownException ex)
    {
        ServiceContexts contexts = null;
        SystemException sys = new UNKNOWN( 0,
            CompletionStatus.COMPLETED_MAYBE);
        contexts = new ServiceContexts( (ORB)messageMediator.getBroker() );
        UEInfoServiceContext uei = new UEInfoServiceContext(sys);
        contexts.put( uei ) ;
        return createSystemExceptionResponse(messageMediator, sys, contexts);
    }
    public CorbaMessageMediator createSystemExceptionResponse(
        CorbaMessageMediator messageMediator,
        SystemException ex,
        ServiceContexts svc)
    {
        if (messageMediator.getConnection() != null) {
            CorbaMessageMediatorImpl mediator = (CorbaMessageMediatorImpl)
                ((CorbaConnection)messageMediator.getConnection())
                .serverRequestMapGet(messageMediator.getRequestId());
            OutputObject existingOutputObject = null;
            if (mediator != null) {
                existingOutputObject = mediator.getOutputObject();
            }
            if (existingOutputObject != null &&
                mediator.sentFragment() &&
                ! mediator.sentFullMessage())
            {
                return mediator;
            }
        }
        if (messageMediator.executePIInResponseConstructor()) {
            ((ORB)messageMediator.getBroker()).getPIHandler().setServerPIInfo( ex );
        }
        if (((ORB)messageMediator.getBroker()).subcontractDebugFlag &&
            ex != null)
        {
            dprint(".createSystemExceptionResponse: "
                   + opAndId(messageMediator),
                   ex);
        }
        ServiceContexts serviceContexts =
            getServiceContextsForReply(messageMediator, svc);
        addExceptionDetailMessage(messageMediator, ex, serviceContexts);
        CorbaMessageMediator response =
            createResponseHelper(messageMediator, serviceContexts, false);
        ORBUtility.writeSystemException(
            ex, (OutputStream)response.getOutputObject());
        return response;
    }
    private void addExceptionDetailMessage(CorbaMessageMediator mediator,
                                           SystemException ex,
                                           ServiceContexts serviceContexts)
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(baos);
        ex.printStackTrace(pw);
        pw.flush(); 
        EncapsOutputStream encapsOutputStream =
            new EncapsOutputStream((ORB)mediator.getBroker());
        encapsOutputStream.putEndian();
        encapsOutputStream.write_wstring(baos.toString());
        UnknownServiceContext serviceContext =
            new UnknownServiceContext(ExceptionDetailMessage.value,
                                      encapsOutputStream.toByteArray());
        serviceContexts.put(serviceContext);
    }
    public CorbaMessageMediator createLocationForward(
        CorbaMessageMediator messageMediator, IOR ior, ServiceContexts svc)
    {
        ReplyMessage reply
            = MessageBase.createReply(
                  (ORB)messageMediator.getBroker(),
                  messageMediator.getGIOPVersion(),
                  messageMediator.getEncodingVersion(),
                  messageMediator.getRequestId(),
                  ReplyMessage.LOCATION_FORWARD,
                  getServiceContextsForReply(messageMediator, svc),
                  ior);
        return createResponseHelper(messageMediator, reply, ior);
    }
    protected CorbaMessageMediator createResponseHelper(
        CorbaMessageMediator messageMediator, ServiceContexts svc)
    {
        ReplyMessage message =
            MessageBase.createReply(
                (ORB)messageMediator.getBroker(),
                messageMediator.getGIOPVersion(),
                messageMediator.getEncodingVersion(),
                messageMediator.getRequestId(),
                ReplyMessage.NO_EXCEPTION,
                svc,
                null);
        return createResponseHelper(messageMediator, message, null);
    }
    protected CorbaMessageMediator createResponseHelper(
        CorbaMessageMediator messageMediator, ServiceContexts svc,boolean user)
    {
        ReplyMessage message =
            MessageBase.createReply(
                (ORB)messageMediator.getBroker(),
                messageMediator.getGIOPVersion(),
                messageMediator.getEncodingVersion(),
                messageMediator.getRequestId(),
                user ? ReplyMessage.USER_EXCEPTION :
                       ReplyMessage.SYSTEM_EXCEPTION,
                svc,
                null);
        return createResponseHelper(messageMediator, message, null);
    }
    protected CorbaMessageMediator createResponseHelper(
        CorbaMessageMediator messageMediator, ReplyMessage reply, IOR ior)
    {
        runServantPostInvoke(messageMediator);
        runInterceptors(messageMediator, reply);
        runRemoveThreadInfo(messageMediator);
        if (((ORB)messageMediator.getBroker()).subcontractDebugFlag) {
            dprint(".createResponseHelper: "
                   + opAndId(messageMediator) + ": "
                   + reply);
        }
        messageMediator.setReplyHeader(reply);
        OutputObject replyOutputObject;
        if (messageMediator.getConnection() == null) {
            replyOutputObject =
                new CDROutputObject(orb, messageMediator,
                                    messageMediator.getReplyHeader(),
                                    messageMediator.getStreamFormatVersion(),
                                    BufferManagerFactory.GROW);
        } else {
            replyOutputObject = messageMediator.getConnection().getAcceptor()
             .createOutputObject(messageMediator.getBroker(), messageMediator);
        }
        messageMediator.setOutputObject(replyOutputObject);
        messageMediator.getOutputObject().setMessageMediator(messageMediator);
        reply.write((OutputStream) messageMediator.getOutputObject());
        if (reply.getIOR() != null) {
            reply.getIOR().write((OutputStream) messageMediator.getOutputObject());
        }
        return messageMediator;
    }
    protected void runServantPostInvoke(CorbaMessageMediator messageMediator)
    {
        ORB orb = null;
        if (messageMediator.executeReturnServantInResponseConstructor()) {
            messageMediator.setExecuteReturnServantInResponseConstructor(false);
            messageMediator.setExecuteRemoveThreadInfoInResponseConstructor(true);
            try {
                orb = (ORB)messageMediator.getBroker();
                OAInvocationInfo info = orb.peekInvocationInfo() ;
                ObjectAdapter oa = info.oa();
                try {
                    oa.returnServant() ;
                } catch (Throwable thr) {
                    wrapper.unexpectedException( thr ) ;
                    if (thr instanceof Error)
                        throw (Error)thr ;
                    else if (thr instanceof RuntimeException)
                        throw (RuntimeException)thr ;
                } finally {
                    oa.exit();
                }
            } catch (EmptyStackException ese) {
                throw wrapper.emptyStackRunServantPostInvoke( ese ) ;
            }
        }
    }
    protected void runInterceptors(CorbaMessageMediator messageMediator,
                                   ReplyMessage reply)
    {
        if( messageMediator.executePIInResponseConstructor() ) {
            ((ORB)messageMediator.getBroker()).getPIHandler().
                invokeServerPIEndingPoint( reply );
            ((ORB)messageMediator.getBroker()).getPIHandler().
                cleanupServerPIRequest();
            messageMediator.setExecutePIInResponseConstructor(false);
        }
    }
    protected void runRemoveThreadInfo(CorbaMessageMediator messageMediator)
    {
        if (messageMediator.executeRemoveThreadInfoInResponseConstructor()) {
            messageMediator.setExecuteRemoveThreadInfoInResponseConstructor(false);
            ((ORB)messageMediator.getBroker()).popInvocationInfo() ;
        }
    }
    protected ServiceContexts getServiceContextsForReply(
        CorbaMessageMediator messageMediator, ServiceContexts contexts)
    {
        CorbaConnection c = (CorbaConnection) messageMediator.getConnection();
        if (((ORB)messageMediator.getBroker()).subcontractDebugFlag) {
            dprint(".getServiceContextsForReply: "
                   + opAndId(messageMediator)
                   + ": " + c);
        }
        if (contexts == null) {
            contexts = new ServiceContexts(((ORB)messageMediator.getBroker()));
        }
        if (c != null && !c.isPostInitialContexts()) {
            c.setPostInitialContexts();
            SendingContextServiceContext scsc =
                new SendingContextServiceContext(
                    ((ORB)messageMediator.getBroker()).getFVDCodeBaseIOR()) ;
            if (contexts.get( scsc.getId() ) != null)
                throw wrapper.duplicateSendingContextServiceContext() ;
            contexts.put( scsc ) ;
            if ( ((ORB)messageMediator.getBroker()).subcontractDebugFlag)
                dprint(".getServiceContextsForReply: "
                       + opAndId(messageMediator)
                       + ": added SendingContextServiceContext" ) ;
        }
        ORBVersionServiceContext ovsc
            = new ORBVersionServiceContext(ORBVersionFactory.getORBVersion());
        if (contexts.get( ovsc.getId() ) != null)
            throw wrapper.duplicateOrbVersionServiceContext() ;
        contexts.put( ovsc ) ;
        if ( ((ORB)messageMediator.getBroker()).subcontractDebugFlag)
            dprint(".getServiceContextsForReply: "
                   + opAndId(messageMediator)
                   + ": added ORB version service context");
        return contexts;
    }
    private void releaseByteBufferToPool() {
        if (dispatchByteBuffer != null) {
            orb.getByteBufferPool().releaseByteBuffer(dispatchByteBuffer);
            if (transportDebug()) {
                int bbId = System.identityHashCode(dispatchByteBuffer);
                StringBuffer sb = new StringBuffer();
                sb.append(".handleInput: releasing ByteBuffer (" + bbId +
                          ") to ByteBufferPool");
                dprint(sb.toString());
             }
        }
    }
}
