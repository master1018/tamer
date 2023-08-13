public class SubpackageIgnore extends Doclet {
    public static void main(String[] args) {
        if (com.sun.tools.javadoc.Main.execute(
                "javadoc",
                "SubpackageIgnore",
                SubpackageIgnore.class.getClassLoader(),
                new String[] {"-Xwerror",
                              "-sourcepath",
                              System.getProperty("test.src", "."),
                              "-subpackages",
                              "pkg1"}) != 0)
            throw new Error("Javadoc encountered warnings or errors.");
    }
    public static boolean start(RootDoc root) {
        return true;
    }
}
