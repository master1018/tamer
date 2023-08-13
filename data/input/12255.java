public class Append {
    private static Class [] io = {
        Writer.class, BufferedWriter.class, FilterWriter.class,
        OutputStreamWriter.class, FileWriter.class
    };
    private static Class [] nio = {
        CharArrayWriter.class, StringWriter.class, PrintWriter.class,
        PrintStream.class
    };
    public static void main(String [] args) {
        for (int i = 0; i < io.length; i++)
            test(io[i], true);
        for (int i = 0; i < nio.length; i++)
            test(nio[i], false);
    }
    private static void test(Class c, boolean io) {
        try {
            Class [] cparams = { char.class };
            test(c.getMethod("append", cparams), io);
            Class [] csparams = { CharSequence.class };
            test(c.getMethod("append", csparams), io);
        } catch (NoSuchMethodException x) {
            throw new RuntimeException("No append method found");
        }
    }
    private static void test(Method m, boolean io) {
        Class [] ca = m.getExceptionTypes();
        boolean found = false;
        for (int i = 0; i < ca.length; i++) {
            if (ca[i].equals(IOException.class)) {
                found = true;
                break;
            }
        }
        if (found && !io)
            throw new RuntimeException("Unexpected IOException");
        if (!found && io)
            throw new RuntimeException("Missing IOException");
    }
}
