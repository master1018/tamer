public class GetKeySpecException2 {
    private static final String cipherAlg = "PBEWithMD5AndDES";
    private static final char[] passwd = { 'p','a','s','s','w','d' };
    public static void main(String[] argv) throws Exception {
        byte[] encryptedData = new byte[30];
        encryptedData[20] = (byte) 8;
        EncryptedPrivateKeyInfo epki =
            new EncryptedPrivateKeyInfo(cipherAlg, encryptedData);
        System.out.println("Testing getKeySpec(Cipher) with WRAP_MODE...");
        Cipher c = Cipher.getInstance(cipherAlg, "SunJCE");
        MyPBEKey key = new MyPBEKey(passwd);
        c.init(Cipher.WRAP_MODE, key);
        try {
            epki.getKeySpec(c);
            throw new Exception("Should throw InvalidKeyException");
        } catch (InvalidKeySpecException npe) {
            System.out.println("Expected IKE thrown");
        }
        AlgorithmParameters params = c.getParameters();
        System.out.println("Testing getKeySpec(Cipher) with UNWRAP_MODE...");
        c.init(Cipher.UNWRAP_MODE, key, params);
        try {
            epki.getKeySpec(c);
            throw new Exception("Should throw InvalidKeyException");
        } catch (InvalidKeySpecException npe) {
            System.out.println("Expected IKE thrown");
        }
        System.out.println("All Tests Passed");
    }
}
class MyPBEKey implements PBEKey {
    private char[] password = null;
    MyPBEKey(char[] password) {
        this.password = (char[]) password.clone();
    }
    public int getIterationCount() {
        return 0;
    }
    public char[] getPassword() {
        return (char[]) password.clone();
    }
    public byte[] getSalt() {
        return null;
    }
    public String getAlgorithm() {
        return "PBE";
    }
    public String getFormat() {
        return "RAW";
    }
    public byte[] getEncoded() {
        return new byte[8];
    }
}
