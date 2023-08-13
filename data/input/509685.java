public class RawResourceTest extends AndroidTestCase {
    private Resources mResources;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mResources = mContext.getResources();
    }
    @SmallTest
    public void testReadToEnd() throws IOException {
        final InputStream is = mResources.openRawResource(R.raw.text);
        verifyTextAsset(is);
    }
    static void verifyTextAsset(final InputStream is) throws IOException {
        final String expectedString = "OneTwoThreeFourFiveSixSevenEightNineTen";
        final byte[] buffer = new byte[10];
        int readCount;
        int curIndex = 0;
        while ((readCount = is.read(buffer, 0, buffer.length)) > 0) {
            for (int i = 0; i < readCount; i++) {
                assertEquals("At index " + curIndex
                            + " expected " + expectedString.charAt(curIndex)
                            + " but found " + ((char) buffer[i]),
                        buffer[i], expectedString.charAt(curIndex));
                curIndex++;
            }
        }
        readCount = is.read(buffer, 0, buffer.length);
        assertEquals("Reading end of buffer: expected readCount=-1 but got " + readCount,
                -1, readCount);
        readCount = is.read(buffer, buffer.length, 0);
        assertEquals("Reading end of buffer length 0: expected readCount=0 but got " + readCount,
                0, readCount);
        is.close();
    }
}
