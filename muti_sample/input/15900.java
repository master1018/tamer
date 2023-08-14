public class ZeroedByteArrayEUCTWTest
{
    public static void main(String[] args) throws Exception {
        test("cns11643");
    }
    public static void test(String encoding) throws Exception {
        String result = null;
        byte[] data = new byte[16];
        for (int i = 0; i < 16; i++) {
            data[i] = 0;
        }
        result = new String(data, encoding);
        if (result.length() != 16)
            throw new Exception ("EUC_TW regression test bugID 4522270 failed");
        for (int i=0; i < 16; i++) {
            data[i] = (byte)( 32 + i);
        }
    }
}
