public class NoClassDefFoundErrorTrap {
    private static NoClassDefFoundError ncdfe;
    public interface Bar {}
    public static class Foo implements Bar, java.io.Serializable {}
    public static class TestObjectInputStream extends ObjectInputStream {
        public TestObjectInputStream(InputStream in)
            throws IOException
        {
            super(in);
        }
        protected Class resolveClass(ObjectStreamClass desc)
            throws IOException, ClassNotFoundException
        {
            String name = desc.getName();
            if (name.equals(Foo.class.getName())) {
                ncdfe = new NoClassDefFoundError("Bar");
                throw ncdfe;
            } else {
                return super.resolveClass(desc);
            }
        }
    }
    public static void main(String[] args) {
        System.err.println("\nRegression test for bug 4205440\n");
        try {
            Foo foo = new Foo();
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bout);
            out.writeObject(foo);
            byte[] stream = bout.toByteArray();
            ByteArrayInputStream bin = new ByteArrayInputStream(stream);
            ObjectInputStream in = new TestObjectInputStream(bin);
            try {
                in.readObject();
            } catch (NoClassDefFoundError e) {
                if (e == ncdfe) {
                    System.err.println("TEST PASSED: " + e.toString());
                } else {
                    throw e;
                }
            }
        } catch (Exception e) {
            System.err.println("\nTEST FAILED:");
            e.printStackTrace();
            throw new RuntimeException("TEST FAILED: " + e.toString());
        }
    }
}
