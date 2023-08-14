public class KeyWrapping {
    public static void main(String[] args) throws Exception {
        Cipher c1 = Cipher.getInstance("DES", "SunJCE");
        Cipher c2 = Cipher.getInstance("DES");
        KeyGenerator keyGen = KeyGenerator.getInstance("DES");
        keyGen.init(56);
        SecretKey sKey = keyGen.generateKey();
        SecretKey sessionKey = keyGen.generateKey();
        c1.init(Cipher.WRAP_MODE, sKey);
        byte[] wrappedKey = c1.wrap(sessionKey);
        c1.init(Cipher.UNWRAP_MODE, sKey);
        SecretKey unwrappedSessionKey =
                              (SecretKey)c1.unwrap(wrappedKey, "DES",
                                                  Cipher.SECRET_KEY);
        c2.init(Cipher.ENCRYPT_MODE, unwrappedSessionKey);
        String msg = "Hello";
        byte[] cipherText = c2.doFinal(msg.getBytes());
        c2.init(Cipher.DECRYPT_MODE, unwrappedSessionKey);
        byte[] clearText = c2.doFinal(cipherText);
        if (!msg.equals(new String(clearText)))
            throw new Exception("The unwrapped session key is corrupted.");
        KeyPairGenerator kpairGen = KeyPairGenerator.getInstance("DSA");
        kpairGen.initialize(1024);
        KeyPair kpair = kpairGen.genKeyPair();
        PublicKey pub = kpair.getPublic();
        PrivateKey pri = kpair.getPrivate();
        c1.init(Cipher.WRAP_MODE, sKey);
        byte[] wrappedPub = c1.wrap(pub);
        byte[] wrappedPri = c1.wrap(pri);
        c1.init(Cipher.UNWRAP_MODE, sKey);
        Key unwrappedPub = c1.unwrap(wrappedPub, "DSA",
                                           Cipher.PUBLIC_KEY);
        Key unwrappedPri = c1.unwrap(wrappedPri, "DSA",
                                            Cipher.PRIVATE_KEY);
    }
}
