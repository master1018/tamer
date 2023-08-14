@TestTargetClass(Albums.class)
public class MediaStore_Audio_Artists_AlbumsTest extends InstrumentationTestCase {
    private ContentResolver mContentResolver;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mContentResolver = getInstrumentation().getContext().getContentResolver();
    }
    @TestTargetNew(
      level = TestLevel.COMPLETE,
      method = "getContentUri",
      args = {String.class, long.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "Android API javadocs are incomplete. This is no "
            + "document which describs possible values of the param volumeName.")
    public void testGetContentUri() {
        Uri contentUri = MediaStore.Audio.Artists.Albums.getContentUri(
                MediaStoreAudioTestHelper.INTERNAL_VOLUME_NAME, 1);
        assertNotNull(mContentResolver.query(contentUri, null, null, null, null));
        contentUri = MediaStore.Audio.Artists.Albums.getContentUri(
                MediaStoreAudioTestHelper.EXTERNAL_VOLUME_NAME, 1);
        assertNotNull(mContentResolver.query(contentUri, null, null, null, null));
        String volume = "fakeVolume";
        assertNull(mContentResolver.query(MediaStore.Audio.Artists.Albums.getContentUri(volume, 1),
                null, null, null, null));
    }
    @ToBeFixed(bug = "", explanation = "The result cursor of query for all columns does not "
            + "contain the column Albums.ALBUM_ID.")
    public void testStoreAudioArtistsAlbumsInternal() {
        testStoreAudioArtistsAlbums(true);
    }
    @ToBeFixed(bug = "", explanation = "The result cursor of query for all columns does not "
            + "contain the column Albums.ALBUM_ID.")
    public void testStoreAudioArtistsAlbumsExternal() {
        testStoreAudioArtistsAlbums(false);
    }
    private void testStoreAudioArtistsAlbums(boolean isInternal) {
        Uri audioMediaUri = isInternal ? Audio1.getInstance().insertToInternal(mContentResolver)
                : Audio1.getInstance().insertToExternal(mContentResolver);
        Cursor c = mContentResolver.query(audioMediaUri, new String[] { Media.ARTIST_ID }, null,
                null, null);
        c.moveToFirst();
        Long artistId = c.getLong(c.getColumnIndex(Media.ARTIST_ID));
        c.close();
        Uri artistsAlbumsUri = MediaStore.Audio.Artists.Albums.getContentUri(isInternal ?
                MediaStoreAudioTestHelper.INTERNAL_VOLUME_NAME :
                    MediaStoreAudioTestHelper.EXTERNAL_VOLUME_NAME, artistId);
        try {
            mContentResolver.insert(artistsAlbumsUri, new ContentValues());
            fail("Should throw UnsupportedOperationException!");
        } catch (UnsupportedOperationException e) {
        }
        try {
            c = mContentResolver.query(artistsAlbumsUri, null, null, null, null);
            assertEquals(1, c.getCount());
            c.moveToFirst();
            assertEquals(Audio1.ALBUM, c.getString(c.getColumnIndex(Albums.ALBUM)));
            assertNull(c.getString(c.getColumnIndex(Albums.ALBUM_ART)));
            assertNotNull(c.getString(c.getColumnIndex(Albums.ALBUM_KEY)));
            assertEquals(Audio1.ARTIST, c.getString(c.getColumnIndex(Albums.ARTIST)));
            assertEquals(Audio1.YEAR, c.getInt(c.getColumnIndex(Albums.FIRST_YEAR)));
            assertEquals(Audio1.YEAR, c.getInt(c.getColumnIndex(Albums.LAST_YEAR)));
            assertEquals(1, c.getInt(c.getColumnIndex(Albums.NUMBER_OF_SONGS)));
            assertEquals(1, c.getInt(c.getColumnIndex(Albums.NUMBER_OF_SONGS_FOR_ARTIST)));
            try {
                c.getColumnIndexOrThrow(Albums.ALBUM_ID);
                fail("Should throw IllegalArgumentException because there is no column with name"
                        + " \"Albums.ALBUM_ID\" in the table");
            } catch (IllegalArgumentException e) {
            }
            c.close();
            ContentValues albumValues = new ContentValues();
            albumValues.put(Albums.ALBUM, Audio2.ALBUM);
            try {
                mContentResolver.update(artistsAlbumsUri, albumValues, null, null);
                fail("Should throw UnsupportedOperationException!");
            } catch (UnsupportedOperationException e) {
            }
            try {
                mContentResolver.delete(artistsAlbumsUri, null, null);
                fail("Should throw UnsupportedOperationException!");
            } catch (UnsupportedOperationException e) {
            }
        } finally {
            mContentResolver.delete(audioMediaUri, null, null);
        }
        c = mContentResolver.query(artistsAlbumsUri, null, null, null, null);
        assertEquals(0, c.getCount());
        c.close();
    }
}
