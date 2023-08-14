public abstract class SnmpNamedListTableCache extends SnmpListTableCache {
    protected TreeMap names = new TreeMap();
    protected long last = 0;
    boolean   wrapped = false;
    protected abstract String getKey(Object context, List rawDatas,
                                     int rank, Object item);
    protected SnmpOid makeIndex(Object context, List rawDatas,
                                int rank, Object item) {
        if (++last > 0x00000000FFFFFFFFL) {
            log.debug("makeIndex", "Index wrapping...");
            last = 0;
            wrapped=true;
        }
        if (!wrapped) return new SnmpOid(last);
        for (int i=1;i < 0x00000000FFFFFFFFL;i++) {
            if (++last >  0x00000000FFFFFFFFL) last = 1;
            final SnmpOid testOid = new SnmpOid(last);
            if (names == null) return testOid;
            if (names.containsValue(testOid)) continue;
            if (context == null) return testOid;
            if (((Map)context).containsValue(testOid)) continue;
            return testOid;
        }
        return null;
    }
    protected SnmpOid getIndex(Object context, List rawDatas,
                               int rank, Object item) {
        final String key   = getKey(context,rawDatas,rank,item);
        final Object index = (names==null||key==null)?null:names.get(key);
        final SnmpOid result =
            ((index != null)?((SnmpOid)index):makeIndex(context,rawDatas,
                                                      rank,item));
        if ((context != null) && (key != null) && (result != null)) {
            Map<Object, Object> map = Util.cast(context);
            map.put(key,result);
        }
        log.debug("getIndex","key="+key+", index="+result);
        return result;
    }
    protected SnmpCachedData updateCachedDatas(Object context, List rawDatas) {
        TreeMap ctxt = new TreeMap();
        final SnmpCachedData result =
            super.updateCachedDatas(context,rawDatas);
        names = ctxt;
        return result;
    }
    protected abstract List   loadRawDatas(Map userData);
    protected abstract String getRawDatasKey();
    protected List getRawDatas(Map<Object, Object> userData, String key) {
        List rawDatas = null;
        if (userData != null)
            rawDatas = (List) userData.get(key);
        if (rawDatas == null) {
            rawDatas =  loadRawDatas(userData);
            if (rawDatas != null && userData != null)
                userData.put(key, rawDatas);
        }
        return rawDatas;
    }
    protected SnmpCachedData updateCachedDatas(Object context) {
        final Map<Object, Object> userData =
            (context instanceof Map)?Util.<Map<Object, Object>>cast(context):null;
        final List rawDatas = getRawDatas(userData,getRawDatasKey());
        log.debug("updateCachedDatas","rawDatas.size()=" +
              ((rawDatas==null)?"<no data>":""+rawDatas.size()));
        TreeMap ctxt = new TreeMap();
        final SnmpCachedData result =
            super.updateCachedDatas(ctxt,rawDatas);
        names = ctxt;
        return result;
    }
    static final MibLogger log = new MibLogger(SnmpNamedListTableCache.class);
}
