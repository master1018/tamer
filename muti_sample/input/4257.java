public class CertificatePolicyId {
    private ObjectIdentifier id;
    public CertificatePolicyId(ObjectIdentifier id) {
        this.id = id;
    }
    public CertificatePolicyId(DerValue val) throws IOException {
        this.id = val.getOID();
    }
    public ObjectIdentifier getIdentifier() {
        return (id);
    }
    public String toString() {
        String s = "CertificatePolicyId: ["
                 + id.toString()
                 + "]\n";
        return (s);
    }
    public void encode(DerOutputStream out) throws IOException {
        out.putOID(id);
    }
    public boolean equals(Object other) {
        if (other instanceof CertificatePolicyId)
            return id.equals(((CertificatePolicyId) other).getIdentifier());
        else
            return false;
    }
    public int hashCode() {
      return id.hashCode();
    }
}
