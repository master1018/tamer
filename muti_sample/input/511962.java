@TestTargetClass(SpannedString.class)
public class SpannedStringTest extends AndroidTestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test constructor(s) of {@link SpannedString}",
        method = "SpannedString",
        args = {java.lang.CharSequence.class}
    )
    @ToBeFixed(bug = "1417734", explanation = "should add @throws clause into javadoc of " +
            " constructor SpannedString(CharSequence) when param CharSequence is null")
    public void testConstructor() {
        new SpannedString("test");
        try {
            new SpannedString(null);
            fail("should throw NullPointerException here");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link SpannedString#valueOf(CharSequence)}",
        method = "valueOf",
        args = {java.lang.CharSequence.class}
    )
    @ToBeFixed(bug = "1417734", explanation = "should add @throws clause into javadoc of " +
            "SpannedString#valueOf(CharSequence) when param CharSequence is null")
    public void testValueOf() {
        String text = "test valueOf";
        SpannedString spanned = SpannedString.valueOf(text);
        assertEquals(text, spanned.toString());
        spanned = new SpannedString(text);
        assertSame(spanned, SpannedString.valueOf(spanned));
        try {
            SpannedString.valueOf(null);
            fail("should throw NullPointerException here");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link SpannedString#subSequence(int, int)}",
        method = "subSequence",
        args = {int.class, int.class}
    )
    @ToBeFixed(bug = "1417734", explanation = "should add @throws clause into javadoc of " +
            "SpannedString#subSequence(int, int) when index is out of bounds")
    public void testSubSequence() {
        String text = "hello, world";
        SpannedString spanned = new SpannedString(text);
        CharSequence subSequence = spanned.subSequence(0, 2);
        assertTrue(subSequence instanceof SpannedString);
        assertEquals("he", subSequence.toString());
        subSequence = spanned.subSequence(0, text.length());
        assertTrue(subSequence instanceof SpannedString);
        assertEquals(text, subSequence.toString());
        try {
            spanned.subSequence(-1, text.length() + 1);
            fail("subSequence failed when index is out of bounds");
        } catch (StringIndexOutOfBoundsException e) {
        }
        try {
            spanned.subSequence(2, 0);
            fail("subSequence failed on invalid index");
        } catch (StringIndexOutOfBoundsException e) {
        }
    }
}
