class DirectByteBuffers {
    public static void free(ByteBuffer directBuffer) {
        if ((directBuffer == null) || (!directBuffer.isDirect())) {
            throw new IllegalArgumentException();
        }
        DirectByteBuffer buf = (DirectByteBuffer) directBuffer;
        buf.free();
    }
    public static PlatformAddress getEffectiveAddress(ByteBuffer directBuffer) {
        return toDirectBuffer(directBuffer).getEffectiveAddress();
    }
    private static DirectByteBuffer toDirectBuffer(ByteBuffer directBuffer) {
        if ((directBuffer == null) || (!directBuffer.isDirect())) {
            throw new IllegalArgumentException();
        }
        return (DirectByteBuffer) directBuffer;
    }
}
