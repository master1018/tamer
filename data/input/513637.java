public class Metadata
{
    public static final int ANY = 0;  
    public static final int TITLE = 1;           
    public static final int COMMENT = 2;         
    public static final int COPYRIGHT = 3;       
    public static final int ALBUM = 4;           
    public static final int ARTIST = 5;          
    public static final int AUTHOR = 6;          
    public static final int COMPOSER = 7;        
    public static final int GENRE = 8;           
    public static final int DATE = 9;            
    public static final int DURATION = 10;       
    public static final int CD_TRACK_NUM = 11;   
    public static final int CD_TRACK_MAX = 12;   
    public static final int RATING = 13;         
    public static final int ALBUM_ART = 14;      
    public static final int VIDEO_FRAME = 15;    
    public static final int CAPTION = 16;        
    public static final int BIT_RATE = 17;       
    public static final int AUDIO_BIT_RATE = 18; 
    public static final int VIDEO_BIT_RATE = 19; 
    public static final int AUDIO_SAMPLE_RATE = 20; 
    public static final int VIDEO_FRAME_RATE = 21;  
    public static final int MIME_TYPE = 22;      
    public static final int AUDIO_CODEC = 23;    
    public static final int VIDEO_CODEC = 24;    
    public static final int VIDEO_HEIGHT = 25;   
    public static final int VIDEO_WIDTH = 26;    
    public static final int NUM_TRACKS = 27;     
    public static final int DRM_CRIPPLED = 28;   
    public static final int PAUSE_AVAILABLE = 29;         
    public static final int SEEK_BACKWARD_AVAILABLE = 30; 
    public static final int SEEK_FORWARD_AVAILABLE = 31;  
    private static final int LAST_SYSTEM = 31;
    private static final int FIRST_CUSTOM = 8192;
    public static final Set<Integer> MATCH_NONE = Collections.EMPTY_SET;
    public static final Set<Integer> MATCH_ALL = Collections.singleton(ANY);
    public static final int STRING_VAL     = 1;
    public static final int INTEGER_VAL    = 2;
    public static final int BOOLEAN_VAL    = 3;
    public static final int LONG_VAL       = 4;
    public static final int DOUBLE_VAL     = 5;
    public static final int TIMED_TEXT_VAL = 6;
    public static final int DATE_VAL       = 7;
    public static final int BYTE_ARRAY_VAL = 8;
    private static final int LAST_TYPE = 8;
    private static final String TAG = "media.Metadata";
    private static final int kInt32Size = 4;
    private static final int kMetaHeaderSize = 2 * kInt32Size; 
    private static final int kRecordHeaderSize = 3 * kInt32Size; 
    private static final int kMetaMarker = 0x4d455441;  
    private Parcel mParcel;
    private final HashMap<Integer, Integer> mKeyToPosMap =
            new HashMap<Integer, Integer>();
    public class TimedText {
        private Date mTime;
        private int mDuration;  
        private String mText;
        public TimedText(Date time, int duration, String text) {
            mTime = time;
            mDuration = duration;
            mText = text;
        }
        public String toString() {
            StringBuilder res = new StringBuilder(80);
            res.append(mTime).append("-").append(mDuration)
                    .append(":").append(mText);
            return res.toString();
        }
    }
    public Metadata() { }
    private boolean scanAllRecords(Parcel parcel, int bytesLeft) {
        int recCount = 0;
        boolean error = false;
        mKeyToPosMap.clear();
        while (bytesLeft > kRecordHeaderSize) {
            final int start = parcel.dataPosition();
            final int size = parcel.readInt();
            if (size <= kRecordHeaderSize) {  
                Log.e(TAG, "Record is too short");
                error = true;
                break;
            }
            final int metadataId = parcel.readInt();
            if (!checkMetadataId(metadataId)) {
                error = true;
                break;
            }
            if (mKeyToPosMap.containsKey(metadataId)) {
                Log.e(TAG, "Duplicate metadata ID found");
                error = true;
                break;
            }
            mKeyToPosMap.put(metadataId, parcel.dataPosition());
            final int metadataType = parcel.readInt();
            if (metadataType <= 0 || metadataType > LAST_TYPE) {
                Log.e(TAG, "Invalid metadata type " + metadataType);
                error = true;
                break;
            }
            parcel.setDataPosition(start + size);
            bytesLeft -= size;
            ++recCount;
        }
        if (0 != bytesLeft || error) {
            Log.e(TAG, "Ran out of data or error on record " + recCount);
            mKeyToPosMap.clear();
            return false;
        } else {
            return true;
        }
    }
    public boolean parse(Parcel parcel) {
        if (parcel.dataAvail() < kMetaHeaderSize) {
            Log.e(TAG, "Not enough data " + parcel.dataAvail());
            return false;
        }
        final int pin = parcel.dataPosition();  
        final int size = parcel.readInt();
        if (parcel.dataAvail() + kInt32Size < size || size < kMetaHeaderSize) {
            Log.e(TAG, "Bad size " + size + " avail " + parcel.dataAvail() + " position " + pin);
            parcel.setDataPosition(pin);
            return false;
        }
        final int kShouldBeMetaMarker = parcel.readInt();
        if (kShouldBeMetaMarker != kMetaMarker ) {
            Log.e(TAG, "Marker missing " + Integer.toHexString(kShouldBeMetaMarker));
            parcel.setDataPosition(pin);
            return false;
        }
        if (!scanAllRecords(parcel, size - kMetaHeaderSize)) {
            parcel.setDataPosition(pin);
            return false;
        }
        mParcel = parcel;
        return true;
    }
    public Set<Integer> keySet() {
        return mKeyToPosMap.keySet();
    }
    public boolean has(final int metadataId) {
        if (!checkMetadataId(metadataId)) {
            throw new IllegalArgumentException("Invalid key: " + metadataId);
        }
        return mKeyToPosMap.containsKey(metadataId);
    }
    public String getString(final int key) {
        checkType(key, STRING_VAL);
        return mParcel.readString();
    }
    public int getInt(final int key) {
        checkType(key, INTEGER_VAL);
        return mParcel.readInt();
    }
    public boolean getBoolean(final int key) {
        checkType(key, BOOLEAN_VAL);
        return mParcel.readInt() == 1;
    }
    public long getLong(final int key) {
        checkType(key, LONG_VAL);
        return mParcel.readLong();
    }
    public double getDouble(final int key) {
        checkType(key, DOUBLE_VAL);
        return mParcel.readDouble();
    }
    public byte[] getByteArray(final int key) {
        checkType(key, BYTE_ARRAY_VAL);
        return mParcel.createByteArray();
    }
    public Date getDate(final int key) {
        checkType(key, DATE_VAL);
        final long timeSinceEpoch = mParcel.readLong();
        final String timeZone = mParcel.readString();
        if (timeZone.length() == 0) {
            return new Date(timeSinceEpoch);
        } else {
            TimeZone tz = TimeZone.getTimeZone(timeZone);
            Calendar cal = Calendar.getInstance(tz);
            cal.setTimeInMillis(timeSinceEpoch);
            return cal.getTime();
        }
    }
    public TimedText getTimedText(final int key) {
        checkType(key, TIMED_TEXT_VAL);
        final Date startTime = new Date(mParcel.readLong());  
        final int duration = mParcel.readInt();  
        return new TimedText(startTime,
                             duration,
                             mParcel.readString());
    }
    public static int lastSytemId() { return LAST_SYSTEM; }
    public static int firstCustomId() { return FIRST_CUSTOM; }
    public static int lastType() { return LAST_TYPE; }
    private boolean checkMetadataId(final int val) {
        if (val <= ANY || (LAST_SYSTEM < val && val < FIRST_CUSTOM)) {
            Log.e(TAG, "Invalid metadata ID " + val);
            return false;
        }
        return true;
    }
    private void checkType(final int key, final int expectedType) {
        final int pos = mKeyToPosMap.get(key);
        mParcel.setDataPosition(pos);
        final int type = mParcel.readInt();
        if (type != expectedType) {
            throw new IllegalStateException("Wrong type " + expectedType + " but got " + type);
        }
    }
}
