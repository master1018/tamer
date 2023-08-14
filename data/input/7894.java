public class DecryptWithoutParameters {
    public static void main(String argv[]) throws Exception {
        String algo = "PBEWithMD5AndDES";
        Cipher cipher = Cipher.getInstance(algo, "SunJCE");
        SecretKey key = new SecretKeySpec(new byte[5], algo);
        try {
            cipher.init(Cipher.DECRYPT_MODE, key);
            throw new Exception("Should throw InvalidKeyException when " +
                                "decrypting without parameters");
        } catch (InvalidKeyException ike) {
            System.out.println("Test Passed: InvalidKeyException thrown");
        }
    }
}
