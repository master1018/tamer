public class EUCJPUnderflowDecodeTest {
    public static void main(String[] args) throws Exception{
        ByteBuffer bb = ByteBuffer.allocateDirect(255);
        CharBuffer cc = CharBuffer.allocate(255);
        String[] charsetNames = { "EUC_JP", "EUC-JP-LINUX" };
        for (int i = 0 ; i < charsetNames.length; i++) {
            Charset cs = Charset.forName(charsetNames[i]);
            CharsetDecoder decoder = cs.newDecoder();
            bb.clear();
            cc.clear();
            bb.put((byte)0x8f);
            bb.put((byte)0xa2);
            bb.flip();
            CoderResult result = decoder.decode(bb, cc, false);
            if (result != CoderResult.UNDERFLOW) {
                throw new Exception("test failed - UNDERFLOW not returned");
            }
            decoder.reset();
            bb.clear();
            cc.clear();
            bb.put((byte)0xa1);
            bb.flip();
            result = decoder.decode(bb, cc, false);
            if (result != CoderResult.UNDERFLOW) {
                throw new Exception("test failed");
            }
            decoder.reset();
            bb.clear();
            cc.clear();
            bb.put((byte)0xa1);
            bb.put((byte)0xc0);
            bb.flip();
            result = decoder.decode(bb, cc, false);
            cc.flip();
            if (result != CoderResult.UNDERFLOW && cc.get() != '\uFF3c') {
                throw new Exception("test failed to decode EUC-JP (0xA1C0)");
            }
        }
    }
}
