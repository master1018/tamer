public class AIACheck {
    private final static File baseDir =
        new File(System.getProperty("test.src", "."));
    private static X509Certificate loadCertificate(String name)
        throws Exception
    {
        File certFile = new File(baseDir, name);
        InputStream in = new FileInputStream(certFile);
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate cert = (X509Certificate)cf.generateCertificate(in);
        return cert;
    }
    public static void main(String args[]) throws Exception {
        X509Certificate aiaCert = loadCertificate("AIACert.pem");
        X509Certificate rootCert = loadCertificate("RootCert.pem");
        List<X509Certificate> list =
            Arrays.asList(new X509Certificate[] {aiaCert});
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        CertPath path = cf.generateCertPath(list);
        TrustAnchor anchor = new TrustAnchor(rootCert, null);
        Set<TrustAnchor> anchors = Collections.singleton(anchor);
        PKIXParameters params = new PKIXParameters(anchors);
        params.setRevocationEnabled(true);
        Security.setProperty("ocsp.enable", "true");
        if (Security.getProperty("ocsp.responderURL") != null) {
            throw new
                Exception("The ocsp.responderURL property must not be set");
        }
        CertPathValidator validator = CertPathValidator.getInstance("PKIX");
        try {
            validator.validate(path, params);
            throw new Exception("Successfully validated an invalid path");
        } catch (CertPathValidatorException e ) {
            if (! (e.getCause() instanceof SocketException)) {
                throw e;
            }
            System.out.println("Extracted the URL of the OCSP responder from " +
                "the certificate's AuthorityInfoAccess extension.");
        }
    }
}
