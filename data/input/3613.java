class ForwardBuilder extends Builder {
    private static final Debug debug = Debug.getInstance("certpath");
    private final Set<X509Certificate> trustedCerts;
    private final Set<X500Principal> trustedSubjectDNs;
    private final Set<TrustAnchor> trustAnchors;
    private X509CertSelector eeSelector;
    private AdaptableX509CertSelector caSelector;
    private X509CertSelector caTargetSelector;
    TrustAnchor trustAnchor;
    private Comparator<X509Certificate> comparator;
    private boolean searchAllCertStores = true;
    private boolean onlyEECert = false;
    ForwardBuilder(PKIXBuilderParameters buildParams,
        X500Principal targetSubjectDN, boolean searchAllCertStores,
        boolean onlyEECert)
    {
        super(buildParams, targetSubjectDN);
        trustAnchors = buildParams.getTrustAnchors();
        trustedCerts = new HashSet<X509Certificate>(trustAnchors.size());
        trustedSubjectDNs = new HashSet<X500Principal>(trustAnchors.size());
        for (TrustAnchor anchor : trustAnchors) {
            X509Certificate trustedCert = anchor.getTrustedCert();
            if (trustedCert != null) {
                trustedCerts.add(trustedCert);
                trustedSubjectDNs.add(trustedCert.getSubjectX500Principal());
            } else {
                trustedSubjectDNs.add(anchor.getCA());
            }
        }
        comparator = new PKIXCertComparator(trustedSubjectDNs);
        this.searchAllCertStores = searchAllCertStores;
        this.onlyEECert = onlyEECert;
    }
    Collection<X509Certificate> getMatchingCerts
        (State currentState, List<CertStore> certStores)
        throws CertStoreException, CertificateException, IOException
    {
        if (debug != null) {
            debug.println("ForwardBuilder.getMatchingCerts()...");
        }
        ForwardState currState = (ForwardState) currentState;
        Set<X509Certificate> certs = new TreeSet<X509Certificate>(comparator);
        if (currState.isInitial()) {
            getMatchingEECerts(currState, certStores, certs);
        }
        getMatchingCACerts(currState, certStores, certs);
        return certs;
    }
    private void getMatchingEECerts(ForwardState currentState,
        List<CertStore> certStores, Collection<X509Certificate> eeCerts)
        throws IOException {
        if (debug != null) {
            debug.println("ForwardBuilder.getMatchingEECerts()...");
        }
        if (eeSelector == null) {
            eeSelector = (X509CertSelector) targetCertConstraints.clone();
            eeSelector.setCertificateValid(date);
            if (buildParams.isExplicitPolicyRequired()) {
                eeSelector.setPolicy(getMatchingPolicies());
            }
            eeSelector.setBasicConstraints(-2);
        }
        addMatchingCerts(eeSelector, certStores, eeCerts, searchAllCertStores);
    }
    private void getMatchingCACerts(ForwardState currentState,
        List<CertStore> certStores, Collection<X509Certificate> caCerts)
        throws IOException {
        if (debug != null) {
            debug.println("ForwardBuilder.getMatchingCACerts()...");
        }
        int initialSize = caCerts.size();
        X509CertSelector sel = null;
        if (currentState.isInitial()) {
            if (targetCertConstraints.getBasicConstraints() == -2) {
                return;
            }
            if (debug != null) {
                debug.println("ForwardBuilder.getMatchingCACerts(): ca is target");
            }
            if (caTargetSelector == null) {
                caTargetSelector = (X509CertSelector)
                    targetCertConstraints.clone();
                if (buildParams.isExplicitPolicyRequired())
                    caTargetSelector.setPolicy(getMatchingPolicies());
            }
            sel = caTargetSelector;
        } else {
            if (caSelector == null) {
                caSelector = new AdaptableX509CertSelector();
                if (buildParams.isExplicitPolicyRequired())
                    caSelector.setPolicy(getMatchingPolicies());
            }
            caSelector.setSubject(currentState.issuerDN);
            CertPathHelper.setPathToNames
                (caSelector, currentState.subjectNamesTraversed);
            AuthorityKeyIdentifierExtension akidext =
                    currentState.cert.getAuthorityKeyIdentifierExtension();
            caSelector.parseAuthorityKeyIdentifierExtension(akidext);
            caSelector.setValidityPeriod(currentState.cert.getNotBefore(),
                                            currentState.cert.getNotAfter());
            sel = caSelector;
        }
        sel.setBasicConstraints(-1);
        for (X509Certificate trustedCert : trustedCerts) {
            if (sel.match(trustedCert)) {
                if (debug != null) {
                    debug.println("ForwardBuilder.getMatchingCACerts: "
                        + "found matching trust anchor");
                }
                if (caCerts.add(trustedCert) && !searchAllCertStores) {
                    return;
                }
            }
        }
        sel.setCertificateValid(date);
        sel.setBasicConstraints(currentState.traversedCACerts);
        if (currentState.isInitial() ||
           (buildParams.getMaxPathLength() == -1) ||
           (buildParams.getMaxPathLength() > currentState.traversedCACerts))
        {
            if (addMatchingCerts(sel, certStores,
                    caCerts, searchAllCertStores) && !searchAllCertStores) {
                return;
            }
        }
        if (!currentState.isInitial() && Builder.USE_AIA) {
            AuthorityInfoAccessExtension aiaExt =
                currentState.cert.getAuthorityInfoAccessExtension();
            if (aiaExt != null) {
                getCerts(aiaExt, caCerts);
            }
        }
        if (debug != null) {
            int numCerts = caCerts.size() - initialSize;
            debug.println("ForwardBuilder.getMatchingCACerts: found " +
                numCerts + " CA certs");
        }
    }
    private boolean getCerts(AuthorityInfoAccessExtension aiaExt,
        Collection<X509Certificate> certs) {
        if (Builder.USE_AIA == false) {
            return false;
        }
        List<AccessDescription> adList = aiaExt.getAccessDescriptions();
        if (adList == null || adList.isEmpty()) {
            return false;
        }
        boolean add = false;
        for (AccessDescription ad : adList) {
            CertStore cs = URICertStore.getInstance(ad);
            try {
                if (certs.addAll((Collection<X509Certificate>)
                    cs.getCertificates(caSelector))) {
                    add = true;
                    if (!searchAllCertStores) {
                        return true;
                    }
                }
            } catch (CertStoreException cse) {
                if (debug != null) {
                    debug.println("exception getting certs from CertStore:");
                    cse.printStackTrace();
                }
                continue;
            }
        }
        return add;
    }
    static class PKIXCertComparator implements Comparator<X509Certificate> {
        final static String METHOD_NME = "PKIXCertComparator.compare()";
        private final Set<X500Principal> trustedSubjectDNs;
        PKIXCertComparator(Set<X500Principal> trustedSubjectDNs) {
            this.trustedSubjectDNs = trustedSubjectDNs;
        }
        public int compare(X509Certificate oCert1, X509Certificate oCert2) {
            if (oCert1.equals(oCert2)) return 0;
            X500Principal cIssuer1 = oCert1.getIssuerX500Principal();
            X500Principal cIssuer2 = oCert2.getIssuerX500Principal();
            X500Name cIssuer1Name = X500Name.asX500Name(cIssuer1);
            X500Name cIssuer2Name = X500Name.asX500Name(cIssuer2);
            if (debug != null) {
                debug.println(METHOD_NME + " o1 Issuer:  " + cIssuer1);
                debug.println(METHOD_NME + " o2 Issuer:  " + cIssuer2);
            }
            if (debug != null) {
                debug.println(METHOD_NME + " MATCH TRUSTED SUBJECT TEST...");
            }
            boolean m1 = trustedSubjectDNs.contains(cIssuer1);
            boolean m2 = trustedSubjectDNs.contains(cIssuer2);
            if (debug != null) {
                debug.println(METHOD_NME + " m1: " + m1);
                debug.println(METHOD_NME + " m2: " + m2);
            }
            if (m1 && m2) {
                return -1;
            } else if (m1) {
                return -1;
            } else if (m2) {
                return 1;
            }
            if (debug != null) {
                debug.println(METHOD_NME + " NAMING DESCENDANT TEST...");
            }
            for (X500Principal tSubject : trustedSubjectDNs) {
                X500Name tSubjectName = X500Name.asX500Name(tSubject);
                int distanceTto1 =
                    Builder.distance(tSubjectName, cIssuer1Name, -1);
                int distanceTto2 =
                    Builder.distance(tSubjectName, cIssuer2Name, -1);
                if (debug != null) {
                    debug.println(METHOD_NME +" distanceTto1: " + distanceTto1);
                    debug.println(METHOD_NME +" distanceTto2: " + distanceTto2);
                }
                if (distanceTto1 > 0 || distanceTto2 > 0) {
                    if (distanceTto1 == distanceTto2) {
                        return -1;
                    } else if (distanceTto1 > 0 && distanceTto2 <= 0) {
                        return -1;
                    } else if (distanceTto1 <= 0 && distanceTto2 > 0) {
                        return 1;
                    } else if (distanceTto1 < distanceTto2) {
                        return -1;
                    } else {    
                        return 1;
                    }
                }
            }
            if (debug != null) {
                debug.println(METHOD_NME + " NAMING ANCESTOR TEST...");
            }
            for (X500Principal tSubject : trustedSubjectDNs) {
                X500Name tSubjectName = X500Name.asX500Name(tSubject);
                int distanceTto1 = Builder.distance
                    (tSubjectName, cIssuer1Name, Integer.MAX_VALUE);
                int distanceTto2 = Builder.distance
                    (tSubjectName, cIssuer2Name, Integer.MAX_VALUE);
                if (debug != null) {
                    debug.println(METHOD_NME +" distanceTto1: " + distanceTto1);
                    debug.println(METHOD_NME +" distanceTto2: " + distanceTto2);
                }
                if (distanceTto1 < 0 || distanceTto2 < 0) {
                    if (distanceTto1 == distanceTto2) {
                        return -1;
                    } else if (distanceTto1 < 0 && distanceTto2 >= 0) {
                        return -1;
                    } else if (distanceTto1 >= 0 && distanceTto2 < 0) {
                        return 1;
                    } else if (distanceTto1 > distanceTto2) {
                        return -1;
                    } else {
                        return 1;
                    }
                }
            }
            if (debug != null) {
                debug.println(METHOD_NME +" SAME NAMESPACE AS TRUSTED TEST...");
            }
            for (X500Principal tSubject : trustedSubjectDNs) {
                X500Name tSubjectName = X500Name.asX500Name(tSubject);
                X500Name tAo1 = tSubjectName.commonAncestor(cIssuer1Name);
                X500Name tAo2 = tSubjectName.commonAncestor(cIssuer2Name);
                if (debug != null) {
                    debug.println(METHOD_NME +" tAo1: " + String.valueOf(tAo1));
                    debug.println(METHOD_NME +" tAo2: " + String.valueOf(tAo2));
                }
                if (tAo1 != null || tAo2 != null) {
                    if (tAo1 != null && tAo2 != null) {
                        int hopsTto1 = Builder.hops
                            (tSubjectName, cIssuer1Name, Integer.MAX_VALUE);
                        int hopsTto2 = Builder.hops
                            (tSubjectName, cIssuer2Name, Integer.MAX_VALUE);
                        if (debug != null) {
                            debug.println(METHOD_NME +" hopsTto1: " + hopsTto1);
                            debug.println(METHOD_NME +" hopsTto2: " + hopsTto2);
                        }
                        if (hopsTto1 == hopsTto2) {
                        } else if (hopsTto1 > hopsTto2) {
                            return 1;
                        } else {  
                            return -1;
                        }
                    } else if (tAo1 == null) {
                        return 1;
                    } else {
                        return -1;
                    }
                }
            }
            if (debug != null) {
                debug.println(METHOD_NME+" CERT ISSUER/SUBJECT COMPARISON TEST...");
            }
            X500Principal cSubject1 = oCert1.getSubjectX500Principal();
            X500Principal cSubject2 = oCert2.getSubjectX500Principal();
            X500Name cSubject1Name = X500Name.asX500Name(cSubject1);
            X500Name cSubject2Name = X500Name.asX500Name(cSubject2);
            if (debug != null) {
                debug.println(METHOD_NME + " o1 Subject: " + cSubject1);
                debug.println(METHOD_NME + " o2 Subject: " + cSubject2);
            }
            int distanceStoI1 = Builder.distance
                (cSubject1Name, cIssuer1Name, Integer.MAX_VALUE);
            int distanceStoI2 = Builder.distance
                (cSubject2Name, cIssuer2Name, Integer.MAX_VALUE);
            if (debug != null) {
                debug.println(METHOD_NME + " distanceStoI1: " + distanceStoI1);
                debug.println(METHOD_NME + " distanceStoI2: " + distanceStoI2);
            }
            if (distanceStoI2 > distanceStoI1) {
                return -1;
            } else if (distanceStoI2 < distanceStoI1) {
                return 1;
            }
            if (debug != null) {
                debug.println(METHOD_NME + " no tests matched; RETURN 0");
            }
            return -1;
        }
    }
    void verifyCert(X509Certificate cert, State currentState,
        List<X509Certificate> certPathList) throws GeneralSecurityException
    {
        if (debug != null) {
            debug.println("ForwardBuilder.verifyCert(SN: "
                + Debug.toHexString(cert.getSerialNumber())
                + "\n  Issuer: " + cert.getIssuerX500Principal() + ")"
                + "\n  Subject: " + cert.getSubjectX500Principal() + ")");
        }
        ForwardState currState = (ForwardState) currentState;
        if (certPathList != null) {
            boolean policyMappingFound = false;
            for (X509Certificate cpListCert : certPathList) {
                X509CertImpl cpListCertImpl = X509CertImpl.toImpl(cpListCert);
                PolicyMappingsExtension policyMappingsExt
                    = cpListCertImpl.getPolicyMappingsExtension();
                if (policyMappingsExt != null) {
                    policyMappingFound = true;
                }
                if (debug != null) {
                    debug.println("policyMappingFound = " + policyMappingFound);
                }
                if (cert.equals(cpListCert)) {
                    if ((buildParams.isPolicyMappingInhibited()) ||
                        (!policyMappingFound)) {
                        if (debug != null) {
                            debug.println("loop detected!!");
                        }
                        throw new CertPathValidatorException("loop detected");
                    }
                }
            }
        }
        boolean isTrustedCert = trustedCerts.contains(cert);
        if (!isTrustedCert) {
            Set<String> unresCritExts = cert.getCriticalExtensionOIDs();
            if (unresCritExts == null) {
                unresCritExts = Collections.<String>emptySet();
            }
            for (PKIXCertPathChecker checker : currState.forwardCheckers) {
                checker.check(cert, unresCritExts);
            }
            for (PKIXCertPathChecker checker : buildParams.getCertPathCheckers()) {
                if (!checker.isForwardCheckingSupported()) {
                    Set<String> supportedExts = checker.getSupportedExtensions();
                    if (supportedExts != null) {
                        unresCritExts.removeAll(supportedExts);
                    }
                }
            }
            if (!unresCritExts.isEmpty()) {
                unresCritExts.remove(
                    PKIXExtensions.BasicConstraints_Id.toString());
                unresCritExts.remove(
                    PKIXExtensions.NameConstraints_Id.toString());
                unresCritExts.remove(
                    PKIXExtensions.CertificatePolicies_Id.toString());
                unresCritExts.remove(
                    PKIXExtensions.PolicyMappings_Id.toString());
                unresCritExts.remove(
                    PKIXExtensions.PolicyConstraints_Id.toString());
                unresCritExts.remove(
                    PKIXExtensions.InhibitAnyPolicy_Id.toString());
                unresCritExts.remove(
                    PKIXExtensions.SubjectAlternativeName_Id.toString());
                unresCritExts.remove(PKIXExtensions.KeyUsage_Id.toString());
                unresCritExts.remove(
                    PKIXExtensions.ExtendedKeyUsage_Id.toString());
                if (!unresCritExts.isEmpty())
                    throw new CertPathValidatorException
                        ("Unrecognized critical extension(s)", null, null, -1,
                         PKIXReason.UNRECOGNIZED_CRIT_EXT);
            }
        }
        if (currState.isInitial()) {
            return;
        }
        if (!isTrustedCert) {
            if (cert.getBasicConstraints() == -1) {
                throw new CertificateException("cert is NOT a CA cert");
            }
            KeyChecker.verifyCAKeyUsage(cert);
        }
        if (buildParams.isRevocationEnabled()) {
            if (CrlRevocationChecker.certCanSignCrl(cert)) {
                if (!currState.keyParamsNeeded())
                    currState.crlChecker.check(currState.cert,
                                               cert.getPublicKey(),
                                               true);
            }
        }
        if (!currState.keyParamsNeeded()) {
            (currState.cert).verify(cert.getPublicKey(),
                                    buildParams.getSigProvider());
        }
    }
    boolean isPathCompleted(X509Certificate cert) {
        for (TrustAnchor anchor : trustAnchors) {
            if (anchor.getTrustedCert() != null) {
                if (cert.equals(anchor.getTrustedCert())) {
                    this.trustAnchor = anchor;
                    return true;
                } else {
                    continue;
                }
            } else {
                X500Principal principal = anchor.getCA();
                java.security.PublicKey publicKey = anchor.getCAPublicKey();
                if (principal != null && publicKey != null &&
                        principal.equals(cert.getSubjectX500Principal())) {
                    if (publicKey.equals(cert.getPublicKey())) {
                        this.trustAnchor = anchor;
                        return true;
                    }
                }
                if (principal == null ||
                        !principal.equals(cert.getIssuerX500Principal())) {
                    continue;
                }
            }
            if (buildParams.isRevocationEnabled()) {
                try {
                    CrlRevocationChecker crlChecker = new CrlRevocationChecker
                        (anchor, buildParams, null, onlyEECert);
                    crlChecker.check(cert, anchor.getCAPublicKey(), true);
                } catch (CertPathValidatorException cpve) {
                    if (debug != null) {
                        debug.println("ForwardBuilder.isPathCompleted() cpve");
                        cpve.printStackTrace();
                    }
                    continue;
                }
            }
            try {
                cert.verify(anchor.getCAPublicKey(),
                            buildParams.getSigProvider());
            } catch (InvalidKeyException ike) {
                if (debug != null) {
                    debug.println("ForwardBuilder.isPathCompleted() invalid "
                        + "DSA key found");
                }
                continue;
            } catch (Exception e){
                if (debug != null) {
                    debug.println("ForwardBuilder.isPathCompleted() " +
                        "unexpected exception");
                    e.printStackTrace();
                }
                continue;
            }
            this.trustAnchor = anchor;
            return true;
        }
        return false;
    }
    void addCertToPath(X509Certificate cert,
        LinkedList<X509Certificate> certPathList) {
        certPathList.addFirst(cert);
    }
    void removeFinalCertFromPath(LinkedList<X509Certificate> certPathList) {
        certPathList.removeFirst();
    }
}
