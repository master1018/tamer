public class SSLEngineResult {
    public static enum Status {
        BUFFER_UNDERFLOW,
        BUFFER_OVERFLOW,
        OK,
        CLOSED;
    }
    public static enum HandshakeStatus {
        NOT_HANDSHAKING,
        FINISHED,
        NEED_TASK,
        NEED_WRAP,
        NEED_UNWRAP;
    }
    private final Status status;
    private final HandshakeStatus handshakeStatus;
    private final int bytesConsumed;
    private final int bytesProduced;
    public SSLEngineResult(Status status, HandshakeStatus handshakeStatus,
            int bytesConsumed, int bytesProduced) {
        if ((status == null) || (handshakeStatus == null) ||
                (bytesConsumed < 0) || (bytesProduced < 0)) {
            throw new IllegalArgumentException("Invalid Parameter(s)");
        }
        this.status = status;
        this.handshakeStatus = handshakeStatus;
        this.bytesConsumed = bytesConsumed;
        this.bytesProduced = bytesProduced;
    }
    final public Status getStatus() {
        return status;
    }
    final public HandshakeStatus getHandshakeStatus() {
        return handshakeStatus;
    }
    final public int bytesConsumed() {
        return bytesConsumed;
    }
    final public int bytesProduced() {
        return bytesProduced;
    }
    public String toString() {
        return ("Status = " + status +
            " HandshakeStatus = " + handshakeStatus +
            "\nbytesConsumed = " + bytesConsumed +
            " bytesProduced = " + bytesProduced);
    }
}
