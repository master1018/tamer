public class IBM943C_OLD extends Charset implements HistoricallyNamedCharset
{
    public IBM943C_OLD() {
        super("x-IBM943C_OLD", null);
    }
    public String historicalName() {
        return "Cp943_OLDC";
    }
    public boolean contains(Charset cs) {
        return ((cs.name().equals("US-ASCII"))
                || (cs instanceof IBM943C_OLD));
    }
    public CharsetDecoder newDecoder() {
        return new Decoder(this);
    }
    public CharsetEncoder newEncoder() {
        return new Encoder(this);
    }
    private static class Decoder extends IBM943_OLD.Decoder {
        protected static final String singleByteToChar;
        static {
          String indexs = "";
          for (char c = '\0'; c < '\u0080'; ++c) indexs += c;
              singleByteToChar = indexs +
                                 IBM943_OLD.Decoder.singleByteToChar.substring(indexs.length());
        }
        public Decoder(Charset cs) {
            super(cs, singleByteToChar);
        }
    }
    private static class Encoder extends IBM943_OLD.Encoder {
        protected static final short index1[];
        protected static final String index2a;
        protected static final int shift = 6;
        static {
            String indexs = "";
            for (char c = '\0'; c < '\u0080'; ++c) indexs += c;
                index2a = IBM943_OLD.Encoder.index2a + indexs;
            int o = IBM943_OLD.Encoder.index2a.length() + 15000;
            index1 = new short[IBM943_OLD.Encoder.index1.length];
            System.arraycopy(IBM943_OLD.Encoder.index1,
                             0,
                             index1,
                             0,
                             IBM943_OLD.Encoder.index1.length);
            for (int i = 0; i * (1<<shift) < 128; ++i) {
                index1[i] = (short)(o + i * (1<<shift));
            }
        }
        public Encoder(Charset cs) {
            super(cs, index1, index2a);
        }
    }
}
