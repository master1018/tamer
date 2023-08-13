public class AllocateDirectInit {
    public static void main(String [] args){
        for (int i = 0; i < 1024; i++) {
            ByteBuffer bb = ByteBuffer.allocateDirect(1024);
            for (bb.position(0); bb.position() < bb.limit(); ) {
                if ((bb.get() & 0xff) != 0)
                    throw new RuntimeException("uninitialized buffer, position = "
                                               + bb.position());
            }
        }
    }
    private static void printByteBuffer(ByteBuffer bb) {
        System.out.print("byte [");
        for (bb.position(0); bb.position() < bb.limit(); )
            System.out.print(" " + Integer.toHexString(bb.get() & 0xff));
        System.out.println(" ]");
    }
}
