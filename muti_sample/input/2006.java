public class ISO2022_CN
    extends Charset
    implements HistoricallyNamedCharset
{
    private static final byte ISO_ESC = 0x1b;
    private static final byte ISO_SI = 0x0f;
    private static final byte ISO_SO = 0x0e;
    private static final byte ISO_SS2_7 = 0x4e;
    private static final byte ISO_SS3_7 = 0x4f;
    private static final byte MSB = (byte)0x80;
    private static final char REPLACE_CHAR = '\uFFFD';
    private static final byte SODesigGB = 0;
    private static final byte SODesigCNS = 1;
    public ISO2022_CN() {
        super("ISO-2022-CN", ExtendedCharsets.aliasesFor("ISO-2022-CN"));
    }
    public String historicalName() {
        return "ISO2022CN";
    }
    public boolean contains(Charset cs) {
        return ((cs instanceof EUC_CN)     
                || (cs instanceof US_ASCII)
                || (cs instanceof EUC_TW)  
                || (cs instanceof ISO2022_CN));
    }
    public CharsetDecoder newDecoder() {
        return new Decoder(this);
    }
    public CharsetEncoder newEncoder() {
        throw new UnsupportedOperationException();
    }
    public boolean canEncode() {
        return false;
    }
    static class Decoder extends CharsetDecoder {
        private boolean shiftOut;
        private byte currentSODesig;
        private static final Charset gb2312 = new EUC_CN();
        private static final Charset cns = new EUC_TW();
        private final DoubleByte.Decoder gb2312Decoder;
        private final EUC_TW.Decoder cnsDecoder;
        Decoder(Charset cs) {
            super(cs, 1.0f, 1.0f);
            shiftOut = false;
            currentSODesig = SODesigGB;
            gb2312Decoder = (DoubleByte.Decoder)gb2312.newDecoder();
            cnsDecoder = (EUC_TW.Decoder)cns.newDecoder();
        }
        protected void implReset() {
            shiftOut= false;
            currentSODesig = SODesigGB;
        }
        private char cnsDecode(byte byte1, byte byte2, byte SS) {
            byte1 |= MSB;
            byte2 |= MSB;
            int p = 0;
            if (SS == ISO_SS2_7)
                p = 1;    
            else if (SS == ISO_SS3_7)
                p = 2;    
            else
                return REPLACE_CHAR;  
            char[] ret = cnsDecoder.toUnicode(byte1 & 0xff,
                                              byte2 & 0xff,
                                              p);
            if (ret == null || ret.length == 2)
                return REPLACE_CHAR;
            return ret[0];
        }
        private char SODecode(byte byte1, byte byte2, byte SOD) {
            byte1 |= MSB;
            byte2 |= MSB;
            if (SOD == SODesigGB) {
                return gb2312Decoder.decodeDouble(byte1 & 0xff,
                                                  byte2 & 0xff);
            } else {    
                char[] ret = cnsDecoder.toUnicode(byte1 & 0xff,
                                                  byte2 & 0xff,
                                                  0);
                if (ret == null)
                    return REPLACE_CHAR;
                return ret[0];
            }
        }
        private CoderResult decodeBufferLoop(ByteBuffer src,
                                             CharBuffer dst)
        {
            int mark = src.position();
            byte b1 = 0, b2 = 0, b3 = 0, b4 = 0;
            int inputSize = 0;
            char c = REPLACE_CHAR;
            try {
                while (src.hasRemaining()) {
                    b1 = src.get();
                    inputSize = 1;
                    while (b1 == ISO_ESC ||
                           b1 == ISO_SO ||
                           b1 == ISO_SI) {
                        if (b1 == ISO_ESC) {  
                            currentSODesig = SODesigGB;
                            if (src.remaining() < 1)
                                return CoderResult.UNDERFLOW;
                            b2 = src.get();
                            inputSize++;
                            if ((b2 & (byte)0x80) != 0)
                                return CoderResult.malformedForLength(inputSize);
                            if (b2 == (byte)0x24) {
                                if (src.remaining() < 1)
                                    return CoderResult.UNDERFLOW;
                                b3 = src.get();
                                inputSize++;
                                if ((b3 & (byte)0x80) != 0)
                                    return CoderResult.malformedForLength(inputSize);
                                if (b3 == 'A'){              
                                    currentSODesig = SODesigGB;
                                } else if (b3 == ')') {
                                    if (src.remaining() < 1)
                                        return CoderResult.UNDERFLOW;
                                    b4 = src.get();
                                    inputSize++;
                                    if (b4 == 'A'){          
                                        currentSODesig = SODesigGB;
                                    } else if (b4 == 'G'){   
                                        currentSODesig = SODesigCNS;
                                    } else {
                                        return CoderResult.malformedForLength(inputSize);
                                    }
                                } else if (b3 == '*') {
                                    if (src.remaining() < 1)
                                        return CoderResult.UNDERFLOW;
                                    b4 = src.get();
                                    inputSize++;
                                    if (b4 != 'H') {         
                                        return CoderResult.malformedForLength(inputSize);
                                    }
                                } else if (b3 == '+') {
                                    if (src.remaining() < 1)
                                        return CoderResult.UNDERFLOW;
                                    b4 = src.get();
                                    inputSize++;
                                    if (b4 != 'I'){          
                                        return CoderResult.malformedForLength(inputSize);
                                    }
                                } else {
                                        return CoderResult.malformedForLength(inputSize);
                                }
                            } else if (b2 == ISO_SS2_7 || b2 == ISO_SS3_7) {
                                if (src.remaining() < 2)
                                    return CoderResult.UNDERFLOW;
                                b3 = src.get();
                                b4 = src.get();
                                inputSize += 2;
                                if (dst.remaining() < 1)
                                    return CoderResult.OVERFLOW;
                                c = cnsDecode(b3, b4, b2);
                                if (c == REPLACE_CHAR)
                                    return CoderResult.unmappableForLength(inputSize);
                                dst.put(c);
                            } else {
                                return CoderResult.malformedForLength(inputSize);
                            }
                        } else if (b1 == ISO_SO) {
                            shiftOut = true;
                        } else if (b1 == ISO_SI) { 
                            shiftOut = false;
                        }
                        mark += inputSize;
                        if (src.remaining() < 1)
                            return CoderResult.UNDERFLOW;
                        b1 = src.get();
                        inputSize = 1;
                    }
                    if (dst.remaining() < 1)
                        return CoderResult.OVERFLOW;
                    if (!shiftOut) {
                        dst.put((char)(b1 & 0xff));  
                        mark += inputSize;
                    } else {
                        if (src.remaining() < 1)
                            return CoderResult.UNDERFLOW;
                        b2 = src.get();
                        inputSize++;
                        c = SODecode(b1, b2, currentSODesig);
                        if (c == REPLACE_CHAR)
                            return CoderResult.unmappableForLength(inputSize);
                        dst.put(c);
                        mark += inputSize;
                    }
                }
                return CoderResult.UNDERFLOW;
            } finally {
                src.position(mark);
            }
        }
        private CoderResult decodeArrayLoop(ByteBuffer src,
                                            CharBuffer dst)
        {
            int inputSize = 0;
            byte b1 = 0, b2 = 0, b3 = 0, b4 = 0;
            char c = REPLACE_CHAR;
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
                    b1 = sa[sp];
                    inputSize = 1;
                    while (b1 == ISO_ESC || b1 == ISO_SO || b1 == ISO_SI) {
                        if (b1 == ISO_ESC) {  
                            currentSODesig = SODesigGB;
                            if (sp + 2 > sl)
                                return CoderResult.UNDERFLOW;
                            b2 = sa[sp + 1];
                            inputSize++;
                            if ((b2 & (byte)0x80) != 0)
                                return CoderResult.malformedForLength(inputSize);
                            if (b2 == (byte)0x24) {
                                if (sp + 3 > sl)
                                    return CoderResult.UNDERFLOW;
                                b3 = sa[sp + 2];
                                inputSize++;
                                if ((b3 & (byte)0x80) != 0)
                                    return CoderResult.malformedForLength(inputSize);
                                if (b3 == 'A'){              
                                    currentSODesig = SODesigGB;
                                } else if (b3 == ')') {
                                    if (sp + 4 > sl)
                                        return CoderResult.UNDERFLOW;
                                    b4 = sa[sp + 3];
                                    inputSize++;
                                    if (b4 == 'A'){          
                                        currentSODesig = SODesigGB;
                                    } else if (b4 == 'G'){   
                                        currentSODesig = SODesigCNS;
                                    } else {
                                        return CoderResult.malformedForLength(inputSize);
                                    }
                                } else if (b3 == '*') {
                                    if (sp + 4 > sl)
                                        return CoderResult.UNDERFLOW;
                                    b4 = sa[sp + 3];
                                    inputSize++;
                                    if (b4 != 'H'){          
                                        return CoderResult.malformedForLength(inputSize);
                                    }
                                } else if (b3 == '+') {
                                    if (sp + 4 > sl)
                                        return CoderResult.UNDERFLOW;
                                    b4 = sa[sp + 3];
                                    inputSize++;
                                    if (b4 != 'I'){          
                                        return CoderResult.malformedForLength(inputSize);
                                    }
                                } else {
                                        return CoderResult.malformedForLength(inputSize);
                                }
                            } else if (b2 == ISO_SS2_7 || b2 == ISO_SS3_7) {
                                if (sp + 4 > sl) {
                                    return CoderResult.UNDERFLOW;
                                }
                                b3 = sa[sp + 2];
                                b4 = sa[sp + 3];
                                if (dl - dp < 1)  {
                                    return CoderResult.OVERFLOW;
                                }
                                inputSize += 2;
                                c = cnsDecode(b3, b4, b2);
                                if (c == REPLACE_CHAR)
                                    return CoderResult.unmappableForLength(inputSize);
                                da[dp++] = c;
                            } else {
                                return CoderResult.malformedForLength(inputSize);
                            }
                        } else if (b1 == ISO_SO) {
                            shiftOut = true;
                        } else if (b1 == ISO_SI) { 
                            shiftOut = false;
                        }
                        sp += inputSize;
                        if (sp + 1 > sl)
                            return CoderResult.UNDERFLOW;
                        b1 = sa[sp];
                        inputSize = 1;
                    }
                    if (dl - dp < 1) {
                        return CoderResult.OVERFLOW;
                    }
                    if (!shiftOut) {
                        da[dp++] = (char)(b1 & 0xff);  
                    } else {
                        if (sp + 2 > sl)
                            return CoderResult.UNDERFLOW;
                        b2 = sa[sp + 1];
                        inputSize++;
                        c = SODecode(b1, b2, currentSODesig);
                        if (c == REPLACE_CHAR)
                            return CoderResult.unmappableForLength(inputSize);
                        da[dp++] = c;
                    }
                    sp += inputSize;
                }
                return CoderResult.UNDERFLOW;
            } finally {
                src.position(sp - src.arrayOffset());
                dst.position(dp - dst.arrayOffset());
            }
        }
        protected CoderResult decodeLoop(ByteBuffer src,
                                         CharBuffer dst)
        {
            if (src.hasArray() && dst.hasArray())
                return decodeArrayLoop(src, dst);
            else
                return decodeBufferLoop(src, dst);
        }
    }
}
