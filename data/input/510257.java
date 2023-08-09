@TestTargetClass(Albums.class)
public class MediaStore_Audio_AlbumsTest extends InstrumentationTestCase {
    private ContentResolver mContentResolver;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mContentResolver = getInstrumentation().getContext().getContentResolver();
    }
    @TestTargetNew(
      level = TestLevel.SUFFICIENT,
      method = "getContentUri",
      args = {String.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "Android API javadocs are incomplete. This is no "
            + "document which describs possible values of the param volumeName.")
    public void testGetContentUri() {
        assertNotNull(mContentResolver.query(
                Albums.getContentUri(MediaStoreAudioTestHelper.INTERNAL_VOLUME_NAME), null, null,
                null, null));
        assertNotNull(mContentResolver.query(
                Albums.getContentUri(MediaStoreAudioTestHelper.EXTERNAL_VOLUME_NAME), null, null,
                null, null));
        String volume = "fakeVolume";
        assertNull(mContentResolver.query(Albums.getContentUri(volume), null, null, null, null));
    }
    @ToBeFixed(bug = "", explanation = "The result cursor of query for all columns does not "
            + "contain column Albums.ALBUM_ID and Albums.NUMBER_OF_SONGS_FOR_ARTIST.")
    public void testStoreAudioAlbumsInternal() {
        testStoreAudioAlbums(true);
    }
    @ToBeFixed(bug = "", explanation = "The result cursor of query for all columns does not "
            + "contain column Albums.ALBUM_ID and Albums.NUMBER_OF_SONGS_FOR_ARTIST.")
    public void testStoreAudioAlbumsExternal() {
        testStoreAudioAlbums(false);
    }
    private void testStoreAudioAlbums(boolean isInternal) {
        Uri audioAlbumsUri = isInternal? Albums.INTERNAL_CONTENT_URI : Albums.EXTERNAL_CONTENT_URI;
        try {
            mContentResolver.insert(audioAlbumsUri, new ContentValues());
            fail("Should throw UnsupportedOperationException!");
        } catch (UnsupportedOperationException e) {
        }
        Audio1 audio1 = Audio1.getInstance();
        Uri audioMediaUri = isInternal ? audio1.insertToInternal(mContentResolver)
                : audio1.insertToExternal(mContentResolver);
        String selection = Albums.ALBUM +"=?";
        String[] selectionArgs = new String[] { Audio1.ALBUM };
        try {
            Cursor c = mContentResolver.query(audioAlbumsUri, null, selection, selectionArgs,
                    null);
            assertEquals(1, c.getCount());
            c.moveToFirst();
            assertTrue(c.getLong(c.getColumnIndex(Albums._ID)) > 0);
            assertEquals(Audio1.ALBUM, c.getString(c.getColumnIndex(Albums.ALBUM)));
            assertNull(c.getString(c.getColumnIndex(Albums.ALBUM_ART)));
            assertNotNull(c.getString(c.getColumnIndex(Albums.ALBUM_KEY)));
            assertEquals(Audio1.ARTIST, c.getString(c.getColumnIndex(Albums.ARTIST)));
            assertEquals(Audio1.YEAR, c.getInt(c.getColumnIndex(Albums.FIRST_YEAR)));
            assertEquals(Audio1.YEAR, c.getInt(c.getColumnIndex(Albums.LAST_YEAR)));
            assertEquals(1, c.getInt(c.getColumnIndex(Albums.NUMBER_OF_SONGS)));
            try {
                c.getColumnIndexOrThrow(Albums.ALBUM_ID);
                fail("Should throw IllegalArgumentException because there is no column with name "
                        + "\"Albums.ALBUM_ID\" in the table");
            } catch (IllegalArgumentException e) {
            }
            try {
                c.getColumnIndexOrThrow(Albums.NUMBER_OF_SONGS_FOR_ARTIST);
                fail("Should throw IllegalArgumentException because there is no column with name "
                        + "\"Albums.NUMBER_OF_SONGS_FOR_ARTIST\" in the table");
            } catch (IllegalArgumentException e) {
            }
            c.close();
            ContentValues albumValues = new ContentValues();
            albumValues.put(Albums.ALBUM, Audio2.ALBUM);
            try {
                mContentResolver.update(audioAlbumsUri, albumValues, selection, selectionArgs);
                fail("Should throw UnsupportedOperationException!");
            } catch (UnsupportedOperationException e) {
            }
            try {
                mContentResolver.delete(audioAlbumsUri, selection, selectionArgs);
                fail("Should throw UnsupportedOperationException!");
            } catch (UnsupportedOperationException e) {
            }
        } finally {
            mContentResolver.delete(audioMediaUri, null, null);
        }
        Cursor c = mContentResolver.query(audioAlbumsUri, null, selection, selectionArgs, null);
        assertEquals(0, c.getCount());
        c.close();
    }
}
