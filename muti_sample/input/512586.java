public final class CodeSigner implements Serializable {
    private static final long serialVersionUID = 6819288105193937581L;
    private CertPath signerCertPath;
    private Timestamp timestamp;
    private transient int hash;
    public CodeSigner(CertPath signerCertPath, Timestamp timestamp) {
        if (signerCertPath == null) {
            throw new NullPointerException(Messages.getString("security.10")); 
        }
        this.signerCertPath = signerCertPath;
        this.timestamp = timestamp;
    }
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof CodeSigner) {
            CodeSigner that = (CodeSigner) obj;
            if (!signerCertPath.equals(that.signerCertPath)) {
                return false;
            }
            return timestamp == null ? that.timestamp == null : timestamp
                    .equals(that.timestamp);
        }
        return false;
    }
    public CertPath getSignerCertPath() {
        return signerCertPath;
    }
    public Timestamp getTimestamp() {
        return timestamp;
    }
    @Override
    public int hashCode() {
        if (hash == 0) {
            hash = signerCertPath.hashCode()
                    ^ (timestamp == null ? 0 : timestamp.hashCode());
        }
        return hash;
    }
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder(256);
        buf.append("CodeSigner [").append(signerCertPath.getCertificates().get(0)); 
        if( timestamp != null ) {
            buf.append("; ").append(timestamp); 
        }
        buf.append("]"); 
        return buf.toString();
    }
}
