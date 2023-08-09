public class BreakIteratorWarning extends Doclet {
    public static void main(String[] args) {
        String thisFile = "" +
            new java.io.File(System.getProperty("test.src", "."),
                             "BreakIteratorWarning.java");
        if (com.sun.tools.javadoc.Main.execute(
                "javadoc",
                "BreakIteratorWarning",
                BreakIteratorWarning.class.getClassLoader(),
                new String[] {"-Xwerror", thisFile}) != 0)
            throw new Error("Javadoc encountered warnings or errors.");
    }
    public static boolean start(RootDoc root) {
        ClassDoc cd = root.classes()[0];
        FieldDoc fd = cd.fields()[0];
        fd.firstSentenceTags();
        return true;
    }
    public String author = "William Goldman";
}
