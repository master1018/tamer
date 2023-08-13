public class MemoryTest {
    private static boolean testFailed = false;
    private static MemoryMXBean mm = ManagementFactory.getMemoryMXBean();
    private static final int HEAP = 0;
    private static final int NONHEAP = 1;
    private static final int NUM_TYPES = 2;
    private static int[] expectedMinNumPools = {3, 2};
    private static int[] expectedMaxNumPools = {3, 4};
    private static int expectedNumGCMgrs = 2;
    private static int expectedNumMgrs = expectedNumGCMgrs + 1;
    private static String[] types = { "heap", "non-heap" };
    public static void main(String args[]) throws Exception {
        Integer value = new Integer(args[0]);
        expectedNumGCMgrs = value.intValue();
        expectedNumMgrs = expectedNumGCMgrs + 1;
        checkMemoryPools();
        checkMemoryManagers();
        if (testFailed)
            throw new RuntimeException("TEST FAILED.");
        System.out.println("Test passed.");
    }
    private static void checkMemoryPools() throws Exception {
        List pools = ManagementFactory.getMemoryPoolMXBeans();
        int[] numPools = new int[NUM_TYPES];
        for (ListIterator iter = pools.listIterator(); iter.hasNext();) {
            MemoryPoolMXBean pool = (MemoryPoolMXBean) iter.next();
            if (pool.getType() == MemoryType.HEAP) {
                numPools[HEAP]++;
            }
            if (pool.getType() == MemoryType.NON_HEAP) {
                numPools[NONHEAP]++;
            }
        }
        for (int i = 0; i < NUM_TYPES; i++) {
            if (numPools[i] < expectedMinNumPools[i] ||
                    numPools[i] > expectedMaxNumPools[i]) {
                throw new RuntimeException("TEST FAILED: " +
                    "Number of " + types[i] + " pools = " + numPools[i] +
                    " but expected <= " + expectedMaxNumPools[i] +
                    " and >= " + expectedMinNumPools[i]);
            }
        }
    }
    private static void checkMemoryManagers() throws Exception {
        List mgrs = ManagementFactory.getMemoryManagerMXBeans();
        int numGCMgr = 0;
        for (ListIterator iter = mgrs.listIterator(); iter.hasNext();) {
            MemoryManagerMXBean mgr = (MemoryManagerMXBean) iter.next();
            String[] poolNames = mgr.getMemoryPoolNames();
            if (poolNames == null || poolNames.length == 0) {
                throw new RuntimeException("TEST FAILED: " +
                    "Expected to have one or more pools for " +
                    mgr.getName() + "manager.");
            }
            if (mgr instanceof GarbageCollectorMXBean) {
                numGCMgr++;
            } else {
                for (int i = 0; i < poolNames.length; i++) {
                    checkPoolType(poolNames[i], MemoryType.NON_HEAP);
                }
            }
        }
        if (mgrs.size() != expectedNumMgrs) {
            throw new RuntimeException("TEST FAILED: " +
                "Number of memory managers = " + mgrs.size() +
                " but expected = " + expectedNumMgrs);
        }
        if (numGCMgr != expectedNumGCMgrs) {
            throw new RuntimeException("TEST FAILED: " +
                "Number of GC managers = " + numGCMgr + " but expected = " +
                expectedNumGCMgrs);
        }
    }
    private static List pools = ManagementFactory.getMemoryPoolMXBeans();
    private static void checkPoolType(String name, MemoryType type)
        throws Exception {
        for (ListIterator iter = pools.listIterator(); iter.hasNext(); ) {
            MemoryPoolMXBean pool = (MemoryPoolMXBean) iter.next();
            if (pool.getName().equals(name)) {
                if (pool.getType() != type) {
                    throw new RuntimeException("TEST FAILED: " +
                        "Pool " + pool.getName() + " is of type " +
                        pool.getType() + " but expected to be " + type);
                } else {
                    return;
                }
            }
        }
        throw new RuntimeException("TEST FAILED: " +
            "Pool " + name + " is of type " + type +
            " not found");
    }
}
