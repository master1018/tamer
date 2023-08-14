public class VCardEntryConstructor implements VCardInterpreter {
    private static String LOG_TAG = "VCardEntryConstructor";
     static final String DEFAULT_CHARSET_FOR_DECODED_BYTES = "UTF-8";
    private VCardEntry.Property mCurrentProperty = new VCardEntry.Property();
    private VCardEntry mCurrentContactStruct;
    private String mParamType;
    private String mInputCharset;
    final private String mCharsetForDecodedBytes;
    final private boolean mStrictLineBreakParsing;
    final private int mVCardType;
    final private Account mAccount;
    private long mTimePushIntoContentResolver;
    final private List<VCardEntryHandler> mEntryHandlers = new ArrayList<VCardEntryHandler>();
    public VCardEntryConstructor() {
        this(null, null, false, VCardConfig.VCARD_TYPE_V21_GENERIC_UTF8, null);
    }
    public VCardEntryConstructor(final int vcardType) {
        this(null, null, false, vcardType, null);
    }
    public VCardEntryConstructor(final String charset, final boolean strictLineBreakParsing,
            final int vcardType, final Account account) {
        this(null, charset, strictLineBreakParsing, vcardType, account);
    }
    public VCardEntryConstructor(final String inputCharset, final String charsetForDetodedBytes,
            final boolean strictLineBreakParsing, final int vcardType,
            final Account account) {
        if (inputCharset != null) {
            mInputCharset = inputCharset;
        } else {
            mInputCharset = VCardConfig.DEFAULT_CHARSET;
        }
        if (charsetForDetodedBytes != null) {
            mCharsetForDecodedBytes = charsetForDetodedBytes;
        } else {
            mCharsetForDecodedBytes = DEFAULT_CHARSET_FOR_DECODED_BYTES;
        }
        mStrictLineBreakParsing = strictLineBreakParsing;
        mVCardType = vcardType;
        mAccount = account;
    }
    public void addEntryHandler(VCardEntryHandler entryHandler) {
        mEntryHandlers.add(entryHandler);
    }
    public void start() {
        for (VCardEntryHandler entryHandler : mEntryHandlers) {
            entryHandler.onStart();
        }
    }
    public void end() {
        for (VCardEntryHandler entryHandler : mEntryHandlers) {
            entryHandler.onEnd();
        }
    }
    public void clear() {
        mCurrentContactStruct = null;
        mCurrentProperty = new VCardEntry.Property();
    }
    public void startEntry() {
        if (mCurrentContactStruct != null) {
            Log.e(LOG_TAG, "Nested VCard code is not supported now.");
        }
        mCurrentContactStruct = new VCardEntry(mVCardType, mAccount);
    }
    public void endEntry() {
        mCurrentContactStruct.consolidateFields();
        for (VCardEntryHandler entryHandler : mEntryHandlers) {
            entryHandler.onEntryCreated(mCurrentContactStruct);
        }
        mCurrentContactStruct = null;
    }
    public void startProperty() {
        mCurrentProperty.clear();
    }
    public void endProperty() {
        mCurrentContactStruct.addProperty(mCurrentProperty);
    }
    public void propertyName(String name) {
        mCurrentProperty.setPropertyName(name);
    }
    public void propertyGroup(String group) {
    }
    public void propertyParamType(String type) {
        if (mParamType != null) {
            Log.e(LOG_TAG, "propertyParamType() is called more than once " +
                    "before propertyParamValue() is called");
        }
        mParamType = type;
    }
    public void propertyParamValue(String value) {
        if (mParamType == null) {
            mParamType = "TYPE";
        }
        mCurrentProperty.addParameter(mParamType, value);
        mParamType = null;
    }
    private String encodeString(String originalString, String charsetForDecodedBytes) {
        if (mInputCharset.equalsIgnoreCase(charsetForDecodedBytes)) {
            return originalString;
        }
        Charset charset = Charset.forName(mInputCharset);
        ByteBuffer byteBuffer = charset.encode(originalString);
        byte[] bytes = new byte[byteBuffer.remaining()];
        byteBuffer.get(bytes);
        try {
            return new String(bytes, charsetForDecodedBytes);
        } catch (UnsupportedEncodingException e) {
            Log.e(LOG_TAG, "Failed to encode: charset=" + charsetForDecodedBytes);
            return null;
        }
    }
    private String handleOneValue(String value, String charsetForDecodedBytes, String encoding) {
        if (encoding != null) {
            if (encoding.equals("BASE64") || encoding.equals("B")) {
                mCurrentProperty.setPropertyBytes(Base64.decodeBase64(value.getBytes()));
                return value;
            } else if (encoding.equals("QUOTED-PRINTABLE")) {
                StringBuilder builder = new StringBuilder();
                int length = value.length();
                for (int i = 0; i < length; i++) {
                    char ch = value.charAt(i);
                    if (ch == '=' && i < length - 1) {
                        char nextCh = value.charAt(i + 1);
                        if (nextCh == ' ' || nextCh == '\t') {
                            builder.append(nextCh);
                            i++;
                            continue;
                        }
                    }
                    builder.append(ch);
                }
                String quotedPrintable = builder.toString();
                String[] lines;
                if (mStrictLineBreakParsing) {
                    lines = quotedPrintable.split("\r\n");
                } else {
                    builder = new StringBuilder();
                    length = quotedPrintable.length();
                    ArrayList<String> list = new ArrayList<String>();
                    for (int i = 0; i < length; i++) {
                        char ch = quotedPrintable.charAt(i);
                        if (ch == '\n') {
                            list.add(builder.toString());
                            builder = new StringBuilder();
                        } else if (ch == '\r') {
                            list.add(builder.toString());
                            builder = new StringBuilder();
                            if (i < length - 1) {
                                char nextCh = quotedPrintable.charAt(i + 1);
                                if (nextCh == '\n') {
                                    i++;
                                }
                            }
                        } else {
                            builder.append(ch);
                        }
                    }
                    String finalLine = builder.toString();
                    if (finalLine.length() > 0) {
                        list.add(finalLine);
                    }
                    lines = list.toArray(new String[0]);
                }
                builder = new StringBuilder();
                for (String line : lines) {
                    if (line.endsWith("=")) {
                        line = line.substring(0, line.length() - 1);
                    }
                    builder.append(line);
                }
                byte[] bytes;
                try {
                    bytes = builder.toString().getBytes(mInputCharset);
                } catch (UnsupportedEncodingException e1) {
                    Log.e(LOG_TAG, "Failed to encode: charset=" + mInputCharset);
                    bytes = builder.toString().getBytes();
                }
                try {
                    bytes = QuotedPrintableCodec.decodeQuotedPrintable(bytes);
                } catch (DecoderException e) {
                    Log.e(LOG_TAG, "Failed to decode quoted-printable: " + e);
                    return "";
                }
                try {
                    return new String(bytes, charsetForDecodedBytes);
                } catch (UnsupportedEncodingException e) {
                    Log.e(LOG_TAG, "Failed to encode: charset=" + charsetForDecodedBytes);
                    return new String(bytes);
                }
            }
        }
        return encodeString(value, charsetForDecodedBytes);
    }
    public void propertyValues(List<String> values) {
        if (values == null || values.isEmpty()) {
            return;
        }
        final Collection<String> charsetCollection = mCurrentProperty.getParameters("CHARSET");
        final String charset =
            ((charsetCollection != null) ? charsetCollection.iterator().next() : null);
        final Collection<String> encodingCollection = mCurrentProperty.getParameters("ENCODING");
        final String encoding =
            ((encodingCollection != null) ? encodingCollection.iterator().next() : null);
        String charsetForDecodedBytes = CharsetUtils.nameForDefaultVendor(charset);
        if (charsetForDecodedBytes == null || charsetForDecodedBytes.length() == 0) {
            charsetForDecodedBytes = mCharsetForDecodedBytes;
        }
        for (final String value : values) {
            mCurrentProperty.addToPropertyValueList(
                    handleOneValue(value, charsetForDecodedBytes, encoding));
        }
    }
    public void showPerformanceInfo() {
        Log.d(LOG_TAG, "time for insert ContactStruct to database: " + 
                mTimePushIntoContentResolver + " ms");
    }
}
