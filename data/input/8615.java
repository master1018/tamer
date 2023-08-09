public class SourceOnly extends com.sun.javadoc.Doclet
{
    public static void main(String[] args) {
        int result = com.sun.tools.javadoc.Main.
            execute("javadoc", "p.SourceOnly", SourceOnly.class.getClassLoader(), new String[] {"p"});
        if (result != 0)
            throw new Error();
    }
    public static boolean start(com.sun.javadoc.RootDoc root) {
        if (root.classes().length != 1)
            throw new Error("wrong set of classes documented: " + java.util.Arrays.asList(root.classes()));
        return true;
    }
}
