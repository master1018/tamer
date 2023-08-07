public class AeReply {
    private IAeReplyReceiver mReplyReceiver;
    private long mProcessId;
    private long mReplyId;
    private IAeMessageData mMessageData;
    private IAeFault mFault;
    private String mReceiverPath;
    public AeReply(long aProcessId, long aReplyId) {
        this(aProcessId, aReplyId, null);
    }
    public AeReply(long aProcessId, long aReplyId, IAeReplyReceiver aReplyReceiver) {
        setProcessId(aProcessId);
        setReplyReceiver(aReplyReceiver);
        setReplyId(aReplyId);
    }
    public long getReplyId() {
        return mReplyId;
    }
    public void setReplyId(long aReplyId) {
        mReplyId = aReplyId;
    }
    public IAeReplyReceiver getReplyReceiver() {
        return mReplyReceiver;
    }
    public void setReplyReceiver(IAeReplyReceiver aReplyReceiver) {
        mReplyReceiver = aReplyReceiver;
    }
    public void setProcessId(long processId) {
        mProcessId = processId;
    }
    public long getProcessId() {
        return mProcessId;
    }
    public boolean equals(Object aObject) {
        if (aObject instanceof AeReply) {
            AeReply other = (AeReply) aObject;
            return getReplyId() == other.getReplyId();
        }
        return false;
    }
    public int hashCode() {
        return new Long(getReplyId()).hashCode();
    }
    public IAeMessageData getMessageData() {
        return mMessageData;
    }
    public void setMessageData(IAeMessageData aMessageData) {
        mMessageData = aMessageData;
    }
    public IAeFault getFault() {
        return mFault;
    }
    public void setFault(IAeFault aFault) {
        mFault = aFault;
    }
    public String getReceiverPath() {
        return mReceiverPath;
    }
    public void setReceiverPath(String aReceiverPath) {
        mReceiverPath = aReceiverPath;
    }
}
