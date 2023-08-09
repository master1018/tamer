public class ResetPeakMemoryUsage {
    private static MemoryMXBean mbean = ManagementFactory.getMemoryMXBean();
    private static List pools = ManagementFactory.getMemoryPoolMXBeans();
    private static MemoryPoolMXBean mpool = null;
    public static void main(String[] argv) {
        ListIterator iter = pools.listIterator();
        while (iter.hasNext()) {
            MemoryPoolMXBean p = (MemoryPoolMXBean) iter.next();
            if (p.getType() == MemoryType.HEAP &&
                    p.isUsageThresholdSupported()) {
                mpool = p;
                System.out.println("Selected memory pool: ");
                MemoryUtil.printMemoryPool(mpool);
                break;
            }
        }
        if (mpool == null) {
            throw new RuntimeException("No heap pool found with threshold != -1");
        }
        MemoryUsage usage0 = mpool.getUsage();
        MemoryUsage peak0 = mpool.getPeakUsage();
        final long largeArraySize = (usage0.getMax() - usage0.getUsed()) / 10;
        System.out.println("Before big object is allocated: ");
        printMemoryUsage();
        Object[][][] obj = new Object[1][1][(int) largeArraySize];
        System.out.println("After the object is allocated: ");
        printMemoryUsage();
        MemoryUsage usage1 = mpool.getUsage();
        MemoryUsage peak1 = mpool.getPeakUsage();
        if (usage1.getUsed() <= usage0.getUsed()) {
            throw new RuntimeException(
                formatSize("Before allocation: used", usage0.getUsed()) +
                " expected to be > " +
                formatSize("After allocation: used", usage1.getUsed()));
        }
        if (peak1.getUsed() <= peak0.getUsed()) {
            throw new RuntimeException(
                formatSize("Before allocation: peak", peak0.getUsed()) +
                " expected to be > " +
                formatSize("After allocation: peak", peak1.getUsed()));
        }
        obj = null;
        mbean.gc();
        System.out.println("After GC: ");
        printMemoryUsage();
        MemoryUsage usage2 = mpool.getUsage();
        MemoryUsage peak2 = mpool.getPeakUsage();
        if (usage2.getUsed() >= usage1.getUsed()) {
            throw new RuntimeException(
                formatSize("Before GC: used", usage1.getUsed()) + " " +
                " expected to be > " +
                formatSize("After GC: used", usage2.getUsed()));
        }
        if (peak2.getUsed() != peak1.getUsed()) {
            throw new RuntimeException(
                formatSize("Before GC: peak", peak1.getUsed()) + " " +
                " expected to be equal to " +
                formatSize("After GC: peak", peak2.getUsed()));
        }
        mpool.resetPeakUsage();
        System.out.println("After resetPeakUsage: ");
        printMemoryUsage();
        MemoryUsage usage3 = mpool.getUsage();
        MemoryUsage peak3 = mpool.getPeakUsage();
        if (peak3.getUsed() != usage3.getUsed()) {
            throw new RuntimeException(
                formatSize("After resetting peak: peak", peak3.getUsed()) + " " +
                " expected to be equal to " +
                formatSize("current used", usage3.getUsed()));
        }
        if (peak3.getUsed() >= peak2.getUsed()) {
            throw new RuntimeException(
                formatSize("After resetting peak: peak", peak3.getUsed()) + " " +
                " expected to be < " +
                formatSize("previous peak", peak2.getUsed()));
        }
        System.out.println("Test passed.");
    }
    private static String INDENT = "    ";
    private static void printMemoryUsage() {
        MemoryUsage current = mpool.getUsage();
        MemoryUsage peak = mpool.getPeakUsage();
        System.out.println("Current Usage: ");
        MemoryUtil.printMemoryUsage(current);
        System.out.println("Peak Usage: ");
        MemoryUtil.printMemoryUsage(peak);
    }
    private static String formatSize(String name, long value) {
        StringBuffer buf = new StringBuffer(name + " = " + value);
        if (value > 0) {
            buf.append(" (" + (value >> 10) + "K)");
        }
        return buf.toString();
    }
}
