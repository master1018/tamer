public class PBEParameterSpec implements AlgorithmParameterSpec {
    private byte[] salt;
    private int iterationCount;
    public PBEParameterSpec(byte[] salt, int iterationCount) {
        this.salt = (byte[])salt.clone();
        this.iterationCount = iterationCount;
    }
    public byte[] getSalt() {
        return (byte[])this.salt.clone();
    }
    public int getIterationCount() {
        return this.iterationCount;
    }
}
