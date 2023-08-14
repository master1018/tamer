public class MTReadProfileTest {
    public final static int THREAD_COUNT = 30;
    public static void main(String [] args) throws InterruptedException {
        Thread [] threads = new Thread[THREAD_COUNT];
        ReadProfileTest [] tests = new ReadProfileTest[THREAD_COUNT];
        for (int i = 0; i < THREAD_COUNT; i++) {
            tests[i] = new ReadProfileTest();
            threads[i] = new Thread(tests[i]);
        }
        for (int i = 0; i < threads.length; i++) {
            threads[i].start();
        }
        for (int i = 0; i < threads.length; i++) {
            threads[i].join();
            if (!tests[i].getStatus()) {
                throw new RuntimeException("Error in MT reading of the ICC " +
                                           "profiles");
            }
        }
    }
}
