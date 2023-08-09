@TestTargetClass(MediaStore.Images.Thumbnails.class)
public class MediaStore_Images_ThumbnailsTest extends InstrumentationTestCase {
    private ArrayList<Uri> mRowsAdded;
    private Context mContext;
    private ContentResolver mContentResolver;
    private FileCopyHelper mHelper;
    @Override
    protected void tearDown() throws Exception {
        for (Uri row : mRowsAdded) {
            try {
                mContentResolver.delete(row, null, null);
            } catch (UnsupportedOperationException e) {
            }
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
    @TestTargets({
      @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "queryMiniThumbnails",
        args = {ContentResolver.class, Uri.class, int.class, String[].class}
      ),
      @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "query",
        args = {ContentResolver.class, Uri.class, String[].class}
      )
    })
    public void testQueryInternalThumbnails() {
        Cursor c = Thumbnails.queryMiniThumbnails(mContentResolver,
                Thumbnails.INTERNAL_CONTENT_URI, Thumbnails.MICRO_KIND, null);
        int previousMicroKindCount = c.getCount();
        c.close();
        String path = mHelper.copy(R.raw.scenery, "testThumbnails.jpg");
        ContentValues values = new ContentValues();
        values.put(Thumbnails.KIND, Thumbnails.MINI_KIND);
        values.put(Thumbnails.DATA, path);
        Uri uri = mContentResolver.insert(Thumbnails.INTERNAL_CONTENT_URI, values);
        if (uri != null) {
            mRowsAdded.add(uri);
        }
        c = Thumbnails.queryMiniThumbnails(mContentResolver, uri, Thumbnails.MINI_KIND, null);
        c.moveToFirst();
        assertEquals(1, c.getCount());
        assertEquals(Thumbnails.MINI_KIND, c.getInt(c.getColumnIndex(Thumbnails.KIND)));
        assertEquals(path, c.getString(c.getColumnIndex(Thumbnails.DATA)));
        c = Thumbnails.queryMiniThumbnails(mContentResolver, Thumbnails.INTERNAL_CONTENT_URI,
                Thumbnails.MICRO_KIND, null);
        assertEquals(previousMicroKindCount, c.getCount());
        c.close();
        c = Thumbnails.query(mContentResolver, uri, null);
        assertEquals(1, c.getCount());
        c.moveToFirst();
        assertEquals(Thumbnails.MINI_KIND, c.getInt(c.getColumnIndex(Thumbnails.KIND)));
        assertEquals(path, c.getString(c.getColumnIndex(Thumbnails.DATA)));
        c.close();
    }
    @TestTargets({
      @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "queryMiniThumbnail",
        args = {ContentResolver.class, long.class, int.class, String[].class}
      ),
      @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "query",
        args = {ContentResolver.class, Uri.class, String[].class}
      )
    })
    public void testQueryExternalMiniThumbnails() {
        Bitmap src = BitmapFactory.decodeResource(mContext.getResources(), R.raw.scenery);
        String stringUrl = null;
        try{
            stringUrl = Media.insertImage(mContentResolver, src, null, null);
        } catch (UnsupportedOperationException e) {
            fail("There is no sdcard attached! " + e.getMessage());
        }
        assertNotNull(stringUrl);
        mRowsAdded.add(Uri.parse(stringUrl));
        Cursor c = mContentResolver.query(Uri.parse(stringUrl), new String[]{ Media._ID }, null,
                null, null);
        c.moveToFirst();
        long imageId = c.getLong(c.getColumnIndex(Media._ID));
        c.close();
        String[] sizeProjection = new String[] { Thumbnails.WIDTH, Thumbnails.HEIGHT };
        c = Thumbnails.queryMiniThumbnail(mContentResolver, imageId, Thumbnails.MINI_KIND,
                sizeProjection);
        assertEquals(1, c.getCount());
        assertTrue(c.moveToFirst());
        assertTrue(c.getLong(c.getColumnIndex(Thumbnails.WIDTH)) >= Math.min(src.getWidth(), 240));
        assertTrue(c.getLong(c.getColumnIndex(Thumbnails.HEIGHT)) >= Math.min(src.getHeight(), 240));
        c.close();
        c = Thumbnails.queryMiniThumbnail(mContentResolver, imageId, Thumbnails.MICRO_KIND,
                sizeProjection);
        assertEquals(1, c.getCount());
        assertTrue(c.moveToFirst());
        assertEquals(50, c.getLong(c.getColumnIndex(Thumbnails.WIDTH)));
        assertEquals(50, c.getLong(c.getColumnIndex(Thumbnails.HEIGHT)));
        c.close();
    }
    @TestTargetNew(
      level = TestLevel.COMPLETE,
      method = "getContentUri",
      args = {String.class}
    )
    public void testGetContentUri() {
        assertNotNull(mContentResolver.query(Thumbnails.getContentUri("internal"), null, null,
                null, null));
        assertNotNull(mContentResolver.query(Thumbnails.getContentUri("external"), null, null,
                null, null));
        String volume = "fakeVolume";
        assertNull(mContentResolver.query(Thumbnails.getContentUri(volume), null, null, null,
                null));
    }
    public void testStoreImagesMediaExternal() {
        final String externalImgPath = Environment.getExternalStorageDirectory() +
                "/testimage.jpg";
        final String externalImgPath2 = Environment.getExternalStorageDirectory() +
                "/testimage1.jpg";
        ContentValues values = new ContentValues();
        values.put(Thumbnails.KIND, Thumbnails.FULL_SCREEN_KIND);
        values.put(Thumbnails.IMAGE_ID, 0);
        values.put(Thumbnails.HEIGHT, 480);
        values.put(Thumbnails.WIDTH, 320);
        values.put(Thumbnails.DATA, externalImgPath);
        Uri uri = mContentResolver.insert(Thumbnails.EXTERNAL_CONTENT_URI, values);
        assertNotNull(uri);
        Cursor c = mContentResolver.query(uri, null, null, null, null);
        assertEquals(1, c.getCount());
        c.moveToFirst();
        long id = c.getLong(c.getColumnIndex(Thumbnails._ID));
        assertTrue(id > 0);
        assertEquals(Thumbnails.FULL_SCREEN_KIND, c.getInt(c.getColumnIndex(Thumbnails.KIND)));
        assertEquals(0, c.getLong(c.getColumnIndex(Thumbnails.IMAGE_ID)));
        assertEquals(480, c.getInt(c.getColumnIndex(Thumbnails.HEIGHT)));
        assertEquals(320, c.getInt(c.getColumnIndex(Thumbnails.WIDTH)));
        assertEquals(externalImgPath, c.getString(c.getColumnIndex(Thumbnails.DATA)));
        c.close();
        values.clear();
        values.put(Thumbnails.KIND, Thumbnails.MICRO_KIND);
        values.put(Thumbnails.IMAGE_ID, 1);
        values.put(Thumbnails.HEIGHT, 50);
        values.put(Thumbnails.WIDTH, 50);
        values.put(Thumbnails.DATA, externalImgPath2);
        assertEquals(1, mContentResolver.update(uri, values, null, null));
        assertEquals(1, mContentResolver.delete(uri, null, null));
    }
    public void testStoreImagesMediaInternal() {
        try {
            mContentResolver.insert(Thumbnails.INTERNAL_CONTENT_URI, new ContentValues());
            fail("Should throw UnsupportedOperationException when inserting into internal "
                    + "database");
        } catch (UnsupportedOperationException e) {
        }
    }
}
