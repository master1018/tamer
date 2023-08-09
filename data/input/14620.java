public final class BuildOddSel {
    private static PKIXBuilderParameters params;
    private static CertSelector sel;
    public static void main(String[] args) throws Exception {
        try {
            createParams();
            build(params);
            throw new Exception
                ("CertPath should not have been built successfully");
        } catch (InvalidAlgorithmParameterException iape) {
        }
    }
    static class OddSel implements CertSelector {
        public Object clone() {
            try {
                return super.clone();
            } catch (CloneNotSupportedException e) {
                throw new UnknownError();
            }
        }
        public boolean match(Certificate cert) {
            return(false);
        }
    }
    public static void createParams() throws Exception {
        TrustAnchor anchor = new TrustAnchor(getCertFromFile("sun.cer"), null);
        Set anchors = Collections.singleton(anchor);
        sel = new OddSel();
        params = new PKIXBuilderParameters(anchors, sel);
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
    public static void build(PKIXBuilderParameters params)
        throws Exception {
        CertPathBuilder builder =
            CertPathBuilder.getInstance("PKIX");
        CertPathBuilderResult cpbr = builder.build(params);
    }
}
