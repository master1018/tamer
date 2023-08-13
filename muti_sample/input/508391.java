public class Test_shl_int_lit8 extends DxTestCase {
    public void testN1() {
        T_shl_int_lit8_1 t = new T_shl_int_lit8_1();
        assertEquals(30, t.run(15));
    }
    public void testN2() {
        T_shl_int_lit8_2 t = new T_shl_int_lit8_2();
        assertEquals(132, t.run(33));
    }
    public void testN3() {
        T_shl_int_lit8_1 t = new T_shl_int_lit8_1();
        assertEquals(-30, t.run(-15));
    }
    public void testN4() {
        T_shl_int_lit8_3 t = new T_shl_int_lit8_3();
        assertEquals(0x80000000, t.run(1));
    }
    public void testN5() {
        T_shl_int_lit8_4 t = new T_shl_int_lit8_4();
        assertEquals(66, t.run(33));
    }
    public void testN6() {
        T_shl_int_lit8_6 t = new T_shl_int_lit8_6();
        try {
            t.run(3.14f);
        } catch (Throwable e) {
        }
    }
    public void testB1() {
        T_shl_int_lit8_3 t = new T_shl_int_lit8_3();
        assertEquals(0, t.run(0));
    }
    public void testB2() {
        T_shl_int_lit8_1 t = new T_shl_int_lit8_1();
        assertEquals(0xfffffffe, t.run(Integer.MAX_VALUE));
    }
    public void testB3() {
        T_shl_int_lit8_1 t = new T_shl_int_lit8_1();
        assertEquals(0, t.run(Integer.MIN_VALUE));
    }
    public void testB4() {
        T_shl_int_lit8_5 t = new T_shl_int_lit8_5();
        assertEquals(1, t.run(1));
    }
    public void testVFE1() {
        try {
            Class.forName("dot.junit.opcodes.shl_int_lit8.d.T_shl_int_lit8_7");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dot.junit.opcodes.shl_int_lit8.d.T_shl_int_lit8_8");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dot.junit.opcodes.shl_int_lit8.d.T_shl_int_lit8_9");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dot.junit.opcodes.shl_int_lit8.d.T_shl_int_lit8_10");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
