public class IBM834 extends Charset
{
    public IBM834() {
        super("x-IBM834", ExtendedCharsets.aliasesFor("x-IBM834"));
    }
    public boolean contains(Charset cs) {
        return (cs instanceof IBM834);
    }
    public CharsetDecoder newDecoder() {
        IBM933.initb2c();
        return new DoubleByte.Decoder_EBCDIC_DBCSONLY(
            this, IBM933.b2c, 0x40, 0xfe);  
    }
    public CharsetEncoder newEncoder() {
        IBM933.initc2b();
        return new Encoder(this);
    }
    protected static class Encoder extends DoubleByte.Encoder_EBCDIC_DBCSONLY {
        public Encoder(Charset cs) {
            super(cs, new byte[] {(byte)0xfe, (byte)0xfe},
                  IBM933.c2b, IBM933.c2bIndex);
        }
        public int encodeChar(char ch) {
            int bb = super.encodeChar(ch);
            if (bb == UNMAPPABLE_ENCODING) {
                if (ch == '\u00b7') {
                    return 0x4143;
                } else if (ch == '\u00ad') {
                    return 0x4148;
                } else if (ch == '\u2015') {
                    return 0x4149;
                } else if (ch == '\u223c') {
                    return 0x42a1;
                } else if (ch == '\uff5e') {
                    return 0x4954;
                } else if (ch == '\u2299') {
                    return 0x496f;
                }
            }
            return bb;
        }
        public boolean isLegalReplacement(byte[] repl) {
            if (repl.length == 2 &&
                repl[0] == (byte)0xfe && repl[1] == (byte)0xfe)
                return true;
            return super.isLegalReplacement(repl);
        }
    }
}
