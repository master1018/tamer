public class JvmMemGCTableMetaImpl extends  JvmMemGCTableMeta {
    protected static class GCTableFilter {
        public SnmpOid getNext(SnmpCachedData datas, SnmpOid index) {
            final boolean dbg = log.isDebugOn();
            final int insertion = (index==null)?-1:datas.find(index);
            if (dbg) log.debug("GCTableFilter","oid="+index+
                               " at insertion="+insertion);
            int next;
            if (insertion > -1) next = insertion+1;
            else next = -insertion -1;
            for (;next<datas.indexes.length;next++) {
                if (dbg) log.debug("GCTableFilter","next="+next);
                final Object value = datas.datas[next];
                if (dbg) log.debug("GCTableFilter","value["+next+"]=" +
                      ((MemoryManagerMXBean)value).getName());
                if (value instanceof GarbageCollectorMXBean) {
                    if (dbg) log.debug("GCTableFilter",
                          ((MemoryManagerMXBean)value).getName() +
                          " is a  GarbageCollectorMXBean.");
                    return datas.indexes[next];
                }
                if (dbg) log.debug("GCTableFilter",
                      ((MemoryManagerMXBean)value).getName() +
                      " is not a  GarbageCollectorMXBean: " +
                      value.getClass().getName());
            }
            return null;
        }
        public SnmpOid getNext(SnmpTableHandler handler, SnmpOid index) {
            if (handler instanceof SnmpCachedData)
                return getNext((SnmpCachedData)handler, index);
            SnmpOid next = index;
            do {
                next = handler.getNext(next);
                final Object value = handler.getData(next);
                if (value instanceof GarbageCollectorMXBean)
                    return next;
            } while (next != null);
            return null;
        }
        public Object  getData(SnmpTableHandler handler, SnmpOid index) {
            final Object value = handler.getData(index);
            if (value instanceof GarbageCollectorMXBean) return value;
            return null;
        }
        public boolean contains(SnmpTableHandler handler, SnmpOid index) {
            if (handler.getData(index) instanceof GarbageCollectorMXBean)
                return true;
            return false;
        }
    }
    private transient JvmMemManagerTableMetaImpl managers = null;
    private static GCTableFilter filter = new GCTableFilter();
    public JvmMemGCTableMetaImpl(SnmpMib myMib,
                                 SnmpStandardObjectServer objserv) {
        super(myMib,objserv);
    }
    private final JvmMemManagerTableMetaImpl getManagers(SnmpMib mib) {
        if (managers == null) {
            managers = (JvmMemManagerTableMetaImpl)
                mib.getRegisteredTableMeta("JvmMemManagerTable");
        }
        return managers;
    }
    protected SnmpTableHandler getHandler(Object userData) {
        JvmMemManagerTableMetaImpl managerTable= getManagers(theMib);
        return managerTable.getHandler(userData);
    }
    protected SnmpOid getNextOid(Object userData)
        throws SnmpStatusException {
        return getNextOid(null,userData);
    }
    protected SnmpOid getNextOid(SnmpOid oid, Object userData)
        throws SnmpStatusException {
        final boolean dbg = log.isDebugOn();
        try {
            if (dbg) log.debug("getNextOid", "previous=" + oid);
            SnmpTableHandler handler = getHandler(userData);
            if (handler == null) {
                if (dbg) log.debug("getNextOid", "handler is null!");
                throw new
                    SnmpStatusException(SnmpStatusException.noSuchInstance);
            }
            final SnmpOid next = filter.getNext(handler,oid);
            if (dbg) log.debug("getNextOid", "next=" + next);
            if (next == null)
                throw new
                    SnmpStatusException(SnmpStatusException.noSuchInstance);
            return next;
        } catch (RuntimeException x) {
            if (dbg) log.debug("getNextOid",x);
            throw x;
        }
    }
    protected boolean contains(SnmpOid oid, Object userData) {
        SnmpTableHandler handler = getHandler(userData);
        if (handler == null)
            return false;
        return filter.contains(handler,oid);
    }
    public Object getEntry(SnmpOid oid)
        throws SnmpStatusException {
        if (oid == null)
            throw new SnmpStatusException(SnmpStatusException.noSuchInstance);
        final Map<Object, Object> m = JvmContextFactory.getUserData();
        final long   index    = oid.getOidArc(0);
        final String entryTag = ((m==null)?null:("JvmMemGCTable.entry." +
                                                 index));
        if (m != null) {
            final Object entry = m.get(entryTag);
            if (entry != null) return entry;
        }
        SnmpTableHandler handler = getHandler(m);
        if (handler == null)
            throw new SnmpStatusException(SnmpStatusException.noSuchInstance);
        final Object data = filter.getData(handler,oid);
        if (data == null)
            throw new SnmpStatusException(SnmpStatusException.noSuchInstance);
        final Object entry =
            new JvmMemGCEntryImpl((GarbageCollectorMXBean)data,(int)index);
        if (m != null && entry != null) {
            m.put(entryTag,entry);
        }
        return entry;
    }
    static final MibLogger log = new MibLogger(JvmMemGCTableMetaImpl.class);
}
