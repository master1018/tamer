class ForwardState implements State {
    private static final Debug debug = Debug.getInstance("certpath");
    X500Principal issuerDN;
    X509CertImpl cert;
    HashSet<GeneralNameInterface> subjectNamesTraversed;
    int traversedCACerts;
    private boolean init = true;
    public CrlRevocationChecker crlChecker;
    ArrayList<PKIXCertPathChecker> forwardCheckers;
    boolean keyParamsNeededFlag = false;
    public boolean isInitial() {
        return init;
    }
    public boolean keyParamsNeeded() {
        return keyParamsNeededFlag;
    }
    public String toString() {
        StringBuffer sb = new StringBuffer();
        try {
            sb.append("State [");
            sb.append("\n  issuerDN of last cert: " + issuerDN);
            sb.append("\n  traversedCACerts: " + traversedCACerts);
            sb.append("\n  init: " + String.valueOf(init));
            sb.append("\n  keyParamsNeeded: "
                + String.valueOf(keyParamsNeededFlag));
            sb.append("\n  subjectNamesTraversed: \n" + subjectNamesTraversed);
            sb.append("]\n");
        } catch (Exception e) {
            if (debug != null) {
                debug.println("ForwardState.toString() unexpected exception");
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
    public void initState(List<PKIXCertPathChecker> certPathCheckers)
        throws CertPathValidatorException
    {
        subjectNamesTraversed = new HashSet<GeneralNameInterface>();
        traversedCACerts = 0;
        forwardCheckers = new ArrayList<PKIXCertPathChecker>();
        if (certPathCheckers != null) {
            for (PKIXCertPathChecker checker : certPathCheckers) {
                if (checker.isForwardCheckingSupported()) {
                    checker.init(true);
                    forwardCheckers.add(checker);
                }
            }
        }
        init = true;
    }
    public void updateState(X509Certificate cert)
        throws CertificateException, IOException, CertPathValidatorException {
        if (cert == null)
            return;
        X509CertImpl icert = X509CertImpl.toImpl(cert);
        PublicKey newKey = icert.getPublicKey();
        if (newKey instanceof DSAPublicKey &&
            ((DSAPublicKey)newKey).getParams() == null) {
            keyParamsNeededFlag = true;
        }
        this.cert = icert;
        issuerDN = cert.getIssuerX500Principal();
        if (!X509CertImpl.isSelfIssued(cert)) {
            if (!init && cert.getBasicConstraints() != -1) {
                traversedCACerts++;
            }
        }
        if (init || !X509CertImpl.isSelfIssued(cert)){
            X500Principal subjName = cert.getSubjectX500Principal();
            subjectNamesTraversed.add(X500Name.asX500Name(subjName));
            try {
                SubjectAlternativeNameExtension subjAltNameExt
                    = icert.getSubjectAlternativeNameExtension();
                if (subjAltNameExt != null) {
                    GeneralNames gNames = (GeneralNames)
                        subjAltNameExt.get(SubjectAlternativeNameExtension.SUBJECT_NAME);
                    for (Iterator<GeneralName> t = gNames.iterator();
                                t.hasNext(); ) {
                        GeneralNameInterface gName = t.next().getName();
                        subjectNamesTraversed.add(gName);
                    }
                }
            } catch (Exception e) {
                if (debug != null) {
                    debug.println("ForwardState.updateState() unexpected "
                        + "exception");
                    e.printStackTrace();
                }
                throw new CertPathValidatorException(e);
            }
        }
        init = false;
    }
    public Object clone() {
        try {
            ForwardState clonedState = (ForwardState) super.clone();
            clonedState.forwardCheckers = (ArrayList<PKIXCertPathChecker>)
                                                forwardCheckers.clone();
            ListIterator<PKIXCertPathChecker> li =
                                clonedState.forwardCheckers.listIterator();
            while (li.hasNext()) {
                PKIXCertPathChecker checker = li.next();
                if (checker instanceof Cloneable) {
                    li.set((PKIXCertPathChecker)checker.clone());
                }
            }
            clonedState.subjectNamesTraversed
                = (HashSet<GeneralNameInterface>)subjectNamesTraversed.clone();
            return clonedState;
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e.toString());
        }
    }
}
