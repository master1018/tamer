public class TestCp834_SBCS {
    public static void main(String args[]) throws Exception {
        CharToByteConverter cb834 = CharToByteConverter.getConverter("Cp834");
        ByteToCharConverter bc834 = ByteToCharConverter.getConverter("Cp834");
        CharsetEncoder enc834 = Charset.forName("Cp834")
                                       .newEncoder()
                                       .onUnmappableCharacter(CodingErrorAction.REPLACE)
                                       .onMalformedInput(CodingErrorAction.REPLACE);
        CharsetDecoder dec834 = Charset.forName("Cp834")
                                       .newDecoder()
                                       .onUnmappableCharacter(CodingErrorAction.REPLACE)
                                       .onMalformedInput(CodingErrorAction.REPLACE);
        CharsetDecoder dec933 = Charset.forName("Cp933")
                                       .newDecoder()
                                       .onUnmappableCharacter(CodingErrorAction.REPLACE)
                                       .onMalformedInput(CodingErrorAction.REPLACE);
        byte[] ba = new byte[1];
        byte[] ba2 = new byte[2];
        ByteBuffer dbb = ByteBuffer.allocateDirect(10);
        char[] ca = new char[1];
        char c;
        for (int i = 0; i <= 0xff; i++) {
            if (i != 0xe && i != 0xf) {   
                ba[0] = (byte)i;
                CharBuffer cb = dec933.decode(ByteBuffer.wrap(ba));
                if ((c = cb.get()) != '\ufffd') {
                    if (dec834.decode(ByteBuffer.wrap(ba)).get() != '\ufffd')
                        throw new Exception("SBCS is supported in IBM834 decoder");
                    if (enc834.canEncode(c))
                        throw new Exception("SBCS can be encoded in IBM834 encoder");
                    ca[0] = c;
                    ByteBuffer bb = enc834.encode(CharBuffer.wrap(ca));
                    if (bb.get() != (byte)0xfe || bb.get() != (byte)0xfe)
                        throw new Exception("SBCS is supported in IBM834 encoder");
                    boolean isMalformed = false;
                    int ret = 0;
                    bc834.reset();
                    try {
                        ret = bc834.convert(ba, 0, 1, ca, 0, 1);
                    } catch (sun.io.MalformedInputException x) { isMalformed = true; }
                    if (!isMalformed && ret != 0 && ca[0] != '\ufffd') {
                        throw new Exception("SBCS is supported in Cp834 b2c");
                    }
                    if (cb834.canConvert(c))
                        throw new Exception("SBCS can be converted in Cp834 c2b ");
                    ca[0] = c;
                    if (cb834.convert(ca, 0, 1, ba2, 0, 2) != 2 ||
                        ba2[0] != (byte)0xfe || ba2[1] != (byte)0xfe) {
                        throw new Exception("SBCS is supported in Cp834 c2b");
                    }
                }
            }
        }
    }
}
