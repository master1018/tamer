public class NoStar extends Doclet
{
    public static void main(String[] args) {
        if (com.sun.tools.javadoc.Main.
            execute("javadoc", "NoStar", NoStar.class.getClassLoader(),
                    new String[] {System.getProperty("test.src", ".") + java.io.File.separatorChar + "NoStar.java"}) != 0)
            throw new Error();
    }
    public static boolean start(com.sun.javadoc.RootDoc root) {
        ClassDoc[] classes = root.classes();
        if (classes.length != 1)
            throw new Error("1 " + Arrays.asList(classes));
        ClassDoc self = classes[0];
        String c = self.commentText();
        System.out.println("\"" + c + "\"");
        return c.equals("First sentence.\n0\n 1\n  2\n   3\n    4\n     5");
    }
}
