public class GsmSmsAddress extends SmsAddress {
    static final int OFFSET_ADDRESS_LENGTH = 0;
    static final int OFFSET_TOA = 1;
    static final int OFFSET_ADDRESS_VALUE = 2;
    public GsmSmsAddress(byte[] data, int offset, int length) {
        origBytes = new byte[length];
        System.arraycopy(data, offset, origBytes, 0, length);
        int addressLength = origBytes[OFFSET_ADDRESS_LENGTH] & 0xff;
        int toa = origBytes[OFFSET_TOA] & 0xff;
        ton = 0x7 & (toa >> 4);
        if ((toa & 0x80) != 0x80) {
            throw new RuntimeException("Invalid TOA - high bit must be set");
        }
        if (isAlphanumeric()) {
            int countSeptets = addressLength * 4 / 7;
            address = GsmAlphabet.gsm7BitPackedToString(origBytes,
                    OFFSET_ADDRESS_VALUE, countSeptets);
        } else {
            byte lastByte = origBytes[length - 1];
            if ((addressLength & 1) == 1) {
                origBytes[length - 1] |= 0xf0;
            }
            address = PhoneNumberUtils.calledPartyBCDToString(origBytes,
                    OFFSET_TOA, length - OFFSET_TOA);
            origBytes[length - 1] = lastByte;
        }
    }
    public String getAddressString() {
        return address;
    }
    public boolean isAlphanumeric() {
        return ton == TON_ALPHANUMERIC;
    }
    public boolean isNetworkSpecific() {
        return ton == TON_NETWORK;
    }
    public boolean isCphsVoiceMessageIndicatorAddress() {
        return (origBytes[OFFSET_ADDRESS_LENGTH] & 0xff) == 4
                && isAlphanumeric() && (origBytes[OFFSET_TOA] & 0x0f) == 0;
    }
    public boolean isCphsVoiceMessageSet() {
        return isCphsVoiceMessageIndicatorAddress()
                && (origBytes[OFFSET_ADDRESS_VALUE] & 0xff) == 0x11;
    }
    public boolean isCphsVoiceMessageClear() {
        return isCphsVoiceMessageIndicatorAddress()
                && (origBytes[OFFSET_ADDRESS_VALUE] & 0xff) == 0x10;
    }
}
