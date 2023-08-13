@TestTargetClass(DrawFilter.class)
public class DrawFilterTest extends TestCase {
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "DrawFilter",
        args = {}
    )
    public void testConstructor() {
        new DrawFilter();
    }
}
