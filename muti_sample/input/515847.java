public abstract class SmsMessageBase {
    private static final String LOG_TAG = "SMS";
    protected String scAddress;
    protected SmsAddress originatingAddress;
    protected String messageBody;
    protected String pseudoSubject;
    protected String emailFrom;
    protected String emailBody;
    protected boolean isEmail;
    protected long scTimeMillis;
    protected byte[] mPdu;
    protected byte[] userData;
    protected SmsHeader userDataHeader;
    protected boolean isMwi;
    protected boolean mwiSense;
    protected boolean mwiDontStore;
    protected int statusOnIcc = -1;
    protected int indexOnIcc = -1;
    public int messageRef;
    public static class TextEncodingDetails {
        public int msgCount;
        public int codeUnitCount;
        public int codeUnitsRemaining;
        public int codeUnitSize;
        @Override
        public String toString() {
            return "TextEncodingDetails " +
                    "{ msgCount=" + msgCount +
                    ", codeUnitCount=" + codeUnitCount +
                    ", codeUnitsRemaining=" + codeUnitsRemaining +
                    ", codeUnitSize=" + codeUnitSize +
                    " }";
        }
    }
    public static abstract class SubmitPduBase  {
        public byte[] encodedScAddress; 
        public byte[] encodedMessage;
        public String toString() {
            return "SubmitPdu: encodedScAddress = "
                    + Arrays.toString(encodedScAddress)
                    + ", encodedMessage = "
                    + Arrays.toString(encodedMessage);
        }
    }
    public String getServiceCenterAddress() {
        return scAddress;
    }
    public String getOriginatingAddress() {
        if (originatingAddress == null) {
            return null;
        }
        return originatingAddress.getAddressString();
    }
    public String getDisplayOriginatingAddress() {
        if (isEmail) {
            return emailFrom;
        } else {
            return getOriginatingAddress();
        }
    }
    public String getMessageBody() {
        return messageBody;
    }
    public abstract MessageClass getMessageClass();
    public String getDisplayMessageBody() {
        if (isEmail) {
            return emailBody;
        } else {
            return getMessageBody();
        }
    }
    public String getPseudoSubject() {
        return pseudoSubject == null ? "" : pseudoSubject;
    }
    public long getTimestampMillis() {
        return scTimeMillis;
    }
    public boolean isEmail() {
        return isEmail;
    }
    public String getEmailBody() {
        return emailBody;
    }
    public String getEmailFrom() {
        return emailFrom;
    }
    public abstract int getProtocolIdentifier();
    public abstract boolean isReplace();
    public abstract boolean isCphsMwiMessage();
    public abstract boolean isMWIClearMessage();
    public abstract boolean isMWISetMessage();
    public abstract boolean isMwiDontStore();
    public byte[] getUserData() {
        return userData;
    }
    public SmsHeader getUserDataHeader() {
        return userDataHeader;
    }
    public byte[] getPdu() {
        return mPdu;
    }
    public abstract int getStatus();
    public abstract boolean isStatusReportMessage();
    public abstract boolean isReplyPathPresent();
    public int getStatusOnIcc() {
        return statusOnIcc;
    }
    public int getIndexOnIcc() {
        return indexOnIcc;
    }
    protected void parseMessageBody() {
        if (originatingAddress != null && originatingAddress.couldBeEmailGateway()) {
            extractEmailAddressFromMessageBody();
        }
    }
    protected void extractEmailAddressFromMessageBody() {
         String[] parts = messageBody.split("( /)|( )", 2);
         if (parts.length < 2) return;
         emailFrom = parts[0];
         emailBody = parts[1];
         isEmail = Telephony.Mms.isEmailAddress(emailFrom);
    }
}
