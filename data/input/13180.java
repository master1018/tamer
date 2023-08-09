public class AccessorMethods {
    private static final String SIGNER1 = "AccessorMethods.signer1";
    private static final String SIGNER2 = "AccessorMethods.signer2";
    private static final String CA = "AccessorMethods.ca";
    public static void main(String[] args) throws Exception {
        File f = new File(System.getProperty("test.src", "."), CA);
        FileInputStream fis = new FileInputStream(f);
        CertificateFactory fac = CertificateFactory.getInstance("X.509");
        Certificate cacert = fac.generateCertificate(fis);
        Certificate[] signercerts = new Certificate[4];
        signercerts[1] = cacert;
        signercerts[3] = cacert;
        f = new File(System.getProperty("test.src", "."), SIGNER1);
        fis = new FileInputStream(f);
        Certificate signer1 = fac.generateCertificate(fis);
        signercerts[0] = signer1;
        f = new File(System.getProperty("test.src", "."), SIGNER2);
        fis = new FileInputStream(f);
        Certificate signer2 = fac.generateCertificate(fis);
        signercerts[2] = signer2;
        UnresolvedPermission up = new UnresolvedPermission
                        ("type", "name", "actions", signercerts);
        if (!up.getUnresolvedType().equals("type") ||
            !up.getUnresolvedName().equals("name") ||
            !up.getUnresolvedActions().equals("actions")) {
            throw new SecurityException("Test 1 Failed");
        }
        Certificate[] certs = up.getUnresolvedCerts();
        if (certs == null || certs.length != 2) {
            throw new SecurityException("Test 2 Failed");
        }
        boolean foundSigner1 = false;
        boolean foundSigner2 = false;
        if (certs[0].equals(signer1) || certs[1].equals(signer1)) {
            foundSigner1 = true;
        }
        if (certs[0].equals(signer2) || certs[1].equals(signer2)) {
            foundSigner2 = true;
        }
        if (!foundSigner1 || !foundSigner2) {
            throw new SecurityException("Test 3 Failed");
        }
    }
}
