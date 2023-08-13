public class DataDescriptor {
    static void copyZip(ZipInputStream in, ZipOutputStream out) throws IOException {
        byte[] buffer = new byte[1 << 14];
        for (ZipEntry ze; (ze = in.getNextEntry()) != null; ) {
            out.putNextEntry(ze);
            for (int nr; 0 < (nr = in.read(buffer)); ) {
                out.write(buffer, 0, nr);
            }
        }
        in.close();
    }
    private static void realMain(String[] args) throws Throwable {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos);
        ZipEntry e = new ZipEntry("testdir/foo");
        byte[] data = "entry data".getBytes("ASCII");
        zos.putNextEntry(e);
        zos.write(data);
        zos.close();
        byte[] zipbytes1 = baos.toByteArray();
        int length1 = zipbytes1.length;
        System.out.println("zip bytes pre-copy length=" + length1);
        ZipInputStream zis =
            new ZipInputStream(
                new ByteArrayInputStream(zipbytes1));
        baos.reset();
        zos = new ZipOutputStream(baos);
        copyZip(zis, zos);
        zos.close();
        byte[] zipbytes2 = baos.toByteArray();
        int length2 = zipbytes2.length;
        System.out.println("zip bytes post-copy length=" + length2);
        equal(length1, length2);
        check(Arrays.equals(zipbytes1, zipbytes2));
        baos.reset();
        zos = new ZipOutputStream(baos);
        copyZip(new ZipInputStream(new ByteArrayInputStream(zipbytes2)), zos);
        zos.close();
        byte[] zipbytes3 = baos.toByteArray();
        int length3 = zipbytes3.length;
        System.out.println("zip bytes post-copy length=" + length3);
        equal(length1, length3);
        check(Arrays.equals(zipbytes1, zipbytes3));
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
        System.out.printf("%nPassed = %d, failed = %d%n%n", passed, failed);
        if (failed > 0) throw new AssertionError("Some tests failed");}
}
