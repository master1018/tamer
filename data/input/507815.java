public class Test_div_float extends DxTestCase {
    public void testN1() {
        T_div_float_1 t = new T_div_float_1();
        assertEquals(0.8598726f, t.run(2.7f, 3.14f));
    }
    public void testN2() {
        T_div_float_1 t = new T_div_float_1();
        assertEquals(0f, t.run(0, 3.14f));
    }
    public void testN3() {
        T_div_float_1 t = new T_div_float_1();
        assertEquals(-1.162963f, t.run(-3.14f, 2.7f));
    }
    public void testN4() {
        T_div_float_5 t = new T_div_float_5();
        try {
            t.run(1, 3.14f);
        } catch (Throwable e) {
        }
    }    
    public void testB1() {
        T_div_float_1 t = new T_div_float_1();
        assertEquals(Float.NaN, t.run(Float.MAX_VALUE, Float.NaN));
    }
    public void testB2() {
        T_div_float_1 t = new T_div_float_1();
        assertEquals(Float.NaN, t.run(Float.POSITIVE_INFINITY,
                Float.NEGATIVE_INFINITY));
    }
    public void testB3() {
        T_div_float_1 t = new T_div_float_1();
        assertEquals(Float.NEGATIVE_INFINITY, t.run(Float.POSITIVE_INFINITY,
                -2.7f));
    }
    public void testB4() {
        T_div_float_1 t = new T_div_float_1();
        assertEquals(0f, t.run(-2.7f, Float.NEGATIVE_INFINITY));
    }
    public void testB5() {
        T_div_float_1 t = new T_div_float_1();
        assertEquals(Float.NaN, t.run(0, 0));
    }
    public void testB6() {
        T_div_float_1 t = new T_div_float_1();
        assertEquals(-0f, t.run(0, -2.7f));
    }
    public void testB7() {
        T_div_float_1 t = new T_div_float_1();
        assertEquals(Float.NEGATIVE_INFINITY, t.run(-2.7f, 0));
    }
    public void testB8() {
        T_div_float_1 t = new T_div_float_1();
        assertEquals(Float.POSITIVE_INFINITY, t.run(1, Float.MIN_VALUE));
    }
    public void testB9() {
        T_div_float_1 t = new T_div_float_1();
        assertEquals(Float.NEGATIVE_INFINITY, t.run(Float.MAX_VALUE, -1E-9f));
    }
    public void testVFE1() {
        try {
            Class.forName("dot.junit.opcodes.div_float.d.T_div_float_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dot.junit.opcodes.div_float.d.T_div_float_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dot.junit.opcodes.div_float.d.T_div_float_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dot.junit.opcodes.div_float.d.T_div_float_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
