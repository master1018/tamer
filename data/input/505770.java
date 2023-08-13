public class Test_ifle extends DxTestCase {
    public void testN1() {
        T_ifle_1 t = new T_ifle_1();
        assertEquals(1234, t.run(5));
    }
    public void testN2() {
        T_ifle_1 t = new T_ifle_1();
        assertEquals(1, t.run(0));
    }
    public void testN3() {
        T_ifle_1 t = new T_ifle_1();
        assertEquals(1, t.run(-5));
    }
    public void testB1() {
        T_ifle_1 t = new T_ifle_1();
        assertEquals(1234, t.run(Integer.MAX_VALUE));
    }
    public void testB2() {
        T_ifle_1 t = new T_ifle_1();
        assertEquals(1, t.run(Integer.MIN_VALUE));
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.ifle.jm.T_ifle_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.ifle.jm.T_ifle_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.ifle.jm.T_ifle_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dxc.junit.opcodes.ifle.jm.T_ifle_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE5() {
        try {
            Class.forName("dxc.junit.opcodes.ifle.jm.T_ifle_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE6() {
        try {
            Class.forName("dxc.junit.opcodes.ifle.jm.T_ifle_7");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
