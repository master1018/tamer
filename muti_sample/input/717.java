public class IBM942C extends Charset implements HistoricallyNamedCharset
{
    public IBM942C() {
        super("x-IBM942C", ExtendedCharsets.aliasesFor("x-IBM942C"));
    }
    public String historicalName() {
        return "Cp942C";
    }
    public boolean contains(Charset cs) {
        return ((cs.name().equals("US-ASCII"))
                || (cs instanceof IBM942C));
    }
    public CharsetDecoder newDecoder() {
        return new DoubleByte.Decoder(this,
                                      IBM942.b2c,
                                      b2cSB,
                                      0x40,
                                      0xfc);
    }
    public CharsetEncoder newEncoder() {
        return new DoubleByte.Encoder(this, c2b, c2bIndex);
    }
    final static char[] b2cSB;
    final static char[] c2b;
    final static char[] c2bIndex;
    static {
        IBM942.initb2c();
        b2cSB = Arrays.copyOf(IBM942.b2cSB, IBM942.b2cSB.length);
        b2cSB[0x1a] = 0x1a;
        b2cSB[0x1c] = 0x1c;
        b2cSB[0x5c] = 0x5c;
        b2cSB[0x7e] = 0x7e;
        b2cSB[0x7f] = 0x7f;
        IBM942.initc2b();
        c2b = Arrays.copyOf(IBM942.c2b, IBM942.c2b.length);
        c2bIndex = Arrays.copyOf(IBM942.c2bIndex, IBM942.c2bIndex.length);
        c2b[c2bIndex[0] + 0x1a] = 0x1a;
        c2b[c2bIndex[0] + 0x1c] = 0x1c;
        c2b[c2bIndex[0] + 0x5c] = 0x5c;
        c2b[c2bIndex[0] + 0x7e] = 0x7e;
        c2b[c2bIndex[0] + 0x7f] = 0x7f;
    }
}
