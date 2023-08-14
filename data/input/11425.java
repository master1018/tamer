public abstract class SnmpListTableCache extends SnmpTableCache {
    protected abstract SnmpOid getIndex(Object context, List rawDatas,
                                        int rank, Object item);
    protected Object getData(Object context, List rawDatas,
                             int rank, Object item) {
        return item;
    }
    protected SnmpCachedData updateCachedDatas(Object context, List rawDatas) {
        final int size = ((rawDatas == null)?0:rawDatas.size());
        if (size == 0) return  null;
        final long time = System.currentTimeMillis();
        final Iterator it  = rawDatas.iterator();
        final TreeMap<SnmpOid, Object> map =
                new TreeMap<SnmpOid, Object>(SnmpCachedData.oidComparator);
        for (int rank=0; it.hasNext() ; rank++) {
            final Object  item  = it.next();
            final SnmpOid index = getIndex(context, rawDatas, rank, item);
            final Object  data  = getData(context, rawDatas, rank, item);
            if (index == null) continue;
            map.put(index,data);
        }
        return new SnmpCachedData(time,map);
    }
}
