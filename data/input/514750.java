public class Test_dconst_1 extends DxTestCase {
    public void testN1() {
        T_dconst_1_1 t = new T_dconst_1_1();
        double b = 1235d;
        double c = 1234d;
        double d = b - c;
        assertEquals(d, t.run());
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.dconst_1.jm.T_dconst_1_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
