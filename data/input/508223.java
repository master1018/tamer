public class PKIXCertPathBuilderResult extends PKIXCertPathValidatorResult
        implements CertPathBuilderResult {
    private final CertPath certPath;
    public PKIXCertPathBuilderResult(CertPath certPath, TrustAnchor trustAnchor,
            PolicyNode policyTree, PublicKey subjectPublicKey) {
        super(trustAnchor, policyTree, subjectPublicKey);
        this.certPath = certPath;
        if (this.certPath == null) {
            throw new NullPointerException(Messages.getString("security.55")); 
        }
    }
    public CertPath getCertPath() {
        return certPath;
    }
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        sb.append("\n Certification Path: "); 
        sb.append(certPath.toString());
        sb.append("\n]"); 
        return sb.toString();
    }
}
