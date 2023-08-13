public class NotifyRespInd extends GenericPdu {
    public NotifyRespInd(int mmsVersion,
                         byte[] transactionId,
                         int status) throws InvalidHeaderValueException {
        super();
        setMessageType(PduHeaders.MESSAGE_TYPE_NOTIFYRESP_IND);
        setMmsVersion(mmsVersion);
        setTransactionId(transactionId);
        setStatus(status);
    }
    NotifyRespInd(PduHeaders headers) {
        super(headers);
    }
    public int getReportAllowed() {
        return mPduHeaders.getOctet(PduHeaders.REPORT_ALLOWED);
    }
    public void setReportAllowed(int value) throws InvalidHeaderValueException {
        mPduHeaders.setOctet(value, PduHeaders.REPORT_ALLOWED);
    }
    public void setStatus(int value) throws InvalidHeaderValueException {
        mPduHeaders.setOctet(value, PduHeaders.STATUS);
    }
    public int getStatus() {
        return mPduHeaders.getOctet(PduHeaders.STATUS);
    }
    public byte[] getTransactionId() {
        return mPduHeaders.getTextString(PduHeaders.TRANSACTION_ID);
    }
    public void setTransactionId(byte[] value) {
            mPduHeaders.setTextString(value, PduHeaders.TRANSACTION_ID);
    }
}
