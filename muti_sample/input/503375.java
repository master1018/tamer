public class ArrayTest extends AndroidTestCase {
    private Resources mResources;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mResources = mContext.getResources();
    }
    private void checkEntry(final int resid, final int index, final Object res,
            final Object expected) {
        assertEquals("in resource 0x" + Integer.toHexString(resid)
                + " at index " + index, expected, res);
    }
    private void checkStringArray(final int resid, final String[] expected) {
        final String[] res = mResources.getStringArray(resid);
        assertEquals(res.length, expected.length);
        for (int i = 0; i < expected.length; i++) {
            checkEntry(resid, i, res[i], expected[i]);
        }
    }
    private void checkTextArray(final int resid, final String[] expected) {
        final CharSequence[] res = mResources.getTextArray(resid);
        assertEquals(res.length, expected.length);
        for (int i = 0; i < expected.length; i++) {
            checkEntry(resid, i, res[i], expected[i]);
        }
    }
    private void checkIntArray(final int resid, final int[] expected) {
        final int[] res = mResources.getIntArray(resid);
        assertEquals(res.length, expected.length);
        for (int i = 0; i < expected.length; i++) {
            assertEquals("in resource 0x" + Integer.toHexString(resid)
                    + " at index " + i, expected[i], res[i]);
        }
    }
    @SmallTest
    public void testStrings() throws Exception {
        checkStringArray(R.array.strings, new String[] {"zero", "1", "here"});
        checkTextArray(R.array.strings, new String[] {"zero", "1", "here"});
        checkStringArray(R.array.integers, new String[] {null, null, null});
        checkTextArray(R.array.integers, new String[] {null, null, null});
    }
    @SmallTest
    public void testIntegers() throws Exception {
        checkIntArray(R.array.strings, new int[] {0, 0, 0});
        checkIntArray(R.array.integers, new int[] {0, 1, 101});
    }
}
