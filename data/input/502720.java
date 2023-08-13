@TestTargetClass(Artists.class)
public class MediaStore_Audio_ArtistsTest extends InstrumentationTestCase {
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
    @ToBeFixed(bug = "1695243", explanation = "Android API javadocs are incomplete. This is no "
            + "document which describs possible values of the param volumeName.")
    public void testGetContentUri() {
        assertNotNull(mContentResolver.query(
                Artists.getContentUri(MediaStoreAudioTestHelper.INTERNAL_VOLUME_NAME), null, null,
                    null, null));
        assertNotNull(mContentResolver.query(
                Artists.getContentUri(MediaStoreAudioTestHelper.INTERNAL_VOLUME_NAME), null, null,
                null, null));
        String volume = "fakeVolume";
        assertNull(mContentResolver.query(Artists.getContentUri(volume), null, null, null, null));
    }
    public void testStoreAudioArtistsInternal() {
        testStoreAudioArtists(true);
    }
    public void testStoreAudioArtistsExternal() {
        testStoreAudioArtists(false);
    }
    private void testStoreAudioArtists(boolean isInternal) {
        Uri artistsUri = isInternal ? Artists.INTERNAL_CONTENT_URI : Artists.EXTERNAL_CONTENT_URI;
        try {
            mContentResolver.insert(artistsUri, new ContentValues());
            fail("Should throw UnsupportedOperationException!");
        } catch (UnsupportedOperationException e) {
        }
        Uri uri = isInternal ? Audio1.getInstance().insertToInternal(mContentResolver)
                : Audio1.getInstance().insertToExternal(mContentResolver);
        String selection = Artists.ARTIST + "=?";
        String[] selectionArgs = new String[] { Audio1.ARTIST };
        try {
            Cursor c = mContentResolver.query(artistsUri, null, selection, selectionArgs, null);
            assertEquals(1, c.getCount());
            c.moveToFirst();
            assertEquals(Audio1.ARTIST, c.getString(c.getColumnIndex(Artists.ARTIST)));
            assertTrue(c.getLong(c.getColumnIndex(Artists._ID)) > 0);
            assertNotNull(c.getString(c.getColumnIndex(Artists.ARTIST_KEY)));
            assertEquals(1, c.getInt(c.getColumnIndex(Artists.NUMBER_OF_ALBUMS)));
            assertEquals(1, c.getInt(c.getColumnIndex(Artists.NUMBER_OF_TRACKS)));
            c.close();
            ContentValues artistValues = new ContentValues();
            artistValues.put(Artists.ARTIST, Audio2.ALBUM);
            try {
                mContentResolver.update(artistsUri, artistValues, selection, selectionArgs);
                fail("Should throw UnsupportedOperationException!");
            } catch (UnsupportedOperationException e) {
            }
            try {
                mContentResolver.delete(artistsUri, selection, selectionArgs);
                fail("Should throw UnsupportedOperationException!");
            } catch (UnsupportedOperationException e) {
            }
        } finally {
            mContentResolver.delete(uri, null, null);
        }
        Cursor c = mContentResolver.query(artistsUri, null, selection, selectionArgs, null);
        assertEquals(0, c.getCount());
        c.close();
    }
}
