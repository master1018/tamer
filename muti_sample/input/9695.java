class UTF_16 extends Unicode
{
    public UTF_16() {
        super("UTF-16", StandardCharsets.aliases_UTF_16);
    }
    public String historicalName() {
        return "UTF-16";
    }
    public CharsetDecoder newDecoder() {
        return new Decoder(this);
    }
    public CharsetEncoder newEncoder() {
        return new Encoder(this);
    }
    private static class Decoder extends UnicodeDecoder {
        public Decoder(Charset cs) {
            super(cs, NONE);
        }
    }
    private static class Encoder extends UnicodeEncoder {
        public Encoder(Charset cs) {
            super(cs, BIG, true);
        }
    }
}
