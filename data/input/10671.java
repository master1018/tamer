public class EscapePath {
    private static String testPath;
    static {
        testPath = System.getProperty("test.src");
        if (testPath == null)
            testPath = "";
        else
            testPath = testPath + File.separator;
    }
    public static void main(String[] args) throws Exception {
        createTestDir();
        copyClassFile();
        invokeJava();
        eraseTestDir();
    }
    private static void createTestDir() throws Exception {
        File testDir = new File("a#b");
        boolean result = testDir.mkdir();
    }
    private static void eraseTestDir() throws Exception {
        File classFile = new File("a#b/Hello.class");
        classFile.delete();
        File testDir = new File("a#b");
        testDir.delete();
    }
    private static void copyClassFile() throws Exception {
        FileInputStream fis = new FileInputStream(testPath + "Hello.class");
        FileOutputStream fos = new FileOutputStream("a#b/Hello.class");
        int bytesRead;
        byte buf[] = new byte[410];
        do {
            bytesRead = fis.read(buf);
            if (bytesRead > 0)
                fos.write(buf, 0, bytesRead);
        } while (bytesRead != -1);
        fis.close();
        fos.flush();
        fos.close();
    }
    private static void invokeJava() throws Exception {
        String command = System.getProperty("java.home") +
                         File.separator + "bin" + File.separator +
                         "java -classpath " + "a#b/ Hello";
        Process p = Runtime.getRuntime().exec(command);
        p.waitFor();
        int result = p.exitValue();
        if (result != 0)
            throw new RuntimeException("Path encoding failure.");
    }
}
