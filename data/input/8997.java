public final class PolicyError extends org.omg.CORBA.UserException {
    public short reason;
    public PolicyError() {
        super();
    }
    public PolicyError(short __reason) {
        super();
        reason = __reason;
    }
    public PolicyError(String reason_string, short __reason) {
        super(reason_string);
        reason = __reason;
    }
}
