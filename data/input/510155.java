public class Test_athrow extends DxTestCase {
    public void testN1() {
        T_athrow_1 t = new T_athrow_1();
        try {
            t.run();
            fail("must throw a RuntimeException");
        } catch (RuntimeException re) {
        }
    }
    public void testN2() {
        T_athrow_2 t = new T_athrow_2();
        try {
            t.run();
            fail("must throw a Throwable");
        } catch (Throwable e) {
        }
    }
    public void testN3() {
        T_athrow_8 t = new T_athrow_8();
        try {
            t.run();
            fail("must throw a Error");
        } catch (Error e) {
        }
    }
    public void testN4() {
        T_athrow_12 t = new T_athrow_12();
        assertTrue(t.run());
    }
    public void testE1() {
        T_athrow_4 t = new T_athrow_4();
        try {
            t.run();
            fail("expected NullPointerException");
        } catch (NullPointerException npe) {
        }
    }
    public void testE2() {
        T_athrow_5 t = new T_athrow_5();
        try {
            t.run();
            fail("expected IllegalMonitorStateException");
        } catch (IllegalMonitorStateException imse) {
        }
    }
    public void testE3() {
        T_athrow_11 t = new T_athrow_11();
        try {
            t.run();
            fail("expected IllegalMonitorStateException");
        } catch (IllegalMonitorStateException imse) {
        } catch (NullPointerException npe) {
            System.out.print("dvmvfe:");
        }
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.athrow.jm.T_athrow_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.athrow.jm.T_athrow_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.athrow.jm.T_athrow_7");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dxc.junit.opcodes.athrow.jm.T_athrow_10");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE5() {
        try {
            Class.forName("dxc.junit.opcodes.athrow.jm.T_athrow_9");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
