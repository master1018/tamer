public class Test_i2c extends DxTestCase {
    public void testN1() {
        T_i2c_1 t = new T_i2c_1();
        assertEquals('A', t.run(65));
    }
    public void testN2() {
        T_i2c_1 t = new T_i2c_1();
        assertEquals('\u0001', t.run(65537));
    }
    public void testN3() {
        T_i2c_1 t = new T_i2c_1();
        assertEquals('\ufffe', t.run(-2));
    }
    public void testN4() {
        T_i2c_1 t = new T_i2c_1();
        assertEquals('\u0000', t.run(0x110000));
    }
    public void testB1() {
        T_i2c_1 t = new T_i2c_1();
        assertEquals('\u0000', t.run(0));
    }
    public void testB2() {
        T_i2c_1 t = new T_i2c_1();
        assertEquals('\uffff', t.run(65535));
    }
    public void testB3() {
        T_i2c_1 t = new T_i2c_1();
        assertEquals('\uffff', t.run(Integer.MAX_VALUE));
    }
    public void testB4() {
        T_i2c_1 t = new T_i2c_1();
        assertEquals('\u0000', t.run(Integer.MIN_VALUE));
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.i2c.jm.T_i2c_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.i2c.jm.T_i2c_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.i2c.jm.T_i2c_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dxc.junit.opcodes.i2c.jm.T_i2c_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
