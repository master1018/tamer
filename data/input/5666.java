public abstract class SnmpTableCache implements Serializable {
    protected long validity;
    protected transient WeakReference<SnmpCachedData> datas;
    protected boolean isObsolete(SnmpCachedData cached) {
        if (cached   == null) return true;
        if (validity < 0)     return false;
        return ((System.currentTimeMillis() - cached.lastUpdated) > validity);
    }
    protected SnmpCachedData getCachedDatas() {
        if (datas == null) return null;
        final SnmpCachedData cached = datas.get();
        if ((cached == null) || isObsolete(cached)) return null;
        return cached;
    }
    protected synchronized SnmpCachedData getTableDatas(Object context) {
        final SnmpCachedData cached   = getCachedDatas();
        if (cached != null) return cached;
        final SnmpCachedData computedDatas = updateCachedDatas(context);
        if (validity != 0) datas = new WeakReference<SnmpCachedData>(computedDatas);
        return computedDatas;
    }
    protected abstract SnmpCachedData updateCachedDatas(Object context);
    public abstract SnmpTableHandler getTableHandler();
}
