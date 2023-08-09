public class GetLastModified {
    static boolean testFailed = false;
    static void test(String s) throws Exception {
        URL url = new URL(s);
        URLConnection conn = url.openConnection();
        if (conn.getLastModified() == 0) {
            System.out.println("Failed: getLastModified returned 0 for URL: " + s);
            testFailed = true;
        }
    }
    public static void main(String args[]) throws Exception {
        File file = new File(System.getProperty("test.src", "."), "jars");
        String fileURL = "file:" + file.getCanonicalPath() + file.separator +
                         "test.jar";
        test(fileURL);
        String jarURL = "jar:" + fileURL + "!/";
        test(jarURL);
        if (testFailed) {
            throw new Exception("Test failed - getLastModified returned 0");
        }
    }
}
