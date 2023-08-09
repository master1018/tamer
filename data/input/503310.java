public class BadFinalizer {
    public static void snooze(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ie) {
            System.out.println("Snooze: " + ie.getMessage());
        }
    }
    protected void finalize() {
        System.out.println("Finalizer started and spinning...");
        int j = 0;
        long start, end;
        start = System.nanoTime();
        for (int i = 0; i < 1000000; i++)
            j++;
        end = System.nanoTime();
        System.out.println("Finalizer done spinning.");
        System.out.println("Finalizer sleeping forever now.");
        while (true) {
            snooze(10000);
        }
    }
}
