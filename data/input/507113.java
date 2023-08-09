public class Test_lor extends DxTestCase {
    public void testN1() {
        T_lor_1 t = new T_lor_1();
        assertEquals(123456789123l, t.run(123456789121l, 2l));
    }
    public void testN2() {
        T_lor_1 t = new T_lor_1();
        assertEquals(0xffffffffffffff9l, t.run(0xffffffffffffff8l,
                0xffffffffffffff1l));
    }
    public void testN3() {
        T_lor_1 t = new T_lor_1();
        assertEquals(-1l, t.run(0xabcdefabcdefl, -1l));
    }
    public void testB1() {
        T_lor_1 t = new T_lor_1();
        assertEquals(-1l, t.run(0l, -1l));
    }
    public void testB2() {
        T_lor_1 t = new T_lor_1();
        assertEquals(-1l, t.run(Long.MAX_VALUE, Long.MIN_VALUE));
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.lor.jm.T_lor_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.lor.jm.T_lor_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.lor.jm.T_lor_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dxc.junit.opcodes.lor.jm.T_lor_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE5() {
        try {
            Class.forName("dxc.junit.opcodes.lor.jm.T_lor_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
