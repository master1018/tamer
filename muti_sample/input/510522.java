public class Test_monitorexit extends DxTestCase {
    public void testE1() throws InterruptedException {
        final T_monitorexit_2 t = new T_monitorexit_2();
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
    public void testE2() {
        T_monitorexit_3 t = new T_monitorexit_3();
        try {
            t.run();
            fail("expected IllegalMonitorStateException");
        } catch (IllegalMonitorStateException imse) {
        }
    }
    public void testE3() {
        T_monitorexit_4 t = new T_monitorexit_4();
        try {
            t.run();
            fail("expected NullPointerException");
        } catch (NullPointerException npe) {
        }
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.monitorexit.jm.T_monitorexit_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.monitorexit.jm.T_monitorexit_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
class TestRunnable implements Runnable {
    private T_monitorexit_2 t;
    private Object o;
    public TestRunnable(T_monitorexit_2 t, Object o) {
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
