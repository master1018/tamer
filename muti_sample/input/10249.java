public class BufferOverflowTest {
    public static void main(String[] args) throws Exception {
        try {
            UnsyncByteArrayOutputStream out = new UnsyncByteArrayOutputStream();
            out.write(new byte[(8192) << 2 + 1]);
            System.out.println("PASSED");
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println("FAILED, got ArrayIndexOutOfBoundsException");
            throw new Exception(e);
        }
    }
}
