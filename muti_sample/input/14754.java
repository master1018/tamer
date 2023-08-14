public class EmptyCollectionSerialization {
    private static Object patheticDeepCopy(Object o) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(o);
        byte[] serializedForm = bos.toByteArray();
        InputStream is = new ByteArrayInputStream(serializedForm);
        ObjectInputStream ois = new ObjectInputStream(is);
        return ois.readObject();
    }
    private static boolean isSingleton(Object o) throws Exception {
        return patheticDeepCopy(o) == o;
    }
    public static void main(String[] args) throws Exception {
        if (!isSingleton(Collections.EMPTY_SET))
            throw new Exception("EMPTY_SET");
        if (!isSingleton(Collections.EMPTY_LIST))
            throw new Exception("EMPTY_LIST");
        if (!isSingleton(Collections.EMPTY_MAP))
            throw new Exception("EMPTY_MAP");
    }
}
