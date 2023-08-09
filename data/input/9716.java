public final class ATR implements java.io.Serializable {
    private static final long serialVersionUID = 6695383790847736493L;
    private byte[] atr;
    private transient int startHistorical, nHistorical;
    public ATR(byte[] atr) {
        this.atr = atr.clone();
        parse();
    }
    private void parse() {
        if (atr.length < 2) {
            return;
        }
        if ((atr[0] != 0x3b) && (atr[0] != 0x3f)) {
            return;
        }
        int t0 = (atr[1] & 0xf0) >> 4;
        int n = atr[1] & 0xf;
        int i = 2;
        while ((t0 != 0) && (i < atr.length)) {
            if ((t0 & 1) != 0) {
                i++;
            }
            if ((t0 & 2) != 0) {
                i++;
            }
            if ((t0 & 4) != 0) {
                i++;
            }
            if ((t0 & 8) != 0) {
                if (i >= atr.length) {
                    return;
                }
                t0 = (atr[i++] & 0xf0) >> 4;
            } else {
                t0 = 0;
            }
        }
        int k = i + n;
        if ((k == atr.length) || (k == atr.length - 1)) {
            startHistorical = i;
            nHistorical = n;
        }
    }
    public byte[] getBytes() {
        return atr.clone();
    }
    public byte[] getHistoricalBytes() {
        byte[] b = new byte[nHistorical];
        System.arraycopy(atr, startHistorical, b, 0, nHistorical);
        return b;
    }
    public String toString() {
        return "ATR: " + atr.length + " bytes";
    }
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof ATR == false) {
            return false;
        }
        ATR other = (ATR)obj;
        return Arrays.equals(this.atr, other.atr);
    }
    public int hashCode() {
        return Arrays.hashCode(atr);
    }
    private void readObject(java.io.ObjectInputStream in)
            throws java.io.IOException, ClassNotFoundException {
        atr = (byte[])in.readUnshared();
        parse();
    }
}
