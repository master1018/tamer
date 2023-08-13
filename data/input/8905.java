public final class SnmpLoadedClassData extends SnmpCachedData {
    public SnmpLoadedClassData(long lastUpdated, TreeMap<SnmpOid, Object> indexMap) {
        super(lastUpdated, indexMap, false);
    }
    public final Object getData(SnmpOid index) {
        int pos = 0;
        try {
            pos = (int) index.getOidArc(0);
        }catch(SnmpStatusException e) {
            return null;
        }
        if (pos >= datas.length) return null;
        return datas[pos];
    }
    public final SnmpOid getNext(SnmpOid index) {
        int pos = 0;
        if (index == null) {
            if( (datas!= null) && (datas.length >= 1) )
                return new SnmpOid(0);
        }
        try {
            pos = (int) index.getOidArc(0);
        }catch(SnmpStatusException e) {
            return null;
        }
        if(pos < (datas.length - 1))
            return new SnmpOid(pos+1);
        else
            return null;
    }
    public final boolean contains(SnmpOid index) {
        int pos = 0;
        try {
            pos = (int) index.getOidArc(0);
        }catch(SnmpStatusException e) {
            return false;
        }
        return (pos < datas.length);
    }
}
