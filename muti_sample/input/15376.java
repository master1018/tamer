public class X11JIS0201 extends Charset {
    public X11JIS0201 () {
        super("X11JIS0201", null);
    }
    public CharsetEncoder newEncoder() {
        return new Encoder(this);
    }
    public CharsetDecoder newDecoder() {
        return new JIS_X_0201.Decoder(this);
    }
    public boolean contains(Charset cs) {
        return cs instanceof X11JIS0201;
    }
    private class Encoder extends JIS_X_0201.Encoder {
        public Encoder(Charset cs) {
            super(cs);
        }
        public boolean canEncode(char c){
            if ((c >= 0xff61 && c <= 0xff9f)
                || c == 0x203e
                || c == 0xa5) {
                return true;
            }
            return false;
        }
    }
}
