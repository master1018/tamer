@TestTargetClass(CharacterStyle.class)
public class CharacterStyleTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link CharacterStyle#wrap(CharacterStyle)}",
        method = "wrap",
        args = {android.text.style.CharacterStyle.class}
    )
    public void testWrap() {
        MetricAffectingSpan metricAffectingSpan = new SuperscriptSpan();
        CharacterStyle result = CharacterStyle.wrap(metricAffectingSpan);
        assertNotNull(result);
        assertSame(metricAffectingSpan, result.getUnderlying());
        assertNotSame(metricAffectingSpan, result);
        CharacterStyle characterStyle = new MyCharacterStyle();
        result = CharacterStyle.wrap(characterStyle);
        assertNotNull(result);
        assertTrue(result instanceof CharacterStyle);
        assertSame(characterStyle, result.getUnderlying());
        assertNotSame(characterStyle, result);
        result = CharacterStyle.wrap((MetricAffectingSpan) null);
        assertNotNull(result);
        assertTrue(result instanceof CharacterStyle);
        result = CharacterStyle.wrap((CharacterStyle) null);
        assertNotNull(result);
        assertTrue(result instanceof CharacterStyle);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link CharacterStyle#getUnderlying()}",
        method = "getUnderlying",
        args = {}
    )
    public void testGetUnderlying() {
        CharacterStyle expected = new MyCharacterStyle();
        assertSame(expected, expected.getUnderlying());
        MetricAffectingSpan metricAffectingSpan = new SuperscriptSpan();
        CharacterStyle result = CharacterStyle.wrap(metricAffectingSpan);
        assertNotNull(result);
        assertTrue(result instanceof MetricAffectingSpan);
        assertSame(metricAffectingSpan, result.getUnderlying());
    }
    private class MyCharacterStyle extends CharacterStyle {
        @Override
        public void updateDrawState(TextPaint tp) {
        }
    }
}
