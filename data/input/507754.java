public class RandomImpl extends SecureRandomSpi {
    public static boolean runEngineGenerateSeed = false;
    public static boolean runEngineNextBytes = false;
    public static boolean runEngineSetSeed = false;
    protected void engineSetSeed(byte[] seed) {
        runEngineSetSeed = true;
    }
    protected void engineNextBytes(byte[] bytes) {
        runEngineNextBytes = true;
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte)(i + 0xF1);
        }
    }
    protected byte[] engineGenerateSeed(int numBytes) {
        runEngineGenerateSeed = true;
        byte[] b = new byte[numBytes];
        for (int i = 0; i < b.length; i++) {
            b[i] = (byte)i;
        }
        return b;
    }
}
