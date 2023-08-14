public class Test_iconst_3 extends DxTestCase {
    public void testN1() {
        T_iconst_3_1 t = new T_iconst_3_1();
        int b = 1237;
        int c = 1234;
        int d = b - c;
        assertEquals(d, t.run());
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.iconst_3.jm.T_iconst_3_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
