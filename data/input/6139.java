final class SnmpRequestTree {
    SnmpRequestTree(SnmpMibRequest req, boolean creationflag, int pdutype) {
        this.request = req;
        this.version  = req.getVersion();
        this.creationflag = creationflag;
        this.hashtable = new Hashtable<Object, Handler>();
        setPduType(pdutype);
    }
    public static int mapSetException(int errorStatus, int version)
        throws SnmpStatusException {
        final int errorCode = errorStatus;
        if (version == SnmpDefinitions.snmpVersionOne)
            return errorCode;
        int mappedErrorCode = errorCode;
        if (errorCode == SnmpStatusException.noSuchObject)
            mappedErrorCode = SnmpStatusException.snmpRspNotWritable;
        else if (errorCode == SnmpStatusException.noSuchInstance)
            mappedErrorCode = SnmpStatusException.snmpRspNotWritable;
        return mappedErrorCode;
    }
    public static int mapGetException(int errorStatus, int version)
        throws SnmpStatusException {
        final int errorCode = errorStatus;
        if (version == SnmpDefinitions.snmpVersionOne)
            return errorCode;
        int mappedErrorCode = errorCode;
        if (errorCode ==
            SnmpStatusException.noSuchObject)
            mappedErrorCode = errorCode;
        else if (errorCode ==
                 SnmpStatusException.noSuchInstance)
            mappedErrorCode = errorCode;
        else if (errorCode ==
                 SnmpStatusException.noAccess)
            mappedErrorCode = SnmpStatusException.noSuchInstance;
        else if (errorCode == SnmpStatusException.snmpRspInconsistentName)
            mappedErrorCode = SnmpStatusException.noSuchInstance;
        else if ((errorCode >= SnmpStatusException.snmpRspWrongType) &&
                 (errorCode <= SnmpStatusException.snmpRspInconsistentValue))
            mappedErrorCode = SnmpStatusException.noSuchInstance;
        else if (errorCode == SnmpStatusException.readOnly)
            mappedErrorCode = SnmpStatusException.noSuchInstance;
        else if (errorCode != SnmpStatusException.snmpRspAuthorizationError &&
                 errorCode != SnmpStatusException.snmpRspGenErr)
            mappedErrorCode = SnmpStatusException.noSuchObject;
        return mappedErrorCode;
    }
    static final class Enum implements Enumeration {
        Enum(SnmpRequestTree hlist,Handler h) {
            handler = h;
            this.hlist = hlist;
            size = h.getSubReqCount();
        }
        private final Handler handler;
        private final SnmpRequestTree hlist;
        private int   entry = 0;
        private int   iter  = 0;
        private int   size  = 0;
        public boolean hasMoreElements() {
            return iter < size;
        }
        public Object nextElement() throws NoSuchElementException  {
            if (iter == 0) {
                if (handler.sublist != null) {
                    iter++;
                    return hlist.getSubRequest(handler);
                }
            }
            iter ++;
            if (iter > size) throw new NoSuchElementException();
            Object result = hlist.getSubRequest(handler,entry);
            entry++;
            return result;
        }
    }
    static final class SnmpMibSubRequestImpl implements SnmpMibSubRequest {
        SnmpMibSubRequestImpl(SnmpMibRequest global, Vector<SnmpVarBind> sublist,
                           SnmpOid entryoid, boolean isnew,
                           boolean getnextflag, SnmpVarBind rs) {
            this.global = global;
            varbinds           = sublist;
            this.version       = global.getVersion();
            this.entryoid      = entryoid;
            this.isnew         = isnew;
            this.getnextflag   = getnextflag;
            this.statusvb      = rs;
        }
        final private Vector<SnmpVarBind> varbinds;
        final private SnmpMibRequest global;
        final private int            version;
        final private boolean        isnew;
        final private SnmpOid        entryoid;
        final private boolean        getnextflag;
        final private SnmpVarBind    statusvb;
        public Enumeration getElements() {
            return varbinds.elements();
        }
        public Vector<SnmpVarBind> getSubList() {
            return varbinds;
        }
        public final int getSize()  {
            if (varbinds == null) return 0;
            return varbinds.size();
        }
        public void addVarBind(SnmpVarBind varbind) {
            varbinds.addElement(varbind);
            global.addVarBind(varbind);
        }
        public boolean isNewEntry() {
            return isnew;
        }
        public SnmpOid getEntryOid() {
            return entryoid;
        }
        public int getVarIndex(SnmpVarBind varbind) {
            if (varbind == null) return 0;
            return global.getVarIndex(varbind);
        }
        public Object getUserData() { return global.getUserData(); }
        public void registerGetException(SnmpVarBind var,
                                         SnmpStatusException exception)
            throws SnmpStatusException {
            if (version == SnmpDefinitions.snmpVersionOne)
                throw new SnmpStatusException(exception, getVarIndex(var)+1);
            if (var == null)
                throw exception;
            if (getnextflag) {
                var.value = SnmpVarBind.endOfMibView;
                return;
            }
            final int errorCode = mapGetException(exception.getStatus(),
                                                  version);
            if (errorCode ==
                SnmpStatusException.noSuchObject)
                var.value= SnmpVarBind.noSuchObject;
            else if (errorCode ==
                     SnmpStatusException.noSuchInstance)
                var.value= SnmpVarBind.noSuchInstance;
            else
                throw new SnmpStatusException(errorCode, getVarIndex(var)+1);
        }
        public void registerSetException(SnmpVarBind var,
                                         SnmpStatusException exception)
            throws SnmpStatusException {
            if (version == SnmpDefinitions.snmpVersionOne)
                throw new SnmpStatusException(exception, getVarIndex(var)+1);
            throw new SnmpStatusException(SnmpDefinitions.snmpRspUndoFailed,
                                          getVarIndex(var)+1);
        }
        public void registerCheckException(SnmpVarBind var,
                                           SnmpStatusException exception)
            throws SnmpStatusException {
            final int errorCode = exception.getStatus();
            final int mappedErrorCode = mapSetException(errorCode,
                                                        version);
            if (errorCode != mappedErrorCode)
                throw new
                    SnmpStatusException(mappedErrorCode, getVarIndex(var)+1);
            else
                throw new SnmpStatusException(exception, getVarIndex(var)+1);
        }
        public int getVersion() {
            return version;
        }
        public SnmpVarBind getRowStatusVarBind() {
            return statusvb;
        }
        public SnmpPdu getPdu() {
            return global.getPdu();
        }
        public int getRequestPduVersion() {
            return global.getRequestPduVersion();
        }
        public SnmpEngine getEngine() {
            return global.getEngine();
        }
        public String getPrincipal() {
            return global.getPrincipal();
        }
        public int getSecurityLevel() {
            return global.getSecurityLevel();
        }
        public int getSecurityModel() {
            return global.getSecurityModel();
        }
        public byte[] getContextName() {
            return global.getContextName();
        }
        public byte[] getAccessContextName() {
            return global.getAccessContextName();
        }
    }
    static final class Handler {
        SnmpMibNode meta;       
        int         depth;      
        Vector<SnmpVarBind> sublist; 
        SnmpOid[]     entryoids  = null; 
        Vector<SnmpVarBind>[] entrylists = null; 
        boolean[]     isentrynew = null; 
        SnmpVarBind[] rowstatus  = null; 
        int entrycount = 0;
        int entrysize  = 0;
        final int type; 
        final private static int Delta = 10;
        public Handler(int pduType) {
            this.type = pduType;
        }
        public void addVarbind(SnmpVarBind varbind) {
            if (sublist == null) sublist = new Vector<SnmpVarBind>();
            sublist.addElement(varbind);
        }
        @SuppressWarnings("unchecked")
        void add(int pos,SnmpOid oid, Vector<SnmpVarBind> v, boolean isnew,
                 SnmpVarBind statusvb) {
            if (entryoids == null) {
                entryoids  = new SnmpOid[Delta];
                entrylists = new Vector[Delta];
                isentrynew = new boolean[Delta];
                rowstatus  = new SnmpVarBind[Delta];
                entrysize  = Delta;
                pos = 0;
            } else if (pos >= entrysize || entrycount == entrysize) {
                SnmpOid[]     olde = entryoids;
                Vector[]      oldl = entrylists;
                boolean[]     oldn = isentrynew;
                SnmpVarBind[] oldr = rowstatus;
                entrysize += Delta;
                entryoids =  new SnmpOid[entrysize];
                entrylists = new Vector[entrysize];
                isentrynew = new boolean[entrysize];
                rowstatus  = new SnmpVarBind[entrysize];
                if (pos > entrycount) pos = entrycount;
                if (pos < 0) pos = 0;
                final int l1 = pos;
                final int l2 = entrycount - pos;
                if (l1 > 0) {
                    java.lang.System.arraycopy(olde,0,entryoids,
                                               0,l1);
                    java.lang.System.arraycopy(oldl,0,entrylists,
                                               0,l1);
                    java.lang.System.arraycopy(oldn,0,isentrynew,
                                               0,l1);
                    java.lang.System.arraycopy(oldr,0,rowstatus,
                                               0,l1);
                }
                if (l2 > 0) {
                    final int l3 = l1+1;
                    java.lang.System.arraycopy(olde,l1,entryoids,
                                               l3,l2);
                    java.lang.System.arraycopy(oldl,l1,entrylists,
                                               l3,l2);
                    java.lang.System.arraycopy(oldn,l1,isentrynew,
                                               l3,l2);
                    java.lang.System.arraycopy(oldr,l1,rowstatus,
                                               l3,l2);
                }
            } else if (pos < entrycount) {
                final int l1 = pos+1;
                final int l2 = entrycount - pos;
                java.lang.System.arraycopy(entryoids,pos,entryoids,
                                           l1,l2);
                java.lang.System.arraycopy(entrylists,pos,entrylists,
                                           l1,l2);
                java.lang.System.arraycopy(isentrynew,pos,isentrynew,
                                           l1,l2);
                java.lang.System.arraycopy(rowstatus,pos,rowstatus,
                                           l1,l2);
            }
            entryoids[pos]  = oid;
            entrylists[pos] = v;
            isentrynew[pos] = isnew;
            rowstatus[pos]  = statusvb;
            entrycount++;
        }
        public void addVarbind(SnmpVarBind varbind, SnmpOid entryoid,
                               boolean isnew, SnmpVarBind statusvb)
            throws SnmpStatusException {
            Vector<SnmpVarBind> v = null;
            SnmpVarBind rs = statusvb;
            if (entryoids == null) {
                v = new Vector<SnmpVarBind>();
                add(0,entryoid,v,isnew,rs);
            } else {
                final int pos =
                    getInsertionPoint(entryoids,entrycount,entryoid);
                if (pos > -1 && pos < entrycount &&
                    entryoid.compareTo(entryoids[pos]) == 0) {
                    v  = entrylists[pos];
                    rs = rowstatus[pos];
                } else {
                    v = new Vector<SnmpVarBind>();
                    add(pos,entryoid,v,isnew,rs);
                }
                if (statusvb != null) {
                    if ((rs != null) && (rs != statusvb) &&
                        ((type == SnmpDefinitions.pduWalkRequest) ||
                         (type == SnmpDefinitions.pduSetRequestPdu))) {
                        throw new SnmpStatusException(
                              SnmpStatusException.snmpRspInconsistentValue);
                    }
                    rowstatus[pos] = statusvb;
                }
            }
            if (statusvb != varbind)
                v.addElement(varbind);
        }
        public int getSubReqCount() {
            int count = 0;
            if (sublist != null) count++;
            if (entryoids != null) count += entrycount;
            return count;
        }
        public Vector<SnmpVarBind> getSubList() {
            return sublist;
        }
        public int getEntryPos(SnmpOid entryoid) {
            return findOid(entryoids,entrycount,entryoid);
        }
        public SnmpOid getEntryOid(int pos) {
            if (entryoids == null) return null;
            if (pos == -1 || pos >= entrycount ) return null;
            return entryoids[pos];
        }
        public boolean isNewEntry(int pos) {
            if (entryoids == null) return false;
            if (pos == -1 || pos >= entrycount ) return false;
            return isentrynew[pos];
        }
        public SnmpVarBind getRowStatusVarBind(int pos) {
            if (entryoids == null) return null;
            if (pos == -1 || pos >= entrycount ) return null;
            return rowstatus[pos];
        }
        public Vector<SnmpVarBind> getEntrySubList(int pos) {
            if (entrylists == null) return null;
            if (pos == -1 || pos >= entrycount ) return null;
            return entrylists[pos];
        }
        public Iterator<SnmpOid> getEntryOids() {
            if (entryoids == null) return null;
            return Arrays.asList(entryoids).iterator();
        }
        public int getEntryCount() {
            if (entryoids == null) return 0;
            return entrycount;
        }
    }
    public Object getUserData() { return request.getUserData(); }
    public boolean isCreationAllowed() {
        return creationflag;
    }
    public boolean isSetRequest() {
        return setreqflag;
    }
    public int getVersion() {
        return version;
    }
    public int getRequestPduVersion() {
        return request.getRequestPduVersion();
    }
    public SnmpMibNode getMetaNode(Handler handler) {
        return handler.meta;
    }
    public int getOidDepth(Handler handler) {
        return handler.depth;
    }
    public Enumeration getSubRequests(Handler handler) {
        return new Enum(this,handler);
    }
    public Enumeration getHandlers() {
        return hashtable.elements();
    }
    public void add(SnmpMibNode meta, int depth, SnmpVarBind varbind)
        throws SnmpStatusException {
        registerNode(meta,depth,null,varbind,false,null);
    }
    public void add(SnmpMibNode meta, int depth, SnmpOid entryoid,
                    SnmpVarBind varbind, boolean isnew)
        throws SnmpStatusException {
        registerNode(meta,depth,entryoid,varbind,isnew,null);
    }
    public void add(SnmpMibNode meta, int depth, SnmpOid entryoid,
                    SnmpVarBind varbind, boolean isnew,
                    SnmpVarBind statusvb)
        throws SnmpStatusException {
        registerNode(meta,depth,entryoid,varbind,isnew,statusvb);
    }
    void setPduType(int pduType) {
        type = pduType;
        setreqflag = ((pduType == SnmpDefinitions.pduWalkRequest) ||
            (pduType == SnmpDefinitions.pduSetRequestPdu));
    }
    void setGetNextFlag() {
        getnextflag = true;
    }
    void switchCreationFlag(boolean flag) {
        creationflag = flag;
    }
    SnmpMibSubRequest getSubRequest(Handler handler) {
        if (handler == null) return null;
        return new SnmpMibSubRequestImpl(request,handler.getSubList(),
                                      null,false,getnextflag,null);
    }
    SnmpMibSubRequest getSubRequest(Handler handler, SnmpOid oid) {
        if (handler == null) return null;
        final int pos = handler.getEntryPos(oid);
        if (pos == -1) return null;
        return new SnmpMibSubRequestImpl(request,
                                         handler.getEntrySubList(pos),
                                         handler.getEntryOid(pos),
                                         handler.isNewEntry(pos),
                                         getnextflag,
                                         handler.getRowStatusVarBind(pos));
    }
    SnmpMibSubRequest getSubRequest(Handler handler, int entry) {
        if (handler == null) return null;
        return new
            SnmpMibSubRequestImpl(request,handler.getEntrySubList(entry),
                                  handler.getEntryOid(entry),
                                  handler.isNewEntry(entry),getnextflag,
                                  handler.getRowStatusVarBind(entry));
    }
    private void put(Object key, Handler handler) {
        if (handler == null) return;
        if (key == null) return;
        if (hashtable == null) hashtable = new Hashtable<Object, Handler>();
        hashtable.put(key,handler);
    }
    private Handler get(Object key) {
        if (key == null) return null;
        if (hashtable == null) return null;
        return hashtable.get(key);
    }
    private static int findOid(SnmpOid[] oids, int count, SnmpOid oid) {
        final int size = count;
        int low= 0;
        int max= size - 1;
        int curr= low + (max-low)/2;
        while (low <= max) {
            final SnmpOid pos = oids[curr];
            final int comp = oid.compareTo(pos);
            if (comp == 0)
                return curr;
            if (oid.equals(pos)) {
                return curr;
            }
            if (comp > 0) {
                low = curr + 1;
            } else {
                max = curr - 1;
            }
            curr = low + (max-low)/2;
        }
        return -1;
    }
    private static int getInsertionPoint(SnmpOid[] oids, int count,
                                         SnmpOid oid) {
        final SnmpOid[] localoids = oids;
        final int size = count;
        int low= 0;
        int max= size - 1;
        int curr= low + (max-low)/2;
        while (low <= max) {
            final SnmpOid pos = localoids[curr];
            final int comp= oid.compareTo(pos);
            if (comp == 0)
                return curr;
            if (comp>0) {
                low= curr +1;
            } else {
                max= curr -1;
            }
            curr= low + (max-low)/2;
        }
        return curr;
    }
    private void registerNode(SnmpMibNode meta, int depth, SnmpOid entryoid,
                              SnmpVarBind varbind, boolean isnew,
                              SnmpVarBind statusvb)
        throws SnmpStatusException {
        if (meta == null) {
            SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,
                    SnmpRequestTree.class.getName(),
                    "registerNode", "meta-node is null!");
            return;
        }
        if (varbind == null) {
            SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,
                    SnmpRequestTree.class.getName(),
                    "registerNode", "varbind is null!");
            return ;
        }
        final Object key = meta;
        Handler handler = get(key);
        if (handler == null) {
            handler = new Handler(type);
            handler.meta  = meta;
            handler.depth = depth;
            put(key,handler);
        }
        if (entryoid == null)
            handler.addVarbind(varbind);
        else
            handler.addVarbind(varbind,entryoid,isnew,statusvb);
        return ;
    }
    private Hashtable<Object, Handler> hashtable = null;
    private SnmpMibRequest request = null;   
    private int       version      = 0;      
    private boolean   creationflag = false;  
    private boolean   getnextflag  = false;  
    private int       type         = 0;      
    private boolean   setreqflag   = false;  
}
