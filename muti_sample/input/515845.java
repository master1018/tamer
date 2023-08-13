public class AssetTest extends AndroidTestCase {
    private AssetManager mAssets;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mAssets = mContext.getAssets();
    }
    public static void verifyTextAsset(InputStream is) throws IOException {
        String expectedString = "OneTwoThreeFourFiveSixSevenEightNineTen";
        byte[] buffer = new byte[10];
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
    @SmallTest
    public void testReadToEnd() throws Exception {
        InputStream is = mAssets.open("text.txt");
        verifyTextAsset(is);
    }
    public void xxtestListDir() throws Exception {
        String[] files = mAssets.list("");
        assertEquals(1, files.length);
        assertEquals("test.txt", files[0]);
    }
}
