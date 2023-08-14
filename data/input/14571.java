public class TestZipError {
    public static void realMain(String[] args) throws Throwable {
        String osName = System.getProperty("os.name");
        if (!System.getProperty("os.name").startsWith("Windows")) {
            return;
        }
        String fileName = "error4615343.zip";
        File f = new File(fileName);
        f.delete();
        ZipOutputStream zos;
        ZipEntry ze;
        zos = new ZipOutputStream(new FileOutputStream(f));
        ze = new ZipEntry("one");
        zos.putNextEntry(ze);
        zos.write("hello".getBytes());
        zos.closeEntry();
        ze = new ZipEntry("two");
        zos.putNextEntry(ze);
        zos.write("world".getBytes());
        zos.closeEntry();
        zos.close();
        ZipFile zf = new ZipFile(fileName);
        f.delete();
        zos = new ZipOutputStream(new FileOutputStream(f));
        ze = new ZipEntry("uno");
        zos.putNextEntry(ze);
        zos.write("hola".getBytes());
        zos.closeEntry();
        zos.close();
        Enumeration<? extends ZipEntry> entries = zf.entries();
        try {
            while (entries.hasMoreElements()) {
                ze = entries.nextElement();
            }
            fail("Did not get expected exception");
        } catch (ZipError e) {
            pass();
        } catch (InternalError e) {
            fail("Caught InternalError instead of expected ZipError");
        } catch (Throwable t) {
            unexpected(t);
        } finally {
            zf.close();
            f.delete();
        }
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
