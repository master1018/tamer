public class TestKeyPairGenerator extends PKCS11Test {
    private static Provider provider;
    private static byte[] data;
    private static void testSignature(String algorithm, PrivateKey privateKey, PublicKey publicKey) throws Exception {
        System.out.println("Testing " + algorithm + "...");
        Signature s = Signature.getInstance(algorithm, provider);
        s.initSign(privateKey);
        s.update(data);
        byte[] sig = s.sign();
        s.initVerify(publicKey);
        s.update(data);
        boolean result = s.verify(sig);
        if (result == false) {
            throw new Exception("Verification failed");
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
    private static void testInvalidSignature(KeyPair kp1, KeyPair kp2) throws Exception {
        System.out.println("Testing signature with incorrect key...");
        Signature sig = Signature.getInstance("MD5withRSA", provider);
        sig.initSign(kp1.getPrivate());
        byte[] data = new byte[100];
        sig.update(data);
        byte[] signature = sig.sign();
        sig.initVerify(kp1.getPublic());
        sig.update(data);
        if (sig.verify(signature) == false) {
            throw new Exception("verification failed");
        }
        sig.initVerify(kp2.getPublic());
        sig.update(data);
        if (sig.verify(signature)) {
            throw new Exception("verification unexpectedly succeeded");
        }
    }
    public static void main(String[] args) throws Exception {
        main(new TestKeyPairGenerator());
    }
    public void main(Provider p) throws Exception {
        long start = System.currentTimeMillis();
        provider = p;
        data = new byte[2048];
        int[] keyLengths = {512, 512, 1024};
        BigInteger[] pubExps = {null, BigInteger.valueOf(3), null};
        KeyPair[] keyPairs = new KeyPair[3];
        new Random().nextBytes(data);
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA", provider);
        for (int i = 0; i < keyLengths.length; i++) {
            int len = keyLengths[i];
            BigInteger exp = pubExps[i];
            System.out.println("Generating " + len + " bit keypair...");
            if (exp == null) {
                kpg.initialize(len);
            } else {
                kpg.initialize(new RSAKeyGenParameterSpec(len, exp));
            }
            KeyPair kp = kpg.generateKeyPair();
            keyPairs[i] = kp;
            RSAPublicKey publicKey = (RSAPublicKey)kp.getPublic();
            System.out.println(publicKey);
            RSAPrivateCrtKey privateKey = (RSAPrivateCrtKey)kp.getPrivate();
            if (publicKey.getModulus().equals(privateKey.getModulus()) == false) {
                throw new Exception("Moduli do not match");
            }
            if (publicKey.getPublicExponent().equals(privateKey.getPublicExponent()) == false) {
                throw new Exception("Exponents do not match");
            }
            int keyLen = publicKey.getModulus().bitLength();
            if ((keyLen > len) || (keyLen < len - 1)) {
                throw new Exception("Incorrect key length: " + keyLen);
            }
            if (exp != null) {
                if (exp.equals(publicKey.getPublicExponent()) == false) {
                    throw new Exception("Incorrect exponent");
                }
            }
            test(privateKey, publicKey);
        }
        testInvalidSignature(keyPairs[0], keyPairs[1]);
        testInvalidSignature(keyPairs[0], keyPairs[2]);
        long stop = System.currentTimeMillis();
        System.out.println("All tests passed (" + (stop - start) + " ms).");
    }
}
