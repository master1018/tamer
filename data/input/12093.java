public class KerberosFlags {
    BitArray bits;
    protected static final int BITS_PER_UNIT = 8;
    public KerberosFlags(int length) throws IllegalArgumentException {
        bits = new BitArray(length);
    }
    public KerberosFlags(int length, byte[] a) throws IllegalArgumentException {
        bits = new BitArray(length, a);
        if (length != Krb5.KRB_FLAGS_MAX+1) {
            bits = new BitArray(Arrays.copyOf(bits.toBooleanArray(), Krb5.KRB_FLAGS_MAX+1));
        }
    }
    public KerberosFlags(boolean[] bools) {
        bits = new BitArray((bools.length==Krb5.KRB_FLAGS_MAX+1)?
            bools:
            Arrays.copyOf(bools, Krb5.KRB_FLAGS_MAX+1));
    }
    public void set(int index, boolean value) {
        bits.set(index, value);
    }
    public boolean get(int index) {
        return bits.get(index);
    }
    public boolean[] toBooleanArray() {
        return bits.toBooleanArray();
    }
    public byte[] asn1Encode() throws IOException {
        DerOutputStream out = new DerOutputStream();
        out.putUnalignedBitString(bits);
        return out.toByteArray();
    }
    public String toString() {
        return bits.toString();
    }
}
