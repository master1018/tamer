public class TestOAEP {
    private static Provider cp;
    private static PrivateKey privateKey;
    private static PublicKey publicKey;
    private static Random random = new Random();
    public static void main(String[] args) throws Exception {
        long start = System.currentTimeMillis();
        cp = Security.getProvider("SunJCE");
        System.out.println("Testing provider " + cp.getName() + "...");
        Provider kfp = Security.getProvider("SunRsaSign");
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA", kfp);
        kpg.initialize(768);
        KeyPair kp = kpg.generateKeyPair();
        privateKey = kp.getPrivate();
        publicKey = kp.getPublic();
        Cipher.getInstance("RSA/ECB/OAEPwithMD5andMGF1Padding");
        Cipher.getInstance("RSA/ECB/OAEPwithSHA1andMGF1Padding");
        Cipher.getInstance("RSA/ECB/OAEPwithSHA-1andMGF1Padding");
        Cipher.getInstance("RSA/ECB/OAEPwithSHA-256andMGF1Padding");
        Cipher.getInstance("RSA/ECB/OAEPwithSHA-384andMGF1Padding");
        Cipher.getInstance("RSA/ECB/OAEPwithSHA-512andMGF1Padding");
        testEncryptDecrypt("MD5", 0);
        testEncryptDecrypt("MD5", 16);
        testEncryptDecrypt("MD5", 62);
        try {
            testEncryptDecrypt("MD5", 63);
            throw new Exception("Unexpectedly completed call");
        } catch (IllegalBlockSizeException e) {
            System.out.println(e);
        }
        testEncryptDecrypt("SHA1", 0);
        testEncryptDecrypt("SHA1", 16);
        testEncryptDecrypt("SHA1", 54);
        try {
            testEncryptDecrypt("SHA1", 55);
            throw new Exception("Unexpectedly completed call");
        } catch (IllegalBlockSizeException e) {
            System.out.println(e);
        }
        testEncryptDecrypt("SHA-1", 16);
        testEncryptDecrypt("SHA-256", 0);
        testEncryptDecrypt("SHA-256", 16);
        testEncryptDecrypt("SHA-256", 30);
        try {
            testEncryptDecrypt("SHA-256", 31);
            throw new Exception("Unexpectedly completed call");
        } catch (IllegalBlockSizeException e) {
            System.out.println(e);
        }
        try {
            testEncryptDecrypt("SHA-512", 1);
            throw new Exception("Unexpectedly completed call");
        } catch (InvalidKeyException e) {
            System.out.println(e);
        }
        Cipher c;
        byte[] enc;
        byte[] data = new byte[16];
        random.nextBytes(data);
        try {
            c = Cipher.getInstance("RSA/ECB/OAEPwithFOOandMGF1Padding", cp);
            throw new Exception("Unexpectedly completed call");
        } catch (NoSuchPaddingException e) {
            System.out.println(e);
        }
        c = Cipher.getInstance("RSA/ECB/OAEPwithMD5andMGF1Padding", cp);
        try {
            c.init(Cipher.ENCRYPT_MODE, privateKey);
            throw new Exception("Unexpectedly completed call");
        } catch (InvalidKeyException e) {
            System.out.println(e);
        }
        try {
            c.init(Cipher.DECRYPT_MODE, publicKey);
            throw new Exception("Unexpectedly completed call");
        } catch (InvalidKeyException e) {
            System.out.println(e);
        }
        c.init(Cipher.DECRYPT_MODE, privateKey);
        try {
            c.doFinal(data);
            throw new Exception("Unexpectedly completed call");
        } catch (BadPaddingException e) {
            System.out.println(e);
        }
        c.init(Cipher.ENCRYPT_MODE, publicKey);
        enc = c.doFinal(data);
        c = Cipher.getInstance("RSA/ECB/OAEPwithSHA1andMGF1Padding", cp);
        c.init(Cipher.DECRYPT_MODE, privateKey);
        try {
            c.doFinal(enc);
            throw new Exception("Unexpectedly completed call");
        } catch (BadPaddingException e) {
            System.out.println(e);
        }
        c = Cipher.getInstance("RSA/ECB/PKCS1Padding", cp);
        c.init(Cipher.ENCRYPT_MODE, publicKey);
        enc = c.doFinal(data);
        c = Cipher.getInstance("RSA/ECB/OAEPwithSHA1andMGF1Padding", cp);
        c.init(Cipher.DECRYPT_MODE, privateKey);
        try {
            c.doFinal(enc);
            throw new Exception("Unexpectedly completed call");
        } catch (BadPaddingException e) {
            System.out.println(e);
        }
        long stop = System.currentTimeMillis();
        System.out.println("Done (" + (stop - start) + " ms).");
    }
    private static void testEncryptDecrypt(String hashAlg, int dataLength) throws Exception {
        System.out.println("Testing OAEP with hash " + hashAlg + ", " + dataLength + " bytes");
        Cipher c = Cipher.getInstance("RSA/ECB/OAEPwith" + hashAlg + "andMGF1Padding", cp);
        c.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] data = new byte[dataLength];
        byte[] enc = c.doFinal(data);
        c.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] dec = c.doFinal(enc);
        if (Arrays.equals(data, dec) == false) {
            throw new Exception("Data does not match");
        }
    }
}
