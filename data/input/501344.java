public class Test_neg_long extends DxTestCase {
    public void testN1() {
        T_neg_long_1 t = new T_neg_long_1();
        assertEquals(-123123123272432432l, t.run(123123123272432432l));
    }
    public void testN2() {
        T_neg_long_1 t = new T_neg_long_1();
        assertEquals(-1l, t.run(1l));
    }
    public void testN3() {
        T_neg_long_1 t = new T_neg_long_1();
        assertEquals(1l, t.run(-1l));
    }
    public void testN4() {
        T_neg_long_2 t = new T_neg_long_2();
        assertTrue(t.run(15l));
    }
    public void testN5() {
        T_neg_long_4 t = new T_neg_long_4();
        try {
            t.run(1.23d);
        } catch (Throwable e) {
        }
    }
    public void testB1() {
        T_neg_long_1 t = new T_neg_long_1();
        assertEquals(0, t.run(0));
    }
    public void testB2() {
        T_neg_long_1 t = new T_neg_long_1();
        assertEquals(-9223372036854775807L, t.run(Long.MAX_VALUE));
    }
    public void testB3() {
        T_neg_long_1 t = new T_neg_long_1();
        assertEquals(-9223372036854775808L, t.run(Long.MIN_VALUE));
    }
    public void testVFE1() {
        try {
            Class.forName("dot.junit.opcodes.neg_long.d.T_neg_long_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dot.junit.opcodes.neg_long.d.T_neg_long_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dot.junit.opcodes.neg_long.d.T_neg_long_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dot.junit.opcodes.neg_long.d.T_neg_long_7");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
