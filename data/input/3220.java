public class Test6329581 extends URLClassLoader implements ExceptionListener {
    public static final class Bean {
    }
    public static void main(String[] args) throws Exception {
        new Test6329581().decode(new Test6329581().encode(Bean.class.getName()));
    }
    private Test6329581() {
        super(new URL[] {
                Test6329581.class.getProtectionDomain().getCodeSource().getLocation()
        });
    }
    @Override
    protected Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class c = findLoadedClass(name);
        if (c == null) {
            if (Bean.class.getName().equals(name)) {
                c = findClass(name);
            }
            else try {
                c = getParent().loadClass(name);
            }
            catch (ClassNotFoundException exception) {
                c = findClass(name);
            }
        }
        if (resolve) {
            resolveClass(c);
        }
        return c;
    }
    public void exceptionThrown(Exception exception) {
        throw new Error("unexpected exception", exception);
    }
    private void validate(Object object) {
        if (!object.getClass().getClassLoader().equals(this)) {
            throw new Error("Bean is loaded with unexpected class loader");
        }
    }
    private byte[] encode(String name) throws Exception {
        Object object = loadClass(name).newInstance();
        validate(object);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        XMLEncoder encoder = new XMLEncoder(out);
        encoder.setExceptionListener(this);
        encoder.writeObject(object);
        encoder.close();
        return out.toByteArray();
    }
    private Object decode(byte[] array) {
        ByteArrayInputStream in = new ByteArrayInputStream(array);
        XMLDecoder decoder = new XMLDecoder(in, null, this, this);
        Object object = decoder.readObject();
        validate(object);
        decoder.close();
        return object;
    }
}
