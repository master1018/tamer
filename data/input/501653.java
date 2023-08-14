public class RC2ParameterSpec implements AlgorithmParameterSpec {
    private final int effectiveKeyBits;
    private final byte[] iv;
    public RC2ParameterSpec(int effectiveKeyBits) {
        this.effectiveKeyBits = effectiveKeyBits;
        iv = null;
    }
    public RC2ParameterSpec(int effectiveKeyBits, byte[] iv) {
        if (iv == null) {
            throw new IllegalArgumentException(Messages.getString("crypto.31")); 
        }
        if (iv.length < 8) {
            throw new IllegalArgumentException(Messages.getString("crypto.41")); 
        }
        this.effectiveKeyBits = effectiveKeyBits;
        this.iv = new byte[8];
        System.arraycopy(iv, 0, this.iv, 0, 8);
    }
    public RC2ParameterSpec(int effectiveKeyBits, byte[] iv, int offset) {
        if (iv == null) {
            throw new IllegalArgumentException(Messages.getString("crypto.31")); 
        }
        if (iv.length - offset < 8) {
            throw new IllegalArgumentException(Messages.getString("crypto.41")); 
        }
        this.effectiveKeyBits = effectiveKeyBits;
        this.iv = new byte[8];
        System.arraycopy(iv, offset, this.iv, 0, 8);
    }
    public int getEffectiveKeyBits() {
        return effectiveKeyBits;
    }
    public byte[] getIV() {
        if (iv == null) {
            return null;
        }
        byte[] result = new byte[iv.length];
        System.arraycopy(iv, 0, result, 0, iv.length);
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof RC2ParameterSpec)) {
            return false;
        }
        RC2ParameterSpec ps = (RC2ParameterSpec) obj;
        return (effectiveKeyBits == ps.effectiveKeyBits)
            && (Arrays.equals(iv, ps.iv));
    }
    @Override
    public int hashCode() {
        int result = effectiveKeyBits;
        if (iv == null) {
            return result;
        }
        for (byte element : iv) {
            result += element;
        }
        return result;
    }
}
