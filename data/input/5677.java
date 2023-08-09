public class JISAutoDetect
    extends Charset
    implements HistoricallyNamedCharset
{
    private final static int EUCJP_MASK       = 0x01;
    private final static int SJIS2B_MASK      = 0x02;
    private final static int SJIS1B_MASK      = 0x04;
    private final static int EUCJP_KANA1_MASK = 0x08;
    private final static int EUCJP_KANA2_MASK = 0x10;
    public JISAutoDetect() {
        super("x-JISAutoDetect", ExtendedCharsets.aliasesFor("x-JISAutoDetect"));
    }
    public boolean contains(Charset cs) {
        return ((cs.name().equals("US-ASCII"))
                || (cs instanceof SJIS)
                || (cs instanceof EUC_JP)
                || (cs instanceof ISO2022_JP));
    }
    public boolean canEncode() {
        return false;
    }
    public CharsetDecoder newDecoder() {
        return new Decoder(this);
    }
    public String historicalName() {
        return "JISAutoDetect";
    }
    public CharsetEncoder newEncoder() {
        throw new UnsupportedOperationException();
    }
    public static byte[] getByteMask1() {
        return Decoder.maskTable1;
    }
    public static byte[] getByteMask2() {
        return Decoder.maskTable2;
    }
    public final static boolean canBeSJIS1B(int mask) {
        return (mask & SJIS1B_MASK) != 0;
    }
    public final static boolean canBeEUCJP(int mask) {
        return (mask & EUCJP_MASK) != 0;
    }
    public final static boolean canBeEUCKana(int mask1, int mask2) {
        return ((mask1 & EUCJP_KANA1_MASK) != 0)
            && ((mask2 & EUCJP_KANA2_MASK) != 0);
    }
    private static boolean looksLikeJapanese(CharBuffer cb) {
        int hiragana = 0;       
        int katakana = 0;       
        while (cb.hasRemaining()) {
            char c = cb.get();
            if (0x3040 <= c && c <= 0x309f && ++hiragana > 1) return true;
            if (0xff65 <= c && c <= 0xff9f && ++katakana > 1) return true;
        }
        return false;
    }
    private static class Decoder extends CharsetDecoder {
        private final static String SJISName = getSJISName();
        private final static String EUCJPName = getEUCJPName();
        private DelegatableDecoder detectedDecoder = null;
        public Decoder(Charset cs) {
            super(cs, 0.5f, 1.0f);
        }
        private static boolean isPlainASCII(byte b) {
            return b >= 0 && b != 0x1b;
        }
        private static void copyLeadingASCII(ByteBuffer src, CharBuffer dst) {
            int start = src.position();
            int limit = start + Math.min(src.remaining(), dst.remaining());
            int p;
            byte b;
            for (p = start; p < limit && isPlainASCII(b = src.get(p)); p++)
                dst.put((char)(b & 0xff));
            src.position(p);
        }
        private CoderResult decodeLoop(Charset cs,
                                       ByteBuffer src, CharBuffer dst) {
            detectedDecoder = (DelegatableDecoder) cs.newDecoder();
            return detectedDecoder.decodeLoop(src, dst);
        }
        protected CoderResult decodeLoop(ByteBuffer src, CharBuffer dst) {
            if (detectedDecoder == null) {
                copyLeadingASCII(src, dst);
                if (! src.hasRemaining())
                    return CoderResult.UNDERFLOW;
                if (! dst.hasRemaining())
                    return CoderResult.OVERFLOW;
                int cbufsiz = (int)(src.limit() * (double)maxCharsPerByte());
                CharBuffer sandbox = CharBuffer.allocate(cbufsiz);
                Charset cs2022 = Charset.forName("ISO-2022-JP");
                DelegatableDecoder dd2022
                    = (DelegatableDecoder) cs2022.newDecoder();
                ByteBuffer src2022 = src.asReadOnlyBuffer();
                CoderResult res2022 = dd2022.decodeLoop(src2022, sandbox);
                if (! res2022.isError())
                    return decodeLoop(cs2022, src, dst);
                Charset csEUCJ = Charset.forName(EUCJPName);
                Charset csSJIS = Charset.forName(SJISName);
                DelegatableDecoder ddEUCJ
                    = (DelegatableDecoder) csEUCJ.newDecoder();
                ByteBuffer srcEUCJ = src.asReadOnlyBuffer();
                sandbox.clear();
                CoderResult resEUCJ = ddEUCJ.decodeLoop(srcEUCJ, sandbox);
                if (resEUCJ.isError())
                    return decodeLoop(csSJIS, src, dst);
                DelegatableDecoder ddSJIS
                    = (DelegatableDecoder) csSJIS.newDecoder();
                ByteBuffer srcSJIS = src.asReadOnlyBuffer();
                CharBuffer sandboxSJIS = CharBuffer.allocate(cbufsiz);
                CoderResult resSJIS = ddSJIS.decodeLoop(srcSJIS, sandboxSJIS);
                if (resSJIS.isError())
                    return decodeLoop(csEUCJ, src, dst);
                if (srcEUCJ.position() > srcSJIS.position())
                    return decodeLoop(csEUCJ, src, dst);
                if (srcEUCJ.position() < srcSJIS.position())
                    return decodeLoop(csSJIS, src, dst);
                if (src.position() == srcEUCJ.position())
                    return CoderResult.UNDERFLOW;
                sandbox.flip();
                Charset guess = looksLikeJapanese(sandbox) ? csEUCJ : csSJIS;
                return decodeLoop(guess, src, dst);
            }
            return detectedDecoder.decodeLoop(src, dst);
        }
        protected void implReset() {
            detectedDecoder = null;
        }
        protected CoderResult implFlush(CharBuffer out) {
            if (detectedDecoder != null)
                return detectedDecoder.implFlush(out);
            else
                return super.implFlush(out);
        }
        public boolean isAutoDetecting() {
            return true;
        }
        public boolean isCharsetDetected() {
            return detectedDecoder != null;
        }
        public Charset detectedCharset() {
            if (detectedDecoder == null)
                throw new IllegalStateException("charset not yet detected");
            return ((CharsetDecoder) detectedDecoder).charset();
        }
        private static String getSJISName() {
            String osName = AccessController.doPrivileged(
                new GetPropertyAction("os.name"));
            if (osName.equals("Solaris") || osName.equals("SunOS"))
                return("PCK");
            else if (osName.startsWith("Windows"))
                return("windows-31J");
            else
                return("Shift_JIS");
        }
        private static String getEUCJPName() {
            String osName = AccessController.doPrivileged(
                new GetPropertyAction("os.name"));
            if (osName.equals("Solaris") || osName.equals("SunOS"))
                return("x-eucjp-open");
            else
                return("EUC_JP");
        }
        private static final byte maskTable1[] = {
            0, 0, 0, 0, 
            0, 0, 0, 0, 
            0, 0, 0, 0, 
            0, 0, 0, 0, 
            0, 0, 0, 0, 
            0, 0, 0, 0, 
            0, 0, 0, 0, 
            0, 0, 0, 0, 
            0, 0, 0, 0, 
            0, 0, 0, 0, 
            0, 0, 0, 0, 
            0, 0, 0, 0, 
            0, 0, 0, 0, 
            0, 0, 0, 0, 
            0, 0, 0, 0, 
            0, 0, 0, 0, 
            0, 0, 0, 0, 
            0, 0, 0, 0, 
            0, 0, 0, 0, 
            0, 0, 0, 0, 
            0, 0, 0, 0, 
            0, 0, 0, 0, 
            0, 0, 0, 0, 
            0, 0, 0, 0, 
            0, 0, 0, 0, 
            0, 0, 0, 0, 
            0, 0, 0, 0, 
            0, 0, 0, 0, 
            0, 0, 0, 0, 
            0, 0, 0, 0, 
            0, 0, 0, 0, 
            0, 0, 0, 0, 
            0, SJIS2B_MASK, SJIS2B_MASK, SJIS2B_MASK,   
            SJIS2B_MASK, SJIS2B_MASK, SJIS2B_MASK, SJIS2B_MASK, 
            SJIS2B_MASK, SJIS2B_MASK, SJIS2B_MASK, SJIS2B_MASK, 
            SJIS2B_MASK, SJIS2B_MASK, SJIS2B_MASK|EUCJP_MASK, SJIS2B_MASK|EUCJP_MASK,   
            SJIS2B_MASK, SJIS2B_MASK, SJIS2B_MASK, SJIS2B_MASK, 
            SJIS2B_MASK, SJIS2B_MASK, SJIS2B_MASK, SJIS2B_MASK, 
            SJIS2B_MASK, SJIS2B_MASK, SJIS2B_MASK, SJIS2B_MASK, 
            SJIS2B_MASK, SJIS2B_MASK, SJIS2B_MASK, SJIS2B_MASK, 
            0, SJIS1B_MASK|EUCJP_MASK, SJIS1B_MASK|EUCJP_MASK, SJIS1B_MASK|EUCJP_MASK,  
            SJIS1B_MASK|EUCJP_MASK|EUCJP_KANA1_MASK, SJIS1B_MASK|EUCJP_MASK, SJIS1B_MASK|EUCJP_MASK, SJIS1B_MASK|EUCJP_MASK,    
            SJIS1B_MASK|EUCJP_MASK, SJIS1B_MASK|EUCJP_MASK, SJIS1B_MASK|EUCJP_MASK, SJIS1B_MASK|EUCJP_MASK,     
            SJIS1B_MASK|EUCJP_MASK, SJIS1B_MASK|EUCJP_MASK, SJIS1B_MASK|EUCJP_MASK, SJIS1B_MASK|EUCJP_MASK,     
            SJIS1B_MASK|EUCJP_MASK, SJIS1B_MASK|EUCJP_MASK, SJIS1B_MASK|EUCJP_MASK, SJIS1B_MASK|EUCJP_MASK,     
            SJIS1B_MASK|EUCJP_MASK, SJIS1B_MASK|EUCJP_MASK, SJIS1B_MASK|EUCJP_MASK, SJIS1B_MASK|EUCJP_MASK,     
            SJIS1B_MASK|EUCJP_MASK, SJIS1B_MASK|EUCJP_MASK, SJIS1B_MASK|EUCJP_MASK, SJIS1B_MASK|EUCJP_MASK,     
            SJIS1B_MASK|EUCJP_MASK, SJIS1B_MASK|EUCJP_MASK, SJIS1B_MASK|EUCJP_MASK, SJIS1B_MASK|EUCJP_MASK,     
            SJIS1B_MASK|EUCJP_MASK, SJIS1B_MASK|EUCJP_MASK, SJIS1B_MASK|EUCJP_MASK, SJIS1B_MASK|EUCJP_MASK,     
            SJIS1B_MASK|EUCJP_MASK, SJIS1B_MASK|EUCJP_MASK, SJIS1B_MASK|EUCJP_MASK, SJIS1B_MASK|EUCJP_MASK,     
            SJIS1B_MASK|EUCJP_MASK, SJIS1B_MASK|EUCJP_MASK, SJIS1B_MASK|EUCJP_MASK, SJIS1B_MASK|EUCJP_MASK,     
            SJIS1B_MASK|EUCJP_MASK, SJIS1B_MASK|EUCJP_MASK, SJIS1B_MASK|EUCJP_MASK, SJIS1B_MASK|EUCJP_MASK,     
            SJIS1B_MASK|EUCJP_MASK, SJIS1B_MASK|EUCJP_MASK, SJIS1B_MASK|EUCJP_MASK, SJIS1B_MASK|EUCJP_MASK,     
            SJIS1B_MASK|EUCJP_MASK, SJIS1B_MASK|EUCJP_MASK, SJIS1B_MASK|EUCJP_MASK, SJIS1B_MASK|EUCJP_MASK,     
            SJIS1B_MASK|EUCJP_MASK, SJIS1B_MASK|EUCJP_MASK, SJIS1B_MASK|EUCJP_MASK, SJIS1B_MASK|EUCJP_MASK,     
            SJIS1B_MASK|EUCJP_MASK, SJIS1B_MASK|EUCJP_MASK, SJIS1B_MASK|EUCJP_MASK, SJIS1B_MASK|EUCJP_MASK,     
            SJIS2B_MASK|EUCJP_MASK, SJIS2B_MASK|EUCJP_MASK, SJIS2B_MASK|EUCJP_MASK, SJIS2B_MASK|EUCJP_MASK,     
            SJIS2B_MASK|EUCJP_MASK, SJIS2B_MASK|EUCJP_MASK, SJIS2B_MASK|EUCJP_MASK, SJIS2B_MASK|EUCJP_MASK,     
            SJIS2B_MASK|EUCJP_MASK, SJIS2B_MASK|EUCJP_MASK, SJIS2B_MASK|EUCJP_MASK, SJIS2B_MASK|EUCJP_MASK,     
            SJIS2B_MASK|EUCJP_MASK, SJIS2B_MASK|EUCJP_MASK, SJIS2B_MASK|EUCJP_MASK, SJIS2B_MASK|EUCJP_MASK,     
            SJIS2B_MASK|EUCJP_MASK, SJIS2B_MASK|EUCJP_MASK, SJIS2B_MASK|EUCJP_MASK, SJIS2B_MASK|EUCJP_MASK,     
            SJIS2B_MASK|EUCJP_MASK, SJIS2B_MASK|EUCJP_MASK, SJIS2B_MASK|EUCJP_MASK, SJIS2B_MASK|EUCJP_MASK,     
            SJIS2B_MASK|EUCJP_MASK, SJIS2B_MASK|EUCJP_MASK, SJIS2B_MASK|EUCJP_MASK, SJIS2B_MASK|EUCJP_MASK,     
            SJIS2B_MASK|EUCJP_MASK, EUCJP_MASK, EUCJP_MASK, 0   
        };
        private static final byte maskTable2[] = {
            0, 0, 0, 0, 
            0, 0, 0, 0, 
            0, 0, 0, 0, 
            0, 0, 0, 0, 
            0, 0, 0, 0, 
            0, 0, 0, 0, 
            0, 0, 0, 0, 
            0, 0, 0, 0, 
            0, 0, 0, 0, 
            0, 0, 0, 0, 
            0, 0, 0, 0, 
            0, 0, 0, 0, 
            0, 0, 0, 0, 
            0, 0, 0, 0, 
            0, 0, 0, 0, 
            0, 0, 0, 0, 
            SJIS2B_MASK, SJIS2B_MASK, SJIS2B_MASK, SJIS2B_MASK, 
            SJIS2B_MASK, SJIS2B_MASK, SJIS2B_MASK, SJIS2B_MASK, 
            SJIS2B_MASK, SJIS2B_MASK, SJIS2B_MASK, SJIS2B_MASK, 
            SJIS2B_MASK, SJIS2B_MASK, SJIS2B_MASK, SJIS2B_MASK, 
            SJIS2B_MASK, SJIS2B_MASK, SJIS2B_MASK, SJIS2B_MASK, 
            SJIS2B_MASK, SJIS2B_MASK, SJIS2B_MASK, SJIS2B_MASK, 
            SJIS2B_MASK, SJIS2B_MASK, SJIS2B_MASK, SJIS2B_MASK, 
            SJIS2B_MASK, SJIS2B_MASK, SJIS2B_MASK, SJIS2B_MASK, 
            SJIS2B_MASK, SJIS2B_MASK, SJIS2B_MASK, SJIS2B_MASK, 
            SJIS2B_MASK, SJIS2B_MASK, SJIS2B_MASK, SJIS2B_MASK, 
            SJIS2B_MASK, SJIS2B_MASK, SJIS2B_MASK, SJIS2B_MASK, 
            SJIS2B_MASK, SJIS2B_MASK, SJIS2B_MASK, SJIS2B_MASK, 
            SJIS2B_MASK, SJIS2B_MASK, SJIS2B_MASK, SJIS2B_MASK, 
            SJIS2B_MASK, SJIS2B_MASK, SJIS2B_MASK, SJIS2B_MASK, 
            SJIS2B_MASK, SJIS2B_MASK, SJIS2B_MASK, SJIS2B_MASK, 
            SJIS2B_MASK, SJIS2B_MASK, SJIS2B_MASK, 0,   
            SJIS2B_MASK, SJIS2B_MASK, SJIS2B_MASK, SJIS2B_MASK, 
            SJIS2B_MASK, SJIS2B_MASK, SJIS2B_MASK, SJIS2B_MASK, 
            SJIS2B_MASK, SJIS2B_MASK, SJIS2B_MASK, SJIS2B_MASK, 
            SJIS2B_MASK, SJIS2B_MASK, SJIS2B_MASK, SJIS2B_MASK, 
            SJIS2B_MASK, SJIS2B_MASK, SJIS2B_MASK, SJIS2B_MASK, 
            SJIS2B_MASK, SJIS2B_MASK, SJIS2B_MASK, SJIS2B_MASK, 
            SJIS2B_MASK, SJIS2B_MASK, SJIS2B_MASK, SJIS2B_MASK, 
            SJIS2B_MASK, SJIS2B_MASK, SJIS2B_MASK, SJIS2B_MASK, 
            SJIS2B_MASK, SJIS1B_MASK|SJIS2B_MASK|EUCJP_MASK|EUCJP_KANA2_MASK, SJIS1B_MASK|SJIS2B_MASK|EUCJP_MASK|EUCJP_KANA2_MASK, SJIS1B_MASK|SJIS2B_MASK|EUCJP_MASK|EUCJP_KANA2_MASK, 
            SJIS1B_MASK|SJIS2B_MASK|EUCJP_MASK|EUCJP_KANA2_MASK, SJIS1B_MASK|SJIS2B_MASK|EUCJP_MASK|EUCJP_KANA2_MASK, SJIS1B_MASK|SJIS2B_MASK|EUCJP_MASK|EUCJP_KANA2_MASK, SJIS1B_MASK|SJIS2B_MASK|EUCJP_MASK|EUCJP_KANA2_MASK, 
            SJIS1B_MASK|SJIS2B_MASK|EUCJP_MASK|EUCJP_KANA2_MASK, SJIS1B_MASK|SJIS2B_MASK|EUCJP_MASK|EUCJP_KANA2_MASK, SJIS1B_MASK|SJIS2B_MASK|EUCJP_MASK|EUCJP_KANA2_MASK, SJIS1B_MASK|SJIS2B_MASK|EUCJP_MASK|EUCJP_KANA2_MASK, 
            SJIS1B_MASK|SJIS2B_MASK|EUCJP_MASK|EUCJP_KANA2_MASK, SJIS1B_MASK|SJIS2B_MASK|EUCJP_MASK|EUCJP_KANA2_MASK, SJIS1B_MASK|SJIS2B_MASK|EUCJP_MASK|EUCJP_KANA2_MASK, SJIS1B_MASK|SJIS2B_MASK|EUCJP_MASK|EUCJP_KANA2_MASK, 
            SJIS1B_MASK|SJIS2B_MASK|EUCJP_MASK|EUCJP_KANA2_MASK, SJIS1B_MASK|SJIS2B_MASK|EUCJP_MASK|EUCJP_KANA2_MASK, SJIS1B_MASK|SJIS2B_MASK|EUCJP_MASK|EUCJP_KANA2_MASK, SJIS1B_MASK|SJIS2B_MASK|EUCJP_MASK|EUCJP_KANA2_MASK, 
            SJIS1B_MASK|SJIS2B_MASK|EUCJP_MASK|EUCJP_KANA2_MASK, SJIS1B_MASK|SJIS2B_MASK|EUCJP_MASK|EUCJP_KANA2_MASK, SJIS1B_MASK|SJIS2B_MASK|EUCJP_MASK|EUCJP_KANA2_MASK, SJIS1B_MASK|SJIS2B_MASK|EUCJP_MASK|EUCJP_KANA2_MASK, 
            SJIS1B_MASK|SJIS2B_MASK|EUCJP_MASK|EUCJP_KANA2_MASK, SJIS1B_MASK|SJIS2B_MASK|EUCJP_MASK|EUCJP_KANA2_MASK, SJIS1B_MASK|SJIS2B_MASK|EUCJP_MASK|EUCJP_KANA2_MASK, SJIS1B_MASK|SJIS2B_MASK|EUCJP_MASK|EUCJP_KANA2_MASK, 
            SJIS1B_MASK|SJIS2B_MASK|EUCJP_MASK|EUCJP_KANA2_MASK, SJIS1B_MASK|SJIS2B_MASK|EUCJP_MASK|EUCJP_KANA2_MASK, SJIS1B_MASK|SJIS2B_MASK|EUCJP_MASK|EUCJP_KANA2_MASK, SJIS1B_MASK|SJIS2B_MASK|EUCJP_MASK|EUCJP_KANA2_MASK, 
            SJIS1B_MASK|SJIS2B_MASK|EUCJP_MASK|EUCJP_KANA2_MASK, SJIS1B_MASK|SJIS2B_MASK|EUCJP_MASK|EUCJP_KANA2_MASK, SJIS1B_MASK|SJIS2B_MASK|EUCJP_MASK|EUCJP_KANA2_MASK, SJIS1B_MASK|SJIS2B_MASK|EUCJP_MASK|EUCJP_KANA2_MASK, 
            SJIS1B_MASK|SJIS2B_MASK|EUCJP_MASK|EUCJP_KANA2_MASK, SJIS1B_MASK|SJIS2B_MASK|EUCJP_MASK|EUCJP_KANA2_MASK, SJIS1B_MASK|SJIS2B_MASK|EUCJP_MASK|EUCJP_KANA2_MASK, SJIS1B_MASK|SJIS2B_MASK|EUCJP_MASK|EUCJP_KANA2_MASK, 
            SJIS1B_MASK|SJIS2B_MASK|EUCJP_MASK|EUCJP_KANA2_MASK, SJIS1B_MASK|SJIS2B_MASK|EUCJP_MASK|EUCJP_KANA2_MASK, SJIS1B_MASK|SJIS2B_MASK|EUCJP_MASK|EUCJP_KANA2_MASK, SJIS1B_MASK|SJIS2B_MASK|EUCJP_MASK|EUCJP_KANA2_MASK, 
            SJIS1B_MASK|SJIS2B_MASK|EUCJP_MASK|EUCJP_KANA2_MASK, SJIS1B_MASK|SJIS2B_MASK|EUCJP_MASK|EUCJP_KANA2_MASK, SJIS1B_MASK|SJIS2B_MASK|EUCJP_MASK|EUCJP_KANA2_MASK, SJIS1B_MASK|SJIS2B_MASK|EUCJP_MASK|EUCJP_KANA2_MASK, 
            SJIS1B_MASK|SJIS2B_MASK|EUCJP_MASK|EUCJP_KANA2_MASK, SJIS1B_MASK|SJIS2B_MASK|EUCJP_MASK|EUCJP_KANA2_MASK, SJIS1B_MASK|SJIS2B_MASK|EUCJP_MASK|EUCJP_KANA2_MASK, SJIS1B_MASK|SJIS2B_MASK|EUCJP_MASK|EUCJP_KANA2_MASK, 
            SJIS1B_MASK|SJIS2B_MASK|EUCJP_MASK|EUCJP_KANA2_MASK, SJIS1B_MASK|SJIS2B_MASK|EUCJP_MASK|EUCJP_KANA2_MASK, SJIS1B_MASK|SJIS2B_MASK|EUCJP_MASK|EUCJP_KANA2_MASK, SJIS1B_MASK|SJIS2B_MASK|EUCJP_MASK|EUCJP_KANA2_MASK, 
            SJIS1B_MASK|SJIS2B_MASK|EUCJP_MASK|EUCJP_KANA2_MASK, SJIS1B_MASK|SJIS2B_MASK|EUCJP_MASK|EUCJP_KANA2_MASK, SJIS1B_MASK|SJIS2B_MASK|EUCJP_MASK|EUCJP_KANA2_MASK, SJIS1B_MASK|SJIS2B_MASK|EUCJP_MASK|EUCJP_KANA2_MASK, 
            SJIS1B_MASK|SJIS2B_MASK|EUCJP_MASK|EUCJP_KANA2_MASK, SJIS1B_MASK|SJIS2B_MASK|EUCJP_MASK|EUCJP_KANA2_MASK, SJIS1B_MASK|SJIS2B_MASK|EUCJP_MASK|EUCJP_KANA2_MASK, SJIS1B_MASK|SJIS2B_MASK|EUCJP_MASK|EUCJP_KANA2_MASK, 
            SJIS2B_MASK|EUCJP_MASK|EUCJP_KANA2_MASK, SJIS2B_MASK|EUCJP_MASK|EUCJP_KANA2_MASK, SJIS2B_MASK|EUCJP_MASK|EUCJP_KANA2_MASK, SJIS2B_MASK|EUCJP_MASK|EUCJP_KANA2_MASK, 
            SJIS2B_MASK|EUCJP_MASK|EUCJP_KANA2_MASK, SJIS2B_MASK|EUCJP_MASK|EUCJP_KANA2_MASK, SJIS2B_MASK|EUCJP_MASK|EUCJP_KANA2_MASK, SJIS2B_MASK|EUCJP_MASK|EUCJP_KANA2_MASK, 
            SJIS2B_MASK|EUCJP_MASK|EUCJP_KANA2_MASK, SJIS2B_MASK|EUCJP_MASK|EUCJP_KANA2_MASK, SJIS2B_MASK|EUCJP_MASK|EUCJP_KANA2_MASK, SJIS2B_MASK|EUCJP_MASK|EUCJP_KANA2_MASK, 
            SJIS2B_MASK|EUCJP_MASK|EUCJP_KANA2_MASK, SJIS2B_MASK|EUCJP_MASK|EUCJP_KANA2_MASK, SJIS2B_MASK|EUCJP_MASK|EUCJP_KANA2_MASK, SJIS2B_MASK|EUCJP_MASK|EUCJP_KANA2_MASK, 
            SJIS2B_MASK|EUCJP_MASK|EUCJP_KANA2_MASK, SJIS2B_MASK|EUCJP_MASK|EUCJP_KANA2_MASK, SJIS2B_MASK|EUCJP_MASK|EUCJP_KANA2_MASK, SJIS2B_MASK|EUCJP_MASK|EUCJP_KANA2_MASK, 
            SJIS2B_MASK|EUCJP_MASK, SJIS2B_MASK|EUCJP_MASK, SJIS2B_MASK|EUCJP_MASK, SJIS2B_MASK|EUCJP_MASK,     
            SJIS2B_MASK|EUCJP_MASK, SJIS2B_MASK|EUCJP_MASK, SJIS2B_MASK|EUCJP_MASK, SJIS2B_MASK|EUCJP_MASK,     
            SJIS2B_MASK|EUCJP_MASK, EUCJP_MASK, EUCJP_MASK, 0   
        };
    }
}
