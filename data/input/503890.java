@TestTargetClass(Members.class)
public class MediaStore_Audio_Genres_MembersTest extends InstrumentationTestCase {
    private ContentResolver mContentResolver;
    private long mAudioIdOfJam;
    private long mAudioIdOfJamLive;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mContentResolver = getInstrumentation().getContext().getContentResolver();
        Uri uri = Audio1.getInstance().insertToExternal(mContentResolver);
        Cursor c = mContentResolver.query(uri, null, null, null, null);
        c.moveToFirst();
        mAudioIdOfJam = c.getLong(c.getColumnIndex(Media._ID));
        c.close();
        uri = Audio2.getInstance().insertToExternal(mContentResolver);
        c = mContentResolver.query(uri, null, null, null, null);
        c.moveToFirst();
        mAudioIdOfJamLive = c.getLong(c.getColumnIndex(Media._ID));
        c.close();
    }
    @Override
    protected void tearDown() throws Exception {
        mContentResolver.delete(Media.EXTERNAL_CONTENT_URI, Media._ID + "=" + mAudioIdOfJam, null);
        mContentResolver.delete(Media.EXTERNAL_CONTENT_URI, Media._ID + "=" + mAudioIdOfJamLive,
                null);
        super.tearDown();
    }
    @TestTargetNew(
      level = TestLevel.COMPLETE,
      method = "getContentUri",
      args = {String.class, long.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "Android API javadocs are incomplete. There is no "
            + "document related to the possible values of param volumeName. @throw clause "
            + "should be added in to javadoc when getting uri for internal volume.")
    public void testGetContentUri() {
        assertNotNull(mContentResolver.query(
                Members.getContentUri(MediaStoreAudioTestHelper.EXTERNAL_VOLUME_NAME, 1), null,
                    null, null, null));
        try {
            assertNotNull(mContentResolver.query(
                    Members.getContentUri(MediaStoreAudioTestHelper.INTERNAL_VOLUME_NAME, 1), null,
                        null, null, null));
            fail("Should throw SQLException as the internal datatbase has no genre");
        } catch (SQLException e) {
        }
        String volume = "fakeVolume";
        assertNull(mContentResolver.query(Members.getContentUri(volume, 1), null, null, null,
                null));
    }
    @ToBeFixed(bug = "", explanation = "The result cursor of query for all columns does not "
            + "contain the column Members.ALBUM_ART, Members.GENRE_ID and  Members.AUDIO_ID.")
    public void testStoreAudioGenresMembersExternal() {
        ContentValues values = new ContentValues();
        values.put(Genres.NAME, Audio1.GENRE);
        Uri uri = mContentResolver.insert(Genres.EXTERNAL_CONTENT_URI, values);
        Cursor c = mContentResolver.query(uri, null, null, null, null);
        c.moveToFirst();
        long genreId = c.getLong(c.getColumnIndex(Genres._ID));
        c.close();
        values.clear();
        values.put(Members.AUDIO_ID, mAudioIdOfJam);
        Uri membersUri = Members.getContentUri(MediaStoreAudioTestHelper.EXTERNAL_VOLUME_NAME,
                genreId);
        assertNotNull(mContentResolver.insert(membersUri, values));
        try {
            c = mContentResolver.query(membersUri, null, null, null, null);
            try {
                c.getColumnIndexOrThrow(Members.ALBUM_ART);
                fail("Should throw IllegalArgumentException because there is no column with name"
                        + " \"Members.ALBUM_ART\" in the table");
            } catch (IllegalArgumentException e) {
            }
            try {
                c.getColumnIndexOrThrow(Members.AUDIO_ID);
                fail("Should throw IllegalArgumentException because there is no column with name"
                        + " \"Members.AUDIO_ID\" in the table");
            } catch (IllegalArgumentException e) {
            }
            try {
                c.getColumnIndexOrThrow(Members.GENRE_ID);
                fail("Should throw IllegalArgumentException because there is no column with name"
                        + " \"Members.GENRE_ID\" in the table");
            } catch (IllegalArgumentException e) {
            }
            assertEquals(1, c.getCount());
            c.moveToFirst();
            assertTrue(c.getLong(c.getColumnIndex(Members._ID)) > 0);
            assertEquals(Audio1.EXTERNAL_DATA, c.getString(c.getColumnIndex(Members.DATA)));
            assertTrue(c.getLong(c.getColumnIndex(Members.DATE_ADDED)) > 0);
            assertEquals(Audio1.DATE_MODIFIED, c.getLong(c.getColumnIndex(Members.DATE_MODIFIED)));
            assertEquals(Audio1.FILE_NAME, c.getString(c.getColumnIndex(Members.DISPLAY_NAME)));
            assertEquals(Audio1.MIME_TYPE, c.getString(c.getColumnIndex(Members.MIME_TYPE)));
            assertEquals(Audio1.SIZE, c.getInt(c.getColumnIndex(Members.SIZE)));
            assertEquals(Audio1.TITLE, c.getString(c.getColumnIndex(Members.TITLE)));
            assertEquals(Audio1.ALBUM, c.getString(c.getColumnIndex(Members.ALBUM)));
            String albumKey = c.getString(c.getColumnIndex(Members.ALBUM_KEY));
            assertNotNull(albumKey);
            long albumId = c.getLong(c.getColumnIndex(Members.ALBUM_ID));
            assertTrue(albumId > 0);
            assertEquals(Audio1.ARTIST, c.getString(c.getColumnIndex(Members.ARTIST)));
            String artistKey = c.getString(c.getColumnIndex(Members.ARTIST_KEY));
            assertNotNull(artistKey);
            long artistId = c.getLong(c.getColumnIndex(Members.ARTIST_ID));
            assertTrue(artistId > 0);
            assertEquals(Audio1.COMPOSER, c.getString(c.getColumnIndex(Members.COMPOSER)));
            assertEquals(Audio1.DURATION, c.getLong(c.getColumnIndex(Members.DURATION)));
            assertEquals(Audio1.IS_ALARM, c.getInt(c.getColumnIndex(Members.IS_ALARM)));
            assertEquals(Audio1.IS_MUSIC, c.getInt(c.getColumnIndex(Members.IS_MUSIC)));
            assertEquals(Audio1.IS_NOTIFICATION,
                    c.getInt(c.getColumnIndex(Members.IS_NOTIFICATION)));
            assertEquals(Audio1.IS_RINGTONE, c.getInt(c.getColumnIndex(Members.IS_RINGTONE)));
            assertEquals(Audio1.TRACK, c.getInt(c.getColumnIndex(Members.TRACK)));
            assertEquals(Audio1.YEAR, c.getInt(c.getColumnIndex(Members.YEAR)));
            String titleKey = c.getString(c.getColumnIndex(Members.TITLE_KEY));
            assertNotNull(titleKey);
            c.close();
            values.clear();
            values.put(Members.AUDIO_ID, mAudioIdOfJamLive);
            try {
                mContentResolver.update(membersUri, values, null, null);
                fail("Should throw SQLException because there is no column with name "
                        + "\"Members.AUDIO_ID\" in the table");
            } catch (SQLException e) {
            }
            try {
                mContentResolver.delete(membersUri, null, null);
                fail("Should throw SQLException because there is no column with name "
                        + "\"Members.GENRE_ID\" in the table");
            } catch (SQLException e) {
            }
        } finally {
            mContentResolver.delete(Genres.EXTERNAL_CONTENT_URI, Genres._ID + "=" + genreId, null);
            c = mContentResolver.query(membersUri, null, null, null, null);
            assertEquals(0, c.getCount());
            c.close();
        }
    }
    public void testStoreAudioGenresMembersInternal() {
        ContentValues values = new ContentValues();
        values.put(Genres.NAME, Audio1.GENRE);
        Uri uri = mContentResolver.insert(Genres.INTERNAL_CONTENT_URI, values);
        assertNull(uri);
    }
}
