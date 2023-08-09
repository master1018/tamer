public final class ValidateNC {
    private static CertPath path;
    private static PKIXParameters params;
    private static Set anchors;
    public static void main(String[] args) throws Exception {
        String[] certs = { "sun2labs2.cer", "labs2isrg2.cer" };
        createPath(certs);
        try {
            validate(path, params);
            throw new Exception("CertPathValidator should have thrown an " +
              "InvalidAlgorithmParameterException");
        } catch (InvalidAlgorithmParameterException iape) {
        }
        try {
            X509CertSelector sel = new X509CertSelector();
            sel.setSubject("cn=sean");
            PKIXBuilderParameters bparams =
                new PKIXBuilderParameters(anchors, sel);
            build(bparams);
            throw new Exception("CertPathBuilder should have thrown an " +
              "InvalidAlgorithmParameterException");
        } catch (InvalidAlgorithmParameterException iape) {
        }
    }
    public static void createPath(String[] certs) throws Exception {
        X509Certificate anchorCert = getCertFromFile(certs[0]);
        byte [] nameConstraints = anchorCert.getExtensionValue("2.5.29.30");
        if (nameConstraints != null) {
            DerInputStream in = new DerInputStream(nameConstraints);
            nameConstraints = in.getOctetString();
        }
        TrustAnchor anchor = new TrustAnchor(anchorCert, nameConstraints);
        List list = new ArrayList();
        for (int i = 1; i < certs.length; i++) {
            list.add(0, getCertFromFile(certs[i]));
        }
        CertificateFactory cf = CertificateFactory.getInstance("X509");
        path = cf.generateCertPath(list);
        anchors = Collections.singleton(anchor);
        params = new PKIXParameters(anchors);
        params.setRevocationEnabled(false);
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
            CertPathValidator.getInstance("PKIX", "SUN");
        CertPathValidatorResult cpvr = validator.validate(path, params);
    }
    public static void build(PKIXBuilderParameters params)
        throws Exception {
        CertPathBuilder builder =
            CertPathBuilder.getInstance("PKIX", "SUN");
        CertPathBuilderResult cpbr = builder.build(params);
    }
}
