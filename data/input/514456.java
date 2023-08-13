public class MediaPlayerMetadataParserTest extends AndroidTestCase {
    private static final String TAG = "MediaPlayerMetadataTest";
    private static final int kMarker = 0x4d455441;  
    private static final int kHeaderSize = 8;
    private Metadata mMetadata = null;
    private Parcel mParcel = null;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mMetadata = new Metadata();
        mParcel = Parcel.obtain();
        resetParcel();
    }
    private void assertParseFail() throws Exception {
        mParcel.setDataPosition(0);
        assertFalse(mMetadata.parse(mParcel));
        assertEquals(0, mParcel.dataPosition());
    }
    private void assertParse() throws Exception {
        mParcel.setDataPosition(0);
        assertTrue(mMetadata.parse(mParcel));
    }
    private void adjustSize() {
        adjustSize(0);
    }
    private void adjustSize(int offset) {
        final int pos = mParcel.dataPosition();
        mParcel.setDataPosition(offset);
        mParcel.writeInt(pos - offset);
        mParcel.setDataPosition(pos);
    }
    private void resetParcel() {
        mParcel.setDataPosition(0);
        mParcel.writeInt(-1);  
        mParcel.writeInt(kMarker);
    }
    @SmallTest
    public void testMissingSizeAndMarker() throws Exception {
        for (int i = 0; i < kHeaderSize; ++i) {
            mParcel.setDataPosition(0);
            mParcel.setDataSize(i);
            assertEquals(i, mParcel.dataAvail());
            assertParseFail();
        }
    }
    @SmallTest
    public void testMissingData() throws Exception {
        final int size = 20;
        mParcel.writeInt(size);
        mParcel.setDataSize(size - 1);
        assertParseFail();
    }
    @SmallTest
    public void testEmptyIsOk() throws Exception {
        adjustSize();
        assertParse();
    }
    @SmallTest
    public void testRecordMissingId() throws Exception {
        mParcel.writeInt(13); 
        adjustSize();
        assertParseFail();
    }
    @SmallTest
    public void testRecordMissingType() throws Exception {
        mParcel.writeInt(13); 
        mParcel.writeInt(Metadata.TITLE);
        adjustSize();
        assertParseFail();
    }
    @SmallTest
    public void testRecordWithZeroPayload() throws Exception {
        mParcel.writeInt(0);
        adjustSize();
        assertParseFail();
    }
    @SmallTest
    public void testRecordMissingPayload() throws Exception {
        mParcel.writeInt(12);
        mParcel.writeInt(Metadata.TITLE);
        mParcel.writeInt(Metadata.STRING_VAL);
        adjustSize();
        assertParseFail();
    }
    @SmallTest
    public void testRecordsFound() throws Exception {
        writeStringRecord(Metadata.TITLE, "a title");
        writeStringRecord(Metadata.GENRE, "comedy");
        writeStringRecord(Metadata.firstCustomId(), "custom");
        adjustSize();
        assertParse();
        assertTrue(mMetadata.has(Metadata.TITLE));
        assertTrue(mMetadata.has(Metadata.GENRE));
        assertTrue(mMetadata.has(Metadata.firstCustomId()));
        assertFalse(mMetadata.has(Metadata.DRM_CRIPPLED));
        assertEquals(3, mMetadata.keySet().size());
    }
    @SmallTest
    public void testBadMetadataType() throws Exception {
        final int start = mParcel.dataPosition();
        mParcel.writeInt(-1);  
        mParcel.writeInt(Metadata.TITLE);
        mParcel.writeInt(0);  
        mParcel.writeString("dummy");
        adjustSize(start);
        adjustSize();
        assertParseFail();
    }
    @SmallTest
    public void testParseClearState() throws Exception {
        writeStringRecord(Metadata.TITLE, "a title");
        writeStringRecord(Metadata.GENRE, "comedy");
        writeStringRecord(Metadata.firstCustomId(), "custom");
        adjustSize();
        assertParse();
        resetParcel();
        writeStringRecord(Metadata.MIME_TYPE, "audio/mpg");
        adjustSize();
        assertParse();
        assertEquals(1, mMetadata.keySet().size());
        assertTrue(mMetadata.has(Metadata.MIME_TYPE));
        assertFalse(mMetadata.has(Metadata.TITLE));
        assertFalse(mMetadata.has(Metadata.GENRE));
        assertFalse(mMetadata.has(Metadata.firstCustomId()));
    }
    @SmallTest
    public void testGetString() throws Exception {
        writeStringRecord(Metadata.TITLE, "a title");
        writeStringRecord(Metadata.GENRE, "comedy");
        adjustSize();
        assertParse();
        assertEquals("a title", mMetadata.getString(Metadata.TITLE));
        assertEquals("comedy", mMetadata.getString(Metadata.GENRE));
    }
    @SmallTest
    public void testGetEmptyString() throws Exception {
        writeStringRecord(Metadata.TITLE, "");
        adjustSize();
        assertParse();
        assertEquals("", mMetadata.getString(Metadata.TITLE));
    }
    @SmallTest
    public void testGetNullString() throws Exception {
        writeStringRecord(Metadata.TITLE, null);
        adjustSize();
        assertParse();
        assertEquals(null, mMetadata.getString(Metadata.TITLE));
    }
    @SmallTest
    public void testWrongType() throws Exception {
        writeIntRecord(Metadata.DURATION, 5);
        adjustSize();
        assertParse();
        try {
            mMetadata.getString(Metadata.DURATION);
        } catch (IllegalStateException ise) {
            return;
        }
        fail("Exception was not thrown");
    }
    @SmallTest
    public void testGetInt() throws Exception {
        writeIntRecord(Metadata.CD_TRACK_NUM, 1);
        adjustSize();
        assertParse();
        assertEquals(1, mMetadata.getInt(Metadata.CD_TRACK_NUM));
    }
    @SmallTest
    public void testGetBoolean() throws Exception {
        writeBooleanRecord(Metadata.DRM_CRIPPLED, true);
        adjustSize();
        assertParse();
        assertEquals(true, mMetadata.getBoolean(Metadata.DRM_CRIPPLED));
    }
    @SmallTest
    public void testGetLong() throws Exception {
        writeLongRecord(Metadata.DURATION, 1L);
        adjustSize();
        assertParse();
        assertEquals(1L, mMetadata.getLong(Metadata.DURATION));
    }
    @SmallTest
    public void testGetDouble() throws Exception {
        writeDoubleRecord(Metadata.VIDEO_FRAME_RATE, 29.97);
        adjustSize();
        assertParse();
        assertEquals(29.97, mMetadata.getDouble(Metadata.VIDEO_FRAME_RATE));
    }
    @SmallTest
    public void testGetByteArray() throws Exception {
        byte data[] = new byte[]{1,2,3,4,5};
        writeByteArrayRecord(Metadata.ALBUM_ART, data);
        adjustSize();
        assertParse();
        byte res[] = mMetadata.getByteArray(Metadata.ALBUM_ART);
        for (int i = 0; i < data.length; ++i) {
            assertEquals(data[i], res[i]);
        }
    }
    @SmallTest
    public void testGetDate() throws Exception {
        writeDateRecord(Metadata.DATE, 0, "PST");
        adjustSize();
        assertParse();
        assertEquals(new Date(0), mMetadata.getDate(Metadata.DATE));
    }
    @SmallTest
    public void testGetTimedText() throws Exception {
        Date now = Calendar.getInstance().getTime();
        writeTimedTextRecord(Metadata.CAPTION, now.getTime(),
                             10, "Some caption");
        adjustSize();
        assertParse();
        Metadata.TimedText caption = mMetadata.getTimedText(Metadata.CAPTION);
        assertEquals("" + now + "-" + 10 + ":Some caption", caption.toString());
    }
    private void writeStringRecord(int metadataId, String val) {
        final int start = mParcel.dataPosition();
        mParcel.writeInt(-1);  
        mParcel.writeInt(metadataId);
        mParcel.writeInt(Metadata.STRING_VAL);
        mParcel.writeString(val);
        adjustSize(start);
    }
    private void writeIntRecord(int metadataId, int val) {
        final int start = mParcel.dataPosition();
        mParcel.writeInt(-1);  
        mParcel.writeInt(metadataId);
        mParcel.writeInt(Metadata.INTEGER_VAL);
        mParcel.writeInt(val);
        adjustSize(start);
    }
    private void writeBooleanRecord(int metadataId, boolean val) {
        final int start = mParcel.dataPosition();
        mParcel.writeInt(-1);  
        mParcel.writeInt(metadataId);
        mParcel.writeInt(Metadata.BOOLEAN_VAL);
        mParcel.writeInt(val ? 1 : 0);
        adjustSize(start);
    }
    private void writeLongRecord(int metadataId, long val) {
        final int start = mParcel.dataPosition();
        mParcel.writeInt(-1);  
        mParcel.writeInt(metadataId);
        mParcel.writeInt(Metadata.LONG_VAL);
        mParcel.writeLong(val);
        adjustSize(start);
    }
    private void writeDoubleRecord(int metadataId, double val) {
        final int start = mParcel.dataPosition();
        mParcel.writeInt(-1);  
        mParcel.writeInt(metadataId);
        mParcel.writeInt(Metadata.DOUBLE_VAL);
        mParcel.writeDouble(val);
        adjustSize(start);
    }
    private void writeByteArrayRecord(int metadataId, byte[] val) {
        final int start = mParcel.dataPosition();
        mParcel.writeInt(-1);  
        mParcel.writeInt(metadataId);
        mParcel.writeInt(Metadata.BYTE_ARRAY_VAL);
        mParcel.writeByteArray(val);
        adjustSize(start);
    }
    private void writeDateRecord(int metadataId, long time, String tz) {
        final int start = mParcel.dataPosition();
        mParcel.writeInt(-1);  
        mParcel.writeInt(metadataId);
        mParcel.writeInt(Metadata.DATE_VAL);
        mParcel.writeLong(time);
        mParcel.writeString(tz);
        adjustSize(start);
    }
    private void writeTimedTextRecord(int metadataId, long begin,
                                      int duration, String text) {
        final int start = mParcel.dataPosition();
        mParcel.writeInt(-1);  
        mParcel.writeInt(metadataId);
        mParcel.writeInt(Metadata.TIMED_TEXT_VAL);
        mParcel.writeLong(begin);
        mParcel.writeInt(duration);
        mParcel.writeString(text);
        adjustSize(start);
    }
}
