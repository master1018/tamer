@TestTargetClass(AssetFileDescriptor.class)
public class AssetFileDescriptorTest extends AndroidTestCase {
    private static final long START_OFFSET = 0;
    private static final long LENGTH = 100;
    private static final String FILE_NAME = "testAssetFileDescriptor";
    private static final byte[] FILE_DATA =
        new byte[] { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08 };
    private static final int FILE_END = -1;
    private AssetFileDescriptor mAssetFileDes;
    private File mFile;
    private ParcelFileDescriptor mFd;
    private FileOutputStream mOutputStream;
    private FileInputStream mInputStream;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mFile = new File(getContext().getFilesDir(), FILE_NAME);
        mFile.createNewFile();
        initAssetFileDescriptor();
    }
    private void initAssetFileDescriptor() throws FileNotFoundException {
        mFd = ParcelFileDescriptor.open(mFile, ParcelFileDescriptor.MODE_READ_WRITE);
        mAssetFileDes = new AssetFileDescriptor(mFd, START_OFFSET, LENGTH);
    }
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        if (mAssetFileDes != null) {
            mAssetFileDes.close();
        }
        getContext().deleteFile(FILE_NAME);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "close",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "createInputStream",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "createOutputStream",
            args = {}
        )
    })
    public void testInputOutputStream() throws IOException {
        mOutputStream = mAssetFileDes.createOutputStream();
        assertNotNull(mOutputStream);
        mOutputStream.write(FILE_DATA);
        mOutputStream.flush();
        mOutputStream.close();
        mOutputStream = null;
        try {
            mOutputStream = mAssetFileDes.createOutputStream();
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        try {
            mInputStream = mAssetFileDes.createInputStream();
            fail("Should throw IOException");
        } catch (IOException e) {
        }
        mAssetFileDes.close();
        mAssetFileDes = null;
        initAssetFileDescriptor();
        mInputStream = mAssetFileDes.createInputStream();
        assertNotNull(mInputStream);
        byte[] dataFromFile = new byte[FILE_DATA.length];
        int readLength = 0;
        int readByte = 0;
        while ((readByte != FILE_END) && (readLength < FILE_DATA.length)) {
            readLength += readByte;
            readByte = mInputStream.read(dataFromFile,
                    readLength, FILE_DATA.length - readLength);
        }
        assertEquals(FILE_DATA.length, readLength);
        assertTrue(Arrays.equals(FILE_DATA, dataFromFile));
        assertEquals(FILE_END, mInputStream.read());
        mInputStream.close();
        mInputStream = null;
        try {
            mInputStream = mAssetFileDes.createInputStream();
            fail("Should throw IOException");
        } catch (IOException e) {
        }
        try {
            mOutputStream = mAssetFileDes.createOutputStream();
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        mAssetFileDes.close();
        mAssetFileDes = null;
        initAssetFileDescriptor();
        mOutputStream = mAssetFileDes.createOutputStream();
        mAssetFileDes.close();
        mAssetFileDes = null;
        try {
            mOutputStream.write(FILE_DATA);
            fail("Should throw IOException");
        } catch (IOException e) {
        }
        initAssetFileDescriptor();
        mInputStream = mAssetFileDes.createInputStream();
        mAssetFileDes.close();
        mAssetFileDes = null;
        try {
            mInputStream.read();
            fail("Should throw IOException");
        } catch (IOException e) {
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test construct.",
            method = "AssetFileDescriptor",
            args = {ParcelFileDescriptor.class, long.class, long.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getParcelFileDescriptor",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getFileDescriptor",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getLength",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getStartOffset",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "toString",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "describeContents",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "writeToParcel",
            args = {Parcel.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getDeclaredLength",
            args = {}
        )
    })
    public void testMiscMethod() {
        new AssetFileDescriptor(null, START_OFFSET, LENGTH);
        assertEquals(LENGTH, mAssetFileDes.getLength());
        assertEquals(START_OFFSET, mAssetFileDes.getStartOffset());
        assertSame(mFd, mAssetFileDes.getParcelFileDescriptor());
        assertSame(mFd.getFileDescriptor(), mAssetFileDes.getFileDescriptor());
        assertNotNull(mAssetFileDes.toString());
        assertEquals(mFd.describeContents(), mAssetFileDes.describeContents());
        Parcel parcel = Parcel.obtain();
        mAssetFileDes.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        AssetFileDescriptor out = AssetFileDescriptor.CREATOR.createFromParcel(parcel);
        assertEquals(out.getStartOffset(), mAssetFileDes.getStartOffset());
        assertEquals(out.getDeclaredLength(), mAssetFileDes.getDeclaredLength());
        assertEquals(out.getParcelFileDescriptor().getStatSize(),
                mAssetFileDes.getParcelFileDescriptor().getStatSize());
    }
}
