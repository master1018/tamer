public class COMPOUND_TEXT extends Charset {
    public COMPOUND_TEXT () {
        super("x-COMPOUND_TEXT",
              ExtendedCharsets.aliasesFor("x-COMPOUND_TEXT"));
    }
    public CharsetEncoder newEncoder() {
        return new COMPOUND_TEXT_Encoder(this);
    }
    public CharsetDecoder newDecoder() {
        return new COMPOUND_TEXT_Decoder(this);
    }
    public boolean contains(Charset cs) {
        return cs instanceof COMPOUND_TEXT;
    }
}
