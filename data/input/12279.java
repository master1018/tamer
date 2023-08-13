public class Test6392804 {
    public static void main(String[] args) throws Throwable {
        test("ISO-2022-JP",
             new byte[] {0x1b,(byte)0x8e, 0x24, 0x40, 0x0, 0x0});
        test("ISO-2022-JP-2",
             new byte[] {0x1b,(byte)0x8e, 0x24, 0x40, 0x0, 0x0});
        test("x-windows-50220",
             new byte[] {0x1b,(byte)0x8e, 0x24, 0x40, 0x0, 0x0});
        test("x-windows-50221",
             new byte[] {0x1b,(byte)0x8e, 0x24, 0x40, 0x0, 0x0});
        test("x-windows-iso2022jp",
             new byte[] {0x1b,(byte)0x8e, 0x24, 0x40, 0x0, 0x0});
        test("EUC_TW",
             new byte[] {(byte)0x8e, (byte)0xa8, (byte)0xad, (byte)0xe5});
        test("EUC_TW",
             new byte[] {(byte)0x8e, (byte)0x92, (byte)0xa1, (byte)0xa1});
        test("EUC_TW",
             new byte[] {(byte)0x8e, (byte)0x98, (byte)0xa1, (byte)0xa1});
    }
    static void test(String csn, byte[] bytes) throws Throwable {
        CharsetDecoder dec = Charset.forName(csn).newDecoder();
        CharBuffer cb = CharBuffer.allocate(1024);
        CoderResult cr = dec.decode(ByteBuffer.wrap(bytes), cb, true);
        if (cr.isUnderflow())
            throw new RuntimeException(csn + " failed cr=" + cr);
    }
}
