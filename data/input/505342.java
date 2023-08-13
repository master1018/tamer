class VpnConnectingError extends IOException {
    private int mErrorCode;
    VpnConnectingError(int errorCode) {
        super("Connecting error: " + errorCode);
        mErrorCode = errorCode;
    }
    int getErrorCode() {
        return mErrorCode;
    }
}
