public class MS950_HKSCS_XP extends Charset
{
    public MS950_HKSCS_XP() {
        super("x-MS950-HKSCS-XP", ExtendedCharsets.aliasesFor("x-MS950-HKSCS-XP"));
    }
    public boolean contains(Charset cs) {
        return ((cs.name().equals("US-ASCII"))
                || (cs instanceof MS950)
                || (cs instanceof MS950_HKSCS_XP));
    }
    public CharsetDecoder newDecoder() {
        return new Decoder(this);
    }
    public CharsetEncoder newEncoder() {
        return new Encoder(this);
    }
    static class Decoder extends HKSCS.Decoder {
        private static DoubleByte.Decoder ms950 =
            (DoubleByte.Decoder)new MS950().newDecoder();
        private static char[][] b2cBmp = new char[0x100][];
        static {
            initb2c(b2cBmp, HKSCS_XPMapping.b2cBmpStr);
        }
        public char decodeDoubleEx(int b1, int b2) {
            return UNMAPPABLE_DECODING;
        }
        private Decoder(Charset cs) {
            super(cs, ms950, b2cBmp, null);
        }
    }
    private static class Encoder extends HKSCS.Encoder {
        private static DoubleByte.Encoder ms950 =
            (DoubleByte.Encoder)new MS950().newEncoder();
        static char[][] c2bBmp = new char[0x100][];
        static {
            initc2b(c2bBmp, HKSCS_XPMapping.b2cBmpStr, null);
        }
        public int encodeSupp(int cp) {
            return UNMAPPABLE_ENCODING;
        }
        private Encoder(Charset cs) {
            super(cs, ms950, c2bBmp, null);
        }
    }
}
