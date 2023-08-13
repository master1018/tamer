public class Sealtest {
    public static void main(String[] args) throws Exception {
        Security.addProvider(new com.sun.crypto.provider.SunJCE());
        KeyPairGenerator kpgen = KeyPairGenerator.getInstance("DSA");
        kpgen.initialize(512);
        KeyPair kp = kpgen.generateKeyPair();
        KeyGenerator kg = KeyGenerator.getInstance("DES");
        SecretKey skey = kg.generateKey();
        Cipher c = Cipher.getInstance("DES/CFB16/PKCS5Padding");
        c.init(Cipher.ENCRYPT_MODE, skey);
        SealedObject sealed = new SealedObject(kp.getPrivate(), c);
        FileOutputStream fos = new FileOutputStream("sealed");
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(sealed);
        FileInputStream fis = new FileInputStream("sealed");
        ObjectInputStream ois = new ObjectInputStream(fis);
        sealed = (SealedObject)ois.readObject();
        System.out.println(sealed.getAlgorithm());
        PrivateKey priv = (PrivateKey)sealed.getObject(skey);
        if (!priv.equals(kp.getPrivate()))
            throw new Exception("TEST FAILED");
        System.out.println("TEST SUCCEEDED");
    }
}
