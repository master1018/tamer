public class OpenSSLCert {
    static final String OUTFILE = "6535697.test";
    public static void main(String[] args) throws Exception {
        test("open");
        test("pem");
        test("open", "open");
        test("open", "pem");
        test("pem", "pem");
        test("pem", "open");
        test("open", "pem", "open");
        test("pem", "open", "pem");
    }
    static void test(String... files) throws Exception {
        try (FileOutputStream fout = new FileOutputStream(OUTFILE)) {
            String here = System.getProperty("test.src", "");
            for (String file: files) {
                Files.copy(Paths.get(here, file), fout);
            }
        }
        try (FileInputStream fin = new FileInputStream(OUTFILE)) {
            System.out.println("Testing " + Arrays.toString(files) + "...");
            if (CertificateFactory.getInstance("X509")
                    .generateCertificates(fin)
                    .size() != files.length) {
                throw new Exception("Not same number");
            }
        }
        Files.delete(Paths.get(OUTFILE));
    }
}
