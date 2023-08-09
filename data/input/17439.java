public abstract class DBCS_IBM_EBCDIC_Decoder extends CharsetDecoder
{
    private DBCSDecoderMapping decoderMapping;
    protected static final char REPLACE_CHAR='\uFFFD';
    protected String  singleByteToChar;
    protected short   index1[];
    protected String  index2;
    protected int     mask1;
    protected int     mask2;
    protected int     shift;
    private static final int SBCS = 0;
    private static final int DBCS = 1;
    private static final int SO = 0x0e;
    private static final int SI = 0x0f;
    private int  currentState;
    protected DBCS_IBM_EBCDIC_Decoder(Charset cs) {
        super(cs, 0.5f, 1.0f);
    }
    protected void implReset() {
        currentState = SBCS;
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
                if (b1 == SO) {  
                    if (currentState != SBCS)
                        return CoderResult.malformedForLength(1);
                    else
                        currentState = DBCS;
                } else if (b1 == SI) {
                    if (currentState != DBCS) {
                        return CoderResult.malformedForLength(1);
                    } else {
                        currentState = SBCS;
                    }
                } else {
                    if (currentState == SBCS) {
                        outputChar = singleByteToChar.charAt(b1);
                    } else {
                    if (sl - sp < 2)
                        return CoderResult.UNDERFLOW;
                    b2 = sa[sp + 1];
                    if (b2 < 0)
                        b2 += 256;
                    inputSize++;
                    if ((b1 != 0x40 || b2 != 0x40) &&
                      (b2 < 0x41 || b2 > 0xfe)) {
                      return CoderResult.malformedForLength(2);
                    }
                    v = b1 * 256 + b2;
                    outputChar = index2.charAt(index1[((v & mask1) >> shift)]
                                                + (v & mask2));
                    }
                    if (outputChar == '\uFFFD')
                        return CoderResult.unmappableForLength(inputSize);
                    if (dl - dp < 1)
                        return CoderResult.OVERFLOW;
                    da[dp++] = outputChar;
                }
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
                int b1, b2;
                int v = 0;
                b1 = src.get();
                int inputSize = 1;
                char outputChar = REPLACE_CHAR;
                if (b1 < 0)
                    b1 += 256;
                if (b1 == SO) {  
                    if (currentState != SBCS)
                        return CoderResult.malformedForLength(1);
                    else
                        currentState = DBCS;
                } else if (b1 == SI) {
                    if (currentState != DBCS) {
                        return CoderResult.malformedForLength(1);
                    } else {
                        currentState = SBCS;
                    }
                } else {
                    if (currentState == SBCS) {
                      outputChar = singleByteToChar.charAt(b1);
                    } else {
                        if (src.remaining() < 1)
                            return CoderResult.UNDERFLOW;
                        b2 = src.get();
                        if (b2 < 0)
                            b2 += 256;
                        inputSize++;
                        if ((b1 != 0x40 || b2 != 0x40) &&
                           (b2 < 0x41 || b2 > 0xfe)) {
                          return CoderResult.malformedForLength(2);
                        }
                        v = b1 * 256 + b2;
                        outputChar = index2.charAt(index1[((v & mask1) >> shift)]
                                                            + (v & mask2));
                    }
                    if (outputChar == REPLACE_CHAR)
                        return CoderResult.unmappableForLength(inputSize);
                    if (!dst.hasRemaining())
                        return CoderResult.OVERFLOW;
                    dst.put(outputChar);
                }
                mark += inputSize;
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
