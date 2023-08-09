public class Test_i2b extends DxTestCase {
    public void testN1() {
        T_i2b_1 t = new T_i2b_1();
        assertEquals(1, t.run(1));
    }
    public void testN2() {
        T_i2b_1 t = new T_i2b_1();
        assertEquals(-1, t.run(-1));
    }
    public void testN3() {
        T_i2b_1 t = new T_i2b_1();
        assertEquals(16, t.run(16));
    }
    public void testN4() {
        T_i2b_1 t = new T_i2b_1();
        assertEquals(-32, t.run(-32));
    }
    public void testN5() {
        T_i2b_1 t = new T_i2b_1();
        assertEquals(-122, t.run(134));
    }
    public void testN6() {
        T_i2b_1 t = new T_i2b_1();
        assertEquals(122, t.run(-134));
    }
    public void testB1() {
        T_i2b_1 t = new T_i2b_1();
        assertEquals(127, t.run(127));
    }
    public void testB2() {
        T_i2b_1 t = new T_i2b_1();
        assertEquals(-128, t.run(128));
    }
    public void testB3() {
        T_i2b_1 t = new T_i2b_1();
        assertEquals(0, t.run(0));
    }
    public void testB4() {
        T_i2b_1 t = new T_i2b_1();
        assertEquals(-128, t.run(-128));
    }
    public void testB5() {
        T_i2b_1 t = new T_i2b_1();
        assertEquals(127, t.run(-129));
    }
    public void testB6() {
        T_i2b_1 t = new T_i2b_1();
        assertEquals(-1, t.run(Integer.MAX_VALUE));
    }
    public void testB7() {
        T_i2b_1 t = new T_i2b_1();
        assertEquals(0, t.run(Integer.MIN_VALUE));
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.i2b.jm.T_i2b_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.i2b.jm.T_i2b_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.i2b.jm.T_i2b_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dxc.junit.opcodes.i2b.jm.T_i2b_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
