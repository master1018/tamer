final class LongList {
    public static int DEFAULT_CAPACITY = 10;
    public static int DEFAULT_INCREMENT = 10;
    private final int DELTA;
    private int size;
    public  long[] list;
    LongList() {
        this(DEFAULT_CAPACITY,DEFAULT_INCREMENT);
    }
    LongList(int initialCapacity) {
        this(initialCapacity,DEFAULT_INCREMENT);
    }
    LongList(int initialCapacity, int delta) {
        size = 0;
        DELTA = delta;
        list = allocate(initialCapacity);
    }
    public final int size() { return size;}
    public final boolean add(final long o) {
        if (size >= list.length)
            resize();
        list[size++]=o;
        return true;
    }
    public final void add(final int index, final long o) {
        if (index >  size) throw new IndexOutOfBoundsException();
        if (index >= list.length) resize();
        if (index == size) {
            list[size++]=o;
            return;
        }
        java.lang.System.arraycopy(list,index,list,index+1,size-index);
        list[index]=o;
        size++;
    }
    public final void add(final int at,final long[] src, final int from,
                          final int count) {
        if (count <= 0) return;
        if (at > size) throw new IndexOutOfBoundsException();
        ensure(size+count);
        if (at < size) {
            java.lang.System.arraycopy(list,at,list,at+count,size-at);
        }
        java.lang.System.arraycopy(src,from,list,at,count);
        size+=count;
    }
    public final long remove(final int from, final int count) {
        if (count < 1 || from < 0) return -1;
        if (from+count > size) return -1;
        final long o = list[from];
        final int oldsize = size;
        size = size - count;
        if (from == size) return o;
        java.lang.System.arraycopy(list,from+count,list,from,
                                   size-from);
        return o;
    }
    public final long remove(final int index) {
        if (index >= size) return -1;
        final long o = list[index];
        list[index]=0;
        if (index == --size) return o;
        java.lang.System.arraycopy(list,index+1,list,index,
                                   size-index);
        return o;
    }
    public final long[] toArray(long[] a) {
        java.lang.System.arraycopy(list,0,a,0,size);
        return a;
    }
    public final long[] toArray() {
        return toArray(new long[size]);
    }
    private final void resize() {
        final long[] newlist = allocate(list.length + DELTA);
        java.lang.System.arraycopy(list,0,newlist,0,size);
        list = newlist;
    }
    private final void ensure(int length) {
        if (list.length < length) {
            final int min = list.length+DELTA;
            length=(length<min)?min:length;
            final long[] newlist = allocate(length);
            java.lang.System.arraycopy(list,0,newlist,0,size);
            list = newlist;
        }
    }
    private final long[] allocate(final int length) {
        return new long[length];
    }
}
class AcmChecker {
    SnmpAccessControlModel model = null;
    String principal = null;
    int securityLevel = -1;
    int version = -1;
    int pduType = -1;
    int securityModel = -1;
    byte[] contextName = null;
    SnmpEngineImpl engine = null;
    LongList l = null;
    AcmChecker(SnmpMibRequest req) {
        engine = (SnmpEngineImpl) req.getEngine();
        if(engine != null) {
            if(engine.isCheckOidActivated()) {
                try {
                    if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)) {
                        SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,
                                SnmpMib.class.getName(),
                                "AcmChecker(SnmpMibRequest)",
                                "SNMP V3 Access Control to be done");
                    }
                    model = (SnmpAccessControlModel)
                        engine.getAccessControlSubSystem().
                        getModel(SnmpDefinitions.snmpVersionThree);
                    principal = req.getPrincipal();
                    securityLevel = req.getSecurityLevel();
                    pduType = req.getPdu().type;
                    version = req.getRequestPduVersion();
                    securityModel = req.getSecurityModel();
                    contextName = req.getAccessContextName();
                    l = new LongList();
                    if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)) {
                        final StringBuilder strb = new StringBuilder()
                        .append("Will check oid for : principal : ")
                        .append(principal)
                        .append("; securityLevel : ").append(securityLevel)
                        .append("; pduType : ").append(pduType)
                        .append("; version : ").append(version)
                        .append("; securityModel : ").append(securityModel)
                        .append("; contextName : ").append(contextName);
                        SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,
                                SnmpMib.class.getName(),
                                "AcmChecker(SnmpMibRequest)", strb.toString());
                    }
                }catch(SnmpUnknownModelException e) {
                    if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)) {
                        SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,
                                SnmpMib.class.getName(),
                                "AcmChecker(SnmpMibRequest)",
                                "Unknown Model, no ACM check.");
                    }
                }
            }
        }
    }
    void add(int index, long arc) {
        if(model != null)
            l.add(index, arc);
    }
    void remove(int index) {
        if(model != null)
            l.remove(index);
    }
    void add(final int at,final long[] src, final int from,
             final int count) {
        if(model != null)
            l.add(at,src,from,count);
    }
    void remove(final int from, final int count) {
        if(model != null)
            l.remove(from,count);
    }
    void checkCurrentOid() throws SnmpStatusException {
        if(model != null) {
            SnmpOid oid = new SnmpOid(l.toArray());
            if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)) {
                SNMP_ADAPTOR_LOGGER.logp(Level.FINEST, SnmpMib.class.getName(),
                        "checkCurrentOid", "Checking access for : " + oid);
            }
            model.checkAccess(version,
                              principal,
                              securityLevel,
                              pduType,
                              securityModel,
                              contextName,
                              oid);
        }
    }
}
public abstract class SnmpMib extends SnmpMibAgent implements Serializable {
    public SnmpMib() {
        root= new SnmpMibOid();
    }
    protected String getGroupOid(String groupName, String defaultOid) {
        return defaultOid;
    }
    protected ObjectName getGroupObjectName(String name, String oid,
                                            String defaultName)
        throws MalformedObjectNameException {
        return new ObjectName(defaultName);
    }
    protected void registerGroupNode(String groupName,   String groupOid,
                                     ObjectName groupObjName, SnmpMibNode node,
                                     Object group, MBeanServer server)
        throws NotCompliantMBeanException, MBeanRegistrationException,
        InstanceAlreadyExistsException, IllegalAccessException {
        root.registerNode(groupOid,node);
        if (server != null && groupObjName != null && group != null)
            server.registerMBean(group,groupObjName);
    }
    public abstract void registerTableMeta(String name, SnmpMibTable table);
    public abstract SnmpMibTable getRegisteredTableMeta(String name);
    public void get(SnmpMibRequest req) throws SnmpStatusException {
        final int reqType = SnmpDefinitions.pduGetRequestPdu;
        SnmpRequestTree handlers = getHandlers(req,false,false,reqType);
        SnmpRequestTree.Handler h = null;
        SnmpMibNode meta = null;
        if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)) {
            SNMP_ADAPTOR_LOGGER.logp(Level.FINEST, SnmpMib.class.getName(),
                    "get", "Processing handlers for GET... ");
        }
        for (Enumeration eh=handlers.getHandlers();eh.hasMoreElements();) {
            h = (SnmpRequestTree.Handler) eh.nextElement();
            meta = handlers.getMetaNode(h);
            final int depth = handlers.getOidDepth(h);
            for (Enumeration rqs=handlers.getSubRequests(h);
                 rqs.hasMoreElements();) {
                meta.get((SnmpMibSubRequest)rqs.nextElement(),depth);
            }
        }
    }
    public void set(SnmpMibRequest req) throws SnmpStatusException {
        SnmpRequestTree handlers = null;
        if (req instanceof SnmpMibRequestImpl)
            handlers = ((SnmpMibRequestImpl)req).getRequestTree();
        final int reqType = SnmpDefinitions.pduSetRequestPdu;
        if (handlers == null) handlers = getHandlers(req,false,true,reqType);
        handlers.switchCreationFlag(false);
        handlers.setPduType(reqType);
        SnmpRequestTree.Handler h = null;
        SnmpMibNode meta = null;
        if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)) {
            SNMP_ADAPTOR_LOGGER.logp(Level.FINEST, SnmpMib.class.getName(),
                    "set", "Processing handlers for SET... ");
        }
        for (Enumeration eh=handlers.getHandlers();eh.hasMoreElements();) {
            h = (SnmpRequestTree.Handler) eh.nextElement();
            meta = handlers.getMetaNode(h);
            final int depth = handlers.getOidDepth(h);
            for (Enumeration rqs=handlers.getSubRequests(h);
                 rqs.hasMoreElements();) {
                meta.set((SnmpMibSubRequest)rqs.nextElement(),depth);
            }
        }
    }
    public void check(SnmpMibRequest req) throws SnmpStatusException {
        final int reqType = SnmpDefinitions.pduWalkRequest;
        SnmpRequestTree handlers = getHandlers(req,true,true,reqType);
        SnmpRequestTree.Handler h = null;
        SnmpMibNode meta = null;
        if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)) {
            SNMP_ADAPTOR_LOGGER.logp(Level.FINEST, SnmpMib.class.getName(),
                    "check", "Processing handlers for CHECK... ");
        }
        for (Enumeration eh=handlers.getHandlers();eh.hasMoreElements();) {
            h = (SnmpRequestTree.Handler) eh.nextElement();
            meta = handlers.getMetaNode(h);
            final int depth = handlers.getOidDepth(h);
            for (Enumeration rqs=handlers.getSubRequests(h);
                 rqs.hasMoreElements();) {
                meta.check((SnmpMibSubRequest)rqs.nextElement(),depth);
            }
        }
        if (req instanceof SnmpMibRequestImpl) {
            ((SnmpMibRequestImpl)req).setRequestTree(handlers);
        }
    }
    public void getNext(SnmpMibRequest req) throws SnmpStatusException {
        SnmpRequestTree handlers = getGetNextHandlers(req);
        SnmpRequestTree.Handler h = null;
        SnmpMibNode meta = null;
        if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)) {
            SNMP_ADAPTOR_LOGGER.logp(Level.FINEST, SnmpMib.class.getName(),
                    "getNext", "Processing handlers for GET-NEXT... ");
        }
        for (Enumeration eh=handlers.getHandlers();eh.hasMoreElements();) {
            h = (SnmpRequestTree.Handler) eh.nextElement();
            meta = handlers.getMetaNode(h);
            int depth = handlers.getOidDepth(h);
            for (Enumeration rqs=handlers.getSubRequests(h);
                 rqs.hasMoreElements();) {
                meta.get((SnmpMibSubRequest)rqs.nextElement(),depth);
            }
        }
    }
    public void getBulk(SnmpMibRequest req, int nonRepeat, int maxRepeat)
        throws SnmpStatusException {
        getBulkWithGetNext(req, nonRepeat, maxRepeat);
    }
    public long[] getRootOid() {
        if( rootOid == null) {
            Vector<Integer> list= new Vector<Integer>(10);
            root.getRootOid(list);
            rootOid= new long[list.size()];
            int i=0;
            for(Enumeration<Integer> e= list.elements(); e.hasMoreElements(); ) {
                Integer val= e.nextElement();
                rootOid[i++]= val.longValue();
            }
        }
        return rootOid;
    }
    private SnmpRequestTree getHandlers(SnmpMibRequest req,
                                        boolean createflag, boolean atomic,
                                        int type)
        throws SnmpStatusException {
        SnmpRequestTree handlers =
            new SnmpRequestTree(req,createflag,type);
        int index=0;
        SnmpVarBind var = null;
        final int ver= req.getVersion();
        for (Enumeration e= req.getElements(); e.hasMoreElements(); index++) {
            var= (SnmpVarBind) e.nextElement();
            try {
                root.findHandlingNode(var,var.oid.longValue(false),
                                      0,handlers);
            } catch(SnmpStatusException x) {
                if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)) {
                    SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,
                            SnmpMib.class.getName(),
                            "getHandlers",
                            "Couldn't find a handling node for " +
                            var.oid.toString());
                }
                if (ver == SnmpDefinitions.snmpVersionOne) {
                    if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)) {
                        SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,
                                SnmpMib.class.getName(),
                                "getHandlers", "\tV1: Throwing exception");
                    }
                    final SnmpStatusException sse =
                        new SnmpStatusException(x, index + 1);
                    sse.initCause(x);
                    throw sse;
                } else if ((type == SnmpDefinitions.pduWalkRequest)   ||
                           (type == SnmpDefinitions.pduSetRequestPdu)) {
                    final int status =
                        SnmpRequestTree.mapSetException(x.getStatus(),ver);
                    if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)) {
                        SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,
                                SnmpMib.class.getName(),
                                "getHandlers", "\tSET: Throwing exception");
                    }
                    final SnmpStatusException sse =
                        new SnmpStatusException(status, index + 1);
                    sse.initCause(x);
                    throw sse;
                } else if (atomic) {
                    if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)) {
                        SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,
                                SnmpMib.class.getName(),
                                "getHandlers", "\tATOMIC: Throwing exception");
                    }
                    final SnmpStatusException sse =
                        new SnmpStatusException(x, index + 1);
                    sse.initCause(x);
                    throw sse;
                }
                final int status =
                    SnmpRequestTree.mapGetException(x.getStatus(),ver);
                if (status == SnmpStatusException.noSuchInstance) {
                    if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)) {
                        SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,
                                SnmpMib.class.getName(),
                                "getHandlers",
                                "\tGET: Registering noSuchInstance");
                    }
                    var.value= SnmpVarBind.noSuchInstance;
                } else if (status == SnmpStatusException.noSuchObject) {
                    if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)) {
                        SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,
                                SnmpMib.class.getName(),
                                "getHandlers",
                                "\tGET: Registering noSuchObject");
                    }
                        var.value= SnmpVarBind.noSuchObject;
                } else {
                    if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)) {
                        SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,
                                SnmpMib.class.getName(),
                                "getHandlers",
                                "\tGET: Registering global error: " + status);
                    }
                    final SnmpStatusException sse =
                        new SnmpStatusException(status, index + 1);
                    sse.initCause(x);
                    throw sse;
                }
            }
        }
        return handlers;
    }
    private SnmpRequestTree getGetNextHandlers(SnmpMibRequest req)
        throws SnmpStatusException {
        SnmpRequestTree handlers = new
            SnmpRequestTree(req,false,SnmpDefinitions.pduGetNextRequestPdu);
        handlers.setGetNextFlag();
        if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)) {
            SNMP_ADAPTOR_LOGGER.logp(Level.FINEST, SnmpMib.class.getName(),
                    "getGetNextHandlers", "Received MIB request : " + req);
        }
        AcmChecker checker = new AcmChecker(req);
        int index=0;
        SnmpVarBind var = null;
        final int ver= req.getVersion();
        SnmpOid original = null;
        for (Enumeration e= req.getElements(); e.hasMoreElements(); index++) {
            var = (SnmpVarBind) e.nextElement();
            SnmpOid result = null;
            try {
                if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)) {
                    SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,
                            SnmpMib.class.getName(),
                            "getGetNextHandlers", " Next OID of : " + var.oid);
                }
                result = new SnmpOid(root.findNextHandlingNode
                                     (var,var.oid.longValue(false),0,
                                      0,handlers, checker));
                if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)) {
                    SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,
                            SnmpMib.class.getName(),
                            "getGetNextHandlers", " is : " + result);
                }
                var.oid = result;
            } catch(SnmpStatusException x) {
                if (ver == SnmpDefinitions.snmpVersionOne) {
                    if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)) {
                        SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,
                                SnmpMib.class.getName(),
                                "getGetNextHandlers",
                                "\tThrowing exception " + x.toString());
                    }
                    throw new SnmpStatusException(x, index + 1);
                }
                if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)) {
                    SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,
                            SnmpMib.class.getName(),
                            "getGetNextHandlers",
                            "Exception : " + x.getStatus());
                }
                var.setSnmpValue(SnmpVarBind.endOfMibView);
            }
        }
        return handlers;
    }
    protected SnmpMibOid root;
    private transient long[] rootOid= null;
}
