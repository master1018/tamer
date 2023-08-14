public class EUC_TW extends Charset implements HistoricallyNamedCharset
{
    private static final int SS2 = 0x8E;
    public EUC_TW() {
        super("x-EUC-TW", ExtendedCharsets.aliasesFor("x-EUC-TW"));
    }
    public String historicalName() {
        return "EUC_TW";
    }
    public boolean contains(Charset cs) {
        return ((cs.name().equals("US-ASCII"))
                || (cs instanceof EUC_TW));
    }
    public CharsetDecoder newDecoder() {
        return new Decoder(this);
    }
    public CharsetEncoder newEncoder() {
        return new Encoder(this);
    }
    public static class Decoder extends CharsetDecoder {
        public Decoder(Charset cs) {
            super(cs, 2.0f, 2.0f);
        }
        char[] c1 = new char[1];
        char[] c2 = new char[2];
        public char[] toUnicode(int b1, int b2, int p) {
            return decode(b1, b2, p, c1, c2);
        }
        static final String[] b2c =  EUC_TWMapping.b2c;
        static final int b1Min    =  EUC_TWMapping.b1Min;
        static final int b1Max    =  EUC_TWMapping.b1Max;
        static final int b2Min    =  EUC_TWMapping.b2Min;
        static final int b2Max    =  EUC_TWMapping.b2Max;
        static final int dbSegSize = b2Max - b2Min + 1;
        static final byte[] b2cIsSupp;
        static final byte[] cnspToIndex = new byte[0x100];
        static {
            Arrays.fill(cnspToIndex, (byte)-1);
            cnspToIndex[0xa2] = 1; cnspToIndex[0xa3] = 2; cnspToIndex[0xa4] = 3;
            cnspToIndex[0xa5] = 4; cnspToIndex[0xa6] = 5; cnspToIndex[0xa7] = 6;
            cnspToIndex[0xaf] = 7;
        }
        static {
            String b2cIsSuppStr = EUC_TWMapping.b2cIsSuppStr;
            byte[] flag = new byte[b2cIsSuppStr.length() << 1];
            int off = 0;
            for (int i = 0; i < b2cIsSuppStr.length(); i++) {
                char c = b2cIsSuppStr.charAt(i);
                flag[off++] = (byte)(c >> 8);
                flag[off++] = (byte)(c & 0xff);
            }
            b2cIsSupp = flag;
        }
        static boolean isLegalDB(int b) {
           return b >= b1Min && b <= b1Max;
        }
        static char[] decode(int b1, int b2, int p, char[] c1, char[] c2)
        {
            if (b1 < b1Min || b1 > b1Max || b2 < b2Min || b2 > b2Max)
                return null;
            int index = (b1 - b1Min) * dbSegSize + b2 - b2Min;
            char c = b2c[p].charAt(index);
            if (c == UNMAPPABLE_DECODING)
                return null;
            if ((b2cIsSupp[index] & (1 << p)) == 0) {
                c1[0] = c;
                return c1;
            } else {
                c2[0] = Character.highSurrogate(0x20000 + c);
                c2[1] = Character.lowSurrogate(0x20000 + c);
                return c2;
            }
        }
        private CoderResult decodeArrayLoop(ByteBuffer src,
                                            CharBuffer dst)
        {
            byte[] sa = src.array();
            int sp = src.arrayOffset() + src.position();
            int sl = src.arrayOffset() + src.limit();
            char[] da = dst.array();
            int dp = dst.arrayOffset() + dst.position();
            int dl = dst.arrayOffset() + dst.limit();
            try {
                while (sp < sl) {
                    int byte1 = sa[sp] & 0xff;
                    if (byte1 == SS2) { 
                        if ( sl - sp < 4)
                            return CoderResult.UNDERFLOW;
                        int cnsPlane = cnspToIndex[sa[sp + 1] & 0xff];
                        if (cnsPlane < 0)
                            return CoderResult.malformedForLength(2);
                        byte1 = sa[sp + 2] & 0xff;
                        int byte2 = sa[sp + 3] & 0xff;
                        char[] cc = toUnicode(byte1, byte2, cnsPlane);
                        if (cc == null) {
                            if (!isLegalDB(byte1) || !isLegalDB(byte2))
                                return CoderResult.malformedForLength(4);
                            return CoderResult.unmappableForLength(4);
                        }
                        if (dl - dp < cc.length)
                            return CoderResult.OVERFLOW;
                        if (cc.length == 1) {
                            da[dp++] = cc[0];
                        } else {
                            da[dp++] = cc[0];
                            da[dp++] = cc[1];
                        }
                        sp += 4;
                    } else if (byte1 < 0x80) {  
                        if (dl - dp < 1)
                           return CoderResult.OVERFLOW;
                        da[dp++] = (char) byte1;
                        sp++;
                    } else {                    
                        if ( sl - sp < 2)
                            return CoderResult.UNDERFLOW;
                        int byte2 = sa[sp + 1] & 0xff;
                        char[] cc = toUnicode(byte1, byte2, 0);
                        if (cc == null) {
                            if (!isLegalDB(byte1) || !isLegalDB(byte2))
                                return CoderResult.malformedForLength(1);
                            return CoderResult.unmappableForLength(2);
                        }
                        if (dl - dp < 1)
                            return CoderResult.OVERFLOW;
                        da[dp++] = cc[0];
                        sp += 2;
                    }
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
            try {
                while (src.hasRemaining()) {
                    int byte1 = src.get() & 0xff;
                    if (byte1 == SS2) {            
                        if ( src.remaining() < 3)
                            return CoderResult.UNDERFLOW;
                        int cnsPlane = cnspToIndex[src.get() & 0xff];
                        if (cnsPlane < 0)
                            return CoderResult.malformedForLength(2);
                        byte1 = src.get() & 0xff;
                        int byte2 = src.get() & 0xff;
                        char[] cc = toUnicode(byte1, byte2, cnsPlane);
                        if (cc == null) {
                            if (!isLegalDB(byte1) || !isLegalDB(byte2))
                                return CoderResult.malformedForLength(4);
                            return CoderResult.unmappableForLength(4);
                        }
                        if (dst.remaining() < cc.length)
                            return CoderResult.OVERFLOW;
                        if (cc.length == 1) {
                            dst.put(cc[0]);
                        } else {
                            dst.put(cc[0]);
                            dst.put(cc[1]);
                        }
                        mark += 4;
                    } else if (byte1 < 0x80) {        
                        if (!dst.hasRemaining())
                           return CoderResult.OVERFLOW;
                        dst.put((char) byte1);
                        mark++;
                    } else {                          
                        if (!src.hasRemaining())
                            return CoderResult.UNDERFLOW;
                        int byte2 = src.get() & 0xff;
                        char[] cc = toUnicode(byte1, byte2, 0);
                        if (cc == null) {
                            if (!isLegalDB(byte1) || !isLegalDB(byte2))
                                return CoderResult.malformedForLength(1);
                            return CoderResult.unmappableForLength(2);
                        }
                        if (!dst.hasRemaining())
                            return CoderResult.OVERFLOW;
                        dst.put(cc[0]);
                        mark +=2;
                    }
               }
               return CoderResult.UNDERFLOW;
            } finally {
                src.position(mark);
            }
        }
        protected CoderResult decodeLoop(ByteBuffer src, CharBuffer dst)
        {
            if (src.hasArray() && dst.hasArray())
                return decodeArrayLoop(src, dst);
            else
                return decodeBufferLoop(src, dst);
        }
    }
    public static class Encoder extends CharsetEncoder {
        private byte[] bb = new byte[4];
        public Encoder(Charset cs) {
            super(cs, 4.0f, 4.0f);
        }
        public boolean canEncode(char c) {
            return (c <= '\u007f' || toEUC(c, bb) != -1);
        }
        public boolean canEncode(CharSequence cs) {
            int i = 0;
            while (i < cs.length()) {
                char c = cs.charAt(i++);
                if (Character.isHighSurrogate(c)) {
                    if (i == cs.length())
                        return false;
                    char low = cs.charAt(i++);
                    if (!Character.isLowSurrogate(low) || toEUC(c, low, bb) == -1)
                        return false;
                } else if (!canEncode(c)) {
                    return false;
                }
            }
            return true;
        }
        public int toEUC(char hi, char low, byte[] bb) {
            return encode(hi, low, bb);
        }
        public int toEUC(char c, byte[] bb) {
            return encode(c, bb);
        }
        private CoderResult encodeArrayLoop(CharBuffer src,
                                            ByteBuffer dst)
        {
            char[] sa = src.array();
            int sp = src.arrayOffset() + src.position();
            int sl = src.arrayOffset() + src.limit();
            byte[] da = dst.array();
            int dp = dst.arrayOffset() + dst.position();
            int dl = dst.arrayOffset() + dst.limit();
            int inSize;
            int outSize;
            try {
                while (sp < sl) {
                    char c = sa[sp];
                    inSize = 1;
                    if (c < 0x80) {  
                        bb[0] = (byte)c;
                        outSize = 1;
                    } else {
                        outSize = toEUC(c, bb);
                        if (outSize == -1) {
                            if (Character.isHighSurrogate(c)) {
                                if ((sp + 1) == sl)
                                    return CoderResult.UNDERFLOW;
                                if (!Character.isLowSurrogate(sa[sp + 1]))
                                    return CoderResult.malformedForLength(1);
                                outSize = toEUC(c, sa[sp+1], bb);
                                    inSize = 2;
                            } else if (Character.isLowSurrogate(c)) {
                                return CoderResult.malformedForLength(1);
                            }
                        }
                    }
                    if (outSize == -1)
                        return CoderResult.unmappableForLength(inSize);
                    if ( dl - dp < outSize)
                        return CoderResult.OVERFLOW;
                    for (int i = 0; i < outSize; i++)
                        da[dp++] = bb[i];
                    sp  += inSize;
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
            int outSize;
            int inSize;
            int mark = src.position();
            try {
                while (src.hasRemaining()) {
                    inSize = 1;
                    char c = src.get();
                    if (c < 0x80) {   
                        outSize = 1;
                        bb[0] = (byte)c;
                    } else {
                        outSize = toEUC(c, bb);
                        if (outSize == -1) {
                            if (Character.isHighSurrogate(c)) {
                                if (!src.hasRemaining())
                                    return CoderResult.UNDERFLOW;
                                char c2 = src.get();
                                if (!Character.isLowSurrogate(c2))
                                    return CoderResult.malformedForLength(1);
                                outSize = toEUC(c, c2, bb);
                                inSize = 2;
                            } else if (Character.isLowSurrogate(c)) {
                                return CoderResult.malformedForLength(1);
                            }
                        }
                    }
                    if (outSize == -1)
                        return CoderResult.unmappableForLength(inSize);
                    if (dst.remaining() < outSize)
                        return CoderResult.OVERFLOW;
                    for (int i = 0; i < outSize; i++)
                        dst.put(bb[i]);
                    mark += inSize;
                }
                return CoderResult.UNDERFLOW;
            } finally {
                src.position(mark);
            }
        }
        protected CoderResult encodeLoop(CharBuffer src, ByteBuffer dst)
        {
            if (src.hasArray() && dst.hasArray())
                return encodeArrayLoop(src, dst);
            else
                return encodeBufferLoop(src, dst);
        }
        static int encode(char hi, char low, byte[] bb) {
            int c = Character.toCodePoint(hi, low);
            if ((c & 0xf0000) != 0x20000)
                return -1;
            c -= 0x20000;
            int index = c2bSuppIndex[c >> 8];
            if (index  == UNMAPPABLE_ENCODING)
                return -1;
            index = index + (c & 0xff);
            int db = c2bSupp[index];
            if (db == UNMAPPABLE_ENCODING)
                return -1;
            int p = (c2bPlane[index] >> 4) & 0xf;
            bb[0] = (byte)SS2;
            bb[1] = (byte)(0xa0 | p);
            bb[2] = (byte)(db >> 8);
            bb[3] = (byte)db;
            return 4;
        }
        static int encode(char c, byte[] bb) {
            int index = c2bIndex[c >> 8];
            if (index  == UNMAPPABLE_ENCODING)
                return -1;
            index = index + (c & 0xff);
            int db = c2b[index];
            if (db == UNMAPPABLE_ENCODING)
                return -1;
            int p = c2bPlane[index] & 0xf;
            if (p == 0) {
                bb[0] = (byte)(db >> 8);
                bb[1] = (byte)db;
                return 2;
            } else {
                bb[0] = (byte)SS2;
                bb[1] = (byte)(0xa0 | p);
                bb[2] = (byte)(db >> 8);
                bb[3] = (byte)db;
                return 4;
            }
        }
        static final char[] c2b;
        static final char[] c2bIndex;
        static final char[] c2bSupp;
        static final char[] c2bSuppIndex;
        static final byte[] c2bPlane;
        static {
            int b1Min    =  Decoder.b1Min;
            int b1Max    =  Decoder.b1Max;
            int b2Min    =  Decoder.b2Min;
            int b2Max    =  Decoder.b2Max;
            int dbSegSize = Decoder.dbSegSize;
            String[] b2c = Decoder.b2c;
            byte[] b2cIsSupp = Decoder.b2cIsSupp;
            c2bIndex = EUC_TWMapping.c2bIndex;
            c2bSuppIndex = EUC_TWMapping.c2bSuppIndex;
            char[] c2b0 = new char[EUC_TWMapping.C2BSIZE];
            char[] c2bSupp0 = new char[EUC_TWMapping.C2BSUPPSIZE];
            byte[] c2bPlane0 = new byte[Math.max(EUC_TWMapping.C2BSIZE,
                                                 EUC_TWMapping.C2BSUPPSIZE)];
            Arrays.fill(c2b0, (char)UNMAPPABLE_ENCODING);
            Arrays.fill(c2bSupp0, (char)UNMAPPABLE_ENCODING);
            for (int p = 0; p < b2c.length; p++) {
                String db = b2c[p];
                int plane = p;
                if (plane == 7)
                    plane = 0xf;
                else if (plane != 0)
                    plane = p + 1;
                int off = 0;
                for (int b1 = b1Min; b1 <= b1Max; b1++) {
                    for (int b2 = b2Min; b2 <= b2Max; b2++) {
                        char c = db.charAt(off);
                        if (c != UNMAPPABLE_DECODING) {
                            if ((b2cIsSupp[off] & (1 << p)) != 0) {
                                int index = c2bSuppIndex[c >> 8] + (c&0xff);
                                c2bSupp0[index] = (char)((b1 << 8) + b2);
                                c2bPlane0[index] |= (byte)(plane << 4);
                            } else {
                                int index = c2bIndex[c >> 8] + (c&0xff);
                                c2b0[index] = (char)((b1 << 8) + b2);
                                c2bPlane0[index] |= (byte)plane;
                            }
                        }
                        off++;
                    }
                }
            }
            c2b = c2b0;
            c2bSupp = c2bSupp0;
            c2bPlane = c2bPlane0;
        }
    }
}
