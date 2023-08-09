public final class HeaderSet {
    public static final int COUNT = 0xC0;
    public static final int NAME = 0x01;
    public static final int TYPE = 0x42;
    public static final int LENGTH = 0xC3;
    public static final int TIME_ISO_8601 = 0x44;
    public static final int TIME_4_BYTE = 0xC4;
    public static final int DESCRIPTION = 0x05;
    public static final int TARGET = 0x46;
    public static final int HTTP = 0x47;
    public static final int BODY = 0x48;
    public static final int END_OF_BODY = 0x49;
    public static final int WHO = 0x4A;
    public static final int CONNECTION_ID = 0xCB;
    public static final int APPLICATION_PARAMETER = 0x4C;
    public static final int AUTH_CHALLENGE = 0x4D;
    public static final int AUTH_RESPONSE = 0x4E;
    public static final int OBJECT_CLASS = 0x4F;
    private Long mCount; 
    private String mName; 
    private String mType; 
    private Long mLength; 
    private Calendar mIsoTime; 
    private Calendar mByteTime; 
    private String mDescription; 
    private byte[] mTarget; 
    private byte[] mHttpHeader; 
    private byte[] mWho; 
    private byte[] mAppParam; 
    private byte[] mObjectClass; 
    private String[] mUnicodeUserDefined; 
    private byte[][] mSequenceUserDefined; 
    private Byte[] mByteUserDefined; 
    private Long[] mIntegerUserDefined; 
    private final SecureRandom mRandom;
     byte[] nonce;
    public byte[] mAuthChall; 
    public byte[] mAuthResp; 
    public byte[] mConnectionID; 
    public int responseCode;
    public HeaderSet() {
        mUnicodeUserDefined = new String[16];
        mSequenceUserDefined = new byte[16][];
        mByteUserDefined = new Byte[16];
        mIntegerUserDefined = new Long[16];
        responseCode = -1;
        mRandom = new SecureRandom();
    }
    public void setHeader(int headerID, Object headerValue) {
        long temp = -1;
        switch (headerID) {
            case COUNT:
                if (!(headerValue instanceof Long)) {
                    if (headerValue == null) {
                        mCount = null;
                        break;
                    }
                    throw new IllegalArgumentException("Count must be a Long");
                }
                temp = ((Long)headerValue).longValue();
                if ((temp < 0L) || (temp > 0xFFFFFFFFL)) {
                    throw new IllegalArgumentException("Count must be between 0 and 0xFFFFFFFF");
                }
                mCount = (Long)headerValue;
                break;
            case NAME:
                if ((headerValue != null) && (!(headerValue instanceof String))) {
                    throw new IllegalArgumentException("Name must be a String");
                }
                mName = (String)headerValue;
                break;
            case TYPE:
                if ((headerValue != null) && (!(headerValue instanceof String))) {
                    throw new IllegalArgumentException("Type must be a String");
                }
                mType = (String)headerValue;
                break;
            case LENGTH:
                if (!(headerValue instanceof Long)) {
                    if (headerValue == null) {
                        mLength = null;
                        break;
                    }
                    throw new IllegalArgumentException("Length must be a Long");
                }
                temp = ((Long)headerValue).longValue();
                if ((temp < 0L) || (temp > 0xFFFFFFFFL)) {
                    throw new IllegalArgumentException("Length must be between 0 and 0xFFFFFFFF");
                }
                mLength = (Long)headerValue;
                break;
            case TIME_ISO_8601:
                if ((headerValue != null) && (!(headerValue instanceof Calendar))) {
                    throw new IllegalArgumentException("Time ISO 8601 must be a Calendar");
                }
                mIsoTime = (Calendar)headerValue;
                break;
            case TIME_4_BYTE:
                if ((headerValue != null) && (!(headerValue instanceof Calendar))) {
                    throw new IllegalArgumentException("Time 4 Byte must be a Calendar");
                }
                mByteTime = (Calendar)headerValue;
                break;
            case DESCRIPTION:
                if ((headerValue != null) && (!(headerValue instanceof String))) {
                    throw new IllegalArgumentException("Description must be a String");
                }
                mDescription = (String)headerValue;
                break;
            case TARGET:
                if (headerValue == null) {
                    mTarget = null;
                } else {
                    if (!(headerValue instanceof byte[])) {
                        throw new IllegalArgumentException("Target must be a byte array");
                    } else {
                        mTarget = new byte[((byte[])headerValue).length];
                        System.arraycopy(headerValue, 0, mTarget, 0, mTarget.length);
                    }
                }
                break;
            case HTTP:
                if (headerValue == null) {
                    mHttpHeader = null;
                } else {
                    if (!(headerValue instanceof byte[])) {
                        throw new IllegalArgumentException("HTTP must be a byte array");
                    } else {
                        mHttpHeader = new byte[((byte[])headerValue).length];
                        System.arraycopy(headerValue, 0, mHttpHeader, 0, mHttpHeader.length);
                    }
                }
                break;
            case WHO:
                if (headerValue == null) {
                    mWho = null;
                } else {
                    if (!(headerValue instanceof byte[])) {
                        throw new IllegalArgumentException("WHO must be a byte array");
                    } else {
                        mWho = new byte[((byte[])headerValue).length];
                        System.arraycopy(headerValue, 0, mWho, 0, mWho.length);
                    }
                }
                break;
            case OBJECT_CLASS:
                if (headerValue == null) {
                    mObjectClass = null;
                } else {
                    if (!(headerValue instanceof byte[])) {
                        throw new IllegalArgumentException("Object Class must be a byte array");
                    } else {
                        mObjectClass = new byte[((byte[])headerValue).length];
                        System.arraycopy(headerValue, 0, mObjectClass, 0, mObjectClass.length);
                    }
                }
                break;
            case APPLICATION_PARAMETER:
                if (headerValue == null) {
                    mAppParam = null;
                } else {
                    if (!(headerValue instanceof byte[])) {
                        throw new IllegalArgumentException(
                                "Application Parameter must be a byte array");
                    } else {
                        mAppParam = new byte[((byte[])headerValue).length];
                        System.arraycopy(headerValue, 0, mAppParam, 0, mAppParam.length);
                    }
                }
                break;
            default:
                if ((headerID >= 0x30) && (headerID <= 0x3F)) {
                    if ((headerValue != null) && (!(headerValue instanceof String))) {
                        throw new IllegalArgumentException(
                                "Unicode String User Defined must be a String");
                    }
                    mUnicodeUserDefined[headerID - 0x30] = (String)headerValue;
                    break;
                }
                if ((headerID >= 0x70) && (headerID <= 0x7F)) {
                    if (headerValue == null) {
                        mSequenceUserDefined[headerID - 0x70] = null;
                    } else {
                        if (!(headerValue instanceof byte[])) {
                            throw new IllegalArgumentException(
                                    "Byte Sequence User Defined must be a byte array");
                        } else {
                            mSequenceUserDefined[headerID - 0x70] = new byte[((byte[])headerValue).length];
                            System.arraycopy(headerValue, 0, mSequenceUserDefined[headerID - 0x70],
                                    0, mSequenceUserDefined[headerID - 0x70].length);
                        }
                    }
                    break;
                }
                if ((headerID >= 0xB0) && (headerID <= 0xBF)) {
                    if ((headerValue != null) && (!(headerValue instanceof Byte))) {
                        throw new IllegalArgumentException("ByteUser Defined must be a Byte");
                    }
                    mByteUserDefined[headerID - 0xB0] = (Byte)headerValue;
                    break;
                }
                if ((headerID >= 0xF0) && (headerID <= 0xFF)) {
                    if (!(headerValue instanceof Long)) {
                        if (headerValue == null) {
                            mIntegerUserDefined[headerID - 0xF0] = null;
                            break;
                        }
                        throw new IllegalArgumentException("Integer User Defined must be a Long");
                    }
                    temp = ((Long)headerValue).longValue();
                    if ((temp < 0L) || (temp > 0xFFFFFFFFL)) {
                        throw new IllegalArgumentException(
                                "Integer User Defined must be between 0 and 0xFFFFFFFF");
                    }
                    mIntegerUserDefined[headerID - 0xF0] = (Long)headerValue;
                    break;
                }
                throw new IllegalArgumentException("Invalid Header Identifier");
        }
    }
    public Object getHeader(int headerID) throws IOException {
        switch (headerID) {
            case COUNT:
                return mCount;
            case NAME:
                return mName;
            case TYPE:
                return mType;
            case LENGTH:
                return mLength;
            case TIME_ISO_8601:
                return mIsoTime;
            case TIME_4_BYTE:
                return mByteTime;
            case DESCRIPTION:
                return mDescription;
            case TARGET:
                return mTarget;
            case HTTP:
                return mHttpHeader;
            case WHO:
                return mWho;
            case OBJECT_CLASS:
                return mObjectClass;
            case APPLICATION_PARAMETER:
                return mAppParam;
            default:
                if ((headerID >= 0x30) && (headerID <= 0x3F)) {
                    return mUnicodeUserDefined[headerID - 0x30];
                }
                if ((headerID >= 0x70) && (headerID <= 0x7F)) {
                    return mSequenceUserDefined[headerID - 0x70];
                }
                if ((headerID >= 0xB0) && (headerID <= 0xBF)) {
                    return mByteUserDefined[headerID - 0xB0];
                }
                if ((headerID >= 0xF0) && (headerID <= 0xFF)) {
                    return mIntegerUserDefined[headerID - 0xF0];
                }
                throw new IllegalArgumentException("Invalid Header Identifier");
        }
    }
    public int[] getHeaderList() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        if (mCount != null) {
            out.write(COUNT);
        }
        if (mName != null) {
            out.write(NAME);
        }
        if (mType != null) {
            out.write(TYPE);
        }
        if (mLength != null) {
            out.write(LENGTH);
        }
        if (mIsoTime != null) {
            out.write(TIME_ISO_8601);
        }
        if (mByteTime != null) {
            out.write(TIME_4_BYTE);
        }
        if (mDescription != null) {
            out.write(DESCRIPTION);
        }
        if (mTarget != null) {
            out.write(TARGET);
        }
        if (mHttpHeader != null) {
            out.write(HTTP);
        }
        if (mWho != null) {
            out.write(WHO);
        }
        if (mAppParam != null) {
            out.write(APPLICATION_PARAMETER);
        }
        if (mObjectClass != null) {
            out.write(OBJECT_CLASS);
        }
        for (int i = 0x30; i < 0x40; i++) {
            if (mUnicodeUserDefined[i - 0x30] != null) {
                out.write(i);
            }
        }
        for (int i = 0x70; i < 0x80; i++) {
            if (mSequenceUserDefined[i - 0x70] != null) {
                out.write(i);
            }
        }
        for (int i = 0xB0; i < 0xC0; i++) {
            if (mByteUserDefined[i - 0xB0] != null) {
                out.write(i);
            }
        }
        for (int i = 0xF0; i < 0x100; i++) {
            if (mIntegerUserDefined[i - 0xF0] != null) {
                out.write(i);
            }
        }
        byte[] headers = out.toByteArray();
        out.close();
        if ((headers == null) || (headers.length == 0)) {
            return null;
        }
        int[] result = new int[headers.length];
        for (int i = 0; i < headers.length; i++) {
            result[i] = headers[i] & 0xFF;
        }
        return result;
    }
    public void createAuthenticationChallenge(String realm, boolean userID, boolean access)
            throws IOException {
        nonce = new byte[16];
        for (int i = 0; i < 16; i++) {
            nonce[i] = (byte)mRandom.nextInt();
        }
        mAuthChall = ObexHelper.computeAuthenticationChallenge(nonce, realm, access, userID);
    }
    public int getResponseCode() throws IOException {
        if (responseCode == -1) {
            throw new IOException("May not be called on a server");
        } else {
            return responseCode;
        }
    }
}
