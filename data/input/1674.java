abstract class SimpleEUCDecoder
    extends CharsetDecoder
{
    private final int SS2 =  0x8E;
    private final int SS3 =  0x8F;
    protected static String  mappingTableG1;
    protected static String  byteToCharTable;
    protected SimpleEUCDecoder(Charset cs) {
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
                int byte1, byte2;
                int inputSize = 1;
                char outputChar = '\uFFFD';
                byte1 = sa[sp] & 0xff;
                if ( byte1 <= 0x9f ) {  
                    if (byte1 == SS2 || byte1 == SS3 ) {
                        return CoderResult.malformedForLength(1);
                    }
                    outputChar = byteToCharTable.charAt(byte1);
                } else if (byte1 < 0xa1 || byte1 > 0xfe) {  
                    return CoderResult.malformedForLength(1);
                } else {                                        
                    if (sl - sp < 2) {
                        return CoderResult.UNDERFLOW;
                    }
                    byte2 = sa[sp + 1] & 0xff;
                    inputSize++;
                    if ( byte2 < 0xa1 || byte2 > 0xfe) {
                        return CoderResult.malformedForLength(2);
                    }
                    outputChar = mappingTableG1.charAt(((byte1 - 0xa1) * 94) + byte2 - 0xa1);
                }
                if  (outputChar == '\uFFFD') {
                    return CoderResult.unmappableForLength(inputSize);
                }
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
                char outputChar = '\uFFFD';
                int inputSize = 1;
                int byte1, byte2;
                byte1 = src.get() & 0xff;
                if ( byte1 <= 0x9f ) {
                    if (byte1 == SS2 || byte1 == SS3 ) {
                        return CoderResult.malformedForLength(1);
                    }
                    outputChar = byteToCharTable.charAt(byte1);
                } else if (byte1 < 0xa1 || byte1 > 0xfe) {
                    return CoderResult.malformedForLength(1);
                } else {
                    if (!src.hasRemaining()) {
                        return CoderResult.UNDERFLOW;
                    }
                    byte2 = src.get() & 0xff;
                    inputSize++;
                    if ( byte2 < 0xa1 || byte2 > 0xfe) {
                        return CoderResult.malformedForLength(2);
                    }
                    outputChar = mappingTableG1.charAt(((byte1 - 0xa1) * 94) + byte2 - 0xa1);
                }
                if (outputChar == '\uFFFD') {
                    return CoderResult.unmappableForLength(inputSize);
                }
                if (!dst.hasRemaining())
                    return CoderResult.OVERFLOW;
                    dst.put(outputChar);
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
