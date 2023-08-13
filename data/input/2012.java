public abstract class Builder {
    private static final Debug debug = Debug.getInstance("certpath");
    private Set<String> matchingPolicies;
    final PKIXBuilderParameters buildParams;
    final X500Principal targetSubjectDN;
    final Date date;
    final X509CertSelector targetCertConstraints;
    final static boolean USE_AIA = AccessController.doPrivileged
        (new GetBooleanAction("com.sun.security.enableAIAcaIssuers"));
    Builder(PKIXBuilderParameters buildParams, X500Principal targetSubjectDN) {
        this.buildParams = buildParams;
        this.targetSubjectDN = targetSubjectDN;
        Date paramsDate = buildParams.getDate();
        this.date = paramsDate != null ? paramsDate : new Date();
        this.targetCertConstraints =
            (X509CertSelector) buildParams.getTargetCertConstraints();
    }
    abstract Collection<X509Certificate> getMatchingCerts
        (State currentState, List<CertStore> certStores)
        throws CertStoreException, CertificateException, IOException;
    abstract void verifyCert(X509Certificate cert, State currentState,
        List<X509Certificate> certPathList) throws GeneralSecurityException;
    abstract boolean isPathCompleted(X509Certificate cert);
    abstract void addCertToPath(X509Certificate cert,
        LinkedList<X509Certificate> certPathList);
    abstract void removeFinalCertFromPath
        (LinkedList<X509Certificate> certPathList);
    static int distance(GeneralNameInterface base,
        GeneralNameInterface test, int incomparable) {
        switch (base.constrains(test)) {
        case GeneralNameInterface.NAME_DIFF_TYPE:
            if (debug != null) {
                debug.println("Builder.distance(): Names are different types");
            }
        case GeneralNameInterface.NAME_SAME_TYPE:
            if (debug != null) {
                debug.println("Builder.distance(): Names are same type but " +
                    "in different subtrees");
            }
            return incomparable;
        case GeneralNameInterface.NAME_MATCH:
            return 0;
        case GeneralNameInterface.NAME_WIDENS:
            break;
        case GeneralNameInterface.NAME_NARROWS:
            break;
        default: 
            return incomparable;
        }
        return test.subtreeDepth() - base.subtreeDepth();
    }
    static int hops(GeneralNameInterface base, GeneralNameInterface test,
        int incomparable) {
        int baseRtest = base.constrains(test);
        switch (baseRtest) {
        case GeneralNameInterface.NAME_DIFF_TYPE:
            if (debug != null) {
                debug.println("Builder.hops(): Names are different types");
            }
            return incomparable;
        case GeneralNameInterface.NAME_SAME_TYPE:
            break;
        case GeneralNameInterface.NAME_MATCH:
            return 0;
        case GeneralNameInterface.NAME_WIDENS:
            return (test.subtreeDepth()-base.subtreeDepth());
        case GeneralNameInterface.NAME_NARROWS:
            return (test.subtreeDepth()-base.subtreeDepth());
        default: 
            return incomparable;
        }
        if (base.getType() != GeneralNameInterface.NAME_DIRECTORY) {
            if (debug != null) {
                debug.println("Builder.hops(): hopDistance not implemented " +
                    "for this name type");
            }
            return incomparable;
        }
        X500Name baseName = (X500Name)base;
        X500Name testName = (X500Name)test;
        X500Name commonName = baseName.commonAncestor(testName);
        if (commonName == null) {
            if (debug != null) {
                debug.println("Builder.hops(): Names are in different " +
                    "namespaces");
            }
            return incomparable;
        } else {
            int commonDistance = commonName.subtreeDepth();
            int baseDistance = baseName.subtreeDepth();
            int testDistance = testName.subtreeDepth();
            return (baseDistance + testDistance - (2 * commonDistance));
        }
    }
    static int targetDistance(NameConstraintsExtension constraints,
            X509Certificate cert, GeneralNameInterface target)
            throws IOException {
        if (constraints != null && !constraints.verify(cert)) {
            throw new IOException("certificate does not satisfy existing name "
                + "constraints");
        }
        X509CertImpl certImpl;
        try {
            certImpl = X509CertImpl.toImpl(cert);
        } catch (CertificateException e) {
            throw (IOException)new IOException("Invalid certificate").initCause(e);
        }
        X500Name subject = X500Name.asX500Name(certImpl.getSubjectX500Principal());
        if (subject.equals(target)) {
            return 0;
        }
        SubjectAlternativeNameExtension altNameExt =
            certImpl.getSubjectAlternativeNameExtension();
        if (altNameExt != null) {
            GeneralNames altNames =
                (GeneralNames)altNameExt.get(altNameExt.SUBJECT_NAME);
            if (altNames != null) {
                for (int j = 0, n = altNames.size(); j < n; j++) {
                    GeneralNameInterface altName = altNames.get(j).getName();
                    if (altName.equals(target)) {
                        return 0;
                    }
                }
            }
        }
        NameConstraintsExtension ncExt = certImpl.getNameConstraintsExtension();
        if (ncExt == null) {
            return -1;
        }
        if (constraints != null) {
            constraints.merge(ncExt);
        } else {
            constraints = (NameConstraintsExtension) ncExt.clone();
        }
        if (debug != null) {
            debug.println("Builder.targetDistance() merged constraints: "
                + String.valueOf(constraints));
        }
        GeneralSubtrees permitted = (GeneralSubtrees)
            constraints.get(constraints.PERMITTED_SUBTREES);
        GeneralSubtrees excluded = (GeneralSubtrees)
            constraints.get(constraints.EXCLUDED_SUBTREES);
        if (permitted != null) {
            permitted.reduce(excluded);
        }
        if (debug != null) {
            debug.println("Builder.targetDistance() reduced constraints: "
                + permitted);
        }
        if (!constraints.verify(target)) {
            throw new IOException("New certificate not allowed to sign "
                + "certificate for target");
        }
        if (permitted == null) {
            return -1;
        }
        for (int i = 0, n = permitted.size(); i < n; i++) {
            GeneralNameInterface perName = permitted.get(i).getName().getName();
            int distance = distance(perName, target, -1);
            if (distance >= 0) {
                return (distance + 1);
            }
        }
        return -1;
    }
    Set<String> getMatchingPolicies() {
        if (matchingPolicies != null) {
            Set<String> initialPolicies = buildParams.getInitialPolicies();
            if ((!initialPolicies.isEmpty()) &&
                (!initialPolicies.contains(PolicyChecker.ANY_POLICY)) &&
                (buildParams.isPolicyMappingInhibited()))
            {
                initialPolicies.add(PolicyChecker.ANY_POLICY);
                matchingPolicies = initialPolicies;
            } else {
                matchingPolicies = Collections.<String>emptySet();
            }
        }
        return matchingPolicies;
    }
    boolean addMatchingCerts(X509CertSelector selector,
            Collection<CertStore> certStores,
            Collection<X509Certificate> resultCerts, boolean checkAll) {
        X509Certificate targetCert = selector.getCertificate();
        if (targetCert != null) {
            if (selector.match(targetCert) && !X509CertImpl.isSelfSigned
                (targetCert, buildParams.getSigProvider())) {
                if (debug != null) {
                    debug.println("Builder.addMatchingCerts: adding target cert");
                }
                return resultCerts.add(targetCert);
            }
            return false;
        }
        boolean add = false;
        for (CertStore store : certStores) {
            try {
                Collection<? extends Certificate> certs =
                                        store.getCertificates(selector);
                for (Certificate cert : certs) {
                    if (!X509CertImpl.isSelfSigned
                        ((X509Certificate)cert, buildParams.getSigProvider())) {
                        if (resultCerts.add((X509Certificate)cert)) {
                            add = true;
                        }
                    }
                }
                if (!checkAll && add) {
                    return true;
                }
            } catch (CertStoreException cse) {
                if (debug != null) {
                    debug.println("Builder.addMatchingCerts, non-fatal " +
                        "exception retrieving certs: " + cse);
                    cse.printStackTrace();
                }
            }
        }
        return add;
    }
    static boolean isLocalCertStore(CertStore certStore) {
        return (certStore.getType().equals("Collection") ||
                certStore.getCertStoreParameters() instanceof
                CollectionCertStoreParameters);
    }
}
