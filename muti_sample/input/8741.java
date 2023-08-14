public class JvmRTInputArgsTableMetaImpl extends JvmRTInputArgsTableMeta {
    private SnmpTableCache cache;
    private static class JvmRTInputArgsTableCache extends SnmpTableCache {
        private JvmRTInputArgsTableMetaImpl meta;
        JvmRTInputArgsTableCache(JvmRTInputArgsTableMetaImpl meta,
                                 long validity) {
            this.meta = meta;
            this.validity = validity;
        }
        public SnmpTableHandler getTableHandler() {
            final Map userData = JvmContextFactory.getUserData();
            return getTableDatas(userData);
        }
        protected SnmpCachedData updateCachedDatas(Object userData) {
            final String[] args = JvmRuntimeImpl.getInputArguments(userData);
            final long time = System.currentTimeMillis();
            SnmpOid indexes[] = new SnmpOid[args.length];
            for(int i = 0; i < args.length; i++) {
                indexes[i] = new SnmpOid(i + 1);
            }
            return new SnmpCachedData(time, indexes, args);
        }
    }
    public JvmRTInputArgsTableMetaImpl(SnmpMib myMib,
                                       SnmpStandardObjectServer objserv) {
        super(myMib, objserv);
        cache = new JvmRTInputArgsTableCache(this, -1);
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
                                 ("JvmRTInputArgsTable.entry." +
                                  oid.toString()));
        if (m != null) {
            final Object entry = m.get(entryTag);
            if (entry != null) {
                if (dbg)
                    log.debug("getEntry", "Entry is already in the cache");
                return entry;
            } else if (dbg) log.debug("getEntry", "Entry is not in the cache");
        }
        SnmpTableHandler handler = getHandler(m);
        if (handler == null)
            throw new SnmpStatusException(SnmpStatusException.noSuchInstance);
        final Object data = handler.getData(oid);
        if (data == null)
            throw new SnmpStatusException(SnmpStatusException.noSuchInstance);
        if (dbg) log.debug("getEntry","data is a: " +
                           data.getClass().getName());
        final Object entry =
            new JvmRTInputArgsEntryImpl((String) data, (int) oid.getOidArc(0));
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
                (SnmpTableHandler)m.get("JvmRTInputArgsTable.handler");
            if (handler != null) return handler;
        }
        final SnmpTableHandler handler = cache.getTableHandler();
        if (m != null && handler != null )
            m.put("JvmRTInputArgsTable.handler",handler);
        return handler;
    }
    static final MibLogger log =
        new MibLogger(JvmRTInputArgsTableMetaImpl.class);
}
