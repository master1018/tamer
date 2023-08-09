public class BadSuper {
    public static void main(String[] args) {
        String srcpath = System.getProperty("test.src", ".");
        if (com.sun.tools.javadoc.Main.execute(
                new String[] {"-d", "doc", "-sourcepath", srcpath, "p"}) != 0)
            throw new Error("Javadoc encountered warnings or errors.");
    }
}
