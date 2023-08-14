public class Clone {
    public static void main(String argv[]) throws Exception {
        ZipFile zf = new ZipFile(new File(System.getProperty("test.src"),
                                          "input.jar"));
        ZipEntry e = new ZipEntry("foo");
        e.clone();
    }
}
