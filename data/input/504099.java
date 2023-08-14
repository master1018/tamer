public class GZIPStreamTest extends TestCase {
    @MediumTest
    public void testGZIPStream() throws Exception {
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        createGZIP(bytesOut);
        byte[] zipData;
        zipData = bytesOut.toByteArray();
        ByteArrayInputStream bytesIn = new ByteArrayInputStream(zipData);
        scanGZIP(bytesIn);
    }
    static byte[] makeSampleFile(int stepStep) throws IOException {
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
    static void createGZIP(ByteArrayOutputStream bytesOut) throws IOException {
        GZIPOutputStream out = new GZIPOutputStream(bytesOut);
        try {
            byte[] input = makeSampleFile(1);
            out.write(input, 0, input.length);
        } finally {
            out.close();
        }
    }
    static void scanGZIP(ByteArrayInputStream bytesIn) throws IOException {
        GZIPInputStream in = new GZIPInputStream(bytesIn);
        try {
            ByteArrayOutputStream contents = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            int len, totalLen = 0;
            while ((len = in.read(buf)) > 0) {
                contents.write(buf, 0, len);
                totalLen += len;
            }
            assertEquals(totalLen, 128 * 1024);
        } finally {
            in.close();
        }
    }
}
