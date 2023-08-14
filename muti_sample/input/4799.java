public class ISO2022_CN_CNS extends ISO2022 implements HistoricallyNamedCharset
{
    public ISO2022_CN_CNS() {
        super("x-ISO-2022-CN-CNS", ExtendedCharsets.aliasesFor("x-ISO-2022-CN-CNS"));
    }
    public boolean contains(Charset cs) {
        return ((cs instanceof EUC_TW) ||
                (cs.name().equals("US-ASCII")) ||
                (cs instanceof ISO2022_CN_CNS));
    }
    public String historicalName() {
        return "ISO2022CN_CNS";
    }
    public CharsetDecoder newDecoder() {
        return new ISO2022_CN.Decoder(this);
    }
    public CharsetEncoder newEncoder() {
        return new Encoder(this);
    }
    private static class Encoder extends ISO2022.Encoder {
        public Encoder(Charset cs)
        {
            super(cs);
            SODesig = "$)G";
            SS2Desig = "$*H";
            SS3Desig = "$+I";
            try {
                Charset cset = Charset.forName("EUC_TW"); 
                ISOEncoder = cset.newEncoder();
            } catch (Exception e) { }
        }
        private byte[] bb = new byte[4];
        public boolean canEncode(char c) {
            int n = 0;
            return (c <= '\u007f' ||
                    (n = ((EUC_TW.Encoder)ISOEncoder).toEUC(c, bb)) == 2 ||
                    (n == 4 && bb[0] == SS2 &&
                     (bb[1] == PLANE2 || bb[1] == PLANE3)));
        }
        public boolean isLegalReplacement(byte[] repl) {
            return (repl.length == 1 && repl[0] == (byte) 0x3f);
        }
    }
}
