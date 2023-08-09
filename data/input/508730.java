public class Test_if_icmple extends DxTestCase {
    public void testN1() {
        T_if_icmple_1 t = new T_if_icmple_1();
        assertEquals(1, t.run(5, 6));
    }
    public void testN2() {
        T_if_icmple_1 t = new T_if_icmple_1();
        assertEquals(1, t.run(0x0f0e0d0c, 0x0f0e0d0c));
    }
    public void testN3() {
        T_if_icmple_1 t = new T_if_icmple_1();
        assertEquals(1234, t.run(5, -5));
    }
    public void testN4() {
        T_if_icmple_1 t = new T_if_icmple_1();
        assertEquals(1234, t.run(0x0f0e0d0d, 0x0f0e0d0c));
    }
    public void testN5() {
        T_if_icmple_1 t = new T_if_icmple_1();
        assertEquals(1, t.run(0x1234, 0x01001234));
    }
    public void testN6() {
        T_if_icmple_1 t = new T_if_icmple_1();
        assertEquals(1, t.run(-5, 5));
    }
    public void testB1() {
        T_if_icmple_1 t = new T_if_icmple_1();
        assertEquals(1, t.run(Integer.MAX_VALUE, Integer.MAX_VALUE));
    }
    public void testB2() {
        T_if_icmple_1 t = new T_if_icmple_1();
        assertEquals(1, t.run(Integer.MIN_VALUE, Integer.MIN_VALUE));
    }
    public void testB3() {
        T_if_icmple_1 t = new T_if_icmple_1();
        assertEquals(1234, t.run(1234567, 0));
    }
    public void testB4() {
        T_if_icmple_1 t = new T_if_icmple_1();
        assertEquals(1, t.run(0, 1234567));
    }
    public void testB5() {
        T_if_icmple_1 t = new T_if_icmple_1();
        assertEquals(1, t.run(0, 0));
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.if_icmple.jm.T_if_icmple_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.if_icmple.jm.T_if_icmple_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.if_icmple.jm.T_if_icmple_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dxc.junit.opcodes.if_icmple.jm.T_if_icmple_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE5() {
        try {
            Class.forName("dxc.junit.opcodes.if_icmple.jm.T_if_icmple_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE6() {
        try {
            Class.forName("dxc.junit.opcodes.if_icmple.jm.T_if_icmple_7");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
