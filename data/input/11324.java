class CrlRevocationChecker extends PKIXCertPathChecker {
    private static final Debug debug = Debug.getInstance("certpath");
    private final TrustAnchor mAnchor;
    private final List<CertStore> mStores;
    private final String mSigProvider;
    private final Date mCurrentTime;
    private PublicKey mPrevPubKey;
    private boolean mCRLSignFlag;
    private HashSet<X509CRL> mPossibleCRLs;
    private HashSet<X509CRL> mApprovedCRLs;
    private final PKIXParameters mParams;
    private static final boolean [] mCrlSignUsage =
        { false, false, false, false, false, false, true };
    private static final boolean[] ALL_REASONS =
        {true, true, true, true, true, true, true, true, true};
    private boolean mOnlyEECert = false;
    private static final long MAX_CLOCK_SKEW = 900000;
    CrlRevocationChecker(TrustAnchor anchor, PKIXParameters params)
        throws CertPathValidatorException
    {
        this(anchor, params, null);
    }
    CrlRevocationChecker(TrustAnchor anchor, PKIXParameters params,
        Collection<X509Certificate> certs) throws CertPathValidatorException
    {
        this(anchor, params, certs, false);
    }
    CrlRevocationChecker(TrustAnchor anchor, PKIXParameters params,
        Collection<X509Certificate> certs, boolean onlyEECert)
        throws CertPathValidatorException {
        mAnchor = anchor;
        mParams = params;
        mStores = new ArrayList<CertStore>(params.getCertStores());
        mSigProvider = params.getSigProvider();
        if (certs != null) {
            try {
                mStores.add(CertStore.getInstance("Collection",
                    new CollectionCertStoreParameters(certs)));
            } catch (Exception e) {
                if (debug != null) {
                    debug.println("CrlRevocationChecker: " +
                        "error creating Collection CertStore: " + e);
                }
            }
        }
        Date testDate = params.getDate();
        mCurrentTime = (testDate != null ? testDate : new Date());
        mOnlyEECert = onlyEECert;
        init(false);
    }
    public void init(boolean forward) throws CertPathValidatorException
    {
        if (!forward) {
            if (mAnchor != null) {
                if (mAnchor.getCAPublicKey() != null) {
                    mPrevPubKey = mAnchor.getCAPublicKey();
                } else {
                    mPrevPubKey = mAnchor.getTrustedCert().getPublicKey();
                }
            } else {
                mPrevPubKey = null;
            }
            mCRLSignFlag = true;
        } else {
            throw new CertPathValidatorException("forward checking "
                                + "not supported");
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
        verifyRevocationStatus(currCert, mPrevPubKey, mCRLSignFlag, true);
        PublicKey cKey = currCert.getPublicKey();
        if (cKey instanceof DSAPublicKey &&
            ((DSAPublicKey)cKey).getParams() == null) {
            cKey = BasicChecker.makeInheritedParamsKey(cKey, mPrevPubKey);
        }
        mPrevPubKey = cKey;
        mCRLSignFlag = certCanSignCrl(currCert);
    }
    public boolean check(X509Certificate currCert, PublicKey prevKey,
        boolean signFlag) throws CertPathValidatorException
    {
        verifyRevocationStatus(currCert, prevKey, signFlag, true);
        return certCanSignCrl(currCert);
    }
    static boolean certCanSignCrl(X509Certificate currCert) {
        boolean[] kbools = currCert.getKeyUsage();
        if (kbools != null) {
            return kbools[6];
        }
        return false;
    }
    private void verifyRevocationStatus(X509Certificate currCert,
        PublicKey prevKey, boolean signFlag, boolean allowSeparateKey)
        throws CertPathValidatorException
    {
        verifyRevocationStatus(currCert, prevKey, signFlag,
                   allowSeparateKey, null, mParams.getTrustAnchors());
    }
    private void verifyRevocationStatus(X509Certificate currCert,
        PublicKey prevKey, boolean signFlag, boolean allowSeparateKey,
        Set<X509Certificate> stackedCerts,
        Set<TrustAnchor> trustAnchors) throws CertPathValidatorException {
        String msg = "revocation status";
        if (debug != null) {
            debug.println("CrlRevocationChecker.verifyRevocationStatus()" +
                " ---checking " + msg + "...");
        }
        if (mOnlyEECert && currCert.getBasicConstraints() != -1) {
            if (debug != null) {
                debug.println("Skipping revocation check, not end entity cert");
            }
            return;
        }
        if ((stackedCerts != null) && stackedCerts.contains(currCert)) {
            if (debug != null) {
                debug.println("CrlRevocationChecker.verifyRevocationStatus()" +
                    " circular dependency");
            }
            throw new CertPathValidatorException
                ("Could not determine revocation status", null, null, -1,
                 BasicReason.UNDETERMINED_REVOCATION_STATUS);
        }
        mPossibleCRLs = new HashSet<X509CRL>();
        mApprovedCRLs = new HashSet<X509CRL>();
        boolean[] reasonsMask = new boolean[9];
        try {
            X509CRLSelector sel = new X509CRLSelector();
            sel.setCertificateChecking(currCert);
            CertPathHelper.setDateAndTime(sel, mCurrentTime, MAX_CLOCK_SKEW);
            for (CertStore mStore : mStores) {
                for (java.security.cert.CRL crl : mStore.getCRLs(sel)) {
                    mPossibleCRLs.add((X509CRL)crl);
                }
            }
            DistributionPointFetcher store =
                DistributionPointFetcher.getInstance();
            mApprovedCRLs.addAll(store.getCRLs(sel, signFlag, prevKey,
                mSigProvider, mStores, reasonsMask, trustAnchors,
                mParams.getDate()));
        } catch (Exception e) {
            if (debug != null) {
                debug.println("CrlRevocationChecker.verifyRevocationStatus() "
                    + "unexpected exception: " + e.getMessage());
            }
            throw new CertPathValidatorException(e);
        }
        if (debug != null) {
            debug.println("CrlRevocationChecker.verifyRevocationStatus() " +
                "crls.size() = " + mPossibleCRLs.size());
        }
        if (!mPossibleCRLs.isEmpty()) {
            mApprovedCRLs.addAll(verifyPossibleCRLs(mPossibleCRLs, currCert,
                signFlag, prevKey, reasonsMask, trustAnchors));
        }
        if (debug != null) {
            debug.println("CrlRevocationChecker.verifyRevocationStatus() " +
                "approved crls.size() = " + mApprovedCRLs.size());
        }
        if (mApprovedCRLs.isEmpty() ||
            !Arrays.equals(reasonsMask, ALL_REASONS)) {
            if (allowSeparateKey) {
                verifyWithSeparateSigningKey(currCert, prevKey, signFlag,
                                             stackedCerts);
                return;
            } else {
                throw new CertPathValidatorException
                ("Could not determine revocation status", null, null, -1,
                 BasicReason.UNDETERMINED_REVOCATION_STATUS);
            }
        }
        if (debug != null) {
            BigInteger sn = currCert.getSerialNumber();
            debug.println("CrlRevocationChecker.verifyRevocationStatus() " +
                            "starting the final sweep...");
            debug.println("CrlRevocationChecker.verifyRevocationStatus" +
                            " cert SN: " + sn.toString());
        }
        CRLReason reasonCode = CRLReason.UNSPECIFIED;
        X509CRLEntryImpl entry = null;
        for (X509CRL crl : mApprovedCRLs) {
            X509CRLEntry e = crl.getRevokedCertificate(currCert);
            if (e != null) {
                try {
                    entry = X509CRLEntryImpl.toImpl(e);
                } catch (CRLException ce) {
                    throw new CertPathValidatorException(ce);
                }
                if (debug != null) {
                    debug.println("CrlRevocationChecker.verifyRevocationStatus"
                        + " CRL entry: " + entry.toString());
                }
                Set<String> unresCritExts = entry.getCriticalExtensionOIDs();
                if (unresCritExts != null && !unresCritExts.isEmpty()) {
                    unresCritExts.remove
                        (PKIXExtensions.ReasonCode_Id.toString());
                    unresCritExts.remove
                        (PKIXExtensions.CertificateIssuer_Id.toString());
                    if (!unresCritExts.isEmpty()) {
                        if (debug != null) {
                            debug.println("Unrecognized "
                            + "critical extension(s) in revoked CRL entry: "
                            + unresCritExts);
                        }
                        throw new CertPathValidatorException
                        ("Could not determine revocation status", null, null,
                         -1, BasicReason.UNDETERMINED_REVOCATION_STATUS);
                    }
                }
                reasonCode = entry.getRevocationReason();
                if (reasonCode == null) {
                    reasonCode = CRLReason.UNSPECIFIED;
                }
                Throwable t = new CertificateRevokedException
                    (entry.getRevocationDate(), reasonCode,
                     crl.getIssuerX500Principal(), entry.getExtensions());
                throw new CertPathValidatorException(t.getMessage(), t,
                    null, -1, BasicReason.REVOKED);
            }
        }
    }
    private void verifyWithSeparateSigningKey(X509Certificate currCert,
        PublicKey prevKey, boolean signFlag, Set<X509Certificate> stackedCerts)
        throws CertPathValidatorException {
        String msg = "revocation status";
        if (debug != null) {
            debug.println(
                "CrlRevocationChecker.verifyWithSeparateSigningKey()" +
                " ---checking " + msg + "...");
        }
        if ((stackedCerts != null) && stackedCerts.contains(currCert)) {
            if (debug != null) {
                debug.println(
                    "CrlRevocationChecker.verifyWithSeparateSigningKey()" +
                    " circular dependency");
            }
            throw new CertPathValidatorException
                ("Could not determine revocation status", null, null,
                 -1, BasicReason.UNDETERMINED_REVOCATION_STATUS);
        }
        if (!signFlag) {
            prevKey = null;
        }
        buildToNewKey(currCert, prevKey, stackedCerts);
    }
    private void buildToNewKey(X509Certificate currCert,
        PublicKey prevKey, Set<X509Certificate> stackedCerts)
        throws CertPathValidatorException {
        if (debug != null) {
            debug.println("CrlRevocationChecker.buildToNewKey()" +
                          " starting work");
        }
        Set<PublicKey> badKeys = new HashSet<PublicKey>();
        if (prevKey != null) {
            badKeys.add(prevKey);
        }
        X509CertSelector certSel = new RejectKeySelector(badKeys);
        certSel.setSubject(currCert.getIssuerX500Principal());
        certSel.setKeyUsage(mCrlSignUsage);
        Set<TrustAnchor> newAnchors =
            (mAnchor == null ? mParams.getTrustAnchors() :
                                Collections.singleton(mAnchor));
        PKIXBuilderParameters builderParams;
        if (mParams instanceof PKIXBuilderParameters) {
            builderParams = (PKIXBuilderParameters) mParams.clone();
            builderParams.setTargetCertConstraints(certSel);
            builderParams.setPolicyQualifiersRejected(true);
            try {
                builderParams.setTrustAnchors(newAnchors);
            } catch (InvalidAlgorithmParameterException iape) {
                throw new RuntimeException(iape); 
            }
        } else {
            try {
                builderParams = new PKIXBuilderParameters(newAnchors, certSel);
            } catch (InvalidAlgorithmParameterException iape) {
                throw new RuntimeException(iape); 
            }
            builderParams.setInitialPolicies(mParams.getInitialPolicies());
            builderParams.setCertStores(mStores);
            builderParams.setExplicitPolicyRequired
                (mParams.isExplicitPolicyRequired());
            builderParams.setPolicyMappingInhibited
                (mParams.isPolicyMappingInhibited());
            builderParams.setAnyPolicyInhibited(mParams.isAnyPolicyInhibited());
            builderParams.setDate(mParams.getDate());
            builderParams.setCertPathCheckers(mParams.getCertPathCheckers());
            builderParams.setSigProvider(mParams.getSigProvider());
        }
        builderParams.setRevocationEnabled(false);
        if (Builder.USE_AIA == true) {
            X509CertImpl currCertImpl = null;
            try {
                currCertImpl = X509CertImpl.toImpl(currCert);
            } catch (CertificateException ce) {
                if (debug != null) {
                    debug.println("CrlRevocationChecker.buildToNewKey: " +
                        "error decoding cert: " + ce);
                }
            }
            AuthorityInfoAccessExtension aiaExt = null;
            if (currCertImpl != null) {
                aiaExt = currCertImpl.getAuthorityInfoAccessExtension();
            }
            if (aiaExt != null) {
                List<AccessDescription> adList = aiaExt.getAccessDescriptions();
                if (adList != null) {
                    for (AccessDescription ad : adList) {
                        CertStore cs = URICertStore.getInstance(ad);
                        if (cs != null) {
                            if (debug != null) {
                                debug.println("adding AIAext CertStore");
                            }
                            builderParams.addCertStore(cs);
                        }
                    }
                }
            }
        }
        CertPathBuilder builder = null;
        try {
            builder = CertPathBuilder.getInstance("PKIX");
        } catch (NoSuchAlgorithmException nsae) {
            throw new CertPathValidatorException(nsae);
        }
        while (true) {
            try {
                if (debug != null) {
                    debug.println("CrlRevocationChecker.buildToNewKey()" +
                                  " about to try build ...");
                }
                PKIXCertPathBuilderResult cpbr =
                    (PKIXCertPathBuilderResult) builder.build(builderParams);
                if (debug != null) {
                    debug.println("CrlRevocationChecker.buildToNewKey()" +
                                  " about to check revocation ...");
                }
                if (stackedCerts == null) {
                    stackedCerts = new HashSet<X509Certificate>();
                }
                stackedCerts.add(currCert);
                TrustAnchor ta = cpbr.getTrustAnchor();
                PublicKey prevKey2 = ta.getCAPublicKey();
                if (prevKey2 == null) {
                    prevKey2 = ta.getTrustedCert().getPublicKey();
                }
                boolean signFlag = true;
                List<? extends Certificate> cpList =
                    cpbr.getCertPath().getCertificates();
                try {
                    for (int i = cpList.size()-1; i >= 0; i-- ) {
                        X509Certificate cert = (X509Certificate) cpList.get(i);
                        if (debug != null) {
                            debug.println("CrlRevocationChecker.buildToNewKey()"
                                + " index " + i + " checking " + cert);
                        }
                        verifyRevocationStatus(cert, prevKey2, signFlag, true,
                                stackedCerts, newAnchors);
                        signFlag = certCanSignCrl(cert);
                        prevKey2 = cert.getPublicKey();
                    }
                } catch (CertPathValidatorException cpve) {
                    badKeys.add(cpbr.getPublicKey());
                    continue;
                }
                if (debug != null) {
                    debug.println("CrlRevocationChecker.buildToNewKey()" +
                                  " got key " + cpbr.getPublicKey());
                }
                PublicKey newKey = cpbr.getPublicKey();
                try {
                    verifyRevocationStatus(currCert, newKey, true, false);
                    return;
                } catch (CertPathValidatorException cpve) {
                    if (cpve.getReason() == BasicReason.REVOKED) {
                        throw cpve;
                    }
                }
                badKeys.add(newKey);
            } catch (InvalidAlgorithmParameterException iape) {
                throw new CertPathValidatorException(iape);
            } catch (CertPathBuilderException cpbe) {
                throw new CertPathValidatorException
                    ("Could not determine revocation status", null, null,
                     -1, BasicReason.UNDETERMINED_REVOCATION_STATUS);
            }
        }
    }
    private static class RejectKeySelector extends X509CertSelector {
        private final Set<PublicKey> badKeySet;
        RejectKeySelector(Set<PublicKey> badPublicKeys) {
            this.badKeySet = badPublicKeys;
        }
        public boolean match(Certificate cert) {
            if (!super.match(cert))
                return(false);
            if (badKeySet.contains(cert.getPublicKey())) {
                if (debug != null)
                    debug.println("RejectCertSelector.match: bad key");
                return false;
            }
            if (debug != null)
                debug.println("RejectCertSelector.match: returning true");
            return true;
        }
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("RejectCertSelector: [\n");
            sb.append(super.toString());
            sb.append(badKeySet);
            sb.append("]");
            return sb.toString();
        }
    }
    private Collection<X509CRL> verifyPossibleCRLs(Set<X509CRL> crls,
        X509Certificate cert, boolean signFlag, PublicKey prevKey,
        boolean[] reasonsMask,
        Set<TrustAnchor> trustAnchors) throws CertPathValidatorException {
        try {
            X509CertImpl certImpl = X509CertImpl.toImpl(cert);
            if (debug != null) {
                debug.println("CRLRevocationChecker.verifyPossibleCRLs: " +
                        "Checking CRLDPs for "
                        + certImpl.getSubjectX500Principal());
            }
            CRLDistributionPointsExtension ext =
                certImpl.getCRLDistributionPointsExtension();
            List<DistributionPoint> points = null;
            if (ext == null) {
                X500Name certIssuer = (X500Name)certImpl.getIssuerDN();
                DistributionPoint point = new DistributionPoint
                    (new GeneralNames().add(new GeneralName(certIssuer)),
                     null, null);
                points = Collections.singletonList(point);
            } else {
                points = (List<DistributionPoint>)ext.get(
                                        CRLDistributionPointsExtension.POINTS);
            }
            Set<X509CRL> results = new HashSet<X509CRL>();
            DistributionPointFetcher dpf =
                DistributionPointFetcher.getInstance();
            for (Iterator<DistributionPoint> t = points.iterator();
                 t.hasNext() && !Arrays.equals(reasonsMask, ALL_REASONS); ) {
                DistributionPoint point = t.next();
                for (X509CRL crl : crls) {
                    if (dpf.verifyCRL(certImpl, point, crl, reasonsMask,
                            signFlag, prevKey, mSigProvider,
                            trustAnchors, mStores, mParams.getDate())) {
                        results.add(crl);
                    }
                }
            }
            return results;
        } catch (Exception e) {
            if (debug != null) {
                debug.println("Exception while verifying CRL: "+e.getMessage());
                e.printStackTrace();
            }
            return Collections.emptySet();
        }
    }
}
