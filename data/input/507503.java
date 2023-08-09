public class Test_neg_float extends DxTestCase {
    public void testN1() {
        T_neg_float_1 t = new T_neg_float_1();
        assertEquals(-1f, t.run(1f));
    }
    public void testN2() {
        T_neg_float_1 t = new T_neg_float_1();
        assertEquals(1f, t.run(-1f));
    }
    public void testN3() {
        T_neg_float_1 t = new T_neg_float_1();
        assertEquals(-0f, t.run(+0f));
    }
    public void testN4() {
        T_neg_float_1 t = new T_neg_float_1();
        assertEquals(2.7f, t.run(-2.7f));
    }
    public void testN5() {
        T_neg_float_6 t = new T_neg_float_6();
        try {
            t.run(5);
        } catch (Throwable e) {
        }
    }
    public void testB1() {
        T_neg_float_1 t = new T_neg_float_1();
        assertEquals(Float.NaN, t.run(Float.NaN));
    }
    public void testB2() {
        T_neg_float_1 t = new T_neg_float_1();
        assertEquals(Float.POSITIVE_INFINITY, t.run(Float.NEGATIVE_INFINITY));
    }
    public void testB3() {
        T_neg_float_1 t = new T_neg_float_1();
        assertEquals(Float.NEGATIVE_INFINITY, t.run(Float.POSITIVE_INFINITY));
    }
    public void testB4() {
        T_neg_float_1 t = new T_neg_float_1();
        assertEquals(-3.4028235E38f, t.run(Float.MAX_VALUE));
    }
    public void testB5() {
        T_neg_float_1 t = new T_neg_float_1();
        assertEquals(-1.4E-45f, t.run(Float.MIN_VALUE));
    }
    public void testVFE1() {
        try {
            Class.forName("dot.junit.opcodes.neg_float.d.T_neg_float_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dot.junit.opcodes.neg_float.d.T_neg_float_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dot.junit.opcodes.neg_float.d.T_neg_float_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dot.junit.opcodes.neg_float.d.T_neg_float_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
