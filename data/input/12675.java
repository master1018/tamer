public class SerializeExceptions {
    public static void main(String args[]) throws Exception {
        test(new BackingStoreException("Hi"));
        test(new InvalidPreferencesFormatException("Mom!"));
    }
    static void test(Object o) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bos);
        out.writeObject(o);
        out.flush();
        out.close();
    }
}
