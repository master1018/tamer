public class Test_a1 extends DxTestCase {
    public void testVFE1() {
        try {
            Class.forName("dot.junit.verify.a1.d.T_a1_1");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
