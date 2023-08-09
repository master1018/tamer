public class JvmRTBootClassPathTableMetaImpl
    extends JvmRTBootClassPathTableMeta {
    private SnmpTableCache cache;
    private static class JvmRTBootClassPathTableCache extends SnmpTableCache {
        private JvmRTBootClassPathTableMetaImpl meta;
        JvmRTBootClassPathTableCache(JvmRTBootClassPathTableMetaImpl meta,
                                 long validity) {
            this.meta = meta;
            this.validity = validity;
        }
        public SnmpTableHandler getTableHandler() {
            final Map userData = JvmContextFactory.getUserData();
            return getTableDatas(userData);
        }
        protected SnmpCachedData updateCachedDatas(Object userData) {
            final String[] path =
                JvmRuntimeImpl.getBootClassPath(userData);
            final long time = System.currentTimeMillis();
            final int len = path.length;
            SnmpOid indexes[] = new SnmpOid[len];
            for(int i = 0; i < len; i++) {
                indexes[i] = new SnmpOid(i + 1);
            }
            return new SnmpCachedData(time, indexes, path);
        }
    }
    public JvmRTBootClassPathTableMetaImpl(SnmpMib myMib,
                                       SnmpStandardObjectServer objserv) {
        super(myMib, objserv);
        cache = new JvmRTBootClassPathTableCache(this, -1);
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
        if (dbg) log.debug("*** **** **** **** getNextOid", "next=" + next);
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
        final boolean dbg = log.isDebugOn();
        if (dbg) log.debug("getEntry", "oid [" + oid + "]");
        if (oid == null || oid.getLength() != 1) {
            if (dbg) log.debug("getEntry", "Invalid oid [" + oid + "]");
            throw new SnmpStatusException(SnmpStatusException.noSuchInstance);
        }
        final Map<Object, Object> m = JvmContextFactory.getUserData();
        final String entryTag = ((m==null)?null:
                                 ("JvmRTBootClassPathTable.entry." +
                                  oid.toString()));
        if (m != null) {
            final Object entry = m.get(entryTag);
            if (entry != null) {
                if (dbg)
                    log.debug("getEntry", "Entry is already in the cache");
                return entry;
            } else
                if (dbg) log.debug("getEntry", "Entry is not in the cache");
        }
        SnmpTableHandler handler = getHandler(m);
        if (handler == null)
            throw new SnmpStatusException(SnmpStatusException.noSuchInstance);
        final Object data = handler.getData(oid);
        if (data == null)
            throw new SnmpStatusException(SnmpStatusException.noSuchInstance);
        if (dbg)
            log.debug("getEntry","data is a: " + data.getClass().getName());
        final Object entry =
            new JvmRTBootClassPathEntryImpl((String) data,
                                            (int) oid.getOidArc(0));
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
                (SnmpTableHandler)m.get("JvmRTBootClassPathTable.handler");
            if (handler != null) return handler;
        }
        final SnmpTableHandler handler = cache.getTableHandler();
        if (m != null && handler != null )
            m.put("JvmRTBootClassPathTable.handler",handler);
        return handler;
    }
    static final MibLogger log =
        new MibLogger(JvmRTBootClassPathTableMetaImpl.class);
}
