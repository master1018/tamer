public class EmptyInput {
    public static void main(String[] args) throws Exception {
        ByteBuffer bb = ByteBuffer.allocate(10);
        bb.flip();
        CharsetDecoder cd = Charset.forName("US-ASCII").newDecoder();
        try {
            cd.decode(bb, CharBuffer.allocate(10), true).throwException();
        } catch (BufferUnderflowException x) {
            System.err.println("BufferUnderflowException thrown as expected");
            return;
        }
        throw new Exception("BufferUnderflowException not thrown");
    }
}
