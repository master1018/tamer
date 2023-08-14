public class Test_sub_double_2addr extends DxTestCase {
    public void testN1() {
        T_sub_double_2addr_1 t = new T_sub_double_2addr_1();
        assertEquals(-0.43999999999999995d, t.run(2.7d, 3.14d));
    }
    public void testN2() {
        T_sub_double_2addr_1 t = new T_sub_double_2addr_1();
        assertEquals(3.14d, t.run(0, -3.14d));
    }
    public void testN3() {
        T_sub_double_2addr_1 t = new T_sub_double_2addr_1();
        assertEquals(-0.43999999999999995d, t.run(-3.14d, -2.7d));
    }
    public void testN4() {
        T_sub_double_2addr_3 t = new T_sub_double_2addr_3();
        try {
            t.run(12345l, 3.14d);
        } catch (Throwable e) {
        }
    }  
    public void testB1() {
        T_sub_double_2addr_1 t = new T_sub_double_2addr_1();
        assertEquals(Double.NaN, t.run(Double.MAX_VALUE, Double.NaN));
    }
    public void testB2() {
        T_sub_double_2addr_1 t = new T_sub_double_2addr_1();
        assertEquals(Double.POSITIVE_INFINITY, t.run(Double.POSITIVE_INFINITY,
                Double.NEGATIVE_INFINITY));
    }
    public void testB3() {
        T_sub_double_2addr_1 t = new T_sub_double_2addr_1();
        assertEquals(Double.NaN, t.run(Double.POSITIVE_INFINITY,
                Double.POSITIVE_INFINITY));
    }
    public void testB4() {
        T_sub_double_2addr_1 t = new T_sub_double_2addr_1();
        assertEquals(Double.POSITIVE_INFINITY, t.run(Double.POSITIVE_INFINITY,
                -2.7d));
    }
    public void testB5() {
        T_sub_double_2addr_1 t = new T_sub_double_2addr_1();
        assertEquals(+0d, t.run(+0d, -0d));
    }
    public void testB6() {
        T_sub_double_2addr_1 t = new T_sub_double_2addr_1();
        assertEquals(0d, t.run(-0d, -0d));
    }
    public void testB7() {
        T_sub_double_2addr_1 t = new T_sub_double_2addr_1();
        assertEquals(+0d, t.run(+0d, +0d));
    }
    public void testB8() {
        T_sub_double_2addr_1 t = new T_sub_double_2addr_1();
        assertEquals(0d, t.run(2.7d, 2.7d));
    }
    public void testB9() {
        T_sub_double_2addr_1 t = new T_sub_double_2addr_1();
        assertEquals(0d, t.run(Double.MAX_VALUE, Double.MAX_VALUE));
    }
    public void testB10() {
        T_sub_double_2addr_1 t = new T_sub_double_2addr_1();
        assertEquals(0d, t.run(Double.MIN_VALUE, 4.9E-324));
    }
    public void testVFE1() {
        try {
            Class.forName("dot.junit.opcodes.sub_double_2addr.d.T_sub_double_2addr_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dot.junit.opcodes.sub_double_2addr.d.T_sub_double_2addr_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dot.junit.opcodes.sub_double_2addr.d.T_sub_double_2addr_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dot.junit.opcodes.sub_double_2addr.d.T_sub_double_2addr_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
