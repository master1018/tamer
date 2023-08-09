@TestTargetClass(Media.class)
public class MediaStore_Audio_MediaTest extends InstrumentationTestCase {
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
    public void testGetContentUri() {
        assertNotNull(mContentResolver.query(
                Media.getContentUri(MediaStoreAudioTestHelper.INTERNAL_VOLUME_NAME), null, null,
                    null, null));
        assertNotNull(mContentResolver.query(
                Media.getContentUri(MediaStoreAudioTestHelper.EXTERNAL_VOLUME_NAME), null, null,
                    null, null));
        String volume = "faveVolume";
        assertNull(mContentResolver.query(Media.getContentUri(volume), null, null, null, null));
    }
    @TestTargetNew(
      level = TestLevel.COMPLETE,
      method = "getContentUriForPath",
      args = {String.class}
    )
    public void testGetContentUriForPath() {
        String externalPath = Environment.getExternalStorageDirectory().getPath();
        assertNotNull(mContentResolver.query(Media.getContentUriForPath(externalPath), null, null,
                null, null));
        String internalPath =
            getInstrumentation().getTargetContext().getFilesDir().getAbsolutePath();
        assertNotNull(mContentResolver.query(Media.getContentUriForPath(internalPath), null, null,
                null, null));
    }
    @ToBeFixed(bug = "", explanation = "The column Media.ALBUM_ART does not exist.")
    public void testStoreAudioMediaInternal() {
        testStoreAudioMedia(true);
    }
    @ToBeFixed(bug = "", explanation = "The column Media.ALBUM_ART does not exist.")
    public void testStoreAudioMediaExternal() {
        testStoreAudioMedia(false);
    }
    private void testStoreAudioMedia(boolean isInternal) {
        Audio1 audio1 = Audio1.getInstance();
        ContentValues values = audio1.getContentValues(isInternal);
        Uri mediaUri = isInternal ? Media.INTERNAL_CONTENT_URI : Media.EXTERNAL_CONTENT_URI;
        Uri uri = mContentResolver.insert(mediaUri, values);
        assertNotNull(uri);
        try {
            Cursor c = mContentResolver.query(uri, null, null, null, null);
            assertEquals(1, c.getCount());
            c.moveToFirst();
            assertTrue(c.getLong(c.getColumnIndex(Media._ID)) > 0);
            String expected = isInternal ? Audio1.INTERNAL_DATA : Audio1.EXTERNAL_DATA;
            assertEquals(expected, c.getString(c.getColumnIndex(Media.DATA)));
            assertTrue(c.getLong(c.getColumnIndex(Media.DATE_ADDED)) > 0);
            assertEquals(Audio1.DATE_MODIFIED, c.getLong(c.getColumnIndex(Media.DATE_MODIFIED)));
            assertEquals(Audio1.FILE_NAME, c.getString(c.getColumnIndex(Media.DISPLAY_NAME)));
            assertEquals(Audio1.MIME_TYPE, c.getString(c.getColumnIndex(Media.MIME_TYPE)));
            assertEquals(Audio1.SIZE, c.getInt(c.getColumnIndex(Media.SIZE)));
            assertEquals(Audio1.TITLE, c.getString(c.getColumnIndex(Media.TITLE)));
            assertEquals(Audio1.ALBUM, c.getString(c.getColumnIndex(Media.ALBUM)));
            String albumKey = c.getString(c.getColumnIndex(Media.ALBUM_KEY));
            assertNotNull(albumKey);
            long albumId = c.getLong(c.getColumnIndex(Media.ALBUM_ID));
            assertTrue(albumId > 0);
            assertEquals(Audio1.ARTIST, c.getString(c.getColumnIndex(Media.ARTIST)));
            String artistKey = c.getString(c.getColumnIndex(Media.ARTIST_KEY));
            assertNotNull(artistKey);
            long artistId = c.getLong(c.getColumnIndex(Media.ARTIST_ID));
            assertTrue(artistId > 0);
            assertEquals(Audio1.COMPOSER, c.getString(c.getColumnIndex(Media.COMPOSER)));
            assertEquals(Audio1.DURATION, c.getLong(c.getColumnIndex(Media.DURATION)));
            assertEquals(Audio1.IS_ALARM, c.getInt(c.getColumnIndex(Media.IS_ALARM)));
            assertEquals(Audio1.IS_MUSIC, c.getInt(c.getColumnIndex(Media.IS_MUSIC)));
            assertEquals(Audio1.IS_NOTIFICATION, c.getInt(c.getColumnIndex(Media.IS_NOTIFICATION)));
            assertEquals(Audio1.IS_RINGTONE, c.getInt(c.getColumnIndex(Media.IS_RINGTONE)));
            assertEquals(Audio1.TRACK, c.getInt(c.getColumnIndex(Media.TRACK)));
            assertEquals(Audio1.YEAR, c.getInt(c.getColumnIndex(Media.YEAR)));
            String titleKey = c.getString(c.getColumnIndex(Media.TITLE_KEY));
            assertNotNull(titleKey);
            try {
                c.getColumnIndexOrThrow(Media.ALBUM_ART);
                fail("Should throw IllegalArgumentException because there is no column with name"
                        + " \"Media.ALBUM_ART\" in the table");
            } catch (IllegalArgumentException e) {
            }
            c.close();
            Audio2 audio2 = Audio2.getInstance();
            values = audio2.getContentValues(isInternal);
            int result = mContentResolver.update(uri, values, null, null);
            assertEquals(1, result);
            c = mContentResolver.query(uri, null, null, null, null);
            assertEquals(1, c.getCount());
            c.moveToFirst();
            expected = isInternal ? Audio2.INTERNAL_DATA : Audio2.EXTERNAL_DATA;
            assertEquals(expected, c.getString(c.getColumnIndex(Media.DATA)));
            assertEquals(Audio2.DATE_MODIFIED, c.getLong(c.getColumnIndex(Media.DATE_MODIFIED)));
            assertEquals(Audio2.DISPLAY_NAME, c.getString(c.getColumnIndex(Media.DISPLAY_NAME)));
            assertEquals(Audio2.MIME_TYPE, c.getString(c.getColumnIndex(Media.MIME_TYPE)));
            assertEquals(Audio2.SIZE, c.getInt(c.getColumnIndex(Media.SIZE)));
            assertEquals(Audio2.TITLE, c.getString(c.getColumnIndex(Media.TITLE)));
            assertEquals(Audio2.ALBUM, c.getString(c.getColumnIndex(Media.ALBUM)));
            assertFalse(albumKey.equals(c.getString(c.getColumnIndex(Media.ALBUM_KEY))));
            assertTrue(albumId !=  c.getLong(c.getColumnIndex(Media.ALBUM_ID)));
            assertEquals(Audio2.ARTIST, c.getString(c.getColumnIndex(Media.ARTIST)));
            assertFalse(artistKey.equals(c.getString(c.getColumnIndex(Media.ARTIST_KEY))));
            assertTrue(artistId !=  c.getLong(c.getColumnIndex(Media.ARTIST_ID)));
            assertEquals(Audio2.COMPOSER, c.getString(c.getColumnIndex(Media.COMPOSER)));
            assertEquals(Audio2.DURATION, c.getLong(c.getColumnIndex(Media.DURATION)));
            assertEquals(Audio2.IS_ALARM, c.getInt(c.getColumnIndex(Media.IS_ALARM)));
            assertEquals(Audio2.IS_MUSIC, c.getInt(c.getColumnIndex(Media.IS_MUSIC)));
            assertEquals(Audio2.IS_NOTIFICATION,
                    c.getInt(c.getColumnIndex(Media.IS_NOTIFICATION)));
            assertEquals(Audio2.IS_RINGTONE, c.getInt(c.getColumnIndex(Media.IS_RINGTONE)));
            assertEquals(Audio2.TRACK, c.getInt(c.getColumnIndex(Media.TRACK)));
            assertEquals(Audio2.YEAR, c.getInt(c.getColumnIndex(Media.YEAR)));
            assertTrue(titleKey.equals(c.getString(c.getColumnIndex(Media.TITLE_KEY))));
            c.close();
        } finally {
            int result = mContentResolver.delete(uri, null, null);
            assertEquals(1, result);
        }
    }
}
