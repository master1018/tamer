public class SerializeProvider extends PKCS11Test {
    public void main(Provider p) throws Exception {
        if (Security.getProvider(p.getName()) != p) {
            System.out.println("Provider not installed in Security, skipping");
            return;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream oout = new ObjectOutputStream(out);
        oout.writeObject(p);
        oout.close();
        byte[] data = out.toByteArray();
        InputStream in = new ByteArrayInputStream(data);
        ObjectInputStream oin = new ObjectInputStream(in);
        Provider p2 = (Provider)oin.readObject();
        System.out.println("Reconstituted: " + p2);
        if (p != p2) {
            throw new Exception("Provider object mismatch");
        }
    }
    public static void main(String[] args) throws Exception {
        main(new SerializeProvider());
    }
}
