public class PduComposer {
    static private final int PDU_PHONE_NUMBER_ADDRESS_TYPE = 1;
    static private final int PDU_EMAIL_ADDRESS_TYPE = 2;
    static private final int PDU_IPV4_ADDRESS_TYPE = 3;
    static private final int PDU_IPV6_ADDRESS_TYPE = 4;
    static private final int PDU_UNKNOWN_ADDRESS_TYPE = 5;
    static final String REGEXP_PHONE_NUMBER_ADDRESS_TYPE = "\\+?[0-9|\\.|\\-]+";
    static final String REGEXP_EMAIL_ADDRESS_TYPE = "[a-zA-Z| ]*\\<{0,1}[a-zA-Z| ]+@{1}" +
            "[a-zA-Z| ]+\\.{1}[a-zA-Z| ]+\\>{0,1}";
    static final String REGEXP_IPV6_ADDRESS_TYPE =
        "[a-fA-F]{4}\\:{1}[a-fA-F0-9]{4}\\:{1}[a-fA-F0-9]{4}\\:{1}" +
        "[a-fA-F0-9]{4}\\:{1}[a-fA-F0-9]{4}\\:{1}[a-fA-F0-9]{4}\\:{1}" +
        "[a-fA-F0-9]{4}\\:{1}[a-fA-F0-9]{4}";
    static final String REGEXP_IPV4_ADDRESS_TYPE = "[0-9]{1,3}\\.{1}[0-9]{1,3}\\.{1}" +
            "[0-9]{1,3}\\.{1}[0-9]{1,3}";
    static final String STRING_PHONE_NUMBER_ADDRESS_TYPE = "/TYPE=PLMN";
    static final String STRING_IPV4_ADDRESS_TYPE = "/TYPE=IPV4";
    static final String STRING_IPV6_ADDRESS_TYPE = "/TYPE=IPV6";
    static private final int PDU_COMPOSE_SUCCESS = 0;
    static private final int PDU_COMPOSE_CONTENT_ERROR = 1;
    static private final int PDU_COMPOSE_FIELD_NOT_SET = 2;
    static private final int PDU_COMPOSE_FIELD_NOT_SUPPORTED = 3;
    static private final int QUOTED_STRING_FLAG = 34;
    static private final int END_STRING_FLAG = 0;
    static private final int LENGTH_QUOTE = 31;
    static private final int TEXT_MAX = 127;
    static private final int SHORT_INTEGER_MAX = 127;
    static private final int LONG_INTEGER_LENGTH_MAX = 8;
    static private final int PDU_COMPOSER_BLOCK_SIZE = 1024;
    protected ByteArrayOutputStream mMessage = null;
    private GenericPdu mPdu = null;
    protected int mPosition = 0;
    private BufferStack mStack = null;
    private final ContentResolver mResolver;
    private PduHeaders mPduHeader = null;
    private static HashMap<String, Integer> mContentTypeMap = null;
    static {
        mContentTypeMap = new HashMap<String, Integer>();
        int i;
        for (i = 0; i < PduContentTypes.contentTypes.length; i++) {
            mContentTypeMap.put(PduContentTypes.contentTypes[i], i);
        }
    }
    public PduComposer(Context context, GenericPdu pdu) {
        mPdu = pdu;
        mResolver = context.getContentResolver();
        mPduHeader = pdu.getPduHeaders();
        mStack = new BufferStack();
        mMessage = new ByteArrayOutputStream();
        mPosition = 0;
    }
    public byte[] make() {
        int type = mPdu.getMessageType();
        switch (type) {
            case PduHeaders.MESSAGE_TYPE_SEND_REQ:
                if (makeSendReqPdu() != PDU_COMPOSE_SUCCESS) {
                    return null;
                }
                break;
            case PduHeaders.MESSAGE_TYPE_NOTIFYRESP_IND:
                if (makeNotifyResp() != PDU_COMPOSE_SUCCESS) {
                    return null;
                }
                break;
            case PduHeaders.MESSAGE_TYPE_ACKNOWLEDGE_IND:
                if (makeAckInd() != PDU_COMPOSE_SUCCESS) {
                    return null;
                }
                break;
            case PduHeaders.MESSAGE_TYPE_READ_REC_IND:
                if (makeReadRecInd() != PDU_COMPOSE_SUCCESS) {
                    return null;
                }
                break;
            default:
                return null;
        }
        return mMessage.toByteArray();
    }
    protected void arraycopy(byte[] buf, int pos, int length) {
        mMessage.write(buf, pos, length);
        mPosition = mPosition + length;
    }
    protected void append(int value) {
        mMessage.write(value);
        mPosition ++;
    }
    protected void appendShortInteger(int value) {
        append((value | 0x80) & 0xff);
    }
    protected void appendOctet(int number) {
        append(number);
    }
    protected void appendShortLength(int value) {
        append(value);
    }
    protected void appendLongInteger(long longInt) {
        int size;
        long temp = longInt;
        for(size = 0; (temp != 0) && (size < LONG_INTEGER_LENGTH_MAX); size++) {
            temp = (temp >>> 8);
        }
        appendShortLength(size);
        int i;
        int shift = (size -1) * 8;
        for (i = 0; i < size; i++) {
            append((int)((longInt >>> shift) & 0xff));
            shift = shift - 8;
        }
    }
    protected void appendTextString(byte[] text) {
        if (((text[0])&0xff) > TEXT_MAX) { 
            append(TEXT_MAX);
        }
        arraycopy(text, 0, text.length);
        append(0);
    }
    protected void appendTextString(String str) {
        appendTextString(str.getBytes());
    }
    protected void appendEncodedString(EncodedStringValue enStr) {
        assert(enStr != null);
        int charset = enStr.getCharacterSet();
        byte[] textString = enStr.getTextString();
        if (null == textString) {
            return;
        }
        mStack.newbuf();
        PositionMarker start = mStack.mark();
        appendShortInteger(charset);
        appendTextString(textString);
        int len = start.getLength();
        mStack.pop();
        appendValueLength(len);
        mStack.copy();
    }
    protected void appendUintvarInteger(long value) {
        int i;
        long max = SHORT_INTEGER_MAX;
        for (i = 0; i < 5; i++) {
            if (value < max) {
                break;
            }
            max = (max << 7) | 0x7fl;
        }
        while(i > 0) {
            long temp = value >>> (i * 7);
            temp = temp & 0x7f;
            append((int)((temp | 0x80) & 0xff));
            i--;
        }
        append((int)(value & 0x7f));
    }
    protected void appendDateValue(long date) {
        appendLongInteger(date);
    }
    protected void appendValueLength(long value) {
        if (value < LENGTH_QUOTE) {
            appendShortLength((int) value);
            return;
        }
        append(LENGTH_QUOTE);
        appendUintvarInteger(value);
    }
    protected void appendQuotedString(byte[] text) {
        append(QUOTED_STRING_FLAG);
        arraycopy(text, 0, text.length);
        append(END_STRING_FLAG);
    }
    protected void appendQuotedString(String str) {
        appendQuotedString(str.getBytes());
    }
    private EncodedStringValue appendAddressType(EncodedStringValue address) {
        EncodedStringValue temp = null;
        try {
            int addressType = checkAddressType(address.getString());
            temp = EncodedStringValue.copy(address);
            if (PDU_PHONE_NUMBER_ADDRESS_TYPE == addressType) {
                temp.appendTextString(STRING_PHONE_NUMBER_ADDRESS_TYPE.getBytes());
            } else if (PDU_IPV4_ADDRESS_TYPE == addressType) {
                temp.appendTextString(STRING_IPV4_ADDRESS_TYPE.getBytes());
            } else if (PDU_IPV6_ADDRESS_TYPE == addressType) {
                temp.appendTextString(STRING_IPV6_ADDRESS_TYPE.getBytes());
            }
        } catch (NullPointerException e) {
            return null;
        }
        return temp;
    }
    private int appendHeader(int field) {
        switch (field) {
            case PduHeaders.MMS_VERSION:
                appendOctet(field);
                int version = mPduHeader.getOctet(field);
                if (0 == version) {
                    appendShortInteger(PduHeaders.CURRENT_MMS_VERSION);
                } else {
                    appendShortInteger(version);
                }
                break;
            case PduHeaders.MESSAGE_ID:
            case PduHeaders.TRANSACTION_ID:
                byte[] textString = mPduHeader.getTextString(field);
                if (null == textString) {
                    return PDU_COMPOSE_FIELD_NOT_SET;
                }
                appendOctet(field);
                appendTextString(textString);
                break;
            case PduHeaders.TO:
            case PduHeaders.BCC:
            case PduHeaders.CC:
                EncodedStringValue[] addr = mPduHeader.getEncodedStringValues(field);
                if (null == addr) {
                    return PDU_COMPOSE_FIELD_NOT_SET;
                }
                EncodedStringValue temp;
                for (int i = 0; i < addr.length; i++) {
                    temp = appendAddressType(addr[i]);
                    if (temp == null) {
                        return PDU_COMPOSE_CONTENT_ERROR;
                    }
                    appendOctet(field);
                    appendEncodedString(temp);
                }
                break;
            case PduHeaders.FROM:
                appendOctet(field);
                EncodedStringValue from = mPduHeader.getEncodedStringValue(field);
                if ((from == null)
                        || TextUtils.isEmpty(from.getString())
                        || new String(from.getTextString()).equals(
                                PduHeaders.FROM_INSERT_ADDRESS_TOKEN_STR)) {
                    append(1);
                    append(PduHeaders.FROM_INSERT_ADDRESS_TOKEN);
                } else {
                    mStack.newbuf();
                    PositionMarker fstart = mStack.mark();
                    append(PduHeaders.FROM_ADDRESS_PRESENT_TOKEN);
                    temp = appendAddressType(from);
                    if (temp == null) {
                        return PDU_COMPOSE_CONTENT_ERROR;
                    }
                    appendEncodedString(temp);
                    int flen = fstart.getLength();
                    mStack.pop();
                    appendValueLength(flen);
                    mStack.copy();
                }
                break;
            case PduHeaders.READ_STATUS:
            case PduHeaders.STATUS:
            case PduHeaders.REPORT_ALLOWED:
            case PduHeaders.PRIORITY:
            case PduHeaders.DELIVERY_REPORT:
            case PduHeaders.READ_REPORT:
                int octet = mPduHeader.getOctet(field);
                if (0 == octet) {
                    return PDU_COMPOSE_FIELD_NOT_SET;
                }
                appendOctet(field);
                appendOctet(octet);
                break;
            case PduHeaders.DATE:
                long date = mPduHeader.getLongInteger(field);
                if (-1 == date) {
                    return PDU_COMPOSE_FIELD_NOT_SET;
                }
                appendOctet(field);
                appendDateValue(date);
                break;
            case PduHeaders.SUBJECT:
                EncodedStringValue enString =
                    mPduHeader.getEncodedStringValue(field);
                if (null == enString) {
                    return PDU_COMPOSE_FIELD_NOT_SET;
                }
                appendOctet(field);
                appendEncodedString(enString);
                break;
            case PduHeaders.MESSAGE_CLASS:
                byte[] messageClass = mPduHeader.getTextString(field);
                if (null == messageClass) {
                    return PDU_COMPOSE_FIELD_NOT_SET;
                }
                appendOctet(field);
                if (Arrays.equals(messageClass,
                        PduHeaders.MESSAGE_CLASS_ADVERTISEMENT_STR.getBytes())) {
                    appendOctet(PduHeaders.MESSAGE_CLASS_ADVERTISEMENT);
                } else if (Arrays.equals(messageClass,
                        PduHeaders.MESSAGE_CLASS_AUTO_STR.getBytes())) {
                    appendOctet(PduHeaders.MESSAGE_CLASS_AUTO);
                } else if (Arrays.equals(messageClass,
                        PduHeaders.MESSAGE_CLASS_PERSONAL_STR.getBytes())) {
                    appendOctet(PduHeaders.MESSAGE_CLASS_PERSONAL);
                } else if (Arrays.equals(messageClass,
                        PduHeaders.MESSAGE_CLASS_INFORMATIONAL_STR.getBytes())) {
                    appendOctet(PduHeaders.MESSAGE_CLASS_INFORMATIONAL);
                } else {
                    appendTextString(messageClass);
                }
                break;
            case PduHeaders.EXPIRY:
                long expiry = mPduHeader.getLongInteger(field);
                if (-1 == expiry) {
                    return PDU_COMPOSE_FIELD_NOT_SET;
                }
                appendOctet(field);
                mStack.newbuf();
                PositionMarker expiryStart = mStack.mark();
                append(PduHeaders.VALUE_RELATIVE_TOKEN);
                appendLongInteger(expiry);
                int expiryLength = expiryStart.getLength();
                mStack.pop();
                appendValueLength(expiryLength);
                mStack.copy();
                break;
            default:
                return PDU_COMPOSE_FIELD_NOT_SUPPORTED;
        }
        return PDU_COMPOSE_SUCCESS;
    }
    private int makeReadRecInd() {
        if (mMessage == null) {
            mMessage = new ByteArrayOutputStream();
            mPosition = 0;
        }
        appendOctet(PduHeaders.MESSAGE_TYPE);
        appendOctet(PduHeaders.MESSAGE_TYPE_READ_REC_IND);
        if (appendHeader(PduHeaders.MMS_VERSION) != PDU_COMPOSE_SUCCESS) {
            return PDU_COMPOSE_CONTENT_ERROR;
        }
        if (appendHeader(PduHeaders.MESSAGE_ID) != PDU_COMPOSE_SUCCESS) {
            return PDU_COMPOSE_CONTENT_ERROR;
        }
        if (appendHeader(PduHeaders.TO) != PDU_COMPOSE_SUCCESS) {
            return PDU_COMPOSE_CONTENT_ERROR;
        }
        if (appendHeader(PduHeaders.FROM) != PDU_COMPOSE_SUCCESS) {
            return PDU_COMPOSE_CONTENT_ERROR;
        }
        appendHeader(PduHeaders.DATE);
        if (appendHeader(PduHeaders.READ_STATUS) != PDU_COMPOSE_SUCCESS) {
            return PDU_COMPOSE_CONTENT_ERROR;
        }
        return PDU_COMPOSE_SUCCESS;
    }
    private int makeNotifyResp() {
        if (mMessage == null) {
            mMessage = new ByteArrayOutputStream();
            mPosition = 0;
        }
        appendOctet(PduHeaders.MESSAGE_TYPE);
        appendOctet(PduHeaders.MESSAGE_TYPE_NOTIFYRESP_IND);
        if (appendHeader(PduHeaders.TRANSACTION_ID) != PDU_COMPOSE_SUCCESS) {
            return PDU_COMPOSE_CONTENT_ERROR;
        }
        if (appendHeader(PduHeaders.MMS_VERSION) != PDU_COMPOSE_SUCCESS) {
            return PDU_COMPOSE_CONTENT_ERROR;
        }
        if (appendHeader(PduHeaders.STATUS) != PDU_COMPOSE_SUCCESS) {
            return PDU_COMPOSE_CONTENT_ERROR;
        }
        return PDU_COMPOSE_SUCCESS;
    }
    private int makeAckInd() {
        if (mMessage == null) {
            mMessage = new ByteArrayOutputStream();
            mPosition = 0;
        }
        appendOctet(PduHeaders.MESSAGE_TYPE);
        appendOctet(PduHeaders.MESSAGE_TYPE_ACKNOWLEDGE_IND);
        if (appendHeader(PduHeaders.TRANSACTION_ID) != PDU_COMPOSE_SUCCESS) {
            return PDU_COMPOSE_CONTENT_ERROR;
        }
        if (appendHeader(PduHeaders.MMS_VERSION) != PDU_COMPOSE_SUCCESS) {
            return PDU_COMPOSE_CONTENT_ERROR;
        }
        appendHeader(PduHeaders.REPORT_ALLOWED);
        return PDU_COMPOSE_SUCCESS;
    }
    private int makeSendReqPdu() {
        if (mMessage == null) {
            mMessage = new ByteArrayOutputStream();
            mPosition = 0;
        }
        appendOctet(PduHeaders.MESSAGE_TYPE);
        appendOctet(PduHeaders.MESSAGE_TYPE_SEND_REQ);
        appendOctet(PduHeaders.TRANSACTION_ID);
        byte[] trid = mPduHeader.getTextString(PduHeaders.TRANSACTION_ID);
        if (trid == null) {
            throw new IllegalArgumentException("Transaction-ID is null.");
        }
        appendTextString(trid);
        if (appendHeader(PduHeaders.MMS_VERSION) != PDU_COMPOSE_SUCCESS) {
            return PDU_COMPOSE_CONTENT_ERROR;
        }
        appendHeader(PduHeaders.DATE);
        if (appendHeader(PduHeaders.FROM) != PDU_COMPOSE_SUCCESS) {
            return PDU_COMPOSE_CONTENT_ERROR;
        }
        boolean recipient = false;
        if (appendHeader(PduHeaders.TO) != PDU_COMPOSE_CONTENT_ERROR) {
            recipient = true;
        }
        if (appendHeader(PduHeaders.CC) != PDU_COMPOSE_CONTENT_ERROR) {
            recipient = true;
        }
        if (appendHeader(PduHeaders.BCC) != PDU_COMPOSE_CONTENT_ERROR) {
            recipient = true;
        }
        if (false == recipient) {
            return PDU_COMPOSE_CONTENT_ERROR;
        }
        appendHeader(PduHeaders.SUBJECT);
        appendHeader(PduHeaders.MESSAGE_CLASS);
        appendHeader(PduHeaders.EXPIRY);
        appendHeader(PduHeaders.PRIORITY);
        appendHeader(PduHeaders.DELIVERY_REPORT);
        appendHeader(PduHeaders.READ_REPORT);
        appendOctet(PduHeaders.CONTENT_TYPE);
        makeMessageBody();
        return PDU_COMPOSE_SUCCESS;  
    }
    private int makeMessageBody() {
        mStack.newbuf();  
        PositionMarker ctStart = mStack.mark();
        String contentType = new String(mPduHeader.getTextString(PduHeaders.CONTENT_TYPE));
        Integer contentTypeIdentifier = mContentTypeMap.get(contentType);
        if (contentTypeIdentifier == null) {
            return PDU_COMPOSE_CONTENT_ERROR;
        }
        appendShortInteger(contentTypeIdentifier.intValue());
        PduBody body = ((SendReq) mPdu).getBody();
        if (null == body || body.getPartsNum() == 0) {
            appendUintvarInteger(0);
            mStack.pop();
            mStack.copy();
            return PDU_COMPOSE_SUCCESS;
        }
        PduPart part;
        try {
            part = body.getPart(0);
            byte[] start = part.getContentId();
            if (start != null) {
                appendOctet(PduPart.P_DEP_START);
                if (('<' == start[0]) && ('>' == start[start.length - 1])) {
                    appendTextString(start);
                } else {
                    appendTextString("<" + new String(start) + ">");
                }
            }
            appendOctet(PduPart.P_CT_MR_TYPE);
            appendTextString(part.getContentType());
        }
        catch (ArrayIndexOutOfBoundsException e){
            e.printStackTrace();
        }
        int ctLength = ctStart.getLength();
        mStack.pop();
        appendValueLength(ctLength);
        mStack.copy();
        int partNum = body.getPartsNum();
        appendUintvarInteger(partNum);
        for (int i = 0; i < partNum; i++) {
            part = body.getPart(i);
            mStack.newbuf();  
            PositionMarker attachment = mStack.mark();
            mStack.newbuf();  
            PositionMarker contentTypeBegin = mStack.mark();
            byte[] partContentType = part.getContentType();
            if (partContentType == null) {
                return PDU_COMPOSE_CONTENT_ERROR;
            }
            Integer partContentTypeIdentifier =
                mContentTypeMap.get(new String(partContentType));
            if (partContentTypeIdentifier == null) {
                appendTextString(partContentType);
            } else {
                appendShortInteger(partContentTypeIdentifier.intValue());
            }
            byte[] name = part.getName();
            if (null == name) {
                name = part.getFilename();
                if (null == name) {
                    name = part.getContentLocation();
                    if (null == name) {
                        return PDU_COMPOSE_CONTENT_ERROR;
                    }
                }
            }
            appendOctet(PduPart.P_DEP_NAME);
            appendTextString(name);
            int charset = part.getCharset();
            if (charset != 0) {
                appendOctet(PduPart.P_CHARSET);
                appendShortInteger(charset);
            }
            int contentTypeLength = contentTypeBegin.getLength();
            mStack.pop();
            appendValueLength(contentTypeLength);
            mStack.copy();
            byte[] contentId = part.getContentId();
            if (null != contentId) {
                appendOctet(PduPart.P_CONTENT_ID);
                if (('<' == contentId[0]) && ('>' == contentId[contentId.length - 1])) {
                    appendQuotedString(contentId);
                } else {
                    appendQuotedString("<" + new String(contentId) + ">");
                }
            }
            byte[] contentLocation = part.getContentLocation();
            if (null != contentLocation) {
            	appendOctet(PduPart.P_CONTENT_LOCATION);
            	appendTextString(contentLocation);
            }
            int headerLength = attachment.getLength();
            int dataLength = 0; 
            byte[] partData = part.getData();
            if (partData != null) {
                arraycopy(partData, 0, partData.length);
                dataLength = partData.length;
            } else {
                InputStream cr;
                try {
                    byte[] buffer = new byte[PDU_COMPOSER_BLOCK_SIZE];
                    cr = mResolver.openInputStream(part.getDataUri());
                    int len = 0;
                    while ((len = cr.read(buffer)) != -1) {
                        mMessage.write(buffer, 0, len);
                        mPosition += len;
                        dataLength += len;
                    }
                } catch (FileNotFoundException e) {
                    return PDU_COMPOSE_CONTENT_ERROR;
                } catch (IOException e) {
                    return PDU_COMPOSE_CONTENT_ERROR;
                } catch (RuntimeException e) {
                    return PDU_COMPOSE_CONTENT_ERROR;
                }
            }
            if (dataLength != (attachment.getLength() - headerLength)) {
                throw new RuntimeException("BUG: Length sanity check failed");
            }
            mStack.pop();
            appendUintvarInteger(headerLength);
            appendUintvarInteger(dataLength);
            mStack.copy();
        }
        return PDU_COMPOSE_SUCCESS;
    }
    static private class LengthRecordNode {
        ByteArrayOutputStream currentMessage = null;
        public int currentPosition = 0;
        public LengthRecordNode next = null;
    }
    private class PositionMarker {
        private int c_pos;   
        private int currentStackSize;  
        int getLength() {
            if (currentStackSize != mStack.stackSize) {
                throw new RuntimeException("BUG: Invalid call to getLength()");
            }
            return mPosition - c_pos;
        }
    }
    private class BufferStack {
        private LengthRecordNode stack = null;
        private LengthRecordNode toCopy = null;
        int stackSize = 0;
        void newbuf() {
            if (toCopy != null) {
                throw new RuntimeException("BUG: Invalid newbuf() before copy()");
            }
            LengthRecordNode temp = new LengthRecordNode();
            temp.currentMessage = mMessage;
            temp.currentPosition = mPosition;
            temp.next = stack;
            stack = temp;
            stackSize = stackSize + 1;
            mMessage = new ByteArrayOutputStream();
            mPosition = 0;
        }
        void pop() {
            ByteArrayOutputStream currentMessage = mMessage;
            int currentPosition = mPosition;
            mMessage = stack.currentMessage;
            mPosition = stack.currentPosition;
            toCopy = stack;
            stack = stack.next;
            stackSize = stackSize - 1;
            toCopy.currentMessage = currentMessage;
            toCopy.currentPosition = currentPosition;
        }
        void copy() {
            arraycopy(toCopy.currentMessage.toByteArray(), 0,
                    toCopy.currentPosition);
            toCopy = null;
        }
        PositionMarker mark() {
            PositionMarker m = new PositionMarker();
            m.c_pos = mPosition;
            m.currentStackSize = stackSize;
            return m;
        }
    }
    protected static int checkAddressType(String address) {
        if (null == address) {
            return PDU_UNKNOWN_ADDRESS_TYPE;
        }
        if (address.matches(REGEXP_IPV4_ADDRESS_TYPE)) {
            return PDU_IPV4_ADDRESS_TYPE;
        }else if (address.matches(REGEXP_PHONE_NUMBER_ADDRESS_TYPE)) {
            return PDU_PHONE_NUMBER_ADDRESS_TYPE;
        } else if (address.matches(REGEXP_EMAIL_ADDRESS_TYPE)) {
            return PDU_EMAIL_ADDRESS_TYPE;
        } else if (address.matches(REGEXP_IPV6_ADDRESS_TYPE)) {
            return PDU_IPV6_ADDRESS_TYPE;
        } else {
            return PDU_UNKNOWN_ADDRESS_TYPE;
        }
    }
}
