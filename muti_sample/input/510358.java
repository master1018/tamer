public class IllegalHeartbeatException extends EasException {
    private static final long serialVersionUID = 1L;
    public final int mLegalHeartbeat;
    public IllegalHeartbeatException(int legalHeartbeat) {
        mLegalHeartbeat = legalHeartbeat;
    }
}
