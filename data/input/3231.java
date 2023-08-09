public class Chew {
    public static void main(String[] args) {
        for (int i = 0; i < 64; i++)
            ByteBuffer.allocateDirect(1 << 20);
    }
}
