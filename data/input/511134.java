public class Test_freturn extends DxTestCase {
    public void testN1() {
        T_freturn_1 t = new T_freturn_1();
        assertEquals(123456f, t.run());
    }
    public void testN2() {
        T_freturn_6 t = new T_freturn_6();
        assertEquals(123456f, t.run());
    }
    public void testN3() {
        assertTrue(T_freturn_7.execute());
    }
    public void testE1() {
        T_freturn_8 t = new T_freturn_8();
        try {
            assertTrue(t.run());
            fail("expected IllegalMonitorStateException");
        } catch (IllegalMonitorStateException imse) {
        }
    }
    public void testE2() {
        T_freturn_9 t = new T_freturn_9();
        try {
            assertEquals(1f, t.run());
            System.out.print("dvmvfe:");
        } catch (IllegalMonitorStateException imse) {
        }
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.freturn.jm.T_freturn_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.freturn.jm.T_freturn_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.freturn.jm.T_freturn_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dxc.junit.opcodes.freturn.jm.T_freturn_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE5() {
        try {
            Class.forName("dxc.junit.opcodes.freturn.jm.T_freturn_10");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE6() {
        try {
            Class.forName("dxc.junit.opcodes.freturn.jm.T_freturn_11");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
