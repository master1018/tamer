public class Test_lconst_0 extends DxTestCase {
    public void testN1() {
        T_lconst_0_1 t = new T_lconst_0_1();
        long a = 20l;
        long b = 20l;
        assertEquals(a - b, t.run());
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.lconst_0.jm.T_lconst_0_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
