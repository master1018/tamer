public class SerialDSAPubKey {
    private static final String SUN = "SUN";
    public static void main(String[] args) throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("DSA", SUN);
        kpg.initialize(512);
        KeyPair dsaKp = kpg.genKeyPair();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(dsaKp);
        oos.close();
        ObjectInputStream ois = new ObjectInputStream
                        (new ByteArrayInputStream(baos.toByteArray()));
        KeyPair dsaKp2 = (KeyPair)ois.readObject();
        ois.close();
        if (!dsaKp2.getPublic().equals(dsaKp.getPublic()) ||
            !dsaKp2.getPrivate().equals(dsaKp.getPrivate())) {
            throw new SecurityException("DSA test failed");
        }
    }
}
