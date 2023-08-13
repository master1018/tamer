public class Test_double_to_float extends DxTestCase {
    public void testN1() {
        T_double_to_float_1 t = new T_double_to_float_1();
        assertEquals(2.71f, t.run(2.71d));
    }
    public void testN2() {
        T_double_to_float_1 t = new T_double_to_float_1();
        assertEquals(1f, t.run(1d));
    }
    public void testN3() {
        T_double_to_float_1 t = new T_double_to_float_1();
        assertEquals(-1f, t.run(-1d));
    }
    public void testN4() {
        T_double_to_float_3 t = new T_double_to_float_3();
        try {
            t.run(12345l);
        } catch (Throwable e) {
        }
    }
    public void testB1() {
        T_double_to_float_1 t = new T_double_to_float_1();
        assertEquals(Float.POSITIVE_INFINITY, t.run(Double.MAX_VALUE));
    }
    public void testB2() {
        T_double_to_float_1 t = new T_double_to_float_1();
        assertEquals(0f, t.run(Double.MIN_VALUE));
    }
    public void testB3() {
        T_double_to_float_1 t = new T_double_to_float_1();
        assertEquals(-0f, t.run(-0d));
    }
    public void testB4() {
        T_double_to_float_1 t = new T_double_to_float_1();
        assertTrue(Float.isNaN(t.run(Double.NaN)));
    }
    public void testB5() {
        T_double_to_float_1 t = new T_double_to_float_1();
        assertTrue(Float.isInfinite(t.run(Double.POSITIVE_INFINITY)));
    }
    public void testB6() {
        T_double_to_float_1 t = new T_double_to_float_1();
        assertTrue(Float.isInfinite(t.run(Double.NEGATIVE_INFINITY)));
    }
    public void testB7() {
        T_double_to_float_1 t = new T_double_to_float_1();
        assertEquals(-0f, t.run(-4.9E-324d));
    }
    public void testVFE2() {
        try {
            Class.forName("dot.junit.opcodes.double_to_float.d.T_double_to_float_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dot.junit.opcodes.double_to_float.d.T_double_to_float_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dot.junit.opcodes.double_to_float.d.T_double_to_float_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE5() {
        try {
            Class.forName("dot.junit.opcodes.double_to_float.d.T_double_to_float_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
