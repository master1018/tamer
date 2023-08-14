public class ChecksumTest extends TestCase {
    @SmallTest
    public void testChecksum() throws Exception {
        adler32Test(mTestString, 0x9de210dbL);
        cRC32Test(mTestString, 0x939f04afL);
        wrongChecksumWithAdler32Test();
    }
    private void adler32Test(byte[] values, long expected) {
        Adler32 adler = new Adler32();
        adler.update(values);
        assertEquals(adler.getValue(), expected);
        adler.reset();
        for (int i = 0; i < values.length; i++) {
            adler.update(values[i]);
        }
        assertEquals(adler.getValue(), expected);
    }
    private void cRC32Test(byte[] values, long expected) {
        CRC32 crc = new CRC32();
        crc.update(values);
        assertEquals(crc.getValue(), expected);
        crc.reset();
        for (int i = 0; i < values.length; i++) {
            crc.update(values[i]);
        }
        assertEquals(crc.getValue(), expected);
    }
    private static byte[] mTestString = {
            0x54, 0x68, 0x65, 0x20, 0x71, 0x75, 0x69, 0x63,
            0x6b, 0x20, 0x62, 0x72, 0x6f, 0x77, 0x6e, 0x20,
            0x66, 0x6f, 0x78, 0x20, 0x6a, 0x75, 0x6d, 0x70,
            0x65, 0x64, 0x20, 0x6f, 0x76, 0x65, 0x72, 0x20,
            0x74, 0x68, 0x65, 0x20, 0x6c, 0x61, 0x7a, 0x79,
            0x20, 0x64, 0x6f, 0x67, 0x73, 0x2e, 0x0a
    };
    private void wrongChecksumWithAdler32Test() {
        byte[] bytes = {1, 0, 5, 0, 15, 0, 1, 11, 0, 1};
        Adler32 adler = new Adler32();
        adler.update(bytes);
        long arrayChecksum = adler.getValue();
        adler.reset();
        for (int i = 0; i < bytes.length; i++) {
            adler.update(bytes[i]);
        }
        assertEquals("Checksums not equal: expected: " + arrayChecksum +
                " actual: " + adler.getValue(), arrayChecksum, adler.getValue());
    }
}
