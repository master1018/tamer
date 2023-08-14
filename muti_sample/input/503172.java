public class Test_rem_double extends DxTestCase {
    public void testN1() {
        T_rem_double_1 t = new T_rem_double_1();
        assertEquals(2.7d, t.run(2.7d, 3.14d));
    }
    public void testN2() {
        T_rem_double_1 t = new T_rem_double_1();
        assertEquals(0d, t.run(0, 3.14d));
    }
    public void testN3() {
        T_rem_double_1 t = new T_rem_double_1();
        assertEquals(-0.43999999999999995d, t.run(-3.14d, 2.7d));
    }
    public void testN4() {
        T_rem_double_4 t = new T_rem_double_4();
        try {
            t.run(500000l, 1.05d);
        } catch (Throwable e) {
        }
    }
    public void testB1() {
        T_rem_double_1 t = new T_rem_double_1();
        assertEquals(Double.NaN, t.run(Double.MAX_VALUE, Double.NaN));
    }
    public void testB2() {
        T_rem_double_1 t = new T_rem_double_1();
        assertEquals(Double.NaN, t.run(Double.POSITIVE_INFINITY,
                Double.NEGATIVE_INFINITY));
    }
    public void testB3() {
        T_rem_double_1 t = new T_rem_double_1();
        assertEquals(Double.NaN, t.run(Double.POSITIVE_INFINITY, -2.7d));
    }
    public void testB4() {
        T_rem_double_1 t = new T_rem_double_1();
        assertEquals(-2.7d, t.run(-2.7d, Double.NEGATIVE_INFINITY));
    }
    public void testB5() {
        T_rem_double_1 t = new T_rem_double_1();
        assertEquals(Double.NaN, t.run(0, 0));
    }
    public void testB6() {
        T_rem_double_1 t = new T_rem_double_1();
        assertEquals(0d, t.run(0, -2.7d));
    }
    public void testB7() {
        T_rem_double_1 t = new T_rem_double_1();
        assertEquals(Double.NaN, t.run(-2.7d, 0));
    }
    public void testB8() {
        T_rem_double_1 t = new T_rem_double_1();
        assertEquals(0d, t.run(1, Double.MIN_VALUE));
    }
    public void testB9() {
        T_rem_double_1 t = new T_rem_double_1();
        assertEquals(1.543905285031139E-10d, t.run(Double.MAX_VALUE, -1E-9d));
    }
    public void testVFE1() {
        try {
            Class.forName("dot.junit.opcodes.rem_double.d.T_rem_double_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dot.junit.opcodes.rem_double.d.T_rem_double_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dot.junit.opcodes.rem_double.d.T_rem_double_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dot.junit.opcodes.rem_double.d.T_rem_double_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
