public class X509CertPathImpl extends CertPath {
    private static final long serialVersionUID = 7989755106209515436L;
    public static final int PKI_PATH = 0;
    public static final int PKCS7 = 1;
    private static final String[] encodingsArr =
                                        new String[] {"PkiPath", "PKCS7"}; 
    static final List encodings = Collections.unmodifiableList(
                                            Arrays.asList(encodingsArr));
    private final List certificates;
    private byte[] pkiPathEncoding;
    private byte[] pkcs7Encoding;
    public X509CertPathImpl(List certs) throws CertificateException {
        super("X.509"); 
        int size = certs.size();
        certificates = new ArrayList(size);
        for (int i=0; i<size; i++) {
            Object cert = certs.get(i);
            if (!(cert instanceof X509Certificate) ) {
                throw new CertificateException(
                        Messages.getString("security.15D")); 
            }
            certificates.add(cert);
        }
    }
    private X509CertPathImpl(List certs, int type, byte[] encoding) {
        super("X.509"); 
        if (type == PKI_PATH) {
            this.pkiPathEncoding = encoding;
        } else { 
            this.pkcs7Encoding = encoding;
        }
        certificates = certs;
    }
    public static X509CertPathImpl getInstance(InputStream in)
                                        throws CertificateException {
        try {
            return (X509CertPathImpl) ASN1.decode(in);
        } catch (IOException e) {
            throw new CertificateException(Messages.getString("security.15E", 
                    e.getMessage()));
        }
    }
    public static X509CertPathImpl getInstance(InputStream in, String encoding)
        throws CertificateException {
        if (!encodings.contains(encoding)) {
            throw new CertificateException(
                    Messages.getString("security.15F", encoding)); 
        }
        try {
            if (encodingsArr[0].equals(encoding)) {
                return (X509CertPathImpl) ASN1.decode(in);
            } else {
                ContentInfo ci = (ContentInfo) ContentInfo.ASN1.decode(in);
                SignedData sd = ci.getSignedData();
                if (sd == null) {
                    throw new CertificateException(
                        Messages.getString("security.160")); 
                }
                List certs = sd.getCertificates();
                if (certs == null) {
                    certs = new ArrayList();
                }
                List result = new ArrayList();
                for (int i=0; i<certs.size(); i++) {
                    result.add(new X509CertImpl((Certificate) certs.get(i)));
                }
                return new X509CertPathImpl(result, PKCS7, ci.getEncoded());
            }
        } catch (IOException e) {
            throw new CertificateException(Messages.getString("security.15E", 
                    e.getMessage()));
        }
    }
    public static X509CertPathImpl getInstance(byte[] in)
                                        throws CertificateException {
        try {
            return (X509CertPathImpl) ASN1.decode(in);
        } catch (IOException e) {
            throw new CertificateException(Messages.getString("security.15E", 
                    e.getMessage()));
        }
    }
    public static X509CertPathImpl getInstance(byte[] in, String encoding)
        throws CertificateException {
        if (!encodings.contains(encoding)) {
            throw new CertificateException(
                    Messages.getString("security.15F", encoding)); 
        }
        try {
            if (encodingsArr[0].equals(encoding)) {
                return (X509CertPathImpl) ASN1.decode(in);
            } else {
                ContentInfo ci = (ContentInfo) ContentInfo.ASN1.decode(in);
                SignedData sd = ci.getSignedData();
                if (sd == null) {
                    throw new CertificateException(
                        Messages.getString("security.160")); 
                }
                List certs = sd.getCertificates();
                if (certs == null) {
                    certs = new ArrayList();
                }
                List result = new ArrayList();
                for (int i=0; i<certs.size(); i++) {
                    result.add(new X509CertImpl((Certificate) certs.get(i)));
                }
                return new X509CertPathImpl(result, PKCS7, ci.getEncoded());
            }
        } catch (IOException e) {
            throw new CertificateException(Messages.getString("security.15E", 
                    e.getMessage()));
        }
    }
    public List getCertificates() {
        return Collections.unmodifiableList(certificates);
    }
    public byte[] getEncoded() throws CertificateEncodingException {
        if (pkiPathEncoding == null) {
            pkiPathEncoding = ASN1.encode(this);
        }
        byte[] result = new byte[pkiPathEncoding.length];
        System.arraycopy(pkiPathEncoding, 0, result, 0, pkiPathEncoding.length);
        return result;
    }
    public byte[] getEncoded(String encoding)
        throws CertificateEncodingException {
        if (!encodings.contains(encoding)) {
            throw new CertificateEncodingException(
                    Messages.getString("security.15F", encoding)); 
        }
        if (encodingsArr[0].equals(encoding)) {
            return getEncoded();
        } else {
            if (pkcs7Encoding == null) {
                pkcs7Encoding = PKCS7_SIGNED_DATA_OBJECT.encode(this);
            }
            byte[] result = new byte[pkcs7Encoding.length];
            System.arraycopy(pkcs7Encoding, 0, result, 0,
                                        pkcs7Encoding.length);
            return result;
        }
    }
    public Iterator getEncodings() {
        return encodings.iterator();
    }
    public static final ASN1SequenceOf ASN1 =
                                    new ASN1SequenceOf(ASN1Any.getInstance()) {
        public Object getDecodedObject(BerInputStream in) throws IOException {
            List encodings = (List) in.content;
            int size = encodings.size();
            List certificates = new ArrayList(size);
            for (int i=0; i<size; i++) {
                certificates.add(
                    new X509CertImpl((Certificate)
                        Certificate.ASN1.decode((byte[]) encodings.get(i))));
            }
            return new X509CertPathImpl(
                    certificates, PKI_PATH, in.getEncoded());
        }
        public Collection getValues(Object object) {
            X509CertPathImpl cp = (X509CertPathImpl) object;
            if (cp.certificates == null) {
                return new ArrayList();
            }
            int size = cp.certificates.size();
            List encodings = new ArrayList(size);
            try {
                for (int i=0; i<size; i++) {
                    encodings.add(((X509Certificate)
                                cp.certificates.get(i)).getEncoded());
                }
            } catch (CertificateEncodingException e) {
                throw new IllegalArgumentException(Messages.getString("security.161")); 
            }
            return encodings;
        }
    };
    private static final ASN1Sequence ASN1_SIGNED_DATA = new ASN1Sequence(
            new ASN1Type[] {
                    ASN1Any.getInstance(),
                    new ASN1Implicit(0, ASN1),
                    ASN1Any.getInstance(),
            }) {
        private final byte[] PRECALCULATED_HEAD = new byte[] { 0x02, 0x01,
                0x01,
                0x31, 0x00,
                0x30, 0x03, 0x06, 0x01, 0x00 
        };
        private final byte[] SIGNERS_INFO = new byte[] { 0x31, 0x00 };
        protected void getValues(Object object, Object[] values) {
            values[0] = PRECALCULATED_HEAD;
            values[1] = object; 
            values[2] = SIGNERS_INFO;
        }
        public Object decode(BerInputStream in) throws IOException {
            throw new RuntimeException(
                    "Invalid use of encoder for PKCS#7 SignedData object");
        }
    };
    private static final ASN1Sequence PKCS7_SIGNED_DATA_OBJECT = new ASN1Sequence(
            new ASN1Type[] { ASN1Any.getInstance(), 
                    new ASN1Explicit(0, ASN1_SIGNED_DATA) 
            }) {
        private final byte[] SIGNED_DATA_OID = ASN1Oid.getInstance().encode(
                ContentInfo.SIGNED_DATA);
        protected void getValues(Object object, Object[] values) {
            values[0] = SIGNED_DATA_OID;
            values[1] = object; 
        }
        public Object decode(BerInputStream in) throws IOException {
            throw new RuntimeException(
                    "Invalid use of encoder for PKCS#7 SignedData object");
        }
    };
}
