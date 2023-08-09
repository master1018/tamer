public class NullKeySealedObject {
    public static void main(String[] argv) throws Exception {
        Cipher c = Cipher.getInstance("DES/CBC/PKCS5Padding");
        SecretKey cipherKey = new SecretKeySpec(new byte[8], "DES");
        Key obj = new SecretKeySpec(new byte[8], "ANY");
        c.init(Cipher.ENCRYPT_MODE, cipherKey);
        SealedObject so = new SealedObject(obj, c);
        try {
            so.getObject((Key)null);
            throw new Exception("Sealed Object didn't throw an exception");
        } catch (NullPointerException e) {
            System.out.println("Got expected NullPointer");
        }
        try {
            so.getObject((Key)null, "ANY");
            throw new Exception("Sealed Object didn't throw an exception");
        } catch (NullPointerException e) {
            System.out.println("Got expected NullPointer");
        }
    }
}
