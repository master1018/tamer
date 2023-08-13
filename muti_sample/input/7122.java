public class RC2ParameterSpec implements AlgorithmParameterSpec {
    private byte[] iv = null;
    private int effectiveKeyBits;
    public RC2ParameterSpec(int effectiveKeyBits) {
        this.effectiveKeyBits = effectiveKeyBits;
    }
    public RC2ParameterSpec(int effectiveKeyBits, byte[] iv) {
        this(effectiveKeyBits, iv, 0);
    }
    public RC2ParameterSpec(int effectiveKeyBits, byte[] iv, int offset) {
        this.effectiveKeyBits = effectiveKeyBits;
        if (iv == null) throw new IllegalArgumentException("IV missing");
        int blockSize = 8;
        if (iv.length - offset < blockSize) {
            throw new IllegalArgumentException("IV too short");
        }
        this.iv = new byte[blockSize];
        System.arraycopy(iv, offset, this.iv, 0, blockSize);
    }
    public int getEffectiveKeyBits() {
        return this.effectiveKeyBits;
    }
    public byte[] getIV() {
        return (iv == null? null:(byte[])iv.clone());
    }
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof RC2ParameterSpec)) {
            return false;
        }
        RC2ParameterSpec other = (RC2ParameterSpec) obj;
        return ((effectiveKeyBits == other.effectiveKeyBits) &&
                java.util.Arrays.equals(iv, other.iv));
    }
    public int hashCode() {
        int retval = 0;
        if (iv != null) {
            for (int i = 1; i < iv.length; i++) {
                retval += iv[i] * i;
            }
        }
        return (retval += effectiveKeyBits);
    }
}
