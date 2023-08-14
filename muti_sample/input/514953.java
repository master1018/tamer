public class Test_ireturn extends DxTestCase {
    public void testN1() {
        T_ireturn_1 t = new T_ireturn_1();
        assertEquals(123456, t.run());
    }
    public void testN2() {
        T_ireturn_6 t = new T_ireturn_6();
        assertEquals(123456, t.run());
    }
    public void testN3() {
        assertTrue(T_ireturn_7.execute());
    }
    public void testE1() {
        T_ireturn_8 t = new T_ireturn_8();
        try {
            assertTrue(t.run());
            fail("expected IllegalMonitorStateException");
        } catch (IllegalMonitorStateException imse) {
        }
    }
    public void testE2() {
        T_ireturn_9 t = new T_ireturn_9();
        try {
            assertEquals(1, t.run());
            System.out.print("dvmvfe:");
        } catch (IllegalMonitorStateException imse) {
        }
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.ireturn.jm.T_ireturn_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.ireturn.jm.T_ireturn_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.ireturn.jm.T_ireturn_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dxc.junit.opcodes.ireturn.jm.T_ireturn_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE5() {
        try {
            Class.forName("dxc.junit.opcodes.ireturn.jm.T_ireturn_10");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE6() {
        try {
            Class.forName("dxc.junit.opcodes.ireturn.jm.T_ireturn_11");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
