class ConstraintsChecker extends PKIXCertPathChecker {
    private static final Debug debug = Debug.getInstance("certpath");
    private final int certPathLength;
    private int maxPathLength;
    private int i;
    private NameConstraintsExtension prevNC;
    private Set<String> supportedExts;
    ConstraintsChecker(int certPathLength) throws CertPathValidatorException {
        this.certPathLength = certPathLength;
        init(false);
    }
    public void init(boolean forward) throws CertPathValidatorException {
        if (!forward) {
            i = 0;
            maxPathLength = certPathLength;
            prevNC = null;
        } else {
            throw new CertPathValidatorException
                ("forward checking not supported");
        }
    }
    public boolean isForwardCheckingSupported() {
        return false;
    }
    public Set<String> getSupportedExtensions() {
        if (supportedExts == null) {
            supportedExts = new HashSet<String>();
            supportedExts.add(PKIXExtensions.BasicConstraints_Id.toString());
            supportedExts.add(PKIXExtensions.NameConstraints_Id.toString());
            supportedExts = Collections.unmodifiableSet(supportedExts);
        }
        return supportedExts;
    }
    public void check(Certificate cert, Collection<String> unresCritExts)
        throws CertPathValidatorException
    {
        X509Certificate currCert = (X509Certificate) cert;
        i++;
        checkBasicConstraints(currCert);
        verifyNameConstraints(currCert);
        if (unresCritExts != null && !unresCritExts.isEmpty()) {
            unresCritExts.remove(PKIXExtensions.BasicConstraints_Id.toString());
            unresCritExts.remove(PKIXExtensions.NameConstraints_Id.toString());
        }
    }
    private void verifyNameConstraints(X509Certificate currCert)
        throws CertPathValidatorException
    {
        String msg = "name constraints";
        if (debug != null) {
            debug.println("---checking " + msg + "...");
        }
        if (prevNC != null && ((i == certPathLength) ||
                !X509CertImpl.isSelfIssued(currCert))) {
            if (debug != null) {
                debug.println("prevNC = " + prevNC);
                debug.println("currDN = " + currCert.getSubjectX500Principal());
            }
            try {
                if (!prevNC.verify(currCert)) {
                    throw new CertPathValidatorException(msg + " check failed",
                        null, null, -1, PKIXReason.INVALID_NAME);
                }
            } catch (IOException ioe) {
                throw new CertPathValidatorException(ioe);
            }
        }
        prevNC = mergeNameConstraints(currCert, prevNC);
        if (debug != null)
            debug.println(msg + " verified.");
    }
    static NameConstraintsExtension
        mergeNameConstraints(X509Certificate currCert,
            NameConstraintsExtension prevNC) throws CertPathValidatorException
    {
        X509CertImpl currCertImpl;
        try {
            currCertImpl = X509CertImpl.toImpl(currCert);
        } catch (CertificateException ce) {
            throw new CertPathValidatorException(ce);
        }
        NameConstraintsExtension newConstraints =
            currCertImpl.getNameConstraintsExtension();
        if (debug != null) {
            debug.println("prevNC = " + prevNC);
            debug.println("newNC = " + String.valueOf(newConstraints));
        }
        if (prevNC == null) {
            if (debug != null) {
                debug.println("mergedNC = " + String.valueOf(newConstraints));
            }
            if (newConstraints == null) {
                return newConstraints;
            } else {
                return (NameConstraintsExtension) newConstraints.clone();
            }
        } else {
            try {
                prevNC.merge(newConstraints);
            } catch (IOException ioe) {
                throw new CertPathValidatorException(ioe);
            }
            if (debug != null) {
                debug.println("mergedNC = " + prevNC);
            }
            return prevNC;
        }
    }
    private void checkBasicConstraints(X509Certificate currCert)
        throws CertPathValidatorException
    {
        String msg = "basic constraints";
        if (debug != null) {
            debug.println("---checking " + msg + "...");
            debug.println("i = " + i);
            debug.println("maxPathLength = " + maxPathLength);
        }
        if (i < certPathLength) {
            int pathLenConstraint = -1;
            if (currCert.getVersion() < 3) {    
                if (i == 1) {                   
                    if (X509CertImpl.isSelfIssued(currCert)) {
                        pathLenConstraint = Integer.MAX_VALUE;
                    }
                }
            } else {
                pathLenConstraint = currCert.getBasicConstraints();
            }
            if (pathLenConstraint == -1) {
                throw new CertPathValidatorException
                    (msg + " check failed: this is not a CA certificate",
                     null, null, -1, PKIXReason.NOT_CA_CERT);
            }
            if (!X509CertImpl.isSelfIssued(currCert)) {
                if (maxPathLength <= 0) {
                   throw new CertPathValidatorException
                        (msg + " check failed: pathLenConstraint violated - "
                         + "this cert must be the last cert in the "
                         + "certification path", null, null, -1,
                         PKIXReason.PATH_TOO_LONG);
                }
                maxPathLength--;
            }
            if (pathLenConstraint < maxPathLength)
                maxPathLength = pathLenConstraint;
        }
        if (debug != null) {
            debug.println("after processing, maxPathLength = " + maxPathLength);
            debug.println(msg + " verified.");
        }
    }
    static int mergeBasicConstraints(X509Certificate cert, int maxPathLength) {
        int pathLenConstraint = cert.getBasicConstraints();
        if (!X509CertImpl.isSelfIssued(cert)) {
            maxPathLength--;
        }
        if (pathLenConstraint < maxPathLength) {
            maxPathLength = pathLenConstraint;
        }
        return maxPathLength;
    }
}
