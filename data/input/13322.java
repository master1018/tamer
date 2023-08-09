public class LargeZip {
    static final boolean debug = System.getProperty("debug") != null;
    static final int DATA_LEN = 80 * 1024;
    static final int DATA_SIZE = 8;
    static long fileSize = 6L * 1024L * 1024L * 1024L; 
    static boolean userFile = false;
    static byte[] data;
    static File largeFile;
    static String lastEntryName;
    static void realMain (String[] args) throws Throwable {
        if (args.length > 0) {
            try {
                fileSize = Long.parseLong(args[0]);
                System.out.println("Testing with file of size " + fileSize);
            } catch (NumberFormatException ex) {
                largeFile = new File(args[0]);
                if (!largeFile.exists()) {
                    throw new Exception("Specified file " + args[0] + " does not exist");
                }
                userFile = true;
                System.out.println("Testing with user-provided file " + largeFile);
            }
        }
        File testDir = null;
        if (largeFile == null) {
            testDir = new File(System.getProperty("test.scratch", "."),
                                    "LargeZip");
            if (testDir.exists()) {
                if (!testDir.delete()) {
                    throw new Exception("Cannot delete already-existing test directory");
                }
            }
            check(!testDir.exists() && testDir.mkdirs());
            largeFile = new File(testDir, "largezip.zip");
            createLargeZip();
        }
        readLargeZip1();
        readLargeZip2();
        if (!userFile && !debug) {
            check(largeFile.delete());
            check(testDir.delete());
        }
    }
    static void createLargeZip() throws Throwable {
        int iterations = DATA_LEN / DATA_SIZE;
        ByteBuffer bb = ByteBuffer.allocate(DATA_SIZE);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (int i = 0; i < iterations; i++) {
            bb.putDouble(0, Math.random());
            baos.write(bb.array(), 0, DATA_SIZE);
        }
        data = baos.toByteArray();
        try (FileOutputStream fos = new FileOutputStream(largeFile);
             BufferedOutputStream bos = new BufferedOutputStream(fos);
             ZipOutputStream zos = new ZipOutputStream(bos))
        {
            long length = 0;
            while (length < fileSize) {
                ZipEntry ze = new ZipEntry("entry-" + length);
                lastEntryName = ze.getName();
                zos.putNextEntry(ze);
                zos.write(data, 0, data.length);
                zos.closeEntry();
                length = largeFile.length();
            }
            System.out.println("Last entry written is " + lastEntryName);
        }
    }
    static void readLargeZip1() throws Throwable {
        ZipFile zipFile = new ZipFile(largeFile);
        ZipEntry entry = null;
        String entryName = null;
        int count = 0;
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            entry = entries.nextElement();
            entryName = entry.getName();
            count++;
        }
        System.out.println("Number of entries read: " + count);
        System.out.println("Last entry read is " + entryName);
        check(!entry.isDirectory());
        if (check(entryName.equals(lastEntryName))) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            InputStream is = zipFile.getInputStream(entry);
            byte buf[] = new byte[4096];
            int len;
            while ((len = is.read(buf)) >= 0) {
                baos.write(buf, 0, len);
            }
            baos.close();
            is.close();
            check(Arrays.equals(data, baos.toByteArray()));
        }
    }
    static void readLargeZip2() throws Throwable {
        try (FileInputStream fis = new FileInputStream(largeFile);
             BufferedInputStream bis = new BufferedInputStream(fis);
             ZipInputStream zis = new ZipInputStream(bis))
        {
            ZipEntry entry = null;
            String entryName = null;
            int count = 0;
            while ((entry = zis.getNextEntry()) != null) {
                entryName = entry.getName();
                if (entryName.equals(lastEntryName)) {
                    break;
                }
                count++;
            }
            System.out.println("Number of entries read: " + count);
            System.out.println("Last entry read is " + entryName);
            check(!entry.isDirectory());
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte buf[] = new byte[4096];
            int len;
            while ((len = zis.read(buf)) >= 0) {
                baos.write(buf, 0, len);
            }
            baos.close();
            check(Arrays.equals(data, baos.toByteArray()));
            check(zis.getNextEntry() == null);
        }
    }
    static volatile int passed = 0, failed = 0;
    static void pass() {passed++;}
    static void pass(String msg) {System.out.println(msg); passed++;}
    static void fail() {failed++; Thread.dumpStack();}
    static void fail(String msg) {System.out.println(msg); fail();}
    static void unexpected(Throwable t) {failed++; t.printStackTrace();}
    static void unexpected(Throwable t, String msg) {
        System.out.println(msg); failed++; t.printStackTrace();}
    static boolean check(boolean cond) {if (cond) pass(); else fail(); return cond;}
    static void equal(Object x, Object y) {
        if (x == null ? y == null : x.equals(y)) pass();
        else fail(x + " not equal to " + y);}
    public static void main(String[] args) throws Throwable {
        try {realMain(args);} catch (Throwable t) {unexpected(t);}
        System.out.println("\nPassed = " + passed + " failed = " + failed);
        if (failed > 0) throw new AssertionError("Some tests failed");}
}
