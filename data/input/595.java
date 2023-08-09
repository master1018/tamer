class DistributionPointFetcher {
    private static final Debug debug = Debug.getInstance("certpath");
    private static final boolean[] ALL_REASONS =
        {true, true, true, true, true, true, true, true, true};
    private final static boolean USE_CRLDP = AccessController.doPrivileged
        (new GetBooleanAction("com.sun.security.enableCRLDP"));
    private static final DistributionPointFetcher INSTANCE =
        new DistributionPointFetcher();
    private DistributionPointFetcher() {}
    static DistributionPointFetcher getInstance() {
        return INSTANCE;
    }
    Collection<X509CRL> getCRLs(X509CRLSelector selector, boolean signFlag,
        PublicKey prevKey, String provider, List<CertStore> certStores,
        boolean[] reasonsMask, Set<TrustAnchor> trustAnchors,
        Date validity) throws CertStoreException {
        if (USE_CRLDP == false) {
            return Collections.emptySet();
        }
        X509Certificate cert = selector.getCertificateChecking();
        if (cert == null) {
            return Collections.emptySet();
        }
        try {
            X509CertImpl certImpl = X509CertImpl.toImpl(cert);
            if (debug != null) {
                debug.println("DistributionPointFetcher.getCRLs: Checking "
                        + "CRLDPs for " + certImpl.getSubjectX500Principal());
            }
            CRLDistributionPointsExtension ext =
                certImpl.getCRLDistributionPointsExtension();
            if (ext == null) {
                if (debug != null) {
                    debug.println("No CRLDP ext");
                }
                return Collections.emptySet();
            }
            List<DistributionPoint> points = (List<DistributionPoint>)ext.get(
                                        CRLDistributionPointsExtension.POINTS);
            Set<X509CRL> results = new HashSet<X509CRL>();
            for (Iterator<DistributionPoint> t = points.iterator();
                 t.hasNext() && !Arrays.equals(reasonsMask, ALL_REASONS); ) {
                DistributionPoint point = t.next();
                Collection<X509CRL> crls = getCRLs(selector, certImpl,
                    point, reasonsMask, signFlag, prevKey, provider,
                    certStores, trustAnchors, validity);
                results.addAll(crls);
            }
            if (debug != null) {
                debug.println("Returning " + results.size() + " CRLs");
            }
            return results;
        } catch (CertificateException e) {
            return Collections.emptySet();
        } catch (IOException e) {
            return Collections.emptySet();
        }
    }
    private Collection<X509CRL> getCRLs(X509CRLSelector selector,
        X509CertImpl certImpl, DistributionPoint point, boolean[] reasonsMask,
        boolean signFlag, PublicKey prevKey, String provider,
        List<CertStore> certStores, Set<TrustAnchor> trustAnchors,
        Date validity) {
        GeneralNames fullName = point.getFullName();
        if (fullName == null) {
            RDN relativeName = point.getRelativeName();
            if (relativeName == null) {
                return Collections.emptySet();
            }
            try {
                GeneralNames crlIssuers = point.getCRLIssuer();
                if (crlIssuers == null) {
                    fullName = getFullNames
                        ((X500Name) certImpl.getIssuerDN(), relativeName);
                } else {
                    if (crlIssuers.size() != 1) {
                        return Collections.emptySet();
                    } else {
                        fullName = getFullNames
                            ((X500Name) crlIssuers.get(0).getName(), relativeName);
                    }
                }
            } catch (IOException ioe) {
                return Collections.emptySet();
            }
        }
        Collection<X509CRL> possibleCRLs = new ArrayList<X509CRL>();
        Collection<X509CRL> crls = new ArrayList<X509CRL>(2);
        for (Iterator<GeneralName> t = fullName.iterator(); t.hasNext(); ) {
            GeneralName name = t.next();
            if (name.getType() == GeneralNameInterface.NAME_DIRECTORY) {
                X500Name x500Name = (X500Name) name.getName();
                possibleCRLs.addAll(
                    getCRLs(x500Name, certImpl.getIssuerX500Principal(),
                            certStores));
            } else if (name.getType() == GeneralNameInterface.NAME_URI) {
                URIName uriName = (URIName)name.getName();
                X509CRL crl = getCRL(uriName);
                if (crl != null) {
                    possibleCRLs.add(crl);
                }
            }
        }
        for (X509CRL crl : possibleCRLs) {
            try {
                selector.setIssuerNames(null);
                if (selector.match(crl) && verifyCRL(certImpl, point, crl,
                        reasonsMask, signFlag, prevKey, provider, trustAnchors,
                        certStores, validity)) {
                    crls.add(crl);
                }
            } catch (Exception e) {
                if (debug != null) {
                    debug.println("Exception verifying CRL: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        return crls;
    }
    private X509CRL getCRL(URIName name) {
        URI uri = name.getURI();
        if (debug != null) {
            debug.println("Trying to fetch CRL from DP " + uri);
        }
        try {
            CertStore ucs = URICertStore.getInstance
                (new URICertStore.URICertStoreParameters(uri));
            Collection<? extends CRL> crls = ucs.getCRLs(null);
            if (crls.isEmpty()) {
                return null;
            } else {
                return (X509CRL) crls.iterator().next();
            }
        } catch (Exception e) {
            if (debug != null) {
                debug.println("Exception getting CRL from CertStore: " + e);
                e.printStackTrace();
            }
        }
        return null;
    }
    private Collection<X509CRL> getCRLs(X500Name name,
        X500Principal certIssuer, List<CertStore> certStores)
    {
        if (debug != null) {
            debug.println("Trying to fetch CRL from DP " + name);
        }
        X509CRLSelector xcs = new X509CRLSelector();
        xcs.addIssuer(name.asX500Principal());
        xcs.addIssuer(certIssuer);
        Collection<X509CRL> crls = new ArrayList<X509CRL>();
        for (CertStore store : certStores) {
            try {
                for (CRL crl : store.getCRLs(xcs)) {
                    crls.add((X509CRL)crl);
                }
            } catch (CertStoreException cse) {
                if (debug != null) {
                    debug.println("Non-fatal exception while retrieving " +
                        "CRLs: " + cse);
                    cse.printStackTrace();
                }
            }
        }
        return crls;
    }
    boolean verifyCRL(X509CertImpl certImpl, DistributionPoint point,
        X509CRL crl, boolean[] reasonsMask, boolean signFlag,
        PublicKey prevKey, String provider,
        Set<TrustAnchor> trustAnchors, List<CertStore> certStores,
        Date validity) throws CRLException, IOException {
        boolean indirectCRL = false;
        X509CRLImpl crlImpl = X509CRLImpl.toImpl(crl);
        IssuingDistributionPointExtension idpExt =
            crlImpl.getIssuingDistributionPointExtension();
        X500Name certIssuer = (X500Name) certImpl.getIssuerDN();
        X500Name crlIssuer = (X500Name) crlImpl.getIssuerDN();
        GeneralNames pointCrlIssuers = point.getCRLIssuer();
        X500Name pointCrlIssuer = null;
        if (pointCrlIssuers != null) {
            if (idpExt == null ||
                ((Boolean) idpExt.get
                    (IssuingDistributionPointExtension.INDIRECT_CRL)).equals
                        (Boolean.FALSE)) {
                return false;
            }
            boolean match = false;
            for (Iterator<GeneralName> t = pointCrlIssuers.iterator();
                 !match && t.hasNext(); ) {
                GeneralNameInterface name = t.next().getName();
                if (crlIssuer.equals(name) == true) {
                    pointCrlIssuer = (X500Name) name;
                    match = true;
                }
            }
            if (match == false) {
                return false;
            }
            if (issues(certImpl, crlImpl, provider)) {
                prevKey = certImpl.getPublicKey();
            } else {
                indirectCRL = true;
            }
        } else if (crlIssuer.equals(certIssuer) == false) {
            if (debug != null) {
                debug.println("crl issuer does not equal cert issuer");
            }
            return false;
        } else {
            byte[] certAKID = certImpl.getExtensionValue(
                                PKIXExtensions.AuthorityKey_Id.toString());
            byte[] crlAKID = crlImpl.getExtensionValue(
                                PKIXExtensions.AuthorityKey_Id.toString());
            if (certAKID == null || crlAKID == null) {
                if (issues(certImpl, crlImpl, provider)) {
                    prevKey = certImpl.getPublicKey();
                }
            } else if (!Arrays.equals(certAKID, crlAKID)) {
                if (issues(certImpl, crlImpl, provider)) {
                    prevKey = certImpl.getPublicKey();
                } else {
                    indirectCRL = true;
                }
            }
        }
        if (!indirectCRL && !signFlag) {
            return false;
        }
        if (idpExt != null) {
            DistributionPointName idpPoint = (DistributionPointName)
                idpExt.get(IssuingDistributionPointExtension.POINT);
            if (idpPoint != null) {
                GeneralNames idpNames = idpPoint.getFullName();
                if (idpNames == null) {
                    RDN relativeName = idpPoint.getRelativeName();
                    if (relativeName == null) {
                        if (debug != null) {
                           debug.println("IDP must be relative or full DN");
                        }
                        return false;
                    }
                    if (debug != null) {
                        debug.println("IDP relativeName:" + relativeName);
                    }
                    idpNames = getFullNames(crlIssuer, relativeName);
                }
                if (point.getFullName() != null ||
                    point.getRelativeName() != null) {
                    GeneralNames pointNames = point.getFullName();
                    if (pointNames == null) {
                        RDN relativeName = point.getRelativeName();
                        if (relativeName == null) {
                            if (debug != null) {
                                debug.println("DP must be relative or full DN");
                            }
                            return false;
                        }
                        if (debug != null) {
                            debug.println("DP relativeName:" + relativeName);
                        }
                        if (indirectCRL) {
                            if (pointCrlIssuers.size() != 1) {
                                if (debug != null) {
                                    debug.println("must only be one CRL " +
                                        "issuer when relative name present");
                                }
                                return false;
                            }
                            pointNames = getFullNames
                                (pointCrlIssuer, relativeName);
                        } else {
                            pointNames = getFullNames(certIssuer, relativeName);
                        }
                    }
                    boolean match = false;
                    for (Iterator<GeneralName> i = idpNames.iterator();
                         !match && i.hasNext(); ) {
                        GeneralNameInterface idpName = i.next().getName();
                        if (debug != null) {
                            debug.println("idpName: " + idpName);
                        }
                        for (Iterator<GeneralName> p = pointNames.iterator();
                             !match && p.hasNext(); ) {
                            GeneralNameInterface pointName = p.next().getName();
                            if (debug != null) {
                                debug.println("pointName: " + pointName);
                            }
                            match = idpName.equals(pointName);
                        }
                    }
                    if (!match) {
                        if (debug != null) {
                            debug.println("IDP name does not match DP name");
                        }
                        return false;
                    }
                } else {
                    boolean match = false;
                    for (Iterator<GeneralName> t = pointCrlIssuers.iterator();
                         !match && t.hasNext(); ) {
                        GeneralNameInterface crlIssuerName = t.next().getName();
                        for (Iterator<GeneralName> i = idpNames.iterator();
                             !match && i.hasNext(); ) {
                            GeneralNameInterface idpName = i.next().getName();
                            match = crlIssuerName.equals(idpName);
                        }
                    }
                    if (!match) {
                        return false;
                    }
                }
            }
            Boolean b = (Boolean)
                idpExt.get(IssuingDistributionPointExtension.ONLY_USER_CERTS);
            if (b.equals(Boolean.TRUE) && certImpl.getBasicConstraints() != -1) {
                if (debug != null) {
                    debug.println("cert must be a EE cert");
                }
                return false;
            }
            b = (Boolean)
                idpExt.get(IssuingDistributionPointExtension.ONLY_CA_CERTS);
            if (b.equals(Boolean.TRUE) && certImpl.getBasicConstraints() == -1) {
                if (debug != null) {
                    debug.println("cert must be a CA cert");
                }
                return false;
            }
            b = (Boolean) idpExt.get
                (IssuingDistributionPointExtension.ONLY_ATTRIBUTE_CERTS);
            if (b.equals(Boolean.TRUE)) {
                if (debug != null) {
                    debug.println("cert must not be an AA cert");
                }
                return false;
            }
        }
        boolean[] interimReasonsMask = new boolean[9];
        ReasonFlags reasons = null;
        if (idpExt != null) {
            reasons = (ReasonFlags)
                idpExt.get(IssuingDistributionPointExtension.REASONS);
        }
        boolean[] pointReasonFlags = point.getReasonFlags();
        if (reasons != null) {
            if (pointReasonFlags != null) {
                boolean[] idpReasonFlags = reasons.getFlags();
                for (int i = 0; i < idpReasonFlags.length; i++) {
                    if (idpReasonFlags[i] && pointReasonFlags[i]) {
                        interimReasonsMask[i] = true;
                    }
                }
            } else {
                interimReasonsMask = reasons.getFlags().clone();
            }
        } else if (idpExt == null || reasons == null) {
            if (pointReasonFlags != null) {
                interimReasonsMask = pointReasonFlags.clone();
            } else {
                interimReasonsMask = new boolean[9];
                Arrays.fill(interimReasonsMask, true);
            }
        }
        boolean oneOrMore = false;
        for (int i=0; i < interimReasonsMask.length && !oneOrMore; i++) {
            if (!reasonsMask[i] && interimReasonsMask[i]) {
                oneOrMore = true;
            }
        }
        if (!oneOrMore) {
            return false;
        }
        if (indirectCRL) {
            X509CertSelector certSel = new X509CertSelector();
            certSel.setSubject(crlIssuer.asX500Principal());
            boolean[] crlSign = {false,false,false,false,false,false,true};
            certSel.setKeyUsage(crlSign);
            AuthorityKeyIdentifierExtension akidext =
                                            crlImpl.getAuthKeyIdExtension();
            if (akidext != null) {
                KeyIdentifier akid = (KeyIdentifier)akidext.get(akidext.KEY_ID);
                if (akid != null) {
                    DerOutputStream derout = new DerOutputStream();
                    derout.putOctetString(akid.getIdentifier());
                    certSel.setSubjectKeyIdentifier(derout.toByteArray());
                }
                SerialNumber asn =
                    (SerialNumber)akidext.get(akidext.SERIAL_NUMBER);
                if (asn != null) {
                    certSel.setSerialNumber(asn.getNumber());
                }
            }
            Set<TrustAnchor> newTrustAnchors = new HashSet<>(trustAnchors);
            if (prevKey != null) {
                X500Principal principal = certImpl.getIssuerX500Principal();
                TrustAnchor temporary =
                        new TrustAnchor(principal, prevKey, null);
                newTrustAnchors.add(temporary);
            }
            PKIXBuilderParameters params = null;
            try {
                params = new PKIXBuilderParameters(newTrustAnchors, certSel);
            } catch (InvalidAlgorithmParameterException iape) {
                throw new CRLException(iape);
            }
            params.setCertStores(certStores);
            params.setSigProvider(provider);
            params.setDate(validity);
            try {
                CertPathBuilder builder = CertPathBuilder.getInstance("PKIX");
                PKIXCertPathBuilderResult result =
                    (PKIXCertPathBuilderResult) builder.build(params);
                prevKey = result.getPublicKey();
            } catch (Exception e) {
                throw new CRLException(e);
            }
        }
        try {
            AlgorithmChecker.check(prevKey, crl);
        } catch (CertPathValidatorException cpve) {
            if (debug != null) {
                debug.println("CRL signature algorithm check failed: " + cpve);
            }
            return false;
        }
        try {
            crl.verify(prevKey, provider);
        } catch (Exception e) {
            if (debug != null) {
                debug.println("CRL signature failed to verify");
            }
            return false;
        }
        Set<String> unresCritExts = crl.getCriticalExtensionOIDs();
        if (unresCritExts != null) {
            unresCritExts.remove
                (PKIXExtensions.IssuingDistributionPoint_Id.toString());
            if (!unresCritExts.isEmpty()) {
                if (debug != null) {
                    debug.println("Unrecognized critical extension(s) in CRL: "
                        + unresCritExts);
                    Iterator<String> i = unresCritExts.iterator();
                    while (i.hasNext())
                        debug.println(i.next());
                }
                return false;
            }
        }
        for (int i=0; i < interimReasonsMask.length; i++) {
            if (!reasonsMask[i] && interimReasonsMask[i]) {
                reasonsMask[i] = true;
            }
        }
        return true;
    }
    private GeneralNames getFullNames(X500Name issuer, RDN rdn)
        throws IOException {
        List<RDN> rdns = new ArrayList<RDN>(issuer.rdns());
        rdns.add(rdn);
        X500Name fullName = new X500Name(rdns.toArray(new RDN[0]));
        GeneralNames fullNames = new GeneralNames();
        fullNames.add(new GeneralName(fullName));
        return fullNames;
    }
    private static boolean issues(X509CertImpl cert, X509CRLImpl crl,
            String provider) throws IOException {
        boolean matched = false;
        AdaptableX509CertSelector issuerSelector =
                                    new AdaptableX509CertSelector();
        boolean[] usages = cert.getKeyUsage();
        if (usages != null) {
            usages[6] = true;       
            issuerSelector.setKeyUsage(usages);
        }
        X500Principal crlIssuer = crl.getIssuerX500Principal();
        issuerSelector.setSubject(crlIssuer);
        AuthorityKeyIdentifierExtension crlAKID = crl.getAuthKeyIdExtension();
        if (crlAKID != null) {
            issuerSelector.parseAuthorityKeyIdentifierExtension(crlAKID);
        }
        matched = issuerSelector.match(cert);
        if (matched && (crlAKID == null ||
                cert.getAuthorityKeyIdentifierExtension() == null)) {
            try {
                crl.verify(cert.getPublicKey(), provider);
                matched = true;
            } catch (Exception e) {
                matched = false;
            }
        }
        return matched;
    }
}
