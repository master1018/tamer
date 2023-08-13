public abstract class PKIXCertPathChecker implements Cloneable {
    protected PKIXCertPathChecker() {}
    public abstract void init(boolean forward)
        throws CertPathValidatorException;
    public abstract boolean isForwardCheckingSupported();
    public abstract Set<String> getSupportedExtensions();
    public abstract void check(Certificate cert,
            Collection<String> unresolvedCritExts)
            throws CertPathValidatorException;
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e.toString());
        }
    }
}
