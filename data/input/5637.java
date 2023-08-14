public class GetBytes {
    public static void main(String args[]) throws Exception {
        ByteBuffer bb = ByteBuffer.wrap(new byte [26 + 2]);
        for (int i = 'a'; i < 'a' + bb.capacity(); i++) {
            bb.put((byte)i);
        }
        bb.position(1);
        bb.limit(bb.capacity() - 1);
        ByteBuffer src = bb.slice();
        CharacterEncoder e = new BASE64Encoder();
        CharacterDecoder d = new BASE64Decoder();
        String encoded = e.encodeBuffer(src);
        ByteBuffer dst = d.decodeBufferToByteBuffer(encoded);
        src.rewind();
        dst.rewind();
        if (src.compareTo(dst) != 0) {
            throw new Exception("Didn't encode/decode correctly");
        }
    }
}
