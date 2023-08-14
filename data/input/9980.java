public class TestRSACipher extends PKCS11Test {
    private static final String[] RSA_ALGOS =
        { "RSA/ECB/PKCS1Padding", "RSA" };
    public void main(Provider p) throws Exception {
        try {
            Cipher.getInstance(RSA_ALGOS[0], p);
        } catch (GeneralSecurityException e) {
            System.out.println("Not supported by provider, skipping");
            return;
        }
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA", p);
        kpg.initialize(1024);
        KeyPair kp = kpg.generateKeyPair();
        PublicKey publicKey = kp.getPublic();
        PrivateKey privateKey = kp.getPrivate();
        Random random = new Random();
        byte[] b, e, d;
        b = new byte[16];
        random.nextBytes(b);
        for (String rsaAlgo: RSA_ALGOS) {
            Cipher c1 = Cipher.getInstance(rsaAlgo, p);
            Cipher c2 = Cipher.getInstance(rsaAlgo, "SunJCE");
            c1.init(Cipher.ENCRYPT_MODE, publicKey);
            e = c1.doFinal(b);
            c1.init(Cipher.DECRYPT_MODE, privateKey);
            d = c1.doFinal(e);
            match(b, d);
            c2.init(Cipher.DECRYPT_MODE, privateKey);
            d = c2.doFinal(e);
            match(b, d);
            c1.init(Cipher.DECRYPT_MODE, publicKey);
            try {
                d = c1.doFinal(e);
                throw new Exception("completed call");
            } catch (BadPaddingException ee) {
                ee.printStackTrace();
            }
            c1.init(Cipher.ENCRYPT_MODE, privateKey);
            e = c1.doFinal(b);
            c1.init(Cipher.DECRYPT_MODE, publicKey);
            d = c1.doFinal(e);
            match(b, d);
            c2.init(Cipher.DECRYPT_MODE, publicKey);
            d = c2.doFinal(e);
            match(b, d);
            c1.init(Cipher.ENCRYPT_MODE, privateKey);
            c1.init(Cipher.ENCRYPT_MODE, privateKey);
            e = c1.doFinal(b);
            e = c1.doFinal(b);
            c1.update(b);
            c1.update(b);
            c1.init(Cipher.ENCRYPT_MODE, privateKey);
            e = c1.doFinal();
            e = c1.doFinal();
            c1.update(b);
            e = c1.doFinal();
            c1.update(new byte[256]);
            try {
                e = c1.doFinal();
                throw new Exception("completed call");
            } catch (IllegalBlockSizeException ee) {
                System.out.println(ee);
            }
        }
    }
    private static void match(byte[] b1, byte[] b2) throws Exception {
        if (Arrays.equals(b1, b2) == false) {
            System.out.println(toString(b1));
            System.out.println(toString(b2));
            throw new Exception("mismatch");
        }
    }
    public static void main(String[] args) throws Exception {
        main(new TestRSACipher());
    }
}
