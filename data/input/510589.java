public class DeliveryInd extends GenericPdu {
    public DeliveryInd() throws InvalidHeaderValueException {
        super();
        setMessageType(PduHeaders.MESSAGE_TYPE_DELIVERY_IND);
    }
    DeliveryInd(PduHeaders headers) {
        super(headers);
    }
    public long getDate() {
        return mPduHeaders.getLongInteger(PduHeaders.DATE);
    }
    public void setDate(long value) {
        mPduHeaders.setLongInteger(value, PduHeaders.DATE);
    }
    public byte[] getMessageId() {
        return mPduHeaders.getTextString(PduHeaders.MESSAGE_ID);
    }
    public void setMessageId(byte[] value) {
        mPduHeaders.setTextString(value, PduHeaders.MESSAGE_ID);
    }
    public int getStatus() {
        return mPduHeaders.getOctet(PduHeaders.STATUS);
    }
    public void setStatus(int value) throws InvalidHeaderValueException {
        mPduHeaders.setOctet(value, PduHeaders.STATUS);
    }
    public EncodedStringValue[] getTo() {
        return mPduHeaders.getEncodedStringValues(PduHeaders.TO);
    }
    public void setTo(EncodedStringValue[] value) {
        mPduHeaders.setEncodedStringValues(value, PduHeaders.TO);
    }
}
