public class SSLEngineResult {
    public enum HandshakeStatus {
        NOT_HANDSHAKING,
        FINISHED,
        NEED_TASK,
        NEED_WRAP,
        NEED_UNWRAP
    }
    public static enum Status {
        BUFFER_OVERFLOW,
        BUFFER_UNDERFLOW,
        CLOSED,
        OK
    }
    private final SSLEngineResult.Status status;
    private final SSLEngineResult.HandshakeStatus handshakeStatus;
    private final int bytesConsumed;
    private final int bytesProduced;
    public SSLEngineResult(SSLEngineResult.Status status,
            SSLEngineResult.HandshakeStatus handshakeStatus, int bytesConsumed, int bytesProduced) {
        if (status == null) {
            throw new IllegalArgumentException("status is null");
        }
        if (handshakeStatus == null) {
            throw new IllegalArgumentException("handshakeStatus is null");
        }
        if (bytesConsumed < 0) {
            throw new IllegalArgumentException("bytesConsumed is negative");
        }
        if (bytesProduced < 0) {
            throw new IllegalArgumentException("bytesProduced is negative");
        }
        this.status = status;
        this.handshakeStatus = handshakeStatus;
        this.bytesConsumed = bytesConsumed;
        this.bytesProduced = bytesProduced;
    }
    public final Status getStatus() {
        return status;
    }
    public final HandshakeStatus getHandshakeStatus() {
        return handshakeStatus;
    }
    public final int bytesConsumed() {
        return bytesConsumed;
    }
    public final int bytesProduced() {
        return bytesProduced;
    }
    @Override
    public String toString() {
        return "SSLEngineReport: Status = " + status + "  HandshakeStatus = " + handshakeStatus
                + "\n                 bytesConsumed = " + bytesConsumed + " bytesProduced = "
                + bytesProduced;
    }
}
