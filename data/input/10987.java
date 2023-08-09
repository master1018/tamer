public final class PRNG extends SecureRandomSpi
    implements java.io.Serializable {
    private static native byte[] generateSeed(int length, byte[] seed);
    public PRNG() {
    }
    protected void engineSetSeed(byte[] seed) {
        if (seed != null) {
            generateSeed(-1, seed);
        }
    }
    protected void engineNextBytes(byte[] bytes) {
        if (bytes != null) {
            if (generateSeed(0, bytes) == null) {
                throw new ProviderException("Error generating random bytes");
            }
        }
    }
    protected byte[] engineGenerateSeed(int numBytes) {
        byte[] seed = generateSeed(numBytes, null);
        if (seed == null) {
            throw new ProviderException("Error generating seed bytes");
        }
        return seed;
    }
}
