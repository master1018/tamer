public class ZipStreamTest extends TestCase {
    @LargeTest
    public void testZipStream() throws Exception {
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        createCompressedZip(bytesOut);
        byte[] zipData = bytesOut.toByteArray();
        ByteArrayInputStream bytesIn = new ByteArrayInputStream(zipData);
        scanZip(bytesIn);
        bytesOut = new ByteArrayOutputStream();
        createUncompressedZip(bytesOut);
        zipData = bytesOut.toByteArray();
        bytesIn = new ByteArrayInputStream(zipData);
        scanZip(bytesIn);
    }
    private static byte[] makeSampleFile(int stepStep) throws IOException {
        byte[] sample = new byte[128 * 1024];
        byte val, step;
        int i, j, offset;
        val = 0;
        step = 1;
        offset = 0;
        for (i = 0; i < (128 * 1024) / 256; i++) {
            for (j = 0; j < 256; j++) {
                sample[offset++] = val;
                val += step;
            }
            step += stepStep;
        }
        return sample;
    }
    private static void createCompressedZip(ByteArrayOutputStream bytesOut) throws IOException {
        ZipOutputStream out = new ZipOutputStream(bytesOut);
        try {
            int i;
            for (i = 0; i < 3; i++) {
                byte[] input = makeSampleFile(i);
                ZipEntry newEntry = new ZipEntry("file-" + i);
                if (i != 1)
                    newEntry.setComment("this is file " + i);
                out.putNextEntry(newEntry);
                out.write(input, 0, input.length);
                out.closeEntry();
            }
            out.setComment("This is a lovely compressed archive!");
        } finally {
            out.close();
        }
    }
    private static void createUncompressedZip(ByteArrayOutputStream bytesOut) throws IOException {
        ZipOutputStream out = new ZipOutputStream(bytesOut);
        try {
            long[] crcs = {0x205fbff3, 0x906fae57L, 0x2c235131};
            int i;
            for (i = 0; i < 3; i++) {
                byte[] input = makeSampleFile(i);
                ZipEntry newEntry = new ZipEntry("file-" + i);
                if (i != 1)
                    newEntry.setComment("this is file " + i);
                newEntry.setMethod(ZipEntry.STORED);
                newEntry.setSize(128 * 1024);
                newEntry.setCrc(crcs[i]);
                out.putNextEntry(newEntry);
                out.write(input, 0, input.length);
                out.closeEntry();
            }
            out.setComment("This is a lovely, but uncompressed, archive!");
        } finally {
            out.close();
        }
    }
    private static void scanZip(ByteArrayInputStream bytesIn) throws IOException {
        ZipInputStream in = new ZipInputStream(bytesIn);
        try {
            int i;
            for (i = 0; i < 3; i++) {
                ZipEntry entry = in.getNextEntry();
                ByteArrayOutputStream contents = new ByteArrayOutputStream();
                byte[] buf = new byte[4096];
                int len, totalLen = 0;
                while ((len = in.read(buf)) > 0) {
                    contents.write(buf, 0, len);
                    totalLen += len;
                }
                assertEquals(128 * 1024, totalLen);
            }
            assertNull("should only be three entries", in.getNextEntry());
        } finally {
            in.close();
        }
    }
}
