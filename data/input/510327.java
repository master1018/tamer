public class Test_mul_long extends DxTestCase {
    public void testN1() {
        T_mul_long_1 t = new T_mul_long_1();
        assertEquals(3195355577426903040l, t.run(222000000000l, 5000000000l));
    }
    public void testN2() {
        T_mul_long_1 t = new T_mul_long_1();
        assertEquals(-15241578750190521l, t.run(-123456789l, 123456789l));
    }
    public void testN3() {
        T_mul_long_1 t = new T_mul_long_1();
        assertEquals(15241578750190521l, t.run(-123456789l, -123456789l));
    }
    public void testN4() {
        T_mul_long_3 t = new T_mul_long_3();
        try {
            t.run(500000l, 2.7d);
        } catch (Throwable e) {
        }
    }
    public void testB1() {
        T_mul_long_1 t = new T_mul_long_1();
        assertEquals(0, t.run(0, Long.MAX_VALUE));
    }
    public void testB2() {
        T_mul_long_1 t = new T_mul_long_1();
        assertEquals(9223372036854775807L, t.run(Long.MAX_VALUE, 1));
    }
    public void testB3() {
        T_mul_long_1 t = new T_mul_long_1();
        assertEquals(-9223372036854775808L, t.run(Long.MIN_VALUE, 1));
    }
    public void testB4() {
        T_mul_long_1 t = new T_mul_long_1();
        assertEquals(-9223372036854775808L, t.run(Long.MAX_VALUE,
                Long.MIN_VALUE));
    }
    public void testB5() {
        T_mul_long_1 t = new T_mul_long_1();
        assertEquals(0, t.run(0, 0));
    }
    public void testB6() {
        T_mul_long_1 t = new T_mul_long_1();
        assertEquals(-9223372036854775807L, t.run(Long.MAX_VALUE, -1));
    }
    public void testB7() {
        T_mul_long_1 t = new T_mul_long_1();
        assertEquals(-9223372036854775808L, t.run(Long.MIN_VALUE, -1));
    }
    public void testVFE1() {
        try {
            Class.forName("dot.junit.opcodes.mul_long.d.T_mul_long_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dot.junit.opcodes.mul_long.d.T_mul_long_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dot.junit.opcodes.mul_long.d.T_mul_long_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dot.junit.opcodes.mul_long.d.T_mul_long_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
