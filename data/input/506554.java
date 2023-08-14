public class Test_if_ge extends DxTestCase {
    public void testN1() {
        T_if_ge_1 t = new T_if_ge_1();
        assertEquals(1234, t.run(5, 6));
    }
    public void testN2() {
        T_if_ge_1 t = new T_if_ge_1();
        assertEquals(1, t.run(0x0f0e0d0c, 0x0f0e0d0c));
    }
    public void testN3() {
        T_if_ge_1 t = new T_if_ge_1();
        assertEquals(1, t.run(5, -5));
    }
    public void testN4() {
        T_if_ge_3 t = new T_if_ge_3();
        assertEquals(1, t.run(1f, 1));
    }
    public void testB1() {
        T_if_ge_1 t = new T_if_ge_1();
        assertEquals(1, t.run(Integer.MAX_VALUE, Integer.MAX_VALUE));
    }
    public void testB2() {
        T_if_ge_1 t = new T_if_ge_1();
        assertEquals(1234, t.run(Integer.MIN_VALUE, Integer.MAX_VALUE));
    }
    public void testB3() {
        T_if_ge_1 t = new T_if_ge_1();
        assertEquals(1, t.run(Integer.MAX_VALUE, Integer.MIN_VALUE));
    }
    public void testB4() {
        T_if_ge_1 t = new T_if_ge_1();
        assertEquals(1, t.run(0, Integer.MIN_VALUE));
    }
    public void testB5() {
        T_if_ge_1 t = new T_if_ge_1();
        assertEquals(1, t.run(0, 0));
    }
    public void testVFE1() {
        try {
            Class.forName("dot.junit.opcodes.if_ge.d.T_if_ge_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dot.junit.opcodes.if_ge.d.T_if_ge_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dot.junit.opcodes.if_ge.d.T_if_ge_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dot.junit.opcodes.if_ge.d.T_if_ge_7");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE6() {
        try {
            Class.forName("dot.junit.opcodes.if_ge.d.T_if_ge_9");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE7() {
        try {
            Class.forName("dot.junit.opcodes.if_ge.d.T_if_ge_10");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE8() {
        try {
            Class.forName("dot.junit.opcodes.if_ge.d.T_if_ge_11");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
