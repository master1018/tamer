public class Test_lxor extends DxTestCase {
    public void testN1() {
        T_lxor_1 t = new T_lxor_1();
        assertEquals(3, t.run(23423432423777l, 23423432423778l));
    }
    public void testN2() {
        T_lxor_1 t = new T_lxor_1();
        assertEquals(4, t.run(0xfffffff5, 0xfffffff1));
    }
    public void testN3() {
        T_lxor_1 t = new T_lxor_1();
        assertEquals(0x54321054, t.run(0xABCDEFAB, -1l));
    }
    public void testB1() {
        T_lxor_1 t = new T_lxor_1();
        assertEquals(-1l, t.run(0l, -1l));
    }
    public void testB2() {
        T_lxor_1 t = new T_lxor_1();
        assertEquals(0xffffffff, t.run(Long.MAX_VALUE, Long.MIN_VALUE));
    }
    public void testB3() {
        T_lxor_1 t = new T_lxor_1();
        assertEquals(0l, t.run(Long.MAX_VALUE, Long.MAX_VALUE));
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.lxor.jm.T_lxor_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.lxor.jm.T_lxor_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.lxor.jm.T_lxor_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dxc.junit.opcodes.lxor.jm.T_lxor_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE5() {
        try {
            Class.forName("dxc.junit.opcodes.lxor.jm.T_lxor_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
