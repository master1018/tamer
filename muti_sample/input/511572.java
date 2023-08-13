@TestTargetClass(CommaTokenizer.class)
public class MultiAutoCompleteTextView_CommaTokenizerTest extends TestCase {
    private static final String TEST_TEXT = "first token, second token";
    CommaTokenizer mCommaTokenizer;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mCommaTokenizer = new CommaTokenizer();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "MultiAutoCompleteTextView.CommaTokenizer",
        args = {}
    )
    public void testConstructor() {
        new CommaTokenizer();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "findTokenStart",
        args = {java.lang.CharSequence.class, int.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "does not declare the corner cases." +
            " 1. cursor is out of bound. 2. text is null.")
    public void testFindTokenStart() {
        int indexOfSecondToken = TEST_TEXT.indexOf("second");
        assertEquals(indexOfSecondToken,
                mCommaTokenizer.findTokenStart(TEST_TEXT, indexOfSecondToken));
        assertEquals(indexOfSecondToken - 1,
                mCommaTokenizer.findTokenStart(TEST_TEXT, indexOfSecondToken - 1));
        assertEquals(indexOfSecondToken,
                mCommaTokenizer.findTokenStart(TEST_TEXT, TEST_TEXT.length()));
        assertEquals(0, mCommaTokenizer.findTokenStart(TEST_TEXT, indexOfSecondToken - 2));
        assertEquals(0, mCommaTokenizer.findTokenStart(TEST_TEXT, 1));
        assertEquals(-1, mCommaTokenizer.findTokenStart(TEST_TEXT, -1));
        assertEquals(-2, mCommaTokenizer.findTokenStart(TEST_TEXT, -2));
        try {
            mCommaTokenizer.findTokenStart(TEST_TEXT, TEST_TEXT.length() + 1);
            fail("did not throw IndexOutOfBoundsException when cursor is large than length");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            mCommaTokenizer.findTokenStart(null, TEST_TEXT.length());
            fail("did not throw NullPointerException when text is null");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "findTokenEnd",
        args = {java.lang.CharSequence.class, int.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "does not declare the corner cases." +
            " 1. cursor is out of bound. 2. text is null.")
    public void testFindTokenEnd() {
        int indexOfComma = TEST_TEXT.indexOf(",");
        assertEquals(indexOfComma, mCommaTokenizer.findTokenEnd(TEST_TEXT, 0));
        assertEquals(indexOfComma, mCommaTokenizer.findTokenEnd(TEST_TEXT, indexOfComma - 1));
        assertEquals(indexOfComma, mCommaTokenizer.findTokenEnd(TEST_TEXT, indexOfComma));
        assertEquals(TEST_TEXT.length(),
                mCommaTokenizer.findTokenEnd(TEST_TEXT, indexOfComma + 1));
        assertEquals(TEST_TEXT.length(),
                mCommaTokenizer.findTokenEnd(TEST_TEXT, TEST_TEXT.length()));
        assertEquals(TEST_TEXT.length(),
                mCommaTokenizer.findTokenEnd(TEST_TEXT, TEST_TEXT.length() + 1));
        try {
            mCommaTokenizer.findTokenEnd(TEST_TEXT, -1);
            fail("did not throw IndexOutOfBoundsException when cursor is -1");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            mCommaTokenizer.findTokenEnd(null, 1);
            fail("did not throw NullPointerException when text is null");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "terminateToken",
        args = {java.lang.CharSequence.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "does not declare the corner cases." +
            " 1. text is null." +
            " 2. javadoc says ends with a token terminator (for example a space or comma)," +
            " but actually ends with both comma and space")
    public void testTerminateToken() {
        String text = "end with comma,";
        assertEquals(text, mCommaTokenizer.terminateToken(text));
        text = "end without comma";
        assertEquals(text + ", ", mCommaTokenizer.terminateToken(text));
        text = "end without comma!";
        assertEquals(text + ", ", mCommaTokenizer.terminateToken(text));
        text = "has ending spaces   ";
        assertEquals(text + ", ", mCommaTokenizer.terminateToken(text));
        text = "has ending spaces ,   ";
        assertEquals(text, mCommaTokenizer.terminateToken(text));
        text = "test Spanned text";
        SpannableString spannableString = new SpannableString(text);
        SpannableString expected = new SpannableString(text + ", ");
        UnderlineSpan what = new UnderlineSpan();
        spannableString.setSpan(what, 0, spannableString.length(),
                Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        SpannableString actual = (SpannableString) mCommaTokenizer.terminateToken(spannableString);
        assertEquals(expected.toString(), actual.toString());
        assertEquals(0, actual.getSpanStart(what));
        assertEquals(spannableString.length(), actual.getSpanEnd(what));
        try {
            mCommaTokenizer.terminateToken(null);
            fail("did not throw NullPointerException when text is null");
        } catch (NullPointerException e) {
        }
    }
}
