public class CertPathValidatorException extends GeneralSecurityException {
    private static final long serialVersionUID = -3083180014971893139L;
    private CertPath certPath;
    private int index = -1;
    public CertPathValidatorException(String msg, Throwable cause,
            CertPath certPath, int index) {
        super(msg, cause);
        if ((certPath == null) && (index != -1)) {
            throw new IllegalArgumentException(
                    Messages.getString("security.53")); 
        }
        if ((certPath != null)
                && ((index < -1) || (index >= certPath.getCertificates().size()))) {
            throw new IndexOutOfBoundsException(Messages.getString("security.54")); 
        }
        this.certPath = certPath;
        this.index = index;
    }
    public CertPathValidatorException(String msg, Throwable cause) {
        super(msg, cause);
    }
    public CertPathValidatorException(Throwable cause) {
        super(cause);
    }
    public CertPathValidatorException(String msg) {
        super(msg);
    }
    public CertPathValidatorException() {
    }
    public CertPath getCertPath() {
        return certPath;
    }
    public int getIndex() {
        return index;
    }
}
