public class Test_aconst_null extends DxTestCase {
    public void testN1() {
        assertEquals(null, new T_aconst_null_1().run());
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.aconst_null.jm.T_aconst_null_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
