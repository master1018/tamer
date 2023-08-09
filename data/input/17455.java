public class SnmpCachedData implements SnmpTableHandler {
    public static final Comparator<SnmpOid> oidComparator =
        new Comparator<SnmpOid>() {
            public int compare(SnmpOid o1, SnmpOid o2) {
                return o1.compareTo(o2);
            }
            public boolean equals(Object o1, Object o2) {
                if (o1 == o2) return true;
                else return o1.equals(o2);
            }
        };
    public SnmpCachedData(long lastUpdated, SnmpOid indexes[],
                          Object  datas[]) {
        this.lastUpdated = lastUpdated;
        this.indexes     = indexes;
        this.datas       = datas;
    }
    public SnmpCachedData(long lastUpdated, TreeMap<SnmpOid, Object> indexMap) {
        this(lastUpdated, indexMap, true);
    }
    public SnmpCachedData(long lastUpdated, TreeMap<SnmpOid, Object> indexMap,
                          boolean b) {
        final int size = indexMap.size();
        this.lastUpdated = lastUpdated;
        this.indexes     = new SnmpOid[size];
        this.datas       = new Object[size];
        if(b) {
            indexMap.keySet().toArray(this.indexes);
            indexMap.values().toArray(this.datas);
        } else
            indexMap.values().toArray(this.datas);
    }
    public final long    lastUpdated;
    public final SnmpOid indexes[];
    public final Object  datas[];
    public final int find(SnmpOid index) {
        return Arrays.binarySearch(indexes,index,oidComparator);
    }
    public  Object getData(SnmpOid index) {
        final int pos = find(index);
        if ((pos < 0)||(pos >= datas.length)) return null;
        return datas[pos];
    }
    public  SnmpOid getNext(SnmpOid index) {
        if (index == null) {
            if (indexes.length>0) return indexes[0];
            else return null;
        }
        final int pos = find(index);
        if (pos > -1) {
            if (pos < (indexes.length -1) ) return indexes[pos+1];
            else return null;
        }
        final int insertion = -pos -1;
        if ((insertion > -1) && (insertion < indexes.length))
            return indexes[insertion];
        else return null;
    }
    public  boolean contains(SnmpOid index) {
        final int pos = find(index);
        return ((pos > -1)&&(pos < indexes.length));
    }
}
