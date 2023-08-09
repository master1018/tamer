class ReverseState implements State {
    private static final Debug debug = Debug.getInstance("certpath");
    X500Principal subjectDN;
    PublicKey pubKey;
    SubjectKeyIdentifierExtension subjKeyId;
    NameConstraintsExtension nc;
    int explicitPolicy;
    int policyMapping;
    int inhibitAnyPolicy;
    int certIndex;
    PolicyNodeImpl rootNode;
    int remainingCACerts;
    ArrayList<PKIXCertPathChecker> userCheckers;
    private boolean init = true;
    public CrlRevocationChecker crlChecker;
    AlgorithmChecker algorithmChecker;
    TrustAnchor trustAnchor;
    public boolean crlSign = true;
    public boolean isInitial() {
        return init;
    }
    public String toString() {
        StringBuffer sb = new StringBuffer();
        try {
            sb.append("State [");
            sb.append("\n  subjectDN of last cert: " + subjectDN);
            sb.append("\n  subjectKeyIdentifier: " + String.valueOf(subjKeyId));
            sb.append("\n  nameConstraints: " + String.valueOf(nc));
            sb.append("\n  certIndex: " + certIndex);
            sb.append("\n  explicitPolicy: " + explicitPolicy);
            sb.append("\n  policyMapping:  " + policyMapping);
            sb.append("\n  inhibitAnyPolicy:  " + inhibitAnyPolicy);
            sb.append("\n  rootNode: " + rootNode);
            sb.append("\n  remainingCACerts: " + remainingCACerts);
            sb.append("\n  crlSign: " + crlSign);
            sb.append("\n  init: " + init);
            sb.append("\n]\n");
        } catch (Exception e) {
            if (debug != null) {
                debug.println("ReverseState.toString() unexpected exception");
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
    public void initState(int maxPathLen, boolean explicitPolicyRequired,
        boolean policyMappingInhibited, boolean anyPolicyInhibited,
        List<PKIXCertPathChecker> certPathCheckers)
        throws CertPathValidatorException
    {
        remainingCACerts = (maxPathLen == -1 ? Integer.MAX_VALUE : maxPathLen);
        if (explicitPolicyRequired) {
            explicitPolicy = 0;
        } else {
            explicitPolicy = (maxPathLen == -1)
                ? maxPathLen
                : maxPathLen + 2;
        }
        if (policyMappingInhibited) {
            policyMapping = 0;
        } else {
            policyMapping = (maxPathLen == -1)
                ? maxPathLen
                : maxPathLen + 2;
        }
        if (anyPolicyInhibited) {
            inhibitAnyPolicy = 0;
        } else {
            inhibitAnyPolicy = (maxPathLen == -1)
                ? maxPathLen
                : maxPathLen + 2;
        }
        certIndex = 1;
        Set<String> initExpPolSet = new HashSet<String>(1);
        initExpPolSet.add(PolicyChecker.ANY_POLICY);
        rootNode = new PolicyNodeImpl
            (null, PolicyChecker.ANY_POLICY, null, false, initExpPolSet, false);
        if (certPathCheckers != null) {
            userCheckers = new ArrayList<PKIXCertPathChecker>(certPathCheckers);
            for (PKIXCertPathChecker checker : certPathCheckers) {
                checker.init(false);
            }
        } else {
            userCheckers = new ArrayList<PKIXCertPathChecker>();
        }
        crlSign = true;
        init = true;
    }
    public void updateState(TrustAnchor anchor)
        throws CertificateException, IOException, CertPathValidatorException
    {
        trustAnchor = anchor;
        X509Certificate trustedCert = anchor.getTrustedCert();
        if (trustedCert != null) {
            updateState(trustedCert);
        } else {
            X500Principal caName = anchor.getCA();
            updateState(anchor.getCAPublicKey(), caName);
        }
        for (PKIXCertPathChecker checker : userCheckers) {
            if (checker instanceof AlgorithmChecker) {
                ((AlgorithmChecker)checker).trySetTrustAnchor(anchor);
            }
        }
        init = false;
    }
    private void updateState(PublicKey pubKey, X500Principal subjectDN) {
        this.subjectDN = subjectDN;
        this.pubKey = pubKey;
    }
    public void updateState(X509Certificate cert)
        throws CertificateException, IOException, CertPathValidatorException {
        if (cert == null) {
            return;
        }
        subjectDN = cert.getSubjectX500Principal();
        X509CertImpl icert = X509CertImpl.toImpl(cert);
        PublicKey newKey = cert.getPublicKey();
        if (newKey instanceof DSAPublicKey &&
            (((DSAPublicKey)newKey).getParams() == null)) {
            newKey = BasicChecker.makeInheritedParamsKey(newKey, pubKey);
        }
        pubKey = newKey;
        if (init) {
            init = false;
            return;
        }
        subjKeyId = icert.getSubjectKeyIdentifierExtension();
        crlSign = CrlRevocationChecker.certCanSignCrl(cert);
        if (nc != null) {
            nc.merge(icert.getNameConstraintsExtension());
        } else {
            nc = icert.getNameConstraintsExtension();
            if (nc != null) {
                nc = (NameConstraintsExtension) nc.clone();
            }
        }
        explicitPolicy =
            PolicyChecker.mergeExplicitPolicy(explicitPolicy, icert, false);
        policyMapping =
            PolicyChecker.mergePolicyMapping(policyMapping, icert);
        inhibitAnyPolicy =
            PolicyChecker.mergeInhibitAnyPolicy(inhibitAnyPolicy, icert);
        certIndex++;
        remainingCACerts =
            ConstraintsChecker.mergeBasicConstraints(cert, remainingCACerts);
        init = false;
    }
    public boolean keyParamsNeeded() {
        return false;
    }
    public Object clone() {
        try {
            ReverseState clonedState = (ReverseState) super.clone();
            clonedState.userCheckers =
                        (ArrayList<PKIXCertPathChecker>)userCheckers.clone();
            ListIterator<PKIXCertPathChecker> li =
                        clonedState.userCheckers.listIterator();
            while (li.hasNext()) {
                PKIXCertPathChecker checker = li.next();
                if (checker instanceof Cloneable) {
                    li.set((PKIXCertPathChecker)checker.clone());
                }
            }
            if (nc != null) {
                clonedState.nc = (NameConstraintsExtension) nc.clone();
            }
            if (rootNode != null) {
                clonedState.rootNode = rootNode.copyTree();
            }
            return clonedState;
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e.toString());
        }
    }
}
