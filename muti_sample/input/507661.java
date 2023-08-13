public class Test_ushr_int_lit8 extends DxTestCase {
    public void testN1() {
        T_ushr_int_lit8_1 t = new T_ushr_int_lit8_1();
        assertEquals(7, t.run());
    }
    public void testN2() {
        T_ushr_int_lit8_2 t = new T_ushr_int_lit8_2();
        assertEquals(8, t.run());
    }
    public void testN3() {
        T_ushr_int_lit8_3 t = new T_ushr_int_lit8_3();
        assertEquals(0x7FFFFFF8, t.run());
    }
    public void testN4() {
        T_ushr_int_lit8_4 t = new T_ushr_int_lit8_4();
        assertEquals(0, t.run());
    }
    public void testN5() {
        T_ushr_int_lit8_5 t = new T_ushr_int_lit8_5();
        assertEquals(16, t.run());
    }
    public void testN6() {
        T_ushr_int_lit8_13 t = new  T_ushr_int_lit8_13();
        try {
            t.run(3.14f);
        } catch (Throwable e) {
        }
    }
    public void testB1() {
        T_ushr_int_lit8_6 t = new T_ushr_int_lit8_6();
        assertEquals(0, t.run());
    }
    public void testB2() {
        T_ushr_int_lit8_7 t = new T_ushr_int_lit8_7();
        assertEquals(0x3FFFFFFF, t.run());
    }
    public void testB3() {
        T_ushr_int_lit8_8 t = new T_ushr_int_lit8_8();
        assertEquals(0x40000000, t.run());
    }
    public void testB4() {
        T_ushr_int_lit8_9 t = new T_ushr_int_lit8_9();
        assertEquals(1, t.run());
    }
    public void testVFE1() {
        try {
            Class.forName("dot.junit.opcodes.ushr_int_lit8.d.T_ushr_int_lit8_10");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dot.junit.opcodes.ushr_int_lit8.d.T_ushr_int_lit8_11");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dot.junit.opcodes.ushr_int_lit8.d.T_ushr_int_lit8_12");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dot.junit.opcodes.ushr_int_lit8.d.T_ushr_int_lit8_14");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
