public class X11JIS0212 extends Charset {
    public X11JIS0212 () {
        super("X11JIS0212", null);
    }
    public CharsetEncoder newEncoder() {
        return new JIS_X_0212_Encoder(this);
    }
    public CharsetDecoder newDecoder() {
        return new JIS_X_0212_Decoder(this);
    }
    public boolean contains(Charset cs) {
        return cs instanceof X11JIS0212;
    }
}
