public class MXBeanException {
    private static MemoryPoolMXBean pool;
    public static void main(String[] argv) throws Exception {
        List<MemoryPoolMXBean> pools =
            ManagementFactory.getMemoryPoolMXBeans();
        for (MemoryPoolMXBean p : pools) {
            if (!p.isUsageThresholdSupported()) {
                pool = p;
                break;
            }
        }
        try {
            pool.setUsageThreshold(1000);
            throw new RuntimeException("TEST FAILED: " +
                "UnsupportedOperationException not thrown");
        } catch (UnsupportedOperationException e) {
        }
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ObjectName on = new ObjectName(MEMORY_POOL_MXBEAN_DOMAIN_TYPE +
                                       ",name=" + pool.getName());
        Attribute att = new Attribute("UsageThreshold", 1000);
        try {
            mbs.setAttribute(on, att);
        } catch (RuntimeMBeanException e) {
            checkMBeanException(e);
        } catch (RuntimeOperationsException e) {
            checkMBeanException(e);
        }
        MemoryPoolMXBean proxy = newPlatformMXBeanProxy(mbs,
                                     on.toString(),
                                     MemoryPoolMXBean.class);
        try {
            proxy.setUsageThreshold(1000);
            throw new RuntimeException("TEST FAILED: " +
                "UnsupportedOperationException not thrown via proxy");
        } catch (UnsupportedOperationException e) {
        }
        System.out.println("Test passed");
    }
    private static void checkMBeanException(JMRuntimeException e) {
        Throwable cause = e.getCause();
        if (!(cause instanceof UnsupportedOperationException)) {
            throw new RuntimeException("TEST FAILED: " + cause +
                "is not UnsupportedOperationException");
        }
    }
}
