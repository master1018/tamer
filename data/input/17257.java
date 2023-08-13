public class PKIXCertPathValidator extends CertPathValidatorSpi {
    private static final Debug debug = Debug.getInstance("certpath");
    private Date testDate;
    private List<PKIXCertPathChecker> userCheckers;
    private String sigProvider;
    private BasicChecker basicChecker;
    private boolean ocspEnabled = false;
    private boolean onlyEECert = false;
    public PKIXCertPathValidator() {}
    public CertPathValidatorResult engineValidate(CertPath cp,
        CertPathParameters param)
        throws CertPathValidatorException, InvalidAlgorithmParameterException
    {
        if (debug != null)
            debug.println("PKIXCertPathValidator.engineValidate()...");
        if (!(param instanceof PKIXParameters)) {
            throw new InvalidAlgorithmParameterException("inappropriate "
                + "parameters, must be an instance of PKIXParameters");
        }
        if (!cp.getType().equals("X.509") && !cp.getType().equals("X509")) {
            throw new InvalidAlgorithmParameterException("inappropriate "
                + "certification path type specified, must be X.509 or X509");
        }
        PKIXParameters pkixParam = (PKIXParameters) param;
        Set<TrustAnchor> anchors = pkixParam.getTrustAnchors();
        for (TrustAnchor anchor : anchors) {
            if (anchor.getNameConstraints() != null) {
                throw new InvalidAlgorithmParameterException
                    ("name constraints in trust anchor not supported");
            }
        }
        ArrayList<X509Certificate> certList = new ArrayList<X509Certificate>
            ((List<X509Certificate>)cp.getCertificates());
        if (debug != null) {
            if (certList.isEmpty()) {
                debug.println("PKIXCertPathValidator.engineValidate() "
                    + "certList is empty");
            }
            debug.println("PKIXCertPathValidator.engineValidate() "
                + "reversing certpath...");
        }
        Collections.reverse(certList);
        populateVariables(pkixParam);
        X509Certificate firstCert = null;
        if (!certList.isEmpty()) {
            firstCert = certList.get(0);
        }
        CertPathValidatorException lastException = null;
        for (TrustAnchor anchor : anchors) {
            X509Certificate trustedCert = anchor.getTrustedCert();
            if (trustedCert != null) {
                if (debug != null) {
                    debug.println("PKIXCertPathValidator.engineValidate() "
                        + "anchor.getTrustedCert() != null");
                }
                if (!isWorthTrying(trustedCert, firstCert)) {
                    continue;
                }
                if (debug != null) {
                    debug.println("anchor.getTrustedCert()."
                        + "getSubjectX500Principal() = "
                        + trustedCert.getSubjectX500Principal());
                }
            } else {
                if (debug != null) {
                    debug.println("PKIXCertPathValidator.engineValidate(): "
                        + "anchor.getTrustedCert() == null");
                }
            }
            try {
                PolicyNodeImpl rootNode = new PolicyNodeImpl(null,
                    PolicyChecker.ANY_POLICY, null, false,
                    Collections.singleton(PolicyChecker.ANY_POLICY), false);
                PolicyNode policyTree =
                    doValidate(anchor, cp, certList, pkixParam, rootNode);
                return new PKIXCertPathValidatorResult(anchor, policyTree,
                    basicChecker.getPublicKey());
            } catch (CertPathValidatorException cpe) {
                lastException = cpe;
            }
        }
        if (lastException != null) {
            throw lastException;
        }
        throw new CertPathValidatorException
            ("Path does not chain with any of the trust anchors",
             null, null, -1, PKIXReason.NO_TRUST_ANCHOR);
    }
    private boolean isWorthTrying(X509Certificate trustedCert,
          X509Certificate firstCert) {
        boolean worthy = false;
        if (debug != null) {
            debug.println("PKIXCertPathValidator.isWorthTrying() checking "
                + "if this trusted cert is worth trying ...");
        }
        if (firstCert == null) {
            return true;
        }
        AdaptableX509CertSelector issuerSelector =
                        new AdaptableX509CertSelector();
        issuerSelector.setSubject(firstCert.getIssuerX500Principal());
        issuerSelector.setValidityPeriod(firstCert.getNotBefore(),
                                                firstCert.getNotAfter());
        try {
            X509CertImpl firstCertImpl = X509CertImpl.toImpl(firstCert);
            issuerSelector.parseAuthorityKeyIdentifierExtension(
                        firstCertImpl.getAuthorityKeyIdentifierExtension());
            worthy = issuerSelector.match(trustedCert);
        } catch (Exception e) {
        }
        if (debug != null) {
            if (worthy) {
                debug.println("YES - try this trustedCert");
            } else {
                debug.println("NO - don't try this trustedCert");
            }
        }
        return worthy;
    }
    private void populateVariables(PKIXParameters pkixParam)
    {
        testDate = pkixParam.getDate();
        if (testDate == null) {
            testDate = new Date(System.currentTimeMillis());
        }
        userCheckers = pkixParam.getCertPathCheckers();
        sigProvider = pkixParam.getSigProvider();
        if (pkixParam.isRevocationEnabled()) {
            ocspEnabled = AccessController.doPrivileged(
                new GetBooleanSecurityPropertyAction
                    (OCSPChecker.OCSP_ENABLE_PROP));
            onlyEECert = AccessController.doPrivileged(
                new GetBooleanSecurityPropertyAction
                    ("com.sun.security.onlyCheckRevocationOfEECert"));
        }
    }
    private PolicyNode doValidate(
            TrustAnchor anchor, CertPath cpOriginal,
            ArrayList<X509Certificate> certList, PKIXParameters pkixParam,
            PolicyNodeImpl rootNode) throws CertPathValidatorException
    {
        int certPathLen = certList.size();
        basicChecker = new BasicChecker(anchor, testDate, sigProvider, false);
        AlgorithmChecker algorithmChecker = new AlgorithmChecker(anchor);
        KeyChecker keyChecker = new KeyChecker(certPathLen,
            pkixParam.getTargetCertConstraints());
        ConstraintsChecker constraintsChecker =
            new ConstraintsChecker(certPathLen);
        PolicyChecker policyChecker =
            new PolicyChecker(pkixParam.getInitialPolicies(), certPathLen,
                              pkixParam.isExplicitPolicyRequired(),
                              pkixParam.isPolicyMappingInhibited(),
                              pkixParam.isAnyPolicyInhibited(),
                              pkixParam.getPolicyQualifiersRejected(),
                              rootNode);
        ArrayList<PKIXCertPathChecker> certPathCheckers =
            new ArrayList<PKIXCertPathChecker>();
        certPathCheckers.add(algorithmChecker);
        certPathCheckers.add(keyChecker);
        certPathCheckers.add(constraintsChecker);
        certPathCheckers.add(policyChecker);
        certPathCheckers.add(basicChecker);
        if (pkixParam.isRevocationEnabled()) {
            if (ocspEnabled) {
                OCSPChecker ocspChecker =
                    new OCSPChecker(cpOriginal, pkixParam, onlyEECert);
                certPathCheckers.add(ocspChecker);
            }
            CrlRevocationChecker revocationChecker = new
                CrlRevocationChecker(anchor, pkixParam, certList, onlyEECert);
            certPathCheckers.add(revocationChecker);
        }
        certPathCheckers.addAll(userCheckers);
        PKIXMasterCertPathValidator masterValidator =
            new PKIXMasterCertPathValidator(certPathCheckers);
        masterValidator.validate(cpOriginal, certList);
        return policyChecker.getPolicyTree();
    }
}
