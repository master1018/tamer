public class HttpResponseException extends ClientProtocolException {
    private static final long serialVersionUID = -7186627969477257933L;
    private final int statusCode;
    public HttpResponseException(int statusCode, final String s) {
        super(s);
        this.statusCode = statusCode;
    }
    public int getStatusCode() {
        return this.statusCode;
    }
}
