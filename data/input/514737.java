public class Test_ifnull extends DxTestCase {
    public void testN1() {
        T_ifnull_1 t = new T_ifnull_1();
        assertEquals(1234, t.run(this));
    }
    public void testN2() {
        T_ifnull_1 t = new T_ifnull_1();
        assertEquals(1, t.run(null));
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.ifnull.jm.T_ifnull_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.ifnull.jm.T_ifnull_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.ifnull.jm.T_ifnull_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dxc.junit.opcodes.ifnull.jm.T_ifnull_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE5() {
        try {
            Class.forName("dxc.junit.opcodes.ifnull.jm.T_ifnull_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
