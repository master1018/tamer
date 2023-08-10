public class ICCPMsgReplyThread implements Runnable {
    private long timeout;
    private String messageID;
    public ICCPMsgReplyThread(ICCPMsgReplyObject replyObject) {
        this.timeout = replyObject.getTimeout();
        this.messageID = replyObject.getMessageID();
    }
    public void run() {
        try {
            Thread.sleep(timeout);
        } catch (Exception e) {
        }
        ICCPMsgReplyQueue.fireTimeoutEvent(this.messageID);
    }
}
