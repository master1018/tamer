@TestTargetClass(MetricAffectingSpan.class)
public class MetricAffectingSpanTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link MetricAffectingSpan#getUnderlying()}",
        method = "getUnderlying",
        args = {}
    )
    public void testGetUnderlying() {
        MetricAffectingSpan metricAffectingSpan = new MyMetricAffectingSpan();
        assertSame(metricAffectingSpan, metricAffectingSpan.getUnderlying());
        metricAffectingSpan = new SuperscriptSpan();
        CharacterStyle result = CharacterStyle.wrap(metricAffectingSpan);
        assertNotNull(result);
        assertTrue(result instanceof MetricAffectingSpan);
        assertSame(metricAffectingSpan, result.getUnderlying());
    }
    private class MyMetricAffectingSpan extends MetricAffectingSpan {
        @Override
        public void updateMeasureState(TextPaint p) {
        }
        @Override
        public void updateDrawState(TextPaint tp) {
        }
    }
}
