abstract class DBCS_IBM_ASCII_Decoder extends CharsetDecoder
{
    protected static final char REPLACE_CHAR='\uFFFD';
    protected String  singleByteToChar;
    protected boolean leadByte[];
    protected short   index1[];
    protected String  index2;
    protected int     mask1;
    protected int     mask2;
    protected int     shift;
    protected DBCS_IBM_ASCII_Decoder(Charset cs) {
        super(cs, 0.5f, 1.0f);
    }
    private CoderResult decodeArrayLoop(ByteBuffer src, CharBuffer dst) {
        byte[] sa = src.array();
        int sp = src.arrayOffset() + src.position();
        int sl = src.arrayOffset() + src.limit();
        assert (sp <= sl);
        sp = (sp <= sl ? sp : sl);
        char[] da = dst.array();
        int dp = dst.arrayOffset() + dst.position();
        int dl = dst.arrayOffset() + dst.limit();
        assert (dp <= dl);
        dp = (dp <= dl ? dp : dl);
        try {
            while (sp < sl) {
                int b1, b2;
                b1 = sa[sp];
                int inputSize = 1;
                int v = 0;
                char outputChar = REPLACE_CHAR;
                if (b1 < 0)
                    b1 += 256;
                if (!leadByte[b1])
                {
                  outputChar = singleByteToChar.charAt(b1);
                } else {
                    if (sl - sp < 2)
                        return CoderResult.UNDERFLOW;
                    b2 = sa[sp + 1];
                    if (b2 < 0)
                        b2 += 256;
                    inputSize++;
                    v = b1 * 256 + b2;
                    outputChar = index2.charAt(index1[((v & mask1) >> shift)]
                                                + (v & mask2));
                }
                if (outputChar == '\uFFFD')
                    return CoderResult.unmappableForLength(inputSize);
                if (dl - dp < 1)
                    return CoderResult.OVERFLOW;
                da[dp++] = outputChar;
                sp += inputSize;
            }
            return CoderResult.UNDERFLOW;
        } finally {
            src.position(sp - src.arrayOffset());
            dst.position(dp - dst.arrayOffset());
        }
    }
    private CoderResult decodeBufferLoop(ByteBuffer src, CharBuffer dst) {
        int mark = src.position();
        try {
            while (src.hasRemaining()) {
                char outputChar = REPLACE_CHAR;
                int inputSize = 1;
                int b1, b2;
                int v = 0;
                b1 = src.get();
                if (b1 < 0)
                    b1 += 256;
                if (!leadByte[b1])
                {
                  outputChar = singleByteToChar.charAt(b1);
                } else {
                    if (src.remaining() < 1)
                        return CoderResult.UNDERFLOW;
                    b2 = src.get();
                    if (b2 < 0)
                        b2 += 256;
                    inputSize++;
                    v = b1 * 256 + b2;
                    outputChar = index2.charAt(index1[((v & mask1) >> shift)]
                                                        + (v & mask2));
                }
                if (outputChar == REPLACE_CHAR)
                    return CoderResult.unmappableForLength(inputSize);
                if (!dst.hasRemaining())
                    return CoderResult.OVERFLOW;
                mark += inputSize;
                dst.put(outputChar);
            }
            return CoderResult.UNDERFLOW;
        } finally {
            src.position(mark);
        }
    }
    protected CoderResult decodeLoop(ByteBuffer src, CharBuffer dst) {
        if (src.hasArray() && dst.hasArray())
            return decodeArrayLoop(src, dst);
        else
            return decodeBufferLoop(src, dst);
    }
}
