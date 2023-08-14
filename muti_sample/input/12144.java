class HttpRetryException extends IOException {
    private static final long serialVersionUID = -9186022286469111381L;
    private int responseCode;
    private String location;
    public HttpRetryException(String detail, int code) {
        super(detail);
        responseCode = code;
    }
    public HttpRetryException(String detail, int code, String location) {
        super (detail);
        responseCode = code;
        this.location = location;
    }
    public int responseCode() {
        return responseCode;
    }
    public String getReason() {
        return super.getMessage();
    }
    public String getLocation() {
        return location;
    }
}
