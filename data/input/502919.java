@TestTargetClass(Rasterizer.class)
public class RasterizerTest extends AndroidTestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test finalize function",
            method = "finalize",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test finalize function",
            method = "Rasterizer",
            args = {}
        )
    })
    public void testFinalize() {
        MockRasterizer mr = new MockRasterizer();
        try {
            mr.finalize();
        } catch (Throwable e) {
            fail(e.getMessage());
        }
    }
    class MockRasterizer extends Rasterizer {
        @Override
        public void finalize() throws Throwable {
            super.finalize();
        }
    }
}
