class SnmpSubBulkRequestHandler extends SnmpSubRequestHandler {
    private SnmpAdaptorServer server = null;
    protected SnmpSubBulkRequestHandler(SnmpEngine engine,
                                        SnmpAdaptorServer server,
                                        SnmpIncomingRequest incRequest,
                                        SnmpMibAgent agent,
                                        SnmpPdu req,
                                        int nonRepeat,
                                        int maxRepeat,
                                        int R) {
        super(engine, incRequest, agent, req);
        init(server, req, nonRepeat, maxRepeat, R);
    }
    protected SnmpSubBulkRequestHandler(SnmpAdaptorServer server,
                                        SnmpMibAgent agent,
                                        SnmpPdu req,
                                        int nonRepeat,
                                        int maxRepeat,
                                        int R) {
        super(agent, req);
        init(server, req, nonRepeat, maxRepeat, R);
    }
    public void run() {
        size= varBind.size();
        try {
            final ThreadContext oldContext =
                ThreadContext.push("SnmpUserData",data);
            try {
                if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)) {
                    SNMP_ADAPTOR_LOGGER.logp(Level.FINER, SnmpSubRequestHandler.class.getName(),
                        "run", "[" + Thread.currentThread() +
                        "]:getBulk operation on " + agent.getMibName());
                }
                agent.getBulk(createMibRequest(varBind,version,data),
                              nonRepeat, maxRepeat);
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
                "run", "[" + Thread.currentThread() +
                  "]:operation completed");
        }
    }
    private void init(SnmpAdaptorServer server,
                      SnmpPdu req,
                      int nonRepeat,
                      int maxRepeat,
                      int R) {
        this.server = server;
        this.nonRepeat= nonRepeat;
        this.maxRepeat= maxRepeat;
        this.globalR= R;
        final int max= translation.length;
        final SnmpVarBind[] list= req.varBindList;
        final NonSyncVector<SnmpVarBind> nonSyncVarBind =
                ((NonSyncVector<SnmpVarBind>)varBind);
        for(int i=0; i < max; i++) {
            translation[i]= i;
            final SnmpVarBind newVarBind =
                new SnmpVarBind(list[i].oid, list[i].value);
            nonSyncVarBind.addNonSyncElement(newVarBind);
        }
    }
    private SnmpVarBind findVarBind(SnmpVarBind element,
                                    SnmpVarBind result) {
        if (element == null) return null;
        if (result.oid == null) {
             return element;
        }
        if (element.value == SnmpVarBind.endOfMibView) return result;
        if (result.value == SnmpVarBind.endOfMibView) return element;
        final SnmpValue val = result.value;
        int comp = element.oid.compareTo(result.oid);
        if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)) {
            SNMP_ADAPTOR_LOGGER.logp(Level.FINER, SnmpSubRequestHandler.class.getName(),
                "findVarBind","Comparing OID element : " + element.oid +
                  " with result : " + result.oid);
            SNMP_ADAPTOR_LOGGER.logp(Level.FINER, SnmpSubRequestHandler.class.getName(),
                "findVarBind","Values element : " + element.value +
                  " result : " + result.value);
        }
        if (comp < 0) {
            return element;
        }
        else {
            if(comp == 0) {
                if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)) {
                    SNMP_ADAPTOR_LOGGER.logp(Level.FINER, SnmpSubRequestHandler.class.getName(),
                        "findVarBind"," oid overlapping. Oid : " +
                          element.oid + "value :" + element.value);
                    SNMP_ADAPTOR_LOGGER.logp(Level.FINER, SnmpSubRequestHandler.class.getName(),
                         "findVarBind","Already present varBind : " +
                          result);
                }
                SnmpOid oid = result.oid;
                SnmpMibAgent deeperAgent = server.getAgentMib(oid);
                if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)) {
                    SNMP_ADAPTOR_LOGGER.logp(Level.FINER, SnmpSubRequestHandler.class.getName(),
                        "findVarBind","Deeper agent : " + deeperAgent);
                }
                if(deeperAgent == agent) {
                    if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)) {
                        SNMP_ADAPTOR_LOGGER.logp(Level.FINER, SnmpSubRequestHandler.class.getName(),
                            "findVarBind","The current agent is the deeper one. Update the value with the current one");
                    }
                    return element;
                } else {
                    if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)) {
                        SNMP_ADAPTOR_LOGGER.logp(Level.FINER, SnmpSubRequestHandler.class.getName(),
                            "findVarBind","The current agent is not the deeper one. return the previous one.");
                    }
                    return result;
                }
            }
            else {
                if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)) {
                    SNMP_ADAPTOR_LOGGER.logp(Level.FINER, SnmpSubRequestHandler.class.getName(),
                        "findVarBind","The right varBind is the already present one");
                }
                return result;
            }
        }
    }
    protected void updateResult(SnmpVarBind[] result) {
        final Enumeration e= varBind.elements();
        final int max= result.length;
        for(int i=0; i < size; i++) {
            if (e.hasMoreElements() == false)
                return;
            final int pos=translation[i];
            if (pos >= max) {
                if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)) {
                    SNMP_ADAPTOR_LOGGER.logp(Level.FINEST, SnmpSubRequestHandler.class.getName(),
                        "updateResult","Position '"+pos+"' is out of bound...");
                }
                continue;
            }
            final SnmpVarBind element= (SnmpVarBind) e.nextElement();
            if (element == null) continue;
            if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)) {
                SNMP_ADAPTOR_LOGGER.logp(Level.FINER, SnmpSubRequestHandler.class.getName(),
                    "updateResult","Non repeaters Current element : " +
                      element + " from agent : " + agent);
            }
            final SnmpVarBind res = findVarBind(element,result[pos]);
            if(res == null) continue;
            result[pos] = res;
        }
        int localR= size - nonRepeat;
        for (int i = 2 ; i <= maxRepeat ; i++) {
            for (int r = 0 ; r < localR ; r++) {
                final int pos = (i-1)* globalR + translation[nonRepeat + r] ;
                if (pos >= max)
                    return;
                if (e.hasMoreElements() ==false)
                    return;
                final SnmpVarBind element= (SnmpVarBind) e.nextElement();
                if (element == null) continue;
                if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)) {
                    SNMP_ADAPTOR_LOGGER.logp(Level.FINER, SnmpSubRequestHandler.class.getName(),
                        "updateResult","Repeaters Current element : " +
                          element + " from agent : " + agent);
                }
                final SnmpVarBind res = findVarBind(element, result[pos]);
                if(res == null) continue;
                result[pos] = res;
            }
        }
    }
    protected int nonRepeat=0;
    protected int maxRepeat=0;
    protected int globalR=0;
    protected int size=0;
}
