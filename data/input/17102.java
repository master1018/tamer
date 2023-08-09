public abstract class DBCS_ONLY_IBM_EBCDIC_Decoder extends CharsetDecoder
{
    protected static final char REPLACE_CHAR='\uFFFD';
    protected short   index1[];
    protected String  index2;
    protected int     mask1;
    protected int     mask2;
    protected int     shift;
    protected DBCS_ONLY_IBM_EBCDIC_Decoder(Charset cs) {
        super(cs, 0.5f, 1.0f);
    }
    private static boolean isValidDoubleByte(int b1, int b2) {
        return (b1 == 0x40 && b2 == 0x40) 
            || (0x41 <= b1 && b1 <= 0xfe &&
                0x41 <= b2 && b2 <= 0xfe);
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
            while (sp + 1 < sl) {
                int b1 = sa[sp] & 0xff;
                int b2 = sa[sp + 1] & 0xff;
                if (!isValidDoubleByte(b1, b2)) {
                    return CoderResult.malformedForLength(2);
                }
                int v = b1 * 256 + b2;
                char outputChar = index2.charAt(index1[((v & mask1) >> shift)]
                                                + (v & mask2));
                if (outputChar == REPLACE_CHAR)
                    return CoderResult.unmappableForLength(2);
                if (dl - dp < 1)
                    return CoderResult.OVERFLOW;
                da[dp++] = outputChar;
                sp += 2;
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
            while (src.remaining() > 1) {
                int b1 = src.get() & 0xff;
                int b2 = src.get() & 0xff;
                if (!isValidDoubleByte(b1, b2)) {
                    return CoderResult.malformedForLength(2);
                }
                int v = b1 * 256 + b2;
                char outputChar = index2.charAt(index1[((v & mask1) >> shift)]
                                                + (v & mask2));
                if (outputChar == REPLACE_CHAR)
                    return CoderResult.unmappableForLength(2);
                if (!dst.hasRemaining())
                    return CoderResult.OVERFLOW;
                dst.put(outputChar);
                mark += 2;
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
