@TestTargetClass(BoringLayout.Metrics.class)
public class BoringLayout_MetricsTest extends AndroidTestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "toString",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "BoringLayout.Metrics",
            args = {}
        )
    })
    public void testMetrics() {
        BoringLayout.Metrics bm = new BoringLayout.Metrics();
        assertNotNull(bm.toString());
    }
}
