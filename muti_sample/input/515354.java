public class PackedString {
    private static final char DELIMITER_ELEMENT = '\1';
    private static final char DELIMITER_TAG = '\2';
    private String mString;
    private HashMap<String, String> mExploded;
    private static final HashMap<String, String> EMPTY_MAP = new HashMap<String, String>();
    public PackedString(String string) {
        mString = string;
        mExploded = null;
    }
    public String get(String tag) {
        if (mExploded == null) {
            mExploded = explode(mString);
        }
        return mExploded.get(tag);
    }
    public Map<String, String> unpack() {
        if (mExploded == null) {
            mExploded = explode(mString);
        }
        return new HashMap<String,String>(mExploded);
    }
    private static HashMap<String, String> explode(String packed) {
        if (packed == null || packed.length() == 0) {
            return EMPTY_MAP;
        }
        HashMap<String, String> map = new HashMap<String, String>();
        int length = packed.length();
        int elementStartIndex = 0;
        int elementEndIndex = 0;
        int tagEndIndex = packed.indexOf(DELIMITER_TAG);
        while (elementStartIndex < length) {
            elementEndIndex = packed.indexOf(DELIMITER_ELEMENT, elementStartIndex);
            if (elementEndIndex == -1) {
                elementEndIndex = length;
            }
            String tag;
            String value;
            if (tagEndIndex == -1 || elementEndIndex <= tagEndIndex) {
                value = packed.substring(elementStartIndex, elementEndIndex);
                tag = Integer.toString(map.size());
            } else {
                value = packed.substring(elementStartIndex, tagEndIndex);
                tag = packed.substring(tagEndIndex + 1, elementEndIndex);
                tagEndIndex = packed.indexOf(DELIMITER_TAG, elementEndIndex + 1);
            }
            map.put(tag, value);
            elementStartIndex = elementEndIndex + 1;
        }
        return map;
    }
    static public class Builder {
        HashMap<String, String> mMap;
        public Builder() {
            mMap = new HashMap<String, String>();
        }
        public Builder(String packed) {
            mMap = explode(packed);
        }
        public void put(String tag, String value) {
            if (value == null) {
                mMap.remove(tag);
            } else {
                mMap.put(tag, value);
            }
        }
        public String get(String tag) {
            return mMap.get(tag);
        }
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String,String> entry : mMap.entrySet()) {
                if (sb.length() > 0) {
                    sb.append(DELIMITER_ELEMENT);
                }
                sb.append(entry.getValue());
                sb.append(DELIMITER_TAG);
                sb.append(entry.getKey());
            }
            return sb.toString();
        }
    }
}
