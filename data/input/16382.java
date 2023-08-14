public class XWerror extends Doclet
{
    public static void main(String[] args) {
        if (com.sun.tools.javadoc.Main.
            execute("javadoc", "XWerror", XWerror.class.getClassLoader(),
                    new String[] {"-Xwerror",
                                  System.getProperty("test.src", ".") +
                                  java.io.File.separatorChar +
                                  "XWerror.java"}) == 0)
            throw new Error();
    }
    public static boolean start(com.sun.javadoc.RootDoc root) {
        root.printWarning(null, "warning message");
        return false;
    }
}
