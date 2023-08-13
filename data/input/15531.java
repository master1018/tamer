class ReverseBuilder extends Builder {
    private Debug debug = Debug.getInstance("certpath");
    Set<String> initPolicies;
    ReverseBuilder(PKIXBuilderParameters buildParams,
        X500Principal targetSubjectDN) {
        super(buildParams, targetSubjectDN);
        Set<String> initialPolicies = buildParams.getInitialPolicies();
        initPolicies = new HashSet<String>();
        if (initialPolicies.isEmpty()) {
            initPolicies.add(PolicyChecker.ANY_POLICY);
        } else {
            for (String policy : initialPolicies) {
                initPolicies.add(policy);
            }
        }
    }
    Collection<X509Certificate> getMatchingCerts
        (State currState, List<CertStore> certStores)
        throws CertStoreException, CertificateException, IOException
    {
        ReverseState currentState = (ReverseState) currState;
        if (debug != null)
            debug.println("In ReverseBuilder.getMatchingCerts.");
        Collection<X509Certificate> certs =
            getMatchingEECerts(currentState, certStores);
        certs.addAll(getMatchingCACerts(currentState, certStores));
        return certs;
    }
    private Collection<X509Certificate> getMatchingEECerts
        (ReverseState currentState, List<CertStore> certStores)
        throws CertStoreException, CertificateException, IOException {
      X509CertSelector sel = (X509CertSelector) targetCertConstraints.clone();
      sel.setIssuer(currentState.subjectDN);
      sel.setCertificateValid(date);
      if (currentState.explicitPolicy == 0)
          sel.setPolicy(getMatchingPolicies());
      sel.setBasicConstraints(-2);
      HashSet<X509Certificate> eeCerts = new HashSet<X509Certificate>();
      addMatchingCerts(sel, certStores, eeCerts, true);
      if (debug != null) {
        debug.println("ReverseBuilder.getMatchingEECerts got " + eeCerts.size()
                    + " certs.");
      }
      return eeCerts;
    }
    private Collection<X509Certificate> getMatchingCACerts
        (ReverseState currentState, List<CertStore> certStores)
        throws CertificateException, CertStoreException, IOException {
      X509CertSelector sel = new X509CertSelector();
      sel.setIssuer(currentState.subjectDN);
      sel.setCertificateValid(date);
      sel.addPathToName(4, targetCertConstraints.getSubjectAsBytes());
      if (currentState.explicitPolicy == 0)
          sel.setPolicy(getMatchingPolicies());
      sel.setBasicConstraints(0);
      ArrayList<X509Certificate> reverseCerts =
          new ArrayList<X509Certificate>();
      addMatchingCerts(sel, certStores, reverseCerts, true);
      Collections.sort(reverseCerts, new PKIXCertComparator());
      if (debug != null)
        debug.println("ReverseBuilder.getMatchingCACerts got " +
                    reverseCerts.size() + " certs.");
      return reverseCerts;
    }
    class PKIXCertComparator implements Comparator<X509Certificate> {
        private Debug debug = Debug.getInstance("certpath");
        public int compare(X509Certificate cert1, X509Certificate cert2) {
            if (cert1.getSubjectX500Principal().equals(targetSubjectDN)) {
                return -1;
            }
            if (cert2.getSubjectX500Principal().equals(targetSubjectDN)) {
                return 1;
            }
            int targetDist1;
            int targetDist2;
            try {
                X500Name targetSubjectName = X500Name.asX500Name(targetSubjectDN);
                targetDist1 = Builder.targetDistance(
                    null, cert1, targetSubjectName);
                targetDist2 = Builder.targetDistance(
                    null, cert2, targetSubjectName);
            } catch (IOException e) {
                if (debug != null) {
                    debug.println("IOException in call to Builder.targetDistance");
                    e.printStackTrace();
                }
                throw new ClassCastException
                    ("Invalid target subject distinguished name");
            }
            if (targetDist1 == targetDist2)
                return 0;
            if (targetDist1 == -1)
                return 1;
            if (targetDist1 < targetDist2)
                return -1;
            return 1;
        }
    }
    void verifyCert(X509Certificate cert, State currState,
        List<X509Certificate> certPathList)
        throws GeneralSecurityException
    {
        if (debug != null) {
            debug.println("ReverseBuilder.verifyCert(SN: "
                + Debug.toHexString(cert.getSerialNumber())
                + "\n  Subject: " + cert.getSubjectX500Principal() + ")");
        }
        ReverseState currentState = (ReverseState) currState;
        if (currentState.isInitial()) {
            return;
        }
        if ((certPathList != null) && (!certPathList.isEmpty())) {
            List<X509Certificate> reverseCertList =
                new ArrayList<X509Certificate>();
            for (X509Certificate c : certPathList) {
                reverseCertList.add(0, c);
            }
            boolean policyMappingFound = false;
            for (X509Certificate cpListCert : reverseCertList) {
                X509CertImpl cpListCertImpl = X509CertImpl.toImpl(cpListCert);
                PolicyMappingsExtension policyMappingsExt =
                        cpListCertImpl.getPolicyMappingsExtension();
                if (policyMappingsExt != null) {
                    policyMappingFound = true;
                }
                if (debug != null)
                    debug.println("policyMappingFound = " + policyMappingFound);
                if (cert.equals(cpListCert)){
                    if ((buildParams.isPolicyMappingInhibited()) ||
                        (!policyMappingFound)){
                        if (debug != null)
                            debug.println("loop detected!!");
                        throw new CertPathValidatorException("loop detected");
                    }
                }
            }
        }
        boolean finalCert = cert.getSubjectX500Principal().equals(targetSubjectDN);
        boolean caCert = (cert.getBasicConstraints() != -1 ? true : false);
        if (!finalCert) {
            if (!caCert)
                throw new CertPathValidatorException("cert is NOT a CA cert");
            if ((currentState.remainingCACerts <= 0) && !X509CertImpl.isSelfIssued(cert)) {
                    throw new CertPathValidatorException
                        ("pathLenConstraint violated, path too long", null,
                         null, -1, PKIXReason.PATH_TOO_LONG);
            }
            KeyChecker.verifyCAKeyUsage(cert);
        } else {
            if (targetCertConstraints.match(cert) == false) {
                throw new CertPathValidatorException("target certificate " +
                    "constraints check failed");
            }
        }
        if (buildParams.isRevocationEnabled()) {
            currentState.crlChecker.check(cert,
                                          currentState.pubKey,
                                          currentState.crlSign);
        }
        if (finalCert || !X509CertImpl.isSelfIssued(cert)){
            if (currentState.nc != null){
                try {
                    if (!currentState.nc.verify(cert)){
                        throw new CertPathValidatorException
                            ("name constraints check failed", null, null, -1,
                             PKIXReason.INVALID_NAME);
                    }
                } catch (IOException ioe){
                    throw new CertPathValidatorException(ioe);
                }
            }
        }
        X509CertImpl certImpl = X509CertImpl.toImpl(cert);
        currentState.rootNode = PolicyChecker.processPolicies
            (currentState.certIndex, initPolicies,
            currentState.explicitPolicy, currentState.policyMapping,
            currentState.inhibitAnyPolicy,
            buildParams.getPolicyQualifiersRejected(), currentState.rootNode,
            certImpl, finalCert);
        Set<String> unresolvedCritExts = cert.getCriticalExtensionOIDs();
        if (unresolvedCritExts == null) {
            unresolvedCritExts = Collections.<String>emptySet();
        }
        currentState.algorithmChecker.check(cert, unresolvedCritExts);
        for (PKIXCertPathChecker checker : currentState.userCheckers) {
            checker.check(cert, unresolvedCritExts);
        }
        if (!unresolvedCritExts.isEmpty()) {
            unresolvedCritExts.remove(PKIXExtensions.BasicConstraints_Id.toString());
            unresolvedCritExts.remove(PKIXExtensions.NameConstraints_Id.toString());
            unresolvedCritExts.remove(PKIXExtensions.CertificatePolicies_Id.toString());
            unresolvedCritExts.remove(PKIXExtensions.PolicyMappings_Id.toString());
            unresolvedCritExts.remove(PKIXExtensions.PolicyConstraints_Id.toString());
            unresolvedCritExts.remove(PKIXExtensions.InhibitAnyPolicy_Id.toString());
            unresolvedCritExts.remove(PKIXExtensions.SubjectAlternativeName_Id.toString());
            unresolvedCritExts.remove(PKIXExtensions.KeyUsage_Id.toString());
            unresolvedCritExts.remove(PKIXExtensions.ExtendedKeyUsage_Id.toString());
            if (!unresolvedCritExts.isEmpty())
                throw new CertPathValidatorException
                    ("Unrecognized critical extension(s)", null, null, -1,
                     PKIXReason.UNRECOGNIZED_CRIT_EXT);
        }
        if (buildParams.getSigProvider() != null) {
            cert.verify(currentState.pubKey, buildParams.getSigProvider());
        } else {
            cert.verify(currentState.pubKey);
        }
    }
    boolean isPathCompleted(X509Certificate cert) {
        return cert.getSubjectX500Principal().equals(targetSubjectDN);
    }
    void addCertToPath(X509Certificate cert,
        LinkedList<X509Certificate> certPathList) {
        certPathList.addLast(cert);
    }
    void removeFinalCertFromPath(LinkedList<X509Certificate> certPathList) {
        certPathList.removeLast();
    }
}
