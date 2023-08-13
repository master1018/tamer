@TestTargetClass(PathDashPathEffect.Style.class)
public class PathDashPathEffect_StyleTest extends AndroidTestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "valueOf",
        args = {java.lang.String.class}
    )
    public void testValueOf() {
        assertEquals(Style.TRANSLATE, Style.valueOf("TRANSLATE"));
        assertEquals(Style.ROTATE, Style.valueOf("ROTATE"));
        assertEquals(Style.MORPH, Style.valueOf("MORPH"));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "values",
        args = {}
    )
    public void testValues() {
        Style[] expected = {
                Style.TRANSLATE,
                Style.ROTATE,
                Style.MORPH};
        Style[] actual = Style.values();
        assertEquals(expected.length, actual.length);
        for (int i = 0; i < actual.length; i ++) {
            assertEquals(expected[i], actual[i]);
        }
    }
}
