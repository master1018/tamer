public class JvmMemMgrPoolRelTableMetaImpl extends JvmMemMgrPoolRelTableMeta
    implements Serializable {
    private static class JvmMemMgrPoolRelTableCache
        extends SnmpTableCache {
        final private JvmMemMgrPoolRelTableMetaImpl meta;
        JvmMemMgrPoolRelTableCache(JvmMemMgrPoolRelTableMetaImpl meta,
                                   long validity) {
            this.validity = validity;
            this.meta     = meta;
        }
        public SnmpTableHandler getTableHandler() {
            final Map userData = JvmContextFactory.getUserData();
            return getTableDatas(userData);
        }
        private static Map<String, SnmpOid> buildPoolIndexMap(SnmpTableHandler handler) {
            if (handler instanceof SnmpCachedData)
                return buildPoolIndexMap((SnmpCachedData)handler);
            final Map<String, SnmpOid> m = new HashMap<String, SnmpOid>();
            SnmpOid index=null;
            while ((index = handler.getNext(index))!=null) {
                final MemoryPoolMXBean mpm =
                    (MemoryPoolMXBean)handler.getData(index);
                if (mpm == null) continue;
                final String name = mpm.getName();
                if (name == null) continue;
                m.put(name,index);
            }
            return m;
        }
        private static Map<String, SnmpOid> buildPoolIndexMap(SnmpCachedData cached) {
            if (cached == null) return Collections.emptyMap();
            final SnmpOid[] indexes = cached.indexes;
            final Object[]  datas   = cached.datas;
            final int len = indexes.length;
            final Map<String, SnmpOid> m = new HashMap<String, SnmpOid>(len);
            for (int i=0; i<len; i++) {
                final SnmpOid index = indexes[i];
                if (index == null) continue;
                final MemoryPoolMXBean mpm =
                    (MemoryPoolMXBean)datas[i];
                if (mpm == null) continue;
                final String name = mpm.getName();
                if (name == null) continue;
                m.put(name,index);
            }
            return m;
        }
        protected SnmpCachedData updateCachedDatas(Object userData) {
            final SnmpTableHandler mmHandler =
                meta.getManagerHandler(userData);
            final SnmpTableHandler mpHandler =
                meta.getPoolHandler(userData);
            final long time = System.currentTimeMillis();
            final Map poolIndexMap = buildPoolIndexMap(mpHandler);
            final TreeMap<SnmpOid, Object> table =
                    new TreeMap<SnmpOid, Object>(SnmpCachedData.oidComparator);
            updateTreeMap(table,userData,mmHandler,mpHandler,poolIndexMap);
            return new SnmpCachedData(time,table);
        }
        protected String[] getMemoryPools(Object userData,
                                      MemoryManagerMXBean mmm, long mmarc) {
            final String listTag =
                "JvmMemManager." + mmarc + ".getMemoryPools";
            String[] result=null;
            if (userData instanceof Map) {
                result = (String[])((Map)userData).get(listTag);
                if (result != null) return result;
            }
            if (mmm!=null) {
                result = mmm.getMemoryPoolNames();
            }
            if ((result!=null)&&(userData instanceof Map)) {
                Map<Object, Object> map = Util.cast(userData);
                map.put(listTag,result);
            }
            return result;
        }
        protected void updateTreeMap(TreeMap<SnmpOid, Object> table, Object userData,
                                     MemoryManagerMXBean mmm,
                                     SnmpOid mmIndex,
                                     Map poolIndexMap) {
            final long mmarc;
            try {
                mmarc = mmIndex.getOidArc(0);
            } catch (SnmpStatusException x) {
                log.debug("updateTreeMap",
                          "Bad MemoryManager OID index: "+mmIndex);
                log.debug("updateTreeMap",x);
                return;
            }
            final String[] mpList = getMemoryPools(userData,mmm,mmarc);
            if (mpList == null || mpList.length < 1) return;
            final String mmmName = mmm.getName();
            for (int i = 0; i < mpList.length; i++) {
                final String mpmName = mpList[i];
                if (mpmName == null) continue;
                final SnmpOid mpIndex = (SnmpOid)poolIndexMap.get(mpmName);
                if (mpIndex == null) continue;
                final long mparc;
                try {
                    mparc  = mpIndex.getOidArc(0);
                } catch (SnmpStatusException x) {
                    log.debug("updateTreeMap","Bad MemoryPool OID index: " +
                          mpIndex);
                    log.debug("updateTreeMap",x);
                    continue;
                }
                final long[] arcs = { mmarc, mparc };
                final SnmpOid index = new SnmpOid(arcs);
                table.put(index, new JvmMemMgrPoolRelEntryImpl(mmmName,
                                                               mpmName,
                                                               (int)mmarc,
                                                               (int)mparc));
            }
        }
        protected void updateTreeMap(TreeMap<SnmpOid, Object> table, Object userData,
                                     SnmpTableHandler mmHandler,
                                     SnmpTableHandler mpHandler,
                                     Map poolIndexMap) {
            if (mmHandler instanceof SnmpCachedData) {
                updateTreeMap(table,userData,(SnmpCachedData)mmHandler,
                              mpHandler,poolIndexMap);
                return;
            }
            SnmpOid mmIndex=null;
            while ((mmIndex = mmHandler.getNext(mmIndex))!=null) {
                final MemoryManagerMXBean mmm =
                    (MemoryManagerMXBean)mmHandler.getData(mmIndex);
                if (mmm == null) continue;
                updateTreeMap(table,userData,mmm,mmIndex,poolIndexMap);
            }
        }
        protected void updateTreeMap(TreeMap<SnmpOid, Object> table, Object userData,
                                     SnmpCachedData mmHandler,
                                     SnmpTableHandler mpHandler,
                                     Map poolIndexMap) {
            final SnmpOid[] indexes = mmHandler.indexes;
            final Object[]  datas   = mmHandler.datas;
            final int size = indexes.length;
            for (int i=size-1; i>-1; i--) {
                final MemoryManagerMXBean mmm =
                    (MemoryManagerMXBean)datas[i];
                if (mmm == null) continue;
                updateTreeMap(table,userData,mmm,indexes[i],poolIndexMap);
            }
        }
    }
    protected SnmpTableCache cache;
    private transient JvmMemManagerTableMetaImpl managers = null;
    private transient JvmMemPoolTableMetaImpl    pools    = null;
    public JvmMemMgrPoolRelTableMetaImpl(SnmpMib myMib,
                                      SnmpStandardObjectServer objserv) {
        super(myMib,objserv);
        this.cache = new
            JvmMemMgrPoolRelTableCache(this,((JVM_MANAGEMENT_MIB_IMPL)myMib).
                                       validity());
    }
    private final JvmMemManagerTableMetaImpl getManagers(SnmpMib mib) {
        if (managers == null) {
            managers = (JvmMemManagerTableMetaImpl)
                mib.getRegisteredTableMeta("JvmMemManagerTable");
        }
        return managers;
    }
    private final JvmMemPoolTableMetaImpl getPools(SnmpMib mib) {
        if (pools == null) {
            pools = (JvmMemPoolTableMetaImpl)
                mib.getRegisteredTableMeta("JvmMemPoolTable");
        }
        return pools;
    }
    protected SnmpTableHandler getManagerHandler(Object userData) {
        final JvmMemManagerTableMetaImpl managerTable = getManagers(theMib);
        return managerTable.getHandler(userData);
    }
    protected SnmpTableHandler getPoolHandler(Object userData) {
        final JvmMemPoolTableMetaImpl poolTable = getPools(theMib);
        return poolTable.getHandler(userData);
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
        if (oid == null || oid.getLength() < 2)
            throw new SnmpStatusException(SnmpStatusException.noSuchInstance);
        final Map<Object, Object> m = JvmContextFactory.getUserData();
        final long   mgrIndex     = oid.getOidArc(0);
        final long   poolIndex    = oid.getOidArc(1);
        final String entryTag = ((m==null)?null:
                                 ("JvmMemMgrPoolRelTable.entry." +
                                  mgrIndex + "." + poolIndex));
        if (m != null) {
            final Object entry = m.get(entryTag);
            if (entry != null) return entry;
        }
        SnmpTableHandler handler = getHandler(m);
        if (handler == null)
            throw new SnmpStatusException(SnmpStatusException.noSuchInstance);
        final Object data = handler.getData(oid);
        if (!(data instanceof JvmMemMgrPoolRelEntryImpl))
            throw new SnmpStatusException(SnmpStatusException.noSuchInstance);
        final Object entry = (JvmMemMgrPoolRelEntryImpl)data;
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
                (SnmpTableHandler)m.get("JvmMemMgrPoolRelTable.handler");
            if (handler != null) return handler;
        }
        final SnmpTableHandler handler = cache.getTableHandler();
        if (m != null && handler != null )
            m.put("JvmMemMgrPoolRelTable.handler",handler);
        return handler;
    }
    static final MibLogger log =
        new MibLogger(JvmMemMgrPoolRelTableMetaImpl.class);
}
