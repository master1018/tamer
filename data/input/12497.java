public class TestSignatures extends PKCS11Test {
    private final static String BASE = System.getProperty("test.src", ".");
    private static final char[] password = "test12".toCharArray();
    private static Provider provider;
    private static byte[] data;
    static KeyStore getKeyStore() throws Exception {
        InputStream in = new FileInputStream(new File(BASE, "rsakeys.ks"));
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(in, password);
        in.close();
        return ks;
    }
    private static void testSignature(String algorithm, PrivateKey privateKey, PublicKey publicKey) throws Exception {
        System.out.println("Testing " + algorithm + "...");
        Signature s = Signature.getInstance(algorithm, provider);
        s.initSign(privateKey);
        s.update(data);
        byte[] sig = s.sign();
        s.initVerify(publicKey);
        s.update(data);
        boolean result;
        result = s.verify(sig);
        if (result == false) {
            throw new Exception("Verification 1 failed");
        }
        s.update(data);
        result = s.verify(sig);
        if (result == false) {
            throw new Exception("Verification 2 failed");
        }
        result = s.verify(sig);
        if (result == true) {
            throw new Exception("Verification 3 succeeded");
        }
    }
    private static void test(PrivateKey privateKey, PublicKey publicKey) throws Exception {
        testSignature("MD2withRSA", privateKey, publicKey);
        testSignature("MD5withRSA", privateKey, publicKey);
        testSignature("SHA1withRSA", privateKey, publicKey);
        testSignature("SHA256withRSA", privateKey, publicKey);
        RSAPublicKey rsaKey = (RSAPublicKey)publicKey;
        if (rsaKey.getModulus().bitLength() > 512) {
            testSignature("SHA384withRSA", privateKey, publicKey);
            testSignature("SHA512withRSA", privateKey, publicKey);
        }
    }
    public static void main(String[] args) throws Exception {
        main(new TestSignatures());
    }
    public void main(Provider p) throws Exception {
        long start = System.currentTimeMillis();
        provider = p;
        data = new byte[2048];
        new Random().nextBytes(data);
        KeyStore ks = getKeyStore();
        KeyFactory kf = KeyFactory.getInstance("RSA", provider);
        for (Enumeration e = ks.aliases(); e.hasMoreElements(); ) {
            String alias = (String)e.nextElement();
            if (ks.isKeyEntry(alias)) {
                System.out.println("* Key " + alias + "...");
                PrivateKey privateKey = (PrivateKey)ks.getKey(alias, password);
                PublicKey publicKey = ks.getCertificate(alias).getPublicKey();
                privateKey = (PrivateKey)kf.translateKey(privateKey);
                publicKey = (PublicKey)kf.translateKey(publicKey);
                test(privateKey, publicKey);
            }
        }
        long stop = System.currentTimeMillis();
        System.out.println("All tests passed (" + (stop - start) + " ms).");
    }
}
