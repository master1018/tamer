final class LdapRequest {
    LdapRequest next;   
    int msgId;          
    private int gotten = 0;
    private BlockingQueue<BerDecoder> replies;
    private int highWatermark = -1;
    private boolean cancelled = false;
    private boolean pauseAfterReceipt = false;
    private boolean completed = false;
    LdapRequest(int msgId, boolean pause) {
        this(msgId, pause, -1);
    }
    LdapRequest(int msgId, boolean pause, int replyQueueCapacity) {
        this.msgId = msgId;
        this.pauseAfterReceipt = pause;
        if (replyQueueCapacity == -1) {
            this.replies = new LinkedBlockingQueue<BerDecoder>();
        } else {
            this.replies =
                new LinkedBlockingQueue<BerDecoder>(replyQueueCapacity);
            highWatermark = (replyQueueCapacity * 80) / 100; 
        }
    }
    synchronized void cancel() {
        cancelled = true;
        notify();
    }
    synchronized boolean addReplyBer(BerDecoder ber) {
        if (cancelled) {
            return false;
        }
        try {
            replies.put(ber);
        } catch (InterruptedException e) {
        }
        try {
            ber.parseSeq(null);
            ber.parseInt();
            completed = (ber.peekByte() == LdapClient.LDAP_REP_RESULT);
        } catch (IOException e) {
        }
        ber.reset();
        notify(); 
        if (highWatermark != -1 && replies.size() >= highWatermark) {
            return true; 
        }
        return pauseAfterReceipt;
    }
    synchronized BerDecoder getReplyBer() throws CommunicationException {
        if (cancelled) {
            throw new CommunicationException("Request: " + msgId +
                " cancelled");
        }
        BerDecoder reply = replies.poll();
        return reply;
    }
    synchronized boolean hasSearchCompleted() {
        return completed;
    }
}
