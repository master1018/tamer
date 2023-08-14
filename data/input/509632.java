public class Test_ior extends DxTestCase {
    public void testN1() {
        T_ior_1 t = new T_ior_1();
        assertEquals(15, t.run(15, 8));
    }
    public void testN2() {
        T_ior_1 t = new T_ior_1();
        assertEquals(0xfffffff9, t.run(0xfffffff8, 0xfffffff1));
    }
    public void testN3() {
        T_ior_1 t = new T_ior_1();
        assertEquals(-1, t.run(0xcafe, -1));
    }
    public void testB1() {
        T_ior_1 t = new T_ior_1();
        assertEquals(-1, t.run(0, -1));
    }
    public void testB2() {
        T_ior_1 t = new T_ior_1();
        assertEquals(0xffffffff, t.run(Integer.MAX_VALUE, Integer.MIN_VALUE));
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.ior.jm.T_ior_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.ior.jm.T_ior_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.ior.jm.T_ior_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dxc.junit.opcodes.ior.jm.T_ior_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
