@TestTargetClass(Resources.Theme.class)
public class Resources_ThemeTest extends AndroidTestCase {
    private Resources.Theme mResTheme;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mResTheme = getContext().getResources().newTheme();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "applyStyle",
            args = {int.class, boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "dump",
            args = {int.class, java.lang.String.class, java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setTo",
            args = {android.content.res.Resources.Theme.class}
        )
    })
    public void testSetMethods() {
        mResTheme.applyStyle(R.raw.testmp3, false);
        mResTheme.dump(1, "hello", "world");
        final Theme other = getContext().getTheme();
        mResTheme.setTo(other);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "obtainStyledAttributes",
            args = {android.util.AttributeSet.class, int[].class, int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "obtainStyledAttributes",
            args = {int.class, int[].class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "obtainStyledAttributes",
            args = {int[].class}
        )
    })
    public void testObtainStyledAttributes() {
        final int[] attrs = new int[1];
        attrs[0] = R.raw.testmp3;
        TypedArray testTypedArray = mResTheme.obtainStyledAttributes(attrs);
        assertNotNull(testTypedArray);
        assertTrue(testTypedArray.length() > 0);
        testTypedArray.recycle();
        testTypedArray = mResTheme.obtainStyledAttributes(R.raw.testmp3, attrs);
        assertNotNull(testTypedArray);
        assertTrue(testTypedArray.length() > 0);
        testTypedArray.recycle();
        XmlPullParser parser = getContext().getResources().getXml(R.xml.colors);
        AttributeSet set = Xml.asAttributeSet(parser);
        attrs[0] = R.xml.colors;
        testTypedArray =mResTheme.obtainStyledAttributes(set, attrs, 0, 0);
        assertNotNull(testTypedArray);
        assertTrue(testTypedArray.length() > 0);
        testTypedArray.recycle();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "resolveAttribute",
        args = {int.class, android.util.TypedValue.class, boolean.class}
    )
    public void testResolveAttribute() {
        final TypedValue value = new TypedValue();
        getContext().getResources().getValue(R.raw.testmp3, value, true);
        assertFalse(mResTheme.resolveAttribute(R.raw.testmp3, value, false));
    }
}
