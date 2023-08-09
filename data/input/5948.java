public class MemoryUtil {
    private static String INDENT = "    ";
    private static String formatSize(String name, long value) {
        StringBuffer buf = new StringBuffer(name + " = " + value);
        if (value > 0) {
            buf.append(" (" + (value >> 10) + "K)");
        }
        return buf.toString();
    }
    public static void printMemoryUsage(MemoryUsage usage) {
        System.out.println(INDENT + formatSize("Initial size  ", usage.getInit()));
        System.out.println(INDENT + formatSize("Used size     ", usage.getUsed()));
        System.out.println(INDENT + formatSize("Committd size ", usage.getCommitted()));
        System.out.println(INDENT + formatSize("Max size      ", usage.getMax()));
    }
    public static void printMemoryPool(MemoryPoolMXBean pool) {
        System.out.println(INDENT + "Memory Pool name: " + pool.getName());
        System.out.println(INDENT + "Type: " + pool.getType());
        System.out.println(INDENT + "Memory Usage: " +
            pool.getUsage());
        System.out.println(INDENT + "Threshold: " +
            (pool.isUsageThresholdSupported() ? pool.getUsageThreshold() : -1));
        System.out.print(INDENT + "Manager = [");
        String[] mgrs = pool.getMemoryManagerNames();
        for (int i = 0; i < mgrs.length; i++) {
            System.out.print(mgrs[i]);
            if (i < (mgrs.length - 1)) {
                System.out.print(" | ");
            }
        }
        System.out.println("]");
    }
    public static void printMemoryPools(List pools) {
        ListIterator iter = pools.listIterator();
        System.out.println(INDENT + "Number of memory pools = " + pools.size());
        while (iter.hasNext()) {
            MemoryPoolMXBean pool = (MemoryPoolMXBean) iter.next();
            printMemoryPool(pool);
        }
    }
    public static void printMemoryManager(MemoryManagerMXBean mgr) {
        if (mgr instanceof GarbageCollectorMXBean) {
            GarbageCollectorMXBean gc = (GarbageCollectorMXBean) mgr;
            System.out.println(INDENT + "Garbage Collector name: " + gc.getName());
            System.out.println(INDENT + "GC Count: " + gc.getCollectionCount());
            System.out.println(INDENT + "GC Time : " + gc.getCollectionTime());
        } else {
            System.out.println(INDENT + "Memory Manager name: " + mgr.getName());
        }
        System.out.print("Pool = [");
        String[] pools = mgr.getMemoryPoolNames();
        for (int i = 0; i < pools.length; i++) {
            System.out.print(INDENT + pools[i]);
            if (i < (pools.length - 1)) {
                System.out.print(" | ");
            }
        }
        System.out.println("]");
    }
    public static void printMemoryManagers(List managers) {
        ListIterator iter = managers.listIterator();
        System.out.println(INDENT + "Number of memory managers = " +
            managers.size());
        while (iter.hasNext()) {
            MemoryManagerMXBean mgr = (MemoryManagerMXBean) iter.next();
            printMemoryManager(mgr);
        }
    }
    public static void printMemoryNotificationInfo
        (MemoryNotificationInfo minfo, String type) {
        System.out.print("Notification for " + minfo.getPoolName());
        System.out.print(" [type = " + type);
        System.out.println(" count = " + minfo.getCount() + "]");
        System.out.println(INDENT + "usage = " + minfo.getUsage());
    }
}
