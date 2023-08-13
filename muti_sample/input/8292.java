public class DGCDeadLock implements Runnable {
    final static public int HOLD_TARGET_TIME = 25000;
    public static int TEST_FAIL_TIME = HOLD_TARGET_TIME + 30000;
    public static boolean finished = false;
    static DGCDeadLock test = new DGCDeadLock();
    static {
        System.setProperty("sun.rmi.transport.cleanInterval", "50");
    }
    static public void main(String[] args) {
        JavaVM testImplVM = null;
        System.err.println("\nregression test for 4118056\n");
        TestLibrary.suggestSecurityManager("java.rmi.RMISecurityManager");
        try {
            String options = " -Djava.security.policy=" +
                TestParams.defaultPolicy +
                " -Djava.rmi.dgc.leaseValue=500000" +
                "  -Dsun.rmi.dgc.checkInterval=" +
                (HOLD_TARGET_TIME - 5000) + "";
            testImplVM = new JavaVM("TestImpl", options, "");
            testImplVM.start();
            synchronized (test) {
                Thread t = new Thread(test);
                t.setDaemon(true);
                t.start();
                test.wait(TEST_FAIL_TIME);
            }
            if (!finished) {
                TestLibrary.bomb("Test failed, had exception or exercise" +
                                           " routines took too long to " +
                                           "execute");
            }
            System.err.println("Test passed, exercises " +
                               "finished in time.");
        } catch (Exception e) {
            testImplVM = null;
            TestLibrary.bomb("test failed", e);
        }
    }
    public void run() {
        try {
            String echo = null;
            Thread.currentThread().sleep(8000);
            Test foo = (Test) Naming.lookup("rmi:
                                            TestLibrary.REGISTRY_PORT +
                                            "/Foo");
            echo = foo.echo("Hello world");
            System.err.println("Test object created.");
            Thread.currentThread().sleep(5000);
            foo = null;
            Runtime.getRuntime().gc();
            Runtime.getRuntime().runFinalization();
            Test bar = (Test) Naming.lookup("rmi:
                                            TestLibrary.REGISTRY_PORT +
                                            "/Bar");
            try {
                for (int i = 0; i < 500; i++) {
                    echo = bar.echo("Remote call" + i);
                    Thread.sleep(10);
                }
                finished = true;
            } catch (RemoteException e) {
            }
        } catch (Exception e) {
            TestLibrary.bomb("test failed", e);
        } finally {
        }
    }
}
