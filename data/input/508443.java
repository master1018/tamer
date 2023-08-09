public class MyCertificate extends Certificate implements X509Extension {
    private static final long serialVersionUID = -1835303280727190066L;
    private final byte[] encoding;
    public CertificateRep rep;
    public MyCertificate(String type, byte[] encoding) {
        super(type);
        this.encoding = encoding;
    }
    public byte[] getEncoded() throws CertificateEncodingException {
        return encoding.clone();
    }
    public void verify(PublicKey key) throws CertificateException,
            NoSuchAlgorithmException, InvalidKeyException,
            NoSuchProviderException, SignatureException {
    }
    public void verify(PublicKey key, String sigProvider)
            throws CertificateException, NoSuchAlgorithmException,
            InvalidKeyException, NoSuchProviderException, SignatureException {
    }
    public String toString() {
        return "[My test Certificate, type: " + getType() + "]";
    }
    public Object writeReplace() throws ObjectStreamException {
        return super.writeReplace();
    }
    public PublicKey getPublicKey() {
        return new PublicKey() {
           private static final long serialVersionUID = 788077928335589816L;
            public String getAlgorithm() {
                return "TEST";
            }
            public byte[] getEncoded() {
                return new byte[] {(byte)1, (byte)2, (byte)3};
            }
            public String getFormat() {
                return "TEST_FORMAT";
            }
        };
    }
    public Certificate.CertificateRep getCertificateRep()
            throws ObjectStreamException {
        Object obj = super.writeReplace();
        return (MyCertificateRep) obj;
    }
    public class MyCertificateRep extends Certificate.CertificateRep {
        private static final long serialVersionUID = -3474284043994635553L;
        private String type;
        private byte[] data; 
        public MyCertificateRep(String type, byte[] data) {
            super(type, data);
            this.data = data;
            this.type = type;
        }
        public Object readResolve() throws ObjectStreamException {
            return super.readResolve();
        }
        public String getType() {
            return type;
        }
        public byte[] getData() {
            return data;
        }
    }
    public Set<String> getNonCriticalExtensionOIDs() {
        return null;
    }
    public Set<String> getCriticalExtensionOIDs() {
        return null;
    }
    public byte[] getExtensionValue(String oid) {
        return null;
    }
    public boolean hasUnsupportedCriticalExtension() {
        return false;
    }
}
