public class CertPathValidatorException extends GeneralSecurityException {
    private static final long serialVersionUID = -3083180014971893139L;
    private int index = -1;
    private CertPath certPath;
    private Reason reason = BasicReason.UNSPECIFIED;
    public CertPathValidatorException() {
        this(null, null);
    }
    public CertPathValidatorException(String msg) {
        this(msg, null);
    }
    public CertPathValidatorException(Throwable cause) {
        this((cause == null ? null : cause.toString()), cause);
    }
    public CertPathValidatorException(String msg, Throwable cause) {
        this(msg, cause, null, -1);
    }
    public CertPathValidatorException(String msg, Throwable cause,
            CertPath certPath, int index) {
        this(msg, cause, certPath, index, BasicReason.UNSPECIFIED);
    }
    public CertPathValidatorException(String msg, Throwable cause,
            CertPath certPath, int index, Reason reason) {
        super(msg, cause);
        if (certPath == null && index != -1) {
            throw new IllegalArgumentException();
        }
        if (index < -1 ||
            (certPath != null && index >= certPath.getCertificates().size())) {
            throw new IndexOutOfBoundsException();
        }
        if (reason == null) {
            throw new NullPointerException("reason can't be null");
        }
        this.certPath = certPath;
        this.index = index;
        this.reason = reason;
    }
    public CertPath getCertPath() {
        return this.certPath;
    }
    public int getIndex() {
        return this.index;
    }
    public Reason getReason() {
        return this.reason;
    }
    private void readObject(ObjectInputStream stream)
        throws ClassNotFoundException, IOException {
        stream.defaultReadObject();
        if (reason == null) {
            reason = BasicReason.UNSPECIFIED;
        }
        if (certPath == null && index != -1) {
            throw new InvalidObjectException("certpath is null and index != -1");
        }
        if (index < -1 ||
            (certPath != null && index >= certPath.getCertificates().size())) {
            throw new InvalidObjectException("index out of range");
        }
    }
    public static interface Reason extends java.io.Serializable { }
    public static enum BasicReason implements Reason {
        UNSPECIFIED,
        EXPIRED,
        NOT_YET_VALID,
        REVOKED,
        UNDETERMINED_REVOCATION_STATUS,
        INVALID_SIGNATURE,
        ALGORITHM_CONSTRAINED
    }
}
