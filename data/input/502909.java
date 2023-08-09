public class Test_d2f extends DxTestCase {
    public void testN1() {
        T_d2f_1 t = new T_d2f_1();
        assertEquals(2.71f, t.run(2.71d));
    }
    public void testN2() {
        T_d2f_1 t = new T_d2f_1();
        assertEquals(1f, t.run(1d));
    }
    public void testN3() {
        T_d2f_1 t = new T_d2f_1();
        assertEquals(-1f, t.run(-1d));
    }
    public void testB1() {
        T_d2f_1 t = new T_d2f_1();
        assertEquals(Float.POSITIVE_INFINITY, t.run(Double.MAX_VALUE));
    }
    public void testB2() {
        T_d2f_1 t = new T_d2f_1();
        assertEquals(0f, t.run(Double.MIN_VALUE));
    }
    public void testB3() {
        T_d2f_1 t = new T_d2f_1();
        assertEquals(-0f, t.run(-0d));
    }
    public void testB4() {
        T_d2f_1 t = new T_d2f_1();
        assertTrue(Float.isNaN(t.run(Double.NaN)));
    }
    public void testB5() {
        T_d2f_1 t = new T_d2f_1();
        assertTrue(Float.isInfinite(t.run(Double.POSITIVE_INFINITY)));
    }
    public void testB6() {
        T_d2f_1 t = new T_d2f_1();
        assertTrue(Float.isInfinite(t.run(Double.NEGATIVE_INFINITY)));
    }
    public void testB7() {
        T_d2f_1 t = new T_d2f_1();
        assertEquals(-0f, t.run(-4.9E-324d));
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.d2f.jm.T_d2f_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.d2f.jm.T_d2f_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.d2f.jm.T_d2f_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dxc.junit.opcodes.d2f.jm.T_d2f_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
