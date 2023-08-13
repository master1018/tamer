public class Test_cmpg_double extends DxTestCase {
    public void testN1() {
        T_cmpg_double_1 t = new T_cmpg_double_1();
        assertEquals(1, t.run(3.14d, 2.7d));
    }
    public void testN2() {
        T_cmpg_double_1 t = new T_cmpg_double_1();
        assertEquals(-1, t.run(-3.14d, 2.7d));
    }
    public void testN3() {
        T_cmpg_double_1 t = new T_cmpg_double_1();
        assertEquals(0, t.run(3.14d, 3.14d));
    }
    public void testN4() {
        T_cmpg_double_3 t = new T_cmpg_double_3();
        try {
            t.run(123l, 3.145d);
        } catch (Throwable e) {
        }
    }  
    public void testB1() {
        T_cmpg_double_1 t = new T_cmpg_double_1();
        assertEquals(1, t.run(Double.NaN, Double.MAX_VALUE));
    }
    public void testB2() {
        T_cmpg_double_1 t = new T_cmpg_double_1();
        assertEquals(0, t.run(+0f, -0f));
    }
    public void testB3() {
        T_cmpg_double_1 t = new T_cmpg_double_1();
        assertEquals(-1, t.run(Double.NEGATIVE_INFINITY, Double.MIN_VALUE));
    }
    public void testB4() {
        T_cmpg_double_1 t = new T_cmpg_double_1();
        assertEquals(1, t.run(Double.POSITIVE_INFINITY, Double.MAX_VALUE));
    }
    public void testB5() {
        T_cmpg_double_1 t = new T_cmpg_double_1();
        assertEquals(1, t.run(Double.POSITIVE_INFINITY,
                Double.NEGATIVE_INFINITY));
    }
    public void testVFE1() {
        try {
            Class.forName("dot.junit.opcodes.cmpg_double.d.T_cmpg_double_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dot.junit.opcodes.cmpg_double.d.T_cmpg_double_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dot.junit.opcodes.cmpg_double.d.T_cmpg_double_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dot.junit.opcodes.cmpg_double.d.T_cmpg_double_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
