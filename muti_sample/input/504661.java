@TestTargetClass(InputFilter.LengthFilter.class)
public class InputFilter_LengthFilterTest extends AndroidTestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "filter",
            args = {java.lang.CharSequence.class, int.class, int.class, android.text.Spanned.class,
                    int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "InputFilter.LengthFilter",
            args = {int.class}
        )
    })
    public void testFilter() {
        CharSequence source;
        SpannableStringBuilder dest;
        LengthFilter lengthFilter = new LengthFilter(10);
        InputFilter[] filters = {lengthFilter};
        source = "abc";
        dest = new SpannableStringBuilder("abcdefgh");
        dest.setFilters(filters);
        dest.insert(1, source);
        String expectedString1 = "aabbcdefgh";
        assertEquals(expectedString1, dest.toString());
        dest.replace(5, 8, source);
        String expectedString2 = "aabbcabcgh";
        assertEquals(expectedString2, dest.toString());
        dest.delete(1, 3);
        String expectedString3 = "abcabcgh";
        assertEquals(expectedString3, dest.toString());
        dest = new SpannableStringBuilder("abcdefgh");
        CharSequence beforeFilterSource = "TestLengthFilter";
        String expectedAfterFilter = "TestLength";
        CharSequence actualAfterFilter = lengthFilter.filter(beforeFilterSource, 0,
                beforeFilterSource.length(), dest, 0, dest.length());
        assertEquals(expectedAfterFilter, actualAfterFilter);
    }
}
