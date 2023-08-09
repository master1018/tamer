public class Test_lreturn extends DxTestCase {
    public void testN1() {
        T_lreturn_1 t = new T_lreturn_1();
        assertEquals(12345612345l, t.run());
    }
    public void testN2() {
        T_lreturn_6 t = new T_lreturn_6();
        assertEquals(0l, t.run());
    }
    public void testN3() {
        assertTrue(T_lreturn_7.execute());
    }
    public void testE1() {
        T_lreturn_8 t = new T_lreturn_8();
        try {
            assertTrue(t.run());
            fail("expected IllegalMonitorStateException");
        } catch (IllegalMonitorStateException imse) {
        }
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.lreturn.jm.T_lreturn_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.lreturn.jm.T_lreturn_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.lreturn.jm.T_lreturn_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dxc.junit.opcodes.lreturn.jm.T_lreturn_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE5() {
        try {
            Class.forName("dxc.junit.opcodes.lreturn.jm.T_lreturn_10");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE6() {
        try {
            Class.forName("dxc.junit.opcodes.lreturn.jm.T_lreturn_11");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
