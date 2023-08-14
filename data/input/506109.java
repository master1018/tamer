public class Test_not_long extends DxTestCase {
    public void testN1() {
        T_not_long_1 t = new T_not_long_1();
        assertEquals(-500001l, t.run(500000l));
    }
    public void testN2() {
        T_not_long_1 t = new T_not_long_1();
        assertEquals(499999l, t.run(-500000l));
    }
    public void testN3() {
        T_not_long_1 t = new T_not_long_1();
        assertEquals(-0xcaff, t.run(0xcafe));
        assertEquals(-0x12d, t.run(0x12c));
    }
    public void testN4() {
        T_not_long_2 t = new T_not_long_2();
        try {
            t.run(1.79d);
        } catch (Throwable e) {
        }
    }
    public void testB1() {
        T_not_long_1 t = new T_not_long_1();
        assertEquals(Long.MIN_VALUE, t.run(Long.MAX_VALUE));
    }
    public void testB2() {
        T_not_long_1 t = new T_not_long_1();
        assertEquals(Long.MAX_VALUE, t.run(Long.MIN_VALUE));
    }
    public void testB3() {
        T_not_long_1 t = new T_not_long_1();
        assertEquals(-2l, t.run(1l));
    }
    public void testB4() {
        T_not_long_1 t = new T_not_long_1();
        assertEquals(-1l, t.run(0l));
    }
    public void testB5() {
        T_not_long_1 t = new T_not_long_1();
        assertEquals(0l, t.run(-1l));
    }
    public void testVFE1() {
        try {
            Class.forName("dot.junit.opcodes.not_long.d.T_not_long_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dot.junit.opcodes.not_long.d.T_not_long_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dot.junit.opcodes.not_long.d.T_not_long_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dot.junit.opcodes.not_long.d.T_not_long_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
