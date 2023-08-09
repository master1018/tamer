@TestTargetClass(HideReturnsTransformationMethod.class)
public class HideReturnsTransformationMethodTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test constructor "
                + "HideReturnsTransformationMethod#HideReturnsTransformationMethod().",
        method = "HideReturnsTransformationMethod",
        args = {}
    )
    public void testConstructor() {
        new HideReturnsTransformationMethod();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link HideReturnsTransformationMethod#getOriginal()}.",
        method = "getOriginal",
        args = {}
    )
    public void testGetOriginal() {
        MyHideReturnsTranformationMethod method = new MyHideReturnsTranformationMethod();
        TextMethodUtils.assertEquals(new char[] { '\r' }, method.getOriginal());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link HideReturnsTransformationMethod#getInstance()}.",
        method = "getInstance",
        args = {}
    )
    public void testGetInstance() {
        HideReturnsTransformationMethod method0 = HideReturnsTransformationMethod.getInstance();
        assertNotNull(method0);
        HideReturnsTransformationMethod method1 = HideReturnsTransformationMethod.getInstance();
        assertSame(method0, method1);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link HideReturnsTransformationMethod#getReplacement()}.",
        method = "getReplacement",
        args = {}
    )
    public void testGetReplacement() {
        MyHideReturnsTranformationMethod method = new MyHideReturnsTranformationMethod();
        TextMethodUtils.assertEquals(new char[] { '\uFEFF' }, method.getReplacement());
    }
    private static class MyHideReturnsTranformationMethod extends HideReturnsTransformationMethod {
        @Override
        protected char[] getOriginal() {
            return super.getOriginal();
        }
        @Override
        protected char[] getReplacement() {
            return super.getReplacement();
        }
    }
}
