public class MyCertificateFactorySpi extends CertificateFactorySpi {
    private static boolean mode;
    private Set<String> list;
    public MyCertificateFactorySpi() {
        super();
        mode = true;
        list = new HashSet<String>();
        list.add("aa");
        list.add("bb");
    }
    public static void putMode(boolean newMode) {
        mode = newMode;
    }
    public Certificate engineGenerateCertificate(InputStream inStream)
            throws CertificateException {
        if (!(inStream instanceof DataInputStream)) {
            throw new CertificateException("Incorrect inputstream");
        }
        return null;
    }
    @SuppressWarnings("unchecked")
    public Collection engineGenerateCertificates(InputStream inStream)
            throws CertificateException {
        if (!(inStream instanceof DataInputStream)) {
            throw new CertificateException("Incorrect inputstream");
        }
        return null;
    }
    public CRL engineGenerateCRL(InputStream inStream) throws CRLException {
        if (!(inStream instanceof DataInputStream)) {
            throw new CRLException("Incorrect inputstream");
        }
        return null;
    }
    @SuppressWarnings("unchecked")
    public Collection engineGenerateCRLs(InputStream inStream)
            throws CRLException {
        if (!(inStream instanceof DataInputStream)) {
            throw new CRLException("Incorrect inputstream");
        }
        return null;
    }
    public CertPath engineGenerateCertPath(InputStream inStream)
            throws CertificateException {
        if (!(inStream instanceof DataInputStream)) {
            throw new CertificateException("Incorrect inputstream");
        }
        Iterator<String> it = engineGetCertPathEncodings();
        if (!it.hasNext()) {
            throw new CertificateException("There are no CertPath encodings");
        }
        return engineGenerateCertPath(inStream, it.next());
    }
    public CertPath engineGenerateCertPath(InputStream inStream, String encoding)
            throws CertificateException {
        if (!(inStream instanceof DataInputStream)) {
            throw new CertificateException("Incorrect inputstream");
        }
        if (encoding.length() == 0) {
            if (mode) {
                throw new IllegalArgumentException("Encoding is empty");
            }
        }
        return null;
    }
    public CertPath engineGenerateCertPath(List<? extends Certificate> certificates) {
        if (certificates == null) {
            if (mode) {
                throw new NullPointerException("certificates is null");
            }
        }
        return null;
    }
    public Iterator<String> engineGetCertPathEncodings() {
        if (!mode) {
            list.clear();
        }
        return list.iterator();
    }
}