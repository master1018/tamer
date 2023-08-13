@TestTargetClass(AlteredCharSequence.class)
public class AlteredCharSequenceTest extends AndroidTestCase {
    private static final String SOURCE_STR = "This is a char sequence.";
    private AlteredCharSequence mAlteredCharSequence;
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "charAt",
        args = {int.class}
    )
    public void testCharAt() {
        mAlteredCharSequence = null;
        char[] sub = { 'i', 's' };
        CharSequence source = "abcdefgh";
        mAlteredCharSequence = AlteredCharSequence.make(source, sub, 0, sub.length);
        assertEquals('i', mAlteredCharSequence.charAt(0));
        assertEquals('s', mAlteredCharSequence.charAt(1));
        assertEquals('c', mAlteredCharSequence.charAt(2));
        assertEquals('d', mAlteredCharSequence.charAt(3));
        try {
            mAlteredCharSequence.charAt(-1);
            fail("should raise a StringIndexOutOfBoundsException.");
        } catch (StringIndexOutOfBoundsException e) {
        }
        try {
            mAlteredCharSequence.charAt(mAlteredCharSequence.length() + 1);
            fail("should raise a StringIndexOutOfBoundsException.");
        } catch (StringIndexOutOfBoundsException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getChars",
        args = {int.class, int.class, char[].class, int.class}
    )
    public void testGetChars() {
        mAlteredCharSequence = null;
        char[] sub = { 'i', 's' };
        int start = 0;
        int end = 2;
        int off = 1;
        mAlteredCharSequence = AlteredCharSequence.make(SOURCE_STR, sub, 0, sub.length);
        char[] dest = new char[4];
        mAlteredCharSequence.getChars(start, end, dest, off);
        char[] expected = { 0, 'T', 'h', 0 };
        for (int i = off; i < end - start + off; i++) {
            assertEquals(expected[i], dest[i]);
        }
        end = 0;
        for (int i = 0; i < 4; i++) {
            dest[i] = 'a';
        }
        mAlteredCharSequence.getChars(start, end, dest, off);
        for (int i = off; i < end - start + off; i++) {
            assertEquals('a', dest[i]);
        }
        start = end + 1;
        try {
            mAlteredCharSequence.getChars(start, end, dest, off);
            fail("should raise a StringIndexOutOfBoundsException.");
        } catch (StringIndexOutOfBoundsException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "length",
        args = {}
    )
    public void testLength() {
        char[] sub = { 'i', 's' };
        CharSequence source = SOURCE_STR;
        for (int i = 1; i < 10; i++) {
            source = source + "a";
            mAlteredCharSequence = AlteredCharSequence.make(source, sub, 0, sub.length);
            assertEquals(source.length(), mAlteredCharSequence.length());
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "make",
        args = {java.lang.CharSequence.class, char[].class, int.class, int.class}
    )
    public void testMake() {
        mAlteredCharSequence = null;
        char[] sub = { 'i', 's' };
        CharSequence source = SOURCE_STR;
        mAlteredCharSequence = AlteredCharSequence.make(source, sub, 0, sub.length);
        assertNotNull(mAlteredCharSequence);
        assertEquals(source.toString(), mAlteredCharSequence.toString());
        String acsClassName = mAlteredCharSequence.getClass().getName();
        MockSpanned spanned = new MockSpanned("This is a spanned.");
        mAlteredCharSequence = AlteredCharSequence.make(spanned, sub, 0, sub.length);
        assertNotNull(mAlteredCharSequence);
        assertEquals(0, mAlteredCharSequence.length());
        String spanClassName = mAlteredCharSequence.getClass().getName();
        assertFalse(0 == acsClassName.compareTo(spanClassName));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "subSequence",
        args = {int.class, int.class}
    )
    public void testSubSequence() {
        mAlteredCharSequence = null;
        char[] sub = { 'i', 's' };
        CharSequence source = SOURCE_STR;
        mAlteredCharSequence = AlteredCharSequence.make(source, sub, 0, sub.length);
        assertEquals("Th", mAlteredCharSequence.subSequence(0, 2).toString());
        try {
            mAlteredCharSequence.subSequence(0, 100);
            fail("Should throw StringIndexOutOfBoundsException!");
        } catch (StringIndexOutOfBoundsException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "toString",
        args = {}
    )
    public void testToString() {
        mAlteredCharSequence = null;
        char[] sub = { 'i', 's' };
        CharSequence source = SOURCE_STR;
        mAlteredCharSequence = AlteredCharSequence.make(source, sub, 0, sub.length);
        assertNotNull(mAlteredCharSequence.toString());
    }
    class MockSpanned implements Spanned {
        public MockSpanned(String sequence) {
        }
        public int getSpanEnd(Object tag) {
            return 0;
        }
        public int getSpanFlags(Object tag) {
            return 0;
        }
        public int getSpanStart(Object tag) {
            return 0;
        }
        public <T> T[] getSpans(int start, int end, Class<T> type) {
            return null;
        }
        @SuppressWarnings("unchecked")
        public int nextSpanTransition(int start, int limit, Class type) {
            return 0;
        }
        public char charAt(int index) {
            return 0;
        }
        public int length() {
            return 0;
        }
        public CharSequence subSequence(int start, int end) {
            return null;
        }
    }
}
