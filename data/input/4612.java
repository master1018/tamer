public class CommunicationException extends javax.management.JMRuntimeException {
    private static final long serialVersionUID = -2499186113233316177L;
    public CommunicationException(Throwable target) {
        super(target.getMessage());
        initCause(target);
    }
    public CommunicationException(Throwable target, String msg) {
        super(msg);
        initCause(target);
    }
    public CommunicationException(String msg) {
        super(msg);
    }
    public Throwable getTargetException() {
        return getCause();
    }
}
