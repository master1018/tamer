public class SecondaryLoopTest {
    private static volatile boolean loopStarted;
    private static volatile boolean doubleEntered;
    private static volatile boolean loopActive;
    private static volatile boolean eventDispatched;
    public static void main(String[] args) throws Exception {
        test(true, true);
        test(true, false);
        test(false, true);
        test(false, false);
    }
    private static void test(final boolean enterEDT, final boolean exitEDT) throws Exception {
        System.out.println("Running test(" + enterEDT + ", " + exitEDT + ")");
        System.err.flush();
        loopStarted = true;
        Runnable enterRun = new Runnable() {
            @Override
            public void run() {
                Toolkit tk = Toolkit.getDefaultToolkit();
                EventQueue eq = tk.getSystemEventQueue();
                final SecondaryLoop loop = eq.createSecondaryLoop();
                doubleEntered = false;
                eventDispatched = false;
                Runnable eventRun = new Runnable() {
                    @Override
                    public void run() {
                        sleep(1000);
                        if (loop.enter()) {
                            doubleEntered = true;
                        }
                        eventDispatched = true;
                    }
                };
                EventQueue.invokeLater(eventRun);
                Runnable exitRun = new Runnable() {
                    @Override
                    public void run() {
                        sleep(2000);
                        if (doubleEntered) {
                            loop.exit();
                        }
                        loop.exit();
                    }
                };
                if (exitEDT) {
                    EventQueue.invokeLater(exitRun);
                } else {
                    new Thread(exitRun).start();
                }
                if (!loop.enter()) {
                    loopStarted = false;
                }
                loopActive = eventDispatched;
            }
        };
        if (enterEDT) {
            EventQueue.invokeAndWait(enterRun);
        } else {
            enterRun.run();
        }
        System.out.println("    loopStarted = " + loopStarted);
        System.out.println("    doubleEntered = " + doubleEntered);
        System.out.println("    loopActive = " + loopActive);
        System.out.flush();
        if (!loopStarted) {
            throw new RuntimeException("Test FAILED: the secondary loop is not started");
        }
        if (doubleEntered) {
            throw new RuntimeException("Test FAILED: the secondary loop is started twice");
        }
        if (!loopActive) {
            throw new RuntimeException("Test FAILED: the secondary loop exited immediately");
        }
    }
    private static void sleep(long t) {
        try {
            Thread.sleep(t);
        } catch (InterruptedException e) {
            e.printStackTrace(System.err);
        }
    }
}
