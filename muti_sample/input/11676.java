public class JIS_X_0208
    extends Charset
{
    public JIS_X_0208() {
        super("x-JIS0208", ExtendedCharsets.aliasesFor("x-JIS0208"));
    }
    public boolean contains(Charset cs) {
        return (cs instanceof JIS_X_0208);
    }
    public CharsetDecoder newDecoder() {
        return new Decoder(this);
    }
    public CharsetEncoder newEncoder() {
        return new JIS_X_0208_Encoder(this);
    }
    private static class Decoder extends JIS_X_0208_Decoder {
        protected char decodeSingle(int b) {
            return DoubleByteDecoder.REPLACE_CHAR;
        }
        public Decoder(Charset cs) {
            super(cs);
        }
    }
}
