public class InfiniteValidationLoopTest extends Frame {
    private static volatile boolean failed = false;
    private static final int LOOP_THRESHOLD = 50;
    private static volatile CountDownLatch latch;
    private VolatileImage vi;
    public InfiniteValidationLoopTest() {
        super("InfiniteValidationLoopTest");
    }
    @Override
    public void paint(Graphics g) {
        try {
            runTest(g);
        } finally {
            latch.countDown();
        }
    }
    private void runTest(Graphics g) {
        int status = IMAGE_OK;
        int count1 = 0;
        do {
            GraphicsConfiguration gc = getGraphicsConfiguration();
            int count2 = 0;
            while (vi == null || (status = vi.validate(gc)) != IMAGE_OK) {
                if (++count2 > LOOP_THRESHOLD) {
                    System.err.println("Infinite loop detected: count2="+count2);
                    failed = true;
                    return;
                }
                if (vi == null || status == IMAGE_INCOMPATIBLE) {
                    if (vi != null) { vi.flush(); vi = null; }
                    vi = gc.createCompatibleVolatileImage(100, 100);
                    continue;
                }
                if (status == IMAGE_RESTORED) {
                    Graphics gg = vi.getGraphics();
                    gg.setColor(Color.green);
                    gg.fillRect(0, 0, vi.getWidth(), vi.getHeight());
                    break;
                }
            }
            g.drawImage(vi, getInsets().left, getInsets().top, null);
            if (++count1 > LOOP_THRESHOLD) {
                System.err.println("Infinite loop detected: count1="+count1);
                failed = true;
                return;
            }
        } while (vi.contentsLost());
    }
    public static void main(String[] args) {
        latch = new CountDownLatch(1);
        InfiniteValidationLoopTest t1 = new InfiniteValidationLoopTest();
        t1.pack();
        t1.setSize(200, 200);
        t1.setVisible(true);
        try { latch.await(); } catch (InterruptedException ex) {}
        t1.dispose();
        latch = new CountDownLatch(1);
        t1 = new InfiniteValidationLoopTest();
        t1.pack();
        t1.setSize(50, 50);
        t1.setVisible(true);
        try { latch.await(); } catch (InterruptedException ex) {}
        t1.dispose();
        if (failed) {
            throw new
                RuntimeException("Failed: infinite validattion loop detected");
        }
        System.out.println("Test PASSED");
    }
}
