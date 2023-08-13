public class Test_swap extends DxTestCase {
    public void testN1() {
        T_swap_1 t = new T_swap_1();
        assertEquals(8, t.run(15, 8));
    }
    public void testN2() {
        T_swap_6 t = new T_swap_6();
        assertEquals(8f, t.run(15f, 8f));
    }
    public void testN3() {
        T_swap_7 t = new T_swap_7();
        int tmp[] = new int[1];
        assertEquals(tmp, t.run(this, tmp));
    }
    public void testN4() {
        T_swap_8 t = new T_swap_8();
        assertEquals(this, t.run(0xff, this));
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.swap.jm.T_swap_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.swap.jm.T_swap_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.swap.jm.T_swap_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dxc.junit.opcodes.swap.jm.T_swap_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
