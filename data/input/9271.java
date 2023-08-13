public class MS932_OLD extends Charset implements HistoricallyNamedCharset
{
    public MS932_OLD() {
        super("windows-31j-OLD", null);
    }
    public String historicalName() {
        return "MS932";
    }
    public boolean contains(Charset cs) {
        return ((cs.name().equals("US-ASCII"))
                || (cs instanceof JIS_X_0201)
                || (cs instanceof MS932_OLD));
    }
    public CharsetDecoder newDecoder() {
        return new Decoder(this);
    }
    public CharsetEncoder newEncoder() {
        return new Encoder(this);
    }
    private static class Decoder extends MS932DB.Decoder
    {
        JIS_X_0201.Decoder jisDec0201;
        private Decoder(Charset cs) {
            super(cs);
            jisDec0201 = new JIS_X_0201.Decoder(cs);
        }
        protected char decodeSingle(int b) {
            if ((b & 0xFF80) == 0) {
                return (char)b;
            }
            return jisDec0201.decode(b);
        }
        public CoderResult decodeLoop(ByteBuffer src, CharBuffer dst) {
            return super.decodeLoop(src, dst);
        }
        public void implReset() {
            super.implReset();
        }
        public CoderResult implFlush(CharBuffer out) {
            return super.implFlush(out);
        }
    }
    private static class Encoder extends MS932DB.Encoder {
        private JIS_X_0201.Encoder jisEnc0201;
        private Encoder(Charset cs) {
            super(cs);
            jisEnc0201 = new JIS_X_0201.Encoder(cs);
        }
        protected int encodeSingle(char inputChar) {
            byte b;
            if ((inputChar & 0xFF80) == 0) {
                return ((byte)inputChar);
            }
            if ((b = jisEnc0201.encode(inputChar)) == 0)
                return -1;
            else
                return b;
        }
    }
}
