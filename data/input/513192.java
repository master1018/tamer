public class Test_dconst_0 extends DxTestCase {
    public void testN1() {
        T_dconst_0_1 t = new T_dconst_0_1();
        double b = 1234d;
        double c = 1234d;
        double d = b - c;
        assertEquals(d, t.run());
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.dconst_0.jm.T_dconst_0_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
