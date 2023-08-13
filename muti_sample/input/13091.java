class SnmpRequestHandler extends ClientHandler implements SnmpDefinitions {
    private transient DatagramSocket      socket = null ;
    private transient DatagramPacket      packet = null ;
    private transient Vector              mibs = null ;
    private transient Hashtable<SnmpMibAgent, SnmpSubRequestHandler> subs = null;
    private transient SnmpMibTree root;
    private transient Object              ipacl = null ;
    private transient SnmpPduFactory      pduFactory = null ;
    private transient SnmpUserDataFactory userDataFactory = null ;
    private transient SnmpAdaptorServer adaptor = null;
    public SnmpRequestHandler(SnmpAdaptorServer server, int id,
                              DatagramSocket s, DatagramPacket p,
                              SnmpMibTree tree, Vector m, Object a,
                              SnmpPduFactory factory,
                              SnmpUserDataFactory dataFactory,
                              MBeanServer f, ObjectName n)
    {
        super(server, id, f, n);
        adaptor = server;
        socket = s;
        packet = p;
        root= tree;
        mibs = (Vector) m.clone();
        subs= new Hashtable<SnmpMibAgent, SnmpSubRequestHandler>(mibs.size());
        ipacl = a;
        pduFactory = factory ;
        userDataFactory = dataFactory ;
    }
    public void doRun() {
        if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)) {
            SNMP_ADAPTOR_LOGGER.logp(Level.FINER, dbgTag,
                    "doRun","Packet received:\n" +
                    SnmpMessage.dumpHexBuffer(packet.getData(), 0, packet.getLength()));
        }
        DatagramPacket respPacket = makeResponsePacket(packet) ;
        if ((SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)) && (respPacket != null)) {
            SNMP_ADAPTOR_LOGGER.logp(Level.FINER, dbgTag,
                    "doRun","Packet to be sent:\n" +
                    SnmpMessage.dumpHexBuffer(respPacket.getData(), 0, respPacket.getLength()));
        }
        if (respPacket != null) {
            try {
                socket.send(respPacket) ;
            } catch (SocketException e) {
                if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)) {
                    if (e.getMessage().equals(InterruptSysCallMsg)) {
                        SNMP_ADAPTOR_LOGGER.logp(Level.FINEST, dbgTag,
                            "doRun", "interrupted");
                    } else {
                      SNMP_ADAPTOR_LOGGER.logp(Level.FINEST, dbgTag,
                            "doRun", "I/O exception", e);
                    }
                }
            } catch(InterruptedIOException e) {
                if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)) {
                    SNMP_ADAPTOR_LOGGER.logp(Level.FINEST, dbgTag,
                        "doRun", "interrupted");
                }
            } catch(Exception e) {
                if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)) {
                    SNMP_ADAPTOR_LOGGER.logp(Level.FINEST, dbgTag,
                        "doRun", "failure when sending response", e);
                }
            }
        }
    }
    private DatagramPacket makeResponsePacket(DatagramPacket reqPacket) {
        DatagramPacket respPacket = null ;
        SnmpMessage reqMsg = new SnmpMessage() ;
        try {
            reqMsg.decodeMessage(reqPacket.getData(), reqPacket.getLength()) ;
            reqMsg.address = reqPacket.getAddress() ;
            reqMsg.port = reqPacket.getPort() ;
        }
        catch(SnmpStatusException x) {
            if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)) {
                SNMP_ADAPTOR_LOGGER.logp(Level.FINEST, dbgTag,
                    "makeResponsePacket", "packet decoding failed", x);
            }
            reqMsg = null ;
            ((SnmpAdaptorServer)adaptorServer).incSnmpInASNParseErrs(1) ;
        }
        SnmpMessage respMsg = null ;
        if (reqMsg != null) {
            respMsg = makeResponseMessage(reqMsg) ;
        }
        if (respMsg != null) {
            try {
                reqPacket.setLength(respMsg.encodeMessage(reqPacket.getData())) ;
                respPacket = reqPacket ;
            }
            catch(SnmpTooBigException x) {
                if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)) {
                    SNMP_ADAPTOR_LOGGER.logp(Level.FINEST, dbgTag,
                        "makeResponsePacket", "response message is too big");
                }
                try {
                    respMsg = newTooBigMessage(reqMsg) ;
                    reqPacket.setLength(respMsg.encodeMessage(reqPacket.getData())) ;
                    respPacket = reqPacket ;
                }
                catch(SnmpTooBigException xx) {
                    if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)) {
                        SNMP_ADAPTOR_LOGGER.logp(Level.FINEST, dbgTag,
                            "makeResponsePacket", "'too big' is 'too big' !!!");
                    }
                    adaptor.incSnmpSilentDrops(1);
                }
            }
        }
        return respPacket ;
    }
    private SnmpMessage makeResponseMessage(SnmpMessage reqMsg) {
        SnmpMessage respMsg = null ;
        SnmpPduPacket reqPdu = null ;
        Object userData = null;
        try {
            reqPdu = (SnmpPduPacket)pduFactory.decodeSnmpPdu(reqMsg) ;
            if (reqPdu != null && userDataFactory != null)
                userData = userDataFactory.allocateUserData(reqPdu);
        }
        catch(SnmpStatusException x) {
            reqPdu = null ;
            SnmpAdaptorServer snmpServer = (SnmpAdaptorServer)adaptorServer ;
            snmpServer.incSnmpInASNParseErrs(1) ;
            if (x.getStatus()== SnmpDefinitions.snmpWrongSnmpVersion)
                snmpServer.incSnmpInBadVersions(1) ;
            if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)) {
                SNMP_ADAPTOR_LOGGER.logp(Level.FINEST, dbgTag,
                    "makeResponseMessage", "message decoding failed", x);
            }
        }
        SnmpPduPacket respPdu = null ;
        if (reqPdu != null) {
            respPdu = makeResponsePdu(reqPdu,userData) ;
            try {
                if (userDataFactory != null)
                    userDataFactory.releaseUserData(userData,respPdu);
            } catch (SnmpStatusException x) {
                respPdu = null;
            }
        }
        if (respPdu != null) {
            try {
                respMsg = (SnmpMessage)pduFactory.
                    encodeSnmpPdu(respPdu, packet.getData().length) ;
            }
            catch(SnmpStatusException x) {
                respMsg = null ;
                if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)) {
                    SNMP_ADAPTOR_LOGGER.logp(Level.FINEST, dbgTag,
                        "makeResponseMessage", "failure when encoding the response message", x);
                }
            }
            catch(SnmpTooBigException x) {
                if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)) {
                    SNMP_ADAPTOR_LOGGER.logp(Level.FINEST, dbgTag,
                        "makeResponseMessage", "response message is too big");
                }
                try {
                    if (packet.getData().length <=32)
                        throw x;
                    int pos= x.getVarBindCount();
                    if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)) {
                        SNMP_ADAPTOR_LOGGER.logp(Level.FINEST, dbgTag,
                            "makeResponseMessage", "fail on element" + pos);
                    }
                    int old= 0;
                    while (true) {
                        try {
                            respPdu = reduceResponsePdu(reqPdu, respPdu, pos) ;
                            respMsg = (SnmpMessage)pduFactory.
                                encodeSnmpPdu(respPdu,
                                              packet.getData().length -32) ;
                            break;
                        } catch (SnmpTooBigException xx) {
                            if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)) {
                                SNMP_ADAPTOR_LOGGER.logp(Level.FINEST, dbgTag,
                                    "makeResponseMessage", "response message is still too big");
                            }
                            old= pos;
                            pos= xx.getVarBindCount();
                            if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)) {
                                SNMP_ADAPTOR_LOGGER.logp(Level.FINEST, dbgTag,
                                    "makeResponseMessage","fail on element" + pos);
                            }
                            if (pos == old) {
                                throw xx;
                            }
                        }
                    }
                } catch(SnmpStatusException xx) {
                    respMsg = null ;
                    if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)) {
                        SNMP_ADAPTOR_LOGGER.logp(Level.FINEST, dbgTag,
                           "makeResponseMessage", "failure when encoding the response message", xx);
                    }
                }
                catch(SnmpTooBigException xx) {
                    try {
                        respPdu = newTooBigPdu(reqPdu) ;
                        respMsg = (SnmpMessage)pduFactory.
                            encodeSnmpPdu(respPdu, packet.getData().length) ;
                    }
                    catch(SnmpTooBigException xxx) {
                        respMsg = null ;
                        if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)) {
                            SNMP_ADAPTOR_LOGGER.logp(Level.FINEST, dbgTag,
                               "makeResponseMessage", "'too big' is 'too big' !!!");
                        }
                        adaptor.incSnmpSilentDrops(1);
                    }
                    catch(Exception xxx) {
                        if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)) {
                            SNMP_ADAPTOR_LOGGER.logp(Level.FINEST, dbgTag,
                               "makeResponseMessage", "Got unexpected exception", xxx);
                        }
                        respMsg = null ;
                    }
                }
                catch(Exception xx) {
                    if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)) {
                        SNMP_ADAPTOR_LOGGER.logp(Level.FINEST, dbgTag,
                           "makeResponseMessage", "Got unexpected exception", xx);
                    }
                    respMsg = null ;
                }
            }
        }
        return respMsg ;
    }
    private SnmpPduPacket makeResponsePdu(SnmpPduPacket reqPdu,
                                          Object userData) {
        SnmpAdaptorServer snmpServer = (SnmpAdaptorServer)adaptorServer ;
        SnmpPduPacket respPdu = null ;
        snmpServer.updateRequestCounters(reqPdu.type) ;
        if (reqPdu.varBindList != null)
            snmpServer.updateVarCounters(reqPdu.type,
                                         reqPdu.varBindList.length) ;
        if (checkPduType(reqPdu)) {
            respPdu = checkAcl(reqPdu) ;
            if (respPdu == null) { 
                if (mibs.size() < 1) {
                    if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)) {
                        SNMP_ADAPTOR_LOGGER.logp(Level.FINER, dbgTag,
                           "makeResponsePdu", "Request " + reqPdu.requestId +
                           " received but no MIB registered.");
                    }
                    return makeNoMibErrorPdu((SnmpPduRequest)reqPdu, userData);
                }
                switch(reqPdu.type) {
                case SnmpPduPacket.pduGetRequestPdu:
                case SnmpPduPacket.pduGetNextRequestPdu:
                case SnmpPduPacket.pduSetRequestPdu:
                    respPdu = makeGetSetResponsePdu((SnmpPduRequest)reqPdu,
                                                    userData) ;
                    break ;
                case SnmpPduPacket.pduGetBulkRequestPdu:
                    respPdu = makeGetBulkResponsePdu((SnmpPduBulk)reqPdu,
                                                     userData) ;
                    break ;
                }
            }
            else { 
                if (!snmpServer.getAuthRespEnabled()) { 
                    respPdu = null ;
                }
                if (snmpServer.getAuthTrapEnabled()) { 
                    try {
                        snmpServer.snmpV1Trap(SnmpPduTrap.
                                              trapAuthenticationFailure, 0,
                                              new SnmpVarBindList()) ;
                    }
                    catch(Exception x) {
                        if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)) {
                            SNMP_ADAPTOR_LOGGER.logp(Level.FINEST, dbgTag,
                               "makeResponsePdu", "Failure when sending authentication trap", x);
                        }
                    }
                }
            }
        }
        return respPdu ;
    }
    SnmpPduPacket makeErrorVarbindPdu(SnmpPduPacket req, int statusTag) {
        final SnmpVarBind[] vblist = req.varBindList;
        final int length = vblist.length;
        switch (statusTag) {
        case SnmpDataTypeEnums.errEndOfMibViewTag:
            for (int i=0 ; i<length ; i++)
                vblist[i].value = SnmpVarBind.endOfMibView;
            break;
        case SnmpDataTypeEnums.errNoSuchObjectTag:
            for (int i=0 ; i<length ; i++)
                vblist[i].value = SnmpVarBind.noSuchObject;
            break;
        case SnmpDataTypeEnums.errNoSuchInstanceTag:
            for (int i=0 ; i<length ; i++)
                vblist[i].value = SnmpVarBind.noSuchInstance;
            break;
        default:
            return newErrorResponsePdu(req,snmpRspGenErr,1);
        }
        return newValidResponsePdu(req,vblist);
    }
    SnmpPduPacket makeNoMibErrorPdu(SnmpPduRequest req, Object userData) {
        if (req.version == SnmpDefinitions.snmpVersionOne) {
            return
                newErrorResponsePdu(req,snmpRspNoSuchName,1);
        } else if (req.version == SnmpDefinitions.snmpVersionTwo) {
            switch (req.type) {
            case pduSetRequestPdu :
            case pduWalkRequest :
                return
                    newErrorResponsePdu(req,snmpRspNoAccess,1);
            case pduGetRequestPdu :
                return
                    makeErrorVarbindPdu(req,SnmpDataTypeEnums.
                                        errNoSuchObjectTag);
            case pduGetNextRequestPdu :
            case pduGetBulkRequestPdu :
                return
                    makeErrorVarbindPdu(req,SnmpDataTypeEnums.
                                        errEndOfMibViewTag);
            default:
            }
        }
        return newErrorResponsePdu(req,snmpRspGenErr,1);
    }
    private SnmpPduPacket makeGetSetResponsePdu(SnmpPduRequest req,
                                                Object userData) {
        if (req.varBindList == null) {
            return newValidResponsePdu(req, null) ;
        }
        splitRequest(req);
        int nbSubRequest= subs.size();
        if (nbSubRequest == 1)
            return turboProcessingGetSet(req,userData);
        SnmpPduPacket result= executeSubRequest(req,userData);
        if (result != null)
            return result;
        if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)) {
            SNMP_ADAPTOR_LOGGER.logp(Level.FINER, dbgTag,
               "makeGetSetResponsePdu",
               "Build the unified response for request " + req.requestId);
        }
        return mergeResponses(req);
    }
    private SnmpPduPacket executeSubRequest(SnmpPduPacket req,
                                            Object userData) {
        int errorStatus = SnmpDefinitions.snmpRspNoError ;
        int nbSubRequest= subs.size();
        int i=0;
        if (req.type == pduSetRequestPdu) {
            i=0;
            for(Enumeration e= subs.elements(); e.hasMoreElements() ; i++) {
                SnmpSubRequestHandler sub= (SnmpSubRequestHandler)
                    e.nextElement();
                sub.setUserData(userData);
                sub.type= pduWalkRequest;
                sub.run();
                sub.type= pduSetRequestPdu;
                if (sub.getErrorStatus() != SnmpDefinitions.snmpRspNoError) {
                    if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)) {
                        SNMP_ADAPTOR_LOGGER.logp(Level.FINEST, dbgTag,
                           "executeSubRequest", "an error occurs");
                    }
                    return newErrorResponsePdu(req, errorStatus,
                                               sub.getErrorIndex() + 1) ;
                }
            }
        }
        i=0;
        for(Enumeration e= subs.elements(); e.hasMoreElements() ;i++) {
            SnmpSubRequestHandler sub= (SnmpSubRequestHandler) e.nextElement();
            sub.setUserData(userData);
            sub.run();
            if (sub.getErrorStatus() != SnmpDefinitions.snmpRspNoError) {
                if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)) {
                    SNMP_ADAPTOR_LOGGER.logp(Level.FINEST, dbgTag,
                       "executeSubRequest", "an error occurs");
                }
                return newErrorResponsePdu(req, errorStatus,
                                           sub.getErrorIndex() + 1) ;
            }
        }
        return null;
    }
    private SnmpPduPacket turboProcessingGetSet(SnmpPduRequest req,
                                                Object userData) {
        int errorStatus = SnmpDefinitions.snmpRspNoError ;
        SnmpSubRequestHandler sub = subs.elements().nextElement();
        sub.setUserData(userData);
        if (req.type == SnmpDefinitions.pduSetRequestPdu) {
            sub.type= pduWalkRequest;
            sub.run();
            sub.type= pduSetRequestPdu;
            errorStatus= sub.getErrorStatus();
            if (errorStatus != SnmpDefinitions.snmpRspNoError) {
                return newErrorResponsePdu(req, errorStatus,
                                           sub.getErrorIndex() + 1) ;
            }
        }
        sub.run();
        errorStatus= sub.getErrorStatus();
        if (errorStatus != SnmpDefinitions.snmpRspNoError) {
            if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)) {
                SNMP_ADAPTOR_LOGGER.logp(Level.FINEST, dbgTag,
                   "turboProcessingGetSet", "an error occurs");
            }
            int realIndex= sub.getErrorIndex() + 1;
            return newErrorResponsePdu(req, errorStatus, realIndex) ;
        }
        if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)) {
            SNMP_ADAPTOR_LOGGER.logp(Level.FINER, dbgTag,
               "turboProcessingGetSet",  "build the unified response for request "
                + req.requestId);
        }
        return mergeResponses(req);
    }
    private SnmpPduPacket makeGetBulkResponsePdu(SnmpPduBulk req,
                                                 Object userData) {
        SnmpVarBind[] respVarBindList = null ;
        int L = req.varBindList.length ;
        int N = Math.max(Math.min(req.nonRepeaters, L), 0) ;
        int M = Math.max(req.maxRepetitions, 0) ;
        int R = L - N ;
        if (req.varBindList == null) {
            return newValidResponsePdu(req, null) ;
        }
        splitBulkRequest(req, N, M, R);
        SnmpPduPacket result= executeSubRequest(req,userData);
        if (result != null)
            return result;
        respVarBindList= mergeBulkResponses(N + (M * R));
        int m2 ; 
        int t = respVarBindList.length ;
        while ((t > N) && (respVarBindList[t-1].
                           value.equals(SnmpVarBind.endOfMibView))) {
            t-- ;
        }
        if (t == N)
            m2 = N + R ;
        else
            m2 = N + ((t -1 -N) / R + 2) * R ; 
        if (m2 < respVarBindList.length) {
            SnmpVarBind[] truncatedList = new SnmpVarBind[m2] ;
            for (int i = 0 ; i < m2 ; i++) {
                truncatedList[i] = respVarBindList[i] ;
            }
            respVarBindList = truncatedList ;
        }
        return newValidResponsePdu(req, respVarBindList) ;
    }
    private boolean checkPduType(SnmpPduPacket pdu) {
        boolean result = true ;
        switch(pdu.type) {
        case SnmpDefinitions.pduGetRequestPdu:
        case SnmpDefinitions.pduGetNextRequestPdu:
        case SnmpDefinitions.pduSetRequestPdu:
        case SnmpDefinitions.pduGetBulkRequestPdu:
            result = true ;
            break;
        default:
            if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)) {
                SNMP_ADAPTOR_LOGGER.logp(Level.FINEST, dbgTag,
                   "checkPduType", "cannot respond to this kind of PDU");
            }
            result = false ;
            break;
        }
        return result ;
    }
    private SnmpPduPacket checkAcl(SnmpPduPacket pdu) {
        SnmpPduPacket response = null ;
        String community = new String(pdu.community) ;
        if (ipacl != null) {
            if (pdu.type == SnmpDefinitions.pduSetRequestPdu) {
                if (!((InetAddressAcl)ipacl).
                    checkWritePermission(pdu.address, community)) {
                    if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)) {
                        SNMP_ADAPTOR_LOGGER.logp(Level.FINER, dbgTag,
                           "checkAcl", "sender is " + pdu.address +
                              " with " + community +". Sender has no write permission");
                    }
                    int err = SnmpSubRequestHandler.
                        mapErrorStatus(SnmpDefinitions.
                                       snmpRspAuthorizationError,
                                       pdu.version, pdu.type);
                    response = newErrorResponsePdu(pdu, err, 0) ;
                }
                else {
                    if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)) {
                        SNMP_ADAPTOR_LOGGER.logp(Level.FINER, dbgTag,
                           "checkAcl", "sender is " + pdu.address +
                              " with " + community +". Sender has write permission");
                    }
                }
            }
            else {
                if (!((InetAddressAcl)ipacl).checkReadPermission(pdu.address, community)) {
                    if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)) {
                        SNMP_ADAPTOR_LOGGER.logp(Level.FINER, dbgTag,
                           "checkAcl", "sender is " + pdu.address +
                              " with " + community +". Sender has no read permission");
                    }
                    int err = SnmpSubRequestHandler.
                        mapErrorStatus(SnmpDefinitions.
                                       snmpRspAuthorizationError,
                                       pdu.version, pdu.type);
                    response = newErrorResponsePdu(pdu,
                                                   err,
                                                   0);
                    SnmpAdaptorServer snmpServer =
                        (SnmpAdaptorServer)adaptorServer;
                    snmpServer.updateErrorCounters(SnmpDefinitions.
                                                   snmpRspNoSuchName);
                }
                else {
                    if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)) {
                        SNMP_ADAPTOR_LOGGER.logp(Level.FINER, dbgTag,
                           "checkAcl", "sender is " + pdu.address +
                              " with " + community +". Sender has read permission");
                    }
                }
            }
        }
        if (response != null) {
            SnmpAdaptorServer snmpServer = (SnmpAdaptorServer)adaptorServer ;
            snmpServer.incSnmpInBadCommunityUses(1) ;
            if (((InetAddressAcl)ipacl).checkCommunity(community) == false)
                snmpServer.incSnmpInBadCommunityNames(1) ;
        }
        return response ;
    }
    private SnmpPduRequest newValidResponsePdu(SnmpPduPacket reqPdu,
                                               SnmpVarBind[] varBindList) {
        SnmpPduRequest result = new SnmpPduRequest() ;
        result.address = reqPdu.address ;
        result.port = reqPdu.port ;
        result.version = reqPdu.version ;
        result.community = reqPdu.community ;
        result.type = result.pduGetResponsePdu ;
        result.requestId = reqPdu.requestId ;
        result.errorStatus = SnmpDefinitions.snmpRspNoError ;
        result.errorIndex = 0 ;
        result.varBindList = varBindList ;
        ((SnmpAdaptorServer)adaptorServer).
            updateErrorCounters(result.errorStatus) ;
        return result ;
    }
    private SnmpPduRequest newErrorResponsePdu(SnmpPduPacket req,int s,int i) {
        SnmpPduRequest result = newValidResponsePdu(req, null) ;
        result.errorStatus = s ;
        result.errorIndex = i ;
        result.varBindList = req.varBindList ;
        ((SnmpAdaptorServer)adaptorServer).
            updateErrorCounters(result.errorStatus) ;
        return result ;
    }
    private SnmpMessage newTooBigMessage(SnmpMessage reqMsg)
        throws SnmpTooBigException {
        SnmpMessage result = null ;
        SnmpPduPacket reqPdu = null ;
        try {
            reqPdu = (SnmpPduPacket)pduFactory.decodeSnmpPdu(reqMsg) ;
            if (reqPdu != null) {
                SnmpPduPacket respPdu = newTooBigPdu(reqPdu) ;
                result = (SnmpMessage)pduFactory.
                    encodeSnmpPdu(respPdu, packet.getData().length) ;
            }
        }
        catch(SnmpStatusException x) {
            if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)) {
                SNMP_ADAPTOR_LOGGER.logp(Level.FINEST, dbgTag,
                   "newTooBigMessage", "Internal error", x);
            }
            throw new InternalError() ;
        }
        return result ;
    }
    private SnmpPduPacket newTooBigPdu(SnmpPduPacket req) {
        SnmpPduRequest result =
            newErrorResponsePdu(req, SnmpDefinitions.snmpRspTooBig, 0) ;
        result.varBindList = null ;
        return result ;
    }
    private SnmpPduPacket reduceResponsePdu(SnmpPduPacket req,
                                            SnmpPduPacket resp,
                                            int acceptedVbCount)
        throws SnmpTooBigException {
        if (req.type != req.pduGetBulkRequestPdu) {
            if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)) {
                SNMP_ADAPTOR_LOGGER.logp(Level.FINEST, dbgTag,
                   "reduceResponsePdu", "cannot remove anything");
            }
            throw new SnmpTooBigException(acceptedVbCount) ;
        }
        int vbCount = resp.varBindList.length ;
        if (acceptedVbCount >= 3)
            vbCount = Math.min(acceptedVbCount - 1, resp.varBindList.length) ;
        else if (acceptedVbCount == 1)
            vbCount = 1 ;
        else 
            vbCount = resp.varBindList.length / 2 ;
        if (vbCount < 1) {
            if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)) {
                SNMP_ADAPTOR_LOGGER.logp(Level.FINEST, dbgTag,
                   "reduceResponsePdu", "cannot remove anything");
            }
            throw new SnmpTooBigException(acceptedVbCount) ;
        }
        else {
            SnmpVarBind[] newVbList = new SnmpVarBind[vbCount] ;
            for (int i = 0 ; i < vbCount ; i++) {
                newVbList[i] = resp.varBindList[i] ;
            }
            if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)) {
                SNMP_ADAPTOR_LOGGER.logp(Level.FINEST, dbgTag,
                   "reduceResponsePdu", (resp.varBindList.length - newVbList.length) +
                    " items have been removed");
            }
            resp.varBindList = newVbList ;
        }
        return resp ;
    }
    private void splitRequest(SnmpPduRequest req) {
        int nbAgents= mibs.size();
        SnmpMibAgent agent= (SnmpMibAgent) mibs.firstElement();
        if (nbAgents == 1) {
            subs.put(agent, new SnmpSubRequestHandler(agent, req, true));
            return;
        }
        if (req.type == pduGetNextRequestPdu) {
            for(Enumeration e= mibs.elements(); e.hasMoreElements(); ) {
                SnmpMibAgent ag= (SnmpMibAgent) e.nextElement();
                subs.put(ag, new SnmpSubNextRequestHandler(adaptor, ag, req));
            }
            return;
        }
        int nbReqs= req.varBindList.length;
        SnmpVarBind[] vars= req.varBindList;
        SnmpSubRequestHandler sub;
        for(int i=0; i < nbReqs; i++) {
            agent= root.getAgentMib(vars[i].oid);
            sub= subs.get(agent);
            if (sub == null) {
                sub= new SnmpSubRequestHandler(agent, req);
                subs.put(agent, sub);
            }
            sub.updateRequest(vars[i], i);
        }
    }
    private void splitBulkRequest(SnmpPduBulk req,
                                  int nonRepeaters,
                                  int maxRepetitions,
                                  int R) {
        for(Enumeration e= mibs.elements(); e.hasMoreElements(); ) {
            SnmpMibAgent agent = (SnmpMibAgent) e.nextElement();
            if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)) {
                SNMP_ADAPTOR_LOGGER.logp(Level.FINER, dbgTag,
                   "splitBulkRequest", "Create a sub with : " + agent + " " + nonRepeaters
                   + " " + maxRepetitions + " " + R);
            }
            subs.put(agent,
                     new SnmpSubBulkRequestHandler(adaptor,
                                                   agent,
                                                   req,
                                                   nonRepeaters,
                                                   maxRepetitions,
                                                   R));
        }
        return;
    }
    private SnmpPduPacket mergeResponses(SnmpPduRequest req) {
        if (req.type == pduGetNextRequestPdu) {
            return mergeNextResponses(req);
        }
        SnmpVarBind[] result= req.varBindList;
        for(Enumeration e= subs.elements(); e.hasMoreElements();) {
            SnmpSubRequestHandler sub= (SnmpSubRequestHandler) e.nextElement();
            sub.updateResult(result);
        }
        return newValidResponsePdu(req,result);
    }
    private SnmpPduPacket mergeNextResponses(SnmpPduRequest req) {
        int max= req.varBindList.length;
        SnmpVarBind[] result= new SnmpVarBind[max];
        for(Enumeration e= subs.elements(); e.hasMoreElements();) {
            SnmpSubRequestHandler sub= (SnmpSubRequestHandler) e.nextElement();
            sub.updateResult(result);
        }
        if (req.version == snmpVersionTwo) {
            return newValidResponsePdu(req,result);
        }
        for(int i=0; i < max; i++) {
            SnmpValue val= result[i].value;
            if (val == SnmpVarBind.endOfMibView)
                return newErrorResponsePdu(req,
                                   SnmpDefinitions.snmpRspNoSuchName, i+1);
        }
        return newValidResponsePdu(req,result);
    }
    private SnmpVarBind[] mergeBulkResponses(int size) {
        SnmpVarBind[] result= new SnmpVarBind[size];
        for(int i= size-1; i >=0; --i) {
            result[i]= new SnmpVarBind();
            result[i].value= SnmpVarBind.endOfMibView;
        }
        for(Enumeration e= subs.elements(); e.hasMoreElements();) {
            SnmpSubRequestHandler sub= (SnmpSubRequestHandler) e.nextElement();
            sub.updateResult(result);
        }
        return result;
    }
    protected String makeDebugTag() {
        return "SnmpRequestHandler[" + adaptorServer.getProtocol() + ":" +
            adaptorServer.getPort() + "]";
    }
    Thread createThread(Runnable r) {
        return null;
    }
    static final private String InterruptSysCallMsg =
        "Interrupted system call";
    static final private SnmpStatusException noSuchNameException =
        new SnmpStatusException(SnmpDefinitions.snmpRspNoSuchName) ;
}
