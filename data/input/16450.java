public class PublicKeyInterop {
    public static void main(String[] arg) throws Exception {
        PrivateKey privKey = null;
        Certificate cert = null;
        KeyStore ks = KeyStore.getInstance("Windows-MY");
        ks.load(null, null);
        System.out.println("Loaded keystore: Windows-MY");
        PublicKey myPuKey =
            (PublicKey) ks.getCertificate("6888925").getPublicKey();
        System.out.println("Public key is a " + myPuKey.getClass().getName());
        PrivateKey myPrKey = (PrivateKey) ks.getKey("6888925", null);
        System.out.println("Private key is a " + myPrKey.getClass().getName());
        System.out.println();
        byte[] plain = new byte[] {0x01, 0x02, 0x03, 0x04, 0x05};
        HexDumpEncoder hde = new HexDumpEncoder();
        System.out.println("Plaintext:\n" + hde.encode(plain) + "\n");
        Cipher rsa = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        rsa.init(Cipher.ENCRYPT_MODE, myPuKey);
        byte[] encrypted = rsa.doFinal(plain);
        System.out.println("Encrypted plaintext using RSA Cipher from " +
            rsa.getProvider().getName() + " JCE provider\n");
        System.out.println(hde.encode(encrypted) + "\n");
        Cipher rsa2 = Cipher.getInstance("RSA/ECB/PKCS1Padding", "SunMSCAPI");
        rsa2.init(Cipher.ENCRYPT_MODE, myPuKey);
        byte[] encrypted2 = rsa2.doFinal(plain);
        System.out.println("Encrypted plaintext using RSA Cipher from " +
            rsa2.getProvider().getName() + " JCE provider\n");
        System.out.println(hde.encode(encrypted2) + "\n");
        Cipher rsa3 = Cipher.getInstance("RSA/ECB/PKCS1Padding", "SunMSCAPI");
        rsa3.init(Cipher.DECRYPT_MODE, myPrKey);
        byte[] decrypted = rsa3.doFinal(encrypted);
        System.out.println("Decrypted first ciphertext using RSA Cipher from " +
            rsa3.getProvider().getName() + " JCE provider\n");
        System.out.println(hde.encode(decrypted) + "\n");
        if (! Arrays.equals(plain, decrypted)) {
            throw new Exception("First decrypted ciphertext does not match " +
                "original plaintext");
        }
        decrypted = rsa3.doFinal(encrypted2);
        System.out.println("Decrypted second ciphertext using RSA Cipher from "
            + rsa3.getProvider().getName() + " JCE provider\n");
        System.out.println(hde.encode(decrypted) + "\n");
        if (! Arrays.equals(plain, decrypted)) {
            throw new Exception("Second decrypted ciphertext does not match " +
                "original plaintext");
        }
    }
}
