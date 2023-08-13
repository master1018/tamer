public class GcInfo implements CompositeData, CompositeDataView {
    private final long index;
    private final long startTime;
    private final long endTime;
    private final Map<String, MemoryUsage> usageBeforeGc;
    private final Map<String, MemoryUsage> usageAfterGc;
    private final Object[] extAttributes;
    private final CompositeData cdata;
    private final GcInfoBuilder builder;
    private GcInfo(GcInfoBuilder builder,
                   long index, long startTime, long endTime,
                   MemoryUsage[] muBeforeGc,
                   MemoryUsage[] muAfterGc,
                   Object[] extAttributes) {
        this.builder       = builder;
        this.index         = index;
        this.startTime     = startTime;
        this.endTime       = endTime;
        String[] poolNames = builder.getPoolNames();
        this.usageBeforeGc = new HashMap<String, MemoryUsage>(poolNames.length);
        this.usageAfterGc = new HashMap<String, MemoryUsage>(poolNames.length);
        for (int i = 0; i < poolNames.length; i++) {
            this.usageBeforeGc.put(poolNames[i],  muBeforeGc[i]);
            this.usageAfterGc.put(poolNames[i],  muAfterGc[i]);
        }
        this.extAttributes = extAttributes;
        this.cdata = new GcInfoCompositeData(this, builder, extAttributes);
    }
    private GcInfo(CompositeData cd) {
        GcInfoCompositeData.validateCompositeData(cd);
        this.index         = GcInfoCompositeData.getId(cd);
        this.startTime     = GcInfoCompositeData.getStartTime(cd);
        this.endTime       = GcInfoCompositeData.getEndTime(cd);
        this.usageBeforeGc = GcInfoCompositeData.getMemoryUsageBeforeGc(cd);
        this.usageAfterGc  = GcInfoCompositeData.getMemoryUsageAfterGc(cd);
        this.extAttributes = null;
        this.builder       = null;
        this.cdata         = cd;
    }
    public long getId() {
        return index;
    }
    public long getStartTime() {
        return startTime;
    }
    public long getEndTime() {
        return endTime;
    }
    public long getDuration() {
        return endTime - startTime;
    }
    public Map<String, MemoryUsage> getMemoryUsageBeforeGc() {
        return Collections.unmodifiableMap(usageBeforeGc);
    }
    public Map<String, MemoryUsage> getMemoryUsageAfterGc() {
        return Collections.unmodifiableMap(usageAfterGc);
    }
    public static GcInfo from(CompositeData cd) {
        if (cd == null) {
            return null;
        }
        if (cd instanceof GcInfoCompositeData) {
            return ((GcInfoCompositeData) cd).getGcInfo();
        } else {
            return new GcInfo(cd);
        }
    }
    public boolean containsKey(String key) {
        return cdata.containsKey(key);
    }
    public boolean containsValue(Object value) {
        return cdata.containsValue(value);
    }
    public boolean equals(Object obj) {
        return cdata.equals(obj);
    }
    public Object get(String key) {
        return cdata.get(key);
    }
    public Object[] getAll(String[] keys) {
        return cdata.getAll(keys);
    }
    public CompositeType getCompositeType() {
        return cdata.getCompositeType();
    }
    public int hashCode() {
        return cdata.hashCode();
    }
    public String toString() {
        return cdata.toString();
    }
    public Collection values() {
        return cdata.values();
    }
    public CompositeData toCompositeData(CompositeType ct) {
        return cdata;
    }
}
