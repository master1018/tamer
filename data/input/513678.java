public class MyCertPath extends CertPath {
    private static final long serialVersionUID = 7444835599161870893L;
    private final Vector<MyCertificate> certificates;
    private final Vector<String> encodingNames;
    private final byte[] encoding;
    public MyCertPath(byte[] encoding) {
        super("MyEncoding");
        this.encoding = encoding;
        certificates = new Vector<MyCertificate>();
        certificates.add(new MyCertificate("MyEncoding", encoding));
        encodingNames = new Vector<String>();
        encodingNames.add("MyEncoding");
    }
    public List<MyCertificate> getCertificates() {
        return Collections.unmodifiableList(certificates);
    }
    public byte[] getEncoded() throws CertificateEncodingException {
        return encoding.clone();
    }
    public byte[] getEncoded(String encoding)
            throws CertificateEncodingException {
        if (getType().equals(encoding)) {
            return this.encoding.clone();
        }
        throw new CertificateEncodingException("Encoding not supported: " +
                encoding);
    }
    public Iterator<String> getEncodings() {
        return Collections.unmodifiableCollection(encodingNames).iterator();
    }
    public Object writeReplace() throws ObjectStreamException {
        return super.writeReplace();
    }
    public class MyCertPathRep extends CertPath.CertPathRep {
        private static final long serialVersionUID = 1609000085450479173L;
        private String type;
        private byte[] data; 
        public MyCertPathRep(String type, byte[] data) {
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
}
