public final class OSFCodeSetRegistry
{
    public static final int ISO_8859_1_VALUE = 0x00010001;
    public static final int UTF_16_VALUE = 0x00010109;
    public static final int UTF_8_VALUE = 0x05010001;
    public static final int UCS_2_VALUE = 0x00010100;
    public static final int ISO_646_VALUE = 0x00010020;
    private OSFCodeSetRegistry() {}
    public final static class Entry
    {
        private String javaName;
        private int encodingNum;
        private boolean isFixedWidth;
        private int maxBytesPerChar;
        private Entry(String javaName,
                      int encodingNum,
                      boolean isFixedWidth,
                      int maxBytesPerChar) {
            this.javaName = javaName;
            this.encodingNum = encodingNum;
            this.isFixedWidth = isFixedWidth;
            this.maxBytesPerChar = maxBytesPerChar;
        }
        public String getName() {
            return javaName;
        }
        public int getNumber() {
            return encodingNum;
        }
        public boolean isFixedWidth() {
            return isFixedWidth;
        }
        public int getMaxBytesPerChar() {
            return maxBytesPerChar;
        }
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!(obj instanceof OSFCodeSetRegistry.Entry))
                return false;
            OSFCodeSetRegistry.Entry other
                = (OSFCodeSetRegistry.Entry)obj;
            return (javaName.equals(other.javaName) &&
                    encodingNum == other.encodingNum &&
                    isFixedWidth == other.isFixedWidth &&
                    maxBytesPerChar == other.maxBytesPerChar);
        }
        public int hashCode() {
            return encodingNum;
        }
    }
    public static final Entry ISO_8859_1
        = new Entry("ISO-8859-1",
                    ISO_8859_1_VALUE,
                    true,
                    1);
    static final Entry UTF_16BE
        = new Entry("UTF-16BE",
                    -1,
                    true,
                    2);
    static final Entry UTF_16LE
        = new Entry("UTF-16LE",
                    -2,
                    true,
                    2);
    public static final Entry UTF_16
        = new Entry("UTF-16",
                    UTF_16_VALUE,
                    true,
                    4);
    public static final Entry UTF_8
        = new Entry("UTF-8",
                    UTF_8_VALUE,
                    false,
                    6);
    public static final Entry UCS_2
        = new Entry("UCS-2",
                    UCS_2_VALUE,
                    true,
                    2);
    public static final Entry ISO_646
        = new Entry("US-ASCII",
                    ISO_646_VALUE,
                    true,
                    1);
    public static Entry lookupEntry(int encodingValue) {
        switch(encodingValue) {
            case ISO_8859_1_VALUE:
                return OSFCodeSetRegistry.ISO_8859_1;
            case UTF_16_VALUE:
                return OSFCodeSetRegistry.UTF_16;
            case UTF_8_VALUE:
                return OSFCodeSetRegistry.UTF_8;
            case ISO_646_VALUE:
                return OSFCodeSetRegistry.ISO_646;
            case UCS_2_VALUE:
                return OSFCodeSetRegistry.UCS_2;
            default:
                return null;
        }
    }
}
