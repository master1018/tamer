public class DeflateTest extends TestCase {
    @LargeTest
    public void testDeflate() throws Exception {
        simpleTest();
        bigTest(0, 1738149618);
        bigTest(1, 934350518);
        bigTest(2, -532869390);
    }
    private void simpleTest()
            throws UnsupportedEncodingException, DataFormatException {
        String inputString = "blahblahblah??";
        byte[] input = inputString.getBytes("UTF-8");
        byte[] output = new byte[100];
        Deflater compresser = new Deflater();
        compresser.setInput(input);
        compresser.finish();
        int compressedDataLength = compresser.deflate(output);
        Inflater decompresser = new Inflater();
        decompresser.setInput(output, 0, compressedDataLength);
        byte[] result = new byte[100];
        int resultLength = decompresser.inflate(result);
        String outputString = new String(result, 0, resultLength, "UTF-8");
        assertEquals(inputString, outputString);
        assertEquals(compresser.getAdler(), decompresser.getAdler());
        decompresser.end();
    }
    private void bigTest(int step, int expectedAdler)
            throws UnsupportedEncodingException, DataFormatException {
        byte[] input = new byte[128 * 1024];
        byte[] comp = new byte[128 * 1024 + 512];
        byte[] output = new byte[128 * 1024 + 512];
        Inflater inflater = new Inflater(false);
        Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION, false);
        createSample(input, step);
        compress(deflater, input, comp);
        expand(inflater, comp, (int) deflater.getBytesWritten(), output);
        assertEquals(inflater.getBytesWritten(), input.length);
        assertEquals(deflater.getAdler(), inflater.getAdler());
        assertEquals(deflater.getAdler(), expectedAdler);
    }
    private void createSample(byte[] sample, int stepStep) {
        byte val, step;
        int i, j, offset;
        assertTrue(sample.length >= 128 * 1024);
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
    }
    private static final int LOCAL_BUF_SIZE = 256;
    private void compress(Deflater deflater, byte[] inBuf, byte[] outBuf) {
        int inCount = inBuf.length;        
        int inPosn;
        int outPosn;
        inPosn = outPosn = 0;
        while (!deflater.finished()) {
            int want = -1, got;
            if (deflater.needsInput() && inCount != 0) {
                want = (inCount < LOCAL_BUF_SIZE) ? inCount : LOCAL_BUF_SIZE;
                deflater.setInput(inBuf, inPosn, want);
                inCount -= want;
                inPosn += want;
                if (inCount == 0) {
                    deflater.finish();
                }
            }
            int compCount;
            compCount = deflater.deflate(outBuf, outPosn, LOCAL_BUF_SIZE);
            outPosn += compCount;
        }
    }
    private void expand(Inflater inflater, byte[] inBuf, int inCount,
            byte[] outBuf) throws DataFormatException {
        int inPosn;
        int outPosn;
        inPosn = outPosn = 0;
        while (!inflater.finished()) {
            int want = -1, got;
            if (inflater.needsInput() && inCount != 0) {
                want = (inCount < LOCAL_BUF_SIZE) ? inCount : LOCAL_BUF_SIZE;
                inflater.setInput(inBuf, inPosn, want);
                inCount -= want;
                inPosn += want;
            }
            int compCount;
            compCount = inflater.inflate(outBuf, outPosn, LOCAL_BUF_SIZE);
            outPosn += compCount;
        }
    }
}
