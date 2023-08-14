public class Test_iconst_2 extends DxTestCase {
    public void testN1() {
        T_iconst_2_1 t = new T_iconst_2_1();
        int b = 1236;
        int c = 1234;
        int d = b - c;
        assertEquals(d, t.run());
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.iconst_2.jm.T_iconst_2_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
