@TestTargetClass(MediaStore.class)
public class MediaStoreTest extends InstrumentationTestCase {
    private static final String TEST_VOLUME_NAME = "volume_for_cts";
    private static final String[] PROJECTION = new String[] { MediaStore.MEDIA_SCANNER_VOLUME };
    private Uri mScannerUri;
    private String mVolumnBackup;
    private ContentResolver mContentResolver;
    @Override
    protected void setUp() throws Exception {
        mScannerUri = MediaStore.getMediaScannerUri();
        mContentResolver = getInstrumentation().getContext().getContentResolver();
        Cursor c = mContentResolver.query(mScannerUri, PROJECTION, null, null, null);
        if (c != null) {
            c.moveToFirst();
            mVolumnBackup = c.getString(0);
            c.close();
        }
    }
    @Override
    protected void tearDown() throws Exception {
        if (mVolumnBackup != null) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.MEDIA_SCANNER_VOLUME, mVolumnBackup);
            mContentResolver.insert(mScannerUri, values);
        }
        super.tearDown();
    }
    @TestTargetNew(
      level = TestLevel.COMPLETE,
      method = "getMediaScannerUri",
      args = {}
    )
    public void testGetMediaScannerUri() {
        ContentValues values = new ContentValues();
        String selection = MediaStore.MEDIA_SCANNER_VOLUME + "=?";
        String[] selectionArgs = new String[] { TEST_VOLUME_NAME };
        assertNull(mContentResolver.query(mScannerUri, PROJECTION,
                selection, selectionArgs, null));
        values.put(MediaStore.MEDIA_SCANNER_VOLUME, TEST_VOLUME_NAME);
        assertEquals(MediaStore.getMediaScannerUri(),
                mContentResolver.insert(mScannerUri, values));
        Cursor c = mContentResolver.query(mScannerUri, PROJECTION,
                selection, selectionArgs, null);
        assertEquals(1, c.getCount());
        c.moveToFirst();
        assertEquals(TEST_VOLUME_NAME, c.getString(0));
        c.close();
        assertEquals(1, mContentResolver.delete(mScannerUri, null, null));
        assertNull(mContentResolver.query(mScannerUri, PROJECTION, null, null, null));
    }
}
