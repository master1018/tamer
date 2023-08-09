public class SunJCE_BC_LoadOrdering {
    public static void main(String args[]) throws Exception {
        Security.insertProviderAt(new MyProvider(), 1);
        KeyGenerator keyGen = KeyGenerator.getInstance("DES");
        keyGen.init(new SecureRandom());
        Key key = keyGen.generateKey();
        Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        cipher.doFinal("some string".getBytes());
    }
}
