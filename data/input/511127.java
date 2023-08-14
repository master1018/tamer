public class Test_i2f extends DxTestCase {
    public void testN1() {
        T_i2f_1 t = new T_i2f_1();
        assertEquals(123456f, t.run(123456), 0f);
    }
    public void testN2() {
        T_i2f_1 t = new T_i2f_1();
        assertEquals(1f, t.run(1), 0f);
    }
    public void testN3() {
        T_i2f_1 t = new T_i2f_1();
        assertEquals(-1f, t.run(-1), 0f);
    }
    public void testN4() {
        T_i2f_1 t = new T_i2f_1();
        assertEquals(3.356444E7f, t.run(33564439), 0f);
    }
    public void testB1() {
        T_i2f_1 t = new T_i2f_1();
        assertEquals(0f, t.run(0), 0f);
    }
    public void testB2() {
        T_i2f_1 t = new T_i2f_1();
        assertEquals(2147483650f, t.run(Integer.MAX_VALUE), 0f);
    }
    public void testB3() {
        T_i2f_1 t = new T_i2f_1();
        assertEquals(-2147483650f, t.run(Integer.MIN_VALUE), 0f);
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.i2f.jm.T_i2f_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.i2f.jm.T_i2f_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.i2f.jm.T_i2f_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dxc.junit.opcodes.i2f.jm.T_i2f_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
