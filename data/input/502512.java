public class MultimediaMessagePdu extends GenericPdu{
    private PduBody mMessageBody;
    public MultimediaMessagePdu() {
        super();
    }
    public MultimediaMessagePdu(PduHeaders header, PduBody body) {
        super(header);
        mMessageBody = body;
    }
    MultimediaMessagePdu(PduHeaders headers) {
        super(headers);
    }
    public PduBody getBody() {
        return mMessageBody;
    }
    public void setBody(PduBody body) {
        mMessageBody = body;
    }
    public EncodedStringValue getSubject() {
        return mPduHeaders.getEncodedStringValue(PduHeaders.SUBJECT);
    }
    public void setSubject(EncodedStringValue value) {
        mPduHeaders.setEncodedStringValue(value, PduHeaders.SUBJECT);
    }
    public EncodedStringValue[] getTo() {
        return mPduHeaders.getEncodedStringValues(PduHeaders.TO);
    }
    public void addTo(EncodedStringValue value) {
        mPduHeaders.appendEncodedStringValue(value, PduHeaders.TO);
    }
    public int getPriority() {
        return mPduHeaders.getOctet(PduHeaders.PRIORITY);
    }
    public void setPriority(int value) throws InvalidHeaderValueException {
        mPduHeaders.setOctet(value, PduHeaders.PRIORITY);
    }
    public long getDate() {
        return mPduHeaders.getLongInteger(PduHeaders.DATE);
    }
    public void setDate(long value) {
        mPduHeaders.setLongInteger(value, PduHeaders.DATE);
    }
}
