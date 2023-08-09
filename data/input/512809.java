@TestTargetClass(ClickableSpan.class)
public class ClickableSpanTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link ClickableSpan#updateDrawState(TextPaint)}",
        method = "updateDrawState",
        args = {android.text.TextPaint.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "should add @throws clause into javadoc of " +
            "ClickableSpan#updateDrawState(TextPaint) when the input TextPaint is null")
    public void testUpdateDrawState() {
        ClickableSpan clickableSpan = new MyClickableSpan();
        TextPaint tp = new TextPaint();
        tp.linkColor = Color.RED;
        tp.setUnderlineText(false);
        assertFalse(tp.isUnderlineText());
        clickableSpan.updateDrawState(tp);
        assertEquals(Color.RED, tp.getColor());
        assertTrue(tp.isUnderlineText());
        tp.linkColor = Color.BLUE;
        clickableSpan.updateDrawState(tp);
        assertEquals(Color.BLUE, tp.getColor());
        assertTrue(tp.isUnderlineText());
        try {
            clickableSpan.updateDrawState(null);
            fail("should throw NullPointerException when TextPaint is null.");
        } catch (NullPointerException e) {
        }
    }
    private class MyClickableSpan extends ClickableSpan {
        @Override
        public void onClick(View widget) {
        }
    }
}
