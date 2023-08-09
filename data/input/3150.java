public final class VerifyNameConstraints {
    private static PKIXParameters params;
    private static CertPath path;
    public static void main(String[] args) throws Exception {
        String[] certs = { "sun.cer", "sun2labs2.cer", "labs2isrg2.cer" };
        try {
            createPath(certs);
            validate(path, params);
            throw new Exception
                ("CertPath should not have been validated succesfully");
        } catch (CertPathValidatorException cve) {
            System.out.println("Test failed as expected: " + cve);
        }
    }
    public static void createPath(String[] certs) throws Exception {
        TrustAnchor anchor = new TrustAnchor(getCertFromFile(certs[0]), null);
        List list = new ArrayList();
        for (int i = 1; i < certs.length; i++) {
            list.add(0, getCertFromFile(certs[i]));
        }
        CertificateFactory cf = CertificateFactory.getInstance("X509");
        path = cf.generateCertPath(list);
        Set anchors = Collections.singleton(anchor);
        params = new PKIXParameters(anchors);
        params.setRevocationEnabled(false);
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
    public static X509Certificate getCertFromFile(String certFilePath)
        throws IOException {
            X509Certificate cert = null;
            try {
                File certFile = new File(System.getProperty("test.src", "."),
                    certFilePath);
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
    public static void validate(CertPath path, PKIXParameters params)
        throws Exception {
        CertPathValidator validator =
            CertPathValidator.getInstance("PKIX");
        CertPathValidatorResult cpvr = validator.validate(path, params);
    }
}
