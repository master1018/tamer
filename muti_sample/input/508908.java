@TestTargetClass(LoginFilter.class)
public class LoginFilterTest extends TestCase {
    @ToBeFixed(bug="1448885", explanation="LoginFilter is an abstract class and its" +
            " constructors are all package private, we can not extends it directly" +
            " to test. So, we try to extends its subclass UsernameFilterGeneric to test")
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "filter",
        args = {java.lang.CharSequence.class, int.class, int.class, android.text.Spanned.class,
                int.class, int.class}
    )
    @ToBeFixed(bug="1417734", explanation="should add @throws clause into javadoc " +
        " of LoginFilter#filter(CharSequence, int, int, Spanned, int, int) when" +
        " the source or dest is null")
    public void testFilter() {
        CharSequence result;
        MockLoginFilter loginFilter = new MockLoginFilter();
        Spanned dest1 = new SpannedString("dest_without_invalid_char");
        Spanned dest2 = new SpannedString("&*dest_with_invalid_char#$");
        String source1 = "source_without_invalid_char";
        String source2 = "+=source_with_invalid_char%!";
        Spanned spannedSource = new SpannedString("&*spanned_source_with_invalid_char#$");
        assertFalse(loginFilter.isStarted());
        assertFalse(loginFilter.isStopped());
        assertEquals(0, loginFilter.getInvalidCharacterCount());
        assertNull(loginFilter.filter(source1, 0, source1.length(), dest1, 0, dest1.length()));
        assertTrue(loginFilter.isStarted());
        assertTrue(loginFilter.isStopped());
        assertEquals(0, loginFilter.getInvalidCharacterCount());
        loginFilter.reset();
        assertNull(loginFilter.filter(source1, 0, source1.length(), dest2, 5, 6));
        assertTrue(loginFilter.isStarted());
        assertTrue(loginFilter.isStopped());
        assertEquals(4, loginFilter.getInvalidCharacterCount());
        loginFilter = new MockLoginFilter(true);
        assertNull(loginFilter.filter(source2, 0, source2.length(),
                dest1, 0, dest1.length()));
        assertTrue(loginFilter.isStarted());
        assertTrue(loginFilter.isStopped());
        assertEquals(3, loginFilter.getInvalidCharacterCount());
        loginFilter.reset();
        assertNull(loginFilter.filter(spannedSource, 0, spannedSource.length(),
                dest1, 0, dest1.length()));
        assertTrue(loginFilter.isStarted());
        assertTrue(loginFilter.isStopped());
        assertEquals(4, loginFilter.getInvalidCharacterCount());
        loginFilter = new MockLoginFilter(false);
        result = loginFilter.filter(source2, 0, source2.length(), dest1, 0, dest1.length());
        assertFalse(result instanceof SpannableString);
        assertEquals("+source_with_invalid_char", result.toString());
        assertTrue(loginFilter.isStarted());
        assertTrue(loginFilter.isStopped());
        assertEquals(3, loginFilter.getInvalidCharacterCount());
        loginFilter.reset();
        result = loginFilter.filter(spannedSource, 0, spannedSource.length(),
                dest1, 0, dest1.length());
        assertEquals("spanned_source_with_invalid_char", result.toString());
        assertTrue(loginFilter.isStarted());
        assertTrue(loginFilter.isStopped());
        assertEquals(4, loginFilter.getInvalidCharacterCount());
        try {
            loginFilter.filter(null, 0, source1.length(), dest1, 0, dest1.length());
            fail("should throw NullPointerException when source is null");
        } catch (NullPointerException e) {
        }
        try {
            loginFilter.filter(source1, -1, source1.length() + 1, dest1, 0, dest1.length());
            fail("should throw StringIndexOutOfBoundsException" +
                    " when start and end are out of bound");
        } catch (StringIndexOutOfBoundsException e) {
        }
        assertNull("should return null when start is larger than end",
                loginFilter.filter(source1, source1.length(), 0, dest1, 0, dest1.length()));
        try {
            loginFilter.filter(source1, 0, source1.length(), null, 2, dest1.length());
            fail("should throw NullPointerException when dest is null");
        } catch (NullPointerException e) {
        }
        loginFilter.filter(source1, 0, source1.length(), dest1, -1, dest1.length() + 1);
        loginFilter.filter(source1, 0, source1.length(), dest1, dest1.length(),  0);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "onInvalidCharacter",
        args = {char.class}
    )
    public void testOnInvalidCharacter() {
        LoginFilter loginFilter = new MockLoginFilter();
        loginFilter.onInvalidCharacter('a');
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "onStop",
        args = {}
    )
    public void testOnStop() {
        LoginFilter loginFilter = new MockLoginFilter();
        loginFilter.onStop();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "onStart",
        args = {}
    )
    public void testOnStart() {
        LoginFilter loginFilter = new LoginFilter.UsernameFilterGeneric();
        loginFilter.onStart();
    }
    private final class MockLoginFilter extends LoginFilter.UsernameFilterGeneric {
        private int mInvalidCharacterCount;
        private boolean mIsStarted = false;
        private boolean mIsStopped = false;
        public MockLoginFilter() {
            super();
        }
        public MockLoginFilter(boolean appendInvalid) {
            super(appendInvalid);
        }
        @Override
        public void onInvalidCharacter(char c) {
            mInvalidCharacterCount++;
            super.onInvalidCharacter(c);
        }
        public int getInvalidCharacterCount() {
            return mInvalidCharacterCount;
        }
        @Override
        public void onStart() {
            mIsStarted = true;
            super.onStart();
        }
        public boolean isStarted() {
            return mIsStarted;
        }
        @Override
        public void onStop() {
            mIsStopped = true;
            super.onStop();
        }
        public boolean isStopped() {
            return mIsStopped;
        }
        public void reset() {
            mInvalidCharacterCount = 0;
            mIsStarted = false;
            mIsStopped = false;
        }
    }
}
