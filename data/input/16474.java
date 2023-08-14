public class ISO2022_CN_GB extends ISO2022 implements HistoricallyNamedCharset
{
    public ISO2022_CN_GB() {
        super("x-ISO-2022-CN-GB",
               ExtendedCharsets.aliasesFor("x-ISO-2022-CN-GB"));
    }
    public boolean contains(Charset cs) {
        return ((cs instanceof EUC_CN) ||
                (cs.name().equals("US-ASCII")) ||
                (cs instanceof ISO2022_CN_GB));
    }
    public String historicalName() {
        return "ISO2022CN_GB";
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
            SODesig = "$)A";
            try {
                Charset cset = Charset.forName("EUC_CN"); 
                ISOEncoder = cset.newEncoder();
            } catch (Exception e) { }
        }
        public boolean isLegalReplacement(byte[] repl) {
            return (repl.length == 1 && repl[0] == (byte) 0x3f);
        }
    }
}
