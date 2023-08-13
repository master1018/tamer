public class Test_a3 extends DxTestCase {
    public void testVFE1() {
        try {
            Class.forName("dot.junit.verify.a3.d.T_a3_1");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
