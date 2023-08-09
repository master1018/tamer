public class CertUtils {
    private CertUtils() {}
    public static X509Certificate getCertFromFile(String certFilePath)
        throws IOException {
            X509Certificate cert = null;
            try {
                File certFile = new File(System.getProperty("test.src", "."),
                    certFilePath);
                if (!certFile.canRead())
                    throw new IOException("File " +
                                          certFile.toString() +
                                          " is not a readable file.");
                FileInputStream certFileInputStream =
                    new FileInputStream(certFile);
                CertificateFactory cf = CertificateFactory.getInstance("X509");
                cert = (X509Certificate)
                    cf.generateCertificate(certFileInputStream);
            } catch (Exception e) {
                e.printStackTrace();
                throw new IOException("Can't construct X509Certificate: " +
                                      e.getMessage());
            }
            return cert;
    }
    public static X509CRL getCRLFromFile(String crlFilePath)
        throws IOException {
            X509CRL crl = null;
            try {
                File crlFile = new File(System.getProperty("test.src", "."),
                    crlFilePath);
                if (!crlFile.canRead())
                    throw new IOException("File " +
                                          crlFile.toString() +
                                          " is not a readable file.");
                FileInputStream crlFileInputStream =
                    new FileInputStream(crlFile);
                CertificateFactory cf = CertificateFactory.getInstance("X509");
                crl = (X509CRL) cf.generateCRL(crlFileInputStream);
            } catch (Exception e) {
                e.printStackTrace();
                throw new IOException("Can't construct X509CRL: " +
                                      e.getMessage());
            }
            return crl;
    }
    public static CertPath buildPath(String [] fileNames) throws Exception {
        return buildPath("", fileNames);
    }
    public static CertPath buildPath(String relPath, String [] fileNames)
        throws Exception {
        List<X509Certificate> list = new ArrayList<X509Certificate>();
        for (int i = 0; i < fileNames.length; i++) {
            list.add(0, getCertFromFile(relPath + fileNames[i]));
        }
        CertificateFactory cf = CertificateFactory.getInstance("X509");
        return(cf.generateCertPath(list));
    }
    public static CertStore createStore(String [] fileNames) throws Exception {
        return createStore("", fileNames);
    }
    public static CertStore createStore(String relPath, String [] fileNames)
        throws Exception {
        Set<X509Certificate> certs = new HashSet<X509Certificate>();
        for (int i = 0; i < fileNames.length; i++) {
            certs.add(getCertFromFile(relPath + fileNames[i]));
        }
        return CertStore.getInstance("Collection",
            new CollectionCertStoreParameters(certs));
    }
    public static CertStore createCRLStore(String [] fileNames)
        throws Exception {
        return createCRLStore("", fileNames);
    }
    public static CertStore createCRLStore(String relPath, String [] fileNames)
        throws Exception {
        Set<X509CRL> crls = new HashSet<X509CRL>();
        for (int i = 0; i < fileNames.length; i++) {
            crls.add(getCRLFromFile(relPath + fileNames[i]));
        }
        return CertStore.getInstance("Collection",
            new CollectionCertStoreParameters(crls));
    }
    public static PKIXCertPathBuilderResult build(PKIXBuilderParameters params)
        throws Exception {
        CertPathBuilder builder =
            CertPathBuilder.getInstance("PKIX");
        return (PKIXCertPathBuilderResult) builder.build(params);
    }
    public static PKIXCertPathValidatorResult validate
        (CertPath path, PKIXParameters params) throws Exception {
        CertPathValidator validator =
            CertPathValidator.getInstance("PKIX");
        return (PKIXCertPathValidatorResult) validator.validate(path, params);
    }
    private static byte[] getTotalBytes(InputStream is) throws IOException {
           byte[] buffer = new byte[8192];
        ByteArrayOutputStream baos = new ByteArrayOutputStream(2048);
        int n;
        baos.reset();
        while ((n = is.read(buffer, 0, buffer.length)) != -1) {
            baos.write(buffer, 0, n);
        }
        return baos.toByteArray();
    }
}
