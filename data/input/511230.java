@TestTargetClass(AssetManager.AssetInputStream.class)
public class AssetManager_AssetInputStreamTest extends AndroidTestCase {
    private AssetManager.AssetInputStream mAssetInputStream;
    private final String CONTENT_STRING = "OneTwoThreeFourFiveSixSevenEightNineTen";
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mAssetInputStream = (AssetManager.AssetInputStream)mContext.getAssets().open("text.txt");
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "close",
        args = {}
    )
    public void testClose() throws IOException {
        mAssetInputStream.close();
        try {
            mAssetInputStream.read();
            fail("should throw exception");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "getAssetInt",
        args = {}
    )
    public void testGetAssetInt() {
        mAssetInputStream.getAssetInt();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "mark",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "reset",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "markSupported",
            args = {}
        )
    })
    public void testMarkReset() throws IOException {
        assertTrue(mAssetInputStream.markSupported());
        final int readlimit = 10;
        final byte[] bytes = CONTENT_STRING.getBytes();
        for (int i = 0; i < readlimit; i++) {
            assertEquals(bytes[i], mAssetInputStream.read());
        }
        mAssetInputStream.mark(readlimit);
        mAssetInputStream.reset();
        for (int i = 0; i < readlimit; i++) {
            assertEquals(bytes[i + readlimit], mAssetInputStream.read());
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "read",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test read method.",
            method = "read",
            args = {byte[].class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "read",
            args = {byte[].class, int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "skip",
            args = {long.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "available",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "reset",
            args = {}
        )
    })
    public void testReadMethods() throws IOException {
        final byte[] bytes = CONTENT_STRING.getBytes();
        int len = mAssetInputStream.available();
        int end = -1;
        assertEquals(CONTENT_STRING.length(), len);
        for (int i = 0; i < len; i++) {
            assertEquals(bytes[i], mAssetInputStream.read());
        }
        assertEquals(end, mAssetInputStream.read());
        mAssetInputStream.reset();
        int dataLength = 10;
        byte[] data = new byte[dataLength];
        int ret = mAssetInputStream.read(data);
        assertEquals(dataLength, ret);
        for (int i = 0; i < dataLength; i++) {
            assertEquals(bytes[i], data[i]);
        }
        data = new byte[len - dataLength];
        assertEquals(len - dataLength, mAssetInputStream.read(data));
        for (int i = 0; i < len - dataLength; i++) {
            assertEquals(bytes[i + dataLength], data[i]);
        }
        assertEquals(end, mAssetInputStream.read(data));
        mAssetInputStream.reset();
        int offset = 0;
        ret = mAssetInputStream.read(data, offset, dataLength);
        assertEquals(dataLength, ret);
        for (int i = offset; i < ret; i++) {
            assertEquals(bytes[i], data[offset + i]);
        }
        mAssetInputStream.reset();
        offset = 2;
        ret = mAssetInputStream.read(data, offset, dataLength);
        assertEquals(dataLength, ret);
        for (int i = offset; i < ret; i++) {
            assertEquals(bytes[i], data[offset + i]);
        }
        data = new byte[len + offset];
        ret = mAssetInputStream.read(data, offset, len);
        assertEquals(len - dataLength, ret);
        for (int i = offset; i < ret; i++) {
            assertEquals(bytes[i + dataLength], data[offset + i]);
        }
        assertEquals(end, mAssetInputStream.read(data, offset, len));
        mAssetInputStream.reset();
        assertEquals(0, mAssetInputStream.read(data, 0, 0));
        int skipLenth = 8;
        mAssetInputStream.reset();
        mAssetInputStream.skip(skipLenth);
        assertEquals(CONTENT_STRING.charAt(skipLenth), mAssetInputStream.read());
        try {
            mAssetInputStream.read(null);
            fail("should throw NullPointerException ");
        } catch (NullPointerException e) {
        }
        try {
            mAssetInputStream.read(null, 0, mAssetInputStream.available());
            fail("should throw NullPointerException ");
        } catch (NullPointerException e) {
        }
        try {
            data = new byte[10];
            mAssetInputStream.read(data, -1, mAssetInputStream.available());
            fail("should throw IndexOutOfBoundsException ");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            data = new byte[10];
            assertEquals(0, mAssetInputStream.read(data, 0, data.length + 2));
            fail("should throw IndexOutOfBoundsException ");
        } catch (IndexOutOfBoundsException e) {
        }
    }
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        mAssetInputStream.close();
    }
}
