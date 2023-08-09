class KeyChecker extends PKIXCertPathChecker {
    private static final Debug debug = Debug.getInstance("certpath");
    private static final int keyCertSign = 5;
    private final int certPathLen;
    private CertSelector targetConstraints;
    private int remainingCerts;
    private Set<String> supportedExts;
    KeyChecker(int certPathLen, CertSelector targetCertSel)
        throws CertPathValidatorException
    {
        this.certPathLen = certPathLen;
        this.targetConstraints = targetCertSel;
        init(false);
    }
    public void init(boolean forward) throws CertPathValidatorException {
        if (!forward) {
            remainingCerts = certPathLen;
        } else {
            throw new CertPathValidatorException
                ("forward checking not supported");
        }
    }
    public final boolean isForwardCheckingSupported() {
        return false;
    }
    public Set<String> getSupportedExtensions() {
        if (supportedExts == null) {
            supportedExts = new HashSet<String>();
            supportedExts.add(PKIXExtensions.KeyUsage_Id.toString());
            supportedExts.add(PKIXExtensions.ExtendedKeyUsage_Id.toString());
            supportedExts.add(PKIXExtensions.SubjectAlternativeName_Id.toString());
            supportedExts = Collections.unmodifiableSet(supportedExts);
        }
        return supportedExts;
    }
    public void check(Certificate cert, Collection<String> unresCritExts)
        throws CertPathValidatorException
    {
        X509Certificate currCert = (X509Certificate) cert;
        remainingCerts--;
        if (remainingCerts == 0) {
            if ((targetConstraints != null) &&
                (targetConstraints.match(currCert) == false)) {
                throw new CertPathValidatorException("target certificate " +
                    "constraints check failed");
            }
        } else {
            verifyCAKeyUsage(currCert);
        }
        if (unresCritExts != null && !unresCritExts.isEmpty()) {
            unresCritExts.remove(PKIXExtensions.KeyUsage_Id.toString());
            unresCritExts.remove(PKIXExtensions.ExtendedKeyUsage_Id.toString());
            unresCritExts.remove(
                PKIXExtensions.SubjectAlternativeName_Id.toString());
        }
    }
    static void verifyCAKeyUsage(X509Certificate cert)
            throws CertPathValidatorException {
        String msg = "CA key usage";
        if (debug != null) {
            debug.println("KeyChecker.verifyCAKeyUsage() ---checking " + msg
                + "...");
        }
        boolean[] keyUsageBits = cert.getKeyUsage();
        if (keyUsageBits == null) {
            return;
        }
        if (!keyUsageBits[keyCertSign]) {
            throw new CertPathValidatorException
                (msg + " check failed: keyCertSign bit is not set", null,
                 null, -1, PKIXReason.INVALID_KEY_USAGE);
        }
        if (debug != null) {
            debug.println("KeyChecker.verifyCAKeyUsage() " + msg
                + " verified.");
        }
    }
}
