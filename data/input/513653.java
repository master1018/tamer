public class Test_fconst_2 extends DxTestCase {
    public void testN1() {
        T_fconst_2_1 t = new T_fconst_2_1();
        float b = 1236f;
        float c = 1234f;
        float d = b - c;
        assertEquals(d, t.run());
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.fconst_2.jm.T_fconst_2_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
