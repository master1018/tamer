public class JvmMemManagerTableMetaImpl extends JvmMemManagerTableMeta {
    private static class JvmMemManagerTableCache
        extends SnmpNamedListTableCache {
        JvmMemManagerTableCache(long validity) {
            this.validity = validity;
        }
        protected String getKey(Object context, List rawDatas,
                                int rank, Object item) {
            if (item == null) return null;
            final String name = ((MemoryManagerMXBean)item).getName();
            log.debug("getKey", "key=" + name);
            return name;
        }
        public SnmpTableHandler getTableHandler() {
            final Map userData = JvmContextFactory.getUserData();
            return getTableDatas(userData);
        }
        protected String getRawDatasKey() {
            return "JvmMemManagerTable.getMemoryManagers";
        }
        protected List   loadRawDatas(Map userData) {
            return ManagementFactory.getMemoryManagerMXBeans();
        }
    }
    protected SnmpTableCache cache;
    public JvmMemManagerTableMetaImpl(SnmpMib myMib,
                                      SnmpStandardObjectServer objserv) {
        super(myMib,objserv);
        this.cache = new
            JvmMemManagerTableCache(((JVM_MANAGEMENT_MIB_IMPL)myMib).
                                    validity());
    }
    protected SnmpOid getNextOid(Object userData)
        throws SnmpStatusException {
        return getNextOid(null,userData);
    }
    protected SnmpOid getNextOid(SnmpOid oid, Object userData)
        throws SnmpStatusException {
        final boolean dbg = log.isDebugOn();
        if (dbg) log.debug("getNextOid", "previous=" + oid);
        SnmpTableHandler handler = getHandler(userData);
        if (handler == null) {
            if (dbg) log.debug("getNextOid", "handler is null!");
            throw new SnmpStatusException(SnmpStatusException.noSuchInstance);
        }
        final SnmpOid next = handler.getNext(oid);
        if (dbg) log.debug("getNextOid", "next=" + next);
        if (next == null)
            throw new SnmpStatusException(SnmpStatusException.noSuchInstance);
        return next;
    }
    protected boolean contains(SnmpOid oid, Object userData) {
        SnmpTableHandler handler = getHandler(userData);
        if (handler == null)
            return false;
        return handler.contains(oid);
    }
    public Object getEntry(SnmpOid oid)
        throws SnmpStatusException {
        if (oid == null)
            throw new SnmpStatusException(SnmpStatusException.noSuchInstance);
        final Map<Object, Object> m = JvmContextFactory.getUserData();
        final long   index    = oid.getOidArc(0);
        final String entryTag = ((m==null)?null:("JvmMemManagerTable.entry." +
                                                 index));
        if (m != null) {
            final Object entry = m.get(entryTag);
            if (entry != null) return entry;
        }
        SnmpTableHandler handler = getHandler(m);
        if (handler == null)
            throw new SnmpStatusException(SnmpStatusException.noSuchInstance);
        final Object data = handler.getData(oid);
        if (data == null)
            throw new SnmpStatusException(SnmpStatusException.noSuchInstance);
        final Object entry =
            new JvmMemManagerEntryImpl((MemoryManagerMXBean)data,(int)index);
        if (m != null && entry != null) {
            m.put(entryTag,entry);
        }
        return entry;
    }
    protected SnmpTableHandler getHandler(Object userData) {
        final Map<Object, Object> m;
        if (userData instanceof Map) m=Util.cast(userData);
        else m=null;
        if (m != null) {
            final SnmpTableHandler handler =
                (SnmpTableHandler)m.get("JvmMemManagerTable.handler");
            if (handler != null) return handler;
        }
        final SnmpTableHandler handler = cache.getTableHandler();
        if (m != null && handler != null )
            m.put("JvmMemManagerTable.handler",handler);
        return handler;
    }
    static final MibLogger log =
        new MibLogger(JvmMemManagerTableMetaImpl.class);
}
