@TestTargetClass(MediaStore.Video.class)
public class MediaStore_VideoTest extends InstrumentationTestCase {
    private static final String TEST_VIDEO_3GP = "testVideo.3gp";
    private ArrayList<Uri> mRowsAdded;
    private Context mContext;
    private ContentResolver mContentResolver;
    private FileCopyHelper mHelper;
    @Override
    protected void tearDown() throws Exception {
        for (Uri row : mRowsAdded) {
            mContentResolver.delete(row, null, null);
        }
        mHelper.clear();
        super.tearDown();
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mContext = getInstrumentation().getTargetContext();
        mContentResolver = mContext.getContentResolver();
        mHelper = new FileCopyHelper(mContext);
        mRowsAdded = new ArrayList<Uri>();
    }
    @TestTargetNew(
      level = TestLevel.COMPLETE,
      method = "query",
      args = {ContentResolver.class, Uri.class, String[].class}
    )
    public void testQuery() {
        ContentValues values = new ContentValues();
        String valueOfData = mHelper.copy(R.raw.testvideo, TEST_VIDEO_3GP);
        values.put(VideoColumns.DATA, valueOfData);
        Uri newUri = mContentResolver.insert(Video.Media.INTERNAL_CONTENT_URI, values);
        if (!Video.Media.INTERNAL_CONTENT_URI.equals(newUri)) {
            mRowsAdded.add(newUri);
        }
        Cursor c = Video.query(mContentResolver, newUri, new String[] { VideoColumns.DATA });
        assertEquals(1, c.getCount());
        c.moveToFirst();
        assertEquals(valueOfData, c.getString(c.getColumnIndex(VideoColumns.DATA)));
        c.close();
    }
}
