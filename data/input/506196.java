public class PBEParameterSpec implements AlgorithmParameterSpec {
    private final byte[] salt;
    private final int iterationCount;
    public PBEParameterSpec(byte[] salt, int iterationCount) {
        if (salt == null) {
            throw new NullPointerException(Messages.getString("crypto.3B")); 
        }
        this.salt = new byte[salt.length];
        System.arraycopy(salt, 0, this.salt, 0, salt.length);
        this.iterationCount = iterationCount;
    }
    public byte[] getSalt() {
        byte[] result = new byte[salt.length];
        System.arraycopy(salt, 0, result, 0, salt.length);
        return result;
    }
    public int getIterationCount() {
        return iterationCount;
    }
}
