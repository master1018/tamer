public class MemoryPoolStat {
    private String      poolName;
    private long        usageThreshold;
    private MemoryUsage usage;
    private long        lastGcId;
    private long        lastGcStartTime;
    private long        lastGcEndTime;
    private long        collectThreshold;
    private MemoryUsage beforeGcUsage;
    private MemoryUsage afterGcUsage;
    MemoryPoolStat(String name,
                   long usageThreshold,
                   MemoryUsage usage,
                   long lastGcId,
                   long lastGcStartTime,
                   long lastGcEndTime,
                   long collectThreshold,
                   MemoryUsage beforeGcUsage,
                   MemoryUsage afterGcUsage) {
        this.poolName = name;
        this.usageThreshold = usageThreshold;
        this.usage = usage;
        this.lastGcId = lastGcId;
        this.lastGcStartTime = lastGcStartTime;
        this.lastGcEndTime = lastGcEndTime;
        this.collectThreshold = collectThreshold;
        this.beforeGcUsage = beforeGcUsage;
        this.afterGcUsage = afterGcUsage;
    }
    public String getPoolName() {
        return poolName;
    }
    public MemoryUsage getUsage() {
        return usage;
    }
    public long getUsageThreshold() {
        return usageThreshold;
    }
    public long getCollectionUsageThreshold() {
        return collectThreshold;
    }
    public long getLastGcId() {
        return lastGcId;
    }
    public long getLastGcStartTime() {
        return lastGcStartTime;
    }
    public long getLastGcEndTime() {
        return lastGcEndTime;
    }
    public MemoryUsage getBeforeGcUsage() {
        return beforeGcUsage;
    }
    public MemoryUsage getAfterGcUsage() {
        return beforeGcUsage;
    }
}
