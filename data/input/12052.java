public class RapidExportUnexport {
    private static final int PORT = 2055;
    private static final int REPS = 100;
    private static final long TIMEOUT = 60000;
    public static void main(String[] args) throws Exception {
        System.err.println("\nRegression test for bug 6275081\n");
        Remote impl = new Remote() { };
        long start = System.currentTimeMillis();
        for (int i = 0; i < REPS; i++) {
            System.err.println(i);
            UnicastRemoteObject.exportObject(impl, PORT);
            UnicastRemoteObject.unexportObject(impl, true);
            Thread.sleep(1);    
        }
        long delta = System.currentTimeMillis() - start;
        System.err.println(REPS + " export/unexport operations took " +
                           delta + "ms");
        if (delta > TIMEOUT) {
            throw new Error("TEST FAILED: took over " + TIMEOUT + "ms");
        }
        System.err.println("TEST PASSED");
    }
}
