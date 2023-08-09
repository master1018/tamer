public class TestHostnameChecker {
    private final static String PATH = System.getProperty("test.src", ".");
    public static void main(String[] args) throws Exception {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        InputStream in = new FileInputStream(new File(PATH, "cert1.crt"));
        X509Certificate cert1 = (X509Certificate)cf.generateCertificate(in);
        in.close();
        in = new FileInputStream(new File(PATH, "cert2.crt"));
        X509Certificate cert2 = (X509Certificate)cf.generateCertificate(in);
        in.close();
        in = new FileInputStream(new File(PATH, "cert3.crt"));
        X509Certificate cert3 = (X509Certificate)cf.generateCertificate(in);
        in.close();
        in = new FileInputStream(new File(PATH, "cert4.crt"));
        X509Certificate cert4 = (X509Certificate)cf.generateCertificate(in);
        in.close();
        HostnameChecker checker = HostnameChecker.getInstance(
                                        HostnameChecker.TYPE_TLS);
        System.out.println("TLS tests.........");
        System.out.println("==================");
        check(checker, "foo1.com", cert1, true);
        check(checker, "foo2.com", cert1, false);
        check(checker, "1.2.3.4", cert2, false);
        check(checker, "foo1.com", cert3, false);
        check(checker, "foo2.com", cert3, false);
        check(checker, "altfoo1.com", cert3, true);
        check(checker, "altfoo2.com", cert3, true);
        check(checker, "5.6.7.8", cert3, true);
        check(checker, "foo.bar.com", cert4, true);
        check(checker, "altfoo.bar.com", cert4, true);
        checker = HostnameChecker.getInstance(
                                HostnameChecker.TYPE_LDAP);
        System.out.println();
        System.out.println("LDAP tests.........");
        System.out.println("==================");
        check(checker, "foo1.com", cert1, true);
        check(checker, "foo2.com", cert1, false);
        check(checker, "foo1.com", cert3, false);
        check(checker, "foo2.com", cert3, false);
        check(checker, "altfoo1.com", cert3, true);
        check(checker, "altfoo2.com", cert3, true);
        check(checker, "5.6.7.8", cert3, true);
        check(checker, "foo.bar.com", cert4, true);
        check(checker, "altfoo.bar.com", cert4, false);
    }
    private static void check(HostnameChecker checker, String name,
                 X509Certificate cert, boolean expectedResult)
                 throws Exception {
        try {
            checker.match(name, cert);
            if (expectedResult == false) {
                throw new Exception("Passed invalid test: " + name);
            }
        } catch (CertificateException e) {
            if (expectedResult == true) {
                throw e;
            }
        }
        System.out.println("OK: " + name);
    }
}
