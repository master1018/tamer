public class TestISO2022JPSubBytes {
    static char[][] in = { {'\u25cb', '\u2460', '\u25cb'},
                           {'\u0061', '\u2460', '\u0061'},
                           {'\u25cb', '\u2460', '\u25cb'},
                           {'\u0061', '\u2460', '\u0061'},
                         };
    static byte[][] expected = { {0x1b, 0x24, 0x42, 0x21, 0x7b,
                                  0x21, 0x29,
                                  0x21, 0x7b,
                                  0x1b, 0x28, 0x42},
                                 {0x61,
                                  0x1b, 0x24, 0x42, 0x21, 0x29,
                                  0x1b, 0x28, 0x42, 0x61},
                                 {0x1b, 0x24, 0x42, 0x21, 0x7b,
                                  0x1b, 0x28, 0x42, 0x3f,
                                  0x1b, 0x24, 0x42, 0x21, 0x7b,
                                  0x1b, 0x28, 0x42},
                                 {0x61,
                                  0x3f,
                                  0x61}
                                };
    public static void main(String args[]) throws Exception {
        CharsetEncoder enc = Charset.forName("ISO2022JP")
          .newEncoder()
          .onUnmappableCharacter(CodingErrorAction.REPLACE);
        test(enc, in[0], expected[0]);
        enc.reset();
        test(enc, in[1], expected[1]);
        enc.reset();
        enc.replaceWith(new byte[]{(byte)'?'});
        test(enc, in[2], expected[2]);
        enc.reset();
        test(enc, in[3], expected[3]);
    }
    public static void test (CharsetEncoder enc,
                             char[] inputChars,
                             byte[] expectedBytes) throws Exception
    {
        ByteBuffer bb = ByteBuffer.allocate(expectedBytes.length);
        enc.encode(CharBuffer.wrap(inputChars), bb, true);
        enc.flush(bb);
        bb.flip();
        byte[] outputBuff = bb.array();
        int outputLen = bb.limit();
        if (outputLen != expectedBytes.length) {
            throw new Exception("Output bytes does not match");
        }
        for (int i = 0; i < outputLen; ++i) {
            System.out.printf("<%x:%x> ",
                              expectedBytes[i] & 0xff,
                              outputBuff[i] & 0xff);
            if (expectedBytes[i] != outputBuff[i]) {
                System.out.println("...");
                throw new Exception("Output bytes does not match");
            }
        }
        System.out.println();
    }
}
