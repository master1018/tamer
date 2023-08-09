public class DownloadProviderPermissionsTest extends AndroidTestCase {
    private ContentResolver mContentResolver;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mContentResolver = getContext().getContentResolver();
    }
    @MediumTest
    public void testAccessCacheFilesystem() throws IOException {
        try {
            String filePath = "/cache/this-should-not-exist.txt";
            FileOutputStream strm = new FileOutputStream(filePath);
            strm.write("Oops!".getBytes());
            strm.flush();
            strm.close();
            fail("Was able to create and write to " + filePath);
        } catch (SecurityException e) {
        } catch (FileNotFoundException e) {
        }
    }
    @MediumTest
    public void testReadDownloadProvider() throws IOException {
        try {
            mContentResolver.query(Downloads.Impl.CONTENT_URI, null, null, null, null);
            fail("read from provider did not throw SecurityException as expected.");
        } catch (SecurityException e) {
        }
    }
    @MediumTest
    public void testWriteDownloadProvider() throws IOException {
        try {
            ContentValues values = new ContentValues();
            values.put(Downloads.Impl.COLUMN_URI, "foo");
            mContentResolver.insert(Downloads.Impl.CONTENT_URI, values);
            fail("write to provider did not throw SecurityException as expected.");
        } catch (SecurityException e) {
        }
    }
    @MediumTest
    public void testStartDownloadService() throws IOException {
        try {
            Intent downloadServiceIntent = new Intent();
            downloadServiceIntent.setClassName("com.android.providers.downloads",
                    "com.android.providers.downloads.DownloadService");
            getContext().startService(downloadServiceIntent);
            fail("starting download service did not throw SecurityException as expected.");
        } catch (SecurityException e) {
        }
    }
}
