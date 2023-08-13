@TestTargetClass(PorterDuff.Mode.class)
public class PorterDuff_ModeTest extends AndroidTestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "valueOf",
        args = {java.lang.String.class}
    )
    public void testValueOf() {
        assertEquals(Mode.CLEAR, Mode.valueOf("CLEAR"));
        assertEquals(Mode.SRC, Mode.valueOf("SRC"));
        assertEquals(Mode.DST, Mode.valueOf("DST"));
        assertEquals(Mode.SRC_OVER, Mode.valueOf("SRC_OVER"));
        assertEquals(Mode.DST_OVER, Mode.valueOf("DST_OVER"));
        assertEquals(Mode.SRC_IN, Mode.valueOf("SRC_IN"));
        assertEquals(Mode.DST_IN, Mode.valueOf("DST_IN"));
        assertEquals(Mode.SRC_OUT, Mode.valueOf("SRC_OUT"));
        assertEquals(Mode.DST_OUT, Mode.valueOf("DST_OUT"));
        assertEquals(Mode.SRC_ATOP, Mode.valueOf("SRC_ATOP"));
        assertEquals(Mode.DST_ATOP, Mode.valueOf("DST_ATOP"));
        assertEquals(Mode.XOR, Mode.valueOf("XOR"));
        assertEquals(Mode.DARKEN, Mode.valueOf("DARKEN"));
        assertEquals(Mode.LIGHTEN, Mode.valueOf("LIGHTEN"));
        assertEquals(Mode.MULTIPLY, Mode.valueOf("MULTIPLY"));
        assertEquals(Mode.SCREEN, Mode.valueOf("SCREEN"));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "values",
        args = {}
    )
    public void testValues() {
        Mode[] expected = {
                Mode.CLEAR,
                Mode.SRC,
                Mode.DST,
                Mode.SRC_OVER,
                Mode.DST_OVER,
                Mode.SRC_IN,
                Mode.DST_IN,
                Mode.SRC_OUT,
                Mode.DST_OUT,
                Mode.SRC_ATOP,
                Mode.DST_ATOP,
                Mode.XOR,
                Mode.DARKEN,
                Mode.LIGHTEN,
                Mode.MULTIPLY,
                Mode.SCREEN};
        Mode[] actual = Mode.values();
        assertEquals(expected.length, actual.length);
        for (int i = 0; i < actual.length; i ++) {
            assertEquals(expected[i], actual[i]);
        }
    }
}
