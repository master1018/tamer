public class ProxyExceptions {
    private static MBeanServer server =
        ManagementFactory.getPlatformMBeanServer();
    private static MemoryPoolMXBean heapPool = null;
    private static MemoryPoolMXBean nonHeapPool = null;
    public static void main(String[] argv) throws Exception {
        List<MemoryPoolMXBean> pools = getMemoryPoolMXBeans();
        for (MemoryPoolMXBean p : pools) {
            MemoryPoolMXBean proxy = newPlatformMXBeanProxy(server,
                MEMORY_POOL_MXBEAN_DOMAIN_TYPE + ",name=" + p.getName(),
                MemoryPoolMXBean.class);
            boolean uoeCaught;
            if (!p.isUsageThresholdSupported()) {
                try {
                    proxy.getUsageThreshold();
                    uoeCaught = false;
                } catch (UnsupportedOperationException e) {
                    uoeCaught = true;
                }
                if (!uoeCaught) {
                    throw new RuntimeException("TEST FAILED: " +
                        "UnsupportedOperationException not thrown " +
                        "when calling getUsageThreshold on " + p.getName());
                }
                try {
                    proxy.setUsageThreshold(100);
                    uoeCaught = false;
                } catch (UnsupportedOperationException e) {
                    uoeCaught = true;
                }
                if (!uoeCaught) {
                    throw new RuntimeException("TEST FAILED: " +
                        "UnsupportedOperationException not thrown " +
                        "when calling setUsageThreshold on " + p.getName());
                }
            }
            if (!p.isCollectionUsageThresholdSupported()) {
                try {
                    proxy.getCollectionUsageThreshold();
                    uoeCaught = false;
                } catch (UnsupportedOperationException e) {
                    uoeCaught = true;
                }
                if (!uoeCaught) {
                    throw new RuntimeException("TEST FAILED: " +
                        "UnsupportedOperationException not thrown " +
                        "when calling getCollectionUsageThreshold on " +
                        p.getName());
                }
                try {
                    proxy.setCollectionUsageThreshold(100);
                    uoeCaught = false;
                } catch (UnsupportedOperationException e) {
                    uoeCaught = true;
                }
                if (!uoeCaught) {
                    throw new RuntimeException("TEST FAILED: " +
                        "UnsupportedOperationException not thrown " +
                        "when calling getCollectionUsageThreshold on " +
                        p.getName());
                }
            }
        }
        ThreadMXBean thread = newPlatformMXBeanProxy(server,
                                                  THREAD_MXBEAN_NAME,
                                                  ThreadMXBean.class);
        boolean iaeCaught = false;
        try {
            thread.getThreadInfo(-1);
        } catch (IllegalArgumentException e) {
            iaeCaught = true;
        }
        if (!iaeCaught) {
            throw new RuntimeException("TEST FAILED: " +
                "IllegalArgumentException not thrown " +
                "when calling getThreadInfo(-1)");
        }
        System.out.println("Test passed.");
    }
}
