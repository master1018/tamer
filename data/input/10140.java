public class TestNPE {
    private static byte[] BYTES = new byte[16];
    static {
        new SecureRandom().nextBytes(BYTES);
    }
    public static void main(String[] args) throws Exception {
        NullCipher nc = new NullCipher();
        nc.init(Cipher.ENCRYPT_MODE, (Certificate) null);
        nc.init(Cipher.ENCRYPT_MODE, (Certificate) null, (SecureRandom) null);
        nc.init(Cipher.ENCRYPT_MODE, (Key) null);
        nc.init(Cipher.ENCRYPT_MODE, (Key) null, (AlgorithmParameters) null);
        nc.init(Cipher.ENCRYPT_MODE, (Key) null, (AlgorithmParameterSpec) null);
        nc.init(Cipher.ENCRYPT_MODE, (Key) null, (AlgorithmParameterSpec) null,
            (SecureRandom) null);
        nc.init(Cipher.ENCRYPT_MODE, (Key) null, (AlgorithmParameters) null,
            (SecureRandom) null);
        nc.init(Cipher.ENCRYPT_MODE, (Key) null, (SecureRandom) null);
        if (nc.getBlockSize() != 1) {
            throw new Exception("Error with getBlockSize()");
        }
        byte[] out = nc.update(BYTES);
        if (!Arrays.equals(out, BYTES)) {
            throw new Exception("Error with update(byte[])");
        }
        out = nc.update(BYTES, 0, BYTES.length);
        if (!Arrays.equals(out, BYTES)) {
            throw new Exception("Error with update(byte[], int, int)");
        }
        if (nc.update(BYTES, 0, BYTES.length, out) != BYTES.length) {
            throw new Exception("Error with update(byte[], int, int, byte[])");
        }
        if (nc.update(BYTES, 0, BYTES.length, out, 0) != BYTES.length) {
            throw new Exception(
                "Error with update(byte[], int, int, byte[], int)");
        }
        if (nc.doFinal() != null) {
            throw new Exception("Error with doFinal()");
        }
        if (nc.doFinal(out, 0) != 0) {
             throw new Exception("Error with doFinal(byte[], 0)");
        }
        out = nc.doFinal(BYTES);
        if (!Arrays.equals(out, BYTES)) {
            throw new Exception("Error with doFinal(byte[])");
        }
        out = nc.doFinal(BYTES, 0, BYTES.length);
        if (!Arrays.equals(out, BYTES)) {
            throw new Exception("Error with doFinal(byte[], int, int)");
        }
        if (nc.doFinal(BYTES, 0, BYTES.length, out) != BYTES.length) {
            throw new Exception(
                "Error with doFinal(byte[], int, int, byte[])");
        }
        if (nc.doFinal(BYTES, 0, BYTES.length, out, 0) != BYTES.length) {
            throw new Exception(
                "Error with doFinal(byte[], int, int, byte[], int)");
        }
        if (nc.getExemptionMechanism() != null) {
            throw new Exception("Error with getExemptionMechanism()");
        }
        if (nc.getOutputSize(5) != 5) {
            throw new Exception("Error with getOutputSize()");
        }
        if (nc.getIV() == null) { 
            throw new Exception("Error with getIV()");
        }
        if (nc.getParameters() != null) {
            throw new Exception("Error with getParameters()");
        }
        if (nc.getAlgorithm() != null) {
            throw new Exception("Error with getAlgorithm()");
        }
        if (nc.getProvider() != null) { 
            throw new Exception("Error with getProvider()");
        }
        System.out.println("Test Done");
    }
}
