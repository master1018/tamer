public class MethodLinks extends Doclet
{
    public static void main(String[] args) {
        if (com.sun.tools.javadoc.Main.
            execute("javadoc", "MethodLinks", MethodLinks.class.getClassLoader(),
                    new String[] {System.getProperty("test.src", ".") +
                                  java.io.File.separatorChar + "MethodLinks.java"}
                    ) != 0)
            throw new Error();
    }
    public MethodLinks SAMPLE(MethodLinks x) {
        return x;
    }
    public static boolean start(com.sun.javadoc.RootDoc root) {
        ClassDoc[] classes = root.classes();
        if (classes.length != 1)
            throw new Error("1 " + Arrays.asList(classes));
        ClassDoc self = classes[0];
        MethodDoc[] allMethods = self.methods();
        MethodDoc SAMPLE = null;
        for (int i=0; i<allMethods.length; i++)
            if (allMethods[i].name().equals("SAMPLE"))
                SAMPLE = allMethods[i];
        return
            self == SAMPLE.parameters()[0].type().asClassDoc()
            &&
            self == SAMPLE.returnType().asClassDoc()
            ;
    }
}
