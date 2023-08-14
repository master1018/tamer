public class Test_lconst_1 extends DxTestCase {
    public void testN1() {
        T_lconst_1_1 t = new T_lconst_1_1();
        long a = 20l;
        long b = 19l;
        assertEquals(a - b, t.run());
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.lconst_1.jm.T_lconst_1_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
