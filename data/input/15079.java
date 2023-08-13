public class Test6863155 {
    private static long test(byte b) {
        return b << 24 & 0xff000000L;
    }
    public static void main(String... args) {
        long result = test((byte) 0xc2);
        long expected = 0x00000000c2000000L;
        if (result != expected)
            throw new InternalError(Long.toHexString(result) + " != " + Long.toHexString(expected));
    }
}
