public class Test_add_int_lit8 extends DxTestCase {
    public void testN1() {
        T_add_int_lit8_1 t = new T_add_int_lit8_1();
        assertEquals(12, t.run());
    }
    public void testN2() {
        T_add_int_lit8_2 t = new T_add_int_lit8_2();
        assertEquals(-1, t.run());
    }
    public void testN3() {
        T_add_int_lit8_3 t = new T_add_int_lit8_3();
        assertEquals(-128, t.run());
    }
    public void testN4() {
        T_add_int_lit8_4 t = new T_add_int_lit8_4();
        assertEquals(-2147483647, t.run());
    }
    public void testN5() {
        T_add_int_lit8_5 t = new T_add_int_lit8_5();
        assertEquals(-2147483648, t.run());
    }
    public void testN6() {
        T_add_int_lit8_6 t = new T_add_int_lit8_6();
        assertEquals(0, t.run());
    }
    public void testN7() {
        T_add_int_lit8_16 t = new T_add_int_lit8_16();
        try {
            t.run(3f);
        } catch (Throwable e) {
        }
    }
    public void testB1() {
        T_add_int_lit8_7 t = new T_add_int_lit8_7();
        assertEquals(Byte.MAX_VALUE, t.run());
    }
    public void testB2() {
        T_add_int_lit8_8 t = new T_add_int_lit8_8();
        assertEquals(-2147483522, t.run());
    }
    public void testB3() {
        T_add_int_lit8_9 t = new T_add_int_lit8_9();
        assertEquals(Integer.MIN_VALUE, t.run());
    }
    public void testB4() {
        T_add_int_lit8_10 t = new T_add_int_lit8_10();
        assertEquals(-2147483647, t.run());
    }
    public void testB5() {
        T_add_int_lit8_11 t = new T_add_int_lit8_11();
        assertEquals(0, t.run());
    }
    public void testB6() {
        T_add_int_lit8_12 t = new T_add_int_lit8_12();
        assertEquals(-32896, t.run());
    }
    public void testVFE1() {
        try {
            Class.forName("dot.junit.opcodes.add_int_lit8.d.T_add_int_lit8_13");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dot.junit.opcodes.add_int_lit8.d.T_add_int_lit8_14");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dot.junit.opcodes.add_int_lit8.d.T_add_int_lit8_15");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dot.junit.opcodes.add_int_lit8.d.T_add_int_lit8_17");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
