public class AcknowledgeInd extends GenericPdu {
    public AcknowledgeInd(int mmsVersion, byte[] transactionId)
            throws InvalidHeaderValueException {
        super();
        setMessageType(PduHeaders.MESSAGE_TYPE_ACKNOWLEDGE_IND);
        setMmsVersion(mmsVersion);
        setTransactionId(transactionId);
    }
    AcknowledgeInd(PduHeaders headers) {
        super(headers);
    }
    public int getReportAllowed() {
        return mPduHeaders.getOctet(PduHeaders.REPORT_ALLOWED);
    }
    public void setReportAllowed(int value) throws InvalidHeaderValueException {
        mPduHeaders.setOctet(value, PduHeaders.REPORT_ALLOWED);
    }
    public byte[] getTransactionId() {
        return mPduHeaders.getTextString(PduHeaders.TRANSACTION_ID);
    }
    public void setTransactionId(byte[] value) {
        mPduHeaders.setTextString(value, PduHeaders.TRANSACTION_ID);
    }
}
