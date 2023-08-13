public class FlagsTooEarly extends Doclet {
    public static void main(String[] args) {
        String thisFile = "" +
            new java.io.File(System.getProperty("test.src", "."),
                             "FlagsTooEarly.java");
        if (com.sun.tools.javadoc.Main.execute(
                "javadoc",
                "FlagsTooEarly",
                FlagsTooEarly.class.getClassLoader(),
                new String[] {"-Xwerror", thisFile}) != 0)
            throw new Error("Javadoc encountered warnings or errors.");
    }
    public static boolean start(RootDoc root) {
        return true;
    }
    C2 c;
    static class C1 { }
    static class C2 { }
}
