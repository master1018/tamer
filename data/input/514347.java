@TestTargetClass(AssetFileDescriptor.AutoCloseOutputStream.class)
public class AssetFileDescriptor_AutoCloseOutputStreamTest extends AndroidTestCase {
    private static final String FILE_NAME = "testAssertFileDescriptorAutoCloseOutputStream";
    private static final int FILE_LENGTH = 100;
    private static final int FILE_END = -1;
    private static final byte[] FILE_DATA = new byte[] {
            0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08
            };
    private AssetFileDescriptor mAssetFileDes;
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        if(mAssetFileDes != null) {
            mAssetFileDes.close();
        }
        getContext().deleteFile(FILE_NAME);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "AssetFileDescriptor.AutoCloseOutputStream",
            args = {AssetFileDescriptor.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "write",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "write",
            args = {byte[].class, int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "write",
            args = {byte[].class}
        )
    })
    public void testAutoCloseOutputStream() throws IOException {
        File file = new File(getContext().getFilesDir(), FILE_NAME);
        file.createNewFile();
        ParcelFileDescriptor fd = ParcelFileDescriptor.open(file,
                ParcelFileDescriptor.MODE_READ_WRITE);
        mAssetFileDes = new AssetFileDescriptor(fd, 0, FILE_LENGTH);
        AssetFileDescriptor.AutoCloseOutputStream outputStream =
                new AssetFileDescriptor.AutoCloseOutputStream(mAssetFileDes);
        outputStream.write(FILE_DATA[0]);
        outputStream.write(FILE_DATA, 1, 5);
        outputStream.write(FILE_DATA);
        outputStream.flush();
        mAssetFileDes.close();
        mAssetFileDes = null;
        fd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_WRITE);
        mAssetFileDes = new AssetFileDescriptor(fd, 0, FILE_LENGTH);
        FileInputStream inputStream = mAssetFileDes.createInputStream();
        for (int i = 0; i < 6; i++) {
            assertEquals(FILE_DATA[i], inputStream.read());
        }
        for (int i = 0; i < FILE_DATA.length; i++) {
            assertEquals(FILE_DATA[i], inputStream.read());
        }
        assertEquals(FILE_END, inputStream.read());
        mAssetFileDes.close();
        mAssetFileDes = null;
    }
}
