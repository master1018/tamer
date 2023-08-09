public class PlatformMBeanServerTest {
    public static void main(String[] argv) throws Exception {
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        printMBeans(mbs);
        checkStandardMBeans(mbs);
        checkPlatformMBeans(mbs);
        MBeanServer mbs1 = ManagementFactory.getPlatformMBeanServer();
        if (mbs != mbs1) {
            throw new RuntimeException("Second call to getPlatformMBeanServer()"
                + " returns a different MBeanServer.");
        }
        System.out.println("Test passed.");
    }
    private static void checkMBean(MBeanServer mbs, String mbeanName)
            throws Exception {
        try {
            ObjectName objName = new ObjectName(mbeanName);
            mbs.getMBeanInfo(objName);
        } catch (Exception e) {
            throw e;
        }
    }
    private static void checkStandardMBeans(MBeanServer mbs) throws Exception {
        checkMBean(mbs, CLASS_LOADING_MXBEAN_NAME);
        checkMBean(mbs, MEMORY_MXBEAN_NAME);
        checkMBean(mbs, OPERATING_SYSTEM_MXBEAN_NAME);
        checkMBean(mbs, RUNTIME_MXBEAN_NAME);
        checkMBean(mbs, THREAD_MXBEAN_NAME);
        if (ManagementFactory.getCompilationMXBean() != null) {
            checkMBean(mbs, COMPILATION_MXBEAN_NAME);
        }
        List pools = ManagementFactory.getMemoryPoolMXBeans();
        for (ListIterator iter = pools.listIterator(); iter.hasNext(); ) {
            MemoryPoolMXBean p = (MemoryPoolMXBean) iter.next();
            checkMBean(mbs, MEMORY_POOL_MXBEAN_DOMAIN_TYPE + ",name=" + p.getName());
        }
        Set set = mbs.queryNames(new ObjectName(MEMORY_POOL_MXBEAN_DOMAIN_TYPE + ",*"), null);
        if (set.size() != pools.size()) {
            throw new RuntimeException("Unexpected number of memory pools:" +
                "MBeanServer has " + set.size() +
                ". Expected = " + pools.size());
        }
        List mgrs = ManagementFactory.getMemoryManagerMXBeans();
        int num_mgrs = 0;
        for (ListIterator iter = mgrs.listIterator(); iter.hasNext(); ) {
            MemoryManagerMXBean m = (MemoryManagerMXBean) iter.next();
            if (m instanceof GarbageCollectorMXBean) {
                checkMBean(mbs, GARBAGE_COLLECTOR_MXBEAN_DOMAIN_TYPE
                                    + ",name=" + m.getName());
            } else {
                checkMBean(mbs, MEMORY_MANAGER_MXBEAN_DOMAIN_TYPE
                                    + ",name=" + m.getName());
                num_mgrs++;
            }
        }
        List gcs = ManagementFactory.getGarbageCollectorMXBeans();
        for (ListIterator iter = gcs.listIterator(); iter.hasNext(); ) {
            GarbageCollectorMXBean gc = (GarbageCollectorMXBean) iter.next();
            checkMBean(mbs, GARBAGE_COLLECTOR_MXBEAN_DOMAIN_TYPE
                                + ",name=" + gc.getName());
        }
        set = mbs.queryNames(new ObjectName(MEMORY_MANAGER_MXBEAN_DOMAIN_TYPE + ",*"), null);
        if (set.size() != num_mgrs) {
            throw new RuntimeException("Unexpected number of memory managers:" +
                "MBeanServer has " + set.size() +
                ". Expected = " + num_mgrs);
        }
        set = mbs.queryNames(new ObjectName(GARBAGE_COLLECTOR_MXBEAN_DOMAIN_TYPE + ",*"), null);
        if (set.size() != gcs.size()) {
            throw new RuntimeException("Unexpected number of garbage collectors:" +
                "MBeanServer has " + set.size() +
                ". Expected = " + gcs.size());
        }
    }
    private static void checkPlatformMBeans(MBeanServer mbs) throws Exception {
        checkMBean(mbs, LogManager.LOGGING_MXBEAN_NAME);
    }
    private static void printMBeans(MBeanServer mbs) throws Exception {
        Set set = mbs.queryNames(null, null);
        for (Iterator iter = set.iterator(); iter.hasNext(); ) {
            System.out.println(iter.next());
        }
    }
}
