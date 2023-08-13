public class PKIXParameters implements CertPathParameters {
    private Set<TrustAnchor> unmodTrustAnchors;
    private Date date;
    private List<PKIXCertPathChecker> certPathCheckers;
    private String sigProvider;
    private boolean revocationEnabled = true;
    private Set<String> unmodInitialPolicies;
    private boolean explicitPolicyRequired = false;
    private boolean policyMappingInhibited = false;
    private boolean anyPolicyInhibited = false;
    private boolean policyQualifiersRejected = true;
    private List<CertStore> certStores;
    private CertSelector certSelector;
    public PKIXParameters(Set<TrustAnchor> trustAnchors)
        throws InvalidAlgorithmParameterException
    {
        setTrustAnchors(trustAnchors);
        this.unmodInitialPolicies = Collections.<String>emptySet();
        this.certPathCheckers = new ArrayList<PKIXCertPathChecker>();
        this.certStores = new ArrayList<CertStore>();
    }
    public PKIXParameters(KeyStore keystore)
        throws KeyStoreException, InvalidAlgorithmParameterException
    {
        if (keystore == null)
            throw new NullPointerException("the keystore parameter must be " +
                "non-null");
        Set<TrustAnchor> hashSet = new HashSet<TrustAnchor>();
        Enumeration<String> aliases = keystore.aliases();
        while (aliases.hasMoreElements()) {
            String alias = aliases.nextElement();
            if (keystore.isCertificateEntry(alias)) {
                Certificate cert = keystore.getCertificate(alias);
                if (cert instanceof X509Certificate)
                    hashSet.add(new TrustAnchor((X509Certificate)cert, null));
            }
        }
        setTrustAnchors(hashSet);
        this.unmodInitialPolicies = Collections.<String>emptySet();
        this.certPathCheckers = new ArrayList<PKIXCertPathChecker>();
        this.certStores = new ArrayList<CertStore>();
    }
    public Set<TrustAnchor> getTrustAnchors() {
        return this.unmodTrustAnchors;
    }
    public void setTrustAnchors(Set<TrustAnchor> trustAnchors)
        throws InvalidAlgorithmParameterException
    {
        if (trustAnchors == null) {
            throw new NullPointerException("the trustAnchors parameters must" +
                " be non-null");
        }
        if (trustAnchors.isEmpty()) {
            throw new InvalidAlgorithmParameterException("the trustAnchors " +
                "parameter must be non-empty");
        }
        for (Iterator<TrustAnchor> i = trustAnchors.iterator(); i.hasNext(); ) {
            if (!(i.next() instanceof TrustAnchor)) {
                throw new ClassCastException("all elements of set must be "
                    + "of type java.security.cert.TrustAnchor");
            }
        }
        this.unmodTrustAnchors = Collections.unmodifiableSet
                (new HashSet<TrustAnchor>(trustAnchors));
    }
    public Set<String> getInitialPolicies() {
        return this.unmodInitialPolicies;
    }
    public void setInitialPolicies(Set<String> initialPolicies) {
        if (initialPolicies != null) {
            for (Iterator<String> i = initialPolicies.iterator();
                        i.hasNext();) {
                if (!(i.next() instanceof String))
                    throw new ClassCastException("all elements of set must be "
                        + "of type java.lang.String");
            }
            this.unmodInitialPolicies =
                Collections.unmodifiableSet(new HashSet<String>(initialPolicies));
        } else
            this.unmodInitialPolicies = Collections.<String>emptySet();
    }
    public void setCertStores(List<CertStore> stores) {
        if (stores == null) {
            this.certStores = new ArrayList<CertStore>();
        } else {
            for (Iterator<CertStore> i = stores.iterator(); i.hasNext();) {
                if (!(i.next() instanceof CertStore)) {
                    throw new ClassCastException("all elements of list must be "
                        + "of type java.security.cert.CertStore");
                }
            }
            this.certStores = new ArrayList<CertStore>(stores);
        }
    }
    public void addCertStore(CertStore store) {
        if (store != null) {
            this.certStores.add(store);
        }
    }
    public List<CertStore> getCertStores() {
        return Collections.unmodifiableList
                (new ArrayList<CertStore>(this.certStores));
    }
    public void setRevocationEnabled(boolean val) {
        revocationEnabled = val;
    }
    public boolean isRevocationEnabled() {
        return revocationEnabled;
    }
    public void setExplicitPolicyRequired(boolean val) {
        explicitPolicyRequired = val;
    }
    public boolean isExplicitPolicyRequired() {
        return explicitPolicyRequired;
    }
    public void setPolicyMappingInhibited(boolean val) {
        policyMappingInhibited = val;
    }
    public boolean isPolicyMappingInhibited() {
        return policyMappingInhibited;
    }
    public void setAnyPolicyInhibited(boolean val) {
        anyPolicyInhibited = val;
    }
    public boolean isAnyPolicyInhibited() {
        return anyPolicyInhibited;
    }
    public void setPolicyQualifiersRejected(boolean qualifiersRejected) {
        policyQualifiersRejected = qualifiersRejected;
    }
    public boolean getPolicyQualifiersRejected() {
        return policyQualifiersRejected;
    }
    public Date getDate() {
        if (date == null)
            return null;
        else
            return (Date) this.date.clone();
    }
    public void setDate(Date date) {
        if (date != null)
            this.date = (Date) date.clone();
        else
            date = null;
    }
    public void setCertPathCheckers(List<PKIXCertPathChecker> checkers) {
        if (checkers != null) {
            List<PKIXCertPathChecker> tmpList =
                        new ArrayList<PKIXCertPathChecker>();
            for (PKIXCertPathChecker checker : checkers) {
                tmpList.add((PKIXCertPathChecker)checker.clone());
            }
            this.certPathCheckers = tmpList;
        } else {
            this.certPathCheckers = new ArrayList<PKIXCertPathChecker>();
        }
    }
    public List<PKIXCertPathChecker> getCertPathCheckers() {
        List<PKIXCertPathChecker> tmpList = new ArrayList<PKIXCertPathChecker>();
        for (PKIXCertPathChecker ck : certPathCheckers) {
            tmpList.add((PKIXCertPathChecker)ck.clone());
        }
        return Collections.unmodifiableList(tmpList);
    }
    public void addCertPathChecker(PKIXCertPathChecker checker) {
        if (checker != null) {
            certPathCheckers.add((PKIXCertPathChecker)checker.clone());
        }
    }
    public String getSigProvider() {
        return this.sigProvider;
    }
    public void setSigProvider(String sigProvider) {
        this.sigProvider = sigProvider;
    }
    public CertSelector getTargetCertConstraints() {
        if (certSelector != null) {
            return (CertSelector) certSelector.clone();
        } else {
            return null;
        }
    }
    public void setTargetCertConstraints(CertSelector selector) {
        if (selector != null)
            certSelector = (CertSelector) selector.clone();
        else
            certSelector = null;
    }
    public Object clone() {
        try {
            PKIXParameters copy = (PKIXParameters)super.clone();
            if (certStores != null) {
                copy.certStores = new ArrayList<CertStore>(certStores);
            }
            if (certPathCheckers != null) {
                copy.certPathCheckers =
                    new ArrayList<PKIXCertPathChecker>(certPathCheckers.size());
                for (PKIXCertPathChecker checker : certPathCheckers) {
                    copy.certPathCheckers.add(
                                    (PKIXCertPathChecker)checker.clone());
                }
            }
            return copy;
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e.toString());
        }
    }
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("[\n");
        if (unmodTrustAnchors != null) {
            sb.append("  Trust Anchors: " + unmodTrustAnchors.toString()
                + "\n");
        }
        if (unmodInitialPolicies != null) {
            if (unmodInitialPolicies.isEmpty()) {
                sb.append("  Initial Policy OIDs: any\n");
            } else {
                sb.append("  Initial Policy OIDs: ["
                    + unmodInitialPolicies.toString() + "]\n");
            }
        }
        sb.append("  Validity Date: " + String.valueOf(date) + "\n");
        sb.append("  Signature Provider: " + String.valueOf(sigProvider) + "\n");
        sb.append("  Default Revocation Enabled: " + revocationEnabled + "\n");
        sb.append("  Explicit Policy Required: " + explicitPolicyRequired + "\n");
        sb.append("  Policy Mapping Inhibited: " + policyMappingInhibited + "\n");
        sb.append("  Any Policy Inhibited: " + anyPolicyInhibited + "\n");
        sb.append("  Policy Qualifiers Rejected: " + policyQualifiersRejected + "\n");
        sb.append("  Target Cert Constraints: " + String.valueOf(certSelector) + "\n");
        if (certPathCheckers != null)
            sb.append("  Certification Path Checkers: ["
                + certPathCheckers.toString() + "]\n");
        if (certStores != null)
            sb.append("  CertStores: [" + certStores.toString() + "]\n");
        sb.append("]");
        return sb.toString();
    }
}
