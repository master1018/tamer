public class BufferUnderflowEUCTWTest {
    private static int BUFFERSIZE = 8194;
    public static void main (String[] args) throws Exception {
        int i = 0;
        byte[] b = new byte[BUFFERSIZE];
        for (; i < BUFFERSIZE - 4; i++) 
            b[i] = 0;
        b[i++] = (byte)0x8E;
        b[i++] = (byte)0xA2;
        b[i++] = (byte)0xA1;
        b[i++] = (byte)0xA6;
        ByteArrayInputStream r = new ByteArrayInputStream(b);
        try {
            InputStreamReader isr=new InputStreamReader(r, "EUC-TW");
            char[] cc = new char[BUFFERSIZE];
            int cx = isr.read(cc);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new Exception("Array Index error: bug 4834154");
        }
    }
}
