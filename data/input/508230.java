public class Test_monitor_exit extends DxTestCase {
    public void testE1() throws InterruptedException {
        final T_monitor_exit_1 t = new T_monitor_exit_1();
        final Object o = new Object();
        Runnable r = new TestRunnable(t, o);
        synchronized (o) {
            Thread th = new Thread(r);
            th.start();
            th.join();
        }
        if (t.result == false) {
            fail("expected IllegalMonitorStateException");
        }
    }
    public void testE3() {
        T_monitor_exit_3 t = new T_monitor_exit_3();
        try {
            t.run();
            fail("expected NullPointerException");
        } catch (NullPointerException npe) {
        }
    }
    public void testVFE1() {
        try {
            Class.forName("dot.junit.opcodes.monitor_exit.d.T_monitor_exit_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dot.junit.opcodes.monitor_exit.d.T_monitor_exit_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dot.junit.opcodes.monitor_exit.d.T_monitor_exit_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dot.junit.opcodes.monitor_exit.d.T_monitor_exit_7");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE5() {
        try {
            Class.forName("dot.junit.opcodes.monitor_exit.d.T_monitor_exit_8");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
class TestRunnable implements Runnable {
    private T_monitor_exit_1 t;
    private Object o;
    public TestRunnable(T_monitor_exit_1 t, Object o) {
        this.t = t;
        this.o = o;
    }
    public void run() {
        try {
            t.run(o);
        } catch (IllegalMonitorStateException imse) {
            t.result = true;
        }
    }
}
