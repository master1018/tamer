public class MTColConvTest {
    public final static int THREAD_COUNT = 10;
    public static void main(String [] args) throws InterruptedException {
        Thread [] threads = new Thread[THREAD_COUNT];
        ColConvTest [] tests = new ColConvTest[THREAD_COUNT];
        for (int i = 0; i < threads.length; i+=2) {
            tests[i]= new ColConvDCMTest();
            tests[i].init();
            threads[i] = new Thread(tests[i]);
            tests[i+1] = new ColConvCCMTest();
            tests[i+1].init();
            threads[i+1] = new Thread(tests[i+1]);
        }
        for (int i = 0; i < threads.length; i++) {
            threads[i].start();
        }
        boolean isPassed = true;
        for (int i = 0; i < threads.length; i++) {
            threads[i].join();
            isPassed = isPassed && tests[i].isPassed();
        }
        if (!isPassed) {
            throw new RuntimeException("MT Color Conversion error");
        }
    }
}
