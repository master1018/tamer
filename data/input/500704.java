@TestTargetClass(InputFilter.AllCaps.class)
public class InputFilter_AllCapsTest extends AndroidTestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test filter. Implicitly invoked by SpannableStringBuilder#replace and explicitly" +
                " test to make sure its functionality",
        method = "filter",
        args = {java.lang.CharSequence.class, int.class, int.class, android.text.Spanned.class,
                int.class, int.class}
    )
    public void testFilter() {
        CharSequence source = "Caps";
        SpannableStringBuilder dest = new SpannableStringBuilder("AllTest");
        AllCaps allCaps = new AllCaps();
        InputFilter[] filters = {allCaps};
        dest.setFilters(filters);
        String expectedString1 = "AllCAPSTest";
        dest.insert(3, source);
        assertEquals(expectedString1 , dest.toString());
        String expectedString2 = "AllCAPSCAPS";
        dest.replace(7, 11, source);
        assertEquals(expectedString2, dest.toString());
        dest.delete(0, 4);
        String expectedString3 = "APSCAPS";
        assertEquals(expectedString3, dest.toString());
        CharSequence beforeFilterSource = "TestFilter";
        String expectedAfterFilter = "STFIL";
        CharSequence actualAfterFilter =
            allCaps.filter(beforeFilterSource, 2, 7, dest, 0, beforeFilterSource.length());
        assertEquals(expectedAfterFilter, actualAfterFilter);
    }
}
