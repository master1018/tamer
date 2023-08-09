@TestTargetClass(Factory.class)
public class Spannable_FactoryTest extends AndroidTestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "newSpannable",
        args = {java.lang.CharSequence.class}
    )
    @ToBeFixed(bug="1695243", explanation="should add @throws clause into javadoc of "
        + "Spannable.Factory#newSpannable(CharSequence) when param CharSequence is null")
    public void testNewSpannable() {
        final String text = "test newSpannable";
        Factory factory = Spannable.Factory.getInstance();
        Spannable spannable = factory.newSpannable(text);
        assertNotNull(spannable);
        assertTrue(spannable instanceof SpannableString);
        assertEquals(text, spannable.toString());
        try {
            factory.newSpannable(null);
            fail("should throw NullPointerException here");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getInstance",
        args = {}
    )
    public void testGetInstance() {
        Spannable.Factory factory = Spannable.Factory.getInstance();
        assertNotNull(factory);
        assertSame(factory, Spannable.Factory.getInstance());
    }
}
