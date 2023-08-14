public class Test_fconst_0 extends DxTestCase {
    public void testN1() {
        T_fconst_0_1 t = new T_fconst_0_1();
        float b = 1234f;
        float c = 1234f;
        float d = b - c;
        assertEquals(d, t.run());
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.fconst_0.jm.T_fconst_0_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
