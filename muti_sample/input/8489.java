public class SerialOld {
    public static void main(String[] args) throws Exception {
        deserializeTigerKey("DSA");
        deserializeTigerKey("RSA");
        deserializeKey("DSA");
        deserializeKey("RSA");
        deserializeKey("DH");
        deserializeKey("AES");
        deserializeKey("Blowfish");
        deserializeKey("DES");
        deserializeKey("DESede");
        deserializeKey("RC5");
        deserializeKey("HmacSHA1");
        deserializeKey("HmacMD5");
        deserializeKey("PBE");
    }
    private static void deserializeTigerKey(String algorithm) throws Exception {
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream
                        (System.getProperty("test.src", ".") +
                        File.separator +
                        algorithm + ".1.5.key"));
        ois.readObject();
        ois.close();
    }
    private static void deserializeKey(String algorithm) throws Exception {
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream
                        (System.getProperty("test.src", ".") +
                        File.separator +
                        algorithm + ".pre.1.5.key"));
        ois.readObject();
        ois.close();
    }
}
