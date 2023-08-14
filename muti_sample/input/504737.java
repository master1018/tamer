public class ExifInterface {
    public static final String TAG_ORIENTATION = "Orientation";
    public static final String TAG_DATETIME = "DateTime";
    public static final String TAG_MAKE = "Make";
    public static final String TAG_MODEL = "Model";
    public static final String TAG_FLASH = "Flash";
    public static final String TAG_IMAGE_WIDTH = "ImageWidth";
    public static final String TAG_IMAGE_LENGTH = "ImageLength";
    public static final String TAG_GPS_LATITUDE = "GPSLatitude";
    public static final String TAG_GPS_LONGITUDE = "GPSLongitude";
    public static final String TAG_GPS_LATITUDE_REF = "GPSLatitudeRef";
    public static final String TAG_GPS_LONGITUDE_REF = "GPSLongitudeRef";
    public static final String TAG_GPS_TIMESTAMP = "GPSTimeStamp";
    public static final String TAG_GPS_DATESTAMP = "GPSDateStamp";
    public static final String TAG_WHITE_BALANCE = "WhiteBalance";
    public static final String TAG_FOCAL_LENGTH = "FocalLength";
    public static final String TAG_GPS_PROCESSING_METHOD = "GPSProcessingMethod";
    public static final int ORIENTATION_UNDEFINED = 0;
    public static final int ORIENTATION_NORMAL = 1;
    public static final int ORIENTATION_FLIP_HORIZONTAL = 2;  
    public static final int ORIENTATION_ROTATE_180 = 3;
    public static final int ORIENTATION_FLIP_VERTICAL = 4;  
    public static final int ORIENTATION_TRANSPOSE = 5;  
    public static final int ORIENTATION_ROTATE_90 = 6;  
    public static final int ORIENTATION_TRANSVERSE = 7;  
    public static final int ORIENTATION_ROTATE_270 = 8;  
    public static final int WHITEBALANCE_AUTO = 0;
    public static final int WHITEBALANCE_MANUAL = 1;
    private static SimpleDateFormat sFormatter;
    static {
        System.loadLibrary("exif");
        sFormatter = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
        sFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
    }
    private String mFilename;
    private HashMap<String, String> mAttributes;
    private boolean mHasThumbnail;
    private static Object sLock = new Object();
    public ExifInterface(String filename) throws IOException {
        mFilename = filename;
        loadAttributes();
    }
    public String getAttribute(String tag) {
        return mAttributes.get(tag);
    }
    public int getAttributeInt(String tag, int defaultValue) {
        String value = mAttributes.get(tag);
        if (value == null) return defaultValue;
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }
    public double getAttributeDouble(String tag, double defaultValue) {
        String value = mAttributes.get(tag);
        if (value == null) return defaultValue;
        try {
            int index = value.indexOf("/");
            if (index == -1) return defaultValue;
            double denom = Double.parseDouble(value.substring(index + 1));
            if (denom == 0) return defaultValue;
            double num = Double.parseDouble(value.substring(0, index));
            return num / denom;
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }
    public void setAttribute(String tag, String value) {
        mAttributes.put(tag, value);
    }
    private void loadAttributes() throws IOException {
        mAttributes = new HashMap<String, String>();
        String attrStr;
        synchronized (sLock) {
            attrStr = getAttributesNative(mFilename);
        }
        int ptr = attrStr.indexOf(' ');
        int count = Integer.parseInt(attrStr.substring(0, ptr));
        ++ptr;
        for (int i = 0; i < count; i++) {
            int equalPos = attrStr.indexOf('=', ptr);
            String attrName = attrStr.substring(ptr, equalPos);
            ptr = equalPos + 1;     
            int lenPos = attrStr.indexOf(' ', ptr);
            int attrLen = Integer.parseInt(attrStr.substring(ptr, lenPos));
            ptr = lenPos + 1;       
            String attrValue = attrStr.substring(ptr, ptr + attrLen);
            ptr += attrLen;
            if (attrName.equals("hasThumbnail")) {
                mHasThumbnail = attrValue.equalsIgnoreCase("true");
            } else {
                mAttributes.put(attrName, attrValue);
            }
        }
    }
    public void saveAttributes() throws IOException {
        StringBuilder sb = new StringBuilder();
        int size = mAttributes.size();
        if (mAttributes.containsKey("hasThumbnail")) {
            --size;
        }
        sb.append(size + " ");
        for (Map.Entry<String, String> iter : mAttributes.entrySet()) {
            String key = iter.getKey();
            if (key.equals("hasThumbnail")) {
                continue;
            }
            String val = iter.getValue();
            sb.append(key + "=");
            sb.append(val.length() + " ");
            sb.append(val);
        }
        String s = sb.toString();
        synchronized (sLock) {
            saveAttributesNative(mFilename, s);
            commitChangesNative(mFilename);
        }
    }
    public boolean hasThumbnail() {
        return mHasThumbnail;
    }
    public byte[] getThumbnail() {
        synchronized (sLock) {
            return getThumbnailNative(mFilename);
        }
    }
    public boolean getLatLong(float output[]) {
        String latValue = mAttributes.get(ExifInterface.TAG_GPS_LATITUDE);
        String latRef = mAttributes.get(ExifInterface.TAG_GPS_LATITUDE_REF);
        String lngValue = mAttributes.get(ExifInterface.TAG_GPS_LONGITUDE);
        String lngRef = mAttributes.get(ExifInterface.TAG_GPS_LONGITUDE_REF);
        if (latValue != null && latRef != null && lngValue != null && lngRef != null) {
            output[0] = convertRationalLatLonToFloat(latValue, latRef);
            output[1] = convertRationalLatLonToFloat(lngValue, lngRef);
            return true;
        } else {
            return false;
        }
    }
    public long getDateTime() {
        String dateTimeString = mAttributes.get(TAG_DATETIME);
        if (dateTimeString == null) return -1;
        ParsePosition pos = new ParsePosition(0);
        try {
            Date datetime = sFormatter.parse(dateTimeString, pos);
            if (datetime == null) return -1;
            return datetime.getTime();
        } catch (IllegalArgumentException ex) {
            return -1;
        }
    }
    public long getGpsDateTime() {
        String date = mAttributes.get(TAG_GPS_DATESTAMP);
        String time = mAttributes.get(TAG_GPS_TIMESTAMP);
        if (date == null || time == null) return -1;
        String dateTimeString = date + ' ' + time;
        if (dateTimeString == null) return -1;
        ParsePosition pos = new ParsePosition(0);
        try {
            Date datetime = sFormatter.parse(dateTimeString, pos);
            if (datetime == null) return -1;
            return datetime.getTime();
        } catch (IllegalArgumentException ex) {
            return -1;
        }
    }
    private static float convertRationalLatLonToFloat(
            String rationalString, String ref) {
        try {
            String [] parts = rationalString.split(",");
            String [] pair;
            pair = parts[0].split("/");
            int degrees = (int) (Float.parseFloat(pair[0].trim())
                    / Float.parseFloat(pair[1].trim()));
            pair = parts[1].split("/");
            int minutes = (int) ((Float.parseFloat(pair[0].trim())
                    / Float.parseFloat(pair[1].trim())));
            pair = parts[2].split("/");
            float seconds = Float.parseFloat(pair[0].trim())
                    / Float.parseFloat(pair[1].trim());
            float result = degrees + (minutes / 60F) + (seconds / (60F * 60F));
            if ((ref.equals("S") || ref.equals("W"))) {
                return -result;
            }
            return result;
        } catch (RuntimeException ex) {
            return 0f;
        }
    }
    private native boolean appendThumbnailNative(String fileName,
            String thumbnailFileName);
    private native void saveAttributesNative(String fileName,
            String compressedAttributes);
    private native String getAttributesNative(String fileName);
    private native void commitChangesNative(String fileName);
    private native byte[] getThumbnailNative(String fileName);
}
