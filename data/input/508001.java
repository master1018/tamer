public class Test_i2d extends DxTestCase {
    public void testN1() {
        T_i2d_1 t = new T_i2d_1();
        assertEquals(300000000d, t.run(300000000), 0d);
    }
    public void testN2() {
        T_i2d_1 t = new T_i2d_1();
        assertEquals(1d, t.run(1), 0d);
    }
    public void testN3() {
        T_i2d_1 t = new T_i2d_1();
        assertEquals(-1d, t.run(-1), 0d);
    }
    public void testB1() {
        T_i2d_1 t = new T_i2d_1();
        assertEquals(2147483647d, t.run(Integer.MAX_VALUE), 0d);
    }
    public void testB2() {
        T_i2d_1 t = new T_i2d_1();
        assertEquals(-2147483648d, t.run(Integer.MIN_VALUE), 0d);
    }
    public void testB3() {
        T_i2d_1 t = new T_i2d_1();
        assertEquals(0d, t.run(0), 0d);
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.i2d.jm.T_i2d_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.i2d.jm.T_i2d_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.i2d.jm.T_i2d_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dxc.junit.opcodes.i2d.jm.T_i2d_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE5() {
        try {
            Class.forName("dxc.junit.opcodes.i2d.jm.T_i2d_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
