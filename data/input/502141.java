public class Test_pop2 extends DxTestCase {
    public void testN1() {
        T_pop2_1 t = new T_pop2_1();
        assertEquals(1234l, t.run());
    }
    public void testN2() {
        T_pop2_2 t = new T_pop2_2();
        assertEquals(1234d, t.run());
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.pop2.jm.T_pop2_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.pop2.jm.T_pop2_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
