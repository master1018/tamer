public class PKIXBuilderParameters extends PKIXParameters {
    private int maxPathLength = 5;
    public PKIXBuilderParameters(Set<TrustAnchor> trustAnchors,
            CertSelector targetConstraints)
        throws InvalidAlgorithmParameterException {
        super(trustAnchors);
        super.setTargetCertConstraints(targetConstraints);
    }
    public PKIXBuilderParameters(KeyStore keyStore,
            CertSelector targetConstraints)
        throws KeyStoreException,
               InvalidAlgorithmParameterException {
        super(keyStore);
        super.setTargetCertConstraints(targetConstraints);
    }
    public int getMaxPathLength() {
        return maxPathLength;
    }
    public void setMaxPathLength(int maxPathLength) {
        if (maxPathLength < -1) {
            throw new InvalidParameterException(
                    Messages.getString("security.5B")); 
        }
        this.maxPathLength = maxPathLength;
    }
    public String toString() {
        StringBuilder sb = new StringBuilder("[\n"); 
        sb.append(super.toString());
        sb.append(" Max Path Length: "); 
        sb.append(maxPathLength);
        sb.append("\n]"); 
        return sb.toString();
    }
}
