public class Test_int_to_short extends DxTestCase {
    public void testN1() {
        T_int_to_short_1 t = new T_int_to_short_1();
        assertEquals(1, t.run(1));
    }
    public void testN2() {
        T_int_to_short_1 t = new T_int_to_short_1();
        assertEquals(-1, t.run(-1));
    }
    public void testN3() {
        T_int_to_short_1 t = new T_int_to_short_1();
        assertEquals(32767, t.run(32767));
    }
    public void testN4() {
        T_int_to_short_1 t = new T_int_to_short_1();
        assertEquals(-32768, t.run(-32768));
    }
    public void testN5() {
        T_int_to_short_1 t = new T_int_to_short_1();
        assertEquals(32767, t.run(-32769));
    }
    public void testN6() {
        T_int_to_short_1 t = new T_int_to_short_1();
        assertEquals(-32768, t.run(32768));
    }
    public void testN7() {
        T_int_to_short_1 t = new T_int_to_short_1();
        assertEquals(0xfffffedc, t.run(0x10fedc));
    }
    public void testN8() {
        T_int_to_short_5 t = new T_int_to_short_5();
        try {
            t.run(1.333f);
        } catch (Throwable e) {
        }
    } 
    public void testB1() {
        T_int_to_short_1 t = new T_int_to_short_1();
        assertEquals(0, t.run(0));
    }
    public void testB2() {
        T_int_to_short_1 t = new T_int_to_short_1();
        assertEquals(-1, t.run(Integer.MAX_VALUE));
    }
    public void testB3() {
        T_int_to_short_1 t = new T_int_to_short_1();
        assertEquals(0, t.run(Integer.MIN_VALUE));
    }
    public void testVFE1() {
        try {
            Class.forName("dot.junit.opcodes.int_to_short.d.T_int_to_short_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dot.junit.opcodes.int_to_short.d.T_int_to_short_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dot.junit.opcodes.int_to_short.d.T_int_to_short_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dot.junit.opcodes.int_to_short.d.T_int_to_short_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
