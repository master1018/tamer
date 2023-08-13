public class ClassLoaderObjectInputStream extends ObjectInputStream {
    private ClassLoader classLoader;
    public ClassLoaderObjectInputStream(
            ClassLoader classLoader, InputStream inputStream)
            throws IOException, StreamCorruptedException {
        super(inputStream);
        this.classLoader = classLoader;
    }
    protected Class resolveClass(ObjectStreamClass objectStreamClass)
            throws IOException, ClassNotFoundException {
        Class clazz = Class.forName(objectStreamClass.getName(), false, classLoader);
        if (clazz != null) {
            return clazz;
        } else {
            return super.resolveClass(objectStreamClass);
        }
    }
}
