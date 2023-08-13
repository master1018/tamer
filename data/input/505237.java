@TestTargetClass(PathEffect.class)
public class PathEffectTest extends AndroidTestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "PathEffect",
        args = {}
    )
    public void testConstructor() {
        new PathEffect();
    }
}
