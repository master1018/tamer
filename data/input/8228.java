public class CompletionFailure extends Doclet
{
    public static void main(String[] args) {
        if (com.sun.tools.javadoc.Main.execute("javadoc",
                                               "CompletionFailure",
                                               CompletionFailure.class.getClassLoader(),
                                               new String[]{"pkg"}) != 0)
            throw new Error();
    }
    public static boolean start(com.sun.javadoc.RootDoc root) {
        ClassDoc[] classes = root.classes();
        if (classes.length != 1)
            throw new Error("1 " + Arrays.asList(classes));
        return true;
    }
}
