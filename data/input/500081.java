public class Test_float_to_double extends DxTestCase {
    public void testN1() {
        T_float_to_double_1 t = new T_float_to_double_1();
        assertEquals(0.5d, t.run(0.5f), 0d);
    }
    public void testN2() {
        T_float_to_double_1 t = new T_float_to_double_1();
        assertEquals(1d, t.run(1), 0d);
    }
    public void testN3() {
        T_float_to_double_1 t = new T_float_to_double_1();
        assertEquals(-1d, t.run(-1), 0d);
    }
    public void testN4() {
        T_float_to_double_7 t = new T_float_to_double_7();
        try {
            t.run(1);
        } catch (Throwable e) {
        }
    }  
    public void testB1() {
        T_float_to_double_1 t = new T_float_to_double_1();
        double r = 0x1.fffffeP+127d;
        assertEquals(r, t.run(Float.MAX_VALUE), 0d);
    }
    public void testB2() {
        T_float_to_double_1 t = new T_float_to_double_1();
        double r = 0x0.000002P-126d;
        assertEquals(r, t.run(Float.MIN_VALUE), 0d);
    }
    public void testB3() {
        T_float_to_double_1 t = new T_float_to_double_1();
        assertEquals(-0d, t.run(-0), 0d);
    }
    public void testB4() {
        T_float_to_double_1 t = new T_float_to_double_1();
        assertTrue(Double.isNaN(t.run(Float.NaN)));
    }
    public void testB5() {
        T_float_to_double_1 t = new T_float_to_double_1();
        assertTrue(Double.isInfinite(t.run(Float.POSITIVE_INFINITY)));
    }
    public void testB6() {
        T_float_to_double_1 t = new T_float_to_double_1();
        assertTrue(Double.isInfinite(t.run(Float.NEGATIVE_INFINITY)));
    }
    public void testVFE1() {
        try {
            Class.forName("dot.junit.opcodes.float_to_double.d.T_float_to_double_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dot.junit.opcodes.float_to_double.d.T_float_to_double_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dot.junit.opcodes.float_to_double.d.T_float_to_double_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dot.junit.opcodes.float_to_double.d.T_float_to_double_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE5() {
        try {
            Class.forName("dot.junit.opcodes.float_to_double.d.T_float_to_double_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
