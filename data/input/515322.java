public class Test_mul_int_2addr extends DxTestCase {
    public void testN1() {
        T_mul_int_2addr_1 t = new T_mul_int_2addr_1();
        assertEquals(32, t.run(8, 4));
    }
    public void testN2() {
        T_mul_int_2addr_1 t = new T_mul_int_2addr_1();
        assertEquals(-510, t.run(-2, 255));
    }
    public void testN3() {
        T_mul_int_2addr_1 t = new T_mul_int_2addr_1();
        assertEquals(-4, t.run(0x7ffffffe, 2));
    }
    public void testN4() {
        T_mul_int_2addr_1 t = new T_mul_int_2addr_1();
        assertEquals(4, t.run(4, 0x80000001));
    }
    public void testN5() {
        T_mul_int_2addr_6 t = new T_mul_int_2addr_6();
        try {
            t.run(1, 3.14f);
        } catch (Throwable e) {
        }
    }
    public void testB1() {
        T_mul_int_2addr_1 t = new T_mul_int_2addr_1();
        assertEquals(0, t.run(0, Integer.MAX_VALUE));
    }
    public void testB2() {
        T_mul_int_2addr_1 t = new T_mul_int_2addr_1();
        assertEquals(Integer.MAX_VALUE, t.run(Integer.MAX_VALUE, 1));
    }
    public void testB3() {
        T_mul_int_2addr_1 t = new T_mul_int_2addr_1();
        assertEquals(Integer.MIN_VALUE, t.run(Integer.MIN_VALUE, 1));
    }
    public void testB4() {
        T_mul_int_2addr_1 t = new T_mul_int_2addr_1();
        assertEquals(Integer.MIN_VALUE, t.run(Integer.MAX_VALUE,
                Integer.MIN_VALUE));
    }
    public void testB5() {
        T_mul_int_2addr_1 t = new T_mul_int_2addr_1();
        assertEquals(0, t.run(0, 0));
    }
    public void testVFE1() {
        try {
            Class.forName("dot.junit.opcodes.mul_int_2addr.d.T_mul_int_2addr_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dot.junit.opcodes.mul_int_2addr.d.T_mul_int_2addr_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dot.junit.opcodes.mul_int_2addr.d.T_mul_int_2addr_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dot.junit.opcodes.mul_int_2addr.d.T_mul_int_2addr_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
