public class Test_a5 extends DxTestCase {
    public void testVFE1() {
        try {
            Class.forName("dot.junit.verify.a5.d.T_a5_1");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
