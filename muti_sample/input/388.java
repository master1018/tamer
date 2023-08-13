public class X11GBK_OLD extends Charset {
    public X11GBK_OLD () {
        super("X11GBK-OLD", null);
    }
    public CharsetEncoder newEncoder() {
        return new Encoder(this);
    }
    public CharsetDecoder newDecoder() {
        return new GBK_OLD.Decoder(this);
    }
    public boolean contains(Charset cs) {
        return cs instanceof X11GBK_OLD;
    }
    private class Encoder extends GBK_OLD.Encoder {
        public Encoder(Charset cs) {
            super(cs);
        }
        public boolean canEncode(char ch){
            if (ch < 0x80) return false;
            return super.canEncode(ch);
        }
    }
}
