public class WingDings extends Charset {
    public WingDings () {
        super("WingDings", null);
    }
    public CharsetEncoder newEncoder() {
        return new Encoder(this);
    }
    public CharsetDecoder newDecoder() {
        throw new Error("Decoder isn't implemented for WingDings Charset");
    }
    public boolean contains(Charset cs) {
        return cs instanceof WingDings;
    }
    private static class Encoder extends CharsetEncoder {
        public Encoder(Charset cs) {
            super(cs, 1.0f, 1.0f);
        }
        public boolean canEncode(char c) {
            if(c >= 0x2701 && c <= 0x27be){
                if (table[c - 0x2700] != 0x00)
                    return true;
                else
                    return false;
            }
            return false;
        }
        protected CoderResult encodeLoop(CharBuffer src, ByteBuffer dst) {
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
                    if (dl - dp < 1)
                        return CoderResult.OVERFLOW;
                    if (!canEncode(c))
                        return CoderResult.unmappableForLength(1);
                    sp++;
                    da[dp++] = table[c - 0x2700];
                }
                return CoderResult.UNDERFLOW;
            } finally {
                src.position(sp - src.arrayOffset());
                dst.position(dp - dst.arrayOffset());
            }
        }
        private static byte[] table = {
            (byte)0x00, (byte)0x23, (byte)0x22, (byte)0x00,  
            (byte)0x00, (byte)0x00, (byte)0x29, (byte)0x3e,  
            (byte)0x51, (byte)0x2a, (byte)0x00, (byte)0x00,  
            (byte)0x41, (byte)0x3f, (byte)0x00, (byte)0x00,  
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0xfc,  
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0xfb,  
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,  
            (byte)0x00, (byte)0x00, (byte)0x56, (byte)0x00,  
            (byte)0x58, (byte)0x59, (byte)0x00, (byte)0x00,  
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,  
            (byte)0x00, (byte)0x00, (byte)0xb5, (byte)0x00,  
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,  
            (byte)0xb6, (byte)0x00, (byte)0x00, (byte)0x00,  
            (byte)0xad, (byte)0xaf, (byte)0xac, (byte)0x00,  
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,  
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x7c,  
            (byte)0x7b, (byte)0x00, (byte)0x00, (byte)0x00,  
            (byte)0x54, (byte)0x00, (byte)0x00, (byte)0x00,  
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,  
            (byte)0x00, (byte)0xa6, (byte)0x00, (byte)0x00,  
            (byte)0x00, (byte)0x71, (byte)0x72, (byte)0x00,  
            (byte)0x00, (byte)0x00, (byte)0x75, (byte)0x00,  
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,  
            (byte)0x00, (byte)0x7d, (byte)0x7e, (byte)0x00,  
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,  
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,  
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,  
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,  
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,  
            (byte)0x00, (byte)0x00, (byte)0x8c, (byte)0x8d,  
            (byte)0x8e, (byte)0x8f, (byte)0x90, (byte)0x91,  
            (byte)0x92, (byte)0x93, (byte)0x94, (byte)0x95,  
            (byte)0x81, (byte)0x82, (byte)0x83, (byte)0x84,  
            (byte)0x85, (byte)0x86, (byte)0x87, (byte)0x88,  
            (byte)0x89, (byte)0x8a, (byte)0x8c, (byte)0x8d,  
            (byte)0x8e, (byte)0x8f, (byte)0x90, (byte)0x91,  
            (byte)0x92, (byte)0x93, (byte)0x94, (byte)0x95,  
            (byte)0xe8, (byte)0x00, (byte)0x00, (byte)0x00,  
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,  
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,  
            (byte)0x00, (byte)0xe8, (byte)0xd8, (byte)0x00,  
            (byte)0x00, (byte)0xc4, (byte)0xc6, (byte)0x00,  
            (byte)0x00, (byte)0xf0, (byte)0x00, (byte)0x00,  
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,  
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0xdc,  
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,  
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,  
            (byte)0x00, (byte)0x00, (byte)0x00               
        };
        public boolean isLegalReplacement(byte[] repl) {
            return true;
        }
    }
}
