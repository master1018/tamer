public class LoopRobustness {
    final static long TIMEOUT = 5000;
    final static Object LOCK = new Object();
    public static int clicks = 0;
    public static volatile boolean notifyOccured = false;
    public static volatile boolean otherExceptionsCaught = false;
    public static void main(String [] args) throws Exception {
        ThreadGroup mainThreadGroup = Thread.currentThread().getThreadGroup();
        long at;
        synchronized (LoopRobustness.LOCK) {
            new Thread(new TestThreadGroup(mainThreadGroup, "TestGroup"), new Impl()).start();
            at = System.currentTimeMillis();
            try {
                while (!notifyOccured && (System.currentTimeMillis() - at < TIMEOUT)) {
                    LoopRobustness.LOCK.wait(1000);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException("Test interrupted.", e);
            }
        }
        if (!notifyOccured) {
            throw new RuntimeException("Test FAILED: second thread hasn't notified MainThread");
        }
        at = System.currentTimeMillis();
        while(System.currentTimeMillis() - at < TIMEOUT && clicks < 2) {
            try {
                Thread.sleep(100);
            } catch(InterruptedException e) {
                throw new RuntimeException("Test interrupted.", e);
            }
        }
        if (clicks != 2) {
            throw new RuntimeException("Test FAILED: robot should press button twice");
        }
        if (otherExceptionsCaught) {
            throw new RuntimeException("Test FAILED: unexpected exceptions caught");
        }
    }
}
class Impl implements Runnable{
    static Robot robot;
    public void run() {
        SunToolkit.createNewAppContext();
        Button b = new Button("Press me to test the AWT-Event Queue thread");
        Frame lr = new Frame("ROBUST FRAME");
        lr.setBounds(100, 100, 300, 100);
        b.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    LoopRobustness.clicks++;
                    System.out.println(HostileCrasher.aStaticMethod());
                }
            });
        lr.add(b);
        lr.setVisible(true);
        try {
            robot = new Robot();
        } catch (AWTException e) {
            throw new RuntimeException("Test interrupted.", e);
        }
        Util.waitForIdle(robot);
        synchronized (LoopRobustness.LOCK){
            LoopRobustness.LOCK.notify();
            LoopRobustness.notifyOccured = true;
        }
        int i = 0;
        while (i < 2) {
            robot.mouseMove(b.getLocationOnScreen().x + b.getWidth()/2,
                            b.getLocationOnScreen().y + b.getHeight()/2);
            Util.waitForIdle(robot);
            robot.mousePress(InputEvent.BUTTON1_MASK);
            Util.waitForIdle(robot);
            robot.mouseRelease(InputEvent.BUTTON1_MASK);
            Util.waitForIdle(robot);
            i++;
        }
    }
}
class TestThreadGroup extends ThreadGroup {
    TestThreadGroup(ThreadGroup threadGroup, String name) {
        super(threadGroup, name);
    }
    public void uncaughtException(Thread thread, Throwable e) {
        System.out.println("Exception caught: " + e);
        e.printStackTrace(System.out);
        System.out.flush();
        if ((e instanceof ExceptionInInitializerError) ||
            (e instanceof NoClassDefFoundError))
        {
            return;
        }
        LoopRobustness.otherExceptionsCaught = true;
    }
}
class HostileCrasher {
    static {
        if (Math.random() >= 0.0) {
            throw new RuntimeException("Die, AWT-Event Queue thread!");
        }
    }
    public static String aStaticMethod() {
        return "hello, world";
    }
}
