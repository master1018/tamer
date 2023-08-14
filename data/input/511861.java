public class WspTypeDecoder {
    private static final int WAP_PDU_SHORT_LENGTH_MAX = 30;
    private static final int WAP_PDU_LENGTH_QUOTE = 31;
    public static final int PDU_TYPE_PUSH = 0x06;
    public static final int PDU_TYPE_CONFIRMED_PUSH = 0x07;
    public static final int CONTENT_TYPE_B_DRM_RIGHTS_XML = 0x4a;
    public static final int CONTENT_TYPE_B_DRM_RIGHTS_WBXML = 0x4b;
    public static final int CONTENT_TYPE_B_PUSH_SI = 0x2e;
    public static final int CONTENT_TYPE_B_PUSH_SL = 0x30;
    public static final int CONTENT_TYPE_B_PUSH_CO = 0x32;
    public static final int CONTENT_TYPE_B_MMS = 0x3e;
    public static final int CONTENT_TYPE_B_VND_DOCOMO_PF = 0x0310;
    public static final String CONTENT_MIME_TYPE_B_DRM_RIGHTS_XML =
            "application/vnd.oma.drm.rights+xml";
    public static final String CONTENT_MIME_TYPE_B_DRM_RIGHTS_WBXML =
            "application/vnd.oma.drm.rights+wbxml";
    public static final String CONTENT_MIME_TYPE_B_PUSH_SI = "application/vnd.wap.sic";
    public static final String CONTENT_MIME_TYPE_B_PUSH_SL = "application/vnd.wap.slc";
    public static final String CONTENT_MIME_TYPE_B_PUSH_CO = "application/vnd.wap.coc";
    public static final String CONTENT_MIME_TYPE_B_MMS = "application/vnd.wap.mms-message";
    public static final String CONTENT_MIME_TYPE_B_VND_DOCOMO_PF = "application/vnd.docomo.pf";
    public static final int PARAMETER_ID_X_WAP_APPLICATION_ID = 0x2f;
    byte[] wspData;
    int    dataLength;
    long   unsigned32bit;
    String stringValue;
    public WspTypeDecoder(byte[] pdu) {
        wspData = pdu;
    }
    public boolean decodeTextString(int startIndex) {
        int index = startIndex;
        while (wspData[index] != 0) {
            index++;
        }
        dataLength  = index - startIndex + 1;
        if (wspData[startIndex] == 127) {
            stringValue = new String(wspData, startIndex+1, dataLength - 2);
        } else {
            stringValue = new String(wspData, startIndex, dataLength - 1);
        }
        return true;
    }
    public boolean decodeShortInteger(int startIndex) {
        if ((wspData[startIndex] & 0x80) == 0) {
            return false;
        }
        unsigned32bit = wspData[startIndex] & 0x7f;
        dataLength = 1;
        return true;
    }
    public boolean decodeLongInteger(int startIndex) {
        int lengthMultiOctet = wspData[startIndex] & 0xff;
        if (lengthMultiOctet > WAP_PDU_SHORT_LENGTH_MAX) {
            return false;
        }
        unsigned32bit = 0;
        for (int i=1; i<=lengthMultiOctet; i++) {
            unsigned32bit = (unsigned32bit << 8) | (wspData[startIndex+i] & 0xff);
        }
        dataLength = 1+lengthMultiOctet;
        return true;
    }
    public boolean decodeIntegerValue(int startIndex) {
        if (decodeShortInteger(startIndex) == true) {
            return true;
        }
        return decodeLongInteger(startIndex);
    }
    public boolean decodeUintvarInteger(int startIndex) {
        int  index = startIndex;
        unsigned32bit = 0;
        while ((wspData[index] & 0x80) != 0) {
            if ((index - startIndex) >= 4) {
                return false;
            }
            unsigned32bit = (unsigned32bit << 7) | (wspData[index] & 0x7f);
            index++;
        }
        unsigned32bit = (unsigned32bit << 7) | (wspData[index] & 0x7f);
        dataLength = index - startIndex + 1;
        return true;
    }
    public boolean decodeValueLength(int startIndex) {
        if ((wspData[startIndex] & 0xff) > WAP_PDU_LENGTH_QUOTE) {
            return false;
        }
        if (wspData[startIndex] < WAP_PDU_LENGTH_QUOTE) {
            unsigned32bit = wspData[startIndex];
            dataLength = 1;
        } else {
            decodeUintvarInteger(startIndex+1);
            dataLength ++;
        }
        return true;
    }
    public boolean decodeExtensionMedia(int startIndex) {
        int index = startIndex;
        dataLength = 0;
        stringValue = null;
        int length = wspData.length;
        boolean rtrn = index < length;
        while (index < length && wspData[index] != 0) {
            index++;
        }
        dataLength  = index - startIndex + 1;
        stringValue = new String(wspData, startIndex, dataLength - 1);
        return rtrn;
    }
    public boolean decodeConstrainedEncoding(int startIndex) {
        if (decodeShortInteger(startIndex) == true) {
            stringValue = null;
            return true;
        }
        return decodeExtensionMedia(startIndex);
    }
    public boolean decodeContentType(int startIndex) {
        int mediaPrefixLength;
        long mediaFieldLength;
        if (decodeValueLength(startIndex) == false) {
            return decodeConstrainedEncoding(startIndex);
        }
        mediaPrefixLength = getDecodedDataLength();
        mediaFieldLength = getValue32();
        if (decodeIntegerValue(startIndex + mediaPrefixLength) == true) {
            dataLength += mediaPrefixLength;
            stringValue = null;
            return true;
        }
        if (decodeExtensionMedia(startIndex + mediaPrefixLength) == true) {
            dataLength += mediaPrefixLength;
            return true;
        }
        return false;
    }
    public boolean decodeContentLength(int startIndex) {
        return decodeIntegerValue(startIndex);
    }
    public boolean decodeContentLocation(int startIndex) {
        return decodeTextString(startIndex);
    }
    public boolean decodeXWapApplicationId(int startIndex) {
        if (decodeIntegerValue(startIndex) == true) {
            stringValue = null;
            return true;
        }
        return decodeTextString(startIndex);
    }
    public boolean decodeXWapContentURI(int startIndex) {
        return decodeTextString(startIndex);
    }
    public boolean decodeXWapInitiatorURI(int startIndex) {
        return decodeTextString(startIndex);
    }
    public int getDecodedDataLength() {
        return dataLength;
    }
    public long getValue32() {
        return unsigned32bit;
    }
    public String getValueString() {
        return stringValue;
    }
}
