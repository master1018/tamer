public class X509CertPath extends CertPath {
    private static final long serialVersionUID = 4989800333263052980L;
    private List<X509Certificate> certs;
    private static final String COUNT_ENCODING = "count";
    private static final String PKCS7_ENCODING = "PKCS7";
    private static final String PKIPATH_ENCODING = "PkiPath";
    private static final Collection<String> encodingList;
    static {
        List<String> list = new ArrayList<String>(2);
        list.add(PKIPATH_ENCODING);
        list.add(PKCS7_ENCODING);
        encodingList = Collections.unmodifiableCollection(list);
    }
    public X509CertPath(List<? extends Certificate> certs) throws CertificateException {
        super("X.509");
        for (Object obj : (List<?>)certs) {
            if (obj instanceof X509Certificate == false) {
                throw new CertificateException
                    ("List is not all X509Certificates: "
                    + obj.getClass().getName());
            }
        }
        this.certs = Collections.unmodifiableList(
                new ArrayList<X509Certificate>((List<X509Certificate>)certs));
    }
    public X509CertPath(InputStream is) throws CertificateException {
        this(is, PKIPATH_ENCODING);
    }
    public X509CertPath(InputStream is, String encoding)
            throws CertificateException {
        super("X.509");
        if (PKIPATH_ENCODING.equals(encoding)) {
            certs = parsePKIPATH(is);
        } else if (PKCS7_ENCODING.equals(encoding)) {
            certs = parsePKCS7(is);
        } else {
            throw new CertificateException("unsupported encoding");
        }
    }
    private static List<X509Certificate> parsePKIPATH(InputStream is)
            throws CertificateException {
        List<X509Certificate> certList = null;
        CertificateFactory certFac = null;
        if (is == null) {
            throw new CertificateException("input stream is null");
        }
        try {
            DerInputStream dis = new DerInputStream(readAllBytes(is));
            DerValue[] seq = dis.getSequence(3);
            if (seq.length == 0) {
                return Collections.<X509Certificate>emptyList();
            }
            certFac = CertificateFactory.getInstance("X.509");
            certList = new ArrayList<X509Certificate>(seq.length);
            for (int i = seq.length-1; i >= 0; i--) {
                certList.add((X509Certificate)certFac.generateCertificate
                    (new ByteArrayInputStream(seq[i].toByteArray())));
            }
            return Collections.unmodifiableList(certList);
        } catch (IOException ioe) {
            CertificateException ce = new CertificateException("IOException" +
                " parsing PkiPath data: " + ioe);
            ce.initCause(ioe);
            throw ce;
        }
    }
    private static List<X509Certificate> parsePKCS7(InputStream is)
            throws CertificateException {
        List<X509Certificate> certList;
        if (is == null) {
            throw new CertificateException("input stream is null");
        }
        try {
            if (is.markSupported() == false) {
                is = new ByteArrayInputStream(readAllBytes(is));
            };
            PKCS7 pkcs7 = new PKCS7(is);
            X509Certificate[] certArray = pkcs7.getCertificates();
            if (certArray != null) {
                certList = Arrays.asList(certArray);
            } else {
                certList = new ArrayList<X509Certificate>(0);
            }
        } catch (IOException ioe) {
            throw new CertificateException("IOException parsing PKCS7 data: " +
                                        ioe);
        }
        return Collections.unmodifiableList(certList);
    }
    private static byte[] readAllBytes(InputStream is) throws IOException {
        byte[] buffer = new byte[8192];
        ByteArrayOutputStream baos = new ByteArrayOutputStream(2048);
        int n;
        while ((n = is.read(buffer)) != -1) {
            baos.write(buffer, 0, n);
        }
        return baos.toByteArray();
    }
    public byte[] getEncoded() throws CertificateEncodingException {
        return encodePKIPATH();
    }
    private byte[] encodePKIPATH() throws CertificateEncodingException {
        ListIterator<X509Certificate> li = certs.listIterator(certs.size());
        try {
            DerOutputStream bytes = new DerOutputStream();
            while (li.hasPrevious()) {
                X509Certificate cert = li.previous();
                if (certs.lastIndexOf(cert) != certs.indexOf(cert)) {
                    throw new CertificateEncodingException
                        ("Duplicate Certificate");
                }
                byte[] encoded = cert.getEncoded();
                bytes.write(encoded);
            }
            DerOutputStream derout = new DerOutputStream();
            derout.write(DerValue.tag_SequenceOf, bytes);
            return derout.toByteArray();
        } catch (IOException ioe) {
           CertificateEncodingException ce = new CertificateEncodingException
                ("IOException encoding PkiPath data: " + ioe);
           ce.initCause(ioe);
           throw ce;
        }
    }
    private byte[] encodePKCS7() throws CertificateEncodingException {
        PKCS7 p7 = new PKCS7(new AlgorithmId[0],
                             new ContentInfo(ContentInfo.DATA_OID, null),
                             certs.toArray(new X509Certificate[certs.size()]),
                             new SignerInfo[0]);
        DerOutputStream derout = new DerOutputStream();
        try {
            p7.encodeSignedData(derout);
        } catch (IOException ioe) {
            throw new CertificateEncodingException(ioe.getMessage());
        }
        return derout.toByteArray();
    }
    public byte[] getEncoded(String encoding)
            throws CertificateEncodingException {
        if (PKIPATH_ENCODING.equals(encoding)) {
            return encodePKIPATH();
        } else if (PKCS7_ENCODING.equals(encoding)) {
            return encodePKCS7();
        } else {
            throw new CertificateEncodingException("unsupported encoding");
        }
    }
    public static Iterator<String> getEncodingsStatic() {
        return encodingList.iterator();
    }
    public Iterator<String> getEncodings() {
        return getEncodingsStatic();
    }
    public List<X509Certificate> getCertificates() {
        return certs;
    }
}
