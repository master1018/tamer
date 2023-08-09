public class NotificationInd extends GenericPdu {
    public NotificationInd() throws InvalidHeaderValueException {
        super();
        setMessageType(PduHeaders.MESSAGE_TYPE_NOTIFICATION_IND);
    }
    NotificationInd(PduHeaders headers) {
        super(headers);
    }
    public int getContentClass() {
        return mPduHeaders.getOctet(PduHeaders.CONTENT_CLASS);
    }
    public void setContentClass(int value) throws InvalidHeaderValueException {
        mPduHeaders.setOctet(value, PduHeaders.CONTENT_CLASS);
    }
    public byte[] getContentLocation() {
        return mPduHeaders.getTextString(PduHeaders.CONTENT_LOCATION);
    }
    public void setContentLocation(byte[] value) {
        mPduHeaders.setTextString(value, PduHeaders.CONTENT_LOCATION);
    }
    public long getExpiry() {
        return mPduHeaders.getLongInteger(PduHeaders.EXPIRY);
    }
    public void setExpiry(long value) {
        mPduHeaders.setLongInteger(value, PduHeaders.EXPIRY);
    }
    public EncodedStringValue getFrom() {
       return mPduHeaders.getEncodedStringValue(PduHeaders.FROM);
    }
    public void setFrom(EncodedStringValue value) {
        mPduHeaders.setEncodedStringValue(value, PduHeaders.FROM);
    }
    public byte[] getMessageClass() {
        return mPduHeaders.getTextString(PduHeaders.MESSAGE_CLASS);
    }
    public void setMessageClass(byte[] value) {
        mPduHeaders.setTextString(value, PduHeaders.MESSAGE_CLASS);
    }
    public long getMessageSize() {
        return mPduHeaders.getLongInteger(PduHeaders.MESSAGE_SIZE);
    }
    public void setMessageSize(long value) {
        mPduHeaders.setLongInteger(value, PduHeaders.MESSAGE_SIZE);
    }
    public EncodedStringValue getSubject() {
        return mPduHeaders.getEncodedStringValue(PduHeaders.SUBJECT);
    }
    public void setSubject(EncodedStringValue value) {
        mPduHeaders.setEncodedStringValue(value, PduHeaders.SUBJECT);
    }
    public byte[] getTransactionId() {
        return mPduHeaders.getTextString(PduHeaders.TRANSACTION_ID);
    }
    public void setTransactionId(byte[] value) {
        mPduHeaders.setTextString(value, PduHeaders.TRANSACTION_ID);
    }
    public int getDeliveryReport() {
        return mPduHeaders.getOctet(PduHeaders.DELIVERY_REPORT);
    }
    public void setDeliveryReport(int value) throws InvalidHeaderValueException {
        mPduHeaders.setOctet(value, PduHeaders.DELIVERY_REPORT);
    }
}
