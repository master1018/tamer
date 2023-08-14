public class CertificatePolicies extends ExtensionValue {
    private List policyInformations;
    private byte[] encoding;
    public CertificatePolicies() {}
    public CertificatePolicies(List policyInformations) {
        this.policyInformations = policyInformations;
    }
    public static CertificatePolicies decode(byte[] encoding) 
            throws IOException {
        CertificatePolicies cps = ((CertificatePolicies) ASN1.decode(encoding));
        cps.encoding = encoding;
        return cps;
    }
    private CertificatePolicies(List policyInformations, byte[] encoding) {
        this.policyInformations = policyInformations;
        this.encoding = encoding;
    }
    public List getPolicyInformations() {
        return new ArrayList(policyInformations);
    }
    public CertificatePolicies addPolicyInformation(
            PolicyInformation policyInformation) {
        encoding = null;
        if (policyInformations == null) {
            policyInformations = new ArrayList();
        }
        policyInformations.add(policyInformation);
        return this;
    }
    public byte[] getEncoded() {
        if (encoding == null) {
            encoding = ASN1.encode(this);
        }
        return encoding;
    }
    public void dumpValue(StringBuffer buffer, String prefix) {
        buffer.append(prefix).append("CertificatePolicies [\n"); 
        for (Iterator it=policyInformations.iterator(); it.hasNext();) {
            buffer.append(prefix);
            buffer.append("  "); 
            ((PolicyInformation) it.next()).dumpValue(buffer);
            buffer.append('\n');
        }
        buffer.append(prefix).append("]\n"); 
    }
    public static final ASN1Type ASN1 = 
        new ASN1SequenceOf(PolicyInformation.ASN1) {
        public Object getDecodedObject(BerInputStream in) {
            return new CertificatePolicies((List) in.content, in.getEncoded());
        }
        public Collection getValues(Object object) {
            CertificatePolicies cps = (CertificatePolicies) object;
            return cps.policyInformations;
        }
    };
}
