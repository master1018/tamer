public class JvmThreadInstanceTableMetaImpl
    extends JvmThreadInstanceTableMeta {
    public static final int MAX_STACK_TRACE_DEPTH=0;
    static SnmpOid makeOid(long l) {
        long[] x =  new long [8];
        x[0] = (l >> 56) & 0xFF;
        x[1] =  (l >> 48) & 0x00FF;
        x[2] =  (l >> 40) & 0x0000FF;
        x[3] =  (l >> 32) & 0x000000FF;
        x[4] =  (l >> 24) & 0x00000000FF;
        x[5] =  (l >> 16) & 0x0000000000FF;
        x[6] =  (l >> 8)  & 0x000000000000FF;
        x[7] =  l         & 0x00000000000000FF;
        return new SnmpOid(x);
    }
    static long makeId(SnmpOid oid) {
        long id = 0;
        long[] arcs = oid.longValue(false);
        id |= arcs[0] << 56;
        id |= arcs[1] << 48;
        id |= arcs[2] << 40;
        id |= arcs[3] << 32;
        id |= arcs[4] << 24;
        id |= arcs[5] << 16;
        id |= arcs[6] << 8;
        id |= arcs[7];
        return id;
    }
    private static class JvmThreadInstanceTableCache
        extends SnmpTableCache {
        final private JvmThreadInstanceTableMetaImpl meta;
        JvmThreadInstanceTableCache(JvmThreadInstanceTableMetaImpl meta,
                                   long validity) {
            this.validity = validity;
            this.meta     = meta;
        }
        public SnmpTableHandler getTableHandler() {
            final Map userData = JvmContextFactory.getUserData();
            return getTableDatas(userData);
        }
        protected SnmpCachedData updateCachedDatas(Object userData) {
            long[] id = JvmThreadingImpl.getThreadMXBean().getAllThreadIds();
            final long time = System.currentTimeMillis();
            SnmpOid indexes[] = new SnmpOid[id.length];
            final TreeMap<SnmpOid, Object> table =
                    new TreeMap<SnmpOid, Object>(SnmpCachedData.oidComparator);
            for(int i = 0; i < id.length; i++) {
                log.debug("", "Making index for thread id [" + id[i] +"]");
                SnmpOid oid = makeOid(id[i]);
                table.put(oid, oid);
            }
            return new SnmpCachedData(time, table);
        }
    }
    protected SnmpTableCache cache;
    public JvmThreadInstanceTableMetaImpl(SnmpMib myMib,
                                          SnmpStandardObjectServer objserv) {
        super(myMib, objserv);
        cache = new JvmThreadInstanceTableCache(this,
                            ((JVM_MANAGEMENT_MIB_IMPL)myMib).validity());
        log.debug("JvmThreadInstanceTableMetaImpl", "Create Thread meta");
    }
    protected SnmpOid getNextOid(Object userData)
        throws SnmpStatusException {
        log.debug("JvmThreadInstanceTableMetaImpl", "getNextOid");
        return getNextOid(null,userData);
    }
    protected SnmpOid getNextOid(SnmpOid oid, Object userData)
        throws SnmpStatusException {
        log.debug("getNextOid", "previous=" + oid);
        SnmpTableHandler handler = getHandler(userData);
        if (handler == null) {
            log.debug("getNextOid", "handler is null!");
            throw new SnmpStatusException(SnmpStatusException.noSuchInstance);
        }
        SnmpOid next = oid;
        while(true) {
            next = handler.getNext(next);
            if (next == null) break;
            if (getJvmThreadInstance(userData,next) != null) break;
        }
        log.debug("*** **** **** **** getNextOid", "next=" + next);
        if (next == null)
            throw new SnmpStatusException(SnmpStatusException.noSuchInstance);
        return next;
    }
    protected boolean contains(SnmpOid oid, Object userData) {
        SnmpTableHandler handler = getHandler(userData);
        if (handler == null)
            return false;
        if(!handler.contains(oid))
            return false;
        JvmThreadInstanceEntryImpl inst = getJvmThreadInstance(userData, oid);
        return (inst != null);
    }
    public Object getEntry(SnmpOid oid)
        throws SnmpStatusException {
        log.debug("*** **** **** **** getEntry", "oid [" + oid + "]");
        if (oid == null || oid.getLength() != 8) {
            log.debug("getEntry", "Invalid oid [" + oid + "]");
            throw new SnmpStatusException(SnmpStatusException.noSuchInstance);
        }
        final Map m = JvmContextFactory.getUserData();
        SnmpTableHandler handler = getHandler(m);
        if (handler == null || !handler.contains(oid))
            throw new SnmpStatusException(SnmpStatusException.noSuchInstance);
        final JvmThreadInstanceEntryImpl entry = getJvmThreadInstance(m,oid);
        if (entry == null)
            throw new SnmpStatusException(SnmpStatusException.noSuchInstance);
        return entry;
    }
    protected SnmpTableHandler getHandler(Object userData) {
        final Map<Object, Object> m;
        if (userData instanceof Map) m=Util.cast(userData);
        else m=null;
        if (m != null) {
            final SnmpTableHandler handler =
                (SnmpTableHandler)m.get("JvmThreadInstanceTable.handler");
            if (handler != null) return handler;
        }
        final SnmpTableHandler handler = cache.getTableHandler();
        if (m != null && handler != null )
            m.put("JvmThreadInstanceTable.handler",handler);
        return handler;
    }
    private ThreadInfo getThreadInfo(long id) {
        return JvmThreadingImpl.getThreadMXBean().
                  getThreadInfo(id,MAX_STACK_TRACE_DEPTH);
    }
    private ThreadInfo getThreadInfo(SnmpOid oid) {
        return getThreadInfo(makeId(oid));
    }
    private JvmThreadInstanceEntryImpl getJvmThreadInstance(Object userData,
                                                            SnmpOid oid) {
        JvmThreadInstanceEntryImpl cached = null;
        String entryTag = null;
        Map<Object, Object> map = null;
        final boolean dbg = log.isDebugOn();
        if (userData instanceof Map) {
            map = Util.cast(userData);
            entryTag = "JvmThreadInstanceTable.entry." + oid.toString();
            cached = (JvmThreadInstanceEntryImpl) map.get(entryTag);
        }
        if (cached != null) {
            if (dbg) log.debug("*** getJvmThreadInstance",
                               "Entry found in cache: " + entryTag);
            return cached;
        }
        if (dbg) log.debug("*** getJvmThreadInstance", "Entry [" +
                           oid + "] is not in cache");
        ThreadInfo info = null;
        try {
            info = getThreadInfo(oid);
        } catch (RuntimeException r) {
            log.trace("*** getJvmThreadInstance",
                      "Failed to get thread info for rowOid: " + oid);
            log.debug("*** getJvmThreadInstance",r);
        }
        if(info == null) {
            if (dbg) log.debug("*** getJvmThreadInstance",
                               "No entry by that oid [" + oid + "]");
            return null;
        }
        cached = new JvmThreadInstanceEntryImpl(info, oid.toByte());
        if (map != null) map.put(entryTag, cached);
        if (dbg) log.debug("*** getJvmThreadInstance",
                           "Entry created for Thread OID [" + oid + "]");
        return cached;
    }
     static final MibLogger log =
        new MibLogger(JvmThreadInstanceTableMetaImpl.class);
}
