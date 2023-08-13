public class Test_monitor_enter extends DxTestCase {
    public void testN1() throws InterruptedException {
        final T_monitor_enter_1 t1 = new T_monitor_enter_1();
        Runnable r1 = new TestRunnable(t1);
        Runnable r2 = new TestRunnable(t1);
        Thread tr1 = new Thread(r1);
        Thread tr2 = new Thread(r2);
        tr1.start();
        tr2.start();
        tr1.join();
        tr2.join();
        assertEquals(2, t1.counter);
    }
    public void testN2() throws InterruptedException {
        final T_monitor_enter_2 t1 = new T_monitor_enter_2();
        Runnable r1 = new TestRunnable2(t1, 10);
        Runnable r2 = new TestRunnable2(t1, 20);
        Thread tr1 = new Thread(r1);
        Thread tr2 = new Thread(r2);
        tr1.start();
        tr2.start();
        tr1.join();
        tr2.join();
        assertTrue(t1.result);
    }
    public void testE1() {
        T_monitor_enter_3 t = new T_monitor_enter_3();
        try {
            t.run();
            fail("expected NullPointerException");
        } catch (NullPointerException npe) {
        }
    }
    public void testVFE1() {
        try {
            Class.forName("dot.junit.opcodes.monitor_enter.d.T_monitor_enter_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dot.junit.opcodes.monitor_enter.d.T_monitor_enter_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dot.junit.opcodes.monitor_enter.d.T_monitor_enter_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dot.junit.opcodes.monitor_enter.d.T_monitor_enter_7");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE5() {
        try {
            Class.forName("dot.junit.opcodes.monitor_enter.d.T_monitor_enter_8");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
class TestRunnable implements Runnable {
    private T_monitor_enter_1 t1;
    TestRunnable(T_monitor_enter_1 t1) {
        this.t1 = t1;
    }
    public void run() {
        try {
            t1.run();
        } catch (InterruptedException e) {
            throw new RuntimeException("interrupted!");
        }
    }
}
class TestRunnable2 implements Runnable {
    private T_monitor_enter_2 t2;
    private int val;
    TestRunnable2(T_monitor_enter_2 t2, int val) {
        this.t2 = t2;
        this.val = val;
    }
    public void run() {
        try {
            t2.run(val);
        } catch (InterruptedException e) {
            throw new RuntimeException("interrupted!");
        }
    }
}
