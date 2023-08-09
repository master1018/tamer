public class Test_dreturn extends DxTestCase {
    public void testN1() {
        T_dreturn_1 t = new T_dreturn_1();
        assertEquals(123456d, t.run());
    }
    public void testN2() {
        T_dreturn_6 t = new T_dreturn_6();
        assertEquals(123456d, t.run());
    }
    public void testN3() {
        assertTrue(T_dreturn_7.execute());
    }
    public void testE1() {
        T_dreturn_8 t = new T_dreturn_8();
        try {
            assertTrue(t.run());
            fail("expected IllegalMonitorStateException");
        } catch (IllegalMonitorStateException imse) {
        }
    }
    public void testE2() {
        T_dreturn_9 t = new T_dreturn_9();
        try {
            assertEquals(1d, t.run());
            System.out.print("dvmvfe:");
        } catch (IllegalMonitorStateException imse) {
        }
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.dreturn.jm.T_dreturn_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.dreturn.jm.T_dreturn_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.dreturn.jm.T_dreturn_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dxc.junit.opcodes.dreturn.jm.T_dreturn_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE5() {
        try {
            Class.forName("dxc.junit.opcodes.dreturn.jm.T_dreturn_10");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE6() {
        try {
            Class.forName("dxc.junit.opcodes.dreturn.jm.T_dreturn_11");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
