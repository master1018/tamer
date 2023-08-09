public class RequestCanceledException extends RuntimeException {
    private int requestId = 0;
    public RequestCanceledException(int requestId) {
        this.requestId = requestId;
    }
    public int getRequestId() {
        return this.requestId;
    }
}
