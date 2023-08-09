public class WakeupSpeed {
    public static void main(String argv[]) throws Exception {
        int waitTime = 4000;
        Selector selector = Selector.open();
        try {
            selector.wakeup();
            long t1 = System.currentTimeMillis();
            selector.select(waitTime);
            long t2 = System.currentTimeMillis();
            long totalTime = t2 - t1;
            if (totalTime > waitTime)
                throw new RuntimeException("Test failed");
        } finally {
            selector.close();
        }
    }
}
