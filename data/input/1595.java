public class Pending {
    final static int NO_REF_COUNT = 600;
    final static int REF_COUNT = 600;
    final static int TOTAL_FINALIZABLE = (NO_REF_COUNT + REF_COUNT);
    private static int finalized = 0;
    private static MemoryMXBean mbean
        = ManagementFactory.getMemoryMXBean();
    private static final String INDENT = "      ";
    private static void printFinalizerInstanceCount() {
        if (!trace) return;
        int count = sun.misc.VM.getFinalRefCount();
        System.out.println(INDENT + "Finalizable object Count = " + count);
        count = sun.misc.VM.getPeakFinalRefCount();
        System.out.println(INDENT + "Peak Finalizable object Count = " + count);
    }
    private static boolean trace = false;
    public static void main(String argv[]) throws Exception {
        if (argv.length > 0 && argv[0].equals("trace")) {
            trace = true;
        }
        try {
            if (trace) {
                mbean.setVerbose(true);
            }
            test();
        } finally {
            if (trace) {
                mbean.setVerbose(false);
            }
        }
        System.out.println("Test passed.");
    }
    private static void test() throws Exception {
        System.gc();
        Runtime.getRuntime().runFinalization();
        try {
            Thread.sleep(200);
        } catch (Exception e) {
            throw e;
        }
        int startCount = mbean.getObjectPendingFinalizationCount();
        System.out.println("Number of objects pending for finalization:");
        System.out.println("   Before creating object: " + startCount +
            " finalized = " + finalized);
        printFinalizerInstanceCount();
        for (int i = 0; i < NO_REF_COUNT; i++) {
            new MyObject();
        }
        Snapshot snapshot = getSnapshot();
        System.out.println("   Afer creating objects with no ref: " + snapshot);
        printFinalizerInstanceCount();
        Object[] objs = new Object[REF_COUNT];
        for (int i = 0; i < REF_COUNT; i++) {
            objs[i] = new MyObject();
        }
        snapshot = getSnapshot();
        System.out.println("   Afer creating objects with ref: " + snapshot);
        printFinalizerInstanceCount();
        checkFinalizerCount(NO_REF_COUNT, 0);
        objs = null;
        snapshot = getSnapshot();
        System.out.println("Clear all references finalized = " + snapshot);
        printFinalizerInstanceCount();
        checkFinalizerCount(TOTAL_FINALIZABLE, NO_REF_COUNT);
        snapshot = getSnapshot();
        printFinalizerInstanceCount();
        if (snapshot.curFinalized != TOTAL_FINALIZABLE) {
            throw new RuntimeException("Wrong number of finalized objects "
                                     + snapshot + ". Expected "
                                     + TOTAL_FINALIZABLE);
        }
        if (startCount != 0 || snapshot.curPending != 0) {
            throw new RuntimeException("Wrong number of objects pending "
                                     + "finalization start = " + startCount
                                     + " end = " + snapshot);
        }
    }
    private static void checkFinalizerCount(int expectedTotal, int curFinalized)
        throws Exception {
        int prevCount = -1;
        Snapshot snapshot = getSnapshot();
        if (snapshot.curFinalized != curFinalized) {
            throw new RuntimeException(
                    "Unexpected finalized objects: " + snapshot +
                    " but expected = " + curFinalized);
        }
        int MAX_GC_LOOP = 6;
        for (int i = 1;
             snapshot.curFinalized != expectedTotal && i <= MAX_GC_LOOP;
             i++) {
            System.gc();
            pause();
            printFinalizerInstanceCount();
            for (int j = 0; j < 5; j++) {
                snapshot = getSnapshot();
                if (snapshot.curFinalized == expectedTotal ||
                    snapshot.curPending != 0) {
                    break;
                }
            }
            System.out.println("   After GC " + i + ": " + snapshot);
            Runtime.getRuntime().runFinalization();
            pause();
            snapshot = getSnapshot();
            if (snapshot.curFinalized == expectedTotal &&
                snapshot.curPending != 0) {
                throw new RuntimeException(
                    "Unexpected current number of objects pending for " +
                    "finalization: " + snapshot + " but expected = 0");
            }
            System.out.println("   After runFinalization " + i + ": " + snapshot);
            printFinalizerInstanceCount();
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                throw e;
            }
        }
        if (snapshot.curFinalized != expectedTotal) {
            throw new RuntimeException(
                "Unexpected current number of objects pending for " +
                "finalization: " + snapshot + " but expected > 0");
        }
    }
    private static Object lock = new Object();
    private static class MyObject {
        Object[] dummy = new Object[10];
        public void finalize () {
            synchronized (lock) {
                finalized++;
            }
        }
    }
    static class Snapshot {
        public int curFinalized;
        public int curPending;
        Snapshot(int f, int p) {
            curFinalized = f;
            curPending = p;
        }
        public String toString() {
            return "Current finalized = " + curFinalized +
                   " Current pending = " + curPending;
        }
    }
    private static Snapshot getSnapshot() {
        synchronized (lock) {
            int curCount = mbean.getObjectPendingFinalizationCount();
            return new Snapshot(finalized, curCount);
        }
    }
    private static Object pauseObj = new Object();
    private static void pause() {
        synchronized (pauseObj) {
            try {
                pauseObj.wait(20);
            } catch (Exception e) {
                System.err.println("Unexpected exception.");
                e.printStackTrace(System.err);
            }
        }
    }
}
