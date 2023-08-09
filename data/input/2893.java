public class CertificatePolicySet {
    private final Vector<CertificatePolicyId> ids;
    public CertificatePolicySet(Vector<CertificatePolicyId> ids) {
        this.ids = ids;
    }
    public CertificatePolicySet(DerInputStream in) throws IOException {
        ids = new Vector<CertificatePolicyId>();
        DerValue[] seq = in.getSequence(5);
        for (int i = 0; i < seq.length; i++) {
            CertificatePolicyId id = new CertificatePolicyId(seq[i]);
            ids.addElement(id);
        }
    }
    public String toString() {
        String s = "CertificatePolicySet:[\n"
                 + ids.toString()
                 + "]\n";
        return (s);
    }
    public void encode(DerOutputStream out) throws IOException {
        DerOutputStream tmp = new DerOutputStream();
        for (int i = 0; i < ids.size(); i++) {
            ids.elementAt(i).encode(tmp);
        }
        out.write(DerValue.tag_Sequence,tmp);
    }
    public List<CertificatePolicyId> getCertPolicyIds() {
        return Collections.unmodifiableList(ids);
    }
}
