public class Test_dup_x2 extends DxTestCase {
    public void testN1() {
        T_dup_x2_1 t = new T_dup_x2_1();
        assertTrue(t.run());
    }
    public void testN2() {
        T_dup_x2_6 t = new T_dup_x2_6();
        assertTrue(t.run());
    }
    public void testN3() {
        T_dup_x2_7 t = new T_dup_x2_7();
        assertTrue(t.run());
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.dup_x2.jm.T_dup_x2_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.dup_x2.jm.T_dup_x2_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.dup_x2.jm.T_dup_x2_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dxc.junit.opcodes.dup_x2.jm.T_dup_x2_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
