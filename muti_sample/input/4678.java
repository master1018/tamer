public class CorbaServerRequestDispatcherImpl
    implements CorbaServerRequestDispatcher
{
    protected ORB orb; 
    private ORBUtilSystemException wrapper ;
    private POASystemException poaWrapper ;
    public CorbaServerRequestDispatcherImpl(ORB orb)
    {
        this.orb = orb;
        wrapper = ORBUtilSystemException.get( orb,
            CORBALogDomains.RPC_PROTOCOL ) ;
        poaWrapper = POASystemException.get( orb,
            CORBALogDomains.RPC_PROTOCOL ) ;
    }
    public IOR locate(ObjectKey okey)
    {
        try {
            if (orb.subcontractDebugFlag)
                dprint(".locate->");
            ObjectKeyTemplate oktemp = okey.getTemplate() ;
            try {
                checkServerId(okey);
            } catch (ForwardException fex) {
                return fex.getIOR() ;
            }
            findObjectAdapter(oktemp);
            return null ;
        } finally {
            if (orb.subcontractDebugFlag)
                dprint(".locate<-");
        }
    }
    public void dispatch(MessageMediator messageMediator)
    {
        CorbaMessageMediator request = (CorbaMessageMediator) messageMediator;
        try {
            if (orb.subcontractDebugFlag) {
                dprint(".dispatch->: " + opAndId(request));
            }
            consumeServiceContexts(request);
            ((MarshalInputStream)request.getInputObject())
                .performORBVersionSpecificInit();
            ObjectKey okey = request.getObjectKey();
            try {
                checkServerId(okey);
            } catch (ForwardException fex) {
                if (orb.subcontractDebugFlag) {
                    dprint(".dispatch: " + opAndId(request)
                           + ": bad server id");
                }
                request.getProtocolHandler()
                    .createLocationForward(request, fex.getIOR(), null);
                return;
            }
            String operation = request.getOperationName();
            ObjectAdapter objectAdapter = null ;
            try {
                byte[] objectId = okey.getId().getId() ;
                ObjectKeyTemplate oktemp = okey.getTemplate() ;
                objectAdapter = findObjectAdapter(oktemp);
                java.lang.Object servant = getServantWithPI(request, objectAdapter,
                    objectId, oktemp, operation);
                dispatchToServant(servant, request, objectId, objectAdapter);
            } catch (ForwardException ex) {
                if (orb.subcontractDebugFlag) {
                    dprint(".dispatch: " + opAndId(request)
                           + ": ForwardException caught");
                }
                request.getProtocolHandler()
                    .createLocationForward(request, ex.getIOR(), null);
            } catch (OADestroyed ex) {
                if (orb.subcontractDebugFlag) {
                    dprint(".dispatch: " + opAndId(request)
                           + ": OADestroyed exception caught");
                }
                dispatch(request);
            } catch (RequestCanceledException ex) {
                if (orb.subcontractDebugFlag) {
                    dprint(".dispatch: " + opAndId(request)
                           + ": RequestCanceledException caught");
                }
                throw ex;
            } catch (UnknownException ex) {
                if (orb.subcontractDebugFlag) {
                    dprint(".dispatch: " + opAndId(request)
                           + ": UnknownException caught " + ex);
                }
                if (ex.originalEx instanceof RequestCanceledException) {
                    throw (RequestCanceledException) ex.originalEx;
                }
                ServiceContexts contexts = new ServiceContexts(orb);
                UEInfoServiceContext usc = new UEInfoServiceContext(
                    ex.originalEx);
                contexts.put( usc ) ;
                SystemException sysex = wrapper.unknownExceptionInDispatch(
                        CompletionStatus.COMPLETED_MAYBE, ex ) ;
                request.getProtocolHandler()
                    .createSystemExceptionResponse(request, sysex,
                        contexts);
            } catch (Throwable ex) {
                if (orb.subcontractDebugFlag) {
                    dprint(".dispatch: " + opAndId(request)
                           + ": other exception " + ex);
                }
                request.getProtocolHandler()
                    .handleThrowableDuringServerDispatch(
                        request, ex, CompletionStatus.COMPLETED_MAYBE);
            }
            return;
        } finally {
            if (orb.subcontractDebugFlag) {
                dprint(".dispatch<-: " + opAndId(request));
            }
        }
    }
    private void releaseServant(ObjectAdapter objectAdapter)
    {
        try {
            if (orb.subcontractDebugFlag) {
                dprint(".releaseServant->");
            }
            if (objectAdapter == null) {
                if (orb.subcontractDebugFlag) {
                    dprint(".releaseServant: null object adapter");
                }
                return ;
            }
            try {
                objectAdapter.returnServant();
            } finally {
                objectAdapter.exit();
                orb.popInvocationInfo() ;
            }
        } finally {
            if (orb.subcontractDebugFlag) {
                dprint(".releaseServant<-");
            }
        }
    }
    private java.lang.Object getServant(ObjectAdapter objectAdapter, byte[] objectId,
        String operation)
        throws OADestroyed
    {
        try {
            if (orb.subcontractDebugFlag) {
                dprint(".getServant->");
            }
            OAInvocationInfo info = objectAdapter.makeInvocationInfo(objectId);
            info.setOperation(operation);
            orb.pushInvocationInfo(info);
            objectAdapter.getInvocationServant(info);
            return info.getServantContainer() ;
        } finally {
            if (orb.subcontractDebugFlag) {
                dprint(".getServant<-");
            }
        }
    }
    protected java.lang.Object getServantWithPI(CorbaMessageMediator request,
                                                 ObjectAdapter objectAdapter,
        byte[] objectId, ObjectKeyTemplate oktemp, String operation)
        throws OADestroyed
    {
        try {
            if (orb.subcontractDebugFlag) {
                dprint(".getServantWithPI->");
            }
            orb.getPIHandler().initializeServerPIInfo(request, objectAdapter,
                objectId, oktemp);
            orb.getPIHandler().invokeServerPIStartingPoint();
            objectAdapter.enter() ;
            if (request != null)
                request.setExecuteReturnServantInResponseConstructor(true);
            java.lang.Object servant = getServant(objectAdapter, objectId,
                operation);
            String mdi = "unknown" ;
            if (servant instanceof NullServant)
                handleNullServant(operation, (NullServant)servant);
            else
                mdi = objectAdapter.getInterfaces(servant, objectId)[0] ;
            orb.getPIHandler().setServerPIInfo(servant, mdi);
            if (((servant != null) &&
                !(servant instanceof org.omg.CORBA.DynamicImplementation) &&
                !(servant instanceof org.omg.PortableServer.DynamicImplementation)) ||
                (SpecialMethod.getSpecialMethod(operation) != null)) {
                orb.getPIHandler().invokeServerPIIntermediatePoint();
            }
            return servant ;
        } finally {
            if (orb.subcontractDebugFlag) {
                dprint(".getServantWithPI<-");
            }
        }
    }
    protected void checkServerId(ObjectKey okey)
    {
        try {
            if (orb.subcontractDebugFlag) {
                dprint(".checkServerId->");
            }
            ObjectKeyTemplate oktemp = okey.getTemplate() ;
            int sId = oktemp.getServerId() ;
            int scid = oktemp.getSubcontractId() ;
            if (!orb.isLocalServerId(scid, sId)) {
                if (orb.subcontractDebugFlag) {
                    dprint(".checkServerId: bad server id");
                }
                orb.handleBadServerId(okey);
            }
        } finally {
            if (orb.subcontractDebugFlag) {
                dprint(".checkServerId<-");
            }
        }
    }
    private ObjectAdapter findObjectAdapter(ObjectKeyTemplate oktemp)
    {
        try {
            if (orb.subcontractDebugFlag) {
                dprint(".findObjectAdapter->");
            }
            RequestDispatcherRegistry scr = orb.getRequestDispatcherRegistry() ;
            int scid = oktemp.getSubcontractId() ;
            ObjectAdapterFactory oaf = scr.getObjectAdapterFactory(scid);
            if (oaf == null) {
                if (orb.subcontractDebugFlag) {
                    dprint(".findObjectAdapter: failed to find ObjectAdapterFactory");
                }
                throw wrapper.noObjectAdapterFactory() ;
            }
            ObjectAdapterId oaid = oktemp.getObjectAdapterId() ;
            ObjectAdapter oa = oaf.find(oaid);
            if (oa == null) {
                if (orb.subcontractDebugFlag) {
                    dprint(".findObjectAdapter: failed to find ObjectAdaptor");
                }
                throw wrapper.badAdapterId() ;
            }
            return oa ;
        } finally {
            if (orb.subcontractDebugFlag) {
                dprint(".findObjectAdapter<-");
            }
        }
    }
    protected void handleNullServant(String operation, NullServant nserv )
    {
        try {
            if (orb.subcontractDebugFlag) {
                dprint(".handleNullServant->: " + operation);
            }
            SpecialMethod specialMethod =
                SpecialMethod.getSpecialMethod(operation);
            if ((specialMethod == null) ||
                !specialMethod.isNonExistentMethod()) {
                if (orb.subcontractDebugFlag) {
                    dprint(".handleNullServant: " + operation
                           + ": throwing OBJECT_NOT_EXIST");
                }
                throw nserv.getException() ;
            }
        } finally {
            if (orb.subcontractDebugFlag) {
                dprint(".handleNullServant<-: " + operation);
            }
        }
    }
    protected void consumeServiceContexts(CorbaMessageMediator request)
    {
        try {
            if (orb.subcontractDebugFlag) {
                dprint(".consumeServiceContexts->: "
                       + opAndId(request));
            }
            ServiceContexts ctxts = request.getRequestServiceContexts();
            ServiceContext sc ;
            GIOPVersion giopVersion = request.getGIOPVersion();
            boolean hasCodeSetContext = processCodeSetContext(request, ctxts);
            if (orb.subcontractDebugFlag) {
                dprint(".consumeServiceContexts: " + opAndId(request)
                       + ": GIOP version: " + giopVersion);
                dprint(".consumeServiceContexts: " + opAndId(request)
                       + ": as code set context? " + hasCodeSetContext);
            }
            sc = ctxts.get(
                SendingContextServiceContext.SERVICE_CONTEXT_ID ) ;
            if (sc != null) {
                SendingContextServiceContext scsc =
                    (SendingContextServiceContext)sc ;
                IOR ior = scsc.getIOR() ;
                try {
                    ((CorbaConnection)request.getConnection())
                        .setCodeBaseIOR(ior);
                } catch (ThreadDeath td) {
                    throw td ;
                } catch (Throwable t) {
                    throw wrapper.badStringifiedIor( t ) ;
                }
            }
            boolean isForeignORB = false;
            if (giopVersion.equals(GIOPVersion.V1_0) && hasCodeSetContext) {
                if (orb.subcontractDebugFlag) {
                    dprint(".consumeServiceCOntexts: " + opAndId(request)
                           + ": Determined to be an old Sun ORB");
                }
                orb.setORBVersion(ORBVersionFactory.getOLD()) ;
            } else {
                isForeignORB = true;
            }
            sc = ctxts.get( ORBVersionServiceContext.SERVICE_CONTEXT_ID ) ;
            if (sc != null) {
                ORBVersionServiceContext ovsc =
                   (ORBVersionServiceContext) sc;
                ORBVersion version = ovsc.getVersion();
                orb.setORBVersion(version);
                isForeignORB = false;
            }
            if (isForeignORB) {
                if (orb.subcontractDebugFlag) {
                    dprint(".consumeServiceContexts: " + opAndId(request)
                           + ": Determined to be a foreign ORB");
                }
                orb.setORBVersion(ORBVersionFactory.getFOREIGN());
            }
        } finally {
            if (orb.subcontractDebugFlag) {
                dprint(".consumeServiceContexts<-: " + opAndId(request));
            }
        }
    }
    protected CorbaMessageMediator dispatchToServant(
        java.lang.Object servant,
        CorbaMessageMediator req,
        byte[] objectId, ObjectAdapter objectAdapter)
    {
        try {
            if (orb.subcontractDebugFlag) {
                dprint(".dispatchToServant->: " + opAndId(req));
            }
            CorbaMessageMediator response = null ;
            String operation = req.getOperationName() ;
            SpecialMethod method = SpecialMethod.getSpecialMethod(operation) ;
            if (method != null) {
                if (orb.subcontractDebugFlag) {
                    dprint(".dispatchToServant: " + opAndId(req)
                           + ": Handling special method");
                }
                response = method.invoke(servant, req, objectId, objectAdapter);
                return response ;
            }
            if (servant instanceof org.omg.CORBA.DynamicImplementation) {
                if (orb.subcontractDebugFlag) {
                    dprint(".dispatchToServant: " + opAndId(req)
                           + ": Handling old style DSI type servant");
                }
                org.omg.CORBA.DynamicImplementation dynimpl =
                    (org.omg.CORBA.DynamicImplementation)servant;
                ServerRequestImpl sreq = new ServerRequestImpl(req, orb);
                dynimpl.invoke(sreq);
                response = handleDynamicResult(sreq, req);
            } else if (servant instanceof org.omg.PortableServer.DynamicImplementation) {
                if (orb.subcontractDebugFlag) {
                    dprint(".dispatchToServant: " + opAndId(req)
                           + ": Handling POA DSI type servant");
                }
                org.omg.PortableServer.DynamicImplementation dynimpl =
                    (org.omg.PortableServer.DynamicImplementation)servant;
                ServerRequestImpl sreq = new ServerRequestImpl(req, orb);
                dynimpl.invoke(sreq);
                response = handleDynamicResult(sreq, req);
            } else {
                if (orb.subcontractDebugFlag) {
                    dprint(".dispatchToServant: " + opAndId(req)
                           + ": Handling invoke handler type servant");
                }
                InvokeHandler invhandle = (InvokeHandler)servant ;
                OutputStream stream =
                    (OutputStream)invhandle._invoke(
                      operation,
                      (org.omg.CORBA.portable.InputStream)req.getInputObject(),
                      req);
                response = (CorbaMessageMediator)
                    ((OutputObject)stream).getMessageMediator();
            }
            return response ;
        } finally {
            if (orb.subcontractDebugFlag) {
                dprint(".dispatchToServant<-: " + opAndId(req));
            }
        }
    }
    protected CorbaMessageMediator handleDynamicResult(
        ServerRequestImpl sreq,
        CorbaMessageMediator req)
    {
        try {
            if (orb.subcontractDebugFlag) {
                dprint(".handleDynamicResult->: " + opAndId(req));
            }
            CorbaMessageMediator response = null ;
            Any excany = sreq.checkResultCalled();
            if (excany == null) { 
                if (orb.subcontractDebugFlag) {
                    dprint(".handleDynamicResult: " + opAndId(req)
                           + ": handling normal result");
                }
                response = sendingReply(req);
                OutputStream os = (OutputStream) response.getOutputObject();
                sreq.marshalReplyParams(os);
            }  else {
                if (orb.subcontractDebugFlag) {
                    dprint(".handleDynamicResult: " + opAndId(req)
                           + ": handling error");
                }
                response = sendingReply(req, excany);
            }
            return response ;
        } finally {
            if (orb.subcontractDebugFlag) {
                dprint(".handleDynamicResult<-: " + opAndId(req));
            }
        }
    }
    protected CorbaMessageMediator sendingReply(CorbaMessageMediator req)
    {
        try {
            if (orb.subcontractDebugFlag) {
                dprint(".sendingReply->: " + opAndId(req));
            }
            ServiceContexts scs = new ServiceContexts(orb);
            return req.getProtocolHandler().createResponse(req, scs);
        } finally {
            if (orb.subcontractDebugFlag) {
                dprint(".sendingReply<-: " + opAndId(req));
            }
        }
    }
    protected CorbaMessageMediator sendingReply(CorbaMessageMediator req, Any excany)
    {
        try {
            if (orb.subcontractDebugFlag) {
                dprint(".sendingReply/Any->: " + opAndId(req));
            }
            ServiceContexts scs = new ServiceContexts(orb);
            CorbaMessageMediator resp;
            String repId=null;
            try {
                repId = excany.type().id();
            } catch (org.omg.CORBA.TypeCodePackage.BadKind e) {
                throw wrapper.problemWithExceptionTypecode( e ) ;
            }
            if (ORBUtility.isSystemException(repId)) {
                if (orb.subcontractDebugFlag) {
                    dprint(".sendingReply/Any: " + opAndId(req)
                           + ": handling system exception");
                }
                InputStream in = excany.create_input_stream();
                SystemException ex = ORBUtility.readSystemException(in);
                resp = req.getProtocolHandler()
                    .createSystemExceptionResponse(req, ex, scs);
            } else {
                if (orb.subcontractDebugFlag) {
                    dprint(".sendingReply/Any: " + opAndId(req)
                           + ": handling user exception");
                }
                resp = req.getProtocolHandler()
                    .createUserExceptionResponse(req, scs);
                OutputStream os = (OutputStream)resp.getOutputObject();
                excany.write_value(os);
            }
            return resp;
        } finally {
            if (orb.subcontractDebugFlag) {
                dprint(".sendingReply/Any<-: " + opAndId(req));
            }
        }
    }
    protected boolean processCodeSetContext(
        CorbaMessageMediator request, ServiceContexts contexts)
    {
        try {
            if (orb.subcontractDebugFlag) {
                dprint(".processCodeSetContext->: " + opAndId(request));
            }
            ServiceContext sc = contexts.get(
                CodeSetServiceContext.SERVICE_CONTEXT_ID);
            if (sc != null) {
                if (request.getConnection() == null) {
                    return true;
                }
                if (request.getGIOPVersion().equals(GIOPVersion.V1_0)) {
                    return true;
                }
                CodeSetServiceContext cssc = (CodeSetServiceContext)sc ;
                CodeSetComponentInfo.CodeSetContext csctx = cssc.getCodeSetContext();
                if (((CorbaConnection)request.getConnection())
                    .getCodeSetContext() == null)
                {
                    if (orb.subcontractDebugFlag) {
                        dprint(".processCodeSetContext: " + opAndId(request)
                               + ": Setting code sets to: " + csctx);
                    }
                    ((CorbaConnection)request.getConnection())
                        .setCodeSetContext(csctx);
                    if (csctx.getCharCodeSet() !=
                        OSFCodeSetRegistry.ISO_8859_1.getNumber()) {
                        ((MarshalInputStream)request.getInputObject())
                            .resetCodeSetConverters();
                    }
                }
            }
            return sc != null ;
        } finally {
            if (orb.subcontractDebugFlag) {
                dprint(".processCodeSetContext<-: " + opAndId(request));
            }
        }
    }
    protected void dprint(String msg)
    {
        ORBUtility.dprint("CorbaServerRequestDispatcherImpl", msg);
    }
    protected String opAndId(CorbaMessageMediator mediator)
    {
        return ORBUtility.operationNameAndRequestId(mediator);
    }
}
