@TestTargetClass(Genres.class)
public class MediaStore_Audio_GenresTest extends InstrumentationTestCase {
    private ContentResolver mContentResolver;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mContentResolver = getInstrumentation().getContext().getContentResolver();
    }
    @TestTargetNew(
      level = TestLevel.COMPLETE,
      method = "getContentUri",
      args = {String.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "Android API javadocs are incomplete. There is no "
            + "document related to the possible values of param volumeName. @throw clause "
            + "should be added in to javadoc when getting uri for internal volume.")
    public void testGetContentUri() {
        assertNotNull(mContentResolver.query(
                Genres.getContentUri(MediaStoreAudioTestHelper.EXTERNAL_VOLUME_NAME), null, null,
                    null, null));
        try {
            assertNotNull(mContentResolver.query(
                    Genres.getContentUri(MediaStoreAudioTestHelper.INTERNAL_VOLUME_NAME), null,
                        null, null, null));
            fail("Should throw SQLException as the internal datatbase has no genre");
        } catch (SQLException e) {
        }
        String volume = "fakeVolume";
        assertNull(mContentResolver.query(Genres.getContentUri(volume), null, null, null, null));
    }
    public void testStoreAudioGenresExternal() {
        ContentValues values = new ContentValues();
        values.put(Genres.NAME, "POP");
        Uri uri = mContentResolver.insert(Genres.EXTERNAL_CONTENT_URI, values);
        assertNotNull(uri);
        try {
            Cursor c = mContentResolver.query(uri, null, null, null, null);
            assertEquals(1, c.getCount());
            c.moveToFirst();
            assertEquals("POP", c.getString(c.getColumnIndex(Genres.NAME)));
            assertTrue(c.getLong(c.getColumnIndex(Genres._ID)) > 0);
            c.close();
            values.clear();
            values.put(Genres.NAME, "ROCK");
            assertEquals(1, mContentResolver.update(uri, values, null, null));
            c = mContentResolver.query(uri, null, null, null, null);
            c.moveToFirst();
            assertEquals("ROCK", c.getString(c.getColumnIndex(Genres.NAME)));
            c.close();
        } finally {
            assertEquals(1, mContentResolver.delete(uri, null, null));
        }
    }
    public void testStoreAudioGenresInternal() {
        ContentValues values = new ContentValues();
        values.put(Genres.NAME, "POP");
        Uri uri = mContentResolver.insert(Genres.INTERNAL_CONTENT_URI, values);
        assertNull(uri);
    }
}
