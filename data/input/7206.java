class BasicChecker extends PKIXCertPathChecker {
    private static final Debug debug = Debug.getInstance("certpath");
    private final PublicKey trustedPubKey;
    private final X500Principal caName;
    private final Date testDate;
    private final String sigProvider;
    private final boolean sigOnly;
    private X500Principal prevSubject;
    private PublicKey prevPubKey;
    BasicChecker(TrustAnchor anchor, Date testDate, String sigProvider,
        boolean sigOnly) throws CertPathValidatorException
    {
        if (anchor.getTrustedCert() != null) {
            this.trustedPubKey = anchor.getTrustedCert().getPublicKey();
            this.caName = anchor.getTrustedCert().getSubjectX500Principal();
        } else {
            this.trustedPubKey = anchor.getCAPublicKey();
            this.caName = anchor.getCA();
        }
        this.testDate = testDate;
        this.sigProvider = sigProvider;
        this.sigOnly = sigOnly;
        init(false);
    }
    public void init(boolean forward) throws CertPathValidatorException {
        if (!forward) {
            prevPubKey = trustedPubKey;
            prevSubject = caName;
        } else {
            throw new
                CertPathValidatorException("forward checking not supported");
        }
    }
    public boolean isForwardCheckingSupported() {
        return false;
    }
    public Set<String> getSupportedExtensions() {
        return null;
    }
    public void check(Certificate cert, Collection<String> unresolvedCritExts)
        throws CertPathValidatorException
    {
        X509Certificate currCert = (X509Certificate) cert;
        if (!sigOnly) {
            verifyTimestamp(currCert, testDate);
            verifyNameChaining(currCert, prevSubject);
        }
        verifySignature(currCert, prevPubKey, sigProvider);
        updateState(currCert);
    }
    private void verifySignature(X509Certificate cert, PublicKey prevPubKey,
        String sigProvider) throws CertPathValidatorException
    {
        String msg = "signature";
        if (debug != null)
            debug.println("---checking " + msg + "...");
        try {
            cert.verify(prevPubKey, sigProvider);
        } catch (SignatureException e) {
            throw new CertPathValidatorException
                (msg + " check failed", e, null, -1,
                 BasicReason.INVALID_SIGNATURE);
        } catch (Exception e) {
            throw new CertPathValidatorException(msg + " check failed", e);
        }
        if (debug != null)
            debug.println(msg + " verified.");
    }
    private void verifyTimestamp(X509Certificate cert, Date date)
        throws CertPathValidatorException
    {
        String msg = "timestamp";
        if (debug != null)
            debug.println("---checking " + msg + ":" + date.toString() + "...");
        try {
            cert.checkValidity(date);
        } catch (CertificateExpiredException e) {
            throw new CertPathValidatorException
                (msg + " check failed", e, null, -1, BasicReason.EXPIRED);
        } catch (CertificateNotYetValidException e) {
            throw new CertPathValidatorException
                (msg + " check failed", e, null, -1, BasicReason.NOT_YET_VALID);
        }
        if (debug != null)
            debug.println(msg + " verified.");
    }
    private void verifyNameChaining(X509Certificate cert,
        X500Principal prevSubject) throws CertPathValidatorException
    {
        if (prevSubject != null) {
            String msg = "subject/issuer name chaining";
            if (debug != null)
                debug.println("---checking " + msg + "...");
            X500Principal currIssuer = cert.getIssuerX500Principal();
            if (X500Name.asX500Name(currIssuer).isEmpty()) {
                throw new CertPathValidatorException
                    (msg + " check failed: " +
                     "empty/null issuer DN in certificate is invalid", null,
                     null, -1, PKIXReason.NAME_CHAINING);
            }
            if (!(currIssuer.equals(prevSubject))) {
                throw new CertPathValidatorException
                    (msg + " check failed", null, null, -1,
                     PKIXReason.NAME_CHAINING);
            }
            if (debug != null)
                debug.println(msg + " verified.");
        }
    }
    private void updateState(X509Certificate currCert)
        throws CertPathValidatorException
    {
        PublicKey cKey = currCert.getPublicKey();
        if (debug != null) {
            debug.println("BasicChecker.updateState issuer: " +
                currCert.getIssuerX500Principal().toString() + "; subject: " +
                currCert.getSubjectX500Principal() + "; serial#: " +
                currCert.getSerialNumber().toString());
        }
        if (cKey instanceof DSAPublicKey &&
            ((DSAPublicKey)cKey).getParams() == null) {
            cKey = makeInheritedParamsKey(cKey, prevPubKey);
            if (debug != null) debug.println("BasicChecker.updateState Made " +
                                             "key with inherited params");
        }
        prevPubKey = cKey;
        prevSubject = currCert.getSubjectX500Principal();
    }
    static PublicKey makeInheritedParamsKey(PublicKey keyValueKey,
        PublicKey keyParamsKey) throws CertPathValidatorException
    {
        PublicKey usableKey;
        if (!(keyValueKey instanceof DSAPublicKey) ||
            !(keyParamsKey instanceof DSAPublicKey))
            throw new CertPathValidatorException("Input key is not " +
                                                 "appropriate type for " +
                                                 "inheriting parameters");
        DSAParams params = ((DSAPublicKey)keyParamsKey).getParams();
        if (params == null)
            throw new CertPathValidatorException("Key parameters missing");
        try {
            BigInteger y = ((DSAPublicKey)keyValueKey).getY();
            KeyFactory kf = KeyFactory.getInstance("DSA");
            DSAPublicKeySpec ks = new DSAPublicKeySpec(y,
                                                       params.getP(),
                                                       params.getQ(),
                                                       params.getG());
            usableKey = kf.generatePublic(ks);
        } catch (GeneralSecurityException e) {
            throw new CertPathValidatorException("Unable to generate key with" +
                                                 " inherited parameters: " +
                                                 e.getMessage(), e);
        }
        return usableKey;
    }
    PublicKey getPublicKey() {
        return prevPubKey;
    }
}
