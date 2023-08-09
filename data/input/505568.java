public class DefaultHttpRequestRetryHandler implements HttpRequestRetryHandler {
    private final int retryCount;
    private final boolean requestSentRetryEnabled;
    public DefaultHttpRequestRetryHandler(int retryCount, boolean requestSentRetryEnabled) {
        super();
        this.retryCount = retryCount;
        this.requestSentRetryEnabled = requestSentRetryEnabled;
    }
    public DefaultHttpRequestRetryHandler() {
        this(3, false);
    }
    public boolean retryRequest(
            final IOException exception, 
            int executionCount,
            final HttpContext context) {
        if (exception == null) {
            throw new IllegalArgumentException("Exception parameter may not be null");
        }
        if (context == null) {
            throw new IllegalArgumentException("HTTP context may not be null");
        }
        if (executionCount > this.retryCount) {
            return false;
        }
        if (exception instanceof NoHttpResponseException) {
            return true;
        }
        if (exception instanceof InterruptedIOException) {
            return false;
        }
        if (exception instanceof UnknownHostException) {
            return false;
        }
        if (exception instanceof SSLHandshakeException) {
            return false;
        }
        Boolean b = (Boolean)
            context.getAttribute(ExecutionContext.HTTP_REQ_SENT);
        boolean sent = (b != null && b.booleanValue());
        if (!sent || this.requestSentRetryEnabled) {
            return true;
        }
        return false;
    }
    public boolean isRequestSentRetryEnabled() {
        return requestSentRetryEnabled;
    }
    public int getRetryCount() {
        return retryCount;
    }
}
