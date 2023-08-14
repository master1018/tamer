public class Test_or_long extends DxTestCase {
    public void testN1() {
        T_or_long_1 t = new T_or_long_1();
        assertEquals(123456789123l, t.run(123456789121l, 2l));
    }
    public void testN2() {
        T_or_long_1 t = new T_or_long_1();
        assertEquals(0xffffffffffffff9l, t.run(0xffffffffffffff8l,
                0xffffffffffffff1l));
    }
    public void testN3() {
        T_or_long_1 t = new T_or_long_1();
        assertEquals(-1l, t.run(0xabcdefabcdefl, -1l));
    }
    public void testN4() {
        T_or_long_3 t = new T_or_long_3();
        try {
            t.run(500000l, 1.05d);
        } catch (Throwable e) {
        }
    }
    public void testB1() {
        T_or_long_1 t = new T_or_long_1();
        assertEquals(-1l, t.run(0l, -1l));
    }
    public void testB2() {
        T_or_long_1 t = new T_or_long_1();
        assertEquals(-1l, t.run(Long.MAX_VALUE, Long.MIN_VALUE));
    }
    public void testVFE1() {
        try {
            Class.forName("dot.junit.opcodes.or_long.d.T_or_long_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dot.junit.opcodes.or_long.d.T_or_long_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dot.junit.opcodes.or_long.d.T_or_long_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dot.junit.opcodes.or_long.d.T_or_long_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
