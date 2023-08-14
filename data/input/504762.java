public class RC5ParameterSpec implements AlgorithmParameterSpec {
    private final int version;
    private final int rounds;
    private final int wordSize;
    private final byte[] iv;
    public RC5ParameterSpec(int version, int rounds, int wordSize) {
        this.version = version;
        this.rounds = rounds;
        this.wordSize = wordSize;
        this.iv = null;
    }
    public RC5ParameterSpec(int version, int rounds, int wordSize, byte[] iv) {
        if (iv == null) {
            throw new IllegalArgumentException(Messages.getString("crypto.31")); 
        }
        if (iv.length < 2 * (wordSize / 8)) {
            throw new IllegalArgumentException(
                    Messages.getString("crypto.32")); 
        }
        this.version = version;
        this.rounds = rounds;
        this.wordSize = wordSize;
        this.iv = new byte[2*(wordSize/8)];
        System.arraycopy(iv, 0, this.iv, 0, 2*(wordSize/8));
    }
    public RC5ParameterSpec(int version, int rounds,
                                int wordSize, byte[] iv, int offset) {
        if (iv == null) {
            throw new IllegalArgumentException(Messages.getString("crypto.31")); 
        }
        if (offset < 0) {
            throw new ArrayIndexOutOfBoundsException(Messages.getString("crypto.33")); 
        }
        if (iv.length - offset < 2 * (wordSize / 8)) {
            throw new IllegalArgumentException(
                    Messages.getString("crypto.34")); 
        }
        this.version = version;
        this.rounds = rounds;
        this.wordSize = wordSize;
        this.iv = new byte[offset+2*(wordSize/8)];
        System.arraycopy(iv, offset, this.iv, 0, 2*(wordSize/8));
    }
    public int getVersion() {
        return version;
    }
    public int getRounds() {
        return rounds;
    }
    public int getWordSize() {
        return wordSize;
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
        if (!(obj instanceof RC5ParameterSpec)) {
            return false;
        }
        RC5ParameterSpec ps = (RC5ParameterSpec) obj;
        return (version == ps.version)
            && (rounds == ps.rounds)
            && (wordSize == ps.wordSize)
            && (Arrays.equals(iv, ps.iv));
    }
    @Override
    public int hashCode() {
        int result = version + rounds + wordSize;
        if (iv == null) {
            return result;
        }
        for (byte element : iv) {
            result += element & 0xFF;
        }
        return result;
    }
}
