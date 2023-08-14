public class Test_fconst_1 extends DxTestCase {
    public void testN1() {
        T_fconst_1_1 t = new T_fconst_1_1();
        float b = 1235f;
        float c = 1234f;
        float d = b - c;
        assertEquals(d, t.run());
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.fconst_1.jm.T_fconst_1_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
