class SnmpSubRequestHandler implements SnmpDefinitions, Runnable {
    protected SnmpIncomingRequest incRequest = null;
    protected SnmpEngine engine = null;
    protected SnmpSubRequestHandler(SnmpEngine engine,
                                    SnmpIncomingRequest incRequest,
                                    SnmpMibAgent agent,
                                    SnmpPdu req) {
        this(agent, req);
        init(engine, incRequest);
    }
    protected SnmpSubRequestHandler(SnmpEngine engine,
                                    SnmpIncomingRequest incRequest,
                                    SnmpMibAgent agent,
                                    SnmpPdu req,
                                    boolean nouse) {
        this(agent, req, nouse);
        init(engine, incRequest);
    }
    protected SnmpSubRequestHandler(SnmpMibAgent agent, SnmpPdu req) {
        if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)) {
            SNMP_ADAPTOR_LOGGER.logp(Level.FINER, SnmpSubRequestHandler.class.getName(),
                "constructor", "creating instance for request " + String.valueOf(req.requestId));
        }
        version= req.version;
        type= req.type;
        this.agent= agent;
        reqPdu = req;
        int length= req.varBindList.length;
        translation= new int[length];
        varBind= new NonSyncVector<SnmpVarBind>(length);
    }
    @SuppressWarnings("unchecked")  
    protected SnmpSubRequestHandler(SnmpMibAgent agent,
                                    SnmpPdu req,
                                    boolean nouse) {
        this(agent,req);
        int max= translation.length;
        SnmpVarBind[] list= req.varBindList;
        for(int i=0; i < max; i++) {
            translation[i]= i;
            ((NonSyncVector<SnmpVarBind>)varBind).addNonSyncElement(list[i]);
        }
    }
    SnmpMibRequest createMibRequest(Vector<SnmpVarBind> vblist,
                                    int protocolVersion,
                                    Object userData) {
        if (type == pduSetRequestPdu && mibRequest != null)
            return mibRequest;
        SnmpMibRequest result = null;
        if(incRequest != null) {
            result = SnmpMibAgent.newMibRequest(engine,
                                                reqPdu,
                                                vblist,
                                                protocolVersion,
                                                userData,
                                                incRequest.getPrincipal(),
                                                incRequest.getSecurityLevel(),
                                                incRequest.getSecurityModel(),
                                                incRequest.getContextName(),
                                                incRequest.getAccessContext());
        } else {
            result = SnmpMibAgent.newMibRequest(reqPdu,
                                                vblist,
                                                protocolVersion,
                                                userData);
        }
        if (type == pduWalkRequest)
            mibRequest = result;
        return result;
    }
    void setUserData(Object userData) {
        data = userData;
    }
    public void run() {
        try {
            final ThreadContext oldContext =
                ThreadContext.push("SnmpUserData",data);
            try {
                switch(type) {
                case pduGetRequestPdu:
                    if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)) {
                        SNMP_ADAPTOR_LOGGER.logp(Level.FINER, SnmpSubRequestHandler.class.getName(),
                            "run", "[" + Thread.currentThread() +
                              "]:get operation on " + agent.getMibName());
                    }
                    agent.get(createMibRequest(varBind,version,data));
                    break;
                case pduGetNextRequestPdu:
                    if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)) {
                        SNMP_ADAPTOR_LOGGER.logp(Level.FINER, SnmpSubRequestHandler.class.getName(),
                            "run", "[" + Thread.currentThread() +
                              "]:getNext operation on " + agent.getMibName());
                    }
                    agent.getNext(createMibRequest(varBind,version,data));
                    break;
                case pduSetRequestPdu:
                    if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)) {
                        SNMP_ADAPTOR_LOGGER.logp(Level.FINER, SnmpSubRequestHandler.class.getName(),
                            "run", "[" + Thread.currentThread() +
                            "]:set operation on " + agent.getMibName());
                    }
                    agent.set(createMibRequest(varBind,version,data));
                    break;
                case pduWalkRequest:
                    if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)) {
                        SNMP_ADAPTOR_LOGGER.logp(Level.FINER, SnmpSubRequestHandler.class.getName(),
                            "run", "[" + Thread.currentThread() +
                            "]:check operation on " + agent.getMibName());
                    }
                    agent.check(createMibRequest(varBind,version,data));
                    break;
                default:
                    if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)) {
                        SNMP_ADAPTOR_LOGGER.logp(Level.FINEST, SnmpSubRequestHandler.class.getName(),
                            "run", "[" + Thread.currentThread() +
                              "]:unknown operation (" +  type + ") on " +
                              agent.getMibName());
                    }
                    errorStatus= snmpRspGenErr;
                    errorIndex= 1;
                    break;
                }
            } finally {
                ThreadContext.restore(oldContext);
            }
        } catch(SnmpStatusException x) {
            errorStatus = x.getStatus() ;
            errorIndex=  x.getErrorIndex();
            if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)) {
                SNMP_ADAPTOR_LOGGER.logp(Level.FINEST, SnmpSubRequestHandler.class.getName(),
                    "run", "[" + Thread.currentThread() +
                      "]:an Snmp error occured during the operation", x);
            }
        }
        catch(Exception x) {
            errorStatus = SnmpDefinitions.snmpRspGenErr ;
            if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)) {
                SNMP_ADAPTOR_LOGGER.logp(Level.FINEST, SnmpSubRequestHandler.class.getName(),
                    "run", "[" + Thread.currentThread() +
                      "]:a generic error occured during the operation", x);
            }
        }
        if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)) {
            SNMP_ADAPTOR_LOGGER.logp(Level.FINER, SnmpSubRequestHandler.class.getName(),
                "run", "[" + Thread.currentThread() + "]:operation completed");
        }
    }
    static final int mapErrorStatusToV1(int errorStatus, int reqPduType) {
        if (errorStatus == SnmpDefinitions.snmpRspNoError)
            return SnmpDefinitions.snmpRspNoError;
        if (errorStatus == SnmpDefinitions.snmpRspGenErr)
            return SnmpDefinitions.snmpRspGenErr;
        if (errorStatus == SnmpDefinitions.snmpRspNoSuchName)
            return SnmpDefinitions.snmpRspNoSuchName;
        if ((errorStatus == SnmpStatusException.noSuchInstance) ||
            (errorStatus == SnmpStatusException.noSuchObject)   ||
            (errorStatus == SnmpDefinitions.snmpRspNoAccess)    ||
            (errorStatus == SnmpDefinitions.snmpRspInconsistentName) ||
            (errorStatus == SnmpDefinitions.snmpRspAuthorizationError)){
            return SnmpDefinitions.snmpRspNoSuchName;
        } else if ((errorStatus ==
                    SnmpDefinitions.snmpRspAuthorizationError)         ||
                   (errorStatus == SnmpDefinitions.snmpRspNotWritable)) {
            if (reqPduType == SnmpDefinitions.pduWalkRequest)
                return SnmpDefinitions.snmpRspReadOnly;
            else
                return SnmpDefinitions.snmpRspNoSuchName;
        } else if ((errorStatus == SnmpDefinitions.snmpRspNoCreation)) {
                return SnmpDefinitions.snmpRspNoSuchName;
        } else if ((errorStatus == SnmpDefinitions.snmpRspWrongType)      ||
                   (errorStatus == SnmpDefinitions.snmpRspWrongLength)    ||
                   (errorStatus == SnmpDefinitions.snmpRspWrongEncoding)  ||
                   (errorStatus == SnmpDefinitions.snmpRspWrongValue)     ||
                   (errorStatus == SnmpDefinitions.snmpRspWrongLength)    ||
                   (errorStatus ==
                    SnmpDefinitions.snmpRspInconsistentValue)) {
            if ((reqPduType == SnmpDefinitions.pduSetRequestPdu) ||
                (reqPduType == SnmpDefinitions.pduWalkRequest))
                return SnmpDefinitions.snmpRspBadValue;
            else
                return SnmpDefinitions.snmpRspNoSuchName;
        } else if ((errorStatus ==
                    SnmpDefinitions.snmpRspResourceUnavailable) ||
                   (errorStatus ==
                    SnmpDefinitions.snmpRspCommitFailed)        ||
                   (errorStatus == SnmpDefinitions.snmpRspUndoFailed)) {
            return SnmpDefinitions.snmpRspGenErr;
        }
        if (errorStatus == SnmpDefinitions.snmpRspTooBig)
            return SnmpDefinitions.snmpRspTooBig;
        if( (errorStatus == SnmpDefinitions.snmpRspBadValue) ||
            (errorStatus == SnmpDefinitions.snmpRspReadOnly)) {
            if ((reqPduType == SnmpDefinitions.pduSetRequestPdu) ||
                (reqPduType == SnmpDefinitions.pduWalkRequest))
                return errorStatus;
            else
                return SnmpDefinitions.snmpRspNoSuchName;
        }
        return SnmpDefinitions.snmpRspGenErr;
    }
    static final int mapErrorStatusToV2(int errorStatus, int reqPduType) {
        if (errorStatus == SnmpDefinitions.snmpRspNoError)
            return SnmpDefinitions.snmpRspNoError;
        if (errorStatus == SnmpDefinitions.snmpRspGenErr)
            return SnmpDefinitions.snmpRspGenErr;
        if (errorStatus == SnmpDefinitions.snmpRspTooBig)
            return SnmpDefinitions.snmpRspTooBig;
        if ((reqPduType != SnmpDefinitions.pduSetRequestPdu) &&
            (reqPduType != SnmpDefinitions.pduWalkRequest)) {
            if(errorStatus == SnmpDefinitions.snmpRspAuthorizationError)
                return errorStatus;
            else
                return SnmpDefinitions.snmpRspGenErr;
        }
        if (errorStatus == SnmpDefinitions.snmpRspNoSuchName)
            return SnmpDefinitions.snmpRspNoAccess;
        if (errorStatus == SnmpDefinitions.snmpRspReadOnly)
                return SnmpDefinitions.snmpRspNotWritable;
        if (errorStatus == SnmpDefinitions.snmpRspBadValue)
            return SnmpDefinitions.snmpRspWrongValue;
        if ((errorStatus == SnmpDefinitions.snmpRspNoAccess) ||
            (errorStatus == SnmpDefinitions.snmpRspInconsistentName) ||
            (errorStatus == SnmpDefinitions.snmpRspAuthorizationError) ||
            (errorStatus == SnmpDefinitions.snmpRspNotWritable) ||
            (errorStatus == SnmpDefinitions.snmpRspNoCreation) ||
            (errorStatus == SnmpDefinitions.snmpRspWrongType) ||
            (errorStatus == SnmpDefinitions.snmpRspWrongLength) ||
            (errorStatus == SnmpDefinitions.snmpRspWrongEncoding) ||
            (errorStatus == SnmpDefinitions.snmpRspWrongValue) ||
            (errorStatus == SnmpDefinitions.snmpRspWrongLength) ||
            (errorStatus == SnmpDefinitions.snmpRspInconsistentValue) ||
            (errorStatus == SnmpDefinitions.snmpRspResourceUnavailable) ||
            (errorStatus == SnmpDefinitions.snmpRspCommitFailed) ||
            (errorStatus == SnmpDefinitions.snmpRspUndoFailed))
            return errorStatus;
        return SnmpDefinitions.snmpRspGenErr;
    }
    static final int mapErrorStatus(int errorStatus,
                                    int protocolVersion,
                                    int reqPduType) {
        if (errorStatus == SnmpDefinitions.snmpRspNoError)
            return SnmpDefinitions.snmpRspNoError;
        if (protocolVersion == SnmpDefinitions.snmpVersionOne)
            return mapErrorStatusToV1(errorStatus,reqPduType);
        if (protocolVersion == SnmpDefinitions.snmpVersionTwo ||
            protocolVersion == SnmpDefinitions.snmpVersionThree)
            return mapErrorStatusToV2(errorStatus,reqPduType);
        return SnmpDefinitions.snmpRspGenErr;
    }
    protected int getErrorStatus() {
        if (errorStatus == snmpRspNoError)
            return snmpRspNoError;
        return mapErrorStatus(errorStatus,version,type);
    }
    protected int getErrorIndex() {
        if  (errorStatus == snmpRspNoError)
            return -1;
        if ((errorIndex == 0) || (errorIndex == -1))
            errorIndex = 1;
        return translation[errorIndex -1];
    }
    protected  void updateRequest(SnmpVarBind var, int pos) {
        int size= varBind.size();
        translation[size]= pos;
        varBind.addElement(var);
    }
    protected void updateResult(SnmpVarBind[] result) {
        if (result == null) return;
        final int max=varBind.size();
        final int len=result.length;
        for(int i= 0; i< max ; i++) {
            final int pos=translation[i];
            if (pos < len) {
                result[pos] =
                    (SnmpVarBind)((NonSyncVector)varBind).elementAtNonSync(i);
            } else {
                if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)) {
                    SNMP_ADAPTOR_LOGGER.logp(Level.FINEST, SnmpSubRequestHandler.class.getName(),
                        "updateResult","Position `"+pos+"' is out of bound...");
                }
            }
        }
    }
    private void init(SnmpEngine engine,
                      SnmpIncomingRequest incRequest) {
        this.incRequest = incRequest;
        this.engine = engine;
    }
    protected int version= snmpVersionOne;
    protected int type= 0;
    protected SnmpMibAgent agent;
    protected int errorStatus= snmpRspNoError;
    protected int errorIndex= -1;
    protected Vector<SnmpVarBind> varBind;
    protected int[] translation;
    protected Object data;
    private   SnmpMibRequest mibRequest = null;
    private   SnmpPdu reqPdu = null;
    @SuppressWarnings("serial")  
    class NonSyncVector<E> extends Vector<E> {
        public NonSyncVector(int size) {
            super(size);
        }
        final void addNonSyncElement(E obj) {
            ensureCapacity(elementCount + 1);
            elementData[elementCount++] = obj;
        }
        @SuppressWarnings("unchecked")  
        final E elementAtNonSync(int index) {
            return (E) elementData[index];
        }
    };
}
