public class PrintGCStat {
    private RuntimeMXBean rmbean;
    private MemoryMXBean mmbean;
    private List<MemoryPoolMXBean> pools;
    private List<GarbageCollectorMXBean> gcmbeans;
    public PrintGCStat(MBeanServerConnection server) throws IOException {
        this.rmbean = newPlatformMXBeanProxy(server,
                                             RUNTIME_MXBEAN_NAME,
                                             RuntimeMXBean.class);
        this.mmbean = newPlatformMXBeanProxy(server,
                                             MEMORY_MXBEAN_NAME,
                                             MemoryMXBean.class);
        ObjectName poolName = null;
        ObjectName gcName = null;
        try {
            poolName = new ObjectName(MEMORY_POOL_MXBEAN_DOMAIN_TYPE+",*");
            gcName = new ObjectName(GARBAGE_COLLECTOR_MXBEAN_DOMAIN_TYPE+",*");
        } catch (MalformedObjectNameException e) {
            assert(false);
        }
        Set<ObjectName> mbeans = server.queryNames(poolName, null);
        if (mbeans != null) {
            pools = new ArrayList<MemoryPoolMXBean>();
            for (ObjectName objName : mbeans) {
                MemoryPoolMXBean p =
                    newPlatformMXBeanProxy(server,
                                           objName.getCanonicalName(),
                                           MemoryPoolMXBean.class);
                pools.add(p);
            }
        }
        mbeans = server.queryNames(gcName, null);
        if (mbeans != null) {
            gcmbeans = new ArrayList<GarbageCollectorMXBean>();
            for (ObjectName objName : mbeans) {
                GarbageCollectorMXBean gc =
                    newPlatformMXBeanProxy(server,
                                           objName.getCanonicalName(),
                                           GarbageCollectorMXBean.class);
                gcmbeans.add(gc);
            }
        }
    }
    public PrintGCStat() {
        this.rmbean = getRuntimeMXBean();
        this.mmbean = getMemoryMXBean();
        this.pools = getMemoryPoolMXBeans();
        this.gcmbeans = getGarbageCollectorMXBeans();
    }
    public void printVerboseGc() {
        System.out.println("Uptime: " + formatMillis(rmbean.getUptime()));
        System.out.println("Heap usage: " + mmbean.getHeapMemoryUsage());
        System.out.println("Non-Heap memory usage: " + mmbean.getNonHeapMemoryUsage());
        for (GarbageCollectorMXBean gc : gcmbeans) {
            System.out.print(" [" + gc.getName() + ": ");
            System.out.print("Count=" + gc.getCollectionCount());
            System.out.print(" GCTime=" + formatMillis(gc.getCollectionTime()));
            System.out.print("]");
        }
        System.out.println();
        for (MemoryPoolMXBean p : pools) {
            System.out.print("  [" + p.getName() + ":");
            MemoryUsage u = p.getUsage();
            System.out.print(" Used=" + formatBytes(u.getUsed()));
            System.out.print(" Committed=" + formatBytes(u.getCommitted()));
            System.out.println("]");
        }
    }
    private String formatMillis(long ms) {
        return String.format("%.4fsec", ms / (double) 1000);
    }
    private String formatBytes(long bytes) {
        long kb = bytes;
        if (bytes > 0) {
            kb = bytes / 1024;
        }
        return kb + "K";
    }
}
