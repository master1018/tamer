public final class ValidateCertPath {
    private final static String BASE = System.getProperty("test.src", "./");
    private static CertPath path;
    private static PKIXParameters params;
    public static void main(String[] args) throws Exception {
        try {
            parseArgs(args);
            validate(path, params);
            throw new Exception("Successfully validated invalid path.");
        } catch (CertPathValidatorException e) {
            if (e.getReason() != PKIXReason.INVALID_NAME) {
                throw new Exception("unexpected reason: " + e.getReason());
            }
            System.out.println("Path rejected as expected: " + e);
        }
    }
    public static void parseArgs(String[] args) throws Exception {
        args = new String[] {"jane2jane.cer", "jane2steve.cer", "steve2tom.cer"};
        TrustAnchor anchor = new TrustAnchor(getCertFromFile(args[0]), null);
        List<X509Certificate> list = new ArrayList<X509Certificate>();
        for (int i = 1; i < args.length; i++) {
            list.add(0, getCertFromFile(args[i]));
        }
        CertificateFactory cf = CertificateFactory.getInstance("X509");
        path = cf.generateCertPath(list);
        Set<TrustAnchor> anchors = Collections.singleton(anchor);
        params = new PKIXParameters(anchors);
        params.setRevocationEnabled(false);
        params.setDate(new Date(1243828800000l));
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
                File certFile = new File(BASE, certFilePath);
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
    public static void validate(CertPath path, PKIXParameters params)
        throws Exception {
        CertPathValidator validator =
            CertPathValidator.getInstance("PKIX");
        CertPathValidatorResult cpvr = validator.validate(path, params);
        System.out.println("ValidateCertPath successful.");
    }
}
