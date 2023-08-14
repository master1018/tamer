public class LegacyChainedExceptionSerialization {
    private static Throwable[] broken = {
        new ClassNotFoundException(),
        new ExceptionInInitializerError(),
        new java.lang.reflect.UndeclaredThrowableException(null),
        new java.lang.reflect.InvocationTargetException(null),
        new java.security.PrivilegedActionException(null),
        new java.awt.print.PrinterIOException(null)
    };
    public static void main(String[] args) throws Exception {
        for (int i=0; i<broken.length; i++)
            test(broken[i]);
    }
    private static void test(Throwable e) throws Exception {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bout);
        out.writeObject(e);
        out.flush();
        ByteArrayInputStream bin =
            new ByteArrayInputStream(bout.toByteArray());
        ObjectInputStream in = new ObjectInputStream(bin);
        Throwable clone = (Throwable) in.readObject();
    }
}
