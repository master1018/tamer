public class BitArray {
    private byte[] repn;
    private int length;
    private static final int BITS_PER_UNIT = 8;
    private static int subscript(int idx) {
        return idx / BITS_PER_UNIT;
    }
    private static int position(int idx) { 
        return 1 << (BITS_PER_UNIT - 1 - (idx % BITS_PER_UNIT));
    }
    public BitArray(int length) throws IllegalArgumentException {
        if (length < 0) {
            throw new IllegalArgumentException("Negative length for BitArray");
        }
        this.length = length;
        repn = new byte[(length + BITS_PER_UNIT - 1)/BITS_PER_UNIT];
    }
    public BitArray(int length, byte[] a) throws IllegalArgumentException {
        if (length < 0) {
            throw new IllegalArgumentException("Negative length for BitArray");
        }
        if (a.length * BITS_PER_UNIT < length) {
            throw new IllegalArgumentException("Byte array too short to represent " +
                                               "bit array of given length");
        }
        this.length = length;
        int repLength = ((length + BITS_PER_UNIT - 1)/BITS_PER_UNIT);
        int unusedBits = repLength*BITS_PER_UNIT - length;
        byte bitMask = (byte) (0xFF << unusedBits);
        repn = new byte[repLength];
        System.arraycopy(a, 0, repn, 0, repLength);
        if (repLength > 0) {
            repn[repLength - 1] &= bitMask;
        }
    }
    public BitArray(boolean[] bits) {
        length = bits.length;
        repn = new byte[(length + 7)/8];
        for (int i=0; i < length; i++) {
            set(i, bits[i]);
        }
    }
    private BitArray(BitArray ba) {
        length = ba.length;
        repn = ba.repn.clone();
    }
    public boolean get(int index) throws ArrayIndexOutOfBoundsException {
        if (index < 0 || index >= length) {
            throw new ArrayIndexOutOfBoundsException(Integer.toString(index));
        }
        return (repn[subscript(index)] & position(index)) != 0;
    }
    public void set(int index, boolean value)
    throws ArrayIndexOutOfBoundsException {
        if (index < 0 || index >= length) {
            throw new ArrayIndexOutOfBoundsException(Integer.toString(index));
        }
        int idx = subscript(index);
        int bit = position(index);
        if (value) {
            repn[idx] |= bit;
        } else {
            repn[idx] &= ~bit;
        }
    }
    public int length() {
        return length;
    }
    public byte[] toByteArray() {
        return repn.clone();
    }
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || !(obj instanceof BitArray)) return false;
        BitArray ba = (BitArray) obj;
        if (ba.length != length) return false;
        for (int i = 0; i < repn.length; i += 1) {
            if (repn[i] != ba.repn[i]) return false;
        }
        return true;
    }
    public boolean[] toBooleanArray() {
        boolean[] bits = new boolean[length];
        for (int i=0; i < length; i++) {
            bits[i] = get(i);
        }
        return bits;
    }
    public int hashCode() {
        int hashCode = 0;
        for (int i = 0; i < repn.length; i++)
            hashCode = 31*hashCode + repn[i];
        return hashCode ^ length;
    }
    public Object clone() {
        return new BitArray(this);
    }
    private static final byte[][] NYBBLE = {
        { (byte)'0',(byte)'0',(byte)'0',(byte)'0'},
        { (byte)'0',(byte)'0',(byte)'0',(byte)'1'},
        { (byte)'0',(byte)'0',(byte)'1',(byte)'0'},
        { (byte)'0',(byte)'0',(byte)'1',(byte)'1'},
        { (byte)'0',(byte)'1',(byte)'0',(byte)'0'},
        { (byte)'0',(byte)'1',(byte)'0',(byte)'1'},
        { (byte)'0',(byte)'1',(byte)'1',(byte)'0'},
        { (byte)'0',(byte)'1',(byte)'1',(byte)'1'},
        { (byte)'1',(byte)'0',(byte)'0',(byte)'0'},
        { (byte)'1',(byte)'0',(byte)'0',(byte)'1'},
        { (byte)'1',(byte)'0',(byte)'1',(byte)'0'},
        { (byte)'1',(byte)'0',(byte)'1',(byte)'1'},
        { (byte)'1',(byte)'1',(byte)'0',(byte)'0'},
        { (byte)'1',(byte)'1',(byte)'0',(byte)'1'},
        { (byte)'1',(byte)'1',(byte)'1',(byte)'0'},
        { (byte)'1',(byte)'1',(byte)'1',(byte)'1'}
    };
    private static final int BYTES_PER_LINE = 8;
    public String toString() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (int i = 0; i < repn.length - 1; i++) {
            out.write(NYBBLE[(repn[i] >> 4) & 0x0F], 0, 4);
            out.write(NYBBLE[repn[i] & 0x0F], 0, 4);
            if (i % BYTES_PER_LINE == BYTES_PER_LINE - 1) {
                out.write('\n');
            } else {
                out.write(' ');
            }
        }
        for (int i = BITS_PER_UNIT * (repn.length - 1); i < length; i++) {
            out.write(get(i) ? '1' : '0');
        }
        return new String(out.toByteArray());
    }
    public BitArray truncate() {
        for (int i=length-1; i>=0; i--) {
            if (get(i)) {
                return new BitArray(i+1, Arrays.copyOf(repn, (i + BITS_PER_UNIT)/BITS_PER_UNIT));
            }
        }
        return new BitArray(1);
    }
}
