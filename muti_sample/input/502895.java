public final class ApplicationParameter {
    private byte[] mArray;
    private int mLength;
    private int mMaxLength = 1000;
    public static class TRIPLET_TAGID {
        public static final byte ORDER_TAGID = 0x01;
        public static final byte SEARCH_VALUE_TAGID = 0x02;
        public static final byte SEARCH_ATTRIBUTE_TAGID = 0x03;
        public static final byte MAXLISTCOUNT_TAGID = 0x04;
        public static final byte LISTSTARTOFFSET_TAGID = 0x05;
        public static final byte FILTER_TAGID = 0x06;
        public static final byte FORMAT_TAGID = 0x07;
        public static final byte PHONEBOOKSIZE_TAGID = 0x08;
        public static final byte NEWMISSEDCALLS_TAGID = 0x09;
    }
    public static class TRIPLET_VALUE {
        public static class ORDER {
            public static final byte ORDER_BY_INDEX = 0x00;
            public static final byte ORDER_BY_ALPHANUMERIC = 0x01;
            public static final byte ORDER_BY_PHONETIC = 0x02;
        }
        public static class SEARCHATTRIBUTE {
            public static final byte SEARCH_BY_NAME = 0x00;
            public static final byte SEARCH_BY_NUMBER = 0x01;
            public static final byte SEARCH_BY_SOUND = 0x02;
        }
        public static class FORMAT {
            public static final byte VCARD_VERSION_21 = 0x00;
            public static final byte VCARD_VERSION_30 = 0x01;
        }
    }
    public static class TRIPLET_LENGTH {
        public static final byte ORDER_LENGTH = 1;
        public static final byte SEARCH_ATTRIBUTE_LENGTH = 1;
        public static final byte MAXLISTCOUNT_LENGTH = 2;
        public static final byte LISTSTARTOFFSET_LENGTH = 2;
        public static final byte FILTER_LENGTH = 8;
        public static final byte FORMAT_LENGTH = 1;
        public static final byte PHONEBOOKSIZE_LENGTH = 2;
        public static final byte NEWMISSEDCALLS_LENGTH = 1;
    }
    public ApplicationParameter() {
        mArray = new byte[mMaxLength];
        mLength = 0;
    }
    public void addAPPHeader(byte tag, byte len, byte[] value) {
        if ((mLength + len + 2) > mMaxLength) {
            byte[] array_tmp = new byte[mLength + 4 * len];
            System.arraycopy(mArray, 0, array_tmp, 0, mLength);
            mArray = array_tmp;
            mMaxLength = mLength + 4 * len;
        }
        mArray[mLength++] = tag;
        mArray[mLength++] = len;
        System.arraycopy(value, 0, mArray, mLength, len);
        mLength += len;
    }
    public byte[] getAPPparam() {
        byte[] para = new byte[mLength];
        System.arraycopy(mArray, 0, para, 0, mLength);
        return para;
    }
}
