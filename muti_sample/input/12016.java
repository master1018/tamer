public class PolicyInformation {
    public static final String NAME       = "PolicyInformation";
    public static final String ID         = "id";
    public static final String QUALIFIERS = "qualifiers";
    private CertificatePolicyId policyIdentifier;
    private Set<PolicyQualifierInfo> policyQualifiers;
    public PolicyInformation(CertificatePolicyId policyIdentifier,
            Set<PolicyQualifierInfo> policyQualifiers) throws IOException {
        if (policyQualifiers == null) {
            throw new NullPointerException("policyQualifiers is null");
        }
        this.policyQualifiers =
            new LinkedHashSet<PolicyQualifierInfo>(policyQualifiers);
        this.policyIdentifier = policyIdentifier;
    }
    public PolicyInformation(DerValue val) throws IOException {
        if (val.tag != DerValue.tag_Sequence) {
            throw new IOException("Invalid encoding of PolicyInformation");
        }
        policyIdentifier = new CertificatePolicyId(val.data.getDerValue());
        if (val.data.available() != 0) {
            policyQualifiers = new LinkedHashSet<PolicyQualifierInfo>();
            DerValue opt = val.data.getDerValue();
            if (opt.tag != DerValue.tag_Sequence)
                throw new IOException("Invalid encoding of PolicyInformation");
            if (opt.data.available() == 0)
                throw new IOException("No data available in policyQualifiers");
            while (opt.data.available() != 0)
                policyQualifiers.add(new PolicyQualifierInfo
                        (opt.data.getDerValue().toByteArray()));
        } else {
            policyQualifiers = Collections.emptySet();
        }
    }
    public boolean equals(Object other) {
        if (!(other instanceof PolicyInformation))
            return false;
        PolicyInformation piOther = (PolicyInformation)other;
        if (!policyIdentifier.equals(piOther.getPolicyIdentifier()))
            return false;
        return policyQualifiers.equals(piOther.getPolicyQualifiers());
    }
    public int hashCode() {
        int myhash = 37 + policyIdentifier.hashCode();
        myhash = 37 * myhash + policyQualifiers.hashCode();
        return myhash;
    }
    public CertificatePolicyId getPolicyIdentifier() {
        return policyIdentifier;
    }
    public Set<PolicyQualifierInfo> getPolicyQualifiers() {
        return policyQualifiers;
    }
    public Object get(String name) throws IOException {
        if (name.equalsIgnoreCase(ID)) {
            return policyIdentifier;
        } else if (name.equalsIgnoreCase(QUALIFIERS)) {
            return policyQualifiers;
        } else {
            throw new IOException("Attribute name [" + name +
                "] not recognized by PolicyInformation.");
        }
    }
    public void set(String name, Object obj) throws IOException {
        if (name.equalsIgnoreCase(ID)) {
            if (obj instanceof CertificatePolicyId)
                policyIdentifier = (CertificatePolicyId)obj;
            else
                throw new IOException("Attribute value must be instance " +
                    "of CertificatePolicyId.");
        } else if (name.equalsIgnoreCase(QUALIFIERS)) {
            if (policyIdentifier == null) {
                throw new IOException("Attribute must have a " +
                    "CertificatePolicyIdentifier value before " +
                    "PolicyQualifierInfo can be set.");
            }
            if (obj instanceof Set) {
                Iterator<?> i = ((Set<?>)obj).iterator();
                while (i.hasNext()) {
                    Object obj1 = i.next();
                    if (!(obj1 instanceof PolicyQualifierInfo)) {
                        throw new IOException("Attribute value must be a" +
                                    "Set of PolicyQualifierInfo objects.");
                    }
                }
                policyQualifiers = (Set<PolicyQualifierInfo>) obj;
            } else {
                throw new IOException("Attribute value must be of type Set.");
            }
        } else {
            throw new IOException("Attribute name [" + name +
                "] not recognized by PolicyInformation");
        }
    }
    public void delete(String name) throws IOException {
        if (name.equalsIgnoreCase(QUALIFIERS)) {
            policyQualifiers = Collections.emptySet();
        } else if (name.equalsIgnoreCase(ID)) {
            throw new IOException("Attribute ID may not be deleted from " +
                "PolicyInformation.");
        } else {
            throw new IOException("Attribute name [" + name +
                "] not recognized by PolicyInformation.");
        }
    }
    public Enumeration<String> getElements() {
        AttributeNameEnumeration elements = new AttributeNameEnumeration();
        elements.addElement(ID);
        elements.addElement(QUALIFIERS);
        return elements.elements();
    }
    public String getName() {
        return NAME;
    }
    public String toString() {
        StringBuilder s = new StringBuilder("  [" + policyIdentifier.toString());
        s.append(policyQualifiers + "  ]\n");
        return s.toString();
    }
    public void encode(DerOutputStream out) throws IOException {
        DerOutputStream tmp = new DerOutputStream();
        policyIdentifier.encode(tmp);
        if (!policyQualifiers.isEmpty()) {
            DerOutputStream tmp2 = new DerOutputStream();
            for (PolicyQualifierInfo pq : policyQualifiers) {
                tmp2.write(pq.getEncoded());
            }
            tmp.write(DerValue.tag_Sequence, tmp2);
        }
        out.write(DerValue.tag_Sequence, tmp);
    }
}
