@TestTargetClass(KeyStore.PrivateKeyEntry.class)
public class KSPrivateKeyEntryTest extends TestCase {
    private PrivateKey testPrivateKey;
    private Certificate [] testChain;
    private void createParams(boolean diffCerts, boolean diffKeys) {
        byte[] encoded = {(byte)0, (byte)1, (byte)2, (byte)3};
        testChain = new Certificate[5];
        for (int i = 0; i < testChain.length; i++) {
            String s = (diffCerts ? Integer.toString(i) : "NEW");
            testChain[i] = new MyCertificate("MY_TEST_CERTIFICATE_"
                    .concat(s), encoded);
        }
        testPrivateKey = (diffKeys ? (PrivateKey)new tmpPrivateKey() : 
            (PrivateKey)new tmpPrivateKey(testChain[0].getPublicKey().getAlgorithm()));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "PrivateKeyEntry",
        args = {java.security.PrivateKey.class, java.security.cert.Certificate[].class}
    )
    public void testPrivateKeyEntry01() {
        Certificate[] certs = new MyCertificate[1];
        PrivateKey pk = null;
        try {
            new KeyStore.PrivateKeyEntry(pk, certs);
            fail("NullPointerException must be thrown when privateKey is null");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "PrivateKeyEntry",
        args = {java.security.PrivateKey.class, java.security.cert.Certificate[].class}
    )
    public void testPrivateKeyEntry02() {
        Certificate[] chain = null;
        PrivateKey pk = new tmpPrivateKey();
        try {
            new KeyStore.PrivateKeyEntry(pk, chain);
            fail("NullPointerException must be thrown when chain is null");
        } catch (NullPointerException e) {
        }
        try {
            chain = new Certificate[0];
            new KeyStore.PrivateKeyEntry(pk, chain);
            fail("IllegalArgumentException must be thrown when chain length is 0");
        } catch (IllegalArgumentException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "PrivateKeyEntry",
        args = {java.security.PrivateKey.class, java.security.cert.Certificate[].class}
    )
    public void testPrivateKeyEntry03() {
        createParams(true, false);
        try {
            new KeyStore.PrivateKeyEntry(testPrivateKey, testChain);
            fail("IllegalArgumentException must be thrown when chain contains certificates of different types");
        } catch (IllegalArgumentException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "PrivateKeyEntry",
        args = {java.security.PrivateKey.class, java.security.cert.Certificate[].class}
    )
    public void testPrivateKeyEntry04() {
        createParams(false, true);               
        try {
            new KeyStore.PrivateKeyEntry(testPrivateKey, testChain);
            fail("IllegalArgumentException must be thrown when key algorithms do not match");
        } catch (IllegalArgumentException e) {       
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getPrivateKey",
        args = {}
    )
    public void testGetPrivateKey() {
        createParams(false, false);
        KeyStore.PrivateKeyEntry ksPKE = new KeyStore.PrivateKeyEntry(
                testPrivateKey, testChain);
        assertEquals("Incorrect PrivateKey", testPrivateKey, ksPKE
                .getPrivateKey());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getCertificateChain",
        args = {}
    )
    public void testGetCertificateChain() {
        createParams(false, false);
        KeyStore.PrivateKeyEntry ksPKE = new KeyStore.PrivateKeyEntry(
                testPrivateKey, testChain);
        Certificate[] res = ksPKE.getCertificateChain();
        assertEquals("Incorrect chain length", testChain.length, res.length);
        for (int i = 0; i < res.length; i++) {
            assertEquals("Incorrect chain element: "
                    .concat(Integer.toString(i)), testChain[i], res[i]);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getCertificate",
        args = {}
    )
    public void testGetCertificate() {
        createParams(false, false);
        KeyStore.PrivateKeyEntry ksPKE = new KeyStore.PrivateKeyEntry(
                testPrivateKey, testChain);
        Certificate res = ksPKE.getCertificate();
        assertEquals("Incorrect end certificate (number 0)", testChain[0], res);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "toString",
        args = {}
    )
    public void testToString() {
        createParams(false, false);
        KeyStore.PrivateKeyEntry ksPKE = new KeyStore.PrivateKeyEntry(
                testPrivateKey, testChain);
        String res = ksPKE.toString();
        assertNotNull("toString() returns null", res);
    }
    public static Test suite() {
        return new TestSuite(KSPrivateKeyEntryTest.class);
    }
    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }
    private static class tmpPrivateKey implements PrivateKey {
        private String alg = "My algorithm";
        public String getAlgorithm() {
            return alg;
        }
        public String getFormat() {
            return "My Format";
        }
        public byte[] getEncoded() {
            return new byte[1];
        }
        public tmpPrivateKey() {
        }
        public tmpPrivateKey(String algorithm) {
            super();
            alg = algorithm;
        }
    }
}
