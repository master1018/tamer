public abstract class CertPath implements Serializable {
    private static final long serialVersionUID = 6068470306649138683L;
    private final String type;
    protected CertPath(String type) {
        this.type = type;
    }
    public String getType() {
        return type;
    }
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other instanceof CertPath) {
            CertPath o = (CertPath)other;
            if (getType().equals(o.getType())) {
                if (getCertificates().equals(o.getCertificates())) {
                    return true;
                }
            }
        }
        return false;
    }
    public int hashCode() {
        int hash = getType().hashCode();
        hash = hash*31 + getCertificates().hashCode();
        return hash;
    }
    public String toString() {
        StringBuilder sb = new StringBuilder(getType());
        sb.append(" Cert Path, len="); 
        sb.append(getCertificates().size());
        sb.append(": [\n"); 
        int n=1;
        for (Iterator<? extends Certificate> i=getCertificates().iterator();
                      i.hasNext(); n++) {
            sb.append("---------------certificate "); 
            sb.append(n);
            sb.append("---------------\n"); 
            sb.append(((Certificate)i.next()).toString());
        }
        sb.append("\n]"); 
        return sb.toString();
    }
    public abstract List<? extends Certificate> getCertificates();
    public abstract byte[] getEncoded()
        throws CertificateEncodingException;
    public abstract byte[] getEncoded(String encoding)
        throws CertificateEncodingException;
    public abstract Iterator<String> getEncodings();
    protected Object writeReplace() throws ObjectStreamException {
        try {
            return new CertPathRep(getType(), getEncoded());
        } catch (CertificateEncodingException e) {
            throw new NotSerializableException (
                    Messages.getString("security.66", e)); 
        }
    }
    protected static class CertPathRep implements Serializable {
        private static final long serialVersionUID = 3015633072427920915L;
        private final String type;
        private final byte[] data;
        private static final ObjectStreamField[] serialPersistentFields = {
             new ObjectStreamField("type", String.class), 
             new ObjectStreamField("data", byte[].class, true) 
        };
        protected CertPathRep(String type, byte[] data) {
            this.type = type;
            this.data = data;
        }
        protected Object readResolve() throws ObjectStreamException {
            try {
                CertificateFactory cf = CertificateFactory.getInstance(type);
                return cf.generateCertPath(new ByteArrayInputStream(data));
            } catch (Throwable t) {
                throw new NotSerializableException(
                        Messages.getString("security.67", t)); 
            }
        }
    }
}
