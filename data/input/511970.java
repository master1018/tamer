public class SendConf extends GenericPdu {
    public SendConf() throws InvalidHeaderValueException {
        super();
        setMessageType(PduHeaders.MESSAGE_TYPE_SEND_CONF);
    }
    SendConf(PduHeaders headers) {
        super(headers);
    }
    public byte[] getMessageId() {
        return mPduHeaders.getTextString(PduHeaders.MESSAGE_ID);
    }
    public void setMessageId(byte[] value) {
        mPduHeaders.setTextString(value, PduHeaders.MESSAGE_ID);
    }
    public int getResponseStatus() {
        return mPduHeaders.getOctet(PduHeaders.RESPONSE_STATUS);
    }
    public void setResponseStatus(int value) throws InvalidHeaderValueException {
        mPduHeaders.setOctet(value, PduHeaders.RESPONSE_STATUS);
    }
    public byte[] getTransactionId() {
        return mPduHeaders.getTextString(PduHeaders.TRANSACTION_ID);
    }
    public void setTransactionId(byte[] value) {
            mPduHeaders.setTextString(value, PduHeaders.TRANSACTION_ID);
    }
}
