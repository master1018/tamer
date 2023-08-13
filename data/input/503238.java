@TestTargetClass(AnimationParameters.class)
public class GridLayoutAnimationController_AnimationParametersTest extends AndroidTestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "GridLayoutAnimationController.AnimationParameters",
        args = {}
    )
    public void testConstructor() {
        new GridLayoutAnimationController.AnimationParameters();
    }
}
