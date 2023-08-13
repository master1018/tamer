@TestTargetClass(MaskFilter.class)
public class MaskFilterTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "MaskFilter",
        args = {}
    )
    public void testConstructor() {
        new MaskFilter();
    }
}
