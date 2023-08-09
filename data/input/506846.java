public class CoreTestDummy extends TestCase {
    @AndroidOnly("")
    public void testAndroidOnlyPass() {
    }
    @AndroidOnly("")
    public void testAndroidOnlyFail() {
        fail("Oops!");
    }
    @BrokenTest("")
    public void testBrokenTestPass() {
    }
    @BrokenTest("")
    public void testBrokenTestFail() {
        fail("Oops!");
    }
    @KnownFailure("")
    public void testKnownFailurePass() {
    }
    @KnownFailure("")
    public void testKnownFailureFail() {
        fail("Oops!");
    }
    @SideEffect("")
    public void testSideEffectPass() {
    }
    @SideEffect("")
    public void testSideEffectFail() {
        fail("Oops!");
    }
    public void testNormalPass() {
    }
    public void testNormalFail() {
        fail("Oops!");
    }
}
