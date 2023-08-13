@TestTargetClass(AutoText.class)
public class AutoTextTest extends AndroidTestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "get",
        args = {java.lang.CharSequence.class, int.class, int.class, android.view.View.class}
    )
    public void testGet() {
        CharSequence src;
        String actual;
        Locale.setDefault(Locale.ENGLISH);
        View view = new View(getContext());
        src = "can";
        actual = AutoText.get(src, 0, src.length(), view);
        assertNull(actual);
        src = "acn";
        actual = AutoText.get(src, 0, src.length(), view);
        assertNotNull(actual);
        assertEquals("can", actual);
        src = "acn";
        actual = AutoText.get(src, 0, src.length() + 1, view);
        assertNull(actual);
        src = "acn";
        actual = AutoText.get(src, 0, src.length() - 1, view);
        assertNull(actual);
        src = "acnh";
        actual = AutoText.get(src, 0, src.length() - 1, view);
        assertNotNull(actual);
        assertEquals("can", actual);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getSize",
        args = {android.view.View.class}
    )
    public void testGetSize() {
        Locale.setDefault(Locale.ENGLISH);
        View view = new View(getContext());
        assertTrue(AutoText.getSize(view) > 0);
    }
}
