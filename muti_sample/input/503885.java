@TestTargetClass(Context.class)
public class ContextTest extends AndroidTestCase {
    private Context mContext;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mContext = getContext();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test getString(int, Object...) and getString(int).",
            method = "getString",
            args = {int.class, java.lang.Object[].class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test getString(int, Object...) and getString(int).",
            method = "getString",
            args = {int.class}
        )
    })
    @ToBeFixed(bug = "1417734", explanation = "Unexpected NotFoundException")
    public void testGetString() {
        String testString = mContext.getString(R.string.context_test_string1);
        assertEquals("This is %s string.", testString);
        testString = mContext.getString(R.string.context_test_string1, "expected");
        assertEquals("This is expected string.", testString);
        testString = mContext.getString(R.string.context_test_string2);
        assertEquals("This is test string.", testString);
        try {
            testString = mContext.getString(0, "expected");
            fail("Wrong resource id should not be accepted.");
        } catch (NotFoundException e) {
        }
        try {
            testString = mContext.getString(0);
            fail("Wrong resource id should not be accepted.");
        } catch (NotFoundException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test getText(int).",
        method = "getText",
        args = {int.class}
    )
    @ToBeFixed(bug = "1417734", explanation = "Unexpected NotFoundException")
    public void testGetText() {
        CharSequence testCharSequence = mContext.getText(R.string.context_test_string2);
        assertEquals("This is test string.", testCharSequence.toString());
        try {
            testCharSequence = mContext.getText(0);
            fail("Wrong resource id should not be accepted.");
        } catch (NotFoundException e) {
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "obtainStyledAttributes",
            args = {int[].class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "obtainStyledAttributes",
            args = {int.class, int[].class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "obtainStyledAttributes",
            args = {android.util.AttributeSet.class, int[].class, int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "obtainStyledAttributes",
            args = {android.util.AttributeSet.class, int[].class}
        )
    })
    @ToBeFixed(bug=" ", explanation = "Wrong resource ID can not result in a exception.")
    public void testObtainStyledAttributes() {
        TypedArray testTypedArray = mContext
                .obtainStyledAttributes(com.android.internal.R.styleable.View);
        assertNotNull(testTypedArray);
        assertTrue(testTypedArray.length() > 2);
        assertTrue(testTypedArray.length() > 0);
        testTypedArray.recycle();
        testTypedArray = mContext.obtainStyledAttributes(android.R.style.TextAppearance_Small,
                com.android.internal.R.styleable.TextAppearance);
        assertNotNull(testTypedArray);
        assertTrue(testTypedArray.length() > 2);
        testTypedArray.recycle();
        try {
            testTypedArray = mContext.obtainStyledAttributes(-1, null);
            fail("obtainStyledAttributes will throw a NullPointerException here.");
        } catch (NullPointerException e) {
        }
        int testInt[] = { 0, 0 };
        testTypedArray = mContext.obtainStyledAttributes(-1, testInt);
        assertNotNull(testTypedArray);
        assertEquals(2, testTypedArray.length());
        testTypedArray.recycle();
        testTypedArray = mContext.obtainStyledAttributes(getAttributeSet(R.layout.context_layout),
                com.android.internal.R.styleable.DatePicker);
        assertNotNull(testTypedArray);
        assertEquals(2, testTypedArray.length());
        testTypedArray.recycle();
        testTypedArray = mContext.obtainStyledAttributes(getAttributeSet(R.layout.context_layout),
                com.android.internal.R.styleable.DatePicker, 0, 0);
        assertNotNull(testTypedArray);
        assertEquals(2, testTypedArray.length());
        testTypedArray.recycle();
    }
    private AttributeSet getAttributeSet(int resourceId) {
        final XmlResourceParser parser = getContext().getResources().getXml(
                resourceId);
        try {
            XmlUtils.beginDocument(parser, "RelativeLayout");
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        final AttributeSet attr = Xml.asAttributeSet(parser);
        assertNotNull(attr);
        return attr;
    }
}
