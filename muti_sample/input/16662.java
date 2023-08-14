public class SunCertPathBuilderParameters extends PKIXBuilderParameters {
    private boolean buildForward = true;
    public SunCertPathBuilderParameters(Set<TrustAnchor> trustAnchors,
        CertSelector targetConstraints) throws InvalidAlgorithmParameterException
    {
        super(trustAnchors, targetConstraints);
        setBuildForward(true);
    }
    public SunCertPathBuilderParameters(KeyStore keystore,
        CertSelector targetConstraints)
        throws KeyStoreException, InvalidAlgorithmParameterException
    {
        super(keystore, targetConstraints);
        setBuildForward(true);
    }
    public boolean getBuildForward() {
        return this.buildForward;
    }
    public void setBuildForward(boolean buildForward) {
        this.buildForward = buildForward;
    }
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("[\n");
        sb.append(super.toString());
        sb.append("  Build Forward Flag: " + String.valueOf(buildForward) + "\n");
        sb.append("]\n");
        return sb.toString();
    }
}
