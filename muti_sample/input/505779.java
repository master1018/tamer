public class ServerRequestHandler {
    private long mConnectionId;
    protected ServerRequestHandler() {
        mConnectionId = -1;
    }
    public void setConnectionId(final long connectionId) {
        if ((connectionId < -1) || (connectionId > 0xFFFFFFFFL)) {
            throw new IllegalArgumentException("Illegal Connection ID");
        }
        mConnectionId = connectionId;
    }
    public long getConnectionId() {
        return mConnectionId;
    }
    public int onConnect(HeaderSet request, HeaderSet reply) {
        return ResponseCodes.OBEX_HTTP_OK;
    }
    public void onDisconnect(HeaderSet request, HeaderSet reply) {
    }
    public int onSetPath(HeaderSet request, HeaderSet reply, boolean backup, boolean create) {
        return ResponseCodes.OBEX_HTTP_NOT_IMPLEMENTED;
    }
    public int onDelete(HeaderSet request, HeaderSet reply) {
        return ResponseCodes.OBEX_HTTP_NOT_IMPLEMENTED;
    }
    public int onAbort(HeaderSet request, HeaderSet reply) {
        return ResponseCodes.OBEX_HTTP_NOT_IMPLEMENTED;
    }
    public int onPut(Operation operation) {
        return ResponseCodes.OBEX_HTTP_NOT_IMPLEMENTED;
    }
    public int onGet(Operation operation) {
        return ResponseCodes.OBEX_HTTP_NOT_IMPLEMENTED;
    }
    public void onAuthenticationFailure(byte[] userName) {
    }
    public void updateStatus(String message) {
    }
    public void onClose() {
    }
}
