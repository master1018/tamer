public class AdnRecord implements Parcelable {
    static final String LOG_TAG = "GSM";
    String alphaTag = "";
    String number = "";
    String[] emails;
    int extRecord = 0xff;
    int efid;                   
    int recordNumber;           
    static final int FOOTER_SIZE_BYTES = 14;
    static final int MAX_NUMBER_SIZE_BYTES = 11;
    static final int EXT_RECORD_LENGTH_BYTES = 13;
    static final int EXT_RECORD_TYPE_ADDITIONAL_DATA = 2;
    static final int EXT_RECORD_TYPE_MASK = 3;
    static final int MAX_EXT_CALLED_PARTY_LENGTH = 0xa;
    static final int ADN_BCD_NUMBER_LENGTH = 0;
    static final int ADN_TON_AND_NPI = 1;
    static final int ADN_DAILING_NUMBER_START = 2;
    static final int ADN_DAILING_NUMBER_END = 11;
    static final int ADN_CAPABILITY_ID = 12;
    static final int ADN_EXTENSION_ID = 13;
    public static final Parcelable.Creator<AdnRecord> CREATOR
            = new Parcelable.Creator<AdnRecord>() {
        public AdnRecord createFromParcel(Parcel source) {
            int efid;
            int recordNumber;
            String alphaTag;
            String number;
            String[] emails;
            efid = source.readInt();
            recordNumber = source.readInt();
            alphaTag = source.readString();
            number = source.readString();
            emails = source.readStringArray();
            return new AdnRecord(efid, recordNumber, alphaTag, number, emails);
        }
        public AdnRecord[] newArray(int size) {
            return new AdnRecord[size];
        }
    };
    public AdnRecord (byte[] record) {
        this(0, 0, record);
    }
    public AdnRecord (int efid, int recordNumber, byte[] record) {
        this.efid = efid;
        this.recordNumber = recordNumber;
        parseRecord(record);
    }
    public AdnRecord (String alphaTag, String number) {
        this(0, 0, alphaTag, number);
    }
    public AdnRecord (String alphaTag, String number, String[] emails) {
        this(0, 0, alphaTag, number, emails);
    }
    public AdnRecord (int efid, int recordNumber, String alphaTag, String number, String[] emails) {
        this.efid = efid;
        this.recordNumber = recordNumber;
        this.alphaTag = alphaTag;
        this.number = number;
        this.emails = emails;
    }
    public AdnRecord(int efid, int recordNumber, String alphaTag, String number) {
        this.efid = efid;
        this.recordNumber = recordNumber;
        this.alphaTag = alphaTag;
        this.number = number;
        this.emails = null;
    }
    public String getAlphaTag() {
        return alphaTag;
    }
    public String getNumber() {
        return number;
    }
    public String[] getEmails() {
        return emails;
    }
    public void setEmails(String[] emails) {
        this.emails = emails;
    }
    public String toString() {
        return "ADN Record '" + alphaTag + "' '" + number + " " + emails + "'";
    }
    public boolean isEmpty() {
        return alphaTag.equals("") && number.equals("") && emails == null;
    }
    public boolean hasExtendedRecord() {
        return extRecord != 0 && extRecord != 0xff;
    }
    public boolean isEqual(AdnRecord adn) {
        return ( alphaTag.equals(adn.getAlphaTag()) &&
                number.equals(adn.getNumber()) &&
                Arrays.equals(emails, adn.getEmails()));
    }
    public int describeContents() {
        return 0;
    }
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(efid);
        dest.writeInt(recordNumber);
        dest.writeString(alphaTag);
        dest.writeString(number);
        dest.writeStringArray(emails);
    }
    public byte[] buildAdnString(int recordSize) {
        byte[] bcdNumber;
        byte[] byteTag;
        byte[] adnString = null;
        int footerOffset = recordSize - FOOTER_SIZE_BYTES;
        if (number == null || number.equals("") ||
                alphaTag == null || alphaTag.equals("")) {
            Log.w(LOG_TAG, "[buildAdnString] Empty alpha tag or number");
            adnString = new byte[recordSize];
            for (int i = 0; i < recordSize; i++) {
                adnString[i] = (byte) 0xFF;
            }
        } else if (number.length()
                > (ADN_DAILING_NUMBER_END - ADN_DAILING_NUMBER_START + 1) * 2) {
            Log.w(LOG_TAG,
                    "[buildAdnString] Max length of dailing number is 20");
        } else if (alphaTag.length() > footerOffset) {
            Log.w(LOG_TAG,
                    "[buildAdnString] Max length of tag is " + footerOffset);
        } else {
            adnString = new byte[recordSize];
            for (int i = 0; i < recordSize; i++) {
                adnString[i] = (byte) 0xFF;
            }
            bcdNumber = PhoneNumberUtils.numberToCalledPartyBCD(number);
            System.arraycopy(bcdNumber, 0, adnString,
                    footerOffset + ADN_TON_AND_NPI, bcdNumber.length);
            adnString[footerOffset + ADN_BCD_NUMBER_LENGTH]
                    = (byte) (bcdNumber.length);
            adnString[footerOffset + ADN_CAPABILITY_ID]
                    = (byte) 0xFF; 
            adnString[footerOffset + ADN_EXTENSION_ID]
                    = (byte) 0xFF; 
            byteTag = GsmAlphabet.stringToGsm8BitPacked(alphaTag);
            System.arraycopy(byteTag, 0, adnString, 0, byteTag.length);
        }
        return adnString;
    }
    public void
    appendExtRecord (byte[] extRecord) {
        try {
            if (extRecord.length != EXT_RECORD_LENGTH_BYTES) {
                return;
            }
            if ((extRecord[0] & EXT_RECORD_TYPE_MASK)
                    != EXT_RECORD_TYPE_ADDITIONAL_DATA) {
                return;
            }
            if ((0xff & extRecord[1]) > MAX_EXT_CALLED_PARTY_LENGTH) {
                return;
            }
            number += PhoneNumberUtils.calledPartyBCDFragmentToString(
                                        extRecord, 2, 0xff & extRecord[1]);
        } catch (RuntimeException ex) {
            Log.w(LOG_TAG, "Error parsing AdnRecord ext record", ex);
        }
    }
    private void
    parseRecord(byte[] record) {
        try {
            alphaTag = IccUtils.adnStringFieldToString(
                            record, 0, record.length - FOOTER_SIZE_BYTES);
            int footerOffset = record.length - FOOTER_SIZE_BYTES;
            int numberLength = 0xff & record[footerOffset];
            if (numberLength > MAX_NUMBER_SIZE_BYTES) {
                number = "";
                return;
            }
            number = PhoneNumberUtils.calledPartyBCDToString(
                            record, footerOffset + 1, numberLength);
            extRecord = 0xff & record[record.length - 1];
            emails = null;
        } catch (RuntimeException ex) {
            Log.w(LOG_TAG, "Error parsing AdnRecord", ex);
            number = "";
            alphaTag = "";
            emails = null;
        }
    }
}
