public class MissingImport extends Doclet {
    public static void main(String[] args) {
        String thisFile = "" +
            new java.io.File(System.getProperty("test.src", "."),
                             "I.java");
        if (com.sun.tools.javadoc.Main.execute(
                "javadoc",
                "MissingImport",
                MissingImport.class.getClassLoader(),
                new String[] {thisFile}) != 0)
            throw new Error("Javadoc encountered warnings or errors.");
    }
    public static boolean start(RootDoc root) {
        ClassDoc c = root.classNamed("I");
        ClassDoc[] imps = c.importedClasses();
        if (imps.length == 0 ||
            !imps[0].qualifiedName().equals("bo.o.o.o.Gus")) {
            throw new Error("Import bo.o.o.o.Gus not found");
        }
        return true;
    }
}
