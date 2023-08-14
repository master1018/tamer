public class ISO2022_JP
    extends Charset
    implements HistoricallyNamedCharset
{
    private static final int ASCII = 0;                 
    private static final int JISX0201_1976 = 1;         
    private static final int JISX0208_1978 = 2;         
    private static final int JISX0208_1983 = 3;         
    private static final int JISX0212_1990 = 4;         
    private static final int JISX0201_1976_KANA = 5;    
    private static final int SHIFTOUT = 6;
    private static final int ESC = 0x1b;
    private static final int SO = 0x0e;
    private static final int SI = 0x0f;
    public ISO2022_JP() {
        super("ISO-2022-JP",
              ExtendedCharsets.aliasesFor("ISO-2022-JP"));
    }
    protected ISO2022_JP(String canonicalName,
                         String[] aliases) {
        super(canonicalName, aliases);
    }
    public String historicalName() {
        return "ISO2022JP";
    }
    public boolean contains(Charset cs) {
        return ((cs instanceof JIS_X_0201)
                || (cs instanceof US_ASCII)
                || (cs instanceof JIS_X_0208)
                || (cs instanceof ISO2022_JP));
    }
    public CharsetDecoder newDecoder() {
        return new Decoder(this,
                           getDecIndex1(),
                           getDecIndex2(),
                           get0212Decoder());
    }
    public CharsetEncoder newEncoder() {
        return new Encoder(this,
                           getEncIndex1(),
                           getEncIndex2(),
                           get0212Encoder(),
                           doSBKANA());
    }
    protected short[] getDecIndex1() {
        return JIS_X_0208_Decoder.getIndex1();
    }
    protected String[] getDecIndex2() {
        return JIS_X_0208_Decoder.getIndex2();
    }
    protected DoubleByteDecoder get0212Decoder() {
        return null;
    }
    protected short[] getEncIndex1() {
        return JIS_X_0208_Encoder.getIndex1();
    }
    protected String[] getEncIndex2() {
        return JIS_X_0208_Encoder.getIndex2();
    }
    protected DoubleByteEncoder get0212Encoder() {
        return null;
    }
    protected boolean doSBKANA() {
        return true;
    }
    private static class Decoder extends DoubleByteDecoder
        implements DelegatableDecoder {
        private int currentState;
        private int previousState;
        private DoubleByteDecoder decoder0212;
        protected Decoder(Charset cs,
                          short[] index1,
                          String[] index2,
                          DoubleByteDecoder decoder0212) {
            super(cs,
                  index1,
                  index2,
                  0x21,
                  0x7e);
            this.decoder0212 = decoder0212;
            currentState = ASCII;
            previousState = ASCII;
        }
        protected char convSingleByte(int b) {
            return REPLACE_CHAR;
        }
        public void implReset() {
            currentState = ASCII;
            previousState = ASCII;
        }
        private CoderResult decodeArrayLoop(ByteBuffer src,
                                            CharBuffer dst)
        {
            int inputSize = 0;
            int b1 = 0, b2 = 0, b3 = 0, b4 = 0;
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
                    b1 = sa[sp] & 0xff;
                    inputSize = 1;
                    if ((b1 & 0x80) != 0) {
                        return CoderResult.malformedForLength(inputSize);
                    }
                    if (b1 == ESC || b1 == SO || b1 == SI) {
                        if (b1 == ESC) {
                            if (sp + inputSize + 2 > sl)
                                return CoderResult.UNDERFLOW;
                            b2 = sa[sp + inputSize++] & 0xff;
                            if (b2 == '(') {
                                b3 = sa[sp + inputSize++] & 0xff;
                                if (b3 == 'B'){
                                    currentState = ASCII;
                                } else if (b3 == 'J'){
                                    currentState = JISX0201_1976;
                                } else if (b3 == 'I'){
                                    currentState = JISX0201_1976_KANA;
                                } else {
                                    return CoderResult.malformedForLength(inputSize);
                                }
                            } else if (b2 == '$'){
                                b3 = sa[sp + inputSize++] & 0xff;
                                if (b3 == '@'){
                                    currentState = JISX0208_1978;
                                } else if (b3 == 'B'){
                                    currentState = JISX0208_1983;
                                } else if (b3 == '(' && decoder0212 != null) {
                                    if (sp + inputSize + 1 > sl)
                                        return CoderResult.UNDERFLOW;
                                    b4 = sa[sp + inputSize++] & 0xff;
                                    if (b4 == 'D') {
                                        currentState = JISX0212_1990;
                                    } else {
                                        return CoderResult.malformedForLength(inputSize);
                                    }
                                } else {
                                    return CoderResult.malformedForLength(inputSize);
                                }
                            } else {
                                return CoderResult.malformedForLength(inputSize);
                            }
                        } else if (b1 == SO) {
                            previousState = currentState;
                            currentState = SHIFTOUT;
                        } else if (b1 == SI) {
                            currentState = previousState;
                        }
                        sp += inputSize;
                        continue;
                    }
                    if (dp + 1 > dl)
                        return CoderResult.OVERFLOW;
                    switch (currentState){
                        case ASCII:
                            da[dp++] = (char)(b1 & 0xff);
                            break;
                        case JISX0201_1976:
                          switch (b1) {
                              case 0x5c:  
                                da[dp++] = '\u00a5';
                                break;
                              case 0x7e:
                                da[dp++] = '\u203e';
                                break;
                              default:
                                da[dp++] = (char)b1;
                                break;
                            }
                            break;
                        case JISX0208_1978:
                        case JISX0208_1983:
                            if (sp + inputSize + 1 > sl)
                                return CoderResult.UNDERFLOW;
                            b2 = sa[sp + inputSize++] & 0xff;
                            c = decodeDouble(b1,b2);
                            if (c == REPLACE_CHAR)
                                return CoderResult.unmappableForLength(inputSize);
                            da[dp++] = c;
                            break;
                        case JISX0212_1990:
                            if (sp + inputSize + 1 > sl)
                                return CoderResult.UNDERFLOW;
                            b2 = sa[sp + inputSize++] & 0xff;
                            c = decoder0212.decodeDouble(b1,b2);
                            if (c == REPLACE_CHAR)
                                return CoderResult.unmappableForLength(inputSize);
                            da[dp++] = c;
                            break;
                        case JISX0201_1976_KANA:
                        case SHIFTOUT:
                            if (b1 > 0x60) {
                                return CoderResult.malformedForLength(inputSize);
                            }
                            da[dp++] = (char)(b1 + 0xff40);
                            break;
                    }
                    sp += inputSize;
                }
                return CoderResult.UNDERFLOW;
            } finally {
                src.position(sp - src.arrayOffset());
                dst.position(dp - dst.arrayOffset());
            }
        }
        private CoderResult decodeBufferLoop(ByteBuffer src,
                                             CharBuffer dst)
        {
            int mark = src.position();
            int b1 = 0, b2 = 0, b3 = 0, b4=0;
            char c = REPLACE_CHAR;
            int inputSize = 0;
            try {
                while (src.hasRemaining()) {
                    b1 = src.get() & 0xff;
                    inputSize = 1;
                    if ((b1 & 0x80) != 0)
                        return CoderResult.malformedForLength(inputSize);
                    if (b1 == ESC || b1 == SO || b1 == SI) {
                        if (b1 == ESC) {  
                            if (src.remaining() < 2)
                                return CoderResult.UNDERFLOW;
                            b2 = src.get() & 0xff;
                            inputSize++;
                            if (b2 == '(') {
                                b3 = src.get() & 0xff;
                                inputSize++;
                                if (b3 == 'B'){
                                    currentState = ASCII;
                                } else if (b3 == 'J'){
                                    currentState = JISX0201_1976;
                                } else if (b3 == 'I'){
                                    currentState = JISX0201_1976_KANA;
                                } else {
                                   return CoderResult.malformedForLength(inputSize);
                                }
                            } else if (b2 == '$'){
                                b3 = src.get() & 0xff;
                                inputSize++;
                                if (b3 == '@'){
                                    currentState = JISX0208_1978;
                                } else if (b3 == 'B'){
                                    currentState = JISX0208_1983;
                                } else if (b3 == '(' && decoder0212 != null) {
                                    if (!src.hasRemaining())
                                        return CoderResult.UNDERFLOW;
                                    b4 = src.get() & 0xff;
                                    inputSize++;
                                    if (b4 == 'D') {
                                        currentState = JISX0212_1990;
                                    } else {
                                        return CoderResult.malformedForLength(inputSize);
                                    }
                                } else {
                                    return CoderResult.malformedForLength(inputSize);
                                }
                            } else {
                                return CoderResult.malformedForLength(inputSize);
                            }
                        } else if (b1 == SO) {
                            previousState = currentState;
                            currentState = SHIFTOUT;
                        } else if (b1 == SI) { 
                            currentState = previousState;
                        }
                        mark += inputSize;
                        continue;
                    }
                    if (!dst.hasRemaining())
                        return CoderResult.OVERFLOW;
                    switch (currentState){
                        case ASCII:
                            dst.put((char)(b1 & 0xff));
                            break;
                        case JISX0201_1976:
                            switch (b1) {
                              case 0x5c:  
                                dst.put('\u00a5');
                                break;
                              case 0x7e:
                                dst.put('\u203e');
                                break;
                              default:
                                dst.put((char)b1);
                                break;
                            }
                            break;
                        case JISX0208_1978:
                        case JISX0208_1983:
                            if (!src.hasRemaining())
                                return CoderResult.UNDERFLOW;
                            b2 = src.get() & 0xff;
                            inputSize++;
                            c = decodeDouble(b1,b2);
                            if (c == REPLACE_CHAR)
                                return CoderResult.unmappableForLength(inputSize);
                            dst.put(c);
                            break;
                        case JISX0212_1990:
                            if (!src.hasRemaining())
                                return CoderResult.UNDERFLOW;
                            b2 = src.get() & 0xff;
                            inputSize++;
                            c = decoder0212.decodeDouble(b1,b2);
                            if (c == REPLACE_CHAR)
                                return CoderResult.unmappableForLength(inputSize);
                            dst.put(c);
                            break;
                        case JISX0201_1976_KANA:
                        case SHIFTOUT:
                            if (b1 > 0x60) {
                                return CoderResult.malformedForLength(inputSize);
                            }
                            dst.put((char)(b1 + 0xff40));
                            break;
                    }
                    mark += inputSize;
                }
                return CoderResult.UNDERFLOW;
            } finally {
                src.position(mark);
            }
        }
        public CoderResult decodeLoop(ByteBuffer src, CharBuffer dst) {
            if (src.hasArray() && dst.hasArray())
                return decodeArrayLoop(src, dst);
            else
                return decodeBufferLoop(src, dst);
        }
        public CoderResult implFlush(CharBuffer out) {
            return super.implFlush(out);
        }
    }
    private static class Encoder extends DoubleByteEncoder {
        private static byte[] repl = { (byte)0x21, (byte)0x29 };
        private int currentMode = ASCII;
        private int replaceMode = JISX0208_1983;
        private DoubleByteEncoder encoder0212 = null;
        private boolean doSBKANA;
        private Encoder(Charset cs,
                        short[] index1,
                        String[] index2,
                        DoubleByteEncoder encoder0212,
                        boolean doSBKANA) {
            super(cs,
                  index1,
                  index2,
                  repl,
                  4.0f,
                  (encoder0212 != null)? 9.0f : 8.0f);
            this.encoder0212 = encoder0212;
            this.doSBKANA = doSBKANA;
        }
        protected int encodeSingle(char inputChar) {
            return -1;
        }
        protected void implReset() {
            currentMode = ASCII;
        }
        protected void implReplaceWith(byte[] newReplacement) {
            if (newReplacement.length == 1) {
                replaceMode = ASCII;
            } else if (newReplacement.length == 2) {
                replaceMode = JISX0208_1983;
            }
        }
        protected CoderResult implFlush(ByteBuffer out) {
            if (currentMode != ASCII) {
                if (out.remaining() < 3)
                    return CoderResult.OVERFLOW;
                out.put((byte)0x1b);
                out.put((byte)0x28);
                out.put((byte)0x42);
                currentMode = ASCII;
            }
            return CoderResult.UNDERFLOW;
        }
        public boolean canEncode(char c) {
            return ((c <= '\u007F') ||
                    (c >= 0xFF61 && c <= 0xFF9F) ||
                    (c == '\u00A5') ||
                    (c == '\u203E') ||
                    super.canEncode(c) ||
                    (encoder0212!=null && encoder0212.canEncode(c)));
        }
        private final Surrogate.Parser sgp = new Surrogate.Parser();
        private CoderResult encodeArrayLoop(CharBuffer src,
                                            ByteBuffer dst)
        {
            char[] sa = src.array();
            int sp = src.arrayOffset() + src.position();
            int sl = src.arrayOffset() + src.limit();
            assert (sp <= sl);
            sp = (sp <= sl ? sp : sl);
            byte[] da = dst.array();
            int dp = dst.arrayOffset() + dst.position();
            int dl = dst.arrayOffset() + dst.limit();
            assert (dp <= dl);
            dp = (dp <= dl ? dp : dl);
            try {
                while (sp < sl) {
                    char c = sa[sp];
                    if (c <= '\u007F') {
                        if (currentMode != ASCII) {
                            if (dl - dp < 3)
                                return CoderResult.OVERFLOW;
                            da[dp++] = (byte)0x1b;
                            da[dp++] = (byte)0x28;
                            da[dp++] = (byte)0x42;
                            currentMode = ASCII;
                        }
                        if (dl - dp < 1)
                            return CoderResult.OVERFLOW;
                        da[dp++] = (byte)c;
                    } else if (c >= 0xff61 && c <= 0xff9f && doSBKANA) {
                        if (currentMode != JISX0201_1976_KANA) {
                            if (dl - dp < 3)
                                return CoderResult.OVERFLOW;
                            da[dp++] = (byte)0x1b;
                            da[dp++] = (byte)0x28;
                            da[dp++] = (byte)0x49;
                            currentMode = JISX0201_1976_KANA;
                        }
                        if (dl - dp < 1)
                            return CoderResult.OVERFLOW;
                        da[dp++] = (byte)(c - 0xff40);
                    } else if (c == '\u00A5' || c == '\u203E') {
                        if (currentMode != JISX0201_1976) {
                            if (dl - dp < 3)
                                return CoderResult.OVERFLOW;
                            da[dp++] = (byte)0x1b;
                            da[dp++] = (byte)0x28;
                            da[dp++] = (byte)0x4a;
                            currentMode = JISX0201_1976;
                        }
                        if (dl - dp < 1)
                            return CoderResult.OVERFLOW;
                        da[dp++] = (c == '\u00A5')?(byte)0x5C:(byte)0x7e;
                    } else {
                        int index = encodeDouble(c);
                        if (index != 0) {
                            if (currentMode != JISX0208_1983) {
                                if (dl - dp < 3)
                                    return CoderResult.OVERFLOW;
                                da[dp++] = (byte)0x1b;
                                da[dp++] = (byte)0x24;
                                da[dp++] = (byte)0x42;
                                currentMode = JISX0208_1983;
                            }
                            if (dl - dp < 2)
                                return CoderResult.OVERFLOW;
                            da[dp++] = (byte)(index >> 8);
                            da[dp++] = (byte)(index & 0xff);
                        } else if (encoder0212 != null &&
                                   (index = encoder0212.encodeDouble(c)) != 0) {
                            if (currentMode != JISX0212_1990) {
                                if (dl - dp < 4)
                                    return CoderResult.OVERFLOW;
                                da[dp++] = (byte)0x1b;
                                da[dp++] = (byte)0x24;
                                da[dp++] = (byte)0x28;
                                da[dp++] = (byte)0x44;
                                currentMode = JISX0212_1990;
                            }
                            if (dl - dp < 2)
                                return CoderResult.OVERFLOW;
                            da[dp++] = (byte)(index >> 8);
                            da[dp++] = (byte)(index & 0xff);
                        } else {
                            if (Character.isSurrogate(c) && sgp.parse(c, sa, sp, sl) < 0)
                                return sgp.error();
                            if (unmappableCharacterAction()
                                == CodingErrorAction.REPLACE
                                && currentMode != replaceMode) {
                                if (dl - dp < 3)
                                    return CoderResult.OVERFLOW;
                                if (replaceMode == ASCII) {
                                    da[dp++] = (byte)0x1b;
                                    da[dp++] = (byte)0x28;
                                    da[dp++] = (byte)0x42;
                                } else {
                                    da[dp++] = (byte)0x1b;
                                    da[dp++] = (byte)0x24;
                                    da[dp++] = (byte)0x42;
                                }
                                currentMode = replaceMode;
                            }
                            if (Character.isSurrogate(c))
                                return sgp.unmappableResult();
                            return CoderResult.unmappableForLength(1);
                        }
                    }
                    sp++;
                }
                return CoderResult.UNDERFLOW;
            } finally {
                src.position(sp - src.arrayOffset());
                dst.position(dp - dst.arrayOffset());
            }
        }
        private CoderResult encodeBufferLoop(CharBuffer src,
                                             ByteBuffer dst)
        {
            int mark = src.position();
            try {
                while (src.hasRemaining()) {
                    char c = src.get();
                    if (c <= '\u007F') {
                        if (currentMode != ASCII) {
                            if (dst.remaining() < 3)
                                return CoderResult.OVERFLOW;
                            dst.put((byte)0x1b);
                            dst.put((byte)0x28);
                            dst.put((byte)0x42);
                            currentMode = ASCII;
                        }
                        if (dst.remaining() < 1)
                            return CoderResult.OVERFLOW;
                        dst.put((byte)c);
                    } else if (c >= 0xff61 && c <= 0xff9f && doSBKANA) {
                        if (currentMode != JISX0201_1976_KANA) {
                            if (dst.remaining() < 3)
                                return CoderResult.OVERFLOW;
                            dst.put((byte)0x1b);
                            dst.put((byte)0x28);
                            dst.put((byte)0x49);
                            currentMode = JISX0201_1976_KANA;
                        }
                        if (dst.remaining() < 1)
                            return CoderResult.OVERFLOW;
                        dst.put((byte)(c - 0xff40));
                    } else if (c == '\u00a5' || c == '\u203E') {
                        if (currentMode != JISX0201_1976) {
                            if (dst.remaining() < 3)
                                return CoderResult.OVERFLOW;
                            dst.put((byte)0x1b);
                            dst.put((byte)0x28);
                            dst.put((byte)0x4a);
                            currentMode = JISX0201_1976;
                        }
                        if (dst.remaining() < 1)
                            return CoderResult.OVERFLOW;
                        dst.put((c == '\u00A5')?(byte)0x5C:(byte)0x7e);
                    } else {
                        int index = encodeDouble(c);
                        if (index != 0) {
                            if (currentMode != JISX0208_1983) {
                                if (dst.remaining() < 3)
                                    return CoderResult.OVERFLOW;
                                dst.put((byte)0x1b);
                                dst.put((byte)0x24);
                                dst.put((byte)0x42);
                                currentMode = JISX0208_1983;
                            }
                            if (dst.remaining() < 2)
                                return CoderResult.OVERFLOW;
                            dst.put((byte)(index >> 8));
                            dst.put((byte)(index & 0xff));
                        } else if (encoder0212 != null &&
                                   (index = encoder0212.encodeDouble(c)) != 0) {
                            if (currentMode != JISX0212_1990) {
                                if (dst.remaining() < 4)
                                    return CoderResult.OVERFLOW;
                                dst.put((byte)0x1b);
                                dst.put((byte)0x24);
                                dst.put((byte)0x28);
                                dst.put((byte)0x44);
                                currentMode = JISX0212_1990;
                            }
                            if (dst.remaining() < 2)
                                return CoderResult.OVERFLOW;
                            dst.put((byte)(index >> 8));
                            dst.put((byte)(index & 0xff));
                        } else {
                            if (Character.isSurrogate(c) && sgp.parse(c, src) < 0)
                                return sgp.error();
                            if (unmappableCharacterAction() == CodingErrorAction.REPLACE
                                && currentMode != replaceMode) {
                                if (dst.remaining() < 3)
                                    return CoderResult.OVERFLOW;
                                if (replaceMode == ASCII) {
                                    dst.put((byte)0x1b);
                                    dst.put((byte)0x28);
                                    dst.put((byte)0x42);
                                } else {
                                    dst.put((byte)0x1b);
                                    dst.put((byte)0x24);
                                    dst.put((byte)0x42);
                                }
                                currentMode = replaceMode;
                            }
                            if (Character.isSurrogate(c))
                                return sgp.unmappableResult();
                            return CoderResult.unmappableForLength(1);
                        }
                    }
                    mark++;
                }
                return CoderResult.UNDERFLOW;
              } finally {
                src.position(mark);
            }
        }
        protected CoderResult encodeLoop(CharBuffer src,
                                         ByteBuffer dst)
        {
            if (src.hasArray() && dst.hasArray())
                return encodeArrayLoop(src, dst);
            else
                return encodeBufferLoop(src, dst);
        }
    }
}
