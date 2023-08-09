public class StoredCRC {
    public static void realMain(String[] args) throws Throwable {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos);
        ZipEntry ze = new ZipEntry("test");
        ze.setMethod(ZipOutputStream.STORED);
        String writtenString = "hello, world";
        byte[] writtenData = writtenString.getBytes("ASCII");
        ze.setSize(writtenData.length);
        CRC32 crc = new CRC32();
        crc.update(writtenData);
        ze.setCrc(crc.getValue());
        zos.putNextEntry(ze);
        zos.write(writtenData, 0, writtenData.length);
        zos.close();
        byte[] data = baos.toByteArray();
        if (args.length > 0) {
            FileOutputStream fos = new FileOutputStream("stored.zip");
            fos.write(data, 0, data.length);
            fos.close();
        } else {
            ZipInputStream zis = new ZipInputStream(
                new ByteArrayInputStream(data));
            ze = zis.getNextEntry();
            int pos = 0;
            byte[] readData = new byte[256];
            try {
                int count = zis.read(readData, pos, 1);
                while (count > 0) {
                    count = zis.read(readData, ++pos, 1);
                }
                check(writtenString.equals(new String(readData, 0, pos, "ASCII")));
            } catch (Throwable t) {
                unexpected(t);
            }
            data[39] ^= 1;
            zis = new ZipInputStream(
                new ByteArrayInputStream(data));
            ze = zis.getNextEntry();
            try {
                zis.read(readData, 0, readData.length);
                fail("Did not catch expected ZipException" );
            } catch (ZipException ex) {
                String msg = ex.getMessage();
                check(msg != null && msg.startsWith("invalid entry CRC (expected 0x"));
            } catch (Throwable t) {
                unexpected(t);
            }
        }
    }
    static volatile int passed = 0, failed = 0;
    static boolean pass() {passed++; return true;}
    static boolean fail() {failed++; Thread.dumpStack(); return false;}
    static boolean fail(String msg) {System.out.println(msg); return fail();}
    static void unexpected(Throwable t) {failed++; t.printStackTrace();}
    static boolean check(boolean cond) {if (cond) pass(); else fail(); return cond;}
    static boolean equal(Object x, Object y) {
        if (x == null ? y == null : x.equals(y)) return pass();
        else return fail(x + " not equal to " + y);}
    public static void main(String[] args) throws Throwable {
        try {realMain(args);} catch (Throwable t) {unexpected(t);}
        System.out.println("\nPassed = " + passed + " failed = " + failed);
        if (failed > 0) throw new AssertionError("Some tests failed");}
}
