public class ZipFileTest extends TestCase {
    private static final int SAMPLE_SIZE = 128 * 1024;
    @MediumTest
    public void testZipFile() throws Exception {
        File file = File.createTempFile("ZipFileTest", ".zip");
        try {
            FileOutputStream outStream = new FileOutputStream(file);
            createCompressedZip(outStream);
            scanZip(file.getPath());
            read2(file.getPath());
        } finally {
            file.delete();
        }
    }
    static byte[] makeSampleFile(int stepStep) throws IOException {
        byte[] sample = new byte[SAMPLE_SIZE];
        byte val, step;
        int i, j, offset;
        val = 0;
        step = 1;
        offset = 0;
        for (i = 0; i < SAMPLE_SIZE / 256; i++) {
            for (j = 0; j < 256; j++) {
                sample[offset++] = val;
                val += step;
            }
            step += stepStep;
        }
        return sample;
    }
    static void createCompressedZip(OutputStream bytesOut) throws IOException {
        ZipOutputStream out = new ZipOutputStream(bytesOut);
        try {
            int i;
            for (i = 0; i < 3; i++) {
                byte[] input = makeSampleFile(i);
                ZipEntry newEntry = new ZipEntry("file-" + i);
                if (i != 1) {
                    newEntry.setComment("this is file " + i);
                }
                out.putNextEntry(newEntry);
                out.write(input, 0, input.length);
                out.closeEntry();
            }
            out.setComment("This is a lovely compressed archive!");
        } finally {
            out.close();
        }
    }
    static void scanZip(String fileName) throws IOException {
        ZipFile zipFile = new ZipFile(fileName);
        Enumeration fileList;
        int idx = 0;
        for (fileList = zipFile.entries(); fileList.hasMoreElements();) {
            ZipEntry entry = (ZipEntry) fileList.nextElement();
            assertEquals(entry.getName(), "file-" + idx);
            idx++;
        }
        zipFile.close();
    }
    static void read2(String fileName) throws IOException {
        ZipFile zipFile;
        ZipEntry entry1, entry2;
        byte buf[] = new byte[16384];
        InputStream stream1, stream2;
        int len, totalLen1, totalLen2;
        zipFile = new ZipFile(fileName);
        entry1 = zipFile.getEntry("file-1");
        entry2 = zipFile.getEntry("file-2");
        assertEquals("file-1", entry1.getName());
        assertEquals("file-2", entry2.getName());
        stream1 = zipFile.getInputStream(entry1);
        stream2 = zipFile.getInputStream(entry2);
        totalLen1 = stream1.read(buf);
        assertTrue("initial read failed on #1", totalLen1 >= 0);
        totalLen2 = stream2.read(buf);
        assertTrue("initial read failed on #2", totalLen2 >= 0);
        while ((len = stream1.read(buf)) > 0) {
            totalLen1 += len;
        }
        assertEquals(SAMPLE_SIZE, totalLen1);
        stream1.close();
        while ((len = stream2.read(buf)) > 0) {
            totalLen2 += len;
        }
        assertEquals(SAMPLE_SIZE, totalLen2);
        stream2.close();
        stream1 = zipFile.getInputStream(zipFile.getEntry("file-0"));
        zipFile.close();
        Exception error = null;
        try {
            stream1.read(buf);
        } catch (Exception ex) {
            error = ex;
        }
        assertNotNull("ZipFile shouldn't allow reading of closed files.", error);
    }
}
