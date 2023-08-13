public class ByteSwap {
    public static void main(String args[]) {
        if (Short.reverseBytes((short)0xaabb) != (short)0xbbaa)
            throw new RuntimeException("short");
        if (Character.reverseBytes((char)0xaabb) != (char)0xbbaa)
            throw new RuntimeException("char");
    }
}
