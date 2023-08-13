public class PKIXParameters implements CertPathParameters {
    private Set<TrustAnchor> trustAnchors;
    private Set<String> initialPolicies;
    private List<CertStore> certStores;
    private Date date;
    private List<PKIXCertPathChecker> certPathCheckers;
    private String sigProvider;
    private CertSelector targetCertConstraints;
    private boolean revocationEnabled = true;
    private boolean explicitPolicyRequired = false;
    private boolean policyMappingInhibited = false;
    private boolean anyPolicyInhibited = false;
    private boolean policyQualifiersRejected = true;
    public PKIXParameters(Set<TrustAnchor> trustAnchors)
        throws InvalidAlgorithmParameterException {
        if (trustAnchors == null) {
            throw new NullPointerException(Messages.getString("security.6F")); 
        }
        checkTrustAnchors(trustAnchors);
        this.trustAnchors = new HashSet<TrustAnchor>(trustAnchors);
    }
    public PKIXParameters(KeyStore keyStore)
        throws KeyStoreException,
               InvalidAlgorithmParameterException {
        if (keyStore == null) {
            throw new NullPointerException(Messages.getString("security.41")); 
        }
        if (keyStore.size() == 0) {
            throw new InvalidAlgorithmParameterException(
                    Messages.getString("security.6A")); 
        }
        trustAnchors = new HashSet<TrustAnchor>();
        for (Enumeration i = keyStore.aliases(); i.hasMoreElements();) {
            String alias = (String) i.nextElement();
            if (keyStore.isCertificateEntry(alias)) {
                Certificate c = keyStore.getCertificate(alias);
                if (c instanceof X509Certificate) {
                    trustAnchors.add(new TrustAnchor((X509Certificate)c, null));
                }
            }
        }
        checkTrustAnchors(trustAnchors);
    }
    public Set<TrustAnchor> getTrustAnchors() {
        return Collections.unmodifiableSet(trustAnchors);
    }
    public void setTrustAnchors(Set<TrustAnchor> trustAnchors)
        throws InvalidAlgorithmParameterException {
        if (trustAnchors == null) {
            throw new NullPointerException(
                    Messages.getString("security.6F")); 
        }
        checkTrustAnchors(trustAnchors);
        this.trustAnchors = new HashSet<TrustAnchor>(trustAnchors);
    }
    public boolean isAnyPolicyInhibited() {
        return anyPolicyInhibited;
    }
    public void setAnyPolicyInhibited(boolean anyPolicyInhibited) {
        this.anyPolicyInhibited = anyPolicyInhibited;
    }
    public List<PKIXCertPathChecker> getCertPathCheckers() {
        if (certPathCheckers == null) {
            certPathCheckers = new ArrayList<PKIXCertPathChecker>();
        }
        if (certPathCheckers.isEmpty()) {
            return Collections.unmodifiableList(certPathCheckers);
        }
        ArrayList<PKIXCertPathChecker> modifiableList = 
            new ArrayList<PKIXCertPathChecker>();
        for (Iterator<PKIXCertPathChecker> i 
                = certPathCheckers.iterator(); i.hasNext();) {
            modifiableList.add((PKIXCertPathChecker)i.next().clone());
        }
        return Collections.unmodifiableList(modifiableList);
    }
    public void setCertPathCheckers(List<PKIXCertPathChecker> certPathCheckers) {
        if (certPathCheckers == null || certPathCheckers.isEmpty()) {
            if (this.certPathCheckers != null &&
               !this.certPathCheckers.isEmpty()) {
                this.certPathCheckers = null;
            }
            return;
        }
        this.certPathCheckers = new ArrayList<PKIXCertPathChecker>();
        for (Iterator<PKIXCertPathChecker> i 
                = certPathCheckers.iterator(); i.hasNext();) {
            this.certPathCheckers.add((PKIXCertPathChecker)i.next().clone());
        }
    }
    public void addCertPathChecker(PKIXCertPathChecker checker) {
        if (checker == null) {
            return;
        }
        if (certPathCheckers == null) {
            certPathCheckers = new ArrayList<PKIXCertPathChecker>();
        }
        certPathCheckers.add((PKIXCertPathChecker) checker.clone());
    }
    public List<CertStore> getCertStores() {
        if (certStores == null) {
            certStores = new ArrayList<CertStore>();
        }
        if (certStores.isEmpty()) {
            return Collections.unmodifiableList(certStores);
        }
        ArrayList<CertStore> modifiableList 
            = new ArrayList<CertStore>(certStores);
        return Collections.unmodifiableList(modifiableList);
    }
    public void setCertStores(List<CertStore> certStores) {
        if (certStores == null || certStores.isEmpty()) {
            if (this.certStores != null && !this.certStores.isEmpty()) {
                this.certStores = null;
            }
            return;
        }
        this.certStores = new ArrayList(certStores);
        for (Iterator i = this.certStores.iterator(); i.hasNext();) {
            if (!(i.next() instanceof CertStore)) {
                throw new ClassCastException(Messages.getString("security.6B")); 
            }
        }
    }
    public void addCertStore(CertStore store) {
        if (store == null) {
            return;
        }
        if (certStores == null) {
            certStores = new ArrayList();
        }
        certStores.add(store);
    }
    public Date getDate() {
        return date == null ? null : (Date)date.clone();
    }
    public void setDate(Date date) {
        this.date = (date == null ? null : new Date(date.getTime()));
    }
    public boolean isExplicitPolicyRequired() {
        return explicitPolicyRequired;
    }
    public void setExplicitPolicyRequired(boolean explicitPolicyRequired) {
        this.explicitPolicyRequired = explicitPolicyRequired;
    }
    public Set<String> getInitialPolicies() {
        if (initialPolicies == null) {
            initialPolicies = new HashSet();
        }
        if (initialPolicies.isEmpty()) {
            return Collections.unmodifiableSet(initialPolicies);
        }
        HashSet modifiableSet = new HashSet(initialPolicies);
        return Collections.unmodifiableSet(modifiableSet);
    }
    public void setInitialPolicies(Set<String> initialPolicies) {
        if (initialPolicies == null || initialPolicies.isEmpty()) {
            if (this.initialPolicies != null &&
               !this.initialPolicies.isEmpty()) {
                this.initialPolicies = null;
            }
            return;
        }
        this.initialPolicies = new HashSet(initialPolicies);
        for (Iterator i = this.initialPolicies.iterator(); i.hasNext();) {
            if (!(i.next() instanceof String)) {
                throw new ClassCastException(Messages.getString("security.6C")); 
            }
        }
    }
    public boolean isPolicyMappingInhibited() {
        return policyMappingInhibited;
    }
    public void setPolicyMappingInhibited(boolean policyMappingInhibited) {
        this.policyMappingInhibited = policyMappingInhibited;
    }
    public boolean getPolicyQualifiersRejected() {
        return policyQualifiersRejected;
    }
    public void setPolicyQualifiersRejected(boolean policyQualifiersRejected) {
        this.policyQualifiersRejected = policyQualifiersRejected;
    }
    public boolean isRevocationEnabled() {
        return revocationEnabled;
    }
    public void setRevocationEnabled(boolean revocationEnabled) {
        this.revocationEnabled = revocationEnabled;
    }
    public String getSigProvider() {
        return sigProvider;
    }
    public void setSigProvider(String sigProvider) {
        this.sigProvider = sigProvider;
    }
    public CertSelector getTargetCertConstraints() {
        return (targetCertConstraints == null ? null
                :(CertSelector)targetCertConstraints.clone());
    }
    public void setTargetCertConstraints(CertSelector targetCertConstraints) {
        this.targetCertConstraints = (targetCertConstraints == null ? null
                : (CertSelector)targetCertConstraints.clone());
    }
    public Object clone() {
        try {
            PKIXParameters ret = (PKIXParameters)super.clone();
            if (this.certStores != null) {
                ret.certStores = new ArrayList(this.certStores);
            }
            if (this.certPathCheckers != null) {
                ret.certPathCheckers = new ArrayList(this.certPathCheckers);
            }
            return ret;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e); 
        }
    }
    public String toString() {
        StringBuilder sb =
            new StringBuilder("[\n Trust Anchors: "); 
        sb.append(trustAnchors);
        sb.append("\n Revocation Enabled: "); 
        sb.append(revocationEnabled);
        sb.append("\n Explicit Policy Required: "); 
        sb.append(explicitPolicyRequired);
        sb.append("\n Policy Mapping Inhibited: "); 
        sb.append(policyMappingInhibited);
        sb.append("\n Any Policy Inhibited: "); 
        sb.append(anyPolicyInhibited);
        sb.append("\n Policy Qualifiers Rejected: "); 
        sb.append(policyQualifiersRejected);
        sb.append("\n Initial Policy OIDs: "); 
        sb.append((initialPolicies == null || initialPolicies.isEmpty())
                ? "any" : initialPolicies.toString()); 
        sb.append("\n Cert Stores: "); 
        sb.append((certStores==null||certStores.isEmpty())?
                "no":certStores.toString()); 
        sb.append("\n Validity Date: "); 
        sb.append(date);
        sb.append("\n Cert Path Checkers: "); 
        sb.append((certPathCheckers==null||certPathCheckers.isEmpty())?
                "no":certPathCheckers.toString()); 
        sb.append("\n Signature Provider: "); 
        sb.append(sigProvider);
        sb.append("\n Target Certificate Constraints: "); 
        sb.append(targetCertConstraints);
        sb.append("\n]"); 
        return sb.toString();
    }
    private void checkTrustAnchors(Set trustAnchors)
        throws InvalidAlgorithmParameterException {
        if (trustAnchors.isEmpty()) {
            throw new InvalidAlgorithmParameterException(
                    Messages.getString("security.6D")); 
        }
        for (Iterator i = trustAnchors.iterator(); i.hasNext();) {
            if (!(i.next() instanceof TrustAnchor)) {
                throw new ClassCastException(
             Messages.getString("security.6E")); 
            }
        }
    }
}
