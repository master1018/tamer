public class DupOk extends Doclet
{
    public static void main(String[] args) {
        if (com.sun.tools.javadoc.Main.
            execute("javadoc", "DupOk", DupOk.class.getClassLoader(),
                    new String[]
                {"-sourcepath",
                 System.getProperty("test.src", ".") + java.io.File.separatorChar + "sp1" +
                 System.getProperty("path.separator") +
                 System.getProperty("test.src", ".") + java.io.File.separatorChar + "sp2",
                 "p"
                }) != 0)
            throw new Error();
    }
    public static boolean start(com.sun.javadoc.RootDoc root) {
        ClassDoc[] classes = root.classes();
        if (classes.length != 2)
            throw new Error("1 " + Arrays.asList(classes));
        for (int i=0; i<classes.length; i++) {
            ClassDoc clazz = classes[i];
            if (clazz.fields().length != 1)
                throw new Error("2 " + clazz + " " + Arrays.asList(clazz.fields()));
        }
        return true;
    }
}
