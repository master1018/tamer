public class Test_iflt extends DxTestCase {
    public void testN1() {
        T_iflt_1 t = new T_iflt_1();
        assertEquals(1234, t.run(5));
    }
    public void testN2() {
        T_iflt_1 t = new T_iflt_1();
        assertEquals(1234, t.run(0));
    }
    public void testN3() {
        T_iflt_1 t = new T_iflt_1();
        assertEquals(1, t.run(-5));
    }
    public void testB1() {
        T_iflt_1 t = new T_iflt_1();
        assertEquals(1234, t.run(Integer.MAX_VALUE));
    }
    public void testB2() {
        T_iflt_1 t = new T_iflt_1();
        assertEquals(1, t.run(Integer.MIN_VALUE));
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.iflt.jm.T_iflt_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.iflt.jm.T_iflt_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.iflt.jm.T_iflt_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dxc.junit.opcodes.iflt.jm.T_iflt_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE5() {
        try {
            Class.forName("dxc.junit.opcodes.iflt.jm.T_iflt_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE6() {
        try {
            Class.forName("dxc.junit.opcodes.iflt.jm.T_iflt_7");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
