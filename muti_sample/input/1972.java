public class TestKeyGenerator extends PKCS11Test {
    public static void main(String[] args) throws Exception {
        main(new TestKeyGenerator());
    }
    private TestResult test(String algorithm, int keyLen, Provider p,
                      TestResult expected)
        throws Exception {
        TestResult actual = TestResult.TBD;
        System.out.println("Testing " + algorithm + ", " + keyLen + " bits...");
        KeyGenerator kg;
        try {
            kg = KeyGenerator.getInstance(algorithm, p);
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Not supported, skipping: " + e);
            return TestResult.PASS;
        }
        try {
            kg.init(keyLen);
            actual = TestResult.PASS;
        } catch (InvalidParameterException ipe) {
            actual = TestResult.FAIL;
        }
        if (actual == TestResult.PASS) {
            try {
                SecretKey key = kg.generateKey();
                if (expected == TestResult.FAIL) {
                    throw new Exception("Generated " + key +
                        " using invalid key length");
                }
            } catch (ProviderException e) {
                e.printStackTrace();
                throw (Exception) (new Exception
                    ("key generation failed using valid length").initCause(e));
            }
        }
        if (expected != TestResult.TBD && expected != actual) {
            throw new Exception("Expected to " + expected + ", but " +
                actual);
        }
        return actual;
    }
    public void main(Provider p) throws Exception {
        test("DES", 0, p, TestResult.FAIL);
        test("DES", 56, p, TestResult.PASS); 
        test("DES", 64, p, TestResult.PASS);
        test("DES", 128, p, TestResult.FAIL);
        test("DESede", 0, p, TestResult.FAIL);
        TestResult temp = test("DESede", 112, p, TestResult.TBD);
        test("DESede", 128, p, temp);
        test("DESede", 168, p, TestResult.PASS);
        test("DESede", 192, p, TestResult.PASS);
        test("DESede", 64, p, TestResult.FAIL);
        test("DESede", 256, p, TestResult.FAIL);
        test("Blowfish", 0, p, TestResult.FAIL);
        test("Blowfish", 24, p, TestResult.FAIL);
        test("Blowfish", 32, p, TestResult.FAIL);
        test("Blowfish", 40, p, TestResult.PASS);
        test("Blowfish", 128, p, TestResult.PASS);
        test("Blowfish", 136, p, TestResult.TBD);
        test("Blowfish", 448, p, TestResult.TBD);
        test("Blowfish", 456, p, TestResult.FAIL);
        test("ARCFOUR", 0, p, TestResult.FAIL);
        test("ARCFOUR", 32, p, TestResult.FAIL);
        test("ARCFOUR", 40, p, TestResult.PASS);
        test("ARCFOUR", 128, p, TestResult.PASS);
        if (p.getName().equals("SunPKCS11-Solaris")) {
            test("ARCFOUR", 1024, p, TestResult.TBD);
        } else if (p.getName().equals("SunPKCS11-NSS")) {
            test("ARCFOUR", 1024, p, TestResult.PASS);
            test("ARCFOUR", 2048, p, TestResult.PASS);
            test("ARCFOUR", 2056, p, TestResult.FAIL);
        }
    }
}
