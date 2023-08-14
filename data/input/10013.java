public abstract class AsyncCloseTest {
    public abstract String description();
    public abstract boolean go() throws Exception;
    protected synchronized void failed(String reason) {
        this.reason = reason;
    }
    public synchronized String failureReason() {
        return reason;
    }
    protected synchronized void closed() {
        closed = true;
    }
    protected synchronized boolean isClosed() {
        return closed;
    }
    private String reason;
    private boolean closed;
}
