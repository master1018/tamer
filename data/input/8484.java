public class ManyZipFiles {
    static final int numFiles = 3000;
    public static void realMain(String[] args) throws Throwable {
        String osName = System.getProperty("os.name");
        if (osName.startsWith("Linux") || osName.startsWith("SunOS")) {
            return;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            ZipEntry ze = new ZipEntry("test");
            zos.putNextEntry(ze);
            byte[] hello = "hello, world".getBytes("ASCII");
            zos.write(hello, 0, hello.length);
            zos.closeEntry();
            zos.finish();
        }
        byte[] data = baos.toByteArray();
        ZipFile zips[] = new ZipFile[numFiles];
        try {
            File tmpdir = new File(
                System.getProperty("java.io.tmpdir")
                + File.separator + "ManyZipFiles");
            if (tmpdir.exists() && !tmpdir.isDirectory()) {
                fail(tmpdir.getAbsolutePath()
                     + " already exists but is not a directory");
                return;
            }
            if (!tmpdir.exists()) {
                if (!tmpdir.mkdirs()) {
                    fail("Couldn't create directory "
                         + tmpdir.getAbsolutePath() + " for test files");
                    return;
                }
            } else if (!tmpdir.canWrite()) {
                fail("Don't have write access for directory "
                     + tmpdir.getAbsolutePath() + " for test files");
                return;
            }
            tmpdir.deleteOnExit();
            for (int i = 0; i < numFiles; i++) {
                File f = File.createTempFile("test", ".zip", tmpdir);
                f.deleteOnExit();
                try (FileOutputStream fos = new FileOutputStream(f)) {
                    fos.write(data, 0, data.length);
                }
                try {
                    zips[i] = new ZipFile(f);
                } catch (Throwable t) {
                    fail("Failed to open zip file #" + i + " named "
                         + zips[i].getName());
                    throw t;
                }
            }
        } finally {
            for (int i = 0; i < numFiles; i++) {
                if (zips[i] != null) {
                    try {
                        zips[i].close();
                    } catch (Throwable t) {
                        fail("At zip[" + i + "] named " + zips[i].getName()
                             + " caught " + t);
                    }
                }
            }
        }
        pass();
    }
    static volatile int passed = 0, failed = 0;
    static void pass() {passed++;}
    static void fail() {failed++; Thread.dumpStack();}
    static void fail(String msg) {System.out.println(msg); fail();}
    static void unexpected(Throwable t) {failed++; t.printStackTrace();}
    static void check(boolean cond) {if (cond) pass(); else fail();}
    static void equal(Object x, Object y) {
        if (x == null ? y == null : x.equals(y)) pass();
        else fail(x + " not equal to " + y);}
    public static void main(String[] args) throws Throwable {
        try {realMain(args);} catch (Throwable t) {unexpected(t);}
        System.out.println("\nPassed = " + passed + " failed = " + failed);
        if (failed > 0) throw new AssertionError("Some tests failed");}
}
