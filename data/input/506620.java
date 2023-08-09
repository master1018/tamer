@TestTargetClass(ReplacementSpan.class)
public class ReplacementSpanTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test updateMeasureState(TextPaint p). this method does nothing",
        method = "updateMeasureState",
        args = {android.text.TextPaint.class}
    )
    public void testUpdateMeasureState() {
        ReplacementSpan replacementSpan = new MockReplacementSpan();
        replacementSpan.updateMeasureState(null);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test updateDrawState(TextPaint ds). this method does nothing",
        method = "updateDrawState",
        args = {android.text.TextPaint.class}
    )
    public void testUpdateDrawState() {
        ReplacementSpan replacementSpan = new MockReplacementSpan();
        replacementSpan.updateDrawState(null);
    }
    private class MockReplacementSpan extends ReplacementSpan {
        @Override
        public void draw(Canvas canvas, CharSequence text, int start, int end,
                float x, int top, int y, int bottom, Paint paint) {
        }
        @Override
        public int getSize(Paint paint, CharSequence text, int start, int end,
                FontMetricsInt fm) {
            return 0;
        }
    }
}
