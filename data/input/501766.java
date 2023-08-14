public class PKIXCertPathValidatorResult implements CertPathValidatorResult {
    private final TrustAnchor trustAnchor;
    private final PolicyNode policyTree;
    private final PublicKey subjectPublicKey;
    public PKIXCertPathValidatorResult(TrustAnchor trustAnchor,
            PolicyNode policyTree, PublicKey subjectPublicKey) {
        this.trustAnchor = trustAnchor;
        this.policyTree = policyTree;
        this.subjectPublicKey = subjectPublicKey;
        if (this.trustAnchor == null) {
            throw new NullPointerException(Messages.getString("security.64")); 
        }
        if (this.subjectPublicKey == null) {
            throw new NullPointerException(
                    Messages.getString("security.65")); 
        }
    }
    public PolicyNode getPolicyTree() {
        return policyTree;
    }
    public PublicKey getPublicKey() {
        return subjectPublicKey;
    }
    public TrustAnchor getTrustAnchor() {
        return trustAnchor;
    }
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e); 
        }
    }
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        sb.append(": [\n Trust Anchor: "); 
        sb.append(trustAnchor.toString());
        sb.append("\n Policy Tree: "); 
        sb.append(policyTree == null ? "no valid policy tree\n" 
                                     : policyTree.toString());
        sb.append("\n Subject Public Key: "); 
        sb.append(subjectPublicKey.toString());
        sb.append("\n]"); 
        return sb.toString();
    }
}
