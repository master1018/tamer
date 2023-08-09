public abstract class SnmpMibAgent
    implements SnmpMibAgentMBean, MBeanRegistration, Serializable {
    public SnmpMibAgent() {
    }
    public abstract void init() throws IllegalAccessException;
    public abstract ObjectName preRegister(MBeanServer server,
                                           ObjectName name)
        throws java.lang.Exception;
    public void postRegister (Boolean registrationDone) {
    }
    public void preDeregister() throws java.lang.Exception {
    }
    public void postDeregister() {
    }
    public abstract void get(SnmpMibRequest req)
        throws SnmpStatusException;
    public abstract void getNext(SnmpMibRequest req)
        throws SnmpStatusException;
    public abstract void getBulk(SnmpMibRequest req, int nonRepeat,
                                 int maxRepeat)
        throws SnmpStatusException;
    public abstract void set(SnmpMibRequest req)
        throws SnmpStatusException;
    public abstract void check(SnmpMibRequest req)
        throws SnmpStatusException;
    public abstract long[] getRootOid();
    public MBeanServer getMBeanServer() {
        return server;
    }
    public SnmpMibHandler getSnmpAdaptor() {
        return adaptor;
    }
    public void setSnmpAdaptor(SnmpMibHandler stack) {
        if (adaptor != null) {
            adaptor.removeMib(this);
        }
        adaptor = stack;
        if (adaptor != null) {
            adaptor.addMib(this);
        }
    }
    public void setSnmpAdaptor(SnmpMibHandler stack, SnmpOid[] oids) {
        if (adaptor != null) {
            adaptor.removeMib(this);
        }
        adaptor = stack;
        if (adaptor != null) {
            adaptor.addMib(this, oids);
        }
    }
    public void setSnmpAdaptor(SnmpMibHandler stack, String contextName) {
        if (adaptor != null) {
            adaptor.removeMib(this, contextName);
        }
        adaptor = stack;
        if (adaptor != null) {
            adaptor.addMib(this, contextName);
        }
    }
    public void setSnmpAdaptor(SnmpMibHandler stack,
                               String contextName,
                               SnmpOid[] oids) {
        if (adaptor != null) {
            adaptor.removeMib(this, contextName);
        }
        adaptor = stack;
        if (adaptor != null) {
            adaptor.addMib(this, contextName, oids);
        }
    }
    public ObjectName getSnmpAdaptorName() {
        return adaptorName;
    }
    public void setSnmpAdaptorName(ObjectName name)
        throws InstanceNotFoundException, ServiceNotFoundException {
        if (server == null) {
            throw new ServiceNotFoundException(mibName + " is not registered in the MBean server");
        }
        if (adaptor != null) {
            adaptor.removeMib(this);
        }
        Object[] params = {this};
        String[] signature = {"com.sun.jmx.snmp.agent.SnmpMibAgent"};
        try {
            adaptor = (SnmpMibHandler)(server.invoke(name, "addMib", params,
                                                     signature));
        } catch (InstanceNotFoundException e) {
            throw new InstanceNotFoundException(name.toString());
        } catch (ReflectionException e) {
            throw new ServiceNotFoundException(name.toString());
        } catch (MBeanException e) {
        }
        adaptorName = name;
    }
    public void setSnmpAdaptorName(ObjectName name, SnmpOid[] oids)
        throws InstanceNotFoundException, ServiceNotFoundException {
        if (server == null) {
            throw new ServiceNotFoundException(mibName + " is not registered in the MBean server");
        }
        if (adaptor != null) {
            adaptor.removeMib(this);
        }
        Object[] params = {this, oids};
        String[] signature = {"com.sun.jmx.snmp.agent.SnmpMibAgent",
        oids.getClass().getName()};
        try {
            adaptor = (SnmpMibHandler)(server.invoke(name, "addMib", params,
                                                     signature));
        } catch (InstanceNotFoundException e) {
            throw new InstanceNotFoundException(name.toString());
        } catch (ReflectionException e) {
            throw new ServiceNotFoundException(name.toString());
        } catch (MBeanException e) {
        }
        adaptorName = name;
    }
    public void setSnmpAdaptorName(ObjectName name, String contextName)
        throws InstanceNotFoundException, ServiceNotFoundException {
        if (server == null) {
            throw new ServiceNotFoundException(mibName + " is not registered in the MBean server");
        }
        if (adaptor != null) {
            adaptor.removeMib(this, contextName);
        }
        Object[] params = {this, contextName};
        String[] signature = {"com.sun.jmx.snmp.agent.SnmpMibAgent", "java.lang.String"};
        try {
            adaptor = (SnmpMibHandler)(server.invoke(name, "addMib", params,
                                                     signature));
        } catch (InstanceNotFoundException e) {
            throw new InstanceNotFoundException(name.toString());
        } catch (ReflectionException e) {
            throw new ServiceNotFoundException(name.toString());
        } catch (MBeanException e) {
        }
        adaptorName = name;
    }
    public void setSnmpAdaptorName(ObjectName name,
                                   String contextName, SnmpOid[] oids)
        throws InstanceNotFoundException, ServiceNotFoundException {
        if (server == null) {
            throw new ServiceNotFoundException(mibName + " is not registered in the MBean server");
        }
        if (adaptor != null) {
            adaptor.removeMib(this, contextName);
        }
        Object[] params = {this, contextName, oids};
        String[] signature = {"com.sun.jmx.snmp.agent.SnmpMibAgent", "java.lang.String", oids.getClass().getName()};
        try {
            adaptor = (SnmpMibHandler)(server.invoke(name, "addMib", params,
                                                     signature));
        } catch (InstanceNotFoundException e) {
            throw new InstanceNotFoundException(name.toString());
        } catch (ReflectionException e) {
            throw new ServiceNotFoundException(name.toString());
        } catch (MBeanException e) {
        }
        adaptorName = name;
    }
    public boolean getBindingState() {
        if (adaptor == null)
            return false;
        else
            return true;
    }
    public String getMibName() {
        return mibName;
    }
    public static SnmpMibRequest newMibRequest(SnmpPdu reqPdu,
                                               Vector<SnmpVarBind> vblist,
                                               int version,
                                               Object userData)
    {
        return new SnmpMibRequestImpl(null,
                                      reqPdu,
                                      vblist,
                                      version,
                                      userData,
                                      null,
                                      SnmpDefinitions.noAuthNoPriv,
                                      getSecurityModel(version),
                                      null,null);
    }
    public static SnmpMibRequest newMibRequest(SnmpEngine engine,
                                               SnmpPdu reqPdu,
                                               Vector<SnmpVarBind> vblist,
                                               int version,
                                               Object userData,
                                               String principal,
                                               int securityLevel,
                                               int securityModel,
                                               byte[] contextName,
                                               byte[] accessContextName) {
        return new SnmpMibRequestImpl(engine,
                                      reqPdu,
                                      vblist,
                                      version,
                                      userData,
                                      principal,
                                      securityLevel,
                                      securityModel,
                                      contextName,
                                      accessContextName);
    }
    void getBulkWithGetNext(SnmpMibRequest req, int nonRepeat, int maxRepeat)
        throws SnmpStatusException {
        final Vector<SnmpVarBind> list = req.getSubList();
        final int L = list.size() ;
        final int N = Math.max(Math.min(nonRepeat, L), 0) ;
        final int M = Math.max(maxRepeat, 0) ;
        final int R = L - N ;
        if (L != 0) {
            getNext(req);
            Vector<SnmpVarBind> repeaters= splitFrom(list, N);
            SnmpMibRequestImpl repeatedReq =
                new SnmpMibRequestImpl(req.getEngine(),
                                       req.getPdu(),
                                       repeaters,
                                       SnmpDefinitions.snmpVersionTwo,
                                       req.getUserData(),
                                       req.getPrincipal(),
                                       req.getSecurityLevel(),
                                       req.getSecurityModel(),
                                       req.getContextName(),
                                       req.getAccessContextName());
            for (int i = 2 ; i <= M ; i++) {
                getNext(repeatedReq);
                concatVector(req, repeaters);
            }
        }
    }
    private Vector<SnmpVarBind> splitFrom(Vector<SnmpVarBind> original, int limit) {
        int max= original.size();
        Vector<SnmpVarBind> result= new Vector<SnmpVarBind>(max - limit);
        int i= limit;
        for(Enumeration<SnmpVarBind> e= original.elements(); e.hasMoreElements(); --i) {
            SnmpVarBind var= e.nextElement();
            if (i >0)
                continue;
            result.addElement(new SnmpVarBind(var.oid, var.value));
        }
        return result;
    }
    private void concatVector(SnmpMibRequest req, Vector source) {
        for(Enumeration e= source.elements(); e.hasMoreElements(); ) {
            SnmpVarBind var= (SnmpVarBind) e.nextElement();
            req.addVarBind(new SnmpVarBind(var.oid, var.value));
        }
    }
    private void concatVector(Vector<SnmpVarBind> target, Vector<SnmpVarBind> source) {
        for(Enumeration<SnmpVarBind> e= source.elements(); e.hasMoreElements(); ) {
            SnmpVarBind var= e.nextElement();
            target.addElement(new SnmpVarBind(var.oid, var.value));
        }
    }
    private static int getSecurityModel(int version) {
        switch(version) {
        case SnmpDefinitions.snmpVersionOne:
            return SnmpDefinitions.snmpV1SecurityModel;
        default:
            return SnmpDefinitions.snmpV2SecurityModel;
        }
    }
    protected String mibName;
    protected MBeanServer server;
    private ObjectName adaptorName;
    private transient SnmpMibHandler adaptor;
}
