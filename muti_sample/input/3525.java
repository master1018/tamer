public class SunCertPathBuilderExceptionTest {
    public static void main(String[] args) throws Exception {
        try {
            CertPathBuilder cpb = CertPathBuilder.
                getInstance(CertPathBuilder.getDefaultType());
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            File f = new File
                (System.getProperty("test.src", "."), "speech2speech");
            X509Certificate cert = (X509Certificate)
                cf.generateCertificate(new FileInputStream(f));
            TrustAnchor anchor = new TrustAnchor(cert, null);
            X509CertSelector xs = new X509CertSelector();
            xs.setSubject("CN=A, OU=A, O=A, L=A, ST=A, C=A");
            PKIXBuilderParameters pkp =
                new PKIXBuilderParameters(Collections.singleton(anchor), xs);
            cpb.build(pkp);
        } catch(SunCertPathBuilderException e) {
            System.out.println("Got the Exception: ");
            try {
                ObjectOutputStream o =
                    new ObjectOutputStream(new ByteArrayOutputStream());
                o.writeObject(e);
                o.close();
            } catch(NotSerializableException e2) {
                System.out.println("Test fail: bug not corrected");
                throw e2;
            }
            System.out.println("Test pass: SCPEE is Serializable");
            return;
        }
        throw new Exception("Test fail: Strange, no SCPEE thrown");
    }
}
