public class SmsMessage extends SmsMessageBase{
    static final String LOG_TAG = "GSM";
    private MessageClass messageClass;
    private int mti;
    private int protocolIdentifier;
    private int dataCodingScheme;
    private boolean replyPathPresent = false;
    private boolean automaticDeletion;
    private boolean forSubmit;
    private GsmSmsAddress recipientAddress;
    private long dischargeTimeMillis;
    private int status;
    private boolean isStatusReportMessage = false;
    public static class SubmitPdu extends SubmitPduBase {
    }
    public static SmsMessage createFromPdu(byte[] pdu) {
        try {
            SmsMessage msg = new SmsMessage();
            msg.parsePdu(pdu);
            return msg;
        } catch (RuntimeException ex) {
            Log.e(LOG_TAG, "SMS PDU parsing failed: ", ex);
            return null;
        }
    }
    public static SmsMessage newFromCMT(String[] lines) {
        try {
            SmsMessage msg = new SmsMessage();
            msg.parsePdu(IccUtils.hexStringToBytes(lines[1]));
            return msg;
        } catch (RuntimeException ex) {
            Log.e(LOG_TAG, "SMS PDU parsing failed: ", ex);
            return null;
        }
    }
    public static SmsMessage newFromCMTI(String line) {
        Log.e(LOG_TAG, "newFromCMTI: not yet supported");
        return null;
    }
    public static SmsMessage newFromCDS(String line) {
        try {
            SmsMessage msg = new SmsMessage();
            msg.parsePdu(IccUtils.hexStringToBytes(line));
            return msg;
        } catch (RuntimeException ex) {
            Log.e(LOG_TAG, "CDS SMS PDU parsing failed: ", ex);
            return null;
        }
    }
    public static SmsMessageBase newFromParcel(Parcel p){
        Log.w(LOG_TAG, "newFromParcel: is not supported in GSM mode.");
        return null;
    }
    public static SmsMessage createFromEfRecord(int index, byte[] data) {
        try {
            SmsMessage msg = new SmsMessage();
            msg.indexOnIcc = index;
            if ((data[0] & 1) == 0) {
                Log.w(LOG_TAG,
                        "SMS parsing failed: Trying to parse a free record");
                return null;
            } else {
                msg.statusOnIcc = data[0] & 0x07;
            }
            int size = data.length - 1;
            byte[] pdu = new byte[size];
            System.arraycopy(data, 1, pdu, 0, size);
            msg.parsePdu(pdu);
            return msg;
        } catch (RuntimeException ex) {
            Log.e(LOG_TAG, "SMS PDU parsing failed: ", ex);
            return null;
        }
    }
    public static int getTPLayerLengthForPDU(String pdu) {
        int len = pdu.length() / 2;
        int smscLen = 0;
        smscLen = Integer.parseInt(pdu.substring(0, 2), 16);
        return len - smscLen - 1;
    }
    public static SubmitPdu getSubmitPdu(String scAddress,
            String destinationAddress, String message,
            boolean statusReportRequested, byte[] header) {
        return getSubmitPdu(scAddress, destinationAddress, message, statusReportRequested, header,
                ENCODING_UNKNOWN);
    }
    public static SubmitPdu getSubmitPdu(String scAddress,
            String destinationAddress, String message,
            boolean statusReportRequested, byte[] header, int encoding) {
        if (message == null || destinationAddress == null) {
            return null;
        }
        SubmitPdu ret = new SubmitPdu();
        byte mtiByte = (byte)(0x01 | (header != null ? 0x40 : 0x00));
        ByteArrayOutputStream bo = getSubmitPduHead(
                scAddress, destinationAddress, mtiByte,
                statusReportRequested, ret);
        byte[] userData;
        if (encoding == ENCODING_UNKNOWN) {
            encoding = ENCODING_7BIT;
        }
        try {
            if (encoding == ENCODING_7BIT) {
                userData = GsmAlphabet.stringToGsm7BitPackedWithHeader(message, header);
            } else { 
                try {
                    userData = encodeUCS2(message, header);
                } catch(UnsupportedEncodingException uex) {
                    Log.e(LOG_TAG,
                            "Implausible UnsupportedEncodingException ",
                            uex);
                    return null;
                }
            }
        } catch (EncodeException ex) {
            try {
                userData = encodeUCS2(message, header);
                encoding = ENCODING_16BIT;
            } catch(UnsupportedEncodingException uex) {
                Log.e(LOG_TAG,
                        "Implausible UnsupportedEncodingException ",
                        uex);
                return null;
            }
        }
        if (encoding == ENCODING_7BIT) {
            if ((0xff & userData[0]) > MAX_USER_DATA_SEPTETS) {
                return null;
            }
            bo.write(0x00);
        } else { 
            if ((0xff & userData[0]) > MAX_USER_DATA_BYTES) {
                return null;
            }
            bo.write(0x0b);
        }
        bo.write(userData, 0, userData.length);
        ret.encodedMessage = bo.toByteArray();
        return ret;
    }
    private static byte[] encodeUCS2(String message, byte[] header)
        throws UnsupportedEncodingException {
        byte[] userData, textPart;
        textPart = message.getBytes("utf-16be");
        if (header != null) {
            userData = new byte[header.length + textPart.length + 1];
            userData[0] = (byte)header.length;
            System.arraycopy(header, 0, userData, 1, header.length);
            System.arraycopy(textPart, 0, userData, header.length + 1, textPart.length);
        }
        else {
            userData = textPart;
        }
        byte[] ret = new byte[userData.length+1];
        ret[0] = (byte) (userData.length & 0xff );
        System.arraycopy(userData, 0, ret, 1, userData.length);
        return ret;
    }
    public static SubmitPdu getSubmitPdu(String scAddress,
            String destinationAddress, String message,
            boolean statusReportRequested) {
        return getSubmitPdu(scAddress, destinationAddress, message, statusReportRequested, null);
    }
    public static SubmitPdu getSubmitPdu(String scAddress,
            String destinationAddress, int destinationPort, byte[] data,
            boolean statusReportRequested) {
        SmsHeader.PortAddrs portAddrs = new SmsHeader.PortAddrs();
        portAddrs.destPort = destinationPort;
        portAddrs.origPort = 0;
        portAddrs.areEightBits = false;
        SmsHeader smsHeader = new SmsHeader();
        smsHeader.portAddrs = portAddrs;
        byte[] smsHeaderData = SmsHeader.toByteArray(smsHeader);
        if ((data.length + smsHeaderData.length + 1) > MAX_USER_DATA_BYTES) {
            Log.e(LOG_TAG, "SMS data message may only contain "
                    + (MAX_USER_DATA_BYTES - smsHeaderData.length - 1) + " bytes");
            return null;
        }
        SubmitPdu ret = new SubmitPdu();
        ByteArrayOutputStream bo = getSubmitPduHead(
                scAddress, destinationAddress, (byte) 0x41, 
                statusReportRequested, ret);
        bo.write(0x04);
        bo.write(data.length + smsHeaderData.length + 1);
        bo.write(smsHeaderData.length);
        bo.write(smsHeaderData, 0, smsHeaderData.length);
        bo.write(data, 0, data.length);
        ret.encodedMessage = bo.toByteArray();
        return ret;
    }
    private static ByteArrayOutputStream getSubmitPduHead(
            String scAddress, String destinationAddress, byte mtiByte,
            boolean statusReportRequested, SubmitPdu ret) {
        ByteArrayOutputStream bo = new ByteArrayOutputStream(
                MAX_USER_DATA_BYTES + 40);
        if (scAddress == null) {
            ret.encodedScAddress = null;
        } else {
            ret.encodedScAddress = PhoneNumberUtils.networkPortionToCalledPartyBCDWithLength(
                    scAddress);
        }
        if (statusReportRequested) {
            mtiByte |= 0x20;
            if (Config.LOGD) Log.d(LOG_TAG, "SMS status report requested");
        }
        bo.write(mtiByte);
        bo.write(0);
        byte[] daBytes;
        daBytes = PhoneNumberUtils.networkPortionToCalledPartyBCD(destinationAddress);
        bo.write((daBytes.length - 1) * 2
                - ((daBytes[daBytes.length - 1] & 0xf0) == 0xf0 ? 1 : 0));
        bo.write(daBytes, 0, daBytes.length);
        bo.write(0);
        return bo;
    }
    static class PduParser {
        byte pdu[];
        int cur;
        SmsHeader userDataHeader;
        byte[] userData;
        int mUserDataSeptetPadding;
        int mUserDataSize;
        PduParser(String s) {
            this(IccUtils.hexStringToBytes(s));
        }
        PduParser(byte[] pdu) {
            this.pdu = pdu;
            cur = 0;
            mUserDataSeptetPadding = 0;
        }
        String getSCAddress() {
            int len;
            String ret;
            len = getByte();
            if (len == 0) {
                ret = null;
            } else {
                try {
                    ret = PhoneNumberUtils
                            .calledPartyBCDToString(pdu, cur, len);
                } catch (RuntimeException tr) {
                    Log.d(LOG_TAG, "invalid SC address: ", tr);
                    ret = null;
                }
            }
            cur += len;
            return ret;
        }
        int getByte() {
            return pdu[cur++] & 0xff;
        }
        GsmSmsAddress getAddress() {
            GsmSmsAddress ret;
            int addressLength = pdu[cur] & 0xff;
            int lengthBytes = 2 + (addressLength + 1) / 2;
            ret = new GsmSmsAddress(pdu, cur, lengthBytes);
            cur += lengthBytes;
            return ret;
        }
        long getSCTimestampMillis() {
            int year = IccUtils.gsmBcdByteToInt(pdu[cur++]);
            int month = IccUtils.gsmBcdByteToInt(pdu[cur++]);
            int day = IccUtils.gsmBcdByteToInt(pdu[cur++]);
            int hour = IccUtils.gsmBcdByteToInt(pdu[cur++]);
            int minute = IccUtils.gsmBcdByteToInt(pdu[cur++]);
            int second = IccUtils.gsmBcdByteToInt(pdu[cur++]);
            byte tzByte = pdu[cur++];
            int timezoneOffset = IccUtils.gsmBcdByteToInt((byte) (tzByte & (~0x08)));
            timezoneOffset = ((tzByte & 0x08) == 0) ? timezoneOffset : -timezoneOffset;
            Time time = new Time(Time.TIMEZONE_UTC);
            time.year = year >= 90 ? year + 1900 : year + 2000;
            time.month = month - 1;
            time.monthDay = day;
            time.hour = hour;
            time.minute = minute;
            time.second = second;
            return time.toMillis(true) - (timezoneOffset * 15 * 60 * 1000);
        }
        int constructUserData(boolean hasUserDataHeader, boolean dataInSeptets) {
            int offset = cur;
            int userDataLength = pdu[offset++] & 0xff;
            int headerSeptets = 0;
            int userDataHeaderLength = 0;
            if (hasUserDataHeader) {
                userDataHeaderLength = pdu[offset++] & 0xff;
                byte[] udh = new byte[userDataHeaderLength];
                System.arraycopy(pdu, offset, udh, 0, userDataHeaderLength);
                userDataHeader = SmsHeader.fromByteArray(udh);
                offset += userDataHeaderLength;
                int headerBits = (userDataHeaderLength + 1) * 8;
                headerSeptets = headerBits / 7;
                headerSeptets += (headerBits % 7) > 0 ? 1 : 0;
                mUserDataSeptetPadding = (headerSeptets * 7) - headerBits;
            }
            int bufferLen;
            if (dataInSeptets) {
                bufferLen = pdu.length - offset;
            } else {
                bufferLen = userDataLength - (hasUserDataHeader ? (userDataHeaderLength + 1) : 0);
                if (bufferLen < 0) {
                    bufferLen = 0;
                }
            }
            userData = new byte[bufferLen];
            System.arraycopy(pdu, offset, userData, 0, userData.length);
            cur = offset;
            if (dataInSeptets) {
                int count = userDataLength - headerSeptets;
                return count < 0 ? 0 : count;
            } else {
                return userData.length;
            }
        }
        byte[] getUserData() {
            return userData;
        }
        int getUserDataSeptetPadding() {
            return mUserDataSeptetPadding;
        }
        SmsHeader getUserDataHeader() {
            return userDataHeader;
        }
        String getUserDataGSM7Bit(int septetCount) {
            String ret;
            ret = GsmAlphabet.gsm7BitPackedToString(pdu, cur, septetCount,
                    mUserDataSeptetPadding);
            cur += (septetCount * 7) / 8;
            return ret;
        }
        String getUserDataUCS2(int byteCount) {
            String ret;
            try {
                ret = new String(pdu, cur, byteCount, "utf-16");
            } catch (UnsupportedEncodingException ex) {
                ret = "";
                Log.e(LOG_TAG, "implausible UnsupportedEncodingException", ex);
            }
            cur += byteCount;
            return ret;
        }
        boolean moreDataPresent() {
            return (pdu.length > cur);
        }
    }
    public static TextEncodingDetails calculateLength(CharSequence msgBody,
            boolean use7bitOnly) {
        TextEncodingDetails ted = new TextEncodingDetails();
        try {
            int septets = GsmAlphabet.countGsmSeptets(msgBody, !use7bitOnly);
            ted.codeUnitCount = septets;
            if (septets > MAX_USER_DATA_SEPTETS) {
                ted.msgCount = (septets / MAX_USER_DATA_SEPTETS_WITH_HEADER) + 1;
                ted.codeUnitsRemaining = MAX_USER_DATA_SEPTETS_WITH_HEADER
                    - (septets % MAX_USER_DATA_SEPTETS_WITH_HEADER);
            } else {
                ted.msgCount = 1;
                ted.codeUnitsRemaining = MAX_USER_DATA_SEPTETS - septets;
            }
            ted.codeUnitSize = ENCODING_7BIT;
        } catch (EncodeException ex) {
            int octets = msgBody.length() * 2;
            ted.codeUnitCount = msgBody.length();
            if (octets > MAX_USER_DATA_BYTES) {
                ted.msgCount = (octets / MAX_USER_DATA_BYTES_WITH_HEADER) + 1;
                ted.codeUnitsRemaining = (MAX_USER_DATA_BYTES_WITH_HEADER
                          - (octets % MAX_USER_DATA_BYTES_WITH_HEADER))/2;
            } else {
                ted.msgCount = 1;
                ted.codeUnitsRemaining = (MAX_USER_DATA_BYTES - octets)/2;
            }
            ted.codeUnitSize = ENCODING_16BIT;
        }
        return ted;
    }
    public int getProtocolIdentifier() {
        return protocolIdentifier;
    }
    public boolean isReplace() {
        return (protocolIdentifier & 0xc0) == 0x40
                && (protocolIdentifier & 0x3f) > 0
                && (protocolIdentifier & 0x3f) < 8;
    }
    public boolean isCphsMwiMessage() {
        return ((GsmSmsAddress) originatingAddress).isCphsVoiceMessageClear()
                || ((GsmSmsAddress) originatingAddress).isCphsVoiceMessageSet();
    }
    public boolean isMWIClearMessage() {
        if (isMwi && (mwiSense == false)) {
            return true;
        }
        return originatingAddress != null
                && ((GsmSmsAddress) originatingAddress).isCphsVoiceMessageClear();
    }
    public boolean isMWISetMessage() {
        if (isMwi && (mwiSense == true)) {
            return true;
        }
        return originatingAddress != null
                && ((GsmSmsAddress) originatingAddress).isCphsVoiceMessageSet();
    }
    public boolean isMwiDontStore() {
        if (isMwi && mwiDontStore) {
            return true;
        }
        if (isCphsMwiMessage()) {
            if (" ".equals(getMessageBody())) {
                ;
            }
            return true;
        }
        return false;
    }
    public int getStatus() {
        return status;
    }
    public boolean isStatusReportMessage() {
        return isStatusReportMessage;
    }
    public boolean isReplyPathPresent() {
        return replyPathPresent;
    }
    private void parsePdu(byte[] pdu) {
        mPdu = pdu;
        PduParser p = new PduParser(pdu);
        scAddress = p.getSCAddress();
        if (scAddress != null) {
            if (Config.LOGD) Log.d(LOG_TAG, "SMS SC address: " + scAddress);
        }
        int firstByte = p.getByte();
        mti = firstByte & 0x3;
        switch (mti) {
        case 0:
            parseSmsDeliver(p, firstByte);
            break;
        case 2:
            parseSmsStatusReport(p, firstByte);
            break;
        default:
            throw new RuntimeException("Unsupported message type");
        }
    }
    private void parseSmsStatusReport(PduParser p, int firstByte) {
        isStatusReportMessage = true;
        forSubmit = (firstByte & 0x20) == 0x00;
        messageRef = p.getByte();
        recipientAddress = p.getAddress();
        scTimeMillis = p.getSCTimestampMillis();
        dischargeTimeMillis = p.getSCTimestampMillis();
        status = p.getByte();
        if (p.moreDataPresent()) {
            int extraParams = p.getByte();
            int moreExtraParams = extraParams;
            while ((moreExtraParams & 0x80) != 0) {
                moreExtraParams = p.getByte();
            }
            if ((extraParams & 0x01) != 0) {
                protocolIdentifier = p.getByte();
            }
            if ((extraParams & 0x02) != 0) {
                dataCodingScheme = p.getByte();
            }
            if ((extraParams & 0x04) != 0) {
                boolean hasUserDataHeader = (firstByte & 0x40) == 0x40;
                parseUserData(p, hasUserDataHeader);
            }
        }
    }
    private void parseSmsDeliver(PduParser p, int firstByte) {
        replyPathPresent = (firstByte & 0x80) == 0x80;
        originatingAddress = p.getAddress();
        if (originatingAddress != null) {
            if (Config.LOGV) Log.v(LOG_TAG, "SMS originating address: "
                    + originatingAddress.address);
        }
        protocolIdentifier = p.getByte();
        dataCodingScheme = p.getByte();
        if (Config.LOGV) {
            Log.v(LOG_TAG, "SMS TP-PID:" + protocolIdentifier
                    + " data coding scheme: " + dataCodingScheme);
        }
        scTimeMillis = p.getSCTimestampMillis();
        if (Config.LOGD) Log.d(LOG_TAG, "SMS SC timestamp: " + scTimeMillis);
        boolean hasUserDataHeader = (firstByte & 0x40) == 0x40;
        parseUserData(p, hasUserDataHeader);
    }
    private void parseUserData(PduParser p, boolean hasUserDataHeader) {
        boolean hasMessageClass = false;
        boolean userDataCompressed = false;
        int encodingType = ENCODING_UNKNOWN;
        if ((dataCodingScheme & 0x80) == 0) {
            automaticDeletion = (0 != (dataCodingScheme & 0x40));
            userDataCompressed = (0 != (dataCodingScheme & 0x20));
            hasMessageClass = (0 != (dataCodingScheme & 0x10));
            if (userDataCompressed) {
                Log.w(LOG_TAG, "4 - Unsupported SMS data coding scheme "
                        + "(compression) " + (dataCodingScheme & 0xff));
            } else {
                switch ((dataCodingScheme >> 2) & 0x3) {
                case 0: 
                    encodingType = ENCODING_7BIT;
                    break;
                case 2: 
                    encodingType = ENCODING_16BIT;
                    break;
                case 1: 
                case 3: 
                    Log.w(LOG_TAG, "1 - Unsupported SMS data coding scheme "
                            + (dataCodingScheme & 0xff));
                    encodingType = ENCODING_8BIT;
                    break;
                }
            }
        } else if ((dataCodingScheme & 0xf0) == 0xf0) {
            automaticDeletion = false;
            hasMessageClass = true;
            userDataCompressed = false;
            if (0 == (dataCodingScheme & 0x04)) {
                encodingType = ENCODING_7BIT;
            } else {
                encodingType = ENCODING_8BIT;
            }
        } else if ((dataCodingScheme & 0xF0) == 0xC0
                || (dataCodingScheme & 0xF0) == 0xD0
                || (dataCodingScheme & 0xF0) == 0xE0) {
            if ((dataCodingScheme & 0xF0) == 0xE0) {
                encodingType = ENCODING_16BIT;
            } else {
                encodingType = ENCODING_7BIT;
            }
            userDataCompressed = false;
            boolean active = ((dataCodingScheme & 0x08) == 0x08);
            if ((dataCodingScheme & 0x03) == 0x00) {
                isMwi = true;
                mwiSense = active;
                mwiDontStore = ((dataCodingScheme & 0xF0) == 0xC0);
            } else {
                isMwi = false;
                Log.w(LOG_TAG, "MWI for fax, email, or other "
                        + (dataCodingScheme & 0xff));
            }
        } else {
            Log.w(LOG_TAG, "3 - Unsupported SMS data coding scheme "
                    + (dataCodingScheme & 0xff));
        }
        int count = p.constructUserData(hasUserDataHeader,
                encodingType == ENCODING_7BIT);
        this.userData = p.getUserData();
        this.userDataHeader = p.getUserDataHeader();
        switch (encodingType) {
        case ENCODING_UNKNOWN:
        case ENCODING_8BIT:
            messageBody = null;
            break;
        case ENCODING_7BIT:
            messageBody = p.getUserDataGSM7Bit(count);
            break;
        case ENCODING_16BIT:
            messageBody = p.getUserDataUCS2(count);
            break;
        }
        if (Config.LOGV) Log.v(LOG_TAG, "SMS message body (raw): '" + messageBody + "'");
        if (messageBody != null) {
            parseMessageBody();
        }
        if (!hasMessageClass) {
            messageClass = MessageClass.UNKNOWN;
        } else {
            switch (dataCodingScheme & 0x3) {
            case 0:
                messageClass = MessageClass.CLASS_0;
                break;
            case 1:
                messageClass = MessageClass.CLASS_1;
                break;
            case 2:
                messageClass = MessageClass.CLASS_2;
                break;
            case 3:
                messageClass = MessageClass.CLASS_3;
                break;
            }
        }
    }
    public MessageClass getMessageClass() {
        return messageClass;
    }
}
