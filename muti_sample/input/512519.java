public class Test_ifnonnull extends DxTestCase {
    public void testN1() {
        T_ifnonnull_1 t = new T_ifnonnull_1();
        assertEquals(1, t.run(this));
    }
    public void testN2() {
        T_ifnonnull_1 t = new T_ifnonnull_1();
        assertEquals(1234, t.run(null));
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.ifnonnull.jm.T_ifnonnull_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.ifnonnull.jm.T_ifnonnull_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.ifnonnull.jm.T_ifnonnull_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dxc.junit.opcodes.ifnonnull.jm.T_ifnonnull_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE5() {
        try {
            Class.forName("dxc.junit.opcodes.ifnonnull.jm.T_ifnonnull_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
