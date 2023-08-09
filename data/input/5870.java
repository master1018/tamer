public class FailingConstructors {
    static final String fileName = "FailingConstructorsTest";
    static final String UNSUPPORTED_CHARSET = "unknownCharset";
    static final String FILE_CONTENTS = "This is a small file!";
    private static void realMain(String[] args) throws Throwable {
        test(false, new File(fileName));
        File file = File.createTempFile(fileName, null);
        file.deleteOnExit();
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(FILE_CONTENTS.getBytes());
        fos.close();
        test(true, file);
        file.delete();
    }
    private static void test(boolean exists, File file) throws Throwable {
        try {
            new PrintWriter(file, UNSUPPORTED_CHARSET);
            fail();
        } catch(FileNotFoundException|UnsupportedEncodingException e) {
            pass();
        }
        check(exists, file);
        try {
            new PrintWriter(file, null);
            fail();
        } catch(FileNotFoundException|NullPointerException e) {
            pass();
        }
        check(exists, file);
        try {
            new PrintWriter(file.getName(), UNSUPPORTED_CHARSET);
            fail();
        } catch(FileNotFoundException|UnsupportedEncodingException e) {
            pass();
        }
        check(exists, file);
        try {
            new PrintWriter(file.getName(), null);
            fail();
        } catch(FileNotFoundException|NullPointerException e) {
            pass();
        }
        check(exists, file);
    }
    private static void check(boolean exists, File file) {
        if (exists) {
            verifyContents(file);
        } else {
            if (file.exists()) { fail(file + " should not have been created"); }
        }
    }
    private static void verifyContents(File file) {
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] contents = FILE_CONTENTS.getBytes();
            int read, count = 0;
            while ((read = fis.read()) != -1) {
                if (read != contents[count++])  {
                    fail("file contents have been altered");
                    return;
                }
            }
        } catch (IOException ioe) {
            unexpected(ioe);
        }
    }
    static volatile int passed = 0, failed = 0;
    static void pass() {passed++;}
    static void fail() {failed++; Thread.dumpStack();}
    static void fail(String message) {System.out.println(message); fail(); }
    static void unexpected(Throwable t) {failed++; t.printStackTrace();}
    public static void main(String[] args) throws Throwable {
        try {realMain(args);} catch (Throwable t) {unexpected(t);}
        System.out.printf("%nPassed = %d, failed = %d%n%n", passed, failed);
        if (failed > 0) throw new AssertionError("Some tests failed");}
}
