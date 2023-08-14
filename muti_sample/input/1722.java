public class GcDuringExport {
    private static final long MAX_EXPORT_ITERATIONS = 50000;
    public static void main(String[] args) throws Exception {
        Thread gcInducingThread = new Thread() {
            public void run() {
                while (true) {
                    System.gc();
                    try { Thread.sleep(1); } catch (InterruptedException e) { }
                }
            }
        };
        gcInducingThread.setDaemon(true);
        gcInducingThread.start();
        long i = 0;
        try {
            while (i < MAX_EXPORT_ITERATIONS) {
                i++;
                UnicastRemoteObject.exportObject(new Remote() { }, 0);
            }
        } catch (Throwable e) {
            throw new RuntimeException("Test FAILED on iteration " + i + ".", e);
        }
        System.out.println("Test successfully exported " + i + " objects.");
    }
}
