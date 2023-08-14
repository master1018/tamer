public class EmptySubject {
    public static void main(String[] args) throws Exception {
        try {
            File f = new File(System.getProperty("test.src", "."),
                "emptySubjectCert");
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            try {
                X509Certificate cert = (X509Certificate)
                    cf.generateCertificate(new FileInputStream(f));
                throw new Exception("Test 1 Failed - parsed invalid cert");
            } catch (CertificateParsingException e) {
                System.out.println("Test 1 passed: " + e.toString());
            }
            f = new File(System.getProperty("test.src", "."),
                "emptyIssuerCert");
            try {
                X509Certificate cert2 = (X509Certificate)
                    cf.generateCertificate(new FileInputStream(f));
                throw new Exception("Test 2 Failed - parsed invalid cert");
            } catch (CertificateParsingException e) {
                System.out.println("Test 2 passed: " + e.toString());
            }
        } catch (Exception e) {
            SecurityException se = new SecurityException("Test Failed");
            se.initCause(e);
            throw se;
        }
    }
}
