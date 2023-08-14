public class CreateSerialized {
    public static void main(String[] args) throws Exception {
        Object o = new com.sun.crypto.provider.SunJCE();
        FileOutputStream fos = new FileOutputStream("object.tmp");
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(fos);
        objectOutputStream.writeObject(o);
        fos.close();
    }
}
