public class CertificatePolicyMap {
    private CertificatePolicyId issuerDomain;
    private CertificatePolicyId subjectDomain;
    public CertificatePolicyMap(CertificatePolicyId issuer,
                                CertificatePolicyId subject) {
        this.issuerDomain = issuer;
        this.subjectDomain = subject;
    }
    public CertificatePolicyMap(DerValue val) throws IOException {
        if (val.tag != DerValue.tag_Sequence) {
            throw new IOException("Invalid encoding for CertificatePolicyMap");
        }
        issuerDomain = new CertificatePolicyId(val.data.getDerValue());
        subjectDomain = new CertificatePolicyId(val.data.getDerValue());
    }
    public CertificatePolicyId getIssuerIdentifier() {
        return (issuerDomain);
    }
    public CertificatePolicyId getSubjectIdentifier() {
        return (subjectDomain);
    }
    public String toString() {
        String s = "CertificatePolicyMap: [\n"
                 + "IssuerDomain:" + issuerDomain.toString()
                 + "SubjectDomain:" + subjectDomain.toString()
                 + "]\n";
        return (s);
    }
    public void encode(DerOutputStream out) throws IOException {
        DerOutputStream tmp = new DerOutputStream();
        issuerDomain.encode(tmp);
        subjectDomain.encode(tmp);
        out.write(DerValue.tag_Sequence,tmp);
    }
}
