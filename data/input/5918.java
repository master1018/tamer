public final class TestThreadId extends Thread {
    private static final int ITERATIONCOUNT = 50;
    private static final int THREADCOUNT = 50;
    private static ThreadId id = new ThreadId();
    private int value;
    private synchronized int getIdValue() {
        return value;
    }
    public void run() {
        value = id.get();
    }
    public static void main(String args[]) throws Throwable {
        boolean check[] = new boolean[THREADCOUNT*ITERATIONCOUNT];
        TestThreadId u[] = new TestThreadId[THREADCOUNT];
        for (int i = 0; i < ITERATIONCOUNT; i++) {
            for (int t=0;t<THREADCOUNT;t++) {
                u[t] = new TestThreadId();
                u[t].start();
            }
            for (int t=0;t<THREADCOUNT;t++) {
                try {
                    u[t].join();
                } catch (InterruptedException e) {
                     throw new RuntimeException(
                        "TestThreadId: Failed with unexpected exception" + e);
                }
                try {
                    if (check[u[t].getIdValue()]) {
                        throw new RuntimeException(
                            "TestThreadId: Failed with duplicated id: " +
                                u[t].getIdValue());
                    } else {
                        check[u[t].getIdValue()] = true;
                    }
                } catch (Exception e) {
                    throw new RuntimeException(
                        "TestThreadId: Failed with unexpected id value" + e);
                }
            }
        }
    } 
} 
