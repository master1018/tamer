public class RSAEncryptDecrypt {
    public static final byte[] PLAINTEXT = {1,2,3,4,5,6,7,8,9,0,1,2,3,4,5,6};
    public static void main(String[] args) throws Exception {
        KeyPairGenerator generator =
            KeyPairGenerator.getInstance("RSA", "SunMSCAPI");
        KeyPair keyPair = generator.generateKeyPair();
        Key publicKey = keyPair.getPublic();
        Key privateKey = keyPair.getPrivate();
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance("RSA", "SunMSCAPI");
        } catch (GeneralSecurityException e) {
            System.out.println("Cipher not supported by provider, skipping...");
            return;
        }
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        displayBytes("Plaintext data:", PLAINTEXT);
        byte[] data = cipher.doFinal(PLAINTEXT);
        displayBytes("Encrypted data:", data);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        data = cipher.doFinal(data);
        displayBytes("Decrypted data:", data);
    }
    private static void displayBytes(String label, byte[] bytes) {
        System.out.println(label + " [length=" + bytes.length + "]");
        for (byte b : bytes) {
            System.out.print("0x" + Integer.toHexString(b & 0xFF) + " ");
        }
        System.out.println();
    }
}
