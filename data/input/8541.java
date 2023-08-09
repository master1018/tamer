public class ThresholdTest {
    public static void main(String args[]) throws Exception {
        List<MemoryPoolMXBean> pools = ManagementFactory.getMemoryPoolMXBeans();
        try {
            for (MemoryPoolMXBean p : pools) {
                checkUsageThreshold(p);
                checkCollectionUsageThreshold(p);
            }
        } finally {
            for (MemoryPoolMXBean p : pools) {
                if (p.isUsageThresholdSupported()) {
                    p.setUsageThreshold(0);
                }
                if (p.isCollectionUsageThresholdSupported()) {
                    p.setCollectionUsageThreshold(0);
                }
            }
        }
        System.out.println("Test passed.");
    }
    private static void checkUsageThreshold(MemoryPoolMXBean p) throws Exception {
        if (!p.isUsageThresholdSupported()) {
            return;
        }
        long threshold = p.getUsageThreshold();
        if (threshold != 0) {
            throw new RuntimeException("TEST FAILED: " +
                "Pool " + p.getName() +
                " has non-zero threshold (" + threshold);
        }
        if (p.isUsageThresholdExceeded()) {
            throw new RuntimeException("TEST FAILED: " +
                "Pool " + p.getName() +
                " isUsageThresholdExceeded() returned true" +
                " but threshold = 0");
        }
        p.setUsageThreshold(1);
        MemoryUsage u = p.getUsage();
        if (u.getUsed() >= 1) {
            if (!p.isUsageThresholdExceeded()) {
                throw new RuntimeException("TEST FAILED: " +
                    "Pool " + p.getName() +
                    " isUsageThresholdExceeded() returned false but " +
                    " threshold(" + p.getUsageThreshold() +
                    ") <= used(" + u.getUsed() + ")");
            }
        } else {
            if (p.isUsageThresholdExceeded()) {
                throw new RuntimeException("TEST FAILED: " +
                    "Pool " + p.getName() +
                    " isUsageThresholdExceeded() returned true but" +
                    " threshold(" + p.getUsageThreshold() +
                    ") > used(" + u.getUsed() + ")");
            }
        }
        p.setUsageThreshold(0);
        if (p.isUsageThresholdExceeded()) {
            throw new RuntimeException("TEST FAILED: " +
                "Pool " + p.getName() +
                " isUsageThresholdExceeded() returned true but threshold = 0");
        }
    }
    private static void checkCollectionUsageThreshold(MemoryPoolMXBean p) throws Exception {
        if (!p.isCollectionUsageThresholdSupported()) {
            return;
        }
        long threshold = p.getCollectionUsageThreshold();
        if (threshold != 0) {
            throw new RuntimeException("TEST FAILED: " +
                "Pool " + p.getName() +
                " has non-zero threshold (" + threshold);
        }
        if (p.isCollectionUsageThresholdExceeded()) {
            throw new RuntimeException("TEST FAILED: " +
                "Pool " + p.getName() +
                " isCollectionUsageThresholdExceeded() returned true" +
                " but threshold = 0");
        }
        p.setCollectionUsageThreshold(1);
        MemoryUsage u = p.getCollectionUsage();
        if (u == null) {
            if (p.isCollectionUsageThresholdExceeded()) {
                throw new RuntimeException("TEST FAILED: " +
                    "Pool " + p.getName() +
                    " isCollectionUsageThresholdExceeded() returned true but" +
                    " getCollectionUsage() return null");
            }
        } else if (u.getUsed() >= 1) {
            if (!p.isCollectionUsageThresholdExceeded()) {
                throw new RuntimeException("TEST FAILED: " +
                    "Pool " + p.getName() +
                    " isCollectionUsageThresholdExceeded() returned false but " +
                    " threshold(" + p.getCollectionUsageThreshold() +
                    ") < used(" + u.getUsed() + ")");
            }
        } else {
            if (p.isCollectionUsageThresholdExceeded()) {
                throw new RuntimeException("TEST FAILED: " +
                    "Pool " + p.getName() +
                    " isCollectionUsageThresholdExceeded() returned true but" +
                    " threshold(" + p.getCollectionUsageThreshold() +
                    ") > used(" + u.getUsed() + ")");
            }
        }
        p.setCollectionUsageThreshold(0);
        if (p.isCollectionUsageThresholdExceeded()) {
            throw new RuntimeException("TEST FAILED: " +
                "Pool " + p.getName() +
                " isCollectionUsageThresholdExceeded() returned true but threshold = 0");
        }
    }
}
