public class RC5ParameterSpec implements AlgorithmParameterSpec {
    private byte[] iv = null;
    private int version;
    private int rounds;
    private int wordSize; 
    public RC5ParameterSpec(int version, int rounds, int wordSize) {
        this.version = version;
        this.rounds = rounds;
        this.wordSize = wordSize;
    }
    public RC5ParameterSpec(int version, int rounds, int wordSize, byte[] iv) {
        this(version, rounds, wordSize, iv, 0);
    }
    public RC5ParameterSpec(int version, int rounds, int wordSize,
                            byte[] iv, int offset) {
        this.version = version;
        this.rounds = rounds;
        this.wordSize = wordSize;
        if (iv == null) throw new IllegalArgumentException("IV missing");
        int blockSize = (wordSize / 8) * 2;
        if (iv.length - offset < blockSize) {
            throw new IllegalArgumentException("IV too short");
        }
        this.iv = new byte[blockSize];
        System.arraycopy(iv, offset, this.iv, 0, blockSize);
    }
    public int getVersion() {
        return this.version;
    }
    public int getRounds() {
        return this.rounds;
    }
    public int getWordSize() {
        return this.wordSize;
    }
    public byte[] getIV() {
        return (iv == null? null:(byte[])iv.clone());
    }
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof RC5ParameterSpec)) {
            return false;
        }
        RC5ParameterSpec other = (RC5ParameterSpec) obj;
        return ((version == other.version) &&
                (rounds == other.rounds) &&
                (wordSize == other.wordSize) &&
                java.util.Arrays.equals(iv, other.iv));
    }
    public int hashCode() {
        int retval = 0;
        if (iv != null) {
            for (int i = 1; i < iv.length; i++) {
                retval += iv[i] * i;
            }
        }
        retval += (version + rounds + wordSize);
        return retval;
    }
}
