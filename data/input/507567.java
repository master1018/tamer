@TestTargetClass(LayoutAnimationController.AnimationParameters.class)
public class LayoutAnimationController_AnimationParametersTest extends AndroidTestCase {
    @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "LayoutAnimationController.AnimationParameters",
            args = {}
        )
    public void testConstructor() {
        new LayoutAnimationController.AnimationParameters();
    }
}
